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
import com.badlogic.gdx.utils.viewport.FitViewport;
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
    private final Viewport viewport = new FitViewport(1920, 1080);
    private final GlyphLayout layout = new GlyphLayout();
    private final ScreenManager screenManager;
    private final PlayerData playerData;
    int screenWidth = 1920;
    int screenHeight = 1080;
    private int selectedIndex = -1;
    private final Texture background = new Texture(Gdx.files.internal("images/menu-background.png"));
    private final Texture normalButton = new Texture(Gdx.files.internal("images/menu-button.png"));
    private final Texture highlightedButton = new Texture(Gdx.files.internal("images/menu-button-highlighted.png"));
    private final Texture disabledButton = new Texture(Gdx.files.internal("images/menu-button-disabled.png"));

    public boolean musicOn = true;
    public boolean sfxOn = true;

    private enum Layout {
        MAIN, LEVEL_SELECT, SETTINGS, HOW_TO_PLAY
    }
    private Layout currentLayout = Layout.MAIN;

    public MainMenuScreen(GdxGame game) {
        this.game = game;
        this.batch = game.getBatch();
        this.font = game.getMenuFont();
        this.screenManager = ScreenManager.getInstance(game);
        this.header_font = game.getHeaderFont();
        this.playerData = PlayerData.obtainPlayerData();
        AudioManager.getInstance(game).setMusicHalfVolume();
    }

    private void changeLayout(Layout layout) {
        currentLayout = layout;
        buttons = switch (layout) {
            case MAIN -> menuButtons;
            case LEVEL_SELECT -> levelButtons();
            case SETTINGS -> settingsButtons;
            case HOW_TO_PLAY -> howToPlayButtons;
        };
    }

    // helper function to create uniform, centered buttons on the screen that are spaced out by deltaY automatically, in the order they are created

    /**
     * helper function to create uniform, centered buttons on the screen that are spaced out by deltaY automatically, in the order they are created
     * change variables as needed to adjust spacing and size
     * @param text the text to display on the button
     * @return a new MenuButton object to add to the buttons array
     */
    private MenuButton createCenteredButton(String text, int ID, boolean isDisabled) {
        // global parameters for buttons
        int startingY = 700;
        int spacing = 20;
        int height = 80;
        int width = 800;

        int deltaY = -(height + spacing);
        if (isDisabled) {
            return new MenuButton(text, disabledButton, disabledButton, (screenWidth >> 1) - (width >> 1), startingY + (deltaY * ID), width, height);
        } else {
            return new MenuButton(text, normalButton, highlightedButton, (screenWidth >> 1) - (width >> 1), startingY + (deltaY * ID), width, height);
        }
    }

    // create buttons
    private final MenuButton[] menuButtons = new MenuButton[] {
            createCenteredButton("START GAME", 0, false),
            createCenteredButton("LEVEL SELECT", 1, false),
            createCenteredButton("HOW TO PLAY", 2, false),
            createCenteredButton("SETTINGS", 3, false),
            createCenteredButton("QUIT", 4, false),
    };
    private MenuButton[] buttons = menuButtons;

    private MenuButton[] levelButtons() {
        MenuButton[] buttons = new MenuButton[5];
        buttons[0] = createCenteredButton("BACK", 0, false);
        int highestLevel = 1;
        if (playerData != null) {
            highestLevel = playerData.getLevelUnlocked();
        }
        for (int i = 1; i < 5; i++) {
            int highScore = 0;
            if (playerData != null) {
                try {
                    highScore = playerData.getScore(i);
                } catch (IndexOutOfBoundsException e) {
                    // If the score doesn't exist yet, just leave it at 0!
                    highScore = 0;
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
    private final MenuButton[] levelSelectButtons = levelButtons();

    private final MenuButton musicOnButton = createCenteredButton("MUSIC: ON", 1, false);
    private final MenuButton musicOffButton = createCenteredButton("MUSIC: OFF", 1, false);
    private final MenuButton sfxOnButton = createCenteredButton("SOUND EFFECTS: ON", 2, false);
    private final MenuButton sfxOffButton = createCenteredButton("SOUND EFFECTS: OFF", 2, false);
    private final MenuButton backButton = createCenteredButton("BACK", 0, false);
    private MenuButton[] settingsButtons = new MenuButton[]{};

    private final MenuButton[] howToPlayButtons = new MenuButton[]{
            new MenuButton("BACK", normalButton, highlightedButton, (screenWidth >> 1) - (800 >> 1), 300, 800, 80),
    };

    private void changeLayout(Layout layout) {
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

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        // draw background
        batch.draw(background, 0, 0, screenWidth, screenHeight);

        if (currentLayout == Layout.MAIN) {
            // render title
            layout.setText(header_font, "GRAPHITE GAMBIT");
            header_font.draw(batch, layout, viewport.getWorldWidth() / 2 - layout.width / 2, 950);
        }

        if (currentLayout == Layout.LEVEL_SELECT) {
            layout.setText(header_font, "LEVEL SELECT");
            header_font.draw(batch, layout, viewport.getWorldWidth() / 2 - layout.width / 2, 950);
        }

        if (currentLayout == Layout.SETTINGS) {
            layout.setText(header_font, "SETTINGS");
            header_font.draw(batch, layout, viewport.getWorldWidth() / 2 - layout.width / 2, 950);
        }

        if (currentLayout == Layout.HOW_TO_PLAY) {
            layout.setText(header_font, "HOW TO PLAY");
            header_font.draw(batch, layout, viewport.getWorldWidth() / 2 - layout.width / 2, 950);
            layout.setText(font, """
                    This is the instructions of how to play
                    Will fill in when game is completed
                    -
                    -
                    -
                    -
                    -
                    -
                    -
                    -
                    """);
            font.draw(batch, layout, viewport.getWorldWidth() / 2 - layout.width / 2, 820);
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
        if (!buttonHovered) selectedIndex = -1;
        batch.end();
    }

    /**
     * triggers a hardcoded action for each button based on index
     * @param index clicked button index (index is based on time of creation)
     */
    private void activateButton(int index) {
        if (buttons[index].textureIs(disabledButton)) return;
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
            if (index == 0) {
                changeLayout(Layout.MAIN);
            }
            if (index == 1) {
                screenManager.SetGameScreen(1);
            }
            if (index == 2) {
                screenManager.SetGameScreen(2);
            }
            if (index == 3) {
                screenManager.SetGameScreen(3);
            }
            if (index == 4) {
                screenManager.SetGameScreen(4);
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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
}