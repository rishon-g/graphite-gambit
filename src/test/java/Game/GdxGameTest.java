package Game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GdxGameTest {

    @Test
    public void testAudioStateFlags_DefaultsAndSetters() {
        // instantiate the game shell (without calling create() so graphics don't crash)
        GdxGame game = new GdxGame();

        // audio flags should default to true
        assertTrue(game.isMusicPlaying(), "music should be enabled by default");
        assertTrue(game.isSfxPlaying(), "sfx should be enabled by default");

        // say the user goes to the settings menu and turns them off
        game.setMusicPlaying(false);
        game.setSfxPlaying(false);

        // the flags should successfully update
        assertFalse(game.isMusicPlaying(), "music should be disabled after setting to false.");
        assertFalse(game.isSfxPlaying(), "sfx should be disabled after setting to false.");
    }
}