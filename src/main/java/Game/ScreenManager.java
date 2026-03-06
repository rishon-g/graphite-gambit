package Game;

import com.badlogic.gdx.Screen;
import com.sun.tools.javac.Main;

import java.util.Stack;

/**
 *
 * @author Luke McRae
 * @version 1.1
 */
public class ScreenManager {
    GdxGame game;
    private ScreenManager() {
        // singleton
    }
    // initialize singleton
    private static ScreenManager instance;
    public static ScreenManager getInstance(GdxGame game) {
        if (instance == null) {
            instance = new ScreenManager();
            instance.game = game;
        }
        return instance;
    }

    public void SetMenuScreen(PlayerData data) {
       game.setScreen(new MainMenuScreen(game, data));
    }

    public void SetGameScreen(int id) {
        game.setScreen(new GameScreen(game, id));
    }

    public void render(float delta) {
    }

    public void resize(int width, int height) {
    }

    public void dispose() {
    }
}