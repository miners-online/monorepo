package uk.minersonline.games.lobby;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import uk.minersonline.games.game_materials.blocks.SignBlockHandler;
import uk.minersonline.games.game_materials.entities.FakePlayer;

import java.util.Objects;

public class LobbySignHandler extends SignBlockHandler {
    private final LobbyGame lobbyGame;

    public LobbySignHandler(LobbyGame lobbyGame) {
        this.lobbyGame = lobbyGame;
    }

    @Override
    public void onPlace(@NotNull Placement placement) {
        super.onPlace(placement);
        CompoundBinaryTag data = placement.getBlock().nbt();
        if (data == null) return;
        CompoundBinaryTag frontText = data.getCompound("front_text");

        ListBinaryTag messages = frontText.getList("messages");
        if (messages.isEmpty()) return;

        String firstLine = messages.getString(0);

        if (firstLine.equalsIgnoreCase("[server-npc]")) {
            String description = messages.getString(1);
            if (description.isEmpty()) return;
            String skinName = messages.getString(2);
            if (skinName.isEmpty()) return;

            placement.getInstance().setBlock(placement.getBlockPosition(), Block.AIR);
            FakePlayer npc = new FakePlayer(Component.text(description), skinName);
            npc.setInstance(placement.getInstance(), placement.getBlockPosition().add(0.5, 0, 0.5));
            npc.setView(yawForSign(placement.getBlock()), 0.0f);
            npc.setNoGravity(true);
            npc.setOnInteract(player -> {
                player.sendMessage(Component.text("Hello from " + description + "!"));
                return null;
            });
        }

        if (firstLine.equalsIgnoreCase("[spawn-point]")) {
            int radius = 1;

            // Find first line with radius=
            for (int i = 1; i < messages.size(); i++) {
                String line = messages.getString(i);
                if (line.toLowerCase().startsWith("radius=")) {
                    try {
                        radius = Integer.parseInt(line.substring(7).trim());
                    } catch (NumberFormatException ignored) {
                    }
                    break;
                }
            }

            lobbyGame.setSpawn(placement.getBlockPosition().add(0.5, 1, 0.5).asPos(), radius);
            placement.getInstance().setBlock(placement.getBlockPosition(), Block.AIR);
        }
    }

    public static void register(LobbyGame lobbyGame) {
        BlockManager blockManager = MinecraftServer.getBlockManager();
        RegistryTag<Block> tag = Block.staticRegistry().getTag(TagKey.ofHash("#minecraft:all_signs"));
        LobbySignHandler signHandler = new LobbySignHandler(lobbyGame);
        for (RegistryKey<Block> key : Objects.requireNonNull(tag)) {
            blockManager.registerHandler(key.key(), () -> signHandler);
        }
    }

    private float yawForSign(Block sign) {
        // rotation is 0-15, 0 = south, 4 = west, 8 = north, 12 = east
        // yaw: south = 0, west = 90, north = -180, east = -90
        String rotation = sign.getProperty("rotation");
        if (rotation != null) {
            int rot = Integer.parseInt(rotation);
            float yaw = rot * 22.5f;
            if (yaw >= 180f) yaw -= 360f;
            return yaw;
        }
        String facing = sign.getProperty("facing");
        if (facing != null) {
            Direction dir = Direction.valueOf(facing.toUpperCase());
            return switch (dir) {
                case SOUTH -> 180f;
                case WEST -> 90f;
                case NORTH -> 0f;
                case EAST -> -90f;
                default -> 0f;
            };
        }
        return 0f;
    }
}
