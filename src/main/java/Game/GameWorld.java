package Game;
import Entities.Entity;
import Entities.Player;
import Entities.Nonplayer;

import Components.Transform;
import Components.Vec2;
import Components.Corner;

import java.util.Vector;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector2;


/**
 * The GameWorld class is responsible for managing the world, it's entities, and control over the game.
 * 
 * @author Lane Jacobson
 * @version 1.1
 */
public class GameWorld {
    // measurements for physics
    static final int TILE_SIZE = 128;
    static final int X_ORIGIN = 0;
    static final int Y_ORIGIN = 0;

    // spawner variables
    public Array<Vec2> spawnPoints = new Array<>();
    private float spawnTimer = 0f;
    private final float SPAWN_INTERVAL = 5.0f;
    private final int MAX_PICKUPS = 3;

    int points;
    float time;

    // list of all entities in the game world
    Vector<Entity> entities;

    // list of all solid objects
    public Array<Transform> solidObjects = new Array<>();

    // separate reference to the player entity
    Player player;

    // tilemap for the world (can be used for rendering and collisions)
    int[][] tilemap;

    // reference to the id of this world
    int worldId;

    // reference to the managing screen
    GameScreen screen;

    /**
     * Constructor for the GameWorld class, initializes the entities vector and creates the player entity.
     * @param id The id of this world
     * @param screen a reference to the creating screen
     */
    public GameWorld(int id, GameScreen screen) {
        this.entities = new Vector<Entity>();
        this.worldId = id;
        this.screen = screen;
        this.points = 0;
        this.time = 300f;
    }

    /**
     * Increments/Decrements the amount of points belonging to the user.
     * @param p increment to points
     */
    public void score(int p){
        this.points += p;
    }

    /**
     * Getter for points
     * @return points
     */
    public int getScore(){
        return points;
    }

    /**
     * getter for the worldId
     * @return worldId
     */
    public int getId(){
        return worldId;
    }

    /**
     * getter for the player
     * @return player
     */
    public Player getPlayer(){ return player; }

    /**
     * getter for the tilemap
     * @return tilemap
     */
    public int[][] getTilemap(){ return tilemap; }

    /**
     * getter for the TileSize
     * @return TILE_SIZE
     */
    public static int getTileSize(){ return TILE_SIZE; }

    /**
     * Updates all entities in the game world.
     * @param delta time since last update (used for movement and animations)
     */
    public void update(float delta) {
        // subtract delta to count down
        time -= delta;

        // check for time over
        if (time <= 0) {
            time = 0;
            screen.gameEnd(false); // Trigger game over if time hits zero
        }

        // random spawner logic (meant for graphite shards for now)
        if (spawnPoints.size > 0) {
            spawnTimer += delta;

            if (spawnTimer >= SPAWN_INTERVAL) {

                // count how many pickups concurrently are on the map
                int currentPickups = 0;
                for (Entity e : entities) {
                    if (e instanceof Entities.Pickup) {
                        currentPickups++;
                    }
                }

                // do we have room to spawn more?
                if (currentPickups < MAX_PICKUPS) {

                    int randomIndex = com.badlogic.gdx.math.MathUtils.random(0, spawnPoints.size - 1);
                    Vec2 chosenLoc = spawnPoints.get(randomIndex);

                    // occupancy check (making sure they don't spawn in the same location)
                    boolean spotTaken = false;
                    for (Entity e : entities) {
                        if (e instanceof Entities.Pickup) {
                            // if an existing pickup is extremely close to our chosen location
                            if (Math.abs(e.transform.position.x - chosenLoc.x) < 10f &&
                                    Math.abs(e.transform.position.y - chosenLoc.y) < 10f) {
                                spotTaken = true;
                                break;
                            }
                        }
                    }

                    // we only spawn if the spot is completely empty
                    if (!spotTaken) {
                        Entities.Pickup newPickup = new Entities.Pickup(this);
                        newPickup.transform.setPosition(chosenLoc.x, chosenLoc.y);
                        this.entities.add(newPickup);

                        // successfully spawned, so we reset the time
                        // if the spot was taken, this time is not reset because it will instantly try again
                        // until it finds an empty spot
                        spawnTimer = 0f;
                    }

                } else {
                    // we are at max limit but we try to spawn more
                    spawnTimer = 0f;
                }
            }
        }


        // Entity updates
        for (Entity entity : entities) {
            entity.update(delta);
        }

        // Resolve entity to player collisions
        for (Entity entity : entities) {
            if(entity instanceof Nonplayer){
                if(entity.transform.collides(player.transform)){
                    ((Nonplayer)entity).playerCollide(player);
                }
            }
        }

        // Resolve deaths
        for(int i = entities.size() - 1; i >= 0; i--) {
            if(entities.get(i).dead()){

                // Handle game over
                if(entities.get(i) instanceof Player){
                    screen.gameEnd(false);
                }

                // Remove entity from list
                else{
                    entities.remove(i);
                }
            }
        }
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
        camera.position.set(
                player.transform.position.x * GdxGame.UNIT_SCALE,
                player.transform.position.y * GdxGame.UNIT_SCALE,
                0
        );
    }

