package Pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class for choosing a random reachable patrol target on a tile map.
 *
 * <p>This class is used by mobile enemies while they are not actively chasing
 * the player. It searches tiles around the enemy within a limited radius and
 * returns one random reachable tile.</p>
 *
 * <p>Map convention:</p>
 * <ul>
 *     <li>0 = walkable tile</li>
 *     <li>1 = blocked tile</li>
 * </ul>
 */
public final class RandomPatrol {

    /**
     * Shared random number generator for patrol target selection.
     */
    private static final Random RANDOM = new Random();

    /**
     * Private constructor to prevent instantiation.
     */
    private RandomPatrol() {
    }

    /**
     * Chooses a random reachable patrol target near the given start tile.
     *
     * <p>The method searches all tiles in a square region centered at the
     * start tile with radius {@code searchRadius}. A tile is considered a valid
     * patrol target if it:</p>
     * <ul>
     *     <li>lies inside the map bounds,</li>
     *     <li>is not blocked,</li>
     *     <li>is not the start tile itself,</li>
     *     <li>and is reachable from the start tile using A*.</li>
     * </ul>
     *
     * <p>If no valid target exists, {@code null} is returned.</p>
     *
     * @param map the tile map
     * @param startX the starting tile x-coordinate
     * @param startY the starting tile y-coordinate
     * @param searchRadius how far from the start tile to search
     * @return a random reachable tile as {@code {x, y}}, or {@code null} if none exist
     */
    public static int[] choosePatrolTarget(int[][] map, int startX, int startY, int searchRadius) {
        if (map == null || map.length == 0 || map[0].length == 0) {
            return null;
        }

        if (!isInsideMap(map, startX, startY)) {
            return null;
        }

        if (isBlocked(map, startX, startY)) {
            return null;
        }

        List<int[]> candidates = new ArrayList<int[]>();

        int minX = startX - searchRadius;
        int maxX = startX + searchRadius;
        int minY = startY - searchRadius;
        int maxY = startY + searchRadius;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (!isInsideMap(map, x, y)) {
                    continue;
                }

                if (isBlocked(map, x, y)) {
                    continue;
                }

                if (x == startX && y == startY) {
                    continue;
                }

                if (AStar.findPath(map, startX, startY, x, y).isEmpty()) {
                    continue;
                }

                candidates.add(new int[]{x, y});
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(RANDOM.nextInt(candidates.size()));
    }

    /**
     * Checks whether a tile coordinate lies inside the map bounds.
     *
     * @param map the tile map
     * @param x tile x-coordinate
     * @param y tile y-coordinate
     * @return true if the tile lies inside the map; false otherwise
     */
    private static boolean isInsideMap(int[][] map, int x, int y) {
        return x >= 0 && y >= 0 && x < map.length && y < map[0].length;
    }

    /**
     * Checks whether a tile is blocked.
     *
     * @param map the tile map
     * @param x tile x-coordinate
     * @param y tile y-coordinate
     * @return true if the tile is blocked; false otherwise
     */
    private static boolean isBlocked(int[][] map, int x, int y) {
        return map[x][y] == 1;
    }
}
