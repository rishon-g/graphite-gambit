package Game;
import Entities.Entity;
import Entities.Player;
import Entities.Nonplayer;

import Components.Transform;
import Components.Vec2;
import Components.Corner;

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

    int points;
    float time;

    // list of all entities in the game world
    Vector<Entity> entities;

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
        this.time = 0;
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
        // World updates
        time += delta;

        // Entity updates
        for (Entity entity : entities) {
            entity.update(delta);
        }

        // Resolve entity to player collisions
        for (Entity entity : entities) {
            if(entity instanceof Nonplayer){
                ((Nonplayer)entity).playerCollide(player);
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
        camera.position.set(player.transform.position.x, player.transform.position.y, 0);
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
        // calculate dx and dy from velocities
        Vec2 translate = new Vec2(t.velocity.x * delta, t.velocity.y * delta);

        // horizontal movement
        if(translate.x != 0){
            // rightward movement
            if(translate.x > 0){
                // get positions of edges after movement 
                Vec2 top_coord = t.getCorner(Corner.TR);
                top_coord.x += translate.x;
                Vec2 bottom_coord = t.getCorner(Corner.BR);
                bottom_coord.x += translate.x;
                
                // adjust to not clip tile underneath
                bottom_coord.y -= 1;

                // get indices for new coordinates
                Vec2 top_tile = coordToIndex(top_coord);
                Vec2 bottom_tile = coordToIndex(bottom_coord);

                // cast indices to integers for use
                int tilex = (int)top_tile.x;
                int topy = (int)top_tile.y;
                int bottomy = (int)bottom_tile.y;

                // check all coordinates between corners for collisions
                for(int y = topy; y <= bottomy; y++){
                    // check if collides with a wall
                    if(tilemap[y][tilex] != 0){
                        // set movement to be flush with the tile
                        float tileLeft = tilex * TILE_SIZE;
                        translate.x = tileLeft - t.getCorner(Corner.TR).x;
                        break;
                    }
                }
            }
            
            // leftward movement
            else{
                Vec2 top_coord = t.getCorner(Corner.TL);
                top_coord.x += translate.x;

                Vec2 bottom_coord = t.getCorner(Corner.BL);
                bottom_coord.x += translate.x;

                bottom_coord.y -= 1;

                Vec2 top_tile = coordToIndex(top_coord);
                Vec2 bottom_tile = coordToIndex(bottom_coord);

                int tilex = (int)top_tile.x;
                int topy = (int)top_tile.y;
                int bottomy = (int)bottom_tile.y;

                for(int y = topy; y <= bottomy; y++){
                    if(tilemap[y][tilex] != 0){
                        float tileRight = (tilex + 1) * TILE_SIZE;
                        translate.x = tileRight - t.getCorner(Corner.TL).x;
                        break;
                    }
                }
            }
        }

        // vertical movement
        if(translate.y != 0){

            // downward movement
            if (translate.y > 0) {
                Vec2 left_coord = t.getCorner(Corner.BL);
                left_coord.y += translate.y;

                Vec2 right_coord = t.getCorner(Corner.BR);
                right_coord.y += translate.y;

                right_coord.x -= 1;

                Vec2 left_tile = coordToIndex(left_coord);
                Vec2 right_tile = coordToIndex(right_coord);

                int tiley = (int)left_tile.y;
                int leftx = (int)left_tile.x;
                int rightx = (int)right_tile.x;

                for(int x = leftx; x <= rightx; x++){
                    if(tilemap[tiley][x] != 0){
                        float tileTop = tiley * TILE_SIZE;
                        translate.y = tileTop - t.getCorner(Corner.BL).y;
                        break;
                    }
                }                
            }

            // upward movement
            else{
                Vec2 left_coord = t.getCorner(Corner.TL);
                left_coord.y += translate.y;

                Vec2 right_coord = t.getCorner(Corner.TR);
                right_coord.y += translate.y;

                right_coord.x -= 1;

                Vec2 left_tile = coordToIndex(left_coord);
                Vec2 right_tile = coordToIndex(right_coord);

                int tiley = (int)left_tile.y;
                int leftx = (int)left_tile.x;
                int rightx = (int)right_tile.x;

                for(int x = leftx; x <= rightx; x++){
                    if(tilemap[tiley][x] != 0){
                        float tileBottom = (tiley + 1) * TILE_SIZE;
                        translate.y = tileBottom - t.getCorner(Corner.TL).y;
                        break;
                    }
                }
            }
        }

        // move the entity
        t.move(translate);
    }
}
