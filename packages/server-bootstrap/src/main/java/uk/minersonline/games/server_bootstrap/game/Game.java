package uk.minersonline.games.server_bootstrap.game;

import uk.minersonline.games.server_bootstrap.ServerConfig;
import uk.minersonline.games.server_bootstrap.feature.Feature;

import java.util.List;

public abstract class Game {
    private List<Feature> features;
    private GameConfig config;
    private ServerConfig serverConfig;

    public void init(List<Feature> featuresToLoad, GameConfig config, ServerConfig serverConfig) {
        this.features = featuresToLoad;
        this.config = config;
        this.serverConfig = serverConfig;

        if (features != null) {
            for (Feature feature : features) {
                feature.onInit(this);
            }
        }
        onInit();
    }

    public void start() {
        if (features != null) {
            for (Feature feature : features) {
                feature.onStart();
            }
        }
        onStart();
    }

    public void stop() {
        onStop();
        if (features != null) {
            for (Feature feature : features) {
                feature.onStop();
            }
        }
    }

    public GameConfig getConfig() {
        return config;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    protected abstract void onInit();

    protected abstract void onStart();

    protected abstract void onStop();
}
