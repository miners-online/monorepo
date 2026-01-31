package uk.minersonline.games.message_exchange.proxy;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.kyori.adventure.text.Component;

public interface ProxyHooks {
    void sendPlayerMessage(UUID uuid, Component message);
    CompletableFuture<Boolean> transferPlayerToServer(UUID uuid, String serverName);
}
