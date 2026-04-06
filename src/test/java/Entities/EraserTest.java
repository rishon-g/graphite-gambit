package Entities;

import Game.GameWorld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.GameTest;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EraserTest extends GameTest {

    private Eraser eraser;
    private Player player;

    @BeforeEach
    public void setUp() {
        eraser = new Eraser(mockWorld);
        player = new Player(mockWorld);
    }

    @Test
    void testBeforeMovementUpdate_SavesInitialSpawnPosition() throws Exception {
        eraser.transform.setPosition(150, 220);

        eraser.beforeMovementUpdate(0.1f);

        assertEquals(150f, getPrivateFloat(eraser, "startX"));
        assertEquals(220f, getPrivateFloat(eraser, "startY"));
    }

    @Test
    void testBeforeMovementUpdate_DoesNotOverwriteInitialSpawnPosition() throws Exception {
        eraser.transform.setPosition(150, 220);
        eraser.beforeMovementUpdate(0.1f);

        eraser.transform.setPosition(700, 900);
        eraser.beforeMovementUpdate(0.1f);

        assertEquals(150f, getPrivateFloat(eraser, "startX"));
        assertEquals(220f, getPrivateFloat(eraser, "startY"));
    }

    @Test
    void testBeforeMovementUpdate_RespawnDecreases() throws Exception {
        setPrivateFloat(eraser, "respawnTimer", 1.0f);

        eraser.beforeMovementUpdate(0.25f);

        assertEquals(0.75f, getPrivateFloat(eraser, "respawnTimer"), 0.0001f);
    }

    @Test
    void testBeforeMovementUpdate_RespawnClampsToZero() throws Exception {
        setPrivateFloat(eraser, "respawnTimer", 0.2f);

        eraser.beforeMovementUpdate(0.5f);

        assertEquals(0f, getPrivateFloat(eraser, "respawnTimer"), 0.0001f);
    }

    @Test
    void testBeforeMovementUpdate_ZeroDelta_DoesNotChangeRespawn() throws Exception {
        setPrivateFloat(eraser, "respawnTimer", 0.6f);

        eraser.beforeMovementUpdate(0.0f);

        assertEquals(0.6f, getPrivateFloat(eraser, "respawnTimer"), 0.0001f);
    }

    @Test
    void testBeforeMovementUpdate_ErasesFloorOnlyWhileMoving() {
        eraser.transform.setPosition(100, 100);

        eraser.transform.setVelocity(0, 0);
        eraser.beforeMovementUpdate(0.1f);

        verify(mockWorld, never()).floorDraw(anyFloat(), anyFloat(), eq(true), eq(10), any());

        eraser.transform.setVelocity(50, 0);
        eraser.beforeMovementUpdate(0.1f);

        verify(mockWorld, times(1)).floorDraw(anyFloat(), anyFloat(), eq(true), eq(10), any());
    }

    @Test
    void testBeforeMovementUpdate_FacingRight() throws Exception {
        eraser.transform.setPosition(100, 100);
        eraser.transform.setVelocity(50, 0);

        eraser.beforeMovementUpdate(0.1f);

        assertEquals(2, getFacingValue(eraser));
    }

    @Test
    void testBeforeMovementUpdate_FacingLeft() throws Exception {
        eraser.transform.setPosition(100, 100);
        eraser.transform.setVelocity(-50, 0);

        eraser.beforeMovementUpdate(0.1f);

        assertEquals(3, getFacingValue(eraser));
    }

    @Test
    void testBeforeMovementUpdate_FacingUp() throws Exception {
        eraser.transform.setPosition(100, 100);
        eraser.transform.setVelocity(0, 50);

        eraser.beforeMovementUpdate(0.1f);

        assertEquals(1, getFacingValue(eraser));
    }

    @Test
    void testBeforeMovementUpdate_FacingDown() throws Exception {
        eraser.transform.setPosition(100, 100);
        eraser.transform.setVelocity(0, -50);

        eraser.beforeMovementUpdate(0.1f);

        assertEquals(0, getFacingValue(eraser));
    }

    @Test
    void testBeforeMovementUpdate_DiagonalMovement_HorizontalDominatesRight() throws Exception {
        eraser.transform.setPosition(100, 100);
        eraser.transform.setVelocity(80, 20);

        eraser.beforeMovementUpdate(0.1f);

        assertEquals(2, getFacingValue(eraser)); // RIGHT
    }

    @Test
    void testBeforeMovementUpdate_DiagonalMovement_HorizontalDominatesLeft() throws Exception {
        eraser.transform.setPosition(100, 100);
        eraser.transform.setVelocity(-80, 20);

        eraser.beforeMovementUpdate(0.1f);

        assertEquals(3, getFacingValue(eraser)); // LEFT
    }

    @Test
    void testBeforeMovementUpdate_DiagonalMovement_VerticalDominatesUp() throws Exception {
        eraser.transform.setPosition(100, 100);
        eraser.transform.setVelocity(20, 80);

        eraser.beforeMovementUpdate(0.1f);

        assertEquals(1, getFacingValue(eraser)); // UP
    }

    @Test
    void testBeforeMovementUpdate_DiagonalMovement_VerticalDominatesDown() throws Exception {
        eraser.transform.setPosition(100, 100);
        eraser.transform.setVelocity(20, -80);

        eraser.beforeMovementUpdate(0.1f);

        assertEquals(0, getFacingValue(eraser)); // DOWN
    }

    @Test
    void testGetMoveSpeed_ReturnsCorrectValue() {
        assertEquals(400f, eraser.getMoveSpeed(), 0.0001f);
    }

    @Test
    void testGetAttackRange_Set() {
        assertEquals(50f, eraser.ATTACK_RANGE, 0.0001f);
    }

    @Test
    void testPlayerCollide_DamagesPlayerRespawnsAndStartsRespawnTimer() throws Exception {
        eraser.transform.setPosition(300, 400);
        eraser.beforeMovementUpdate(0.1f);

        eraser.transform.setPosition(700, 800);
        eraser.transform.setVelocity(40, 20);

        eraser.currentPath = List.of(new int[] { 2, 2 });
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
        assertEquals(5.0f, getPrivateFloat(eraser, "respawnTimer"), 0.0001f);

        verify(mockAudio, times(1)).playDamage();
    }

    @Test
    void testPlayerCollide_WithAlreadyEmptyPath_StillRespawnsAndResetsState() throws Exception {
        eraser.transform.setPosition(250, 350);
        eraser.beforeMovementUpdate(0.1f);

        eraser.transform.setPosition(600, 700);
        eraser.transform.setVelocity(30, 40);
        eraser.currentPath = List.of();
        eraser.pathIndex = 0;
        eraser.pathTimer = 0.9f;

        int healthBefore = player.getHealth();

        eraser.playerCollide(player);

        assertEquals(healthBefore - 10, player.getHealth());
        assertEquals(250f, eraser.transform.position.x);
        assertEquals(350f, eraser.transform.position.y);
        assertEquals(0f, eraser.transform.velocity.x, 0.0001f);
        assertEquals(0f, eraser.transform.velocity.y, 0.0001f);
        assertTrue(eraser.currentPath.isEmpty());
        assertEquals(0, eraser.pathIndex);
        assertEquals(0f, eraser.pathTimer, 0.0001f);
        verify(mockAudio, times(1)).playDamage();
    }

    @Test
    void testPlayerCollide_DuringRespawn_DoesNothing() throws Exception {
        eraser.transform.setPosition(100, 100);
        eraser.beforeMovementUpdate(0.1f);

        setPrivateFloat(eraser, "respawnTimer", 0.5f);

        int healthBefore = player.getHealth();

        eraser.playerCollide(player);

        assertEquals(healthBefore, player.getHealth());
        verify(mockAudio, never()).playDamage();
    }

    @Test
    void testPlayerCollide_ImmunePlayer_DoesNothing() {
        player.isImmune = true;

        eraser.transform.setPosition(100, 100);
        eraser.beforeMovementUpdate(0.1f);

        int healthBefore = player.getHealth();

        eraser.playerCollide(player);

        assertEquals(healthBefore, player.getHealth());
        verify(mockAudio, never()).playDamage();
    }

    @Test
    void testPlayerCollide_StunnedPlayer_DoesNothing() {
        player.isStunned = true;

        eraser.transform.setPosition(100, 100);
        eraser.beforeMovementUpdate(0.1f);

        int healthBefore = player.getHealth();

        eraser.playerCollide(player);

        assertEquals(healthBefore, player.getHealth());
        verify(mockAudio, never()).playDamage();
    }

    @Test
    void testPlayerCollide_RespawnPreventsSecondHit() {
        eraser.transform.setPosition(100, 100);
        eraser.beforeMovementUpdate(0.1f);

        int healthBefore = player.getHealth();

        eraser.playerCollide(player);
        eraser.playerCollide(player);

        assertEquals(healthBefore - 10, player.getHealth());
        verify(mockAudio, times(1)).playDamage();
    }

    @Test
    void testPlayerCollide_AfterRespawn_CanHitAgain() {
        eraser.transform.setPosition(100, 100);
        eraser.beforeMovementUpdate(0.1f);

        int healthBefore = player.getHealth();

        eraser.playerCollide(player);
        eraser.beforeMovementUpdate(5.1f);
        eraser.playerCollide(player);

        assertEquals(healthBefore - 20, player.getHealth());
        verify(mockAudio, times(2)).playDamage();
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

    private int getFacingValue(Object target) throws Exception {
        Field field = target.getClass().getDeclaredField("facing");
        field.setAccessible(true);
        return field.getInt(target);
    }
}