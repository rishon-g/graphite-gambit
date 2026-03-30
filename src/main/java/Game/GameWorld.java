package Game;

import Entities.Entity;
import Entities.Player;
import Objects.Nonplayer;

import Components.Transform;
import Components.Vec2;

import java.util.Vector;

import Objects.Door;
import Objects.Ink;
import Objects.Pickup;
import Screens.GameScreen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * The GameWorld class is responsible for managing the world, it's entities, and
 * control over the game.
 * 
 * @author Lane Jacobson
 * @version 1.1
 */
public class GameWorld {
    // measurements for physics
    static final int TILE_SIZE = 128;
    static final int X_ORIGIN = 0;
    static final int Y_ORIGIN = 0;
    static final int DRAW_SIZE = 3;

    // spawner variables
    public Array<Vec2> spawnPoints = new Array<>();
    private float spawnTimer = 0f;
    private final float SPAWN_INTERVAL = 5.0f;
    private final int MAX_PICKUPS = 5;

    int points;
    public float time;
    int plotpoints;

    // list of all entities in the game world
    Vector<Entity> entities;

    // list of all solid objects
    public Array<Transform> solidObjects = new Array<>();

    // separate reference to the player entity
    Player player;

    // the size of the map
    int width, height;

    // tilemap for the world (can be used for rendering and collisions)
    int[][] tilemap;

    // map for the floor drawings
    short[][] drawmap;

    TextureRegion pixel;

    // reference to the id of this world
    int worldId;

    // reference to the managing screen
    GameScreen screen;

    /**
     * Constructor for the GameWorld class, initializes the entities vector and
     * creates the player entity.
     * 
     * @param id     The id of this world
     * @param screen a reference to the creating screen
     */
    public GameWorld(int id, GameScreen screen) {
        this.entities = new Vector<Entity>();
        this.worldId = id;
        this.screen = screen;
        this.points = 0;
        this.plotpoints = 0;
        this.time = 300f;
        createPixel();
    }

