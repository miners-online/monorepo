package uk.minersonline.games.message_exchange.proxy;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.*;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import static uk.minersonline.games.message_exchange.MessageCommon.QUEUE_NAME;

public class ProxyMessageServer implements AutoCloseable {
    private final Connection connection;
    private final ProxyHooks server;
    private final Logger logger;
    private final Gson gson = new Gson();

    private Channel channel;
    private final Map<String, Consumer<JsonObject>> handlers;

    public ProxyMessageServer(Connection connection, ProxyHooks server, Logger logger) {
        this.connection = connection;
        this.server = server;
        this.logger = logger;

        this.handlers = Map.of(
            "transfer", this::handleTransfer
        );
    }

    public void start() {
        try {
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            DeliverCallback deliver = (tag, delivery) -> {
                long deliveryTag = delivery.getEnvelope().getDeliveryTag();
                String body = new String(delivery.getBody(), StandardCharsets.UTF_8);

                try {
                    JsonElement el = gson.fromJson(body, JsonElement.class);
                    if (el == null || !el.isJsonObject()) {
                        nack(deliveryTag, "Malformed JSON", body);
                        return;
                    }

                    JsonObject obj = el.getAsJsonObject();
                    String type = getString(obj, "type");
                    if (type == null) {
                        nack(deliveryTag, "Missing type", body);
                        return;
                    }

                    Consumer<JsonObject> handler = handlers.get(type.toLowerCase());
                    if (handler == null) {
                        nack(deliveryTag, "No handler for type: " + type, body);
                        return;
                    }

                    handler.accept(obj);
                    channel.basicAck(deliveryTag, false);

                } catch (Exception e) {
                    logger.error("Message processing failure", e);
                    channel.basicNack(deliveryTag, false, false);
                }
            };

            channel.basicConsume(QUEUE_NAME, false, deliver, tag -> {});
            logger.info("ProxyMessageServer listening on '{}'", QUEUE_NAME);

        } catch (IOException e) {
            logger.error("Failed to start ProxyMessageServer", e);
        }
    }

    private void nack(long tag, String reason, String body) throws IOException {
        logger.warn("{}: {}", reason, body);
        channel.basicNack(tag, false, false);
    }

    private static String getString(JsonObject obj, String key) {
        var el = obj.get(key);
        if (el == null || el.isJsonNull()) return null;
        try {
            return el.getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (Exception e) {
            logger.warn("Error closing channel", e);
        }
    }

    // --- Handlers ---

    private void handleTransfer(JsonObject obj) {
        String serverName = getString(obj, "server");
        String uuidStr = getString(obj, "playerUuid");

        if (serverName == null || uuidStr == null) {
            logger.warn("Invalid transfer request: {}", obj);
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
}