    /**
     * from a coordinate, calculates the indices of the tile that the coordinate resides within.
     * @param coordinate the coordinate to query
     * @return the tile that the coordinate is in
     */
    public Vec2 coordToIndex(Vec2 coordinate){
        Vec2 index = new Vec2();
        index.x = coordinate.x / TILE_SIZE - X_ORIGIN / TILE_SIZE;
        index.y = coordinate.y / TILE_SIZE - Y_ORIGIN / TILE_SIZE;
        return index;
    }

    /**
     * Called by and entity of the world in order to request to move.
     * Handles any collisions between the entity and the map, then moves it in an allowed way.
     * 
     * @param t the transform of the entity
     */
    public void requestMove(Transform t, float delta){
        float dx = t.velocity.x * delta;
        float dy = t.velocity.y * delta;

        // grid boundary check, so we cant traverse outside of the map
        if (t.position.x + dx < 0) dx = -t.position.x;
        if (t.position.x + t.size.x + dx > tilemap.length * TILE_SIZE) dx = (tilemap.length * TILE_SIZE) - (t.position.x + t.size.x);
        if (t.position.y + dy < 0) dy = -t.position.y;
        if (t.position.y + t.size.y + dy > tilemap[0].length * TILE_SIZE) dy = (tilemap[0].length * TILE_SIZE) - (t.position.y + t.size.y);

        // phantom box to test movements before actually moving the player
        Transform testBox = new Transform();
        testBox.setScale(t.size.x, t.size.y);

        // horizontal movement
        if (dx != 0) {
            testBox.setPosition(t.position.x + dx, t.position.y); // lets see if this collides

            for (Transform wall : solidObjects) {
                if (testBox.collides(wall)) {
                    // snap exactly to the edge of the custom rectangle
                    if (dx > 0) { // move right
                        dx = wall.position.x - (t.position.x + t.size.x) - 0.01f;
                    } else { // move left
                        dx = (wall.position.x + wall.size.x) - t.position.x + 0.01f;
                    }
                    testBox.setPosition(t.position.x + dx, t.position.y); // update phantom box
                }
            }
            t.move(new Vec2(dx, 0)); // if there is a collision, the testBox provides
            // us with a dx value that is flush with the collision rectangle, otherwise we move as normal
        }

        // vertical movement
        if (dy != 0) {
            testBox.setPosition(t.position.x, t.position.y + dy); // test y move

            for (Transform wall : solidObjects) {
                if (testBox.collides(wall)) {
                    // snap exactly to the edge of the custom rectangle
                    if (dy > 0) { // move up
                        dy = wall.position.y - (t.position.y + t.size.y) - 0.01f;
                    } else { // move down
                        dy = (wall.position.y + wall.size.y) - t.position.y + 0.01f;
                    }
                    testBox.setPosition(t.position.x, t.position.y + dy); // update phantom box
                }
            }
            t.move(new Vec2(0, dy)); // same thing as before but now for y
        }
    }
}


