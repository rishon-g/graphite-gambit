package Game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameScreen implements Screen {

    // reference to the main game class, used for switching screens.
    GdxGame game;

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
    public GameScreen(GdxGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        batch = game.getBatch();
        this.world = new WorldLoader().loadWorld(1);
    }

    @Override
    public void show() {
        camera.setToOrtho(false, 1960, 1080);
    }

    /**
     * The render method is called every frame, and is responsible for updating the game world and rendering all entities.
     * 
     * @param delta time since last frame (used for uniform movement and animations)
     */
    @Override
    public void render(float delta) {
        world.update(delta);

        camera.update();
        world.updateCamera(camera);

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        world.render(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {

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
    }
    
}
