package uk.minersonline.games.message_exchange.proxy;

import java.util.UUID;

public interface ProxyHooks {
    void transferPlayerToServer(UUID uuid, String serverName);
}
