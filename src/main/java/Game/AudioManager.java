package Game;

import Screens.ScreenManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

/**
 * Singleton that manages all game audio
 * Each audio track requires its own Sound object and methods
 *
 * @author Luke McRae
 * @version 1.2
 * @since 2026-03-16
 */
public class AudioManager implements Disposable {

    private static AudioManager instance;
    private final GdxGame game;

    // Sounds
    private Music gameMusic;
    private Sound moveSound;
    private Sound puddleSound;
    private Sound sharpenerSound;
    private Sound damageSound;
    private Sound scoreSound;
    private Sound hoverSound;
    private Sound clickSound;
    private Sound lineSound;
    private Sound doorSound;
    private Sound winSound;

    // Volume variables
    final float musicVolumeInit = 0.1f;
    float musicVolume = 0.1f;
    private float moveVolume = 0.4f;
    private float puddleVolume = 1f;
    private float sharpenerVolume = 0.8f;
    private float damageVolume = 1f;
    private float scoreVolume = 0.5f;
    private float hoverVolume = 0.2f;
    private float clickVolume = 0.5f;
    private float lineVolume = 1f;
    private float doorVolume = 2f;
    private float winVolume = 1f;

    // track looping state
    long moveSoundId = -1;
    boolean moveWasPlaying = false;
    boolean moveWasSlowed = false;

    // sharpener looping state
    long sharpenerSoundId = -1;
    boolean sharpenerWasPlaying = false;

    /**
     * Singleton constructor.
     * @param game object of GdxGame
     */
    private AudioManager(GdxGame game) {
        this.game = game;
        load();
    }

    /**
     * Singleton getter
     * @param game object of GdxGame
     * @return instance of AudioManager
     */
    public static AudioManager getInstance(GdxGame game) {
        if (instance == null) {
            instance = new AudioManager(game);
        }
        return instance;
    }

    /**
     * Getter for instance that can be used after initialization without needing game object
     * @return instance of AudioManager
     */
    public static AudioManager getInstance() {
        return instance;
    }

    /**
     * Loads all audio files into Sound objects
     */
    private void load() {
        if (!GdxGame.isTestMode()) {
            gameMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/music.mp3"));
            gameMusic.setLooping(true);
            gameMusic.setVolume(musicVolume);

            moveSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx_move.mp3"));
            puddleSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx_puddle.mp3"));
            sharpenerSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx_sharpener.mp3"));
            damageSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx_damage.mp3"));
            scoreSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx_score.mp3"));
            hoverSound = Gdx.audio.newSound(Gdx.files.internal("audio/ui_hover.mp3"));
            clickSound = Gdx.audio.newSound(Gdx.files.internal("audio/ui_click.mp3"));
            lineSound = Gdx.audio.newSound(Gdx.files.internal("audio/linedraw.mp3"));
            doorSound = Gdx.audio.newSound(Gdx.files.internal("audio/door.wav"));
            winSound = Gdx.audio.newSound(Gdx.files.internal("audio/win.wav"));
        }
    }

    /**
     * Set mock instance for testing purposes
     * @param mock the mock instance
     */
    public static void setMockInstance(AudioManager mock){
        instance = mock;
    }

    /**
     * Starts the music track if it is enabled and not already playing
     */
    public void startMusic() {
        if (gameMusic == null) return;
        if (game.isMusicPlaying() && !gameMusic.isPlaying()) {
            gameMusic.play();
        }
    }

    /**
     * Reduces music volume for main menu and pause menus
     */
    public void setMusicHalfVolume() {
        musicVolume = musicVolumeInit * 0.5f;
        if (gameMusic != null) gameMusic.setVolume(musicVolume);
    }

    /**
     * Return music to full volume
     */
    public void setMusicFullVolume() {
        musicVolume = musicVolumeInit;
        if (gameMusic != null) gameMusic.setVolume(musicVolume);
    }

    public void stopMusic() {
        if (gameMusic != null) gameMusic.stop();
    }

    /**
     * Toggle music based on the game settings
     * @param enabled true to enable music, false to disable
     */
    public void setMusicEnabled(boolean enabled) {
        game.setMusicPlaying(enabled);
        if (enabled) {
            startMusic();
        } else {
            stopMusic();
        }
    }

