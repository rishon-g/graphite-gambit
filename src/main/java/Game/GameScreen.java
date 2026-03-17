package Game;

import Game.Worlds.Asset.AssetService;
import Game.Worlds.Asset.MapAsset;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class GameScreen implements Screen {

    // reference to the main game class, used for switching screens.
    GdxGame game;

    ScreenManager screenManager = ScreenManager.getInstance(game);

    // for camera
    Viewport viewport;

    // render maps and assets
    AssetService assetService;
    OrthogonalTiledMapRenderer mapRenderer;

    // the game world that contains all entities and tilemap data for this screen
    GameWorld world;

    // camera used for rendering the game world
    OrthographicCamera camera;

    // playerData for getting max available levels
    PlayerData playerData = PlayerData.obtainPlayerData();

    // UI
    Texture uiTexture;
    BitmapFont font;
    BitmapFont menuFont;
    BitmapFont headerFont;
    BitmapFont smallHeaderFont;
    GlyphLayout layout = new GlyphLayout();
    OrthographicCamera uiCamera;
    Viewport uiViewport;
    int selectedIndex = -1;

    // sprite batch used for rendering entities
    SpriteBatch batch;

    // state variables
    boolean paused = false;
    boolean gameOver = false;
    boolean gameWon = false;

    // tracked stats
    public int plotPoints = 0;

    // menu textures
    private final Texture normalButton = new Texture(Gdx.files.internal("images/menu-button.png"));
    private final Texture highlightedButton = new Texture(Gdx.files.internal("images/menu-button-highlighted.png"));
    private final Texture textBox = new Texture(Gdx.files.internal("images/menu-text-box.png"));

    // menu buttons
    private final MenuButton[] mainButtons = new MenuButton[] {
            new MenuButton("RESUME", normalButton, highlightedButton, (1920 >> 1) - (700 >> 1), 600, 700, 70),
            new MenuButton("RESTART", normalButton, highlightedButton, (1920 >> 1) - (700 >> 1), 510, 700, 70),
            new MenuButton("SETTINGS", normalButton, highlightedButton, (1920 >> 1) - (700 >> 1), 420, 700, 70),
            new MenuButton("SAVE & QUIT", normalButton, highlightedButton, (1920 >> 1) - (700 >> 1), 330, 700, 70),
    };

    // settings buttons, instance for each toggled state to switch between
    MenuButton musicOnButton = new MenuButton("MUSIC: ON", normalButton, highlightedButton, (1920 >> 1) - (700 >> 1),
            510, 700, 70);
    MenuButton musicOffButton = new MenuButton("MUSIC: OFF", normalButton, highlightedButton, (1920 >> 1) - (700 >> 1),
            510, 700, 70);
    MenuButton sfxOnButton = new MenuButton("SOUND EFFECTS: ON", normalButton, highlightedButton,
            (1920 >> 1) - (700 >> 1), 420, 700, 70);
    MenuButton sfxOffButton = new MenuButton("SOUND EFFECTS: OFF", normalButton, highlightedButton,
            (1920 >> 1) - (700 >> 1), 420, 700, 70);
    MenuButton backButton = new MenuButton("BACK", normalButton, highlightedButton, (1920 >> 1) - (700 >> 1), 600, 700,
            70);
    MenuButton[] settingsButtons;

    // game over buttons
    private final MenuButton[] gameOverButtons = new MenuButton[] {
            new MenuButton("RESTART LEVEL", normalButton, highlightedButton, (1920 >> 1) - (700 >> 1), 420, 700, 70),
            new MenuButton("QUIT TO MENU", normalButton, highlightedButton, (1920 >> 1) - (700 >> 1), 330, 700, 70),
    };

    // game win buttons
    private final MenuButton[] gameWonButtons = new MenuButton[] {
            new MenuButton("NEXT LEVEL", normalButton, highlightedButton, (1920 >> 1) - (700 >> 1), 420, 700, 70),
            new MenuButton("QUIT TO MENU", normalButton, highlightedButton, (1920 >> 1) - (700 >> 1), 330, 700, 70),
    };

    private enum Layout {
        MAIN, SETTINGS, WON, LOST
    }

    private GameScreen.Layout currentLayout = GameScreen.Layout.MAIN;

    // switch sets of buttons based on the current layout
    private MenuButton[] menuButtons = mainButtons;

    /**
     * Switches the current layout of the ui within the game.
     * 
     * @param layout the layout to swap to.
     */
    private void changeLayout(GameScreen.Layout layout) {
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

        currentLayout = layout;
        menuButtons = switch (layout) {
            case MAIN -> mainButtons;
            case SETTINGS -> settingsButtons;
            case WON -> gameWonButtons;
            case LOST -> gameOverButtons;
        };
    }

    /**
     * Constructor for the GameScreen class. Initializes all resources and the
     * GameWorld.
     * 
     * @param game reference to the main game class.
     */
    public GameScreen(GdxGame game, int id) {
        this.game = game;
        this.camera = game.getCamera();
        this.batch = game.getBatch();
        this.viewport = game.getViewport();
        this.assetService = game.getAssetService();

        // determine which level asset to use based on the ID
        MapAsset currentLevel = MapAsset.getLevelAsset(id);

        // load the asset
        this.assetService.load(currentLevel);

        // then, load the world
        this.world = new WorldLoader().loadWorld(game, this, id);

        // initialize the renderer with the newly loaded map
        this.mapRenderer = new OrthogonalTiledMapRenderer(
                this.assetService.get(currentLevel),
                GdxGame.UNIT_SCALE,
                this.batch);

        // UI: this handles everything overlayed on the screen
        this.font = game.getFont();
        this.menuFont = game.getMenuFont();
        this.headerFont = game.getHeaderFont();
        this.smallHeaderFont = game.getSmallHeaderFont();
        this.uiCamera = new OrthographicCamera();
        // 1920x1080 canvas for the HUD that stretches to fit the window
        this.uiViewport = new FitViewport(1920, 1080, uiCamera);

        // generate a 1x1 white pixel texture for drawing the health bar shapes
        // TODO we need to add a nicer looking texture
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.uiTexture = new Texture(pixmap);
        pixmap.dispose();

    }

    @Override
    public void show() {
        AudioManager.getInstance().setMusicFullVolume();
    }

    /**
     * The render method is called every frame, and is responsible for updating the
     * game world and rendering all entities.
     * 
     * @param delta time since last frame (used for uniform movement and animations)
     */
    @Override
    public void render(float delta) {
        if (!paused && !gameOver && !gameWon) {
            // run update loops
            world.update(delta);
        } else {
            AudioManager.getInstance(game).stopMoveSound();
            AudioManager.getInstance(game).stopSharpenerSound();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !gameOver && !gameWon) {
            // toggle pause
            if (paused) {
                gameUnpause();
            } else {
                gamePause();
            }
            changeLayout(Layout.MAIN);
        }

        // follow with camera
        world.updateCamera(camera);
        camera.update();
        viewport.apply();

        // wipe the screen
        ScreenUtils.clear(Color.BLACK);

        // draw map
        this.batch.setColor(Color.WHITE);

        // expand the camera's "culling box" by 5 world units such that big objects do
        // not despawn
        float bleed = 5f;
        float startX = this.camera.position.x - (this.camera.viewportWidth / 2f) - bleed;
        float startY = this.camera.position.y - (this.camera.viewportHeight / 2f) - bleed;
        float boxWidth = this.camera.viewportWidth + (bleed * 2);
        float boxHeight = this.camera.viewportHeight + (bleed * 2);

        this.mapRenderer.setView(this.camera.combined, startX, startY, boxWidth, boxHeight);
        this.mapRenderer.render();

        // draw entities
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        world.render(batch, delta);
        batch.end();

        // static UI overlay
        uiViewport.apply(); // switch to the UI canvas
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        // draw time
        int seconds = (int) world.time;
        font.draw(batch, "TIME: " + seconds, 50, 1030);

        // draw score
        String scoreText = "SCORE: " + world.getScore();
        layout.setText(font, scoreText);
        // subtract layout width so it perfectly aligns to the right edge
        font.draw(batch, layout, 1920 - layout.width - 50, 1030);

        // draw graphite bar
        float barWidth = 800f;
        float barHeight = 40f;
        float barX = (1920f - barWidth) / 2f; // mathematical centering
        float barY = 50f; //

        // calculate health percentage
        float healthPct = (float) world.player.getHealth() / 100f;

        // draw the border
        batch.setColor(Color.BLACK);
        batch.draw(uiTexture, barX - 4f, barY - 4f, barWidth + 8f, barHeight + 8f);

        // draw a light-grey background for the bar
        batch.setColor(Color.LIGHT_GRAY);
        batch.draw(uiTexture, barX, barY, barWidth, barHeight);

        // draw the dark grey filled portion
        batch.setColor(Color.DARK_GRAY);
        // dynamically changes according to health at the moment
        batch.draw(uiTexture, barX, barY, barWidth * healthPct, barHeight);

        // reset batch color to white before drawing text
        batch.setColor(Color.WHITE);

        // draw graphite text
        String graphiteText = "GRAPHITE";
        layout.setText(font, graphiteText);
        font.draw(batch, layout, (1920f - layout.width) / 2f, barY + 30f);

        // pause menu
        if (paused || gameOver || gameWon) {
            // transparent overlay
            batch.setColor(0, 0, 0, 0.7f);
            batch.draw(uiTexture, 0, 0, 1920, 1080);
            batch.setColor(Color.WHITE);

            // create pause menu
            batch.draw(textBox, 1920 / 2 - 400, 1080 / 2 - 300, 800, 600);
            if (paused) {
                layout.setText(headerFont, "PAUSED");
                headerFont.draw(batch, layout, 1920 / 2 - layout.width / 2, 780);
            } else if (gameOver) {
                layout.setText(headerFont, "GAME OVER");
                headerFont.draw(batch, layout, 1920 / 2 - layout.width / 2, 780);

                layout.setText(menuFont, "SCORE: " + world.getScore());
                menuFont.draw(batch, layout, 1920 / 2 - 300, 670);

                layout.setText(menuFont, "TIME REMAINING: " + seconds);
                menuFont.draw(batch, layout, 1920 / 2 - 300, 620);

                layout.setText(menuFont, "PLOT POINTS: " + plotPoints);
                menuFont.draw(batch, layout, 1920 / 2 - 300, 570);
            } else if (gameWon) {
                layout.setText(smallHeaderFont, "LEVEL COMPLETE!");
                smallHeaderFont.draw(batch, layout, 1920 / 2 - layout.width / 2, 780);

                layout.setText(menuFont, "LEVEL " + world.getId());
                menuFont.draw(batch, layout, 1920 / 2 - 300, 680);

                layout.setText(menuFont, "SCORE: " + (world.getScore() - seconds));
                menuFont.draw(batch, layout, 1920 / 2 - 300, 630);

                layout.setText(menuFont, "TIME BONUS: " + seconds);
                menuFont.draw(batch, layout, 1920 / 2 - 300, 580);

                layout.setText(menuFont, "FINAL SCORE: " + world.getScore());
                menuFont.draw(batch, layout, 1920 / 2 - 300, 530);
            }

            // render buttons
            for (int i = 0; i < menuButtons.length; i++) {
                menuButtons[i].render(batch, menuFont, i == selectedIndex);
                if (menuButtons[i].isHovered(uiViewport)) {
                    if (selectedIndex != i) {
                        selectedIndex = i;
                        AudioManager.getInstance().playHover();
                    }
                }
                if (menuButtons[i].isClicked(uiViewport)) {
                    AudioManager.getInstance().playClick();
                    activateButton(i);
                    break;
                }
            }
            boolean buttonHovered = false;
            for (MenuButton button : menuButtons) {
                if (button.isHovered(uiViewport))
                    buttonHovered = true;
            }
            if (!buttonHovered)
                selectedIndex = -1;
        }

        // draw stun
        if (world.getPlayer().isStunned && !gameOver && !gameWon) {

            font.getData().setScale(2.0f);

            layout.setText(font, "MASH SPACE!");

            // 3. Draw it dead center
            font.draw(batch, layout, (1920f - layout.width) / 2f, 700f);

            // 4. Clean the brush! (Crucial so your Score and Time don't turn red)
            font.getData().setScale(1.0f);
            font.setColor(Color.WHITE);
        }

        batch.end();

        // return the viewport to the world camera for the next frame
        viewport.apply();
    }

    /**
     * trigger code given the clicked button
     * 
     * @param index index of the clicked button in its button array
     */
    private void activateButton(int index) {
        if (currentLayout == Layout.MAIN) {
            if (index == 0) {
                gameUnpause();
            }
            if (index == 1) {
                screenManager.SetGameScreen(world.getId());
            }
            if (index == 2) {
                changeLayout(Layout.SETTINGS);
            }
            if (index == 3) {
                screenManager.SetMenuScreen();
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
        } else if (currentLayout == Layout.LOST) {
            if (index == 0) {
                screenManager.SetGameScreen(world.getId());
            }
            if (index == 1) {
                screenManager.SetMenuScreen();
            }
        } else if (currentLayout == Layout.WON) {
            if (index == 0) {
                if (playerData.getLevelUnlocked() >= world.getId() + 1) {
                    screenManager.SetGameScreen(world.getId() + 1);
                } else {
                    screenManager.SetMenuScreen();
                }
            }
            if (index == 1) {
                screenManager.SetMenuScreen();
            }
        }
    }

    /**
     * Pauses the game when called, and reduces music volume.
     */
    private void gamePause() {
        paused = true;
        AudioManager.getInstance(game).setMusicHalfVolume();
    }

    /**
     * unpauses the game when called, and resets music volume.
     */
    private void gameUnpause() {
        paused = false;
        AudioManager.getInstance(game).setMusicFullVolume();
    }

    /**
     * resizes the current viewport. called whenever the user resizes the window.
     * 
     * @param width  the new width of the window
     * @param height the new height of the window
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
    }

    /**
     * Dispose of all resources being used by the screen and the gameworld.
     * called once when screen is being changed, or game is being closed.
     */
    @Override
    public void dispose() {
        AudioManager.getInstance(game).stopMusic();
        AudioManager.getInstance(game).stopMoveSound();
        AudioManager.getInstance(game).stopSharpenerSound();
        world.dispose();
        mapRenderer.dispose();
        uiTexture.dispose();
    }

    /**
     * Handles the event that the GameWorld is finished with it's operation.
     * 
     * @param won true if the player gameWon, false if not
     */
    public void gameEnd(boolean won) {
        paused = false;
        gameWon = false;
        gameOver = false;
        AudioManager.getInstance(game).setMusicHalfVolume();
        int currentScore = world.getScore();

        // Updates the player save if won level
        if (won) {
            changeLayout(Layout.WON);
            gameWon = true;

            int timeBonus = (int) world.time;
            world.score(timeBonus);

            PlayerData d = PlayerData.obtainPlayerData();
            d.completeLevel(world.getId(), world.getScore());
        } else {
            // show game over menu
            changeLayout(Layout.LOST);
            gameOver = true;
        }
    }
}