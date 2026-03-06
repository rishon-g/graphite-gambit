package Game;

import Game.Worlds.Asset.AssetService;
import Game.Worlds.Asset.MapAsset;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen implements Screen {

    // reference to the main game class, used for switching screens.
    GdxGame game;

    Viewport viewport;
    AssetService assetService;

    OrthogonalTiledMapRenderer mapRenderer;

    // the game world that contains all entities and tilemap data for this screen
    GameWorld world;

    // camera used for rendering the game world
    OrthographicCamera camera;

    // sprite batch used for rendering entities
    SpriteBatch batch;

    /**
     * Constructor for the GameScreen class. Initializes all resources and the GameWorld.
     * @param game reference to the main game class.
     */
    public GameScreen(GdxGame game, int id) {
        this.game = game;
        camera = new OrthographicCamera();
        batch = game.getBatch();
        this.world = new WorldLoader().loadWorld(id);
        viewport = game.getViewport();
        assetService = game.getAssetService();
        mapRenderer = new OrthogonalTiledMapRenderer(null,
                GdxGame.UNIT_SCALE, this.batch);
    }

    @Override
    public void show() {
        this.assetService.load(MapAsset.LEVEL1);
        this.mapRenderer.setMap(this.assetService.get(MapAsset.LEVEL1));
    }

    /**
     * The render method is called every frame, and is responsible for updating the game world and rendering all entities.
     * 
     * @param delta time since last frame (used for uniform movement and animations)
     */
    @Override
    public void render(float delta) {
        viewport.apply();
        camera.update();

        this.batch.setColor(Color.WHITE);
        this.mapRenderer.setView(this.camera);
        this.mapRenderer.render();


        world.update(delta);


        world.updateCamera(camera);

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        world.render(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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
        world.dispose();
        mapRenderer.dispose();
    }
    
}
