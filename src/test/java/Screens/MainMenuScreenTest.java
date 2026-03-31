package Screens;

import Game.AudioManager;
import Game.GdxGame;
import Game.PlayerData;
import Screens.MainMenuScreen;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MainMenuScreenTest {

    private GdxGame game;
    private MainMenuScreen screen;
    private ScreenManager mockScreenManager;
    private AudioManager mockAudioManager;

    @BeforeAll
    static void testMode(){
        GdxGame.setTestMode();
        GdxGame.setTestMode();
    }

    @BeforeEach
    void init() throws Exception{
        Field instance = ScreenManager.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);

        PlayerData.reset(false);
        game = mock(GdxGame.class);
        when(game.isMusicPlaying()).thenReturn(true);
        when(game.isSfxPlaying()).thenReturn(true);

        mockScreenManager = mock(ScreenManager.class);
        ScreenManager.setMockInstance(mockScreenManager);
        screen = new MainMenuScreen(game);

        mockAudioManager = mock(AudioManager.class);
        AudioManager.setMockInstance(mockAudioManager);
    }

    // Test all layout changes
    @Test
    void testChangeToLevelSelect() {
        screen.changeLayout(MainMenuScreen.Layout.LEVEL_SELECT);
        assertEquals(MainMenuScreen.Layout.LEVEL_SELECT, screen.currentLayout);
    }

    @Test
    void testChangeToSettings() {
        screen.changeLayout(MainMenuScreen.Layout.SETTINGS);
        assertEquals(MainMenuScreen.Layout.SETTINGS, screen.currentLayout);
    }

    @Test
    void testChangeToHowToPlay() {
        screen.changeLayout(MainMenuScreen.Layout.HOW_TO_PLAY);
        assertEquals(MainMenuScreen.Layout.HOW_TO_PLAY, screen.currentLayout);
    }

    @Test
    void testChangeToBackToMainMenu() {
        screen.changeLayout(MainMenuScreen.Layout.LEVEL_SELECT);
        screen.changeLayout(MainMenuScreen.Layout.MAIN);
        assertEquals(MainMenuScreen.Layout.MAIN, screen.currentLayout);
    }

    // Test audio toggle buttons
    @Test
    void testMusicOnButtonCorrect() {
        when(game.isMusicPlaying()).thenReturn(true);
        screen.changeLayout(MainMenuScreen.Layout.SETTINGS);
        assertSame(screen.musicOnButton, screen.settingsButtons[1]);
    }

    @Test
    void testMusicOffButtonCorrect() {
        when(game.isMusicPlaying()).thenReturn(false);
        screen.changeLayout(MainMenuScreen.Layout.SETTINGS);
        assertSame(screen.musicOffButton, screen.settingsButtons[1]);
    }

    @Test
    void testSfxOnButtonCorrect() {
        when(game.isSfxPlaying()).thenReturn(true);
        screen.changeLayout(MainMenuScreen.Layout.SETTINGS);
        assertSame(screen.sfxOnButton, screen.settingsButtons[2]);
    }

    @Test
    void testSfxOffButtonCorrect() {
        when(game.isSfxPlaying()).thenReturn(false);
        screen.changeLayout(MainMenuScreen.Layout.SETTINGS);
        assertSame(screen.sfxOffButton, screen.settingsButtons[2]);
    }

    // activateButton on main layout
    @Test
    void testMainStartGame() {
        screen.activateButton(0);
        verify(ScreenManager.getInstance(game)).SetGameScreen(1);
    }

    @Test
    void testMainLevelSelectLayoutSwitch() {
        screen.activateButton(1);
        assertEquals(MainMenuScreen.Layout.LEVEL_SELECT, screen.currentLayout);
    }

    @Test
    void testMainHowToPlayLayoutSwitch() {
        screen.activateButton(2);
        assertEquals(MainMenuScreen.Layout.HOW_TO_PLAY, screen.currentLayout);
    }

    @Test
    void testMainSettingsLayoutSwitch() {
        screen.activateButton(3);
        assertEquals(MainMenuScreen.Layout.SETTINGS, screen.currentLayout);
    }

    // activateButton on level select layout
    @Test
    void testLevelSelectBackButton() {
        screen.changeLayout(MainMenuScreen.Layout.LEVEL_SELECT);
        screen.activateButton(0);
        assertEquals(MainMenuScreen.Layout.MAIN, screen.currentLayout);
    }

    @Test
    void testLevelSelectLaunchesCorrectLevel() {
        PlayerData.obtainPlayerData().setLevel(4);
        screen = new MainMenuScreen(game);
        screen.changeLayout(MainMenuScreen.Layout.LEVEL_SELECT);

        screen.activateButton(2);
        verify(mockScreenManager).SetGameScreen(2);

        screen.activateButton(3);
        verify(mockScreenManager).SetGameScreen(3);

        screen.activateButton(4);
        verify(mockScreenManager).SetGameScreen(4);
    }

    // activateButton on settings layout
    @Test
    void testSettingsBackButton() {
        screen.changeLayout(MainMenuScreen.Layout.SETTINGS);
        screen.activateButton(0);
        assertEquals(MainMenuScreen.Layout.MAIN, screen.currentLayout);
    }

    @Test
    void testSettingsToggleMusicOff() {
        when(game.isMusicPlaying()).thenReturn(true);
        screen.changeLayout(MainMenuScreen.Layout.SETTINGS);
        screen.activateButton(1);
        verify(AudioManager.getInstance(game)).setMusicEnabled(false);
        assertSame(screen.musicOffButton, screen.settingsButtons[1]);
    }

    @Test
    void testSettingsToggleMusicOn() {
        when(game.isMusicPlaying()).thenReturn(false);
        screen.changeLayout(MainMenuScreen.Layout.SETTINGS);
        screen.activateButton(1);
        verify(AudioManager.getInstance(game)).setMusicEnabled(true);
        assertSame(screen.musicOnButton, screen.settingsButtons[1]);
    }

    @Test
    void testSettingsToggleSfx() {
        when(game.isSfxPlaying()).thenReturn(true);
        screen.changeLayout(MainMenuScreen.Layout.SETTINGS);
        screen.activateButton(2);
        verify(AudioManager.getInstance(game)).setSfxEnabled(false);
        assertSame(screen.sfxOffButton, screen.settingsButtons[2]);
    }

    // playerdata integration
    @Test
    void testEmptySaveLevelSelect() {
        MenuButton[] levelButtons = screen.levelSelectButtons;
        assertFalse(levelButtons[1].isDisabled());
        assertTrue(levelButtons[2].isDisabled());
        assertTrue(levelButtons[3].isDisabled());
        assertTrue(levelButtons[4].isDisabled());
    }

    @Test
    void testUnlockedLevelShowsEnabled() {
        PlayerData.obtainPlayerData().setLevel(3);
        screen = new MainMenuScreen(game);
        assertFalse(screen.levelSelectButtons[2].isDisabled());
        assertFalse(screen.levelSelectButtons[3].isDisabled());
        assertTrue(screen.levelSelectButtons[4].isDisabled());
    }

    @Test
    void testScoreDisplayedOnButton() {
        PlayerData data = PlayerData.obtainPlayerData();
        data.setLevel(2);
        data.setHighScore(1, 500);
        screen = new MainMenuScreen(game);
        assertEquals("LEVEL 1    Score: 500", screen.levelSelectButtons[1].getLabel());
    }

    @Test
    void testNoScoreDefaultsZero() {
        PlayerData data = PlayerData.obtainPlayerData();
        data.setLevel(2);
        screen = new MainMenuScreen(game);
        assertEquals("LEVEL 1    Score: 0", screen.levelSelectButtons[1].getLabel());
    }
}