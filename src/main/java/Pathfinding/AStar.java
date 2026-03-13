package Pathfinding;

import java.util.*;

/**
 * A* pathfinding of movement in 4 directions: (up/down/left/right).
 *
 * Returns the path as a list of int[]{x,y} cells.
 */
public class AStar {

    /** Node in the search graph (cell on the grid). */
    public static class Node {
        public int x, y;

        /** g = how many steps from the start to this node */
        public int g;

        /** f = g + h (h = The Manhattan Heuristic) */
        public int f;

        /** parent is needed to restore the path back from the goal to the start. */
        public Node parent;

        public Node(int x, int y, int g, int f, Node parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = f;
            this.parent = parent;
        }
    }

    /**
     * Searches for the path from (startX,startY) to (endX,endY).
     * Returns an empty list if the path is not found.
     */
    public static List<int[]> findPath(int[][] map, int startX, int startY, int endX, int endY) {
        // If we are already on the goal, the path is empty
        if (startX == endX && startY == endY) return Collections.emptyList();

        int w = map.length;
        int h = map[0].length;

        // closed[x][y] - checked cell
        boolean[][] closed = new boolean[w][h];

        // bestG[x][y] is the minimum g for the cell.
        int[][] bestG = new int[w][h];
        for (int x = 0; x < w; x++) Arrays.fill(bestG[x], Integer.MAX_VALUE);

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));

        // The first node
        int startH = chebyshev(startX, startY, endX, endY);
        Node start = new Node(startX, startY, 0, startH, null);
        open.add(start);
        bestG[startX][startY] = 0;

        int[][] directions = { {1,0}, {-1,0}, {0,1}, {0,-1}, {1,1}, {1,-1}, {-1,1}, {-1,-1} };

        while (!open.isEmpty()) {
            Node cur = open.poll();

            // If we have already checked the cell, we skip it.
            if (closed[cur.x][cur.y]) continue;
            closed[cur.x][cur.y] = true;

            // If we have reached the goal, we are restoring the path
            if (cur.x == endX && cur.y == endY) {
                return reconstruct(cur, startX, startY);
            }

            // Checking the neighbors (prefix n)
            for (int[] d : directions) {
                int nx = cur.x + d[0];
                int ny = cur.y + d[1];

                // Limitations
                if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                if (isBlocked(map, nx, ny)) continue;
                if (closed[nx][ny]) continue;
                if (isDiagonalMove(d[0], d[1]) && cutsCorner(map, cur.x, cur.y, nx, ny)) continue;

                int ng = cur.g + 1;

                if (ng >= bestG[nx][ny]) continue;
                bestG[nx][ny] = ng;

                // Manhattan Distance Heuristic
                int nf = ng + chebyshev(nx, ny, endX, endY);

                open.add(new Node(nx, ny, ng, nf, cur));
            }
        }

        // The queue is empty
        return Collections.emptyList();
    }

    /** Check whether the cage is a wall. */
    private static boolean isBlocked(int[][] map, int x, int y) {
        return map[x][y] == 1;
    }

    /** Manhattan Distance Heuristic */
    private static int chebyshev(int x1, int y1, int x2, int y2) {
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    /** True if move is diagonal. */
    private static boolean isDiagonalMove(int dx, int dy) {
        return dx != 0 && dy != 0;
    }

    /** Prevent diagonal movement through a blocked corner. */
    private static boolean cutsCorner(int[][] map, int x, int y, int nx, int ny) {
        int dx = nx - x;
        int dy = ny - y;

        if (dx == 0 || dy == 0) {
            return false;
        }

        return isBlocked(map, x + dx, y) || isBlocked(map, x, y + dy);
    }

    /** Restoring the path from the goal to the start via parent. */
    private static List<int[]> reconstruct(Node goal, int startX, int startY) {
        LinkedList<int[]> path = new LinkedList<>();
        Node cur = goal;
        while (cur != null && !(cur.x == startX && cur.y == startY)) {
            path.addFirst(new int[]{cur.x, cur.y});
            cur = cur.parent;
        }
        return path;
    }
}
