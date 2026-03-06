package Game.Worlds.Asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.tiled.TiledMap;

public interface Asset<T extends Map> {
    AssetDescriptor<TiledMap> getDescriptor();
}
