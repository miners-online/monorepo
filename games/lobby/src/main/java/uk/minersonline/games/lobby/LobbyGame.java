package uk.minersonline.games.lobby;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import uk.minersonline.games.server_bootstrap.feature.FeatureRegistry;
import uk.minersonline.games.server_bootstrap.game.Game;
import uk.minersonline.games.world_management.WorldManagement;

public class LobbyGame extends Game {
    private GlobalEventHandler geh;

    @Override
    public void onInit() {
        geh = MinecraftServer.getGlobalEventHandler();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        if (FeatureRegistry.isFeatureLoaded(Key.key("miners_online:world_management"))) {
            WorldManagement.setDefaultInstance(instanceContainer);
        }
    }

    @Override
    public void onStart() {
        geh.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            player.setRespawnPoint(new Pos(0, 42, 0));
        });
    }

    @Override
    public void onStop() {

    }
}
