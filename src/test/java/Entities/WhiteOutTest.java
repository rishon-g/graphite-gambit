package Entities;

import Game.AudioManager;
import Game.GameWorld;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxNativesLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class WhiteOutTest {

    private GameWorld mockWorld;
    private Player mockPlayer;

    // static mocking for the audio singleton
    private MockedStatic<AudioManager> mockedAudioManager;
    private AudioManager mockAudio;

    @BeforeAll
    public static void initLibgdxNatives() {
        // C++ libraries so LibGDC doesn't crash
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

        // load the actual file
        when(Gdx.files.internal(anyString())).thenAnswer(invocation -> {
            String path = invocation.getArgument(0);
            return new FileHandle(new File(path));
        });

        // mock the AudioManager
        mockAudio = mock(AudioManager.class);
        mockedAudioManager = mockStatic(AudioManager.class);
        mockedAudioManager.when(AudioManager::getInstance).thenReturn(mockAudio);

        // mock the dependencies
        mockWorld = mock(GameWorld.class);
        mockPlayer = mock(Player.class);
    }

    @AfterEach
    public void tearDown() {
        //  close static mocks to prevent memory leaks
        if (mockedAudioManager != null) {
            mockedAudioManager.close();
        }
    }

    @Test
    public void testConstructor_LargeInitialization() {
        // new large whiteout
        WhiteOut largeHazard = new WhiteOut(mockWorld, "Large");

        // check if the hitbox scales correctly
        assertEquals(384f, largeHazard.transform.size.x, "Large whiteout should have an X scale of 384");
        assertEquals(256f, largeHazard.transform.size.y, "Large whiteout should have a Y scale of 256");
    }

    @Test
    public void testConstructor_SmallInitialization() {
        // new small whiteout
        WhiteOut smallHazard = new WhiteOut(mockWorld, "Small");

        // check if the hitbox scales correctly
        assertEquals(256f, smallHazard.transform.size.x, "Small whiteout should have an X scale of 256");
        assertEquals(74f, smallHazard.transform.size.y, "Small whiteout should have a Y scale of 74");
    }

    @Test
    public void testPlayerCollide_DealsDamageAndPlaysAudio() {
        // new large whiteout
        WhiteOut largeHazard = new WhiteOut(mockWorld, "Large");

        // collide with plater
        largeHazard.playerCollide(mockPlayer);

        // verify the hazard called modifyHealth(-25) exactly 1 time
        verify(mockPlayer, times(1)).modifyHealth(-25);
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
        verify(mockPlayer, times(1)).modifyHealth(-15);
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
        verify(mockPlayer, times(1)).modifyHealth(-15);

        // another 0.6 seconds goes by, so now, cooldown drops below zero
        smallHazard.updateInternal(0.6f);

        // attempt third hit, this time it should succeed
        smallHazard.playerCollide(mockPlayer);
        // total of 2 hits registered
        verify(mockPlayer, times(2)).modifyHealth(-15);
    }
}