package Asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Disposable;

/**
 * Service wrapper for managing and loading game assets using LibGDX's {@link AssetManager}.
 * Handles the loading, retrieval, and disposal of assets such as tile maps.
 */
public class AssetService implements Disposable {
    private final AssetManager assetManager;

    /**
     * Constructs an AssetService with the specified file handle resolver.
     * Initializes the internal AssetManager and sets the loader for TiledMaps.
     *
     * @param fileHandleResolver the resolver used to locate asset files
     */
    public AssetService(FileHandleResolver fileHandleResolver) {
        this.assetManager = new AssetManager(fileHandleResolver);
        this.assetManager.setLoader(TiledMap.class, new TmxMapLoader());
    }

    /**
     * Loads the specified asset.
     * Blocks the current thread until the asset is fully loaded.
     *
     * @param asset the {@link Asset} to load
     * @param <T> the type of the asset
     * @return the fully loaded asset instance
     */
    public <T> T load(Asset<T> asset) {
        this.assetManager.load(asset.getDescriptor());
        this.assetManager.finishLoading();
        return this.assetManager.get(asset.getDescriptor());
    }

    /**
     * Queues the specified asset for asynchronous loading.
     *
     * @param asset the {@link Asset} to queue
     * @param <T> the type of the asset
     */
    public <T> void queue(Asset<T> asset) {
        this.assetManager.load(asset.getDescriptor());
    }

    /**
     * Retrieves a previously loaded asset
     *
     * @param asset the {@link Asset} to retrieve
     * @param <T> the type of the asset
     * @return the loaded asset instance
     */
    public <T> T get(Asset<T> asset) {
        return this.assetManager.get(asset.getDescriptor());
    }

    /**
     * Updates the asset loading process.
     *
     * @return true if all queued assets have finished loading, false otherwise
     */
    public boolean update() {
        return this.assetManager.update();
    }

    /**
     * Disposes of the internal AssetManager and all associated loaded assets.
     * Should be called when the game or service is shutting down to prevent memory leaks.
     */
    @Override
    public void dispose() {
        this.assetManager.dispose();
    }

    /**
     * Logs diagnostic information about the current state of the AssetManager for debugging purposes.
     */
    public void debugDiagnostics() {
        Gdx.app.debug("AssetService", this.assetManager.getDiagnostics());
    }
}