package Entities;

import Game.GameWorld;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Enemy that moves toward the player and traps them.
 *
 * <p>When the player collides with the pencil sharpener, the player is stunned.
 * While the player remains trapped, this enemy periodically deals damage over time.</p>
 */
public class PencilSharpener extends MobileEnemy {

    /**
     * Movement speed in world units per second.
     */
    private static final float MOVE_SPEED = 150f;

    /**
     * Visual size of the sharpener sprite in world units.
     */
    private static final float DRAW_SIZE = 64f;

    /**
     * Shared texture used to render the pencil sharpener.
     */
    private static Texture TEXTURE;

    /**
     * Timer controlling how often damage is applied while the player is trapped.
     */
    private float damageTimer = 0f;

    /**
     * Creates a new pencil sharpener enemy.
     *
     * @param world the game world
     */
    public PencilSharpener(GameWorld world) {
        super(world);
        transform.setScale(DRAW_SIZE, DRAW_SIZE);

        // TODO Temporary Blue square until we add a real sprite
        if (TEXTURE == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLUE);
            pixmap.fill();
            TEXTURE = new Texture(pixmap);
            pixmap.dispose();
        }
    }

    /**
     * Updates sharpener-specific state before movement logic runs.
     *
     * <p>This reduces the damage timer used for periodic damage while
     * the player is trapped.</p>
     *
     * @param delta time since last update
     */
    @Override
    protected void beforeMovementUpdate(float delta) {
        if (damageTimer > 0f) {
            damageTimer -= delta;
            if (damageTimer < 0f) {
                damageTimer = 0f;
            }
        }
    }

    /**
     * Returns the pencil sharpener movement speed.
     *
     * @return movement speed in world units per second
     */
    @Override
    protected float getMoveSpeed() {
        return MOVE_SPEED;
    }

    /**
     * Renders the pencil sharpener on the screen.
     *
     * @param batch sprite batch used for drawing
     * @param delta time since last update
     */
    @Override
    public void render(SpriteBatch batch, float delta) {
        batch.draw(TEXTURE,
                transform.position.x * Game.GdxGame.UNIT_SCALE,
                transform.position.y * Game.GdxGame.UNIT_SCALE,
                transform.size.x * Game.GdxGame.UNIT_SCALE,
                transform.size.y * Game.GdxGame.UNIT_SCALE
        );
    }

    /**
     * Handles collision with the player.
     *
     * <p>If the player is immune, nothing happens. If the player is not already
     * stunned, the sharpener traps them by applying a stun. If the player is
     * already stunned, the sharpener periodically drains health while the
     * internal damage timer allows it.</p>
     *
     * @param player the player that collided with this sharpener
     */
    @Override
    public void playerCollide(Player player) {
        // if the player is immune, the sharpener can't grab them!
        if (player.isImmune) {
            return;
        }

        // if the player is NOT stunned yet, trap them
        if (!player.isStunned) {
            player.stun(8f);
            damageTimer = 0.5f; // Wait half a second before the first tick of damage
        }
        // if they ARE trapped, grind away their health
        else {
            if (damageTimer <= 0) {
                player.modifyHealth(-5); // Drain 5 graphite points
                damageTimer = 0.5f;      // Reset the timer to wait another half-second
            }
        }
    }
}