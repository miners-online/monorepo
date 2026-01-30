package uk.minersonline.games.game_materials;

import java.nio.charset.StandardCharsets;

import net.minestom.server.entity.Player;
import net.minestom.server.network.NetworkBuffer;

public class WorldID {
    public static final String XAERO_MINIMAP_CHANNEL = "xaerominimap:main";
    public static final String XAERO_WORLDMAP_CHANNEL = "xaeroworldmap:main";
    public static final String WORLDID_CHANNEL = "worldinfo:world_id"; // VoxelMap
    public static final String WORLDID_LEGACY_CHANNEL = "world_id"; // VoxelMap legacy

    private WorldID() {
    }

    public static int generateWorldId() {
        return (int) (Math.random() * Integer.MAX_VALUE);
    }
    
    public static void sendWorldId(Player player, int worldId) {
        // Create Xaero Mini-map/World-map world ID packet
        byte[] data = NetworkBuffer.makeArray(buffer -> {
            buffer.write(NetworkBuffer.BYTE, (byte) 0);
            buffer.write(NetworkBuffer.INT, worldId);
        });
        player.sendPluginMessage(XAERO_MINIMAP_CHANNEL, data);
        player.sendPluginMessage(XAERO_WORLDMAP_CHANNEL, data);

        // Create VoxelMap world ID packet
        byte[] voxelMapData = NetworkBuffer.makeArray(buffer -> {
            buffer.write(NetworkBuffer.BYTE, (byte) 0);
            buffer.write(NetworkBuffer.BYTE, (byte) 42);
            byte[] worldIdBytes = String.valueOf(worldId).getBytes(StandardCharsets.UTF_8);
            buffer.write(NetworkBuffer.INT, worldIdBytes.length);
            buffer.write(NetworkBuffer.RAW_BYTES, worldIdBytes);
        });

        player.sendPluginMessage(WORLDID_CHANNEL, voxelMapData);
        player.sendPluginMessage(WORLDID_LEGACY_CHANNEL, voxelMapData);
    }
}
