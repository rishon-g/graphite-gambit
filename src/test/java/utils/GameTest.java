package utils;

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

import java.io.File;

// we use mockito because JUnit only runs in a standard java environment with no OpenGL graphics
// so, we mock the gdx static environment
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;

/**
 * Base test class that automatically mocks the libGDX environment
 * and static singletons for all game tests.
 */
public abstract class GameTest {

    // protected so child classes can use them if they need to
    protected GameWorld mockWorld;
    protected AudioManager mockAudio;
    private MockedStatic<AudioManager> mockedAudioManager;

    @BeforeAll
    public static void initLibgdxNatives() {
        // load native C++ libraries so LibGDX won't crash (required with mockito)
        GdxNativesLoader.load();
    }

    @BeforeEach
    public void baseSetUp() {
        // mock the libGDX environment
        Gdx.gl = mock(GL20.class);
        Gdx.gl20 = mock(GL20.class);
        Gdx.graphics = mock(Graphics.class);
        Gdx.app = mock(Application.class);
        Gdx.input = mock(Input.class);
        Gdx.files = mock(com.badlogic.gdx.Files.class);

        // this tells the mocked file system to return a real FileHandle for any path requested,
        // as a result Texture loading won't crash.
        when(Gdx.files.internal(anyString())).thenAnswer(invocation -> {
            String path = invocation.getArgument(0);
            return new FileHandle(new File(path));
        });

        // mock the static AudioManager
        mockAudio = mock(AudioManager.class);
        mockedAudioManager = mockStatic(AudioManager.class);
        mockedAudioManager.when(AudioManager::getInstance).thenReturn(mockAudio);

        // provide a default GameWorld mock for child classes
        mockWorld = mock(GameWorld.class);
    }

    @AfterEach
    public void baseTearDown() {
        //  clean up static mocks to prevent memory leaks
        if (mockedAudioManager != null) {
            mockedAudioManager.close();
        }
    }
}