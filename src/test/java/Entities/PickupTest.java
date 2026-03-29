package Entities;

import Game.AudioManager;
import Game.GameWorld;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxNativesLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PickupTest {

    private GameWorld mockWorld;
    // we use a spy to isolate pickup from Player's logic
    // for example, if Player has a bug with gaining health, it would tell you Pickup is broken when
    // it actually boils down to Player. So, we use a "spy" to verify method calls of Pickup

    private Player spiedPlayer;

    // static mocking for the audio singleton
    private MockedStatic<AudioManager> mockedAudioManager;
    private AudioManager mockAudio;

    @BeforeAll
    public static void initLibgdxNatives() {
        GdxNativesLoader.load();
    }

    @BeforeEach
    public void setUp() {
        // mock the libGDX environment
        Gdx.gl = mock(GL20.class);
        Gdx.gl20 = mock(GL20.class);
        Gdx.graphics = mock(Graphics.class);
        Gdx.app = mock(Application.class);
        Gdx.files = mock(com.badlogic.gdx.Files.class);
        Gdx.input = mock(Input.class);

        // ensure textures can load properly without crashing
        when(Gdx.files.internal(anyString())).thenAnswer(invocation -> {
            String path = invocation.getArgument(0);
            return new FileHandle(new File(path));
        });

        // mock the AudioManager so Player update loops don't crash
        mockAudio = mock(AudioManager.class);
        mockedAudioManager = mockStatic(AudioManager.class);
        mockedAudioManager.when(AudioManager::getInstance).thenReturn(mockAudio);

        // set up the GameWorld
        mockWorld = mock(GameWorld.class);

        // create a real player, but spy on it (again, so we can verify method calls)
        Player realPlayer = new Player(mockWorld);
        spiedPlayer = spy(realPlayer);

        // tell the mocked world to return our spied player
        when(mockWorld.getPlayer()).thenReturn(spiedPlayer);
    }

    @AfterEach
    public void tearDown() {
        if (mockedAudioManager != null) {
            mockedAudioManager.close();
        }
    }

    @Test
    public void testConstructor_InitializesHitbox() {
        Pickup pickup = new Pickup(mockWorld);

        // ensure the math and centering stays perfect
        assertEquals(Pickup.DEFAULT_WIDTH, pickup.transform.size.x, "pickup should have an x scale of 64.");
        assertEquals(Pickup.DEFAULT_HEIGHT, pickup.transform.size.y, "pickup should have a y scale of 64.");
    }

    @Test
    public void testUpdate_HoverAnimationAltersYPosition() {
        // Arrange
        Pickup pickup = new Pickup(mockWorld);
        pickup.transform.position.set(100f, 100f);

        // move the player far away so they don't accidentally collide and destroy the pickup
        spiedPlayer.transform.position.set(5000f, 5000f);

        // simulate 0.5 seconds of game time
        pickup.updateInternal(0.5f);

        // after 0.5s, Math.sin(0.5 * 5) is positive, meaning the Y position should be greater than the baseline 100
        assertTrue(pickup.transform.position.y > 100f, "Pickup's Y position should oscillate upwards");
        assertFalse(pickup.dead, "pickup should remain alive since it was not touched");
    }

    @Test
    public void testUpdate_CollisionHealsPlayerAndDestroysPickup() {
        Pickup pickup = new Pickup(mockWorld);
        pickup.transform.position.set(100f, 100f);

        // force the player to stand directly on top of the pickup
        spiedPlayer.transform.position.set(100f, 100f);

        // simulate a single frame
        pickup.updateInternal(0.1f);

        // verify the pickup communicated with the player to heal graphite
        verify(spiedPlayer, times(1)).modifyHealth(Pickup.HEAL_AMOUNT);

        // verify the pickup successfully flagged itself for deletion
        assertTrue(pickup.dead, "pickup should be marked as dead after collection.");
    }

    @Test
    public void testUpdate_NoCollisionIgnoresPlayer() {
        Pickup pickup = new Pickup(mockWorld);
        pickup.transform.position.set(100f, 100f);

        // move the player safely out of range
        spiedPlayer.transform.position.set(900f, 900f);

        //  simulate a single frame
        pickup.updateInternal(0.1f);

        // ensure the modifyHealth method was never called with any integer
        verify(spiedPlayer, never()).modifyHealth(anyInt());
        assertFalse(pickup.dead, "pickup should not flag itself as dead if the player misses it");
    }

    @Test
    public void testUpdate_BaseYLocksAfterFirstFrame() {
        Pickup pickup = new Pickup(mockWorld);
        pickup.transform.position.set(100f, 100f);

        // move the player far away so they don't accidentally collide
        spiedPlayer.transform.position.set(5000f, 5000f);

        // frame 1: The 'if (baseY == -1f)' statement is true, so baseY is locked to 100f
        pickup.updateInternal(0.1f);

        //  frame 2: The 'if (baseY == -1f)' statement is false (branch satisfied)
        pickup.updateInternal(0.1f);

        // verify that the math is still calculating based on the original locked 100f height
        // total time is now 0.2s
        // Math: 100 + sin(0.2 * 5) * 10
        float expectedY = 100f + (float) Math.sin(0.2f * 5f) * 10f;
        assertEquals(expectedY, pickup.transform.position.y, 0.001f, "pickup oscillating incorrectly");
    }
}