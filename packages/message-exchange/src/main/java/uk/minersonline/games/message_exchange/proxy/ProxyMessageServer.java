package uk.minersonline.games.message_exchange.proxy;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.lettuce.core.Limit;
import io.lettuce.core.Range;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static uk.minersonline.games.message_exchange.MessageCommon.REQUEST_STREAM;
import static uk.minersonline.games.message_exchange.MessageCommon.RESPONSE_STREAM_MAX_LEN;
import static uk.minersonline.games.message_exchange.MessageCommon.RESPONSE_STREAM_PREFIX;
import static uk.minersonline.games.message_exchange.MessageCommon.RESPONSE_STREAM_TTL_SECONDS;

public class ProxyMessageServer implements AutoCloseable {
    private final StatefulRedisConnection<String, String> redisConnection;
    private final ProxyHooks server;
    private final Logger logger;
    private final Gson gson = new Gson();
    private final RedisCommands<String, String> commands;

    private final Map<String, BiConsumer<String, Map<String, String>>> handlers;
    private volatile boolean running;
    private Thread consumerThread;
    private String lastSeenId = "0-0";

    public ProxyMessageServer(StatefulRedisConnection<String, String> redisConnection, ProxyHooks server, Logger logger) {
        this.redisConnection = redisConnection;
        this.server = server;
        this.logger = logger;
        this.commands = redisConnection.sync();

        this.handlers = Map.of(
            "transfer", this::handleTransfer,
            "server-list", this::handleServerList
        );
    }

    public void start() {
        if (running) {
            return;
        }

        initializeCursor();
        running = true;
        consumerThread = new Thread(this::consumeLoop, "proxy-message-server");
        consumerThread.setDaemon(true);
        consumerThread.start();
        logger.info("ProxyMessageServer listening on stream '{}'", REQUEST_STREAM);
    }

    private void initializeCursor() {
        try {
            List<StreamMessage<String, String>> latest = commands.xrevrange(
                REQUEST_STREAM,
                Range.create("+", "-"),
                Limit.from(1)
            );
            if (latest != null && !latest.isEmpty()) {
                lastSeenId = latest.get(0).getId();
            } else {
                lastSeenId = "0-0";
            }
        } catch (Exception e) {
            lastSeenId = "0-0";
        }
    }

    private void consumeLoop() {
        while (running) {
            try {
                List<StreamMessage<String, String>> messages = commands.xrange(
                    REQUEST_STREAM,
                    Range.create(lastSeenId, "+"),
                    Limit.from(10)
                );

                if (messages == null || messages.isEmpty()) {
                    Thread.sleep(Duration.ofMillis(100));
                    continue;
                }

                boolean advanced = false;
                for (StreamMessage<String, String> message : messages) {
                    if (message.getId().equals(lastSeenId)) {
                        continue;
                    }
                    advanced = true;
                    String messageId = message.getId();
                    lastSeenId = messageId;
                    Map<String, String> body = message.getBody();
                    String type = body.get("type");

                    if (type == null) {
                        logger.warn("Missing type for stream message {}", message.getId());
                        deleteRequestMessage(messageId);
                        continue;
                    }

                    BiConsumer<String, Map<String, String>> handler = handlers.get(type.toLowerCase());
                    if (handler == null) {
                        logger.warn("No handler for type '{}' in message {}", type, message.getId());
                        deleteRequestMessage(messageId);
                        continue;
                    }

                    try {
                        handler.accept(messageId, body);
                    } catch (Exception e) {
                        logger.error("Message processing failure for stream message {}", message.getId(), e);
                    } finally {
                        deleteRequestMessage(messageId);
                    }
                }

                if (!advanced) {
                    Thread.sleep(Duration.ofMillis(100));
                }
            } catch (InterruptedException e) {
                if (running) {
                    Thread.currentThread().interrupt();
                    logger.error("Redis stream consume loop interrupted", e);
                }
                return;
            } catch (Exception e) {
                if (running) {
                    logger.error("Failed to read from Redis stream '{}'", REQUEST_STREAM, e);
                }
            }
        }
    }

    private void deleteRequestMessage(String messageId) {
        try {
            commands.xdel(REQUEST_STREAM, messageId);
        } catch (Exception e) {
            logger.warn("Failed to delete consumed request message {}", messageId, e);
        }
    }

    @Override
    public void close() {
        running = false;

        if (consumerThread != null) {
            consumerThread.interrupt();
            try {
                consumerThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            if (redisConnection != null && redisConnection.isOpen()) {
                redisConnection.close();
            }
        } catch (Exception e) {
            logger.warn("Error closing Redis connection", e);
        }
    }

    private void handleTransfer(String requestId, Map<String, String> body) {
        String serverName = body.get("server");
        String uuidStr = body.get("playerUuid");

        if (serverName == null || uuidStr == null) {
            logger.warn("Invalid transfer request: {}", body);
            return;
        }

        try {
            UUID uuid = UUID.fromString(uuidStr);
            server.sendPlayerMessage(uuid, Component.text("Transferring to " + serverName + "...").color(NamedTextColor.YELLOW));
            CompletableFuture<Boolean> result = server.transferPlayerToServer(uuid, serverName);
            result.thenAccept(success -> {
                if (success) {
                    logger.info("Player {} transferred to server '{}'", uuid, serverName);
                } else {
                    server.sendPlayerMessage(uuid, Component.text("Transfer to " + serverName + " failed.").color(NamedTextColor.RED));
                    logger.warn("Failed to transfer player {} to server '{}'", uuid, serverName);
                }
            });
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid UUID in transfer request: {}", uuidStr);
        }
    }

    private void handleServerList(String requestId, Map<String, String> body) {
        try {
            CompletableFuture<Map<String, ServerInfo>> listF = server.listServers();
            listF.thenAccept(map -> {
                try {
                    JsonArray arr = new JsonArray();
                    map.forEach((name, info) -> {
                        JsonObject s = new JsonObject();
                        s.addProperty("name", name);
                        s.addProperty("playerCount", info.getPlayerCount());
                        s.addProperty("alive", info.isAlive());
                        arr.add(s);
                    });

                    Map<String, String> response = new HashMap<>();
                    response.put("type", "server-list-response");
                    response.put("servers", gson.toJson(arr));
                    String responseStream = RESPONSE_STREAM_PREFIX + "." + requestId;
                    commands.xadd(
                        responseStream,
                        XAddArgs.Builder.maxlen(RESPONSE_STREAM_MAX_LEN).approximateTrimming(),
                        response
                    );
                    commands.expire(responseStream, RESPONSE_STREAM_TTL_SECONDS);
                } catch (Exception e) {
                    logger.error("Failed to send server-list response", e);
                }
            });
        } catch (Exception e) {
            logger.error("Error handling server-list", e);
        }
    }
}
