package uk.minersonline.games.message_exchange.client;

import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import static uk.minersonline.games.message_exchange.MessageCommon.QUEUE_NAME;

public final class ProxyMessageClient implements AutoCloseable {
    private final Channel channel;

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
}
