package Asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public enum MapAsset implements Asset<TiledMap> {
    LEVEL1("level1.tmx"),
    LEVEL2("level2.tmx");

    private final AssetDescriptor<TiledMap> descriptor;

    MapAsset(String mapName) {
        TmxMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
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
            // Fallback to level 1 if an invalid ID is passed
            default:
                return LEVEL1;
        }
    }
}
