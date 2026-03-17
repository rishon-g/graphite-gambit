package Entities;

import Game.AudioManager;
import Game.GameWorld;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * The Eraser class represents an eraser entity in the game, extending from Entity.
 * The eraser moves toward the player and damages them on contact.
 */
public class Eraser extends MobileEnemy {
    /** Movement speed in world units per second. */
    private static final float MOVE_SPEED = 300f;
    /** Visual size of the eraser sprite in world units. */
    private static final float DRAW_SIZE = 60f;

    private static final int ATTACK_DAMAGE = 10;

    private static final float ATTACK_COOLDOWN = 1.0f;

    private static Texture TEXTURE;

    // respawning
    private float startX = -1;
    private float startY = -1;

    private float attackCooldownTimer = 0f;

    public Eraser(GameWorld world) {
        super(world);
        transform.setScale(DRAW_SIZE, DRAW_SIZE);
        TestTexture();
    }

    /**
     * Creates testTexture for eraser.
     */
    private static void TestTexture() {
        if (TEXTURE == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.RED);
            pixmap.fill();
            TEXTURE = new Texture(pixmap);
            pixmap.dispose();
        }
    }

    @Override
    protected void beforeMovementUpdate(float delta) {
        if (startX == -1f) {
            startX = transform.position.x;
            startY = transform.position.y;
        }

        if (attackCooldownTimer > 0f) {
            attackCooldownTimer -= delta;
            if (attackCooldownTimer < 0f) {
                attackCooldownTimer = 0f;
            }
        }

        if (Math.abs(transform.velocity.x) > 1f || Math.abs(transform.velocity.y) > 1f) {
            eraseWithHitbox();
        }
    }

    @Override
    protected float getMoveSpeed() {
        return MOVE_SPEED;
    }

    /**
     * Erases player-drawn floor tiles across the eraser's whole hitbox.
     */
    private void eraseWithHitbox() {
        float left = transform.position.x;
        float bottom = transform.position.y;
        float width = transform.size.x;
        float height = transform.size.y;

        int brushSize = 1;
        float step = 8f;

        for (float x = left; x <= left + width; x += step) {
            for (float y = bottom; y <= bottom + height; y += step) {
                world.floorDraw(x, y, true, brushSize);
            }
        }

        world.floorDraw(left + width / 2f, bottom + height / 2f, true, brushSize);
    }

    /**
     * The render method is called every frame after update to render the eraser entity on the screen.
     */
    @Override
    public void render(SpriteBatch batch, float delta) {
        batch.draw(
                TEXTURE,
                transform.position.x * Game.GdxGame.UNIT_SCALE,
                transform.position.y * Game.GdxGame.UNIT_SCALE,
                transform.size.x * Game.GdxGame.UNIT_SCALE,
                transform.size.y * Game.GdxGame.UNIT_SCALE
        );
    }

    /**
     * The attack method is called when the eraser attacks the player.
     */
    @Override
    public void playerCollide(Player player) {
        // Respect cooldown and immunity states
        if (attackCooldownTimer > 0f || player.isStunned || player.isImmune) {
            return;
        }

        // play damage sound
        AudioManager.getInstance().playDamage();

        player.modifyHealth(-ATTACK_DAMAGE);

        // despawn: we teleport the eraser back to where it first appeared in the world
        this.transform.setPosition(startX, startY);
        this.transform.setVelocity(0, 0);

        // reset: we clear the path so it has to re-calculate from the start position
        this.currentPath = java.util.Collections.emptyList();
        this.pathIndex = 0;
        this.pathTimer = 0f;

        // start the cooldown immediately so it doesn't double-attack if it respawns near the player
        attackCooldownTimer = ATTACK_COOLDOWN;
    }


}