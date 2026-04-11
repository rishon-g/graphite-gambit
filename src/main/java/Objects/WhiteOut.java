package Objects;

import Entities.Entity;
import Entities.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import Game.GameWorld;

/**
 * Non-player hazard entity that damages the player on contact.
 *
 * <p>A white-out object can be created in different sizes. The selected size
 * determines its sprite, hitbox dimensions, and damage amount. When the player
 * collides with it.</p>
 */
public class WhiteOut extends Entity implements Nonplayer {

    /**
     * Texture used to render this white-out hazard.
     */
    private final Texture texture;

    /**
     * Amount of damage.
     */
    private final int damageAmount;

    /**
     * Remaining cooldown time before this hazard can damage the player again.
     */
    private float damageCooldown = 0f;


    public static final float LARGE_DEFAULT_WIDTH = 384f;
    public static final float LARGE_DEFAULT_HEIGHT = 256f;
    public static final int LARGE_DEFAULT_DAMAGE = -25;

    public static final float SMALL_DEFAULT_WIDTH = 256f;
    public static final float SMALL_DEFAULT_HEIGHT = 74f;
    public static final int SMALL_DEFAULT_DAMAGE = -15;

    /**
     * Creates a white-out hazard with the requested size.
     *
     * @param world the game world
     * @param size the requested size
     */
    public WhiteOut(GameWorld world, String size) {
        super(world);

        // setup based on the requested size
        if (size.equals("Large")) {
            // HAD TO CHANGE TO CONSISTENT FILE LOADING FOR TESTING TODO include on report!
            this.texture = new Texture(Gdx.files.internal("src/main/resources/sprites/whiteout_large.png")); // large graphic
            this.transform.setScale(LARGE_DEFAULT_WIDTH, LARGE_DEFAULT_HEIGHT); // larger hitbox
            this.damageAmount = LARGE_DEFAULT_DAMAGE;
        } else {
            this.texture = new Texture(Gdx.files.internal("src/main/resources/sprites/whiteout_small.png")); //  small graphic
            this.transform.setScale(SMALL_DEFAULT_WIDTH, SMALL_DEFAULT_HEIGHT);
            this.damageAmount = SMALL_DEFAULT_DAMAGE;
        }
    }

    /**
     * Updates the internal cooldown timer.
     *
     * @param delta time since last update
     */
    @Override
    public void updateInternal(float delta) {
        if (damageCooldown > 0) {
            damageCooldown -= delta;
        }
    }

    /**
     * Applies damage to the player if the cooldown timer has expired.
     *
     * @param player the player colliding with this hazard
     */
    @Override
    public void playerCollide(Player player) {

        // only deal damage if the cooldown timer has hit zero
        if (damageCooldown <= 0) {
            player.modifyHealth(damageAmount);
            damageCooldown = 1.0f;
            // trigger damage sound
            Game.AudioManager.getInstance().playDamage();
        }
    }

    /**
     * Renders the white-out hazard.
     *
     * @param batch sprite batch used for drawing
     * @param delta time since last update
     */
    @Override
    public void render(SpriteBatch batch, float delta) {
        renderTexture(batch, texture);
    }
}