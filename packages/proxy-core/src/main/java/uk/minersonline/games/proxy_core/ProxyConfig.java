package uk.minersonline.games.proxy_core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;

public class ProxyConfig {
    private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{([A-Z0-9_]+)}");

    private Properties props;

    public ProxyConfig(String path) {
        this.props = loadProperties(path);
    }

    public Properties getProperties() {
        return props;
    }

    public @Nullable Connection createRabbitMQConnection() {
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

    private static Properties loadProperties(String path) {
        Properties raw = new Properties();

        try (InputStream in = Files.newInputStream(Path.of(path))) {
            raw.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load proxy configuration from: " + path, e);
        }

        Properties resolved = new Properties();
        for (Map.Entry<Object, Object> entry : raw.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            resolved.setProperty(key, resolveEnvPlaceholders(value));
        }

        return resolved;
    }

    private static String resolveEnvPlaceholders(String value) {
        Matcher matcher = ENV_PATTERN.matcher(value);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String envKey = matcher.group(1);
            String envValue = System.getenv(envKey);
            if (envValue == null) {
                throw new IllegalStateException(
                    "Environment variable " + envKey + " referenced but not set."
                );
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(envValue));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