    // SFX controls
    /**
     * Call every frame from Player.updateInternal().
     * @param isMoving true while any movement key is held
     */
    public void updateMoveSound(boolean isMoving, boolean slowed) {
        if (!game.isSfxPlaying()) {
            stopMoveSound();
            return;
        }
        if (isMoving) {
            // Start sound, or swap if slowed state changed
            if (!moveWasPlaying || slowed != moveWasSlowed) {
                if (moveSound != null && puddleSound != null) {
                    stopMoveSound(); // stop whichever sound is active
                    if (slowed) {
                        moveSoundId = puddleSound.loop(puddleVolume);
                    } else {
                        moveSoundId = moveSound.loop(moveVolume);
                    }
                }
                moveWasPlaying = true;
                moveWasSlowed = slowed;
            }
        } else if (moveWasPlaying) {
            stopMoveSound();
        }
    }

    /**
     * Triggered when the player stops moving. Stops the sound and resets the state.
     */
    public void stopMoveSound() {
        if (moveSoundId != -1 && moveSound != null && puddleSound != null) {
            moveSound.stop(moveSoundId);
            puddleSound.stop(moveSoundId);
            moveSoundId = -1;
        }
        moveWasPlaying = false;
        moveWasSlowed = false;
    }

    /**
     * Call every frame from Player.updateInternal().
     * @param isStunned true while the player is stunned by a sharpener
     */
    //TODO make one class for move and sharpener
    public void updateSharpenerSound(boolean isStunned) {
        if (!game.isSfxPlaying()) {
            stopSharpenerSound();
            return;
        }
        if (isStunned && !sharpenerWasPlaying) {
            if (sharpenerSound != null) {
                sharpenerSoundId = sharpenerSound.loop(sharpenerVolume);
            }
            sharpenerWasPlaying = true;
        } else if (!isStunned && sharpenerWasPlaying) {
            stopSharpenerSound();
        }
    }

    /**
     * Triggers when player exits sharpener
     */
    public void stopSharpenerSound() {
        if (sharpenerSoundId != -1 && sharpenerSound != null) {
            sharpenerSound.stop(sharpenerSoundId);
            sharpenerSoundId = -1;
        }
        sharpenerWasPlaying = false;
    }

    public void playDamage() {
        if (game.isSfxPlaying() && damageSound != null) {
            damageSound.play(damageVolume);
        }
    }

    public void playScore() {
        if (game.isSfxPlaying() && scoreSound != null) {
            scoreSound.play(scoreVolume);
        }
    }

    public void playHover() {
        if (game.isSfxPlaying() && hoverSound != null) {
            hoverSound.play(hoverVolume);
        }
    }

    public void playClick() {
        if (game.isSfxPlaying() && clickSound != null) {
            clickSound.play(clickVolume);
        }
    }

    public void playLineDrawSound() {
        if (game.isSfxPlaying() && lineSound != null) {
            lineSound.play(lineVolume);
        }
    }

    public void playDoorOpenSound() {
        if (game.isSfxPlaying() && doorSound != null) {
            doorSound.play(doorVolume);
        }
    }

    public void playWinSound() {
        if (game.isSfxPlaying() && winSound != null) {
            winSound.play(winVolume);
        }
    }

    /**
     * Toggles SFX based on the game settings
     * @param enabled true to enable SFX, false to disable
     */
    public void setSfxEnabled(boolean enabled) {
        game.setSfxPlaying(enabled);
        if (!enabled) {
            stopMoveSound();
            stopSharpenerSound();
        }
    }

    @Override
    public void dispose() {
        if (gameMusic != null) gameMusic.dispose();
        if (moveSound != null) moveSound.dispose();
        if (puddleSound != null) puddleSound.dispose();
        if (sharpenerSound != null) sharpenerSound.dispose();
        if (damageSound != null) damageSound.dispose();
        if (scoreSound != null) scoreSound.dispose();
        if (hoverSound != null) hoverSound.dispose();
        if (clickSound != null) clickSound.dispose();
        if (lineSound != null) lineSound.dispose();
        if (doorSound != null) doorSound.dispose();
        instance = null;
    }
}