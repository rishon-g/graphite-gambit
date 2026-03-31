package Screens;

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

        screen = new MainMenuScreen(game);
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
}
