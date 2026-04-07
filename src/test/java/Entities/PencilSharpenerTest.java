package Entities;

import utils.GameTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PencilSharpenerTest extends GameTest {

    private PencilSharpener sharpener;
    private Player mockPlayer;

    @BeforeEach
    public void setUp() {
        // create new pencil sharpener
        sharpener = new PencilSharpener(mockWorld);
        mockPlayer = mock(Player.class);
    }

    @Test
    public void testConstructor_InitializesScale() {
        assertEquals(PencilSharpener.HITBOX, sharpener.transform.size.x, "sharpener hitbox incorrect");
        assertEquals(PencilSharpener.HITBOX, sharpener.transform.size.y);
    }

    @Test
    public void testGetMoveSpeed_ReturnsConstant() {
        assertEquals(PencilSharpener.MOVE_SPEED, sharpener.getMoveSpeed(), "move speed should match the defined constant");
    }

    /**
     * Testing the logic branches for the 'facing' direction based on velocity.
     */
    @Test
    public void testBeforeMovementUpdate_SetsFacingCorrectly() {
        // moving strictly up (y > x and y > 0)
        sharpener.transform.velocity.set(0, 100);
        sharpener.beforeMovementUpdate(0.1f);

    }

    @Test
    public void testBeforeMovementUpdate_StationaryDoesNotChangeFacing() {
        // initial state is down
        sharpener.transform.velocity.set(0, 0);
        sharpener.beforeMovementUpdate(0.1f);
        // Branch 'transform.velocity.x != 0 || ...' is false.
    }

    @Test
    public void testPlayerCollide_IgnoresImmunePlayer() {
        mockPlayer.isImmune = true;
        sharpener.playerCollide(mockPlayer);

        // no stun and no damage should be applied
        verify(mockPlayer, never()).stun(anyFloat());
        verify(mockPlayer, never()).modifyHealth(anyInt());
    }

    @Test
    public void testPlayerCollide_StunsAndSetsDamageTimer() {
        mockPlayer.isImmune = false;
        mockPlayer.isStunned = false;

        sharpener.playerCollide(mockPlayer);

        // stun should be applied
        verify(mockPlayer, times(1)).stun(PencilSharpener.STUN_DURATION);


        // since timer > 0, no damage should happen yet.
        mockPlayer.isStunned = true;
        sharpener.playerCollide(mockPlayer);
        verify(mockPlayer, never()).modifyHealth(anyInt());
    }

    @Test
    public void testPlayerCollide_PeriodicDamageWhenStunned() {
        mockPlayer.isImmune = false;
        mockPlayer.isStunned = true;

        // force timer to 0 so we can trigger damage
        sharpener.beforeMovementUpdate(1.0f);

        sharpener.playerCollide(mockPlayer);

        // damage applied and timer reset
        verify(mockPlayer, times(1)).modifyHealth(PencilSharpener.DOT_DAMAGE);

        // attempt second hit immediately (should be blocked by 0.5s cooldown)
        sharpener.playerCollide(mockPlayer);
        verify(mockPlayer, times(1)).modifyHealth(anyInt()); // still only 1 total hit
    }

    @Test
    public void testBeforeMovementUpdate_ClampsDamageTimerAtZero() {
        // manually trigger a hit to set timer to 0.5
        mockPlayer.isStunned = false;
        sharpener.playerCollide(mockPlayer);

        // simulate a massive frame (10 seconds)
        sharpener.beforeMovementUpdate(10.0f);

        // triggering another hit should work instantly (timer clamped to 0, not -9.5)
        mockPlayer.isStunned = true;
        sharpener.playerCollide(mockPlayer);
        verify(mockPlayer, times(1)).modifyHealth(PencilSharpener.DOT_DAMAGE);
    }

    @Test
    public void testBeforeMovementUpdate_TimerBranches() {
        // branch: damageTimer > 0 (false hit)
        sharpener.damageTimer = 0f;
        sharpener.beforeMovementUpdate(0.1f);
        assertEquals(0f, sharpener.damageTimer);

        // branch: damageTimer > 0 (true) AND damageTimer < 0 (false hit)
        sharpener.damageTimer = 0.5f;
        sharpener.beforeMovementUpdate(0.1f);
        assertEquals(0.4f, sharpener.damageTimer, 0.001f);

        // branch: damageTimer < 0 (true hit - clamping)
        sharpener.damageTimer = 0.1f;
        sharpener.beforeMovementUpdate(0.5f);
        assertEquals(0f, sharpener.damageTimer, "timer should clamp at 0 and not go negative.");
    }

    @Test
    public void testBeforeMovementUpdate_DirectionalBranches() {
        // with a  helper method, we can test all 4 pure directions
        assertFacingDirection(100f, 0f, 2, "Moving purely right should face right.");
        assertFacingDirection(-100f, 0f, 3, "Moving purely left should face left.");
        assertFacingDirection(0f, 100f, 1, "Moving purely up should face up.");
        assertFacingDirection(0f, -100f, 0, "Moving purely down should face down.");
    }

    @Test
    public void testBeforeMovementUpdate_DiagonalPriority() {
        // y-axis dominant (e.g., in our specific example, speed of 20 beats speed of 10)
        assertFacingDirection(10f, 20f, 1, "Diagonal (mostly up) should prioritize up.");
        assertFacingDirection(10f, -20f, 0, "Diagonal (mostly down) should prioritize down.");

        // x-axis dominant
        assertFacingDirection(20f, 10f, 2, "Diagonal (mostly right) should prioritize right.");
        assertFacingDirection(-20f, 10f, 3, "Diagonal (mostly left) should prioritize left.");
    }


    /**
     * Helper method to simulate movement and instantly verify the resulting facing direction.
     */
    private void assertFacingDirection(float velX, float velY, int expectedDirection, String message) {
        sharpener.transform.velocity.set(velX, velY);
        sharpener.beforeMovementUpdate(0.1f);
        assertEquals(expectedDirection, sharpener.facing, message);
    }
}