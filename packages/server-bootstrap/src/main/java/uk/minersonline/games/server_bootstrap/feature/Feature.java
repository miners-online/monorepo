package uk.minersonline.games.server_bootstrap.feature;

import uk.minersonline.games.server_bootstrap.game.Game;

public interface Feature {
    void onInit(Game game);
    void onStart();
    void onStop();
}