    /**
     * sets the dimensions of the world in tiles
     * 
     * @param width  the width in tiles of the world
     * @param height the height in tiles of the world
     */
    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        drawmap = new short[height / DRAW_SIZE][width / DRAW_SIZE];
    }

    /**
     * Increments/Decrements the amount of points belonging to the user.
     * 
     * @param p increment to points
     */
    public void score(int p) {
        this.points += p;
        AudioManager.getInstance().playScore();
    }

    /**
     * Called when the player scores a plot point. grants points, and reduces plot
     * point counter.
     */
    public void plotPointCollected() {
        screen.collectPlotPoint();
        score(100);
        this.plotpoints--;
        if (plotpoints <= 0) {
            for (Entity e : entities) {
                if (e instanceof Door) {
                    e.dead = true; // This removes it from the world instantly
                }
            }
        }
    }

    /**
     * Getter for points
     * 
     * @return points
     */
    public int getScore() {
        return points;
    }

    /**
     * getter for the worldId
     * 
     * @return worldId
     */
    public int getId() {
        return worldId;
    }

    /**
     * getter for the player
     * 
     * @return player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * getter for the tilemap
     * 
     * @return tilemap
     */
    public int[][] getTilemap() {
        return tilemap;
    }

    /**
     * getter for the TileSize
     * 
     * @return TILE_SIZE
     */
    public static int getTileSize() {
        return TILE_SIZE;
    }

    /**
     * getter for the drawmap
     * 
     * @return drawmap
     */
    public int getDrawSize() {
        return DRAW_SIZE;
    }

    /**
     * getter for the drawmap
     * 
     * @return drawmap
     */
    public short[][] getDrawmap() {
        return drawmap;
    }

    /**
     * Initializes the pixel needed for floor drawing
     */
    private void createPixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixel = new TextureRegion(tex);
        pixmap.dispose();
    }

    /**
     * Draws in graphite around the location requested by the entity.
     * 
     * @param position  Position to draw at
     * @param erase     true if it should instead erase
     * @param brushsize width of drawing
     */
    public void floorDraw(Vec2 position, boolean erase, int brushsize, DrawWeight w) {
        floorDraw(position.x, position.y, erase, brushsize, w);
    }

    /**
     * Draws in graphite around the location requested by the entity.
     * 
     * @param posx      x position to draw at
     * @param posy      y position to draw at
     * @param erase     true if it should instead erase
     * @param brushsize width of the brush to be used
     */
    public void floorDraw(float posx, float posy, boolean erase, int brushsize, DrawWeight w) {
        int tilex = (int) posx / DRAW_SIZE, tiley = (int) posy / DRAW_SIZE;
        for (int y = -brushsize; y <= brushsize; y++) {
            int offset = brushsize - Math.abs(y);
            for (int x = -offset; x <= offset; x++) {

                // pixel draw position
                int xindex = tilex + x, yindex = tiley + y;

                // System.out.println("at: (" + xindex + ", " + yindex + ")");

                // out of bounds checking
                if (xindex < 0 || xindex >= width / DRAW_SIZE || yindex < 0 || yindex >= height / DRAW_SIZE) {
                    continue;
                }

                // get and clamp weight from function
                short weight = (short) w.getWeight(x, y, brushsize);
                if(weight < 0){
                    weight = 0;
                }else if (weight > 10){
                    weight = 10;
                }

                // draw on position
                if (erase)
                    drawmap[yindex][xindex] = (short) Math.min(drawmap[yindex][xindex], 10 - weight);
                else
                    drawmap[yindex][xindex] = (short) Math.max(drawmap[yindex][xindex], weight);
            }
        }
    }

    /**
     * Renders the drawable floor map within the world.
     * 
     * @param batch the spritebatch to render from
     */
    private void renderFloor(SpriteBatch batch) {
        int last = 0;
        for (int y = 0; y < height / DRAW_SIZE; y++) {
            for (int x = 0; x < width / DRAW_SIZE; x++) {
                int value = drawmap[y][x];

                if (value <= 0)
                    continue;

                if (value != last) {
                    batch.setColor(0.2f, 0.2f, 0.2f, (float) value / 10);
                    last = value;
                }

                batch.draw(pixel, x * DRAW_SIZE * GdxGame.UNIT_SCALE, y * DRAW_SIZE * GdxGame.UNIT_SCALE,
                        DRAW_SIZE * GdxGame.UNIT_SCALE, DRAW_SIZE * GdxGame.UNIT_SCALE);
            }
        }

        batch.setColor(Color.WHITE);
    }

    /**
     * Adds an entity to the world. If the entity is a player, it sets the player
     * reference to it.
     * 
     * @param e the entity to be added
     */
    public void addEntity(Entity e) {
        // Set global player reference if this is a player entity
        if (e instanceof Player) {
            // If we already have a player, remove it before adding the new one
            if(this.player != null){
                entities.remove(this.player);
            }
            this.player = (Player) e;
        }
        entities.add(e);
    }

    /**
     * Getter for the entities vector, used for testing purposes.
     * @return the vector of entities in the world
     */
    public Vector<Entity> getEntities() {
        return entities;
    }

    /**
     * Updates all entities in the game world.
     * 
     * @param delta time since last update (used for movement and animations)
     */
    public void update(float delta) {
        // subtract delta to count down
        time -= delta;

        // check for font.getData().setScale(1.0f);time over
        if (time <= 0) {
            time = 0;
            loseGame();
        }

        // random spawner logic (meant for graphite shards for now)
        if (spawnPoints.size > 0) {
            spawnTimer += delta;

            if (spawnTimer >= SPAWN_INTERVAL) {

                // count how many pickups concurrently are on the map
                int currentPickups = 0;
                for (Entity e : entities) {
                    if (e instanceof Pickup) {
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
                        if (e instanceof Pickup) {
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
                        Pickup newPickup = new Pickup(this);
                        newPickup.transform.setPosition(chosenLoc.x, chosenLoc.y);
                        this.entities.add(newPickup);

                        // successfully spawned, so we reset the time
                        // if the spot was taken, this time is not reset because it will instantly try
                        // again until it finds an empty spot
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
            if (entity instanceof Nonplayer) {
                if (isTouchingPlayer(entity.transform)) {
                    ((Nonplayer) entity).playerCollide(player);
                }
            }
        }

        // Resolve deaths
        for (int i = entities.size() - 1; i >= 0; i--) {
            if (entities.get(i).dead()) {

                // Handle game over
                if (entities.get(i) instanceof Player) {
                    loseGame();
                }

                // Remove entity from list
                else {
                    entities.remove(i);
                }
            }
        }
    }

    /**
     * Renders all entities in the game world. This method is called every frame
     * after update.
     * 
     * @param batch the spritebatch to render sprites within
     * @param delta the time since the last update
     */
    public void render(SpriteBatch batch, float delta) {
        renderFloor(batch);
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
     * 
     * @param camera the camera following the player.
     */
    public void updateCamera(OrthographicCamera camera) {
        camera.position.set(
                player.transform.position.x * GdxGame.UNIT_SCALE,
                player.transform.position.y * GdxGame.UNIT_SCALE,
                0);
    }

    /**
     * from a coordinate, calculates the indices of the tile that the coordinate
     * resides within.
     * 
     * @param coordinate the coordinate to query
     * @return the tile that the coordinate is in
     */
    public Vec2 coordToIndex(Vec2 coordinate) {
        Vec2 index = new Vec2();
        index.x = coordinate.x / TILE_SIZE - X_ORIGIN / TILE_SIZE;
        index.y = coordinate.y / TILE_SIZE - Y_ORIGIN / TILE_SIZE;
        return index;
    }

    /**
     * gets the entity that holds ownership over a transform, given it belongs to
     * one.
     * 
     * @param t the transform to search for
     * @return the entity that owns the transform, Null if not owned
     */
    private Entity getEntityByTransform(Transform t) {
        if (t == null) {
            return null;
        }
        if (player != null && player.transform == t) {
            return player;
        }
        for (Entity entity : entities) {
            if (entity.transform == t) {
                return entity;
            }
        }
        return null;
    }

    /**
     * returns whether or not a given entity should block the movement of a moving
     * entity.
     * 
     * @param mover the transform of the moving entity
     * @param other the entity that is coliding
     * @return true if the movement should be blocked, otherwise false.
     */
    private boolean shouldBlockEntityMovement(Transform mover, Entity other) {
        Entity moverEntity = getEntityByTransform(mover);

        if (moverEntity == null || other == null) {
            return false;
        }

        if (other instanceof Door) {
            return true;
        }

        if (other instanceof Ink) {
            return false;
        }

        if (other.transform == mover) {
            return false;
        }

        // Erasers block each other
        if (moverEntity instanceof Nonplayer && other instanceof Nonplayer) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if a non-player entity is touching or very close to the player.
     *
     * @param other transform of the other entity
     * @return true if the entity is close enough to count as contact
     */
    private boolean isTouchingPlayer(Transform other) {
        float epsilon = 0.5f;

        float playerLeft = player.transform.position.x;
        float playerRight = player.transform.position.x + player.transform.size.x;
        float playerBottom = player.transform.position.y;
        float playerTop = player.transform.position.y + player.transform.size.y;

        float otherLeft = other.position.x;
        float otherRight = other.position.x + other.size.x;
        float otherBottom = other.position.y;
        float otherTop = other.position.y + other.size.y;

        return !(playerLeft > otherRight + epsilon ||
                playerRight < otherLeft - epsilon ||
                playerBottom > otherTop + epsilon ||
                playerTop < otherBottom - epsilon);
    }

    /**
     * Triggers the win condition for the game. Called when the player reaches the
     * exit point.
     */
    public void winGame(){
        endGame(true);
    }

    /**
     * Triggers the lose condition for the game. Called when the player dies or time
     */
    public void loseGame(){
        endGame(false);
    }

    /**
     * Ends the game and triggers the end screen. Called when the player wins or loses.
     */
    public void endGame(boolean won) {
        screen.gameEnd(won);
    }

    /**
     * Called by and entity of the world in order to request to move.
     * Handles any collisions between the entity and the map, then moves it in an
     * allowed way.
     *
     * @param t the transform of the entity
     */
    public void requestMove(Transform t, float delta) {
        float dx = t.velocity.x * delta;
        float dy = t.velocity.y * delta;

        // grid boundary check, so we cant traverse outside of the map
        if (t.position.x + dx < 0)
            dx = -t.position.x;
        if (t.position.x + t.size.x + dx > height)
            dx = (height) - (t.position.x + t.size.x);
        if (t.position.y + dy < 0)
            dy = -t.position.y;
        if (t.position.y + t.size.y + dy > width)
            dy = (width) - (t.position.y + t.size.y);

        // phantom box to test movements before actually moving the player
        Transform testBox = new Transform();
        testBox.setScale(t.size.x, t.size.y);

        // horizontal movement
        if (dx != 0) {
            testBox.setPosition(t.position.x + dx, t.position.y); // lets see if this collides

            // collide with walls
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

            // collide with blocking entities
            for (Entity entity : entities) {
                if (!shouldBlockEntityMovement(t, entity)) {
                    continue;
                }

                if (testBox.collides(entity.transform)) {
                    if (dx > 0) {
                        dx = entity.transform.position.x - (t.position.x + t.size.x) - 0.01f;
                    } else {
                        dx = (entity.transform.position.x + entity.transform.size.x) - t.position.x + 0.01f;
                    }
                    testBox.setPosition(t.position.x + dx, t.position.y);
                }
            }

            t.move(new Vec2(dx, 0)); // if there is a collision, the testBox provides
            // us with a dx value that is flush with the collision rectangle, otherwise we
            // move as normal
        }

        // vertical movement
        if (dy != 0) {
            testBox.setPosition(t.position.x, t.position.y + dy); // test y move

            // collide with walls
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

            // collide with blocking entities
            for (Entity entity : entities) {
                if (!shouldBlockEntityMovement(t, entity)) {
                    continue;
                }

                if (testBox.collides(entity.transform)) {
                    if (dy > 0) {
                        dy = entity.transform.position.y - (t.position.y + t.size.y) - 0.01f;
                    } else {
                        dy = (entity.transform.position.y + entity.transform.size.y) - t.position.y + 0.01f;
                    }
                    testBox.setPosition(t.position.x, t.position.y + dy);
                }
            }

            t.move(new Vec2(0, dy)); // same thing as before but now for y
        }
    }
}
