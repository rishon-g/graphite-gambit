package Game;
import Entities.Entity;
import Entities.Player;

import java.util.Vector;
import com.badlogic.gdx.utils.Array;

public class GameWorld {
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
        for (Entity entity : entities) {
            entity.update(delta);
        }
    }

    /**
     * Renders all entities in the game world. This method is called every frame after update.
     */
    public void render() {
        for (Entity entity : entities) {
            entity.render();
        }
    }
}
