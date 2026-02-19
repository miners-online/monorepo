package uk.minersonline.games.proxy_core;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.lettuce.core.api.StatefulRedisConnection;

import net.kyori.adventure.text.Component;
import uk.minersonline.games.message_exchange.MessageCommon;
import uk.minersonline.games.message_exchange.proxy.ProxyHooks;
import uk.minersonline.games.message_exchange.proxy.ProxyMessageServer;
import uk.minersonline.games.message_exchange.proxy.ServerInfo;

import com.velocitypowered.api.event.Subscribe;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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

    // Redis related
    private final ProxyConfig proxyConfig;
    private StatefulRedisConnection<String, String> redisConnection;
    private ProxyMessageServer messageServer;

    @Inject
    public MinersOnlineProxyCore(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;

        Path configPath = dataDirectory.resolve("proxy.properties");
        this.proxyConfig = new ProxyConfig(configPath.toString());

        this.redisConnection = MessageCommon.createRedisConnection(this.proxyConfig.getProperties());
        if (this.redisConnection != null) {
            this.logger.info("Connected to Redis, starting listener...");
            this.messageServer = new ProxyMessageServer(this.redisConnection, this, this.logger);
            this.messageServer.start();
        } else {
            this.logger.warn("Redis connection could not be established; plugin will not listen for proxy requests.");
        }
    }

    @Subscribe
    public void onShutdown(com.velocitypowered.api.event.proxy.ProxyShutdownEvent event) {
        if (this.messageServer != null) {
            try {
                this.messageServer.close();
            } catch (Exception e) {
                this.logger.error("Error while closing Redis listener", e);
            }
        }

        if (this.redisConnection != null) {
            try {
                this.redisConnection.close();
            } catch (Exception e) {
                this.logger.error("Error while closing Redis connection", e);
            }
        }

        this.logger.info("MinersOnlineProxyCore shutting down.");
    }

    @Override
    public CompletableFuture<Boolean> transferPlayerToServer(UUID uuid, String serverName) {
        CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();
        Optional<Player>  optPlayer = server.getPlayer(uuid);
        if (optPlayer.isEmpty()) {
            resultFuture.complete(false);
            return resultFuture;
        }

        Player player = optPlayer.get();
        Optional<RegisteredServer> optServer = server.getServer(serverName);
        if (optServer.isEmpty()) {
            resultFuture.complete(false);
            return resultFuture;
        }

        RegisteredServer targetServer = optServer.get();
        player.createConnectionRequest(targetServer).connect().thenAccept(result -> {
            resultFuture.complete(result.isSuccessful());
        });

        return resultFuture;
    }

    @Override
    public void sendPlayerMessage(UUID uuid, Component message) {
        Optional<Player> optPlayer = server.getPlayer(uuid);
        if (optPlayer.isPresent()) {
            Player player = optPlayer.get();
            player.sendMessage(message);
        }
    }

    @Override
    public CompletableFuture<Map<String, ServerInfo>> listServers() {
        Map<String, ServerInfo> serverInfoMap = new ConcurrentHashMap<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (RegisteredServer regServer : server.getAllServers()) {
            final String serverName = regServer.getServerInfo().getName();
            final int playerCount = regServer.getPlayersConnected().size();

            CompletableFuture<Void> f = regServer.ping()
                .thenAccept(ping -> {
                    boolean alive = ping != null;
                    serverInfoMap.put(serverName, new ServerInfo(alive, playerCount));
                })
                .exceptionally(ex -> {
                    serverInfoMap.put(serverName, new ServerInfo(false, playerCount));
                    return null;
                });

            futures.add(f);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                .thenApply(ignored -> serverInfoMap);
    }
}
