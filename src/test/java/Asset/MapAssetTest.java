package Asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.maps.tiled.TiledMap;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MapAssetTest {

    @Test
    public void testGetLevelAsset_ValidIds_ReturnsCorrectLevel() {
        // assert that the exact integer maps to the exact enum
        assertEquals(MapAsset.LEVEL1, MapAsset.getLevelAsset(1));
        assertEquals(MapAsset.LEVEL2, MapAsset.getLevelAsset(2));
    }

    @Test
    public void testGetLevelAsset_InvalidIds_FallsBackToLevel1() {
        // assert the default fallback works for out-of-bounds IDs
        assertEquals(MapAsset.LEVEL1, MapAsset.getLevelAsset(99));
        assertEquals(MapAsset.LEVEL1, MapAsset.getLevelAsset(-5));
        assertEquals(MapAsset.LEVEL1, MapAsset.getLevelAsset(0));
    }

    @Test
    public void testConstructor_FormatsFilePathCorrectly() {
        // extract the descriptor from the Enum
        AssetDescriptor<TiledMap> descriptor1 = MapAsset.LEVEL1.getDescriptor();
        AssetDescriptor<TiledMap> descriptor2 = MapAsset.LEVEL2.getDescriptor();

        // assert that the constructor successfully prepended "maps/" to the string
        assertEquals("maps/level1.tmx", descriptor1.fileName, "LEVEL1 path should be formatted correctly");
        assertEquals("maps/level2.tmx", descriptor2.fileName, "LEVEL2 path should be formatted correctly");

        // assert it targets the correct libGDX class
        assertEquals(TiledMap.class, descriptor1.type, "descriptor should target TiledMap class.");
    }
}