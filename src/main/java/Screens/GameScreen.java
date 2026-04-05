package Screens;

import Game.*;
import Asset.AssetService;
import Asset.MapAsset;
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

/**
 * The GameScreen class is responsible for managing what is displayed during gameplay, such as UI and world
 */
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
    double scale = 0.875;

    // sprite batch used for rendering entities
    SpriteBatch batch;

    // state variables
    boolean paused = false;
    boolean gameOver = false;
    boolean gameWon = false;

    // tracked stats
    public int plotPoints = 0;
    public void collectPlotPoint() {
        plotPoints++;
    }

    // menu textures
    private final Texture textBox;

    // menu buttons
    private MenuButton[] mainButtons;

    // settings buttons, instance for each toggled state to switch between
    MenuButton backButton;
    MenuButton sfxOffButton;
    MenuButton sfxOnButton;
    MenuButton musicOffButton;
    MenuButton musicOnButton;
    MenuButton[] settingsButtons;


    // game end buttons
    private MenuButton[] gameOverButtons;
    private MenuButton[] gameWonButtons;

    // Different menu screens that can be displayed
    enum Layout {
        MAIN, SETTINGS, WON, LOST
    }
    GameScreen.Layout currentLayout = GameScreen.Layout.MAIN;

    // switch sets of buttons based on the current layout
    private MenuButton[] menuButtons = mainButtons;

    /**
     * Switches the current layout of the ui within the game.
     *
     * @param layout the layout to swap to.
     */
    void changeLayout(GameScreen.Layout layout) {
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

        // assign layout buttons
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

        // Menu Textures
        textBox = new Texture(Gdx.files.internal("images/menu-text-box.png"));

        // Buttons
        mainButtons = new MenuButton[] {
                MenuButton.createCenteredButton("RESUME", 1, scale, false, () -> gameUnpause()),
                MenuButton.createCenteredButton("RESTART", 2, scale, false, () -> screenManager.SetGameScreen(world.getId())),
                MenuButton.createCenteredButton("SETTINGS", 3, scale, false, () -> changeLayout(Layout.SETTINGS)),
                MenuButton.createCenteredButton("SAVE & QUIT", 4, scale, false, () -> screenManager.SetMenuScreen()),
        };

        backButton = MenuButton.createCenteredButton("BACK", 1, scale, false, () -> changeLayout(Layout.MAIN));
        musicOnButton = MenuButton.createCenteredButton("MUSIC: ON", 2, scale, false, this::toggleMusic);
        musicOffButton = MenuButton.createCenteredButton("MUSIC: OFF", 2, scale, false, this::toggleMusic);
        sfxOnButton = MenuButton.createCenteredButton("SOUND EFFECTS: ON", 3, scale, false, this::toggleSfx);
        sfxOffButton = MenuButton.createCenteredButton("SOUND EFFECTS: OFF", 3, scale, false, this::toggleSfx);

        gameOverButtons = new MenuButton[] {
            MenuButton.createCenteredButton("RESTART LEVEL", 3, scale, false, () -> screenManager.SetGameScreen(world.getId())),
            MenuButton.createCenteredButton("QUIT TO MENU", 4, scale, false, () -> screenManager.SetMenuScreen()),
        };
        gameWonButtons = new MenuButton[] {
                MenuButton.createCenteredButton("NEXT LEVEL", 3, scale, false, () -> screenManager.SetGameScreen(world.getId() + 1)),
                MenuButton.createCenteredButton("QUIT TO MENU", 4, scale, false, () -> screenManager.SetMenuScreen()),
        };

        // generate a 1x1 white pixel texture for drawing the health bar shapes
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.uiTexture = new Texture(pixmap);
        pixmap.dispose();

    }

    /**
     * Test only constructor, skips libgdx and texture initialization
     * @param game game class, should be set to test mode
     */
    private GameScreen(GdxGame game) {
        // Construct without textures
        textBox = null;

        // Buttons
        //TODO remove test button creation with null textures FIXED BY MENUBUTTON
        this.game = game;
        this.playerData = PlayerData.obtainPlayerData();
        this.world = null;
        menuButtons = mainButtons;
        settingsButtons = new MenuButton[] {};
    }

    public static GameScreen createForTesting(GdxGame game) {
        return new GameScreen(game);
    }

    @Override
    public void show() {
        AudioManager.getInstance().setMusicFullVolume();
    }

    /**
     * The render method is called every frame, and is responsible for updating the
     * game world and rendering all entities.
     * When paused, the game stops updates to the world and displays the pause menu
     * When the player dies or completes the level, the appropriate menu is displayed and world updates stop
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
        float healthPct = (float) world.getPlayer().getHealth() / 100f;

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
                    menuButtons[i].click();
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

    // TODO switch cases activateButton simplification FIXED via deletion

    /**
     * Pauses the game when called, and reduces music volume.
     */
    void gamePause() {
        paused = true;
        AudioManager.getInstance(game).setMusicHalfVolume();
    }

    /**
     * unpauses the game when called, and resets music volume.
     */
    void gameUnpause() {
        paused = false;
        AudioManager.getInstance(game).setMusicFullVolume();
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