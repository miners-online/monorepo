package uk.minersonline.games.lobby;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
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
            String username = messages.getString(1);
            if (username.isEmpty()) return;
            String skinName = messages.getString(2);
            if (skinName.isEmpty()) return;

            placement.getInstance().setBlock(placement.getBlockPosition(), Block.AIR);
            FakePlayer npc = new FakePlayer(username, skinName);
            npc.setInstance(placement.getInstance(), placement.getBlockPosition().add(0.5, 0, 0.5));
            npc.setView(yawForSign(placement.getBlock()), 0.0f);
            npc.set(DataComponents.CUSTOM_NAME, Component.text(username));
        }
    }

    public static void register() {
        BlockManager blockManager = MinecraftServer.getBlockManager();
        RegistryTag<Block> tag = Block.staticRegistry().getTag(TagKey.ofHash("#minecraft:all_signs"));
        LobbySignHandler signHandler = new LobbySignHandler();
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
