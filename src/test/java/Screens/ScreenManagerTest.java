package Screens;

import Game.GdxGame;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ScreenManagerTest {
    @BeforeAll
    static void testMode(){
        GdxGame.setTestMode();
    }

    @BeforeEach
    void reset() throws Exception{
        Field instance = ScreenManager.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    void testSingletonIdentity() {
        GdxGame game = mock(GdxGame.class);
        ScreenManager first = ScreenManager.getInstance(game);
        ScreenManager second = ScreenManager.getInstance(game);

        assertSame(first, second);
    }

    @Test
    void testSetMenuScreen() {
        GdxGame game = mock(GdxGame.class);

        ScreenManager.getInstance(game).SetMenuScreen();
        verify(game).setScreen(any());
    }

    @Test
    void testSetGameScreen() {
        GdxGame game = mock(GdxGame.class);

        ScreenManager.getInstance(game).SetGameScreen(1);
        verify(game).setScreen(any());
    }
}