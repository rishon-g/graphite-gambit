package Asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.maps.tiled.TiledMap;

/**
 * Enum representing the tile map assets available in the game.
 * Implements the {@link Asset} interface for {@link TiledMap} objects.
 */
public enum MapAsset implements Asset<TiledMap> {
    /** Level 1 map asset. */
    LEVEL1("level1.tmx"),

    /** Level 2 map asset. */
    LEVEL2("level2.tmx"),

    /** Level 3 map asset. */
    LEVEL3("level3.tmx"),

    /** Level 4 map asset. */
    LEVEL4("level4.tmx");

    private final AssetDescriptor<TiledMap> descriptor;

    /**
     * Constructs a MapAsset with the specified map file name.
     *
     * @param mapName the filename of the map (e.g., "level1.tmx")
     */
    MapAsset(String mapName) {
        this.descriptor = new AssetDescriptor<>("maps/" + mapName, TiledMap.class);
    }

    /**
     * Retrieves the AssetDescriptor for this map asset.
     *
     * @return the {@link AssetDescriptor} associated with this map
     */
    @Override
    public AssetDescriptor<TiledMap> getDescriptor() {
        return this.descriptor;
    }

    /**
     * Retrieves the corresponding MapAsset based on the provided level ID.
     *
     * @param id the level ID (1-4)
     * @return the corresponding {@link MapAsset}, (defaulting to LEVEL1 if the ID is invalid)
     */
    public static MapAsset getLevelAsset(int id) {
        switch (id) {
            case 1:
                return LEVEL1;
            case 2:
                return LEVEL2;
            case 3:
                return LEVEL3;
            case 4:
                return LEVEL4;
            // Fallback to level 1 if an invalid ID is passed
            default:
                return LEVEL1;
        }
    }
}