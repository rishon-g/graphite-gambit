package Entities;

import utils.GameTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class WhiteOutTest extends GameTest {

    private Player mockPlayer;


    @BeforeEach
    public void setUp() {
        // mock the dependencies
        mockPlayer = mock(Player.class);
    }

    @Test
    public void testConstructor_LargeInitialization() {
        // new large whiteout
        WhiteOut largeHazard = new WhiteOut(mockWorld, "Large");

        // check if the hitbox scales correctly
        assertEquals(WhiteOut.LARGE_DEFAULT_WIDTH, largeHazard.transform.size.x, "Wrong large whiteout X Scale" );
        assertEquals(WhiteOut.LARGE_DEFAULT_HEIGHT, largeHazard.transform.size.y, "Wrong large whiteout Y Scale");
    }

    @Test
    public void testConstructor_SmallInitialization() {
        // new small whiteout
        WhiteOut smallHazard = new WhiteOut(mockWorld, "Small");

        // check if the hitbox scales correctly
        assertEquals(WhiteOut.SMALL_DEFAULT_WIDTH, smallHazard.transform.size.x, "Wrong small whiteout X Scale" );
        assertEquals(WhiteOut.SMALL_DEFAULT_HEIGHT, smallHazard.transform.size.y, "Wrong small whiteout Y scale");
    }

    @Test
    public void testPlayerCollide_DealsDamageAndPlaysAudio() {
        // new whiteout
        WhiteOut largeHazard = new WhiteOut(mockWorld, "Large");

        // collide with plater
        largeHazard.playerCollide(mockPlayer);

        // verify the hazard called modifyHealth exactly 1 time
        verify(mockPlayer, times(1)).modifyHealth(WhiteOut.LARGE_DEFAULT_DAMAGE);
        // verify the hazard triggered the damage audio exactly 1 time
        verify(mockAudio, times(1)).playDamage();
    }

    @Test
    public void testPlayerCollide_RespectsCooldownTimer() {
        WhiteOut smallHazard = new WhiteOut(mockWorld, "Small");

        // collision 1
        smallHazard.playerCollide(mockPlayer);

        // collision 2 (Should be prevented by cooldown)
        smallHazard.playerCollide(mockPlayer);

        // the player should only have taken damage once, despite two collisions
        verify(mockPlayer, times(1)).modifyHealth(WhiteOut.SMALL_DEFAULT_DAMAGE);
    }

    @Test
    public void testUpdateInternal_ReducesCooldownAndAllowsReHit() {
        WhiteOut smallHazard = new WhiteOut(mockWorld, "Small");

        // initial hit, which triggers 1.0s cooldown
        smallHazard.playerCollide(mockPlayer);

        // 0.5 seconds goes by
        smallHazard.updateInternal(0.5f);

        // attempt second hit (should fail b/c full second hasn't passed)
        smallHazard.playerCollide(mockPlayer);
        // still total of 1 hit registered
        verify(mockPlayer, times(1)).modifyHealth(WhiteOut.SMALL_DEFAULT_DAMAGE);

        // another 0.6 seconds goes by, so now, cooldown drops below zero
        smallHazard.updateInternal(0.6f);

        // attempt third hit, this time it should succeed
        smallHazard.playerCollide(mockPlayer);
        // total of 2 hits registered
        verify(mockPlayer, times(2)).modifyHealth(WhiteOut.SMALL_DEFAULT_DAMAGE);
    }

    @Test
    public void testUpdateInternal_IdleStateIgnoresCooldown() {
        // create a fresh hazard. its internal damageCooldown starts at 0
        WhiteOut smallHazard = new WhiteOut(mockWorld, "Small");

        // simulate some time passing
        // because cooldown is 0, the 'if (damageCooldown > 0)' evaluates to false (branch hit)
        smallHazard.updateInternal(0.5f);

        // to ensure the timer didn't accidentally drop into the negatives (e.g., -0.5f),
        // we hit the player. if the timer is cleanly at 0, the player will take damage immediately
        smallHazard.playerCollide(mockPlayer);

        // verify the player took exactly 1 hit of damage
        verify(mockPlayer, times(1)).modifyHealth(WhiteOut.SMALL_DEFAULT_DAMAGE);
    }
}