package Game;

import Entities.Entity;
import Entities.Player;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.Gdx;

/**
 * The WorldLoader class is responsible for loading specific game worlds (or levels) based on an identifier.
 * 
 * @author Lane Jacobson
 * @version 1.0
 * @since 2026-2-26
 */
public class WorldLoader {

    /**
     * Loads a game world based on the given identifier.
     * parses level data from a json file, creates the tilemap and entities based on the data, and returns the initialized GameWorld object.
     * 
     * @param id identifier for the world to load (e.g. level number)
     * @return the loaded GameWorld object with tilemap and entities initialized according to the level data
     */
    public GameWorld loadWorld(int id){
        GameWorld world = new GameWorld();

        /*
        // parse level data from json file
        Json json = new Json();
        String path = "./Worlds/level" + id + ".json";
        LevelData data = json.fromJson(LevelData.class, Gdx.files.internal(path));

        // create tilemap from level data
        world.initializeTilemap(data.xSize, data.ySize, data.tiles);

        for(EntityData entityData : data.entities) {
            
            // make proper entity based on name in entity data
            Entity entity;
            if (entityData.name.equals("Player")) {
                entity = new Player(world);
            }else{
                continue;
            }

            // set entity position based on entity data and add to world
            entity.transform.setPosition(entityData.x, entityData.y);

            //TODO: set entity sprite based on entity data




            world.entities.add(entity);
        } */

        return world;
    }
}
