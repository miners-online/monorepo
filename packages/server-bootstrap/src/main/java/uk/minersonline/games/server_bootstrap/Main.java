package uk.minersonline.games.server_bootstrap;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.minersonline.games.server_bootstrap.feature.Feature;
import uk.minersonline.games.server_bootstrap.feature.FeatureRegistry;
import uk.minersonline.games.server_bootstrap.game.Game;
import uk.minersonline.games.server_bootstrap.game.GameConfig;
import uk.minersonline.games.server_bootstrap.game.GameLocator;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final GameLocator locator;

    public Main() {
        this.locator = new GameLocator();
    }

    public void run() throws Exception {
        MinecraftServer minecraftServer = MinecraftServer.init();
        MinecraftServer.setBrandName("Miners Online");
        MinecraftServer.getExceptionManager().setExceptionHandler((throwable) -> logger.error("Uncaught exception ", throwable));
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> logger.error("Uncaught exception in thread {}", thread.getName(), throwable));

        FeatureRegistry.INSTANCE.scanAndRegister();

        locator.locateGame();
        Game game = locator.getGame();
        GameConfig config = locator.getConfig();

        List<String> dependencyIDs = config.dependencies() == null ? List.of() : config.dependencies();
        List<Feature> features = new ArrayList<>();
        List<String> loadedFeatures = new ArrayList<>();
        List<String> failedFeatures = new ArrayList<>();

        for (String id : dependencyIDs) {
            Key key;
            try {
                key = Key.key(id);
            } catch (InvalidKeyException e) {
                logger.error("Invalid feature key format: '{}'", id, e);
                failedFeatures.add(id);
                continue;
            }

            FeatureRegistry.FeatureInfo info = FeatureRegistry.INSTANCE.getFeature(key);
            if (info == null) {
                logger.warn("No feature registered with ID '{}'. Skipping.", id);
                failedFeatures.add(id);
                continue;
            }

            try {
                Feature instance = info.createInstance();
                features.add(instance);
                loadedFeatures.add(info.id().asString());
                logger.debug("Loaded feature '{}' (version {})", info.id(), info.version());
            } catch (ReflectiveOperationException e) {
                logger.error("Failed to instantiate feature '{}'", id, e);
                failedFeatures.add(id);
            } catch (Throwable t) {
                logger.error("Unexpected error while loading feature '{}'", id, t);
                failedFeatures.add(id);
            }
        }

        if (!failedFeatures.isEmpty()) {
            logger.warn("Some features failed to load: {}", String.join(", ", failedFeatures));
        }

        game.init(features, locator.getConfig());
        game.start();

        Runtime.getRuntime().addShutdownHook(new Thread(game::stop));

        logger.info(
                "Starting server for game '{}' (version {}) with {} feature(s): {}",
                config.name() != null ? config.name() : "Unknown",
                config.version() != null ? config.version() : "N/A",
                loadedFeatures.isEmpty() ? 0 : loadedFeatures.size(),
                loadedFeatures.isEmpty() ? "None" : String.join(", ", loadedFeatures)
        );

        minecraftServer.start("0.0.0.0", 25565);
    }

    public static void main(String[] args) {
        try {
            new Main().run();
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }
}
