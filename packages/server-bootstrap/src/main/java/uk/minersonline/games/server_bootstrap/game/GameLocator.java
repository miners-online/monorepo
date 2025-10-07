package uk.minersonline.games.server_bootstrap.game;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;

public class GameLocator {
    private static final Logger logger = LoggerFactory.getLogger(GameLocator.class);
    private static final Gson gson = new Gson();
    private Game game;
    private GameConfig config;

    public void locateGame() throws Exception {
        // Load game.json from the classpath
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("game.json")) {
            if (input == null) {
                throw new RuntimeException("Could not find game.json on the classpath");
            }

            // Parse JSON into a GameConfig object
            config = gson.fromJson(new InputStreamReader(input), GameConfig.class);

            if (config.mainClass() == null || config.mainClass().isEmpty()) {
                throw new RuntimeException("Missing field: mainClass in game.json");
            }

            // Load and instantiate the Game
            Class<?> clazz = Class.forName(config.mainClass());
            Object instance = clazz.getDeclaredConstructor().newInstance();

            if (!(instance instanceof Game)) {
                throw new RuntimeException("Class " + config.mainClass() + " does not implement Game interface");
            }

            this.game = (Game) instance;
        }
    }

    public Game getGame() {
        return game;
    }

    public GameConfig getConfig() {
        return config;
    }
}
