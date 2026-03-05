package Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.Game;

/**
 * GdxGame is executed when the game is started, initializes global resources, and sets the initial screen.
 * @author Luke McRae
 * @version 1.0
 */
public class GdxGame extends Game {
    private SpriteBatch batch;
    private BitmapFont font;
    private ScreenManager screenManager;
    @Override
    public void create() {
        batch = new SpriteBatch();
        screenManager = new ScreenManager();

        // load default font
        var generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto.ttf"));
        var fontParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParams.size = 32;
        fontParams.color = Color.WHITE;
        font = generator.generateFont(fontParams);
        generator.dispose();

        // set the initial screen
        // if data exists, load level, otherwise load menu
        screenManager.switchScreen(new MainMenuScreen(this));
        screenManager.push(new PauseScreen(this));
    }

    @Override
    public void render() {
        screenManager.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        screenManager.resize(width, height);
    }

    /**
     * Changes between screen objects
     * @param screen the screen to change to (MainMenuScreen, GameScreen, etc)
     */
    public void changeScreen(Screen screen) {
        screenManager.switchScreen(screen);
    }

    public void overlayScreen(Screen screen) {
        screenManager.push(screen);
    }

    public void popScreen() {
        screenManager.pop();
    }

    public int numScreens() {
        return screenManager.getSize();
    }

    @Override
    public void dispose() {
        super.dispose();
        screenManager.dispose();
        batch.dispose();
        font.dispose();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public BitmapFont getFont() {
        return font;
    }
}