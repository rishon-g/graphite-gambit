package Game;

/**
 * This interface defines how an entity can define their own brush for use in floor drawing.
 */
public interface DrawWeight {
    /**
     * Calculates the weight from the offset, then returns it as an int.
     * @param x x offset from center
     * @param y y offset from center
     * @param brushsize the size of the brush
     * @return the weight of the given pixel in the brush, from 1-10
     */
    int getWeight(int x, int y, int brushsize);
}