package uk.minersonline.games.message_exchange.proxy;

public final class ServerInfo {
    private final boolean alive;
    private final int playerCount;

    public ServerInfo(boolean alive, int playerCount) {
        this.alive = alive;
        this.playerCount = playerCount;
    }

    public boolean isAlive() { return alive; }
    public int getPlayerCount() { return playerCount; }
}
