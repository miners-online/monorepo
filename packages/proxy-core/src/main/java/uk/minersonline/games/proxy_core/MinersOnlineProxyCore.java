package uk.minersonline.games.proxy_core;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import java.nio.file.Path;

import org.slf4j.Logger;

@Plugin(
    id = "minersonline-proxy-core",
    name = "MinersOnline Proxy Core",
    version = "1.0.0",
    description = "Core plugin for MinersOnline proxy server",
    authors = {"samuelh2005"}
)
public class MinersOnlineProxyCore {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public MinersOnlineProxyCore(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        logger.info("Hello there! I made my first plugin with Velocity.");
    }
}
