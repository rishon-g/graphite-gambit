package Game;

import Game.Worlds.Asset.AssetService;
import Game.Worlds.Asset.MapAsset;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
                this.batch
        );

    }

    @Override
    public void show() {
    }

    /**
     * The render method is called every frame, and is responsible for updating the game world and rendering all entities.
     * 
     * @param delta time since last frame (used for uniform movement and animations)
     */
    @Override
    public void render(float delta) {

        // run update loops
        world.update(delta);

        // follow with camera
        world.updateCamera(camera);
        camera.update();
        viewport.apply();

        // wipe the screen
        ScreenUtils.clear(Color.BLACK);

        // draw map
        this.batch.setColor(Color.WHITE);



        // expand the camera's "culling box" by 5 world units such that big objects do not despawn
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
