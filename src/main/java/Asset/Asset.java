package Asset;

import com.badlogic.gdx.assets.AssetDescriptor;

/**
 * An interface representing an asset that can be loaded by the game's asset system.
 *
 * @param <T> the type of the asset
 */
public interface Asset<T> {

    /**
     * Retrieves the underlying LibGDX AssetDescriptor for this asset
     *
     * @return the {@link AssetDescriptor} used to load and manage this asset
     */
    AssetDescriptor<T> getDescriptor();
}