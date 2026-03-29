package Game;

import utils.GameTest;

import Entities.*;
import Game.Worlds.Asset.AssetService;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.File;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class WorldLoaderTest extends GameTest {

    private WorldLoader worldLoader;
    private GdxGame mockGame;
    private GameScreen mockScreen;
    private TiledMap mockMap;

    @BeforeEach
    public void setUp() {
        mockGame = mock(GdxGame.class);
        mockScreen = mock(GameScreen.class);
        AssetService mockAssetService = mock(AssetService.class);
        mockMap = mock(TiledMap.class);

        when(mockGame.getAssetService()).thenReturn(mockAssetService);
        when(mockAssetService.get(any())).thenReturn(mockMap);

        MapLayers mapLayers = new MapLayers();
        when(mockMap.getLayers()).thenReturn(mapLayers);

        worldLoader = new WorldLoader();
    }

    @Test
    public void testLoadWorld_InitializesDimensionsAndTilemap() {
        // ------explaining the process--------
        // goal: isolation
        // we create a fake layer
        TiledMapTileLayer mockBgLayer = mock(TiledMapTileLayer.class);

        // when WorldLoader asks name of layer, we force it to answer "background"
        when(mockBgLayer.getName()).thenReturn("background");

        // similarly for width and height, we answer with fake numbers
        when(mockBgLayer.getWidth()).thenReturn(10);
        when(mockBgLayer.getHeight()).thenReturn(5);

        TiledMapTileLayer.Cell mockCell = mock(TiledMapTileLayer.Cell.class);
        TiledMapTile mockTile = mock(TiledMapTile.class);

        when(mockTile.getId()).thenReturn(1);
        when(mockCell.getTile()).thenReturn(mockTile);
        when(mockBgLayer.getCell(0, 0)).thenReturn(mockCell);

        mockMap.getLayers().add(mockBgLayer);


        FileHandle fakeFileHandle = new FileHandle() {
            @Override
            public Reader reader(String charset) {
                // we tell the system that whenever the game asks for a file, give it this fake one instead
                // turns off json spawning logic so we can isolate
                return new StringReader("{ \"entities\": [] }");
            }
            @Override
            public Reader reader() {
                return new StringReader("{ \"entities\": [] }");
            }
        };
        when(Gdx.files.internal(anyString())).thenReturn(fakeFileHandle);

        // finally, this is the real worldLoader running real loops
        GameWorld world = worldLoader.loadWorld(mockGame, mockScreen, 1);

        // does the world exist?
        assertNotNull(world, "GameWorld should be instantiated");

        // did the WorldLoader's math successfully put the ID 1 in the exact right spot?
        assertEquals(1, world.tilemap[0][0], "Tilemap at [0][0] should be populated with tile ID 1");

        // did the WorldLoader correctly leave empty spots as 0?
        assertEquals(0, world.tilemap[1][1], "Empty cells should default to 0");
    }

    @Test
    public void testLoadWorld_ParsesCollisionLayer() {
        TiledMapTileLayer mockBgLayer = mock(TiledMapTileLayer.class);

        when(mockBgLayer.getName()).thenReturn("background");
        when(mockBgLayer.getWidth()).thenReturn(20);
        when(mockBgLayer.getHeight()).thenReturn(20);
        mockMap.getLayers().add(mockBgLayer);

        com.badlogic.gdx.maps.MapLayer collisionLayer = new com.badlogic.gdx.maps.MapLayer();
        collisionLayer.setName("collisions");
        RectangleMapObject rectObj = new RectangleMapObject(0f, 0f, 128f, 128f);
        collisionLayer.getObjects().add(rectObj);
        mockMap.getLayers().add(collisionLayer);

        FileHandle fakeFileHandle = new FileHandle() {
            @Override
            public Reader reader(String charset) {
                return new StringReader("{ \"entities\": [] }");
            }

            @Override
            public Reader reader() {
                return new StringReader("{ \"entities\": [] }");
            }
        };
        when(Gdx.files.internal(anyString())).thenReturn(fakeFileHandle);

        GameWorld world = worldLoader.loadWorld(mockGame, mockScreen, 1);

        assertEquals(1, world.solidObjects.size, "should have loaded one solid object from the collision layer");
        assertEquals(1, world.tilemap[0][0], "the tile intersecting the collision object should be marked as blocked");
    }

    @Test
    public void testLoadWorld_SpawnsEntitiesFromMapAndJson() {
        TiledMapTileLayer mockBgLayer = mock(TiledMapTileLayer.class);
        when(mockBgLayer.getName()).thenReturn("background");
        when(mockBgLayer.getWidth()).thenReturn(10);
        when(mockBgLayer.getHeight()).thenReturn(10);
        mockMap.getLayers().add(mockBgLayer);

        com.badlogic.gdx.maps.MapLayer entitiesLayer = new com.badlogic.gdx.maps.MapLayer();
        entitiesLayer.setName("entities");

        // create a fake texture and wrap it in a real TextureRegion
        com.badlogic.gdx.graphics.Texture mockTexture = mock(com.badlogic.gdx.graphics.Texture.class);
        com.badlogic.gdx.graphics.g2d.TextureRegion fakeRegion = new com.badlogic.gdx.graphics.g2d.TextureRegion(mockTexture);

        // create the fake tile and tell it to return our fake graphic
        TiledMapTile fakeTile = mock(TiledMapTile.class);
        when(fakeTile.getTextureRegion()).thenReturn(fakeRegion);

        // pass the fully loaded fakeTile into the object
        com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject mockPickupObj =
                new com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject(fakeTile, false, false);
        mockPickupObj.getProperties().put("type", "Pickup");
        mockPickupObj.setX(100f);
        mockPickupObj.setY(100f);
        entitiesLayer.getObjects().add(mockPickupObj);

        mockMap.getLayers().add(entitiesLayer);

        // set up the fake JSON file to test JSON spawning
        String dummyJson = "{ \"entities\": [ { \"type\": \"Player\", \"x\": 5, \"y\": 5 } ] }";
        FileHandle fakeFileHandle = new FileHandle() {
            @Override
            public Reader reader(String charset) {
                return new StringReader(dummyJson);
            }
            @Override
            public Reader reader() {
                return new StringReader(dummyJson);
            }
        };

        when(Gdx.files.internal(anyString())).thenAnswer(invocation -> {
            String path = invocation.getArgument(0);
            if (path.endsWith(".json")) {
                return fakeFileHandle;
            }
            // use File(path) to prevent the getName() NullPointerException
            return new FileHandle(new File(path));
        });

        // run worldLoader
        GameWorld world = worldLoader.loadWorld(mockGame, mockScreen, 1);

        // check did the Tiled map spawner work
        assertEquals(1, world.spawnPoints.size, "should have saved 1 pickup spawn point from the Tiled map");

        // check did the JSON spawner work
        assertNotNull(world.getPlayer(), "WorldLoader should have parsed the JSON and created Player");
        assertEquals(1, world.entities.size(), "entities list should contain exactly 1 entity (the Player)");
    }

    @Test
    public void testLoadWorld_HandlesNullTilesSafely() {
        // create a layer with a cell, but the cell has NO tile
        TiledMapTileLayer mockBgLayer = mock(TiledMapTileLayer.class);
        when(mockBgLayer.getName()).thenReturn("background");

        when(mockBgLayer.getWidth()).thenReturn(2);
        when(mockBgLayer.getHeight()).thenReturn(1);

        // scenario 1: the cell itself is completely missing (tests cell != null -> false)
        when(mockBgLayer.getCell(0, 0)).thenReturn(null);


        // scenario 2: the cell exists, but its tile is missing (tests cell.getTile() != null -> false)
        TiledMapTileLayer.Cell mockCell = mock(TiledMapTileLayer.Cell.class);
        when(mockCell.getTile()).thenReturn(null);
        when(mockBgLayer.getCell(1, 0)).thenReturn(mockCell);

        mockMap.getLayers().add(mockBgLayer);
        mockJsonFile("{ \"entities\": [] }");

        GameWorld world = worldLoader.loadWorld(mockGame, mockScreen, 1);

        // both scenarios should safely default to 0
        assertEquals(0, world.tilemap[0][0]);
        assertEquals(0, world.tilemap[1][0]);
    }

    @Test
    public void testLoadWorld_IgnoresNonRectangleCollisions() {
        TiledMapTileLayer mockBgLayer = mock(TiledMapTileLayer.class);
        when(mockBgLayer.getName()).thenReturn("background");
        mockMap.getLayers().add(mockBgLayer);

        // create a collision layer with a generic (non-rectangle) object
        com.badlogic.gdx.maps.MapLayer collisionLayer = new com.badlogic.gdx.maps.MapLayer();
        collisionLayer.setName("collisions");
        com.badlogic.gdx.maps.MapObject badShape = mock(com.badlogic.gdx.maps.MapObject.class); // Not a Rectangle!
        collisionLayer.getObjects().add(badShape);
        mockMap.getLayers().add(collisionLayer);

        mockJsonFile("{ \"entities\": [] }");


        GameWorld world = worldLoader.loadWorld(mockGame, mockScreen, 1);
        // the bad shape was safely skipped
        assertEquals(0, world.solidObjects.size, "non-rectangle objects should be ignored");
    }

    @Test
    public void testLoadWorld_SafelyClampsOutOfBoundsCollisions() {
        TiledMapTileLayer mockBgLayer = mock(TiledMapTileLayer.class);
        when(mockBgLayer.getName()).thenReturn("background");
        when(mockBgLayer.getWidth()).thenReturn(5);
        when(mockBgLayer.getHeight()).thenReturn(5);
        mockMap.getLayers().add(mockBgLayer);

        com.badlogic.gdx.maps.MapLayer collisionLayer = new com.badlogic.gdx.maps.MapLayer();
        collisionLayer.setName("collisions");

        // fails "tx >= 0" (left of map)
        collisionLayer.getObjects().add(new RectangleMapObject(-200f, 100f, 10f, 10f));
        // fails "tx < length" (right of map. 5 tiles * 128px = 640px. put it at 800)
        collisionLayer.getObjects().add(new RectangleMapObject(800f, 100f, 10f, 10f));
        // fails "ty >= 0" (below map. tx is valid at 100, ty is negative)
        collisionLayer.getObjects().add(new RectangleMapObject(100f, -200f, 10f, 10f));
        // fails "ty < length" (above map. tx is valid at 100, ty is > 640)
        collisionLayer.getObjects().add(new RectangleMapObject(100f, 800f, 10f, 10f));
        mockMap.getLayers().add(collisionLayer);

        mockJsonFile("{ \"entities\": [] }");

        GameWorld world = worldLoader.loadWorld(mockGame, mockScreen, 1);

        // If it doesn't crash, we successfully short-circuited all 4 directions!
        assertNotNull(world, "world should successfully load without crashing on out-of-bounds math");
    }

    @Test
    public void testLoadWorld_IgnoresMalformedEntities() {
        TiledMapTileLayer mockBgLayer = mock(TiledMapTileLayer.class);
        when(mockBgLayer.getName()).thenReturn("background");
        mockMap.getLayers().add(mockBgLayer);

        com.badlogic.gdx.maps.MapLayer entitiesLayer = new com.badlogic.gdx.maps.MapLayer();
        entitiesLayer.setName("entities");

        // malformed 1: no "type" property at all
        com.badlogic.gdx.maps.MapObject noTypeObj = new com.badlogic.gdx.maps.MapObject();
        entitiesLayer.getObjects().add(noTypeObj);

        // malformed 2: as "type", but value is null
        com.badlogic.gdx.maps.MapObject nullTypeObj = new com.badlogic.gdx.maps.MapObject();
        nullTypeObj.getProperties().put("type", null);
        entitiesLayer.getObjects().add(nullTypeObj);

        // malformed 3: unknown entity type that isn't in your if/else block
        com.badlogic.gdx.maps.MapObject unknownTypeObj = new com.badlogic.gdx.maps.MapObject();
        unknownTypeObj.getProperties().put("type", "SomeRandomEntity");
        entitiesLayer.getObjects().add(unknownTypeObj);

        mockMap.getLayers().add(entitiesLayer);

        // dummy json
        mockJsonFile("{ \"entities\": [] }");


        GameWorld world = worldLoader.loadWorld(mockGame, mockScreen, 1);
        // the loader safely ignored all 3 bad objects without crashing
        assertEquals(0, world.entities.size(), "malformed entities should be ignored.");
        assertEquals(0, world.spawnPoints.size, "malformed pickups should be ignored.");
    }

    /**
     * Helper method to intercept libGDX JSON file requests and return a simulated JSON string.
     * @param jsonContent The raw JSON string to simulate.
     */
    private void mockJsonFile(String jsonContent) {
        FileHandle fakeJson = mock(FileHandle.class);
        when(fakeJson.reader(anyString())).thenAnswer(invocation -> new StringReader(jsonContent));
        when(Gdx.files.internal(anyString())).thenReturn(fakeJson);
    }

    @Test
    public void testLoadWorld_IgnoresNonTileEntityObjects() {
        //setup background
        TiledMapTileLayer mockBgLayer = mock(TiledMapTileLayer.class);
        when(mockBgLayer.getName()).thenReturn("background");
        when(mockBgLayer.getWidth()).thenReturn(10);
        when(mockBgLayer.getHeight()).thenReturn(10);
        mockMap.getLayers().add(mockBgLayer);

        //setup entities layer
        com.badlogic.gdx.maps.MapLayer entitiesLayer = new com.badlogic.gdx.maps.MapLayer();
        entitiesLayer.setName("entities");

        // create a rectangle but give it the pickup type
        // this fails the 'instanceof TiledMapTileMapObject' check (branch satisfied)
        RectangleMapObject plainRect = new RectangleMapObject(100, 100, 64, 64);
        plainRect.getProperties().put("type", "Pickup");
        entitiesLayer.getObjects().add(plainRect);

        // also add a WhiteOut typed Rectangle to clear that branch too
        RectangleMapObject plainWhiteOut = new RectangleMapObject(200, 200, 64, 64);
        plainWhiteOut.getProperties().put("type", "WhiteOutSmall");
        entitiesLayer.getObjects().add(plainWhiteOut);

        mockMap.getLayers().add(entitiesLayer);
        mockJsonFile("{ \"entities\": [] }");

        GameWorld world = worldLoader.loadWorld(mockGame, mockScreen, 1);

        // both should have been ignored because they aren't TileObjects
        assertEquals(0, world.spawnPoints.size, "plain rectangles should not be parsed as pickups.");
        assertEquals(0, world.entities.size(), "plain rectangles should not be parsed as whiteout entities.");
    }
}