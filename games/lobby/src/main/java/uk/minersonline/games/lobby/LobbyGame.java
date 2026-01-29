package uk.minersonline.games.lobby;

import net.hollowcube.schem.Schematic;
import net.hollowcube.schem.reader.SchematicReader;
import net.hollowcube.schem.util.Rotation;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import uk.minersonline.games.game_materials.RemotePlayerData;
import uk.minersonline.games.server_bootstrap.game.Game;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

public class LobbyGame extends Game {
    private GlobalEventHandler geh;
    private Pos spawnPoint;
    private InstanceContainer instance;

    @Override
    public void onInit() {
        geh = MinecraftServer.getGlobalEventHandler();
        RemotePlayerData.register();
        LobbySignHandler.register();

        InstanceManager manager = MinecraftServer.getInstanceManager();
        instance = manager.createInstanceContainer();
        instance.setChunkSupplier(LightingChunk::new);
        instance.setTimeRate(0);
        instance.setTime(12000);

        // Preload chunks around spawn
        var chunks = new ArrayList<CompletableFuture<Chunk>>();
        ChunkRange.chunksInRange(0, 0, 32, (x, z) -> chunks.add(instance.loadChunk(x, z)));

        CompletableFuture.runAsync(() -> {
            CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
            // Load schematic
            try {
                String path = System.getenv("SCHEMATIC_PATH");
                InputStream is;
                if (path == null) {
                    is = this.getClass().getClassLoader().getResourceAsStream("void_platform.schem");
                } else {
                    is = new FileInputStream(path);
                }

                Schematic schematic = SchematicReader.detecting().read(is.readAllBytes());
                schematic.createBatch(Rotation.NONE).apply(instance, new Pos(0, 1, 0), (batch) -> {});
                spawnPoint = new Pos(0.5, 1, 0.5);
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
        geh.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instance);
            player.setGameMode(GameMode.CREATIVE);
            player.setRespawnPoint(spawnPoint);
        });
    }

    @Override
    public void onStop() {

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
