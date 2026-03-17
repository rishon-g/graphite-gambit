package Entities;

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
public class WhiteOut extends Nonplayer {

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
            this.texture = new Texture(Gdx.files.internal("sprites/whiteout_large.png")); // large graphic
            this.transform.setScale(384, 256); // larger hitbox
            this.damageAmount = -25;
        } else {
            this.texture = new Texture(Gdx.files.internal("sprites/whiteout_small.png")); //  small graphic
            this.transform.setScale(256, 74);
            this.damageAmount = -15;
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