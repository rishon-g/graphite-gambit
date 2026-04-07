package Screens;

import Game.AudioManager;
import Game.GdxGame;
import Game.PlayerData;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Main Menu, displays on game load and switches to game screen on input
 * 
 * @author Luke McRae
 * @version 2.1
 * @since 2026-03-16
 */
public class MainMenuScreen extends ScreenAdapter {
    private final GdxGame game;
    private final Batch batch;
    private final BitmapFont font;
    private final BitmapFont header_font;
    private final Viewport viewport = new FitViewport(1920, 1080);
    private final GlyphLayout layout = new GlyphLayout();
    private final ScreenManager screenManager;
    int screenWidth = 1920;
    int screenHeight = 1080;
    private int selectedIndex = -1;
    private Texture background;
    Texture normalButton;
    double scale = 1;

    private Texture eraser;
    private Texture pencilsharpener;
    private Texture ink;
    private Texture whiteOut;
    private Texture trophy;
    private Texture graphite;
    private Texture plot;

    float howToPlayStartX = 300;
    float howToPlayIconSize = 50f;

    public boolean musicOn = true;
    public boolean sfxOn = true;

    // create buttons
    private final MenuButton[] menuButtons;
    private MenuButton[] buttons;
    MenuButton[] levelSelectButtons;
    MenuButton[] settingsButtons = new MenuButton[] {};
    // togglable buttons
    MenuButton musicOnButton;
    MenuButton musicOffButton;
    MenuButton sfxOnButton;
    MenuButton sfxOffButton;
    private final MenuButton backButton;
    private MenuButton[] howToPlayButtons;

    // Different sets of menu buttons that can be displayed
    enum Layout {
        MAIN, LEVEL_SELECT, SETTINGS, HOW_TO_PLAY
    }
    Layout currentLayout = Layout.MAIN;

    /**
     * Constructor for the main menu. retrieves rendering resources, save data, and
     * audio.
     * 
     * @param game the game object of this main menu
     */
    public MainMenuScreen(GdxGame game) {
        this.game = game;
        this.batch = game.getBatch();
        this.font = game.getMenuFont();
        this.screenManager = ScreenManager.getInstance(game);
        this.header_font = game.getHeaderFont();

        // only load textures if not testing
        if (!GdxGame.isTestMode()) {
            plot = new Texture(Gdx.files.internal("sprites/Plot.png"));
            graphite = new Texture(Gdx.files.internal("sprites/graphite.png"));
            trophy = new Texture(Gdx.files.internal("sprites/trophy.png"));
            whiteOut = new Texture(Gdx.files.internal("sprites/whiteout_large.png"));
            ink = new Texture(Gdx.files.internal("sprites/ink.png"));
            pencilsharpener = new Texture(Gdx.files.internal("sprites/pencilsharpener.png"));
            eraser = new Texture(Gdx.files.internal("sprites/eraser.png"));
            background = new Texture(Gdx.files.internal("images/menu-background.png"));

            // Back button in how to play screen
            howToPlayButtons = new MenuButton[] {
                    new MenuButton("BACK", (screenWidth >> 1) - (800 >> 1), 100, scale, false, () -> changeLayout(Layout.MAIN)),
            };
        }

        // buttons
        menuButtons = new MenuButton[] {
                MenuButton.createCenteredButton("START GAME", 0, scale, false, () -> screenManager.SetGameScreen(1)),
                MenuButton.createCenteredButton("LEVEL SELECT", 1, scale, false, () -> changeLayout(Layout.LEVEL_SELECT)),
                MenuButton.createCenteredButton("HOW TO PLAY", 2, scale, false, () -> changeLayout(Layout.HOW_TO_PLAY)),
                MenuButton.createCenteredButton("SETTINGS", 3, scale, false, () -> changeLayout(Layout.SETTINGS)),
                MenuButton.createCenteredButton("QUIT", 4, scale, false, () -> Gdx.app.exit()),
        };
        musicOnButton = MenuButton.createCenteredButton("MUSIC: ON", 1, scale, false, this::toggleMusic);
        musicOffButton = MenuButton.createCenteredButton("MUSIC: OFF", 1, scale, false, this::toggleMusic);
        sfxOnButton = MenuButton.createCenteredButton("SOUND EFFECTS: ON", 2, scale, false, this::toggleSfx);
        sfxOffButton = MenuButton.createCenteredButton("SOUND EFFECTS: OFF", 2, scale, false, this::toggleSfx);
        backButton = MenuButton.createCenteredButton("BACK", 0, scale, false, () -> changeLayout(Layout.MAIN));

        levelSelectButtons = levelButtons();
        buttons = menuButtons;
        AudioManager.getInstance(game).setMusicHalfVolume();
    }

    /**
     * Creates an array of MenuButton objects for the level select screen.
     * Includes a back button and buttons for each level, with scores and disabled state based on unlocked levels.
     *
     * @return an array of MenuButton for level selection
     */
    private MenuButton[] levelButtons() {
        MenuButton[] buttons = new MenuButton[5];
        buttons[0] = backButton;
        int highestLevel = 1;
        if (PlayerData.obtainPlayerData() != null) {
            highestLevel = PlayerData.obtainPlayerData().getLevelUnlocked();
        }
        for (int i = 1; i < 5; i++) {
            int highScore = 0;
            if (PlayerData.obtainPlayerData() != null) {
                try {
                    highScore = PlayerData.obtainPlayerData().getScore(i);
                } catch (IndexOutOfBoundsException e) {
                    // If the score doesn't exist yet, just leave it at 0
                }
            }
            if (highestLevel >= i) {
                int finalI = i;
                buttons[i] = MenuButton.createCenteredButton("LEVEL " + (i) + "    Score: " + highScore, i, scale, false, () -> screenManager.SetGameScreen(finalI));
            } else {
                buttons[i] = MenuButton.createCenteredButton("LEVEL " + (i), i, scale, true, null);
            }
        }
        return buttons;
    }

