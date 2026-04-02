package Pathfinding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RandomPatrolTest {

    @Test
    void testChoosePatrolTarget_NullMap_ReturnsNull() {
        int[] result = RandomPatrol.choosePatrolTarget(null, 0, 0, 3);
        assertNull(result);
    }

    @Test
    void testChoosePatrolTarget_EmptyMap_ReturnsNull() {
        int[][] map = new int[0][0];

        int[] result = RandomPatrol.choosePatrolTarget(map, 0, 0, 3);

        assertNull(result);
    }

    @Test
    void testChoosePatrolTarget_StartOutsideMap_ReturnsNull() {
        int[][] map = new int[5][5];

        int[] result = RandomPatrol.choosePatrolTarget(map, 5, 0, 3);

        assertNull(result);
    }

    @Test
    void testChoosePatrolTarget_StartBlocked_ReturnsNull() {
        int[][] map = new int[5][5];
        map[2][2] = 1;

        int[] result = RandomPatrol.choosePatrolTarget(map, 2, 2, 3);

        assertNull(result);
    }

    @Test
    void testChoosePatrolTarget_NoAvailableTiles_ReturnsNull() {
        int[][] map = new int[3][3];

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                map[x][y] = 1;
            }
        }

        map[1][1] = 0;

        int[] result = RandomPatrol.choosePatrolTarget(map, 1, 1, 1);

        assertNull(result);
    }

    @Test
    void testChoosePatrolTarget_ReturnsNonBlockedNonStartTile() {
        int[][] map = new int[5][5];

        int startX = 2;
        int startY = 2;

        int[] result = RandomPatrol.choosePatrolTarget(map, startX, startY, 2);

        assertNotNull(result);
        assertFalse(result[0] == startX && result[1] == startY,
                "Returned tile should not be the start tile");
        assertEquals(0, map[result[0]][result[1]],
                "Returned tile should not be blocked");
    }

    @Test
    void testChoosePatrolTarget_ReturnsTileWithinSearchRadius() {
        int[][] map = new int[7][7];

        int startX = 3;
        int startY = 3;
        int radius = 2;

        int[] result = RandomPatrol.choosePatrolTarget(map, startX, startY, radius);

        assertNotNull(result);
        assertTrue(result[0] >= startX - radius && result[0] <= startX + radius);
        assertTrue(result[1] >= startY - radius && result[1] <= startY + radius);
    }

    @Test
    void testChoosePatrolTarget_WithOnlyOneValidTile_ReturnsThatTile() {
        int[][] map = new int[3][3];

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                map[x][y] = 1;
            }
        }

        map[1][1] = 0;
        map[1][2] = 0;

        int[] result = RandomPatrol.choosePatrolTarget(map, 1, 1, 1);

        assertNotNull(result);
        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
    }

    @Test
    void testChoosePatrolTarget_DoesNotReturnBlockedTile() {
        int[][] map = new int[5][5];
        int startX = 2;
        int startY = 2;

        map[1][1] = 1;
        map[1][2] = 1;
        map[1][3] = 1;
        map[2][1] = 1;
        map[3][1] = 1;
        map[3][2] = 1;
        map[3][3] = 1;

        int[] result = RandomPatrol.choosePatrolTarget(map, startX, startY, 1);

        assertNotNull(result);
        assertEquals(2, result[0]);
        assertEquals(3, result[1]);
    }

    @Test
    void testChoosePatrolTarget_UnreachableFreeTiles_ReturnsNull() {
        int[][] map = new int[5][5];

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                map[x][y] = 1;
            }
        }

        map[2][2] = 0;

        map[4][4] = 0;

        int[] result = RandomPatrol.choosePatrolTarget(map, 2, 2, 3);

        assertNull(result);
    }
}