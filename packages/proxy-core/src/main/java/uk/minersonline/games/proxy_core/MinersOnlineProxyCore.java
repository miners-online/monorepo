package uk.minersonline.games.proxy_core;

import com.google.inject.Inject;
import com.rabbitmq.client.Connection;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import uk.minersonline.games.message_exchange.MessageCommon;
import uk.minersonline.games.message_exchange.proxy.ProxyHooks;
import uk.minersonline.games.message_exchange.proxy.ProxyMessageServer;

import com.velocitypowered.api.event.Subscribe;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;

@Plugin(
    id = "minersonline-proxy-core",
    name = "MinersOnline Proxy Core",
    version = "1.0.0",
    description = "Core plugin for MinersOnline proxy server",
    authors = {"samuelh2005"}
)
public class MinersOnlineProxyCore implements ProxyHooks {
    private final ProxyServer server;
    private final Logger logger;

    // RabbitMQ related
    private final ProxyConfig proxyConfig;
    private Connection rabbitConnection;
    private ProxyMessageServer messageServer;

    @Inject
    public MinersOnlineProxyCore(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;

        Path configPath = dataDirectory.resolve("proxy.properties");
        this.proxyConfig = new ProxyConfig(configPath.toString());

        this.rabbitConnection = MessageCommon.createRabbitMQConnection(this.proxyConfig.getProperties());
        if (this.rabbitConnection != null) {
            this.logger.info("Connected to RabbitMQ, starting listener...");
            this.messageServer = new ProxyMessageServer(this.rabbitConnection, this, this.logger);
            this.messageServer.start();
        } else {
            this.logger.warn("RabbitMQ connection could not be established; plugin will not listen for proxy requests.");
        }
    }

    @Subscribe
    public void onShutdown(com.velocitypowered.api.event.proxy.ProxyShutdownEvent event) {
        if (this.messageServer != null) {
            try {
                this.messageServer.close();
            } catch (Exception e) {
                this.logger.error("Error while closing RabbitMQ listener", e);
            }
        }

        if (this.rabbitConnection != null) {
            try {
                this.rabbitConnection.close();
            } catch (Exception e) {
                this.logger.error("Error while closing RabbitMQ connection", e);
            }
        }

        this.logger.info("MinersOnlineProxyCore shutting down.");
    }

    @Override
    public void transferPlayerToServer(UUID uuid, String serverName) {
        Optional<Player>  optPlayer = server.getPlayer(uuid);
        if (optPlayer.isEmpty()) {
            logger.warn("Player with UUID {} not found for transfer to server '{}'", uuid, serverName);
            return;
        }

        Player player = optPlayer.get();
        Optional<RegisteredServer> optServer = server.getServer(serverName);
        if (optServer.isEmpty()) {
            logger.warn("Target server '{}' not found for player UUID {}", serverName, uuid);
            return;
        }

        RegisteredServer targetServer = optServer.get();
        player.createConnectionRequest(targetServer).connect().thenAccept(result -> {
            if (result.isSuccessful()) {
                logger.info("Player {} transferred to server '{}'", player.getUsername(), serverName);
            } else {
                logger.warn("Failed to transfer player {} to server '{}'", player.getUsername(), serverName);
            }
        });
    }
}
