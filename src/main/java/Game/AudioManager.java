package Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

/**
 * Singleton that manages all game audio
 * Each audio track requires its own Sound object and methods
 */
public class AudioManager implements Disposable {

    private static AudioManager instance;
    private final GdxGame game;

    // Sounds
    private Music gameMusic;
    private Sound moveSound;
    private Sound damageSound;
    private Sound scoreSound;
    private Sound hoverSound;
    private Sound clickSound;

    // Volume variables
    private final float musicVolumeInit = 0.1f;
    private float musicVolume = 0.1f;
    private float moveVolume = 0.4f;
    private float damageVolume = 1f;
    private float scoreVolume = 0.5f;
    private float hoverVolume = 0.2f;
    private float clickVolume = 0.5f;

    // track looping state
    private long moveSoundId = -1;
    private boolean moveWasPlaying = false;

    /**
     * Constructor for the audio manager object.
     * 
     * @param game the game object the manager belongs to
     */
    private AudioManager(GdxGame game) {
        this.game = game;
        load();
    }

    /**
     * Gets a singleton instance of the audio manager. If none exist, create a new
     * one with the provided game object.
     * 
     * @param game the game object the manager belongs to
     * @return the instance of the audio manager
     */
    public static AudioManager getInstance(GdxGame game) {
        if (instance == null) {
            instance = new AudioManager(game);
        }
        return instance;
    }

    /**
     * Getter for the audio manager, without initialization.
     * 
     * @return the instance of the audio manager
     */
    public static AudioManager getInstance() {
        return instance;
    }

    /**
     * Loads the audio for the game from their file locations. called upon
     * initialization.
     */
    private void load() {
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/music.mp3"));
        gameMusic.setLooping(true);
        gameMusic.setVolume(musicVolume);

        moveSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx_move.mp3"));
        damageSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx_damage.mp3"));
        scoreSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx_score.mp3"));
        hoverSound = Gdx.audio.newSound(Gdx.files.internal("audio/ui_hover.mp3"));
        clickSound = Gdx.audio.newSound(Gdx.files.internal("audio/ui_click.mp3"));
    }

    /**
     * Starts the game music if it is not curently playing.
     */
    public void startMusic() {
        if (game.isMusicPlaying() && !gameMusic.isPlaying()) {
            gameMusic.play();
        }
    }

    /**
     * Sets the current volume to half of it's initialized value.
     */
    public void setMusicHalfVolume() {
        musicVolume = musicVolumeInit * 0.5f;
        gameMusic.setVolume(musicVolume);
    }

    /**
     * Sets the current volume to it's initialized value.
     */
    public void setMusicFullVolume() {
        musicVolume = musicVolumeInit;
        gameMusic.setVolume(musicVolume);
    }

    /**
     * Stops the current music playing.
     */
    public void stopMusic() {
        gameMusic.stop();
    }

    /**
     * Enables or disables music playback. If enabled, starts the music; if disabled, stops it.
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
     * 
     * @param isMoving true while any movement key is held
     */
    public void updateMoveSound(boolean isMoving) {
        if (!game.isSfxPlaying()) {
            stopMoveSound();
            return;
        }
        if (isMoving && !moveWasPlaying) {
            // loop the pencil sound
            moveSoundId = moveSound.loop(moveVolume);
            moveWasPlaying = true;
        } else if (!isMoving && moveWasPlaying) {
            stopMoveSound();
        }
    }

    /**
     * Stops the move sound if it is currently playing.
     */
    public void stopMoveSound() {
        if (moveSoundId != -1) {
            moveSound.stop(moveSoundId);
            moveSoundId = -1;
        }
        moveWasPlaying = false;
    }

    /**
     * Plays the damage sound effect if sound effects are enabled.
     */
    public void playDamage() {
        if (game.isSfxPlaying()) {
            damageSound.play(damageVolume);
        }
    }

    /**
     * Plays the score sound effect if sound effects are enabled.
     */
    public void playScore() {
        if (game.isSfxPlaying()) {
            scoreSound.play(scoreVolume);
        }
    }

    /**
     * Plays the hover sound effect if sound effects are enabled.
     */
    public void playHover() {
        if (game.isSfxPlaying()) {
            hoverSound.play(hoverVolume);
        }
    }

    /**
     * Plays the click sound effect if sound effects are enabled.
     */
    public void playClick() {
        if (game.isSfxPlaying()) {
            clickSound.play(clickVolume);
        }
    }

    /**
     * Enables or disables sound effects. If disabled, stops the move sound.
     * @param enabled true to enable SFX, false to disable
     */
    public void setSfxEnabled(boolean enabled) {
        game.setSfxPlaying(enabled);
        if (!enabled) {
            stopMoveSound();
        }
    }

    /**
     * Disposes of all audio resources and resets the singleton instance.
     */
    @Override
    public void dispose() {
        gameMusic.dispose();
        moveSound.dispose();
        damageSound.dispose();
        scoreSound.dispose();
        instance = null;
    }
}
