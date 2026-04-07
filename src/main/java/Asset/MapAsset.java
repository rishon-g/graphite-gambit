package Asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.maps.tiled.TiledMap;

public enum MapAsset implements Asset<TiledMap> {
    LEVEL1("level1.tmx"),
    LEVEL2("level2.tmx"),
    LEVEL3("level3.tmx"),
    LEVEL4("level4.tmx");

    private final AssetDescriptor<TiledMap> descriptor;

    MapAsset(String mapName) {
        this.descriptor = new  AssetDescriptor<>("maps/" + mapName, TiledMap.class);
    }

    @Override
    public AssetDescriptor<TiledMap> getDescriptor() {
        return this.descriptor;
    }

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
