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
    private final PlayerData playerData;
    int screenWidth = 1920;
    int screenHeight = 1080;
    private int selectedIndex = -1;
    private Texture background;
    Texture normalButton;
    private Texture highlightedButton;
    private Texture disabledButton;

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
        this.playerData = PlayerData.obtainPlayerData();
        // only load textures if not testing
        if (!GdxGame.isTestMode()) {
            plot = new Texture(Gdx.files.internal("sprites/Plot.png"));
            graphite = new Texture(Gdx.files.internal("sprites/graphite.png"));
            trophy = new Texture(Gdx.files.internal("sprites/trophy.png"));
            whiteOut = new Texture(Gdx.files.internal("sprites/whiteout_large.png"));
            ink = new Texture(Gdx.files.internal("sprites/ink.png"));
            pencilsharpener = new Texture(Gdx.files.internal("sprites/pencilsharpener.png"));
            eraser = new Texture(Gdx.files.internal("sprites/eraser.png"));
            disabledButton = new Texture(Gdx.files.internal("images/menu-button-disabled.png"));
            highlightedButton = new Texture(Gdx.files.internal("images/menu-button-highlighted.png"));
            normalButton = new Texture(Gdx.files.internal("images/menu-button.png"));
            background = new Texture(Gdx.files.internal("images/menu-background.png"));
            howToPlayButtons = new MenuButton[] {
                    new MenuButton("BACK", normalButton, highlightedButton, (screenWidth >> 1) - (800 >> 1), 100, 800, 80, false),
            };
        } else {
            howToPlayButtons = new MenuButton[] {
                    new MenuButton("BACK", null, null, (screenWidth >> 1) - (800 >> 1), 100, 800, 80, false),
            };
        }
        // buttons
        menuButtons = new MenuButton[] {
                createCenteredButton("START GAME", 0, false),
                createCenteredButton("LEVEL SELECT", 1, false),
                createCenteredButton("HOW TO PLAY", 2, false),
                createCenteredButton("SETTINGS", 3, false),
                createCenteredButton("QUIT", 4, false),
        };
        musicOnButton = createCenteredButton("MUSIC: ON", 1, false);
        buttons = menuButtons;
        musicOffButton = createCenteredButton("MUSIC: OFF", 1, false);
        levelSelectButtons = levelButtons();
        sfxOnButton = createCenteredButton("SOUND EFFECTS: ON", 2, false);
        sfxOffButton = createCenteredButton("SOUND EFFECTS: OFF", 2, false);
        backButton = createCenteredButton("BACK", 0, false);

        AudioManager.getInstance(game).setMusicHalfVolume();
    }
    /**
     * helper function to create uniform, centered buttons on the screen that are
     * spaced out by deltaY automatically, in the order they are created
     * change variables as needed to adjust spacing and size
     * 
     * @param text the text to display on the button
     * @return a new MenuButton object to add to the buttons array
     */
    //TODO make global
    private MenuButton createCenteredButton(String text, int ID, boolean isDisabled) {
        // global parameters for buttons
        int startingY = 700;
        int spacing = 20;
        int height = 80;
        int width = 800;

        int deltaY = -(height + spacing);
        if (GdxGame.isTestMode() && isDisabled) {
            return new MenuButton(text, null, null, (screenWidth >> 1) - (width >> 1),
                    startingY + (deltaY * ID), width, height, true);
        } else if (GdxGame.isTestMode() && !isDisabled) {
                return new MenuButton(text, null, null, (screenWidth >> 1) - (width >> 1),
                        startingY + (deltaY * ID), width, height, false);
        } else if (isDisabled) {
            return new MenuButton(text, disabledButton, disabledButton, (screenWidth >> 1) - (width >> 1),
                    startingY + (deltaY * ID), width, height, true);
        } else {
            return new MenuButton(text, normalButton, highlightedButton, (screenWidth >> 1) - (width >> 1),
                    startingY + (deltaY * ID), width, height, false);
        }
    }

    // create buttons
    private MenuButton[] menuButtons;
    private MenuButton[] buttons;
    MenuButton[] levelSelectButtons;
    MenuButton[] settingsButtons = new MenuButton[] {};
    // togglable buttons
    MenuButton musicOnButton;
    MenuButton musicOffButton;
    MenuButton sfxOnButton;
    MenuButton sfxOffButton;
    private MenuButton backButton;

    private MenuButton[] howToPlayButtons;
    /**
     * Creates an array of MenuButton objects for the level select screen.
     * Includes a back button and buttons for each level, with scores and disabled state based on unlocked levels.
     *
     * @return an array of MenuButton for level selection
     */
    private MenuButton[] levelButtons() {
        MenuButton[] buttons = new MenuButton[5];
        buttons[0] = createCenteredButton("BACK", 0, false);
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
                buttons[i] = createCenteredButton("LEVEL " + (i) + "    Score: " + highScore, i, false);
            } else {
                buttons[i] = createCenteredButton("LEVEL " + (i), i, true);
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

        //TODO switch case or reduce somehow FIXED
        // only difference is text
        //TODO add test

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

            //TODO repeated code, make new mehod FIXED
            // --- LINE 1: ERASERS ---

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
                activateButton(i);
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
     * triggers a hardcoded action for each button based on index
     *
     * @param index clicked button index (index is based on time of creation)
     */
    void activateButton(int index) {
        //TODO switch case, maybe more
        if (buttons[index].isDisabled())
            return;
        if (currentLayout == Layout.MAIN) {
            if (index == 0) {
                screenManager.SetGameScreen(1);
            }
            if (index == 1) {
                // switch to level select screen
                changeLayout(Layout.LEVEL_SELECT);
            }
            if (index == 2) {
                // clear screen and display how to play screen
                changeLayout(Layout.HOW_TO_PLAY);
            }
            if (index == 3) {
                // clear screen and display settings screen
                changeLayout(Layout.SETTINGS);
            }
            if (index == 4) {
                Gdx.app.exit();
            }
        } else if (currentLayout == Layout.LEVEL_SELECT) {
            //TODO setgamescreen(index) FIXED
            if (index == 0) {
                changeLayout(Layout.MAIN);
            } else {
                screenManager.SetGameScreen(index);
            }
        } else if (currentLayout == Layout.SETTINGS) {
            if (index == 0) {
                changeLayout(Layout.MAIN);
            }
            if (index == 1) {
                // toggle music
                boolean nowOn = !game.isMusicPlaying();
                AudioManager.getInstance(game).setMusicEnabled(nowOn);
                settingsButtons[1] = nowOn ? musicOnButton : musicOffButton;
            }
            if (index == 2) {
                // toggle sfx
                boolean nowOn = !game.isSfxPlaying();
                AudioManager.getInstance(game).setSfxEnabled(nowOn);
                settingsButtons[2] = nowOn ? sfxOnButton : sfxOffButton;
            }
        } else if (currentLayout == Layout.HOW_TO_PLAY) {
            if (index == 0) {
                changeLayout(Layout.MAIN);
            }
        }
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
        highlightedButton.dispose();
        disabledButton.dispose();
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