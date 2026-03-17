package Entities;

import Game.AudioManager;
import Game.GameWorld;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Enemy that moves toward the player, erases drawn floor tiles while moving,
 * and damages the player on contact.
 *
 * <p>The Eraser inherits shared pathfinding and movement behavior from
 * {@link MobileEnemy}. In addition to movement, it stores its spawn position,
 * removes player-drawn floor tiles, and
 * respawns back at its starting location after a successful attack.</p>
 */
public class Eraser extends MobileEnemy {

    /**
     * Movement speed in world units per second.
     */
    private static final float MOVE_SPEED = 300f;

    /**
     * Visual size of the eraser sprite in world units.
     */
    private static final float DRAW_SIZE = 60f;

    /**
     * Amount of damage dealt to the player on contact.
     */
    private static final int ATTACK_DAMAGE = 10;

    /**
     * Cooldown in seconds between attacks.
     */
    private static final float ATTACK_COOLDOWN = 1.0f;

    private static final int DOWN = 0;
    private static final int UP = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;


    /**
     * Shared texture used to render the eraser.
     */
    private TextureRegion sprites[];

    /**
     * Initial x-coordinate where the eraser spawned.
     */
    private float startX = -1;

    /**
     * Initial y-coordinate where the eraser spawned.
     */
    private float startY = -1;

    /**
     * Remaining time until the eraser can attack again.
     */
    private float attackCooldownTimer = 0f;

    /**
     * Creates a new eraser enemy.
     *
     * @param world the game world
     */
    public Eraser(GameWorld world) {
        super(world);
        transform.setScale(DRAW_SIZE/2, DRAW_SIZE);
        Texture png = new Texture("src/main/resources/sprites/EraserSheet.png");
        TextureRegion[][] sheet = TextureRegion.split(png, 32, 64);
        sprites = new TextureRegion[4];
        for(int i = 0; i < 4; i++){
            sprites[i] = sheet[0][i];
        }
    }

    /**
     * Updates eraser-specific state before movement logic runs.
     *
     * <p>This includes saving the original spawn position, updating the attack
     * cooldown timer, and erasing drawn floor tiles.</p>
     *
     * @param delta time since last update
     */
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

    /**
     * Returns the eraser movement speed.
     *
     * @return movement speed in world units per second
     */
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
     * Renders the eraser on the screen.
     *
     * @param batch sprite batch used for drawing
     * @param delta time since last update
     */
    @Override
    public void render(SpriteBatch batch, float delta) {
        batch.draw(
                sprites[DOWN],
                transform.position.x * Game.GdxGame.UNIT_SCALE,
                transform.position.y * Game.GdxGame.UNIT_SCALE,
                transform.size.x * Game.GdxGame.UNIT_SCALE,
                transform.size.y * Game.GdxGame.UNIT_SCALE
        );
    }

    /**
     * Handles collision with the player.
     *
     * <p>If the eraser is not on cooldown and the player is neither stunned nor
     * immune, it deals damage, plays a sound effect, teleports back to its spawn
     * position, clears its current path, and starts its attack cooldown.</p>
     *
     * @param player the player that collided with this eraser
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