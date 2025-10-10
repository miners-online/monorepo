package uk.minersonline.games.lobby;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
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
    private Pos spawnPoint;

    @Override
    public void onInit() {
        geh = MinecraftServer.getGlobalEventHandler();
        LobbySignHandler.register();

        if (FeatureRegistry.isFeatureLoaded(Key.key("miners_online:world_management"))) {
            InstanceContainer instance;
            try {
                String path = System.getenv("SCHEMATIC_PATH");
                InputStream is;
                if (path == null) {
                    is = this.getClass().getClassLoader().getResourceAsStream("void_platform.schem");
                } else {
                    is = new FileInputStream(path);
                }
                MinestomSchematic lobby = MinestomSchematic.loadGzip(is);
                DimensionType fullbright = DimensionType.builder().ambientLight(1.0f).build();
                instance = WorldManagement.instanceFromSchematic(lobby, fullbright);
                instance.setTimeRate(0);
                instance.setTime(12000);
                spawnPoint = new Pos(0.5, lobby.offset().y() + 2, 0.5);
                WorldManagement.findHighestBlock(instance, spawnPoint.blockX(), spawnPoint.blockZ(), (y, block) -> {
                    if (block != null && !block.isAir()) {
                        spawnPoint = new Pos(spawnPoint.x(), y + 1, spawnPoint.z());
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            WorldManagement.setDefaultInstance(instance);
        }
    }

    @Override
    public void onStart() {
        geh.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            player.setGameMode(GameMode.CREATIVE);
            player.setRespawnPoint(spawnPoint);
        });
    }

    @Override
    public void onStop() {

    }
}
