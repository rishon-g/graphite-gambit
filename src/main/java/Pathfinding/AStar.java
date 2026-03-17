package Pathfinding;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Utility class that implements A* pathfinding on a 2D tile grid.
 *
 * <p>The implementation supports movement in 8 directions:
 * up, down, left, right, and the 4 diagonals.</p>
 *
 * <p>The map uses the following convention:</p>
 * <ul>
 *     <li>0 = walkable tile</li>
 *     <li>1 = blocked tile</li>
 * </ul>
 *
 * <p>The returned path is represented as a list of grid cells.</p>
 */
public class AStar {

    /**
     * Node used in the A* search graph.
     *
     * <p>Each node represents one grid cell and stores:</p>
     * <ul>
     *     <li>x,y - its coordinates</li>
     *     <li>g - the path cost from the start</li>
     *     <li>f = g + h - the total estimated cost</li>
     *     <li>parent - a reference to its parent node for path reconstruction</li>
     * </ul>
     */
    private static class Node {
        /**
         * X-coordinate of this grid cell.
         */
        final int x;

        /**
         * Y-coordinate of this grid cell.
         */
        final int y;

        /**
         * g = how many steps from the start to this node.
         */
        final int g;

        /**
         * Total estimated cost for A* search.
         */
        final int f;

        /** parent is needed to restore the path back from the goal to the start.
         *
         */
        final Node parent;

        /**
         * Creates a new search node.
         *
         * @param x x-coordinate of the cell
         * @param y y-coordinate of the cell
         * @param g exact cost from the start node
         * @param f estimated total cost
         * @param parent previous node in the path
         */
        private Node(int x, int y, int g, int f, Node parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = f;
            this.parent = parent;
        }
    }

    /**
     * Finds a path from the start cell to the end cell using the A* algorithm.
     *
     * <p>If the start and end cells are the same, the returned path is empty.
     * If no valid path exists, this method also returns an empty list.</p>
     *
     * <p>Diagonal movement is allowed, but diagonal corner-cutting is prevented.
     * This means the algorithm will not move diagonally through the corner of
     * blocked tiles.</p>
     *
     * @param map the tile map
     * @param startX x-coordinate of the starting cell
     * @param startY y-coordinate of the starting cell
     * @param endX x-coordinate of the destination cell
     * @param endY y-coordinate of the destination cell
     * @return a list of cells representing the path, excluding the starting cell;
     */
    public static List<int[]> findPath(int[][] map, int startX, int startY, int endX, int endY) {
        // If we are already on the goal, the path is empty
        if (startX == endX && startY == endY) return Collections.emptyList();

        int width = map.length;
        int height = map[0].length;

        // closed[x][y] - checked cell
        boolean[][] closed = new boolean[width][height];

        // bestG[x][y] is the minimum g for the cell.
        int[][] bestG = new int[width][height];
        for (int x = 0; x < width; x++) Arrays.fill(bestG[x], Integer.MAX_VALUE);

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));

        // The first node
        int startH = chebyshevDistance(startX, startY, endX, endY);
        Node start = new Node(startX, startY, 0, startH, null);
        open.add(start);
        bestG[startX][startY] = 0;

        int[][] directions = { {1,0}, {-1,0}, {0,1}, {0,-1}, {1,1}, {1,-1}, {-1,1}, {-1,-1} };

        while (!open.isEmpty()) {
            Node current = open.poll();

            // If we have already checked the cell, we skip it.
            if (closed[current.x][current.y]) continue;
            closed[current.x][current.y] = true;

            // If we have reached the goal, we are restoring the path
            if (current.x == endX && current.y == endY) {
                return reconstruct(current, startX, startY);
            }

            // Checking the neighbors
            for (int[] d : directions) {
                int neighbourX = current.x + d[0];
                int neighbourY = current.y + d[1];

                // Limitations
                if (neighbourX < 0 || neighbourY < 0 || neighbourX >= width || neighbourY >= height) continue;
                if (isBlocked(map, neighbourX, neighbourY)) continue;
                if (closed[neighbourX][neighbourY]) continue;
                if (isDiagonalMove(d[0], d[1]) && cutsCorner(map, current.x, current.y, neighbourX, neighbourY)) continue;

                int newG = current.g + 1;

                if (newG >= bestG[neighbourX][neighbourY]) continue;
                bestG[neighbourX][neighbourY] = newG;

                // Chebyshev Distance Heuristic
                int newF = newG + chebyshevDistance(neighbourX, neighbourY, endX, endY);

                open.add(new Node(neighbourX, neighbourY, newG, newF, current));
            }
        }

        // The queue is empty
        return Collections.emptyList();
    }

    /**
     * Checks whether the specified cell is blocked.
     *
     * @param map the tile map
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     * @return True if the cell is blocked; False otherwise
     */
    private static boolean isBlocked(int[][] map, int x, int y) {
        return map[x][y] == 1;
    }

    /**
     * Computes the Chebyshev distance between two cells.
     *
     * @param x1 x-coordinate of the first cell
     * @param y1 y-coordinate of the first cell
     * @param x2 x-coordinate of the second cell
     * @param y2 y-coordinate of the second cell
     * @return the Chebyshev distance between the two cells
     */
    private static int chebyshevDistance(int x1, int y1, int x2, int y2) {
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    /**
     * Checks whether a move is diagonal.
     *
     * @param dx change in x
     * @param dy change in y
     * @return True if both dx and dy are nonzero; False otherwise
     */
    private static boolean isDiagonalMove(int dx, int dy) {
        return dx != 0 && dy != 0;
    }

    /**
     * Checks whether a diagonal move would cut through a blocked corner.
     *
     * <p>For a diagonal move from (currentX, currentY) to (neighbourX, neighbourY),
     * the move is disallowed if either horizontally or vertically adjacent
     * side cell is blocked.</p>
     *
     * @param map the tile map
     * @param currentX current x-coordinate
     * @param currentY current y-coordinate
     * @param neighbourX neighbor x-coordinate
     * @param neighbourY neighbor y-coordinate
     * @return True if the diagonal move cuts through a blocked corner; False otherwise
     */
    private static boolean cutsCorner(int[][] map, int currentX, int currentY, int neighbourX, int neighbourY) {
        int dx = neighbourX - currentX;
        int dy = neighbourY - currentY;

        if (dx == 0 || dy == 0) {
            return false;
        }

        return isBlocked(map, currentX + dx, currentY) || isBlocked(map, currentX, currentY + dy);
    }

    /**
     * Reconstructs the final path by walking backward from the goal node
     * through parent references.
     *
     * <p>The starting cell is not included in the returned path.</p>
     *
     * @param target the goal node reached by the search
     * @param startX x-coordinate of the start cell
     * @param startY y-coordinate of the start cell
     * @return the reconstructed path from start to target, excluding the start cell
     */
    private static List<int[]> reconstruct(Node target, int startX, int startY) {
        LinkedList<int[]> path = new LinkedList<>();
        Node current = target;
        while (current != null && !(current.x == startX && current.y == startY)) {
            path.addFirst(new int[]{current.x, current.y});
            current = current.parent;
        }
        return path;
    }
}