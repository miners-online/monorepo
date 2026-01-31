package uk.minersonline.games.message_exchange.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import uk.minersonline.games.message_exchange.proxy.ServerInfo;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import java.util.concurrent.CompletableFuture;
import static uk.minersonline.games.message_exchange.MessageCommon.QUEUE_NAME;

public final class ProxyMessageClient implements AutoCloseable {
    private final Channel channel;
    private final Gson gson = new Gson();

    public ProxyMessageClient(Connection connection) throws Exception {
        this.channel = connection.createChannel();
        this.channel.queueDeclare(QUEUE_NAME, true, false, false, null);
    }

    @Override
    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (Exception ignored) {
        }
    }

    // --- Message Sending Methods ---

    public void sendTransfer(UUID playerUuid, String targetServer) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "transfer");
        msg.addProperty("playerUuid", playerUuid.toString());
        msg.addProperty("server", targetServer);

        try {
            channel.basicPublish(
                "",
                QUEUE_NAME,
                null,
                msg.toString().getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception ignored) {
        }
    }

    // Request list of servers. Returns a map of server -> ServerInfo (alive + playerCount).
    public CompletableFuture<Map<String, ServerInfo>> requestServerList() {
        CompletableFuture<Map<String, ServerInfo>> future = new CompletableFuture<>();
        try {
            String corrId = UUID.randomUUID().toString();
            String replyQueue = channel.queueDeclare().getQueue();

            channel.basicConsume(replyQueue, true, (tag, delivery) -> {
                AMQP.BasicProperties props = delivery.getProperties();
                if (corrId.equals(props.getCorrelationId())) {
                    String body = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    JsonElement el = gson.fromJson(body, JsonElement.class);
                    if (el != null && el.isJsonObject()) {
                        JsonObject obj = el.getAsJsonObject();
                        var arr = obj.getAsJsonArray("servers");
                        if (arr == null) {
                            future.completeExceptionally(new RuntimeException("Malformed response: missing servers array"));
                            return;
                        }
                        Map<String, ServerInfo> map = new java.util.HashMap<>();
                        arr.forEach(e -> {
                            try {
                                if (e.isJsonObject()) {
                                    var so = e.getAsJsonObject();
                                    String name = so.has("name") ? so.get("name").getAsString() : null;
                                    int count = so.has("playerCount") ? so.get("playerCount").getAsInt() : 0;
                                    boolean alive = so.has("alive") && so.get("alive").getAsBoolean();
                                    if (name != null) map.put(name, new ServerInfo(alive, count));
                                }
                            } catch (Exception ignored) {}
                        });
                        future.complete(map);
                    } else {
                        future.completeExceptionally(new RuntimeException("Malformed response"));
                    }
                }
            }, consumerTag -> {});

            JsonObject msg = new JsonObject();
            msg.addProperty("type", "server-list");

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(corrId)
                .replyTo(replyQueue)
                .build();

            channel.basicPublish("", QUEUE_NAME, props, msg.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }
}
