package Game;

/**
 * The screen manager is a singleton that is responsible for initializing and switching the active screen on request.
 * @author Luke McRae
 * @version 1.1
 */
public class ScreenManager {
    GdxGame game;
    private static ScreenManager instance;

    private ScreenManager() {}

    // initialize singleton
    public static ScreenManager getInstance(GdxGame game) {
        if (instance == null) {
            instance = new ScreenManager();
            instance.game = game;
        }
        return instance;
    }

    public void SetMenuScreen() {
        game.setScreen(new MainMenuScreen(game));
    }

    public void SetGameScreen(int id) {
        game.setScreen(new GameScreen(game, id));
    }

    public void dispose(){
        // empty
    }
}