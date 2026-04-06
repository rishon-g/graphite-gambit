package Pathfinding;

import java.util.ArrayDeque;
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
     * Directions used for BFS.
     */
    private static final int[][] DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    /**
     * Private constructor to prevent instantiation.
     */
    private RandomPatrol() {
    }

    /**
     * Chooses a random reachable patrol target near the given start tile.
     *
     * <p>This version avoids repeated A* calls. It first computes which tiles
     * are reachable from the start using one BFS, then picks a random reachable
     * tile within the search radius.</p>
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

        boolean[][] reachable = buildReachableMap(map, startX, startY);
        List<int[]> candidates = new ArrayList<>();

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

                if (!reachable[x][y]) {
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
     * Builds a map of tiles reachable from the start using BFS algorithm.
     *
     * @param map the tile map
     * @param startX start tile x-coordinate
     * @param startY start tile y-coordinate
     * @return reachable[x][y] is true if that tile can be reached
     */
    private static boolean[][] buildReachableMap(int[][] map, int startX, int startY) {
        boolean[][] reachable = new boolean[map.length][map[0].length];
        ArrayDeque<int[]> queue = new ArrayDeque<>();

        reachable[startX][startY] = true;
        queue.add(new int[]{startX, startY});

        while (!queue.isEmpty()) {
            int[] current = queue.removeFirst();
            int currentX = current[0];
            int currentY = current[1];

            for (int[] dir : DIRECTIONS) {
                int nextX = currentX + dir[0];
                int nextY = currentY + dir[1];

                if (!isInsideMap(map, nextX, nextY)) {
                    continue;
                }

                if (isBlocked(map, nextX, nextY)) {
                    continue;
                }

                if (reachable[nextX][nextY]) {
                    continue;
                }

                if (isDiagonalMove(dir[0], dir[1])
                        && cutsCorner(map, currentX, currentY, nextX, nextY)) {
                    continue;
                }

                reachable[nextX][nextY] = true;
                queue.addLast(new int[]{nextX, nextY});
            }
        }

        return reachable;
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

    /**
     * Returns true if the move is diagonal.
     *
     * @param dx x step
     * @param dy y step
     * @return true if diagonal
     */
    private static boolean isDiagonalMove(int dx, int dy) {
        return dx != 0 && dy != 0;
    }

    /**
     * Prevents diagonal movement through blocked corners.
     *
     * @param map the tile map
     * @param currentX current x
     * @param currentY current y
     * @param nextX next x
     * @param nextY next y
     * @return true if the diagonal move cuts a blocked corner
     */
    private static boolean cutsCorner(int[][] map, int currentX, int currentY, int nextX, int nextY) {
        int dx = nextX - currentX;
        int dy = nextY - currentY;

        if (dx == 0 || dy == 0) {
            return false;
        }

        return isBlocked(map, currentX + dx, currentY)
                || isBlocked(map, currentX, currentY + dy);
    }
}