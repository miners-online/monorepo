package uk.minersonline.games.game_materials;

import net.minestom.server.entity.Player;
import net.minestom.server.network.NetworkBuffer;

public class ProxyUtils {
    public static final String BUNGEE_CHANNEL = "bungeecord:main";

    private ProxyUtils() {
    }

    public static void transfer(Player player, String serverName) {
        player.sendPluginMessage(BUNGEE_CHANNEL, NetworkBuffer.makeArray(buffer -> {
            buffer.write(NetworkBuffer.STRING_IO_UTF8, "Connect");
            buffer.write(NetworkBuffer.STRING_IO_UTF8, serverName);
        }));
    }
}
