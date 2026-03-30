package Entities;

import Game.AudioManager;
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
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class EraserTest {

    private GameWorld world;
    private Eraser eraser;

    @BeforeAll
    static void initLibgdx() {
        GdxNativesLoader.load();
    }

    @BeforeEach
    void setUp() throws Exception {
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
        eraser = new Eraser(world);

        AudioManager audioManager = mock(AudioManager.class);
        Field instanceField = AudioManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, audioManager);
    }

    @Test
    void testBeforeMovementUpdate_SavesInitialSpawnPosition() throws Exception {
        eraser.transform.setPosition(150, 220);

        eraser.beforeMovementUpdate(0.1f);

        assertEquals(150f, getPrivateFloat(eraser, "startX"));
        assertEquals(220f, getPrivateFloat(eraser, "startY"));
    }

    @Test
    void testBeforeMovementUpdate_ErasesFloorOnlyWhileMoving() {
        eraser.transform.setPosition(100, 100);

        eraser.transform.setVelocity(0, 0);
        eraser.beforeMovementUpdate(0.1f);

        verify(world, never()).floorDraw(anyFloat(), anyFloat(), eq(true), eq(10), any());

        eraser.transform.setVelocity(50, 0);
        eraser.beforeMovementUpdate(0.1f);

        verify(world, times(1)).floorDraw(anyFloat(), anyFloat(), eq(true), eq(10), any());
    }

    @Test
    void testGetMoveSpeed_ReturnsCorrectValue() {
        assertEquals(400f, eraser.getMoveSpeed(), 0.0001f);
    }

    @Test
    void testPlayerCollide_DamagesPlayerRespawnsAndStartsCooldown() throws Exception {
        Player player = new Player(world);

        eraser.transform.setPosition(300, 400);
        eraser.beforeMovementUpdate(0.1f);
        eraser.transform.setPosition(700, 800);
        eraser.transform.setVelocity(40, 20);

        eraser.currentPath = List.of(new int[]{2, 2});
        eraser.pathIndex = 1;
        eraser.pathTimer = 0.7f;

        int healthBefore = player.getHealth();

        eraser.playerCollide(player);

        assertEquals(healthBefore - 10, player.getHealth());
        assertEquals(300f, eraser.transform.position.x);
        assertEquals(400f, eraser.transform.position.y);
        assertEquals(0f, eraser.transform.velocity.x);
        assertEquals(0f, eraser.transform.velocity.y);
        assertTrue(eraser.currentPath.isEmpty());
        assertEquals(0, eraser.pathIndex);
        assertEquals(0f, eraser.pathTimer);
        assertEquals(1.0f, getPrivateFloat(eraser, "attackCooldownTimer"));
    }

    @Test
    void testPlayerCollide_DuringCooldown_DoesNothing() throws Exception {
        Player player = new Player(world);

        eraser.transform.setPosition(100, 100);
        eraser.beforeMovementUpdate(0.1f);

        setPrivateFloat(eraser, "attackCooldownTimer", 0.5f);

        int healthBefore = player.getHealth();

        eraser.playerCollide(player);

        assertEquals(healthBefore, player.getHealth());
    }

    @Test
    void testPlayerCollide_ImmunePlayer_DoesNothing() {
        Player player = new Player(world);
        player.isImmune = true;

        eraser.transform.setPosition(100, 100);
        eraser.beforeMovementUpdate(0.1f);

        int healthBefore = player.getHealth();

        eraser.playerCollide(player);

        assertEquals(healthBefore, player.getHealth());
    }

    @Test
    void testPlayerCollide_StunnedPlayer_DoesNothing() {
        Player player = new Player(world);
        player.isStunned = true;

        eraser.transform.setPosition(100, 100);
        eraser.beforeMovementUpdate(0.1f);

        int healthBefore = player.getHealth();

        eraser.playerCollide(player);

        assertEquals(healthBefore, player.getHealth());
    }

    @Test
    void testPlayerCollide_CooldownPreventsSecondHit() {
        Player player = new Player(world);

        eraser.transform.setPosition(100, 100);
        eraser.beforeMovementUpdate(0.1f);

        int healthBefore = player.getHealth();

        eraser.playerCollide(player);
        eraser.playerCollide(player);

        assertEquals(healthBefore - 10, player.getHealth());
    }

    private float getPrivateFloat(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getFloat(target);
    }

    private void setPrivateFloat(Object target, String fieldName, float value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setFloat(target, value);
    }
}