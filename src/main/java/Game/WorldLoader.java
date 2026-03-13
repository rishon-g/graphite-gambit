package Game;

import Entities.Entity;
import Entities.Player;

import Game.Worlds.Asset.MapAsset;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
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
    public GameWorld loadWorld(GdxGame game, GameScreen screen, int id){
        GameWorld world = new GameWorld(id, screen);

        // get the map dynamically using the id
        MapAsset currentLevel = MapAsset.getLevelAsset(id);
        TiledMap map = game.getAssetService().get(currentLevel);

        // populate the 2D array
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("background");
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

        // spawn the player
        Entity dummyPlayer = new Player(world);
        // coordinates: (15,10). we multiply by 128 to get pixels for transform
        dummyPlayer.transform.setPosition(15 * 128, 10 * 128);
        dummyPlayer.transform.setScale(100, 100);
        world.entities.add(dummyPlayer);
        world.player = (Player) dummyPlayer;

        return world;
    }
}
