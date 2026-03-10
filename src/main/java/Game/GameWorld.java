package Game;
import Entities.Entity;
import Entities.Player;

import Components.Transform;
import Components.Vec2;

import java.util.Vector;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * The GameWorld class is responsible for managing the world, it's entities, and control over the game.
 * 
 * @author Lane Jacobson
 * @version 1.1
 */
public class GameWorld {
    // measurements for physics
    static final int TILE_SIZE = 50;
    static final int X_ORIGIN = 0;
    static final int Y_ORIGIN = 0;

    // list of all entities in the game world
    Vector<Entity> entities;

    // separate reference to the player entity
    Player player;

    // tilemap for the world (can be used for rendering and collisions)
    int[][] tilemap;

    /**
     * Constructor for the GameWorld class, initializes the entities vector and creates the player entity.
     */
    public GameWorld() {
        this.entities = new Vector<Entity>();
    }

    public void initializeTilemap(int width, int height, Array<String> tileData) {
        // initialize tilemap
        this.tilemap = new int[width][height];

        // parse and place tile data 
        for (int y = 0; y < height; y++) {
            String row = tileData.get(y);
            for (int x = 0; x < width; x++) {
                this.tilemap[x][y] = Character.getNumericValue(row.charAt(x));
            }
        }
    }

    /**
     * Updates all entities in the game world.
     * @param delta time since last update (used for movement and animations)
     */
    public void update(float delta) {
        // World Updates
        // TODO

        // Entity Updates
        for (Entity entity : entities) {
            entity.update(delta);
        }

        // Resolve Deaths
        // TODO
    }

    /**
     * Renders all entities in the game world. This method is called every frame after update.
     */
    public void render(SpriteBatch batch, float delta) {
        for (Entity entity : entities) {
            entity.render(batch, delta);
        }
    }

    /**
     * Disposes of all resources used by the game world.
     * Called when the game is closed to free up memory and resources.
     */
    public void dispose() {
        // EMPTY
    }

    /**
     * updates the position of the camera to follow the player.
     * called by the screen before rendering the spritebatch.
     * @param camera the camera following the player.
     */
    public void updateCamera(OrthographicCamera camera){
        camera.position.set(player.transform.x, player.transform.y, 0);
    }

    /**
     * Called by and entity of the world in order to request to move.
     * Handles any collisions between the entity and the map, then moves it in an allowed way.
     * 
     * @param t the transform of the entity
     */
    public void requestMove(Transform t, float delta){
        // calculate dx and dy from velocities
        Vec2 movement = new Vec2(t.velocity.x * delta, t.velocity.y * delta);

        // actual values to move the entity by
        Vec2 translate = new Vec2();

        // horizontal movement
        if(movement.x != 0){

            // rightward movement
        }

        // vertical movement
        if(movement.y != 0){

        }

        // move the entity
        t.move(translate);
    }
}
