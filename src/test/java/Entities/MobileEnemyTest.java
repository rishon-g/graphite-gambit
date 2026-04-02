package Entities;

import Game.GameWorld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.GameTest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class MobileEnemyTest extends GameTest {

    private Player player;
    private TestMobileEnemy enemy;

    @BeforeEach
    public void setUp() {
        player = new Player(mockWorld);

        when(mockWorld.getPlayer()).thenReturn(player);
        when(mockWorld.getTilemap()).thenReturn(new int[30][30]);

        enemy = new TestMobileEnemy(mockWorld);
        enemy.transform.setScale(64, 64);
        enemy.transform.setPosition(32, 32);

        player.transform.setScale(64, 64);
        player.transform.setPosition(2000, 2000);
    }

    @Test
    void testUpdateInternal_BeforeMovementUpdateIsCalled() {
        enemy.currentPath = Collections.emptyList();

        enemy.updateInternal(0.1f);

        assertTrue(enemy.beforeMovementCalled,
                "beforeMovementUpdate should be called before movement logic");
    }

    @Test
    void testUpdateInternal_NoPlayer_SetsVelocityToZero() {
        when(mockWorld.getPlayer()).thenReturn(null);

        enemy.transform.setVelocity(100, 50);
        enemy.updateInternal(0.1f);

        assertEquals(0f, enemy.transform.velocity.x);
        assertEquals(0f, enemy.transform.velocity.y);
    }

    @Test
    void testUpdateInternal_PlayerFarAway_UsesPatrolRebuild() {
        enemy.currentPath = Collections.emptyList();
        player.transform.setPosition(2000, 2000);

        enemy.updateInternal(0.1f);

        assertEquals(MobileEnemy.MovementState.PATROL, enemy.movementState);
        assertEquals(1, enemy.patrolRebuildCalls);
        assertEquals(0, enemy.chaseRebuildCalls);
    }

    @Test
    void testUpdateInternal_PlayerInVision_UsesChaseRebuild() {
        enemy.currentPath = Collections.emptyList();
        player.transform.setPosition(100, 100);

        enemy.updateInternal(0.1f);

        assertEquals(MobileEnemy.MovementState.CHASE, enemy.movementState);
        assertEquals(1, enemy.chaseRebuildCalls);
        assertEquals(0, enemy.patrolRebuildCalls);
    }

    @Test
    void testUpdateInternal_PathFinished_InPatrol_RebuildsPatrolPath() {
        enemy.currentPath = List.of(new int[]{1, 0});
        enemy.pathIndex = 1;
        player.transform.setPosition(2000, 2000);

        enemy.updateInternal(0.1f);

        assertEquals(1, enemy.patrolRebuildCalls);
        assertEquals(0, enemy.chaseRebuildCalls);
        assertEquals(0, enemy.pathIndex);
    }

    @Test
    void testUpdateInternal_PathFinished_InChase_RebuildsChasePath() {
        enemy.currentPath = List.of(new int[]{1, 0});
        enemy.pathIndex = 1;
        player.transform.setPosition(100, 100);

        enemy.updateInternal(0.1f);

        assertEquals(1, enemy.chaseRebuildCalls);
        assertEquals(0, enemy.patrolRebuildCalls);
        assertEquals(0, enemy.pathIndex);
    }

    @Test
    void testUpdateInternal_WithPath_MovesTowardNextTile() {
        enemy.currentPath = List.of(new int[]{1, 0});
        enemy.pathIndex = 0;

        enemy.updateInternal(0.1f);

        assertTrue(enemy.transform.velocity.x > 0,
                "Enemy should move right toward next tile");
        assertEquals(0f, enemy.transform.velocity.y, 0.0001f);
    }

    @Test
    void testUpdateInternal_NoValidPatrolPath_SetsVelocityToZero() {
        enemy.currentPath = Collections.emptyList();
        enemy.patrolRebuildResult = Collections.emptyList();
        player.transform.setPosition(2000, 2000);

        enemy.updateInternal(0.1f);

        assertEquals(0f, enemy.transform.velocity.x);
        assertEquals(0f, enemy.transform.velocity.y);
    }

    @Test
    void testUpdateInternal_NoValidChasePath_SetsVelocityToZero() {
        enemy.currentPath = Collections.emptyList();
        enemy.chaseRebuildResult = Collections.emptyList();
        player.transform.setPosition(100, 100);

        enemy.updateInternal(0.1f);

        assertEquals(0f, enemy.transform.velocity.x);
        assertEquals(0f, enemy.transform.velocity.y);
    }

    @Test
    void testUpdateInternal_PatrolRebuildWithNewPathStartsMovement() {
        enemy.currentPath = Collections.emptyList();
        enemy.patrolRebuildResult = List.of(new int[]{1, 0});
        player.transform.setPosition(2000, 2000);

        enemy.updateInternal(0.1f);

        assertEquals(1, enemy.patrolRebuildCalls);
        assertTrue(enemy.transform.velocity.x > 0);
    }

    @Test
    void testUpdateInternal_ChaseRebuildWithNewPathStartsMovement() {
        enemy.currentPath = Collections.emptyList();
        enemy.chaseRebuildResult = List.of(new int[]{1, 0});
        player.transform.setPosition(100, 100);

        enemy.updateInternal(0.1f);

        assertEquals(1, enemy.chaseRebuildCalls);
        assertTrue(enemy.transform.velocity.x > 0);
    }

    @Test
    void testUpdateMovementState_StaysInChaseUntilLoseRadius() {
        enemy.movementState = MobileEnemy.MovementState.CHASE;
        player.transform.setPosition(830, 32);

        enemy.updateMovementState(player);

        assertEquals(MobileEnemy.MovementState.CHASE, enemy.movementState);
    }

    @Test
    void testUpdateMovementState_LeavesChaseAfterLoseRadius() {
        enemy.movementState = MobileEnemy.MovementState.CHASE;
        player.transform.setPosition(1200, 32);

        enemy.updateMovementState(player);

        assertEquals(MobileEnemy.MovementState.PATROL, enemy.movementState);
    }

    @Test
    void testRebuildPath_NullMap_GivesEmptyPath() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(mockWorld);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(32, 32);

        when(mockWorld.getTilemap()).thenReturn(null);

        realEnemy.rebuildPath(player);

        assertTrue(realEnemy.currentPath.isEmpty());
        assertEquals(0, realEnemy.pathIndex);
    }

    @Test
    void testRebuildPatrolPath_NullMap_GivesEmptyPath() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(mockWorld);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(32, 32);

        when(mockWorld.getTilemap()).thenReturn(null);

        realEnemy.rebuildPatrolPath();

        assertTrue(realEnemy.currentPath.isEmpty());
        assertEquals(0, realEnemy.pathIndex);
    }

    @Test
    void testRebuildPath_EmptyMap_GivesEmptyPath() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(mockWorld);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(32, 32);

        when(mockWorld.getTilemap()).thenReturn(new int[0][0]);

        realEnemy.rebuildPath(player);

        assertTrue(realEnemy.currentPath.isEmpty());
        assertEquals(0, realEnemy.pathIndex);
    }

    @Test
    void testFindBestTargetTile_ExactPlayerTileReachable() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(mockWorld);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(0, 0);

        player.transform.setScale(64, 64);
        player.transform.setPosition(224, 224);

        int[][] map = new int[5][5];

        int[] result = realEnemy.findBestTargetTile(player, map, 0, 0);

        assertNotNull(result);
        assertEquals(1, result[0]);
        assertEquals(1, result[1]);
    }

    @Test
    void testFindBestTargetTile_BlockedPlayerTileFindsNearbyAlternative() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(mockWorld);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(0, 0);

        player.transform.setScale(64, 64);
        player.transform.setPosition(224, 224);

        int[][] map = new int[5][5];
        map[2][2] = 1;

        int[] result = realEnemy.findBestTargetTile(player, map, 0, 0);

        assertNotNull(result);
        assertFalse(result[0] == 2 && result[1] == 2);
    }

    @Test
    void testFindBestTargetTile_NoReachableTarget_ReturnsNull() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(mockWorld);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(0, 0);

        player.transform.setScale(64, 64);
        player.transform.setPosition(128, 128);

        int[][] map = new int[5][5];
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                map[x][y] = 1;
            }
        }

        int[] result = realEnemy.findBestTargetTile(player, map, 0, 0);

        assertNull(result);
    }

    @Test
    void testShouldRebuildPath_EmptyPath_ReturnsTrue() {
        enemy.currentPath = Collections.emptyList();
        enemy.pathIndex = 0;

        assertTrue(enemy.shouldRebuildPath());
    }

    @Test
    void testShouldRebuildPath_PathFinished_ReturnsTrue() {
        enemy.currentPath = List.of(new int[]{1, 0});
        enemy.pathIndex = 1;

        assertTrue(enemy.shouldRebuildPath());
    }

    @Test
    void testShouldRebuildPath_ChaseBeforeTimer_ReturnsFalse() {
        enemy.currentPath = List.of(new int[]{1, 0});
        enemy.pathIndex = 0;
        enemy.movementState = MobileEnemy.MovementState.CHASE;
        enemy.pathTimer = 0.1f;

        assertFalse(enemy.shouldRebuildPath());
    }

    @Test
    void testShouldRebuildPath_ChaseAfterTimer_ReturnsTrue() {
        enemy.currentPath = List.of(new int[]{1, 0});
        enemy.pathIndex = 0;
        enemy.movementState = MobileEnemy.MovementState.CHASE;
        enemy.pathTimer = 0.5f;

        assertTrue(enemy.shouldRebuildPath());
    }

    @Test
    void testShouldRebuildPath_PatrolBeforeTimer_ReturnsFalse() {
        enemy.currentPath = List.of(new int[]{1, 0});
        enemy.pathIndex = 0;
        enemy.movementState = MobileEnemy.MovementState.PATROL;
        enemy.pathTimer = 0.2f;

        assertFalse(enemy.shouldRebuildPath());
    }

    @Test
    void testShouldRebuildPath_PatrolAfterTimer_ReturnsTrue() {
        enemy.currentPath = List.of(new int[]{1, 0});
        enemy.pathIndex = 0;
        enemy.movementState = MobileEnemy.MovementState.PATROL;
        enemy.pathTimer = 1.0f;

        assertTrue(enemy.shouldRebuildPath());
    }

    @Test
    void testUpdateMovementState_NullPlayer_GoesToPatrol() {
        enemy.movementState = MobileEnemy.MovementState.CHASE;

        enemy.updateMovementState(null);

        assertEquals(MobileEnemy.MovementState.PATROL, enemy.movementState);
    }

    @Test
    void testUpdateMovementState_PlayerFarAway_StaysPatrol() {
        enemy.movementState = MobileEnemy.MovementState.PATROL;
        player.transform.setPosition(3000, 3000);

        enemy.updateMovementState(player);

        assertEquals(MobileEnemy.MovementState.PATROL, enemy.movementState);
    }

    @Test
    void testUpdateInternal_DoesNotRebuildIfPathExistsAndTimerTooSmall_Chase() {
        enemy.currentPath = List.of(new int[]{2, 0});
        enemy.pathIndex = 0;
        enemy.pathTimer = 0f;
        player.transform.setPosition(100, 100);

        enemy.updateInternal(0.1f);

        assertEquals(0, enemy.chaseRebuildCalls);
        assertEquals(0, enemy.patrolRebuildCalls);
    }

    @Test
    void testUpdateInternal_DoesNotRebuildIfPathExistsAndTimerTooSmall_Patrol() {
        enemy.currentPath = List.of(new int[]{2, 0});
        enemy.pathIndex = 0;
        enemy.pathTimer = 0f;
        player.transform.setPosition(3000, 3000);

        enemy.updateInternal(0.1f);

        assertEquals(0, enemy.chaseRebuildCalls);
        assertEquals(0, enemy.patrolRebuildCalls);
    }

    @Test
    void testUpdateInternal_AtTargetTile_AdvancesToNextTile() {
        enemy.transform.setScale(64, 64);

        enemy.currentPath = List.of(new int[]{1, 0}, new int[]{2, 0});
        enemy.pathIndex = 0;
        player.transform.setPosition(3000, 3000);

        float tileSize = GameWorld.getTileSize();
        float targetX = 1 * tileSize + (tileSize - enemy.transform.size.x) / 2f;
        float targetY = 0 * tileSize + (tileSize - enemy.transform.size.y) / 2f;

        enemy.transform.setPosition(targetX, targetY);

        enemy.updateInternal(0.1f);

        assertEquals(1, enemy.pathIndex);
        assertTrue(enemy.transform.velocity.x > 0);
    }

    @Test
    void testUpdateInternal_AllTargetsAlreadyReached_SetsVelocityToZero() {
        enemy.transform.setScale(64, 64);

        enemy.currentPath = List.of(new int[]{1, 0});
        enemy.pathIndex = 0;
        player.transform.setPosition(3000, 3000);

        float tileSize = GameWorld.getTileSize();
        float targetX = 1 * tileSize + (tileSize - enemy.transform.size.x) / 2f;
        float targetY = 0 * tileSize + (tileSize - enemy.transform.size.y) / 2f;

        enemy.transform.setPosition(targetX, targetY);

        enemy.updateInternal(0.1f);

        assertEquals(0f, enemy.transform.velocity.x, 0.0001f);
        assertEquals(0f, enemy.transform.velocity.y, 0.0001f);
        assertEquals(1, enemy.pathIndex);
    }

    @Test
    void testTryAttackPlayer_OutOfRange_DoesNotCallPlayerCollide() {
        enemy.transform.setScale(64, 64);
        enemy.transform.setPosition(32, 32);

        player.transform.setScale(64, 64);
        player.transform.setPosition(500, 500);

        enemy.tryAttackPlayer(player);

        assertEquals(0, enemy.collideCalls);
    }

    @Test
    void testIsPlayerWithinAttackRange_TrueWhenOverlapping() {
        enemy.transform.setScale(64, 64);
        enemy.transform.setPosition(32, 32);

        player.transform.setScale(64, 64);
        player.transform.setPosition(40, 40);

        assertTrue(enemy.isPlayerWithinAttackRange(player));
    }

    @Test
    void testIsPlayerWithinAttackRange_FalseWhenTooFar() {
        enemy.transform.setScale(64, 64);
        enemy.transform.setPosition(32, 32);

        player.transform.setScale(64, 64);
        player.transform.setPosition(500, 500);

        assertFalse(enemy.isPlayerWithinAttackRange(player));
    }

    @Test
    void testRebuildPatrolPath_BlockedStart_GivesEmptyPath() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(mockWorld);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(32, 32);

        int[][] map = new int[30][30];

        int startX = (int) ((realEnemy.transform.position.x + realEnemy.transform.size.x / 2f) / GameWorld.getTileSize());
        int startY = (int) ((realEnemy.transform.position.y + realEnemy.transform.size.y / 2f) / GameWorld.getTileSize());

        map[startX][startY] = 1;
        when(mockWorld.getTilemap()).thenReturn(map);

        realEnemy.rebuildPatrolPath();

        assertTrue(realEnemy.currentPath.isEmpty());
        assertEquals(0, realEnemy.pathIndex);
    }

    @Test
    void testRebuildPath_BlockedStart_GivesEmptyPath() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(mockWorld);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(32, 32);

        int[][] map = new int[30][30];

        int startX = (int) ((realEnemy.transform.position.x + realEnemy.transform.size.x / 2f) / GameWorld.getTileSize());
        int startY = (int) ((realEnemy.transform.position.y + realEnemy.transform.size.y / 2f) / GameWorld.getTileSize());

        map[startX][startY] = 1;
        when(mockWorld.getTilemap()).thenReturn(map);

        realEnemy.rebuildPath(player);

        assertTrue(realEnemy.currentPath.isEmpty());
        assertEquals(0, realEnemy.pathIndex);
    }

    @Test
    void testTryAttackPlayer_NullPlayer_DoesNothing() {
        enemy.tryAttackPlayer(null);

        assertEquals(0, enemy.collideCalls);
    }

    @Test
    void testIsPlayerWithinAttackRange_NullPlayer_ReturnsFalse() {
        assertFalse(enemy.isPlayerWithinAttackRange(null));
    }

    @Test
    void testTryAttackPlayer_ExactlyOnAttackRange_CallsPlayerCollide() {
        enemy.transform.setScale(64, 64);
        enemy.transform.setPosition(32, 32);

        player.transform.setScale(64, 64);

        float enemyRight = enemy.transform.position.x + enemy.transform.size.x;
        float playerX = enemyRight + enemy.getAttackRange();
        player.transform.setPosition(playerX, 32);

        enemy.tryAttackPlayer(player);

        assertEquals(1, enemy.collideCalls);
    }

    @Test
    void testTryAttackPlayer_HorizontalGapWithinRange_CallsPlayerCollide() {
        enemy.transform.setScale(64, 64);
        enemy.transform.setPosition(100, 100);

        player.transform.setScale(64, 64);
        player.transform.setPosition(100 + 64 + 20, 100);

        enemy.tryAttackPlayer(player);

        assertEquals(1, enemy.collideCalls);
    }

    @Test
    void testTryAttackPlayer_VerticalGapWithinRange_CallsPlayerCollide() {
        enemy.transform.setScale(64, 64);
        enemy.transform.setPosition(100, 100);

        player.transform.setScale(64, 64);
        player.transform.setPosition(100, 100 + 64 + 20);

        enemy.tryAttackPlayer(player);

        assertEquals(1, enemy.collideCalls);
    }

    @Test
    void testIsPlayerWithinAttackRange_DiagonalGapWithinRange_ReturnsTrue() {
        enemy.transform.setScale(64, 64);
        enemy.transform.setPosition(100, 100);

        player.transform.setScale(64, 64);

        // gapX = 30, gapY = 30 => sqrt(1800) ≈ 42.43 < 50
        player.transform.setPosition(100 + 64 + 30, 100 + 64 + 30);

        assertTrue(enemy.isPlayerWithinAttackRange(player));
    }

    @Test
    void testIsPlayerWithinAttackRange_DiagonalGapOutOfRange_ReturnsFalse() {
        enemy.transform.setScale(64, 64);
        enemy.transform.setPosition(100, 100);

        player.transform.setScale(64, 64);

        // gapX = 40, gapY = 40 => sqrt(3200) ≈ 56.57 > 50
        player.transform.setPosition(100 + 64 + 40, 100 + 64 + 40);

        assertFalse(enemy.isPlayerWithinAttackRange(player));
    }

    @Test
    void testUpdateMovementState_PlayerExactlyOnVisionRadius_EntersChase() {
        enemy.movementState = MobileEnemy.MovementState.PATROL;

        float enemyCenterX = enemy.transform.position.x + enemy.transform.size.x / 2f;
        float enemyCenterY = enemy.transform.position.y + enemy.transform.size.y / 2f;

        float playerX = enemyCenterX + 700f - player.transform.size.x / 2f;
        float playerY = enemyCenterY - player.transform.size.y / 2f;

        player.transform.setPosition(playerX, playerY);

        enemy.updateMovementState(player);

        assertEquals(MobileEnemy.MovementState.CHASE, enemy.movementState);
    }

    @Test
    void testRebuildPath_TargetTileSameAsStart_GivesEmptyPath() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(mockWorld);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(32, 32);

        int[][] map = new int[30][30];
        when(mockWorld.getTilemap()).thenReturn(map);

        player.transform.setScale(64, 64);
        player.transform.setPosition(32, 32);

        realEnemy.rebuildPath(player);

        assertTrue(realEnemy.currentPath.isEmpty());
        assertEquals(0, realEnemy.pathIndex);
    }

    @Test
    void testRebuildPatrolPath_NoPatrolTarget_GivesEmptyPath() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(mockWorld);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(32, 32);

        int[][] map = new int[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                map[x][y] = 1;
            }
        }

        int startX = (int) ((realEnemy.transform.position.x + realEnemy.transform.size.x / 2f) / GameWorld.getTileSize());
        int startY = (int) ((realEnemy.transform.position.y + realEnemy.transform.size.y / 2f) / GameWorld.getTileSize());
        map[startX][startY] = 0;

        when(mockWorld.getTilemap()).thenReturn(map);

        realEnemy.rebuildPatrolPath();

        assertTrue(realEnemy.currentPath.isEmpty());
        assertEquals(0, realEnemy.pathIndex);
    }

    @Test
    void testRebuildPath_ReachableTarget_BuildsNonEmptyPath() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(mockWorld);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(32, 32);

        int[][] map = new int[30][30];
        when(mockWorld.getTilemap()).thenReturn(map);

        player.transform.setScale(64, 64);
        player.transform.setPosition(300, 300);

        realEnemy.rebuildPath(player);

        assertFalse(realEnemy.currentPath.isEmpty());
        assertEquals(0, realEnemy.pathIndex);
    }

    @Test
    void testRebuildPatrolPath_ReachableTarget_BuildsNonEmptyPath() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(mockWorld);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(32, 32);

        int[][] map = new int[30][30];
        when(mockWorld.getTilemap()).thenReturn(map);

        realEnemy.rebuildPatrolPath();

        assertNotNull(realEnemy.currentPath);
        assertEquals(0, realEnemy.pathIndex);
    }

    @Test
    void testFindBestTargetTile_StartTileReturnedWhenAlreadyInRange() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(mockWorld);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(32, 32);

        player.transform.setScale(64, 64);
        player.transform.setPosition(32, 32);

        int[][] map = new int[5][5];

        int startX = (int) ((realEnemy.transform.position.x + realEnemy.transform.size.x / 2f) / GameWorld.getTileSize());
        int startY = (int) ((realEnemy.transform.position.y + realEnemy.transform.size.y / 2f) / GameWorld.getTileSize());

        int[] result = realEnemy.findBestTargetTile(player, map, startX, startY);

        assertNotNull(result);
        assertEquals(startX, result[0]);
        assertEquals(startY, result[1]);
    }

    @Test
    void testIsPlayerWithinAttackRange_ExactlyOnBoundary_ReturnsTrue() {
        enemy.transform.setScale(64, 64);
        enemy.transform.setPosition(100, 100);

        player.transform.setScale(64, 64);

        float playerX = 100 + 64 + enemy.getAttackRange();
        player.transform.setPosition(playerX, 100);

        assertTrue(enemy.isPlayerWithinAttackRange(player));
    }

    @Test
    void testTryAttackPlayer_DiagonalGapWithinRange_CallsCollide() {
        enemy.transform.setScale(64, 64);
        enemy.transform.setPosition(100, 100);

        player.transform.setScale(64, 64);
        player.transform.setPosition(100 + 64 + 30, 100 + 64 + 30);

        enemy.tryAttackPlayer(player);

        assertEquals(1, enemy.collideCalls);
    }


    static class TestMobileEnemy extends MobileEnemy {
        int chaseRebuildCalls = 0;
        int patrolRebuildCalls = 0;
        int collideCalls = 0;
        boolean beforeMovementCalled = false;

        List<int[]> chaseRebuildResult = List.of(new int[]{1, 0});
        List<int[]> patrolRebuildResult = List.of(new int[]{1, 0});

        TestMobileEnemy(GameWorld world) {
            super(world);
        }

        @Override
        protected void beforeMovementUpdate(float delta) {
            beforeMovementCalled = true;
        }

        @Override
        protected float getMoveSpeed() {
            return 200f;
        }

        @Override
        protected float getAttackRange() {
            return 50f;
        }

        @Override
        protected void rebuildPath(Player player) {
            chaseRebuildCalls++;
            currentPath = chaseRebuildResult;
            pathIndex = 0;
        }

        @Override
        protected void rebuildPatrolPath() {
            patrolRebuildCalls++;
            currentPath = patrolRebuildResult;
            pathIndex = 0;
        }

        @Override
        public void playerCollide(Player player) {
            collideCalls++;
        }

        @Override
        public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, float delta) {
        }
    }

    static class RealPathMobileEnemy extends MobileEnemy {
        RealPathMobileEnemy(GameWorld world) {
            super(world);
        }

        @Override
        protected float getMoveSpeed() {
            return 200f;
        }

        @Override
        protected float getAttackRange() {
            return 0f;
        }

        @Override
        public void playerCollide(Player player) {
        }

        @Override
        public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, float delta) {
        }
    }
}