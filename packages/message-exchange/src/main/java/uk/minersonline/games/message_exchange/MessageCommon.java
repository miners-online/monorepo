package uk.minersonline.games.message_exchange;

import java.util.Properties;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

public class MessageCommon {
    public static final String QUEUE_NAME = "minersonline.messages";
    public static final String REQUEST_STREAM = QUEUE_NAME + ".requests";
    public static final String RESPONSE_STREAM_PREFIX = QUEUE_NAME + ".responses";

    public static StatefulRedisConnection<String, String> createRedisConnection(Properties props) {
        String host = props.getProperty("redis.host");
        String portStr = props.getProperty("redis.port");
        String username = props.getProperty("redis.username");
        String password = props.getProperty("redis.password");

        if (host != null) host = host.trim();
        if (portStr != null) portStr = portStr.trim();
        if (username != null) username = username.trim();
        if (password != null) password = password.trim();

        if (host == null || host.isEmpty() ||
            portStr == null || portStr.isEmpty() ||
            username == null || username.isEmpty() ||
            password == null || password.isEmpty()) {
            return null;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            return null;
        }
        if (port <= 0 || port > 65535) {
            return null;
        }

        RedisURI.Builder builder = RedisURI.builder().withHost(host).withPort(port);
        if (!username.isEmpty()) {
            builder.withAuthentication(username, password.toCharArray());
        } else {
            builder.withPassword(password.toCharArray());
        }

        try {
            RedisClient client = RedisClient.create(builder.build());
            return client.connect();
        } catch (Exception e) {
            return null;
        }
    }
}
