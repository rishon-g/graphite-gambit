package Game;

import Game.Worlds.Asset.AssetService;
import Game.Worlds.Asset.MapAsset;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
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
        camera = game.getCamera();
        batch = game.getBatch();
        this.world = new WorldLoader().loadWorld(id, this);
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
        // 1. FREEZE TIME AND PHYSICS
        // Comment out the world update so gravity stops pulling your player down!
        // world.update(delta);

        // 2. DETACH THE CAMERA
        // Comment out the camera tracker
        // world.updateCamera(camera);

        // 3. HARDCODE CAMERA TO THE CENTER OF THE MAP
        camera.position.set(15f, 10f, 0f);
        camera.update();
        viewport.apply();

        // 4. WIPE THE SCREEN
        ScreenUtils.clear(Color.NAVY);

        // 5. DRAW THE MAP
        this.batch.setColor(Color.WHITE);
        this.mapRenderer.setView(this.camera);
        this.mapRenderer.render();

        // 6. DRAW ENTITIES
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        world.render(batch, delta);
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
    
    /**
     * Handles the event that the GameWorld is finished with it's operation.
     * 
     * @param won true if the player won, false if not
     */
    public void gameEnd(boolean won){

        // Updates the player save if won level
        if(won){
            PlayerData d = PlayerData.obtainPlayerData();
            d.completeLevel(world.getId(), world.getScore());
        }
        // Load Main Menu
        ScreenManager m = ScreenManager.getInstance(game);
        m.SetMenuScreen();
    }
}
