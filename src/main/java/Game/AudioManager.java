package Game;

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

    // Volume variables
    private final float musicVolumeInit = 0.1f;
    private float musicVolume = 0.1f;
    private float moveVolume = 0.4f;
    private float puddleVolume = 1f;
    private float sharpenerVolume = 0.8f;
    private float damageVolume = 1f;
    private float scoreVolume = 0.5f;
    private float hoverVolume = 0.2f;
    private float clickVolume = 0.5f;

    // track looping state
    private long moveSoundId = -1;
    private boolean moveWasPlaying = false;
    private boolean moveWasSlowed = false;

    // sharpener looping state
    private long sharpenerSoundId = -1;
    private boolean sharpenerWasPlaying = false;

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
        gameMusic  = Gdx.audio.newMusic(Gdx.files.internal("audio/music.mp3"));
        gameMusic.setLooping(true);
        gameMusic.setVolume(musicVolume);

        moveSound   = Gdx.audio.newSound(Gdx.files.internal("audio/sfx_move.mp3"));
        puddleSound  = Gdx.audio.newSound(Gdx.files.internal("audio/sfx_puddle.mp3"));
        sharpenerSound  = Gdx.audio.newSound(Gdx.files.internal("audio/sfx_sharpener.mp3"));
        damageSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx_damage.mp3"));
        scoreSound  = Gdx.audio.newSound(Gdx.files.internal("audio/sfx_score.mp3"));
        hoverSound  = Gdx.audio.newSound(Gdx.files.internal("audio/ui_hover.mp3"));
        clickSound  = Gdx.audio.newSound(Gdx.files.internal("audio/ui_click.mp3"));
    }

    /**
     * Starts the music track if it is enabled and not already playing
     */
    public void startMusic() {
        if (game.isMusicPlaying() && !gameMusic.isPlaying()) {
            gameMusic.play();
        }
    }

    /**
     * Reduces music volume for main menu and pause menus
     */
    public void setMusicHalfVolume() {
        musicVolume = musicVolumeInit * 0.5f;
        gameMusic.setVolume(musicVolume);
    }

    /**
     * Return music to full volume
     */
    public void setMusicFullVolume() {
        musicVolume = musicVolumeInit;
        gameMusic.setVolume(musicVolume);
    }

    public void stopMusic() {
        gameMusic.stop();
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
                stopMoveSound(); // stop whichever sound is active
                if (slowed) {
                    moveSoundId = puddleSound.loop(puddleVolume);
                } else {
                    moveSoundId = moveSound.loop(moveVolume);
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
        if (moveSoundId != -1) {
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
    public void updateSharpenerSound(boolean isStunned) {
        if (!game.isSfxPlaying()) {
            stopSharpenerSound();
            return;
        }
        if (isStunned && !sharpenerWasPlaying) {
            playScore();
            sharpenerSoundId = sharpenerSound.loop(sharpenerVolume);
            sharpenerWasPlaying = true;
        } else if (!isStunned && sharpenerWasPlaying) {
            stopSharpenerSound();
        }
    }

    /**
     * Triggers when player exits sharpener
     */
    public void stopSharpenerSound() {
        if (sharpenerSoundId != -1) {
            sharpenerSound.stop(sharpenerSoundId);
            sharpenerSoundId = -1;
        }
        sharpenerWasPlaying = false;
    }

    public void playDamage() {
        if (game.isSfxPlaying()) {
            damageSound.play(damageVolume);
        }
    }

    public void playScore() {
        if (game.isSfxPlaying()) {
            scoreSound.play(scoreVolume);
        }
    }

    public void playHover() {
        if (game.isSfxPlaying()) {
            hoverSound.play(hoverVolume);
        }
    }

    public void playClick() {
        if (game.isSfxPlaying()) {
            clickSound.play(clickVolume);
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
        gameMusic.dispose();
        moveSound.dispose();
        puddleSound.dispose();
        sharpenerSound.dispose();
        damageSound.dispose();
        scoreSound.dispose();
        instance = null;
    }
}