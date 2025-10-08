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
import net.sandrohc.schematic4j.schematic.Schematic;
import uk.minersonline.games.server_bootstrap.feature.FeatureRegistry;
import uk.minersonline.games.server_bootstrap.game.Game;
import uk.minersonline.games.world_management.WorldManagement;

import java.io.InputStream;

public class LobbyGame extends Game {
    private GlobalEventHandler geh;

    @Override
    public void onInit() {
        geh = MinecraftServer.getGlobalEventHandler();

        InstanceContainer voidInstance;
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("void_platform.schem");
            Schematic schematic = WorldManagement.loadCompressedFromInputStream(is);
            DimensionType fullbright = DimensionType.builder().ambientLight(1.0f).build();
            voidInstance = WorldManagement.instanceFromSchematic(schematic, fullbright);
            voidInstance.setTimeRate(0);
            voidInstance.setTime(12000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (FeatureRegistry.isFeatureLoaded(Key.key("miners_online:world_management"))) {
            WorldManagement.setDefaultInstance(voidInstance);
        }
    }

    @Override
    public void onStart() {
        geh.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            player.setGameMode(GameMode.CREATIVE);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });
    }

    @Override
    public void onStop() {

    }
}
