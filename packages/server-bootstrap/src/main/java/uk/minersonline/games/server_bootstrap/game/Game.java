package uk.minersonline.games.server_bootstrap.game;

import uk.minersonline.games.message_exchange.MessageCommon;
import uk.minersonline.games.message_exchange.client.ProxyMessageClient;
import uk.minersonline.games.server_bootstrap.ServerConfig;
import uk.minersonline.games.server_bootstrap.feature.Feature;

import java.util.List;

import com.rabbitmq.client.Connection;

public abstract class Game {
    private List<Feature> features;
    private GameConfig config;
    private ServerConfig serverConfig;

    private Connection rabbitConnection;
    private ProxyMessageClient proxyMessageClient;

    public void init(List<Feature> featuresToLoad, GameConfig config, ServerConfig serverConfig) {
        this.features = featuresToLoad;
        this.config = config;
        this.serverConfig = serverConfig;

        if (features != null) {
            for (Feature feature : features) {
                feature.onInit(this);
            }
        }

        rabbitConnection = MessageCommon.createRabbitMQConnection(serverConfig.getProperties());
        if (rabbitConnection != null) {
            try {
                proxyMessageClient = new ProxyMessageClient(rabbitConnection);
            } catch (Exception e) {
                rabbitConnection = null;
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

    public ProxyMessageClient getProxyMessageClient() {
        return proxyMessageClient;
    }

    protected abstract void onInit();

    protected abstract void onStart();

    protected abstract void onStop();
}
