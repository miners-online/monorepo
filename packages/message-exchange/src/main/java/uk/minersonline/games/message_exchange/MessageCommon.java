package uk.minersonline.games.message_exchange;

import java.util.Properties;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MessageCommon {
    public static final String QUEUE_NAME = "minersonline.messages";

    public static Connection createRabbitMQConnection(Properties props) {
        String host = props.getProperty("rabbitmq.host");
        String portStr = props.getProperty("rabbitmq.port");
        String username = props.getProperty("rabbitmq.username");
        String password = props.getProperty("rabbitmq.password");

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

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        try {
            return factory.newConnection();
        } catch (Exception e) {
            return null;
        }
    }
}
