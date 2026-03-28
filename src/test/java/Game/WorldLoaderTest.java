package Game;

import Entities.*;
import Game.Worlds.Asset.AssetService;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.GdxNativesLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

// we use mockito because JUnit only runs in a standard java environment with no OpenGL graphics
// so, we mock the gdx static environment
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class WorldLoaderTest {

    private WorldLoader worldLoader;
    private GdxGame mockGame;
    private GameScreen mockScreen;
    private AssetService mockAssetService;
    private TiledMap mockMap;

    @BeforeAll
    public static void initLibgdxNatives() {
        // load native C++ libraries so Pixmap won't crash
        GdxNativesLoader.load();
    }

    @BeforeEach
    public void setUp() {
        // mock the libGDX environment
        Gdx.gl = mock(GL20.class);
        Gdx.gl20 = mock(GL20.class);
        Gdx.graphics = mock(Graphics.class);
        Gdx.app = mock(Application.class);
        Gdx.files = mock(com.badlogic.gdx.Files.class);

        // this tells the mocked file system to return a real FileHandle for any path requested,
        // as a result Texture loading won't crash.
        when(Gdx.files.internal(anyString())).thenAnswer(invocation -> {
            String path = invocation.getArgument(0);
            return new FileHandle(new File(path));
        });


        worldLoader = new WorldLoader();
        mockGame = mock(GdxGame.class);
        mockScreen = mock(GameScreen.class);
        mockAssetService = mock(AssetService.class);
        mockMap = mock(TiledMap.class);

        when(mockGame.getAssetService()).thenReturn(mockAssetService);
        when(mockAssetService.get(any())).thenReturn(mockMap);

        MapLayers mapLayers = new MapLayers();
        when(mockMap.getLayers()).thenReturn(mapLayers);
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
}