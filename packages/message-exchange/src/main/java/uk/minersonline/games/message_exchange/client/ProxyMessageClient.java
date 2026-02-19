package uk.minersonline.games.message_exchange.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.lettuce.core.Limit;
import io.lettuce.core.Range;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import uk.minersonline.games.message_exchange.proxy.ServerInfo;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static uk.minersonline.games.message_exchange.MessageCommon.REQUEST_STREAM;
import static uk.minersonline.games.message_exchange.MessageCommon.RESPONSE_STREAM_PREFIX;

public final class ProxyMessageClient implements AutoCloseable {
    private final StatefulRedisConnection<String, String> redisConnection;
    private final RedisCommands<String, String> commands;
    private final Gson gson = new Gson();

    public ProxyMessageClient(StatefulRedisConnection<String, String> connection) throws Exception {
        if (connection == null) {
            throw new IllegalArgumentException("Redis connection cannot be null");
        }
        this.redisConnection = connection;
        this.commands = connection.sync();
    }

    @Override
    public void close() {
        try {
            if (redisConnection != null && redisConnection.isOpen()) {
                redisConnection.close();
            }
        } catch (Exception ignored) {
        }
    }

    public void sendTransfer(UUID playerUuid, String targetServer) {
        Map<String, String> msg = new HashMap<>();
        msg.put("type", "transfer");
        msg.put("playerUuid", playerUuid.toString());
        msg.put("server", targetServer);

        try {
            commands.xadd(REQUEST_STREAM, msg);
        } catch (Exception ignored) {
        }
    }

    public CompletableFuture<Map<String, ServerInfo>> requestServerList() {
        CompletableFuture<Map<String, ServerInfo>> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            String responseStream = null;
            try {
                Map<String, String> msg = new HashMap<>();
                msg.put("type", "server-list");
                String requestId = commands.xadd(REQUEST_STREAM, msg);
                if (requestId == null || requestId.isBlank()) {
                    throw new RuntimeException("Failed to publish server-list request");
                }
                responseStream = RESPONSE_STREAM_PREFIX + "." + requestId;

                long timeoutNanos = Duration.ofSeconds(10).toNanos();
                long deadline = System.nanoTime() + timeoutNanos;
                StreamMessage<String, String> response = null;

                while (System.nanoTime() < deadline) {
                    List<StreamMessage<String, String>> responses = commands.xrange(
                        responseStream,
                        Range.create("0-0", "+"),
                        Limit.from(1)
                    );

                    if (responses != null && !responses.isEmpty()) {
                        response = responses.get(0);
                    }

                    if (response != null) {
                        break;
                    }

                    Thread.sleep(50);
                }

                if (response == null) {
                    throw new RuntimeException("Timed out waiting for server-list response");
                }

                Map<String, String> body = response.getBody();
                String serversJson = body.get("servers");
                if (serversJson == null) {
                    throw new RuntimeException("Malformed response: missing servers field");
                }

                JsonElement el = gson.fromJson(serversJson, JsonElement.class);
                if (el == null || !el.isJsonArray()) {
                    throw new RuntimeException("Malformed response: invalid servers payload");
                }

                Map<String, ServerInfo> map = new HashMap<>();
                el.getAsJsonArray().forEach(e -> {
                    try {
                        if (e.isJsonObject()) {
                            JsonObject so = e.getAsJsonObject();
                            String name = so.has("name") ? so.get("name").getAsString() : null;
                            int count = so.has("playerCount") ? so.get("playerCount").getAsInt() : 0;
                            boolean alive = so.has("alive") && so.get("alive").getAsBoolean();
                            if (name != null) {
                                map.put(name, new ServerInfo(alive, count));
                            }
                        }
                    } catch (Exception ignored) {
                    }
                });

                future.complete(map);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                future.completeExceptionally(e);
            } catch (Exception e) {
                future.completeExceptionally(e);
            } finally {
                try {
                    if (responseStream != null) {
                        commands.del(responseStream);
                    }
                } catch (Exception ignored) {
                }
            }
        });
        return future;
    }
}
