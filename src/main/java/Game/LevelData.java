package Game;

import com.badlogic.gdx.utils.Array;

/**
 * Helper class for loading level data from external files.
 * contains tilemap and list of entities.
 * 
 * @author Lane Jacobson
 * @version 1.0
 * @since 2026-2-26
 */
public class LevelData {
    public int xSize;
    public int ySize;
    public Array<String> tiles;
    public Array<EntityData> entities;
}