    /**
     * Changes the current layout of the ui to the specified layout.
     * 
     * @param layout the layout to swap to
     */
    void changeLayout(Layout layout) {
        currentLayout = layout;
        MenuButton music;
        MenuButton sfx;
        // set settings buttons to match stored settings
        if (game.isSfxPlaying()) {
            sfx = sfxOnButton;
        } else {
            sfx = sfxOffButton;
        }
        if (game.isMusicPlaying()) {
            music = musicOnButton;
        } else {
            music = musicOffButton;
        }
        settingsButtons = new MenuButton[] {
                backButton,
                music,
                sfx,
        };

        buttons = switch (layout) {
            case MAIN -> menuButtons;
            case LEVEL_SELECT -> levelSelectButtons;
            case SETTINGS -> settingsButtons;
            case HOW_TO_PLAY -> howToPlayButtons;
        };
    }

    private void setHeader(String text) {
        layout.setText(header_font, text);
        header_font.draw(batch, layout, viewport.getWorldWidth() / 2 - layout.width / 2, 950);
    }

    private void setHowToPlayText(String text, Texture texture, float y) {
        layout.setText(font, text);
        font.draw(batch, layout, howToPlayStartX, y);
        // Draw image exactly after the text ends! (Adjust the -35 offset to center it
        // vertically with the text)
        batch.draw(texture, howToPlayStartX + layout.width, y - 35f, howToPlayIconSize, howToPlayIconSize);
    }

    /**
     * Renders the ui and background of the main menu.
     * Varies what buttons are rendered based on the current layout.
     * 
     * @param delta the time since the last update
     */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        // draw background
        batch.draw(background, 0, 0, screenWidth, screenHeight);

        switch (currentLayout) {
            case MAIN:
                setHeader("GRAPHITE GAMBIT");
                break;

            case LEVEL_SELECT:
                setHeader("LEVEL SELECT");
                break;

            case SETTINGS:
                setHeader("SETTINGS");
                break;

            case HOW_TO_PLAY:
                setHeader("HOW TO PLAY");
            // layout
            float currentY = 820f;
            float lineSpacing = 70f;

            setHowToPlayText("Avoid ERASERS!  ", eraser, currentY);
            // Move down to the next line
            setHowToPlayText("Watch out for WHITE-OUT  ", whiteOut, currentY -= lineSpacing);
            setHowToPlayText("and INK SPILLS!  ", ink, currentY -= 45);
            setHowToPlayText("Don't get caught by PENCIL SHARPENERS!  ", pencilsharpener, currentY -= lineSpacing);
            setHowToPlayText("Collect GRAPHITE!  ", graphite, currentY -= lineSpacing);
            setHowToPlayText("Collect PLOT POINTS!  ", plot, currentY -= lineSpacing);
            String line5 = "Navigate to the END CELL after all PLOT POINTS  ";
            layout.setText(font, line5);
            font.draw(batch, layout, howToPlayStartX, currentY -= lineSpacing);
            setHowToPlayText("are collected to WIN!  ", trophy, currentY - 45);
            break;
        }

        // render buttons
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].render(batch, font, i == selectedIndex);
            if (buttons[i].isHovered(viewport)) {
                if (selectedIndex != i) {
                    selectedIndex = i;
                    AudioManager.getInstance().playHover();
                }
            }
            if (buttons[i].isClicked(viewport)) {
                AudioManager.getInstance().playClick();
                buttons[i].click();
                break;
            }
        }
        boolean buttonHovered = false;
        for (MenuButton button : buttons) {
            if (button.isHovered(viewport)) {
                buttonHovered = true;
            }
        }
        if (!buttonHovered)
            selectedIndex = -1;
        batch.end();
    }

    /**
     * Helper function to toggle music on/off
     */
    public void toggleMusic() {
        boolean musicNowOn = !game.isMusicPlaying();
        AudioManager.getInstance(game).setMusicEnabled(musicNowOn);
        changeLayout(currentLayout); // Refresh layout to pick up new button
    }

    /**
     * Helper function to toggle sfx on/off
     */
    public void toggleSfx() {
        boolean sfxNowOn = !game.isSfxPlaying();
        AudioManager.getInstance(game).setSfxEnabled(sfxNowOn);
        changeLayout(currentLayout);
    }

    /**
     * Called when the screen is resized. Updates the viewport to match the new dimensions.
     *
     * @param width the new width of the screen
     * @param height the new height of the screen
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    /**
     * Disposes of resources used by the main menu screen, including textures.
     */
    @Override
    public void dispose() {
        background.dispose();
        normalButton.dispose();
        plot.dispose();
        for (MenuButton button : buttons) {
            button.dispose();
        }
        eraser.dispose();
        pencilsharpener.dispose();
        ink.dispose();
        whiteOut.dispose();
        graphite.dispose();
        trophy.dispose();
    }
}