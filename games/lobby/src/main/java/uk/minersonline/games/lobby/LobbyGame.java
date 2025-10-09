package uk.minersonline.games.lobby;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.world.DimensionType;
import uk.minersonline.games.server_bootstrap.feature.FeatureRegistry;
import uk.minersonline.games.server_bootstrap.game.Game;
import uk.minersonline.games.world_management.MinestomSchematic;
import uk.minersonline.games.world_management.WorldManagement;

import java.io.FileInputStream;
import java.io.InputStream;

public class LobbyGame extends Game {
    private GlobalEventHandler geh;
    private MinestomSchematic lobby;

    @Override
    public void onInit() {
        geh = MinecraftServer.getGlobalEventHandler();

        if (FeatureRegistry.isFeatureLoaded(Key.key("miners_online:world_management"))) {
            InstanceContainer voidInstance;
            try {
                String path = System.getenv("SCHEMATIC_PATH");
                InputStream is;
                if (path == null) {
                    is = this.getClass().getClassLoader().getResourceAsStream("void_platform.schem");
                } else {
                    is = new FileInputStream(path);
                }
                lobby = MinestomSchematic.loadGzip(is);
                DimensionType fullbright = DimensionType.builder().ambientLight(1.0f).build();
                voidInstance = WorldManagement.instanceFromSchematic(lobby, fullbright);
                voidInstance.setTimeRate(0);
                voidInstance.setTime(12000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            WorldManagement.setDefaultInstance(voidInstance);
        }
    }

    @Override
    public void onStart() {
        geh.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            BlockVec offset = lobby.offset();
            Pos spawn = new Pos(0.5, offset.y() + 2, 0.5);
            final Player player = event.getPlayer();
            player.setGameMode(GameMode.CREATIVE);
            player.setRespawnPoint(spawn);
        });
    }

    @Override
    public void onStop() {

    }
}
