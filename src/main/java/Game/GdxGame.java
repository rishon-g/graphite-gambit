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
    private ScreenManager screenManager;
    private OrthographicCamera camera;
    private Viewport viewport;
    private AssetService assetService;


    @Override
    public void create() {
        batch = new SpriteBatch();
        screenManager = ScreenManager.getInstance(this);
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        assetService = new AssetService(new InternalFileHandleResolver());

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
        generator.dispose();

        // set the initial screen
        screenManager.SetMenuScreen();
    }


    @Override
    public void render() {
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
        screenManager.dispose();
        batch.dispose();
        font.dispose();
        menuFont.dispose();
        headerFont.dispose();
        assetService.debugDiagnostics();
        assetService.dispose();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public BitmapFont getFont() {
        return font;
    }

    public BitmapFont getHeaderFont() {
        return headerFont;
    }

    public BitmapFont getMenuFont() {
        return menuFont;
    }

    public AssetService getAssetService() {
        return assetService;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}