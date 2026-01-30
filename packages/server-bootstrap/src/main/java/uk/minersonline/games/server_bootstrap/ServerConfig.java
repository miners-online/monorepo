package uk.minersonline.games.server_bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minestom.server.Auth;

public class ServerConfig {
    private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{([A-Z0-9_]+)}");

    private Properties props;

    public ServerConfig(String path) {
        this.props = loadProperties(path);
    }

    public Properties getProperties() {
        return props;
    }

    public Auth getAuth() {
        if (props.containsKey("USE_MOJANG_AUTH")) {
            return new Auth.Online();
        }

        if (props.containsKey("PROXY_AUTH_TYPE")) {
            String type = props.getProperty("PROXY_AUTH_TYPE").toUpperCase();

            if ("VELOCITY".equals(type)) {
                String secret = props.getProperty("VELOCITY_AUTH_SECRET");
                if (secret == null || secret.isBlank()) {
                    throw new IllegalStateException(
                        "VELOCITY_AUTH_SECRET property is required for VELOCITY proxy authentication."
                    );
                }
                return new Auth.Velocity(secret);
            }

            if ("BUNGEE_GUARD".equals(type)) {
                String tokenList = props.getProperty("BUNGEE_GUARD_AUTH_TOKENS");
                if (tokenList == null || tokenList.isBlank()) {
                    throw new IllegalStateException(
                        "BUNGEE_GUARD_AUTH_TOKENS property is required for BUNGEE_GUARD proxy authentication."
                    );
                }
                Set<String> tokens = Set.of(tokenList.split(","));
                return new Auth.Bungee(tokens);
            }
        }

        return new Auth.Offline();
    }

    private static Properties loadProperties(String path) {
        Properties raw = new Properties();

        try (InputStream in = Files.newInputStream(Path.of(path))) {
            raw.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load auth configuration from: " + path, e);
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
