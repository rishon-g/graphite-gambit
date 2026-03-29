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

import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;
import Game.AudioManager;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PlayerTest {

    private GameWorld mockWorld;
    private Player player;

    // for mocking audio (static mocking for singleton)
    private MockedStatic<AudioManager> mockedAudioManager;
    private AudioManager mockAudio;

    @BeforeAll
    public static void initLibgdxNatives() {
        // we must load native C++ libraries so libGDX graphics don't crash
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
        Gdx.input = mock(com.badlogic.gdx.Input.class);

        // ensures the Player constructor can safely load PencilSheet.png
        when(Gdx.files.internal(anyString())).thenAnswer(invocation -> {
            String path = invocation.getArgument(0);
            return new FileHandle(new File(path));
        });

        // mock audio
        mockAudio = mock(AudioManager.class);
        mockedAudioManager = mockStatic(AudioManager.class);
        mockedAudioManager.when(AudioManager::getInstance).thenReturn(mockAudio);

        // we mock the GameWorld because we only want to test the Player in isolation
        mockWorld = mock(GameWorld.class);

        // initialize the player
        player = new Player(mockWorld);
    }

    @AfterEach
    public void tearDown() {
        // close the static mock after every test to prevent memory leaks and crashes in other tests
        if (mockedAudioManager != null) {
            mockedAudioManager.close();
        }
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
}