package uk.minersonline.games.world_management;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Instance;
import uk.minersonline.games.server_bootstrap.feature.Feature;
import uk.minersonline.games.server_bootstrap.feature.FeatureImplementation;
import uk.minersonline.games.server_bootstrap.game.Game;

@FeatureImplementation(
    id = "miners_online:world_management",
    version = "0.0.1"
)
public class WorldManagement implements Feature {
    private static Instance worldInstance;
    private GlobalEventHandler geh;

    @Override
    public void onInit(Game game) {
        geh = MinecraftServer.getGlobalEventHandler();
    }

    @Override
    public void onStart() {
        geh.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            if (worldInstance != null) {
                event.setSpawningInstance(worldInstance);
            } else {
                event.getPlayer().kick(
                        Component.text("No world instance available, please contact the server administrator.")
                                .color(NamedTextColor.RED)
                );
            }
        });
    }

    @Override
    public void onStop() {

    }

    public static void setDefaultInstance(Instance worldInstance) {
        WorldManagement.worldInstance = worldInstance;
    }
}
