package Entities;

import Game.GameWorld;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * The PencilSharpener enemy chases the player and traps them.
 * When the player is caught, it stuns them and deals periodic damage.
 */
public class PencilSharpener extends MobileEnemy {
    private static final float MOVE_SPEED = 150f;
    private static final float DRAW_SIZE = 64f;

    private static Texture TEXTURE;

    private float damageTimer = 0f;

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

    @Override
    protected void beforeMovementUpdate(float delta) {
        if (damageTimer > 0f) {
            damageTimer -= delta;
            if (damageTimer < 0f) {
                damageTimer = 0f;
            }
        }
    }

    @Override
    protected float getMoveSpeed() {
        return MOVE_SPEED;
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        batch.draw(TEXTURE,
                transform.position.x * Game.GdxGame.UNIT_SCALE,
                transform.position.y * Game.GdxGame.UNIT_SCALE,
                transform.size.x * Game.GdxGame.UNIT_SCALE,
                transform.size.y * Game.GdxGame.UNIT_SCALE
        );
    }

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