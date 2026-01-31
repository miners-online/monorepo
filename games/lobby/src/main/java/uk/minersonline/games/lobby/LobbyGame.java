package uk.minersonline.games.lobby;

import net.hollowcube.schem.Schematic;
import net.hollowcube.schem.reader.SchematicReader;
import net.hollowcube.schem.util.Rotation;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.dialog.DialogActionButton;
import net.minestom.server.dialog.DialogAfterAction;
import net.minestom.server.dialog.DialogMetadata;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.player.PlayerPreEatEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.world.DimensionType;
import uk.minersonline.games.game_materials.InstanceLock;
import uk.minersonline.games.game_materials.RemotePlayerData;
import uk.minersonline.games.game_materials.WorldID;
import uk.minersonline.games.server_bootstrap.game.Game;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

public class LobbyGame extends Game {
    public static final Tag<Boolean> SERVER_SELECTOR_TAG = Tag.Boolean("server_selector");

    private GlobalEventHandler geh;
    private Pos spawnPoint;
    private int spawnRadius = 1;
    private InstanceContainer instance;
    private InstanceLock instanceLock;
    private LobbySignHandler lobbySignHandler;
    private int worldId;

    @Override
    public void onInit() {
        geh = MinecraftServer.getGlobalEventHandler();
        RemotePlayerData.register();

        lobbySignHandler = new LobbySignHandler(this);
        BlockManager blockManager = MinecraftServer.getBlockManager();
        blockManager.registerHandler(Key.key("minecraft:sign"), () -> lobbySignHandler);

        spawnPoint = new Pos(0.5, 1, 0.5);

        InstanceManager manager = MinecraftServer.getInstanceManager();
        instance = manager.createInstanceContainer();
        instance.setChunkSupplier(LightingChunk::new);
        instance.setTimeRate(0);
        instance.setTime(12000);
        instanceLock = new InstanceLock(instance);
        worldId = WorldID.generateWorldId();

        // Preload chunks around spawn
        var chunks = new ArrayList<CompletableFuture<Chunk>>();
        ChunkRange.chunksInRange(0, 0, 32, (x, z) -> chunks.add(instance.loadChunk(x, z)));

        CompletableFuture.runAsync(() -> {
            CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
            // Load schematic
            try {
                String path = this.getServerConfig().getProperties().getProperty("SCHEMATIC_PATH");
                InputStream is;
                if (path == null) {
                    is = this.getClass().getClassLoader().getResourceAsStream("void_platform.schem");
                } else {
                    is = new FileInputStream(path);
                }

                Schematic schematic = SchematicReader.detecting().read(is.readAllBytes());
                double height = schematic.size().y() / 2;
                schematic.createBatch(Rotation.NONE).apply(instance, new Pos(0, 0+height, 0), (batch) -> {});
                BlockResult highestBlock = findHighestBlock(instance, 0, 0);
                if (highestBlock != null) {
                    spawnPoint = new Pos(0.5, highestBlock.y() + 1, 0.5);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            LightingChunk.relight(instance, instance.getChunks());
        });
    }

    @Override
    public void onStart() {
        geh.addListener(PlayerEntityInteractEvent.class, event -> {
            Entity entity = event.getTarget();
            Player player = event.getPlayer();
            if (entity.hasTag(LobbySignHandler.NPC_TAG)) {
                String serverName = entity.getTag(LobbySignHandler.NPC_TAG);
                this.getProxyMessageClient().sendTransfer(player.getUuid(), serverName);
            }
        });

        geh.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instance);
            player.setGameMode(GameMode.ADVENTURE);
            player.setRespawnPoint(spawnPoint);
            InstanceLock.lockPlayerBlock(player);
            InstanceLock.lockPlayerItem(player);
        });

        geh.addListener(PlayerPreEatEvent.class, event -> {
            ItemStack item = event.getItemStack();
            if (item.hasTag(SERVER_SELECTOR_TAG)) {
                event.setCancelled(true);
            }
        });

        geh.addListener(PlayerSwapItemEvent.class, event -> {
            ItemStack mainHand = event.getMainHandItem();
            ItemStack offHand = event.getOffHandItem();
            if (mainHand.hasTag(SERVER_SELECTOR_TAG) || offHand.hasTag(SERVER_SELECTOR_TAG)) {
                event.setCancelled(true);
            }
        });

        geh.addListener(PlayerUseItemEvent.class, event -> {
            Player player = event.getPlayer();
            ItemStack item = event.getItemStack();
            if (item.hasTag(SERVER_SELECTOR_TAG)) {
                event.setCancelled(true);

                // player.sendMessage(Component.text("World ID: " + worldId).color(NamedTextColor.GRAY));

                List<String> servers = List.of("survival", "creative", "modded-creative");
                List<DialogActionButton> inputs = new ArrayList<>();
                for (String server : servers) {
                    inputs.add(new DialogActionButton(
                        Component.text(server).color(NamedTextColor.YELLOW),
                        Component.text(server).color(NamedTextColor.YELLOW).append(
                            Component.text("\nONLINE\n").color(NamedTextColor.GREEN).append(
                                Component.text("\nPlayers: 3/10").color(NamedTextColor.WHITE)
                            )
                        ),
                        DialogActionButton.DEFAULT_WIDTH,
                        null
                    ));
                }

                player.showDialog(new Dialog.MultiAction(
                    new DialogMetadata(
                        Component.text("Server Selector").color(NamedTextColor.GOLD), 
                        Component.text("Servers"), 
                        true, 
                        false, 
                        DialogAfterAction.CLOSE, 
                        List.of(), 
                        List.of()
                    ),
                    inputs,
                    new DialogActionButton(Component.text("Close"), null, DialogActionButton.DEFAULT_WIDTH, null),
                    1 // columns
                ));
            }
        });

        geh.addListener(PlayerSpawnEvent.class, event -> {
            // Teleport to a random point within spawn radius
            final Player player = event.getPlayer();
            double offsetX = (Math.random() * 2 - 1) * spawnRadius;
            double offsetZ = (Math.random() * 2 - 1) * spawnRadius;
            player.teleport(spawnPoint.add(offsetX, 0, offsetZ));

            WorldID.sendWorldId(player, worldId);

            ItemStack lobbyCompass = ItemStack.of(Material.fromKey("minecraft:compass"), 1)
            .withTag(InstanceLock.ITEM_LOCK_BYPASS_TAG, true)
            .withTag(SERVER_SELECTOR_TAG, true)
            .withCustomName(Component.text("Server Selector").color(NamedTextColor.GOLD));
            
            player.getInventory().clear();
            player.getInventory().addItemStack(lobbyCompass);
        });
    }

    @Override
    public void onStop() {

    }

    public void setSpawn(Pos spawnPoint, int radius) {
        this.spawnPoint = spawnPoint;
        this.spawnRadius = radius;
    }

    /**
     * <p>Find the highest non-air block at the given x and z coordinates in the instance.</p>
     * @param instance Instance to search in
     * @param x X coordinate
     * @param z Z coordinate
     * @return A BlockResult containing the Y coordinate and Block, or null if no non-air block is found
     */
    public static @Nullable BlockResult findHighestBlock(InstanceContainer instance, int x, int z) {
        DimensionType dimensionType = instance.getCachedDimensionType();
        int highestY = dimensionType.maxY() - 1;
        for (int y = highestY; y >= dimensionType.minY(); y--) {
            Block block = instance.getBlock(x, y, z);
            if (!block.isAir()) {
                return new BlockResult(y, block);
            }
        }
        return null;
    }

    public static record BlockResult(int y, Block block) {
    }
}
