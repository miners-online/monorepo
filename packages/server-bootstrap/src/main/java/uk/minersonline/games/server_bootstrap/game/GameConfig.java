package uk.minersonline.games.server_bootstrap.game;

import java.util.List;

public record GameConfig(
        String mainClass,
        String name,
        String version,
        List<String> dependencies
) {
}