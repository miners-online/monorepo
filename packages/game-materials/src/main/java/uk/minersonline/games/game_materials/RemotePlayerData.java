package uk.minersonline.games.game_materials;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;

public class RemotePlayerData {
    public static void register() {
        MinecraftServer.getConnectionManager().setPlayerProvider(RemotePlayer::new);
    }

    public static class RemotePlayer extends Player {
        public RemotePlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
            super(playerConnection, gameProfile);
        }

    }
}
