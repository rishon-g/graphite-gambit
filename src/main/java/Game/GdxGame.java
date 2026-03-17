package Game;

import Game.Worlds.Asset.AssetService;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * GdxGame is executed when the game is started, initializes global resources, and sets the initial screen.
 * @author Luke McRae, Rishon
 * @version 1.0
 */
public class GdxGame extends Game {
    public static final float WORLD_WIDTH = 20f;
    public static final float WORLD_HEIGHT = 11.25f;
    public static final float UNIT_SCALE = 1f / 128f;


    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont menuFont;
    private BitmapFont headerFont;
    private BitmapFont smallHeaderFont;
    private ScreenManager screenManager;
    private OrthographicCamera camera;
    private Viewport viewport;
    private AssetService assetService;
    private AudioManager audioManager;

    private boolean musicPlaying = true;
    private boolean sfxPlaying = true;

    /**
     * Initializes the game by setting up the SpriteBatch, ScreenManager, camera, viewport, AssetService, AudioManager, and fonts.
     * Sets the initial screen to the menu screen and starts the background music.
     */
    @Override
    public void create() {
        batch = new SpriteBatch();
        screenManager = ScreenManager.getInstance(this);
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        assetService = new AssetService(new InternalFileHandleResolver());
        audioManager = AudioManager.getInstance(this);

        // load default font

        var generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/PressStart.ttf"));
        var fontParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParams.size = 32;
        fontParams.color = Color.WHITE;

        fontParams.borderWidth = 4f; // the thickness of the outline in pixels
        fontParams.borderColor = Color.BLACK;

        // turning off smoothing so pixels are crisp
        fontParams.minFilter = Texture.TextureFilter.Nearest;
        fontParams.magFilter = Texture.TextureFilter.Nearest;


        font = generator.generateFont(fontParams);
        fontParams.borderColor = Color.CLEAR;
        fontParams.color = Color.BLACK;
        fontParams.size = 32;
        fontParams.spaceX = -5;
        menuFont = generator.generateFont(fontParams);
        fontParams.size = 76;
        fontParams.spaceX = -8;
        headerFont = generator.generateFont(fontParams);
        fontParams.size = 55;
        fontParams.spaceX = -7;
        smallHeaderFont = generator.generateFont(fontParams);
        generator.dispose();

        // set the initial screen
        screenManager.SetMenuScreen();
        AudioManager.getInstance(this).startMusic();
    }

    /**
     * Renders the current screen.
     */
    @Override
    public void render() {
        super.render();
    }

    /**
     * Updates the viewport when the window is resized.
     * @param width the new width of the window
     * @param height the new height of the window
     */
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    /**
     * Disposes of all resources including screens, batch, fonts, asset service, and audio manager.
     */
    @Override
    public void dispose() {
        super.dispose();
        screenManager.dispose();
        batch.dispose();
        font.dispose();
        menuFont.dispose();
        headerFont.dispose();
        smallHeaderFont.dispose();
        assetService.debugDiagnostics();
        assetService.dispose();
        audioManager.dispose();
    }

    /**
     * Returns the SpriteBatch used for rendering.
     * @return the SpriteBatch instance
     */
    public SpriteBatch getBatch() {
        return batch;
    }

    /**
     * Returns the default BitmapFont.
     * @return the BitmapFont instance
     */
    public BitmapFont getFont() {
        return font;
    }

    /**
     * Returns the header BitmapFont.
     * @return the header BitmapFont instance
     */
    public BitmapFont getHeaderFont() {
        return headerFont;
    }

    /**
     * Returns the small header BitmapFont.
     * @return the small header BitmapFont instance
     */
    public BitmapFont getSmallHeaderFont() {
        return smallHeaderFont;
    }

    /**
     * Returns the menu BitmapFont.
     * @return the menu BitmapFont instance
     */
    public BitmapFont getMenuFont() {
        return menuFont;
    }

    /**
     * Returns the AssetService for managing game assets.
     * @return the AssetService instance
     */
    public AssetService getAssetService() {
        return assetService;
    }

    /**
     * Returns the Viewport used for rendering.
     * @return the Viewport instance
     */
    public Viewport getViewport() {
        return viewport;
    }

    /**
     * Returns the OrthographicCamera used for rendering.
     * @return the OrthographicCamera instance
     */
    public OrthographicCamera getCamera() {
        return camera;
    }

    /**
     * Checks if music is currently playing.
     * @return true if music is playing, false otherwise
     */
    public boolean isMusicPlaying() {
        return musicPlaying;
    }

    /**
     * Checks if sound effects are currently playing.
     * @return true if sound effects are playing, false otherwise
     */
    public boolean isSfxPlaying() {
        return sfxPlaying;
    }

    /**
     * Sets whether music should be playing.
     * @param musicPlaying true to enable music, false to disable
     */
    public void setMusicPlaying(boolean musicPlaying) {
        this.musicPlaying = musicPlaying;
    }

    /**
     * Sets whether sound effects should be playing.
     * @param sfxPlaying true to enable sound effects, false to disable
     */
    public void setSfxPlaying(boolean sfxPlaying) {
        this.sfxPlaying = sfxPlaying;
    }
}