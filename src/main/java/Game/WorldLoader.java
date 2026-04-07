package Game;

import Components.Transform;
import Components.Vec2;
import Entities.*;

import Asset.MapAsset;
import Objects.*;
import Screens.GameScreen;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Json;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * The WorldLoader class is responsible for loading specific game worlds (or
 * levels) based on an identifier.
 *
 * @author Lane Jacobson
 * @version 1.0
 */
public class WorldLoader {

    /**
     * Loads a game world based on the given identifier.
     * parses level data from a json file, creates the tilemap and entities based on
     * the data, and returns the initialized GameWorld object.
     *
     * @param id identifier for the world to load (e.g. level number)
     * @return the loaded GameWorld object with tilemap and entities initialized
     *         according to the level data
     */
    public GameWorld loadWorld(GdxGame game, GameScreen screen, int id) {
        GameWorld world = new GameWorld(id, screen);

        // we add variability to each world
        switch (id) {
            case 1:
                setWorldConditions(world, 5, 5.0f, -2);
                break;
            case 2:
                setWorldConditions(world, 7, 3.5f, -3);
                break;
            case 3:
                setWorldConditions(world, 14, 1.0f, -4);
                break;
            default:
                setWorldConditions(world, 5 + id, Math.max(1.0f, 6.0f - id), -id - 1);
                break;
        }

        // get the map dynamically using the id
        MapAsset currentLevel = MapAsset.getLevelAsset(id);
        TiledMap map = game.getAssetService().get(currentLevel);
        
        // populate the 2D array
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("background");
        world.setDimensions(layer.getWidth() * GameWorld.getTileSize(), layer.getHeight() * GameWorld.getTileSize());
        world.tilemap = new int[layer.getWidth()][layer.getHeight()];
        PhysicsHandler.CreateHandler(world.entities, world.width, world.height); // temporary dimensions, will be set properly after loading the map
        
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
        PhysicsHandler physics = PhysicsHandler.getInstance();

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
                    physics.solidObjects.add(wall);

                    // also mark blocked tiles for pathfinding
                    int tileSize = GameWorld.getTileSize();

                    int startX = (int) (wall.position.x / tileSize);
                    int endX = (int) ((wall.position.x + wall.size.x - 0.01f) / tileSize);
                    int startY = (int) (wall.position.y / tileSize);
                    int endY = (int) ((wall.position.y + wall.size.y - 0.01f) / tileSize);

                    for (int tx = startX; tx <= endX; tx++) {
                        for (int ty = startY; ty <= endY; ty++) {
                            if (tx >= 0 && tx < world.tilemap.length &&
                                    ty >= 0 && ty < world.tilemap[0].length) {
                                world.tilemap[tx][ty] = 1;
                            }
                        }
                    }
                }
            }
        }

        // extract pickups from tiled
        MapLayer entitiesLayer = map.getLayers().get("entities");

        if (entitiesLayer != null) {
            for (MapObject object : entitiesLayer.getObjects()) {

                // look for the custom property we added within tiled
                if (object.getProperties().containsKey("type")) {
                    String type = object.getProperties().get("type", String.class);

                    // if the property equals "Pickup", save its location
                    if (type != null && type.equals("Pickup")) {
                        if (object instanceof TiledMapTileMapObject) {
                            TiledMapTileMapObject tileObject = (TiledMapTileMapObject) object;

                            float spawnX = tileObject.getX() + 32f;
                            float spawnY = tileObject.getY() + 32f;

                            // add this coordinate to our GameWorld's list
                            world.spawnPoints.add(new Vec2(spawnX, spawnY));
                        }
                    }

                    // white out puddles
                    else if (type != null && (type.equals("WhiteOutSmall") || type.equals("WhiteOutLarge"))) {
                        if (object instanceof TiledMapTileMapObject) {
                            TiledMapTileMapObject tileObject = (TiledMapTileMapObject) object;

                            // determine the size word based on what Tiled says
                            String sizeWord = type.equals("WhiteOutLarge") ? "Large" : "Small";

                            WhiteOut puddle = new WhiteOut(world, sizeWord);

                            float spawnX = tileObject.getX();
                            float spawnY = tileObject.getY();

                            puddle.transform.setPosition(spawnX, spawnY);
                            world.addEntity(puddle);
                        }
                    }
                    // door
                    else if (type != null && (type.equals("DoorLeft") || type.equals("DoorRight"))) {
                        if (object instanceof TiledMapTileMapObject) {
                            TiledMapTileMapObject tileObject = (TiledMapTileMapObject) object;
                            String partWord = type.equals("DoorLeft") ? "Left" : "Right";

                            Door doorPart = new Door(world, partWord);
                            doorPart.transform.setPosition(tileObject.getX(), tileObject.getY());
                            world.addEntity(doorPart);
                        }
                    }

                    // exit point
                    else if (type != null && type.equals("ExitPoint")) {
                        if (object instanceof TiledMapTileMapObject) {
                            TiledMapTileMapObject tileObject = (TiledMapTileMapObject) object;

                            ExitPoint exit = new ExitPoint(world);
                            exit.transform.setPosition(tileObject.getX(), tileObject.getY());
                            world.addEntity(exit);
                        }
                    }

                    // ink
                    else if (type != null && type.equals("Ink")) {
                        if (object instanceof TiledMapTileMapObject) {
                            TiledMapTileMapObject tileObject = (TiledMapTileMapObject) object;

                            Ink inkArea = new Ink(world);
                            inkArea.transform.setPosition(tileObject.getX(), tileObject.getY());
                            world.addEntity(inkArea);
                        }
                    }
                }
            }
        }

        // Load and spawn all entities
        Json json = new Json();
        FileHandle file = Gdx.files.internal("src/main/java/Worlds/level" + id + ".json");
        LevelData data = json.fromJson(LevelData.class, file);

        for (EntityData entity : data.entities) {

            // dynamically create entity of any type
            Entity newEntity;
            System.out.println("New entity: " + entity.type);
            switch (entity.type) {
                case "Player":
                    newEntity = new Player(world);
                    break;
                case "Eraser":
                    newEntity = new Eraser(world);
                    break;
                case "Node":
                    newEntity = new Node(world);
                    newEntity.transform.setTilePosition(entity.x, entity.y, 128);
                    newEntity.transform.position.x += 48f;
                    newEntity.transform.position.y += 48f;

                    // save the precise visual center (node is 32x32, so we add 16)
                    world.nodePositions.add(new Vec2(
                            newEntity.transform.position.x + 16f,
                            newEntity.transform.position.y + 16f
                    ));
                    break;
                case "PencilSharpener":
                    newEntity = new PencilSharpener(world);
                    break;
                default:
                    continue;
            }

            newEntity.transform.setTilePosition(entity.x, entity.y, 128);

            // centers node, (AKA plot point)
            if (newEntity instanceof Node) {
                newEntity.transform.position.x += 48f;
                newEntity.transform.position.y += 48f;
            }

            world.addEntity(newEntity);

        }

        return world;
    }

    public void setWorldConditions(GameWorld world, int pickupCount, float spawnInterval, int movementHealthLoss) {
        world.maxPickups = pickupCount;
        world.spawnInterval = spawnInterval;
        Player.MOVEMENT_HEALTH_LOSS = movementHealthLoss;
    }
}

