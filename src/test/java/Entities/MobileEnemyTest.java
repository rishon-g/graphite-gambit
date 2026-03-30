package Entities;

import Game.GameWorld;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxNativesLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class MobileEnemyTest {

    private GameWorld world;
    private Player player;
    private TestMobileEnemy enemy;

    @BeforeAll
    static void initLibgdx() {
        GdxNativesLoader.load();
    }

    @BeforeEach
    void setUp() {
        Gdx.gl = mock(GL20.class);
        Gdx.gl20 = mock(GL20.class);
        Gdx.graphics = mock(Graphics.class);
        Gdx.app = mock(Application.class);
        Gdx.files = mock(com.badlogic.gdx.Files.class);

        when(Gdx.files.internal(anyString())).thenAnswer(invocation -> {
            String path = invocation.getArgument(0);
            return new FileHandle(new File(path));
        });

        world = mock(GameWorld.class);
        player = new Player(world);

        when(world.getPlayer()).thenReturn(player);
        when(world.getTilemap()).thenReturn(new int[5][5]);

        enemy = new TestMobileEnemy(world);
        enemy.transform.setScale(64, 64);
        enemy.transform.setPosition(32, 32);
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
        when(world.getPlayer()).thenReturn(null);

        enemy.transform.setVelocity(100, 50);
        enemy.updateInternal(0.1f);

        assertEquals(0f, enemy.transform.velocity.x);
        assertEquals(0f, enemy.transform.velocity.y);
    }

    @Test
    void testUpdateInternal_EmptyPath_RebuildsPath() {
        enemy.currentPath = Collections.emptyList();

        enemy.updateInternal(0.1f);

        assertEquals(1, enemy.rebuildCalls);
        assertTrue(enemy.beforeMovementCalled);
    }

    @Test
    void testUpdateInternal_PathFinished_RebuildsAndResetsPathIndex() {
        enemy.currentPath = List.of(new int[]{1, 0});
        enemy.pathIndex = 1;

        enemy.updateInternal(0.1f);

        assertEquals(1, enemy.rebuildCalls);
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
    void testUpdateInternal_NoValidPath_SetsVelocityToZero() {
        enemy.currentPath = Collections.emptyList();
        enemy.rebuildResult = Collections.emptyList();

        enemy.updateInternal(0.1f);

        assertEquals(0f, enemy.transform.velocity.x);
        assertEquals(0f, enemy.transform.velocity.y);
    }

    @Test
    void testUpdateInternal_RebuildWithNewPathStartsMovement() {
        enemy.currentPath = Collections.emptyList();
        enemy.rebuildResult = List.of(new int[]{1, 0});

        enemy.updateInternal(0.1f);

        assertEquals(1, enemy.rebuildCalls);
        assertTrue(enemy.transform.velocity.x > 0);
    }

    @Test
    void testRebuildPath_NullMap_GivesEmptyPath() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(world);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(32, 32);

        when(world.getTilemap()).thenReturn(null);

        realEnemy.rebuildPath(player);

        assertTrue(realEnemy.currentPath.isEmpty());
        assertEquals(0, realEnemy.pathIndex);
    }

    @Test
    void testRebuildPath_EmptyMap_GivesEmptyPath() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(world);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(32, 32);

        when(world.getTilemap()).thenReturn(new int[0][0]);

        realEnemy.rebuildPath(player);

        assertTrue(realEnemy.currentPath.isEmpty());
        assertEquals(0, realEnemy.pathIndex);
    }

    @Test
    void testRebuildPath_SameStartAndTarget_GivesEmptyPath() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(world);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(0, 0);

        player.transform.setScale(64, 64);
        player.transform.setPosition(0, 0);

        when(world.getTilemap()).thenReturn(new int[5][5]);

        realEnemy.rebuildPath(player);

        assertTrue(realEnemy.currentPath.isEmpty());
        assertEquals(0, realEnemy.pathIndex);
    }

    @Test
    void testFindBestTargetTile_ExactPlayerTileReachable() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(world);
        realEnemy.transform.setScale(64, 64);
        realEnemy.transform.setPosition(0, 0);

        player.transform.setScale(64, 64);
        player.transform.setPosition(224, 224);

        int[][] map = new int[5][5];

        int[] result = realEnemy.findBestTargetTile(player, map, 0, 0);

        assertNotNull(result);
        assertEquals(2, result[0]);
        assertEquals(2, result[1]);
    }

    @Test
    void testFindBestTargetTile_BlockedPlayerTileFindsNearbyAlternative() {
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(world);
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
        RealPathMobileEnemy realEnemy = new RealPathMobileEnemy(world);
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

    private static class TestMobileEnemy extends MobileEnemy {
        int rebuildCalls = 0;
        boolean beforeMovementCalled = false;
        List<int[]> rebuildResult = List.of(new int[]{1, 0});

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
        protected void rebuildPath(Player player) {
            rebuildCalls++;
            currentPath = rebuildResult;
            pathIndex = 0;
        }

        @Override
        public void playerCollide(Player player) {
        }

        @Override
        public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, float delta) {
        }
    }

    private static class RealPathMobileEnemy extends MobileEnemy {
        RealPathMobileEnemy(GameWorld world) {
            super(world);
        }

        @Override
        protected float getMoveSpeed() {
            return 200f;
        }

        @Override
        public void playerCollide(Player player) {
        }

        @Override
        public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, float delta) {
        }
    }
}