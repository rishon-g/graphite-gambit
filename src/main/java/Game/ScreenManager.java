package Game;

/**
 * The screen manager is a singleton that is responsible for initializing and
 * switching the active screen on request.
 * 
 * @author Luke McRae
 * @version 1.1
 */
public class ScreenManager {
    GdxGame game;
    private static ScreenManager instance;

    private ScreenManager() {
    }

    /**
     * Singleton getter for the screenmanager. if none exist, it will initalize
     * using the provided game.
     * 
     * @param game game object to initialize upon first load
     * @return the singleton instance
     */
    public static ScreenManager getInstance(GdxGame game) {
        if (instance == null) {
            instance = new ScreenManager();
            instance.game = game;
        }
        return instance;
    }

    /**
     * Changes the active screen to the main menu.
     */
    public void SetMenuScreen() {
        game.setScreen(new MainMenuScreen(game));
    }

    /**
     * Changes the active screen to the game and initializes the selected world.
     * 
     * @param id the level of the world to spawn
     */
    public void SetGameScreen(int id) {
        game.setScreen(new GameScreen(game, id));
    }

    public void dispose() {
        // empty
    }
}