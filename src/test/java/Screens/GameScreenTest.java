package Screens;

import Game.*;
import Game.AudioManager;
import Screens.GameScreen;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameScreenTest {

    private GdxGame game;
    private GameScreen screen;
    private ScreenManager mockScreenManager;
    private AudioManager mockAudioManager;

    @BeforeEach
    void init() {
        PlayerData.setTestMode();
        PlayerData.reset(false);
        game = mock(GdxGame.class);
        when(game.isMusicPlaying()).thenReturn(true);
        when(game.isSfxPlaying()).thenReturn(true);

        mockScreenManager = mock(ScreenManager.class);
        ScreenManager.setMockInstance(mockScreenManager);

        AudioManager mockAudioManager = mock(AudioManager.class);
        AudioManager.setMockInstance(mockAudioManager);

        screen = GameScreen.createForTesting(game);
        screen.world = mock(GameWorld.class);
        when(screen.world.getId()).thenReturn(1);
        when(screen.world.getScore()).thenReturn(0);
    }

    // Pause and Unpause
    @Test
    void testGamePause() {
        screen.gamePause();
        assertTrue(screen.paused);
    }

    @Test
    void testGameUnpause() {
        screen.gamePause();
        screen.gameUnpause();
        assertFalse(screen.paused);
    }

    // Game End
    @Test
    void testLossSetsGameOver() {
        screen.gameEnd(false);
        assertTrue(screen.gameOver);
        assertFalse(screen.gameWon);
    }

    @Test
    void testWinSetsGameWon() {
        screen.gameEnd(true);
        assertTrue(screen.gameWon);
        assertFalse(screen.gameOver);
    }

    @Test
    void testLossSetsLostLayout() {
        screen.gameEnd(false);
        assertEquals(GameScreen.Layout.LOST, screen.currentLayout);
    }

    @Test
    void testWinSetsWonLayout() {
        screen.gameEnd(true);
        assertEquals(GameScreen.Layout.WON, screen.currentLayout);
    }

    @Test
    void testWinSavesPlayerData() {
        when(screen.world.getId()).thenReturn(1);
        when(screen.world.getScore()).thenReturn(500);
        screen.gameEnd(true);
        PlayerData data = PlayerData.obtainPlayerData();
        assertEquals(2, data.getLevelUnlocked());
        assertEquals(500, data.getScore(1));
    }

    @Test
    void testGameWinImprovesScore() {
        PlayerData.obtainPlayerData().setHighScore(1, 200);
        when(screen.world.getId()).thenReturn(1);
        when(screen.world.getScore()).thenReturn(500);
        screen.gameEnd(true);
        assertEquals(500, PlayerData.obtainPlayerData().getScore(1));
    }

    // Test audio toggle buttons
    @Test
    void testMusicOnButtonCorrect() {
        when(game.isMusicPlaying()).thenReturn(true);
        screen.changeLayout(GameScreen.Layout.SETTINGS);
        assertSame(screen.musicOnButton, screen.settingsButtons[1]);
    }

    @Test
    void testMusicOffButtonCorrect() {
        when(game.isMusicPlaying()).thenReturn(false);
        screen.changeLayout(GameScreen.Layout.SETTINGS);
        assertSame(screen.musicOffButton, screen.settingsButtons[1]);
    }

    @Test
    void testSfxOnButtonCorrect() {
        when(game.isSfxPlaying()).thenReturn(true);
        screen.changeLayout(GameScreen.Layout.SETTINGS);
        assertSame(screen.sfxOnButton, screen.settingsButtons[2]);
    }

    @Test
    void testSfxOffButtonCorrect() {
        when(game.isSfxPlaying()).thenReturn(false);
        screen.changeLayout(GameScreen.Layout.SETTINGS);
        assertSame(screen.sfxOffButton, screen.settingsButtons[2]);
    }

    // activateButton on MAIN layout
    @Test
    void testMainResume() {
        screen.paused = true;
        screen.activateButton(0);
        assertFalse(screen.paused);
    }

    @Test
    void testMainRestartReloadsLevel() {
        screen.activateButton(1);
        verify(mockScreenManager).SetGameScreen(1);
    }

    @Test
    void testMainSettingsLayoutSwitch() {
        screen.activateButton(2);
        assertEquals(GameScreen.Layout.SETTINGS, screen.currentLayout);
    }

    @Test
    void testMainQuitToMenu() {
        screen.activateButton(3);
        verify(mockScreenManager).SetMenuScreen();
    }

    // activateButton on SETTINGS layout
    @Test
    void testSettingsBackButton() {
        screen.changeLayout(GameScreen.Layout.SETTINGS);
        screen.activateButton(0);
        assertEquals(GameScreen.Layout.MAIN, screen.currentLayout);
    }

    @Test
    void testSettingsToggleMusicOff() {
        when(game.isMusicPlaying()).thenReturn(true);
        screen.changeLayout(GameScreen.Layout.SETTINGS);
        screen.activateButton(1);
        verify(AudioManager.getInstance(game)).setMusicEnabled(false);
        assertSame(screen.musicOffButton, screen.settingsButtons[1]);
    }

    @Test
    void testSettingsToggleMusicOn() {
        when(game.isMusicPlaying()).thenReturn(false);
        screen.changeLayout(GameScreen.Layout.SETTINGS);
        screen.activateButton(1);
        verify(AudioManager.getInstance(game)).setMusicEnabled(true);
        assertSame(screen.musicOnButton, screen.settingsButtons[1]);
    }

    @Test
    void testSettingsToggleSfx() {
        when(game.isSfxPlaying()).thenReturn(true);
        screen.changeLayout(GameScreen.Layout.SETTINGS);
        screen.activateButton(2);
        verify(AudioManager.getInstance(game)).setSfxEnabled(false);
        assertSame(screen.sfxOffButton, screen.settingsButtons[2]);
    }

    // activateButton on LOST layout
    @Test
    void testLostRestartReloadsLevel() {
        screen.changeLayout(GameScreen.Layout.LOST);
        screen.activateButton(0);
        verify(mockScreenManager).SetGameScreen(1);
    }

    @Test
    void testLostQuitToMenu() {
        screen.changeLayout(GameScreen.Layout.LOST);
        screen.activateButton(1);
        verify(mockScreenManager).SetMenuScreen();
    }

    // activateButton on WON layout
    @Test
    void testWonNextLevelIfUnlocked() {
        PlayerData.obtainPlayerData().completeLevel(1, 100);
        when(screen.world.getId()).thenReturn(1);

        screen.changeLayout(GameScreen.Layout.WON);
        screen.activateButton(0);
        verify(mockScreenManager).SetGameScreen(2);
    }

    @Test
    void testWonNextLevelIfLocked() {
        // level 2 not unlocked, should go to menu
        // should theoretically be impossible to get here, but just in case
        when(screen.world.getId()).thenReturn(1);

        screen.changeLayout(GameScreen.Layout.WON);
        screen.activateButton(0);
        verify(mockScreenManager).SetMenuScreen();
    }

    @Test
    void testWonQuitToMenu() {
        screen.changeLayout(GameScreen.Layout.WON);
        screen.activateButton(1);
        verify(mockScreenManager).SetMenuScreen();
    }
}
