package Game;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AudioManagerTest {

    private GdxGame game;
    private AudioManager am;

    @BeforeAll
    static void init(){
        GdxGame.setTestMode();
    }

    @BeforeEach
    void reset() throws Exception{
        Field instance = AudioManager.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);

        game = mock(GdxGame.class);
        when(game.isMusicPlaying()).thenReturn(true);
        when(game.isSfxPlaying()).thenReturn(true);

        am = AudioManager.getInstance(game);
    }

    @Test
    void testSingletonIdentity() {
        GdxGame game = mock(GdxGame.class);
        AudioManager first = AudioManager.getInstance(game);
        AudioManager second = AudioManager.getInstance(game);

        assertSame(first, second);
    }

    // Toggle Music/SFX
    @Test
    void testMusicEnable() {
        am.setMusicEnabled(true);
        verify(game).setMusicPlaying(true);
    }

    @Test
    void testMusicDisable() {
        am.setMusicEnabled(false);
        verify(game).setMusicPlaying(false);
    }

    @Test
    void testSfxEnable() {
        am.setSfxEnabled(true);
        verify(game).setSfxPlaying(true);
    }

    @Test
    void testSfxDisable() {
        am.setSfxEnabled(false);
        verify(game).setSfxPlaying(false);
    }

    @Test
    void testSfxDisabledStopsMoveSound() {
        am.moveWasPlaying = true;
        am.setSfxEnabled(false);
        assertFalse(am.moveWasPlaying);
    }

    @Test
    void testSfxDisabledStopsSharpenerSound() {
        am.sharpenerWasPlaying = true;
        am.setSfxEnabled(false);
        assertFalse(am.sharpenerWasPlaying);
    }

    // setMusicHalfVolume
    @Test
    void testMusicHalfVolume() {
        am.setMusicHalfVolume();
        assertEquals(am.musicVolumeInit * 0.5f, am.musicVolume);
    }

    @Test
    void testMusicFullVolume() {
        am.setMusicHalfVolume();
        am.setMusicFullVolume();
        assertEquals(am.musicVolumeInit, am.musicVolume);
    }

    // updateMoveSound loop
    @Test
    void testMoveSoundOnMove() {
        am.updateMoveSound(true, false);
        assertTrue(am.moveWasPlaying);
    }

    @Test
    void testMoveSoundStopsOnNoMove() {
        am.updateMoveSound(true, false);
        am.updateMoveSound(false, false);
        assertFalse(am.moveWasPlaying);
    }

    @Test
    void testMoveSoundWhiteout() {
        am.updateMoveSound(true, false);
        assertFalse(am.moveWasSlowed);

        am.updateMoveSound(true, true);
        assertTrue(am.moveWasSlowed);
    }

    @Test
    void testNoMoveSoundWhenSfxDisabled() {
        when(game.isSfxPlaying()).thenReturn(false);
        am.updateMoveSound(true, false);
        assertFalse(am.moveWasPlaying);
    }

    @Test
    void testNoMoveSoundWhenSfxDisabledDuringMove() {
        am.updateMoveSound(true, false);
        assertTrue(am.moveWasPlaying);

        when(game.isSfxPlaying()).thenReturn(false);
        am.updateMoveSound(true, false);
        assertFalse(am.moveWasPlaying);
    }

    // stopMoveSound
    @Test
    void testStopMoveSound() {
        am.moveWasPlaying = true;
        am.stopMoveSound();
        assertFalse(am.moveWasPlaying);
        assertEquals(-1, am.moveSoundId);
    }

    // updateSharpenerSound loop
    @Test
    void testSharpenerSoundWhenStunned() {
        am.updateSharpenerSound(true);
        assertTrue(am.sharpenerWasPlaying);
    }

    @Test
    void testSharpenerSoundStopsWhenUnstunned() {
        am.updateSharpenerSound(true);
        am.updateSharpenerSound(false);
        assertFalse(am.sharpenerWasPlaying);
    }

    @Test
    void testNoSharpenerSoundWhenSfxDisabled() {
        when(game.isSfxPlaying()).thenReturn(false);
        am.updateSharpenerSound(true);
        assertFalse(am.sharpenerWasPlaying);
    }

    @Test
    void testNoSharpenerSoundWhenSfxDisabledDuringStun() {
        am.updateSharpenerSound(true);
        assertTrue(am.sharpenerWasPlaying);

        when(game.isSfxPlaying()).thenReturn(false);
        am.updateSharpenerSound(true);
        assertFalse(am.sharpenerWasPlaying);
    }

    // stopSharpenerSound
    @Test
    void testStopSharpenerSound() {
        am.sharpenerWasPlaying = true;
        am.stopSharpenerSound();
        assertFalse(am.sharpenerWasPlaying);
        assertEquals(-1, am.sharpenerSoundId);
    }
}
