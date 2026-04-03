package Pathfinding;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AStarTest {

    @Test
    void testFindPathOnEmptyGrid() {
        int[][] map = new int[3][3];

        List<int[]> path = AStar.findPath(map, 0, 0, 2, 2);

        assertFalse(path.isEmpty(), "Path should exist on an empty grid");
        assertArrayEquals(new int[]{1, 1}, path.get(0), "First step should move diagonally toward the goal");
        assertArrayEquals(new int[]{2, 2}, path.get(path.size() - 1), "Path should end on the goal tile");
    }

    @Test
    void testUnreachableDestination() {
        int[][] map = {
                {0, 1, 0},
                {1, 1, 1},
                {0, 1, 0}
        };

        List<int[]> path = AStar.findPath(map, 0, 0, 2, 2);

        assertTrue(path.isEmpty(), "Unreachable destination should return an empty path");
    }

    @Test
    void testStartAndEndSameTile() {
        int[][] map = new int[2][2];

        List<int[]> path = AStar.findPath(map, 1, 1, 1, 1);

        assertTrue(path.isEmpty(), "Path should be empty when start and end are the same tile");
    }

    @Test
    void testEightDirectionMovementAllowsDiagonalShortcut() {
        int[][] map = new int[2][2];

        List<int[]> path = AStar.findPath(map, 0, 0, 1, 1);

        assertEquals(1, path.size(), "Diagonal movement should allow reaching the goal in one step");
        assertArrayEquals(new int[]{1, 1}, path.get(0));
    }

    @Test
    void testDiagonalMovementDoesNotPassThroughBlockedCorners() {
        int[][] map = {
                {0, 1},
                {1, 0}
        };

        List<int[]> path = AStar.findPath(map, 0, 0, 1, 1);

        assertTrue(path.isEmpty(), "Diagonal move should be blocked when it would cut a blocked corner");
    }

    @Test
    void testBoundaryAndEdgeOfMapCase() {
        int[][] map = {
                {0, 0, 0},
                {1, 1, 0},
                {0, 0, 0}
        };

        List<int[]> path = AStar.findPath(map, 0, 2, 2, 0);

        assertFalse(path.isEmpty(), "A path along the map edge should still be found");
        assertArrayEquals(new int[]{2, 0}, path.get(path.size() - 1), "Path should end on the target tile");
        for (int[] step : path) {
            assertTrue(step[0] >= 0 && step[0] < 3 && step[1] >= 0 && step[1] < 3,
                    "Every step should stay inside map bounds");
        }
    }

    @Test
    void testReconstructedPathIsInCorrectOrder() {
        int[][] map = new int[3][1];

        List<int[]> path = AStar.findPath(map, 0, 0, 2, 0);

        assertEquals(2, path.size());
        assertArrayEquals(new int[]{1, 0}, path.get(0), "Path should begin with the first move after the start tile");
        assertArrayEquals(new int[]{2, 0}, path.get(1), "Path should preserve forward order toward the destination");
        assertFalse(path.stream().anyMatch(step -> step[0] == 0 && step[1] == 0), "Returned path should exclude the starting tile");
    }

    @Test
    void testFindPath_NullMap_ReturnsEmptyList() {
        List<int[]> path = AStar.findPath(null, 0, 0, 1, 1);

        assertNotNull(path);
        assertTrue(path.isEmpty(), "Null map should return an empty path");
    }

    @Test
    void testFindPath_EmptyMap_ReturnsEmptyList() {
        int[][] map = new int[0][0];

        List<int[]> path = AStar.findPath(map, 0, 0, 1, 1);

        assertNotNull(path);
        assertTrue(path.isEmpty(), "Empty map should return an empty path");
    }

    @Test
    void testFindPath_StartOutsideMap_ReturnsEmptyList() {
        int[][] map = new int[5][5];

        List<int[]> path = AStar.findPath(map, -1, 0, 1, 1);

        assertNotNull(path);
        assertTrue(path.isEmpty(), "Start outside map should return an empty path");
    }

    @Test
    void testFindPath_EndOutsideMap_ReturnsEmptyList() {
        int[][] map = new int[5][5];

        List<int[]> path = AStar.findPath(map, 0, 0, 5, 5);

        assertNotNull(path);
        assertTrue(path.isEmpty(), "End outside map should return an empty path");
    }

    @Test
    void testFindPath_StartBlocked_ReturnsEmptyList() {
        int[][] map = new int[5][5];
        map[0][0] = 1;

        List<int[]> path = AStar.findPath(map, 0, 0, 4, 4);

        assertNotNull(path);
        assertTrue(path.isEmpty(), "Blocked start tile should return an empty path");
    }

    @Test
    void testFindPath_EndBlocked_ReturnsEmptyList() {
        int[][] map = new int[5][5];
        map[4][4] = 1;

        List<int[]> path = AStar.findPath(map, 0, 0, 4, 4);

        assertNotNull(path);
        assertTrue(path.isEmpty(), "Blocked end tile should return an empty path");
    }

    @Test
    void testFindPath_StraightLinePath_Exists() {
        int[][] map = new int[5][5];

        List<int[]> path = AStar.findPath(map, 0, 0, 0, 3);

        assertNotNull(path);
        assertFalse(path.isEmpty(), "Straight line path should exist");
        assertArrayEquals(new int[]{0, 3}, path.get(path.size() - 1));
    }

    @Test
    void testFindPath_NoRoute_ReturnsEmptyList() {
        int[][] map = new int[5][5];
        map[1][0] = 1;
        map[0][1] = 1;
        map[1][1] = 1;

        List<int[]> path = AStar.findPath(map, 0, 0, 4, 4);

        assertNotNull(path);
        assertTrue(path.isEmpty(), "Completely trapped start should return an empty path");
    }
}
