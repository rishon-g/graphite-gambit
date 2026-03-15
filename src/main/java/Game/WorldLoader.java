package Game;

import Components.Transform;
import Entities.Entity;
import Entities.Player;

import Game.Worlds.Asset.MapAsset;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Json;

import java.security.DrbgParameters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * The WorldLoader class is responsible for loading specific game worlds (or levels) based on an identifier.
 * 
 * @author Lane Jacobson
 * @version 1.0
 */
public class WorldLoader {

    /**
     * Loads a game world based on the given identifier.
     * parses level data from a json file, creates the tilemap and entities based on the data, and returns the initialized GameWorld object.
     * 
     * @param id identifier for the world to load (e.g. level number)
     * @return the loaded GameWorld object with tilemap and entities initialized according to the level data
     */
    public GameWorld loadWorld(GdxGame game, GameScreen screen, int id){
        GameWorld world = new GameWorld(id, screen);

        // get the map dynamically using the id
        MapAsset currentLevel = MapAsset.getLevelAsset(id);
        TiledMap map = game.getAssetService().get(currentLevel);

        // populate the 2D array
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("background");
        world.setDimensions(layer.getWidth() * GameWorld.getTileSize(), layer.getHeight() * GameWorld.getTileSize());
        world.tilemap = new int[layer.getWidth()][layer.getHeight()];

        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    world.tilemap[x][y] = cell.getTile().getId();
                } else {
                    world.tilemap[x][y] = 0;
                }
            }
        }

        // get collisions object layer
        MapLayer collisionLayer = map.getLayers().get("collisions");

        if (collisionLayer != null) {
            // loop through all hitboxes
            for (MapObject object : collisionLayer.getObjects()) {

                // we only use rectangles (for now)
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();

                    Transform wall = new Transform();
                    wall.setPosition(rect.x, rect.y);
                    wall.setScale(rect.width, rect.height);

                    // add to solid object array, so we know not to collide with these elements
                    world.solidObjects.add(wall);
                }
            }
        }

        // Load and spawn all entities
        Json json = new Json();
        FileHandle file = Gdx.files.internal("src/main/java/Game/Worlds/level" + id + ".json");
        LevelData data = json.fromJson(LevelData.class, file);

        for(EntityData entity : data.entities){

            // dynamically create entity of any type
            Entity newEntity;
            System.out.println("New entity: " + entity.type);
            switch(entity.type){
                case "Player":
                    newEntity = new Player(world);
                    world.player = (Player) newEntity;
                    break;
                default:
                    continue;
            }

            // set entity position and add to world
            newEntity.transform.setTilePosition(entity.x, entity.y, 128);
            world.entities.add(newEntity);
        }

        return world;
    }
}
