package Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Main Menu, displays on game load and switches to game screen on input
 * @author Luke McRae
 * @version 2.0
 * @since 2026-03-14
 */
public class MainMenuScreen extends ScreenAdapter {
    private final GdxGame game;
    private final Batch batch;
    private final BitmapFont font;
    private final BitmapFont header_font;
    private final Viewport viewport = new ScreenViewport();
    private final GlyphLayout layout = new GlyphLayout();
    private final ScreenManager screenManager;
    private final PlayerData playerData;
    int screenWidth = Gdx.graphics.getWidth();
    int screenHeight = Gdx.graphics.getHeight();
    private int selectedIndex = -1;
    private final Texture background = new Texture(Gdx.files.internal("images/menu-background.png"));
    private final Texture normalButton = new Texture(Gdx.files.internal("images/menu-button.png"));
    private final Texture highlightedButton = new Texture(Gdx.files.internal("images/menu-button-highlighted.png"));

    int buttonCount = -1;
    // helper function to create uniform, centered buttons on the screen that are spaced out by deltaY automatically, in the order they are created

    /**
     * helper function to create uniform, centered buttons on the screen that are spaced out by deltaY automatically, in the order they are created
     * change variables as needed to adjust spacing and size
     * @param text the text to display on the button
     * @return a new MenuButton object to add to the buttons array
     */
    private MenuButton createCenteredButton(String text) {
        // global parameters for buttons
        int startingY = 700;
        int spacing = 20;
        int height = 80;
        int width = 800;

        int deltaY = -(height + spacing);
        buttonCount++;
        return new MenuButton(text, normalButton, highlightedButton, (screenWidth >> 1) - (width >> 1), startingY + (deltaY * buttonCount), width, height);
    }

    // create buttons
    private final MenuButton[] buttons = new MenuButton[] {
            createCenteredButton("START GAME"),
            createCenteredButton("CONTINUE"),
            createCenteredButton("HOW TO PLAY"),
            createCenteredButton("SETTINGS"),
            createCenteredButton("QUIT"),
    };

    public MainMenuScreen(GdxGame game) {
        this.game = game;
        this.batch = game.getBatch();
        this.font = game.getFont();
        this.screenManager = ScreenManager.getInstance(game);
        this.header_font = game.getHeaderFont();
        this.playerData = PlayerData.obtainPlayerData();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        // draw background
        batch.draw(background, 0, 0, screenWidth, screenHeight);

        // render title
        layout.setText(header_font, "GRAPHITE GAMBIT");
        header_font.draw(batch, layout, viewport.getWorldWidth() / 2 - layout.width / 2, 950);

        // render buttons
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].render(batch, font, i == selectedIndex);
            if (buttons[i].isHovered(viewport)) selectedIndex = i;
            if (buttons[i].isClicked(viewport)) activateButton(i);
        }
        boolean buttonHovered = false;
        for (int i = 0; i < buttons.length; i++) {
            if(buttons[i].isHovered(viewport)) buttonHovered = true;
        }
        if (!buttonHovered) selectedIndex = -1;
        batch.end();
    }

    /**
     * triggers a hardcoded action for each button based on index
     * @param index clicked button index (index is based on time of creation)
     */
    private void activateButton(int index) {
        if (index == 0) {
            screenManager.SetGameScreen(1);
        }
        if (index == 1) {
            // switch to level select screen
        }
        if (index == 2) {
            // clear screen and display how to play screen
        }
        if (index == 3) {
            // clear screen and display settings screen
        }
        if (index == 4) {
            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
}