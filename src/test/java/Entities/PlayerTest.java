package Entities;

import utils.GameTest;
import Game.GameWorld;
import Screens.GameScreen;
import com.badlogic.gdx.Gdx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PlayerTest extends GameTest {

    private Player player;

    @BeforeEach
    public void setUp() {
        GameScreen mockScreen = mock(GameScreen.class);

        mockWorld = spy(new GameWorld(-1, mockScreen));
        mockWorld.setDimensions(2000, 2000);

        player = new Player(mockWorld);
    }

    @Test
    public void testModifyHealth_TakingDamage() {
        // player starts with 100 health from constructor
        // take 30 damage
        player.modifyHealth(-30);

        // health should be 70, and player should not be dead
        assertEquals(70, player.getHealth(), "health should decrease by 30");
        assertFalse(player.dead(), "player should not be dead at 70 health.");
    }

    @Test
    public void testModifyHealth_DeathCondition() {
        // take 100 damage
        player.modifyHealth(-100);

        // health hits 0, player should be marked as dead
        assertEquals(0, player.getHealth(), "health should be exactly 0");
        assertTrue(player.dead(), "player should be marked dead when health reaches 0.");
    }

    @Test
    public void testModifyHealth_ExcessHealingConvertsToPoints() {
        // heal for 20 when already at max health
        player.modifyHealth(20);

        // health should stay capped at 100
        assertEquals(100, player.getHealth(), "health should not exceed the maxHealth (100)");

        // the excess 20 healing should trigger a score event in the mocked GameWorld
        verify(mockWorld, times(1)).score(20);
    }

    @Test
    public void testStun_AppliesStunAndStopsMovement() {
        // give the player some velocity
        player.transform.setVelocity(100f, 100f);

        // stun for 5 seconds
        player.stun(5f);

        // make sure all properties of stun are in place
        assertTrue(player.isStunned, "player should be in the stunned state.");
        assertEquals(5f, player.stunTimer, "stun timer should be set to 5");
        assertEquals(0f, player.transform.velocity.x, "velocity X should be instantly set to 0.");
        assertEquals(0f, player.transform.velocity.y, "velocity Y should be instantly set to 0.");
    }

    @Test
    public void testStun_IgnoredIfImmune() {
        // make the player immune first
        player.isImmune = true;
        player.transform.setVelocity(100f, 100f);

        // attempt to stun
        player.stun(5f);

        // player shouldn't be stunned
        assertFalse(player.isStunned, "player should not be stunned because they are immune");
        assertEquals(100f, player.transform.velocity.x, "velocity X should remain unchanged.");
    }

    @Test
    public void testUpdate_ReducesStunTimerAndRestoresMovement() {
        // stun the player for 2 seconds
        player.stun(2f);
        assertTrue(player.isStunned, "player should be stunned.");

        // simulate 1.5 seconds of game time passing
        player.update(1.5f);

        // still stunned, timer went down
        assertTrue(player.isStunned, "player should still be stunned after 1.5s");
        assertEquals(0.5f, player.stunTimer, 0.001f, "stun timer should decrease by delta time.");

        // simulate another 0.6 seconds passing (pushes timer past 0)
        player.update(0.6f);

        // stun wears off
        assertFalse(player.isStunned, "plater should no longer be stunned.");
        assertEquals(0f, player.stunTimer, "stun timer should clamp at 0 (not negative!).");
    }

    @Test
    public void testUpdate_KeyboardInputMovesPlayer() {
        //velocity starts at 0
        player.transform.velocity.set(0, 0);

        // tell our fake keyboard that the D key is currently pressed down
        when(Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)).thenReturn(true);

        // simulate some game time passing
        player.update(0.5f);

        // the X velocity should now be positive
        assertTrue(player.transform.velocity.x > 0, "x velocity should be positive");
    }

    @Test
    public void testUpdate_MovementDrainsHealth() {
        // player starts at 100 health.
        when(Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)).thenReturn(true);

        // 1.0s goes by
        player.update(1.0f);

        // the player should have lost 2 health for moving 1 full second
        assertEquals(100 + Player.MOVEMENT_HEALTH_LOSS, player.getHealth(), "moving for 1 second should drain health when the player actually moves");
    }

    @Test
    public void testUpdate_StationaryDoesNotDrainHealth() {
        //player starts at 100 health. velocity is 0.
        player.transform.velocity.set(0f, 0f);

        // simulate 2.0 full seconds passing while standing still
        player.update(2.0f);

        // health should remain exactly at 100
        assertEquals(100, player.getHealth(), "standing still should not drain graphite.");
    }

    @Test
    public void testUpdate_ImmunityTimerDecays() {
        // force the player into an immune state with a 1-second timer
        player.isImmune = true;
        player.immunityTimer = 1.0f;

        // simulate 1.0 seconds passing
        player.update(1.0f);

        // immunity should wear off after the timer hits 0
        assertFalse(player.isImmune, "player should lose immunity after the timer expires");
        assertTrue(player.immunityTimer <= 0f, "immunity timer should be 0 or less");
    }

    @Test
    public void testApplyInkSlowdown_ReducesSpeedAndPlaysSludgeSound() {
        // tell the fake keyboard to hold D
        when(Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)).thenReturn(true);

        // apply the ink effect, then simulate 0.5 seconds of game time.
        // With 3200 acceleration, 0.5 seconds is enough to hit max speed.
        player.applyInkSlowdown();
        player.update(0.5f);

        // the max speed should be clamped to 240 (600 base * 0.4 multiplier)
        assertEquals(240f, player.transform.velocity.x, 0.001f, "max speed should be clamped to 240 while in ink.");

        // verify the AudioManager was told to play the ink moving sound
        verify(mockAudio, times(1)).updateMoveSound(true, true);
    }

    @Test
    public void testUpdate_VelocityClampsToMaxSpeed() {
        // tell the keyboard to hold W
        when(Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)).thenReturn(true);

        // simulate a massive amount of time passing (10 seconds).
        // with 3200 acceleration, 10 seconds would normally push velocity to 32k
        player.update(10.0f);

        // the bounding math in updateInternal should have clamped it to the max speed
        assertEquals(Player.BASE_SPEED, player.transform.velocity.y, "Y velocity should not exceed BASE_SPEED");
    }

    @Test
    public void testUpdate_SpacebarReducesStunTimer() {
        // stun the player for 2.0 seconds
        player.stun(2.0f);

        // mock the keyboard to pretend the user just mashed space
        when(Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)).thenReturn(true);

        // simulate a very fast frame (0.1 seconds)
        player.updateInternal(0.1f);

        // the timer should lose 0.1s from time passing + 0.5s from the spacebar
        // expected: 2.0 - 0.1 - 0.5 = 1.4
        assertEquals(1.4f, player.stunTimer, 0.001f, "mashing space should is not reducing stun timer correctly");
    }

    @Test
    public void testUpdate_MicroMovementsDoNotDrainHealth() {
        // player is moving incredibly slowly (below the 1.0f threshold)
        player.transform.velocity.set(0.5f, 0.5f);

        // simulate 1 second
        player.updateInternal(1.0f);

        // no health should be lost
        assertEquals(Player.STARTING_HEALTH, player.getHealth(), "velocity under threshold shouldn't drain health.");
    }

    @Test
    public void testStun_IgnoredIfAlreadyStunned() {
        // stun the player for 5 seconds initially
        player.stun(5f);

        // simulate 2 seconds of game time (timer drops to 3.0)
        player.updateInternal(2.0f);

        // attempt to apply a massive 10-second stun while they are still stunned
        player.stun(10f);

        // the second stun should be completely ignored, leaving the timer at 3.0
        assertEquals(3.0f, player.stunTimer, 0.001f, "subsequent stuns should be ignored");
    }

    @Test
    public void testUpdate_ImmunityTimerTicksWithoutExpiring() {
        // 1s of immunity
        player.isImmune = true;
        player.immunityTimer = 1.0f; // (immunityTimer > 0) branch satisfied

        // simulate only 0.5 seconds passing
        player.updateInternal(0.5f);

        // player should STILL be immune, and timer should be exactly 0.5
        assertTrue(player.isImmune, "player should still be immune (timer didnt hit 0)");
        assertEquals(0.5f, player.immunityTimer, 0.001f, "immunity timer should be at 0.5");
    }

    @Test
    public void testUpdate_MovementUnderOneSecondDoesNotDrainHealth() {
        // give the player velocity so the game thinks they are drawing
        player.transform.velocity.set(100f, 0f);

        // simulate only 0.8 seconds of movement (drainTimer < 1) branch satisfied
        player.updateInternal(0.8f);

        // health should still be exactly at maximum (no false drain)
        assertEquals(Player.STARTING_HEALTH, player.getHealth(), "movement under 1 second should not trigger graphite drain");
    }

    @Test
    public void testUpdate_YAxisMovementDrainsHealth() {
        // player moves only on the Y-axis
        when(Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)).thenReturn(true);

        // simulate exactly 1 second of game time
        player.update(1.0f);

        // the player should lose health, hence we satisfy the Math.abs(velocity.y) > 1f branch
        assertEquals(Player.STARTING_HEALTH + Player.MOVEMENT_HEALTH_LOSS, player.getHealth(), "Moving on the Y axis should drain health.");
    }


}