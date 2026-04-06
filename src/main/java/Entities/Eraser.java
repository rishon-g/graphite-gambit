package Entities;

import Game.AudioManager;
import Game.DrawWeight;
import Game.GameWorld;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Enemy that moves toward the player, erases drawn floor tiles while moving,
 * and damages the player on contact.
 *
 * <p>
 * The Eraser inherits shared pathfinding and movement behavior from
 * {@link MobileEnemy}. In addition to movement, it stores its spawn position,
 * removes player-drawn floor tiles, and
 * respawns back at its starting location after a successful attack.
 * </p>
 */
public class Eraser extends MobileEnemy {

    /**
     * Movement speed in world units per second.
     */
    private static final float MOVE_SPEED = 400f;

    /**
     * Amount of damage dealt to the player on contact.
     */
    private static final int ATTACK_DAMAGE = 10;

    /**
     * Indicators for the direction the player is facing for sprite rendering.
     */
    private static final int DOWN = 0;
    private static final int UP = 1;
    private static final int RIGHT = 2;
    private static final int LEFT = 3;
    private int facing = DOWN;

    /**
     * sprites for the eraser
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
     * Remaining time until the eraser can respawn.
     */
    private final float RESPAWN_TIME = 5.0f;
    private float respawnTimer = 0f;

    private static final float DRAW_WIDTH = 128;
    private static final float DRAW_HEIGHT = 192;

    DrawWeight weight = (x, y, brushsize) -> {
        // Manhattan distance (fast, no sqrt)
        float dist = Math.abs(x) + Math.abs(y);

        // Normalize distance
        float t = Math.min(dist / brushsize, 1.0f);

        // Linearly degrade weight based on distance
        return Math.min(5 + (int) (5 * (1.5f - t)), 10);
    };

    /**
     * Creates a new eraser enemy.
     *
     * @param world the game world
     */
    public Eraser(GameWorld world) {
        super(world);
        transform.setScale(64, 128);
        Texture png = new Texture("src/main/resources/sprites/EraserSheet.png");
        TextureRegion[][] sheet = TextureRegion.split(png, 32, 64);
        sprites = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            sprites[i] = sheet[0][i];
        }
        ATTACK_RANGE = 50f;
    }

    /**
     * Updates eraser-specific state before movement logic runs.
     *
     * <p>
     * This includes saving the original spawn position, updating the attack
     * cooldown timer, and erasing drawn floor tiles.
     * </p>
     *
     * @param delta time since last update
     */
    @Override
    protected boolean beforeMovementUpdate(float delta) {
        saveSpawnPositionIfNeeded();

        if(respawnTimer > 0f) {
            respawnTimer -= delta;
            if(respawnTimer < 0f) {
                respawnTimer = 0f;
            }
            return false;
        }

        if (!isMoving()) {
            return true;
        }

        updateFacingFromVelocity();
        eraseFloorUnderEraser();
        return true;
    }

    /**
     * Saves the eraser's initial spawn position the first time it updates.
     */
    private void saveSpawnPositionIfNeeded() {
        if (startX == -1f) {
            startX = transform.position.x;
            startY = transform.position.y;
        }
    }

    /**
     * Checks whether the eraser is currently moving.
     *
     * @return true if the eraser has non-zero velocity
     */
    private boolean isMoving() {
        return transform.velocity.x != 0 || transform.velocity.y != 0;
    }

    /**
     * Updates the facing direction based on the current velocity.
     */
    private void updateFacingFromVelocity() {
        if (Math.abs(transform.velocity.y) > Math.abs(transform.velocity.x)) {
            facing = transform.velocity.y > 0 ? UP : DOWN;
        } else {
            facing = transform.velocity.x > 0 ? RIGHT : LEFT;
        }
    }

    /**
     * Erases drawn floor tiles under the eraser.
     */
    private void eraseFloorUnderEraser() {
        world.floorDraw(
                transform.position.x + transform.size.x / 2,
                transform.position.y + transform.size.y / 4,
                true,
                10,
                weight
        );
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
     * Renders the eraser on the screen.
     *
     * @param batch sprite batch used for drawing
     * @param delta time since last update
     */
    @Override
    public void render(SpriteBatch batch, float delta) {

        if(respawnTimer > 0f) {
            batch.setColor(1f, 1f, 1f, (RESPAWN_TIME - respawnTimer) / RESPAWN_TIME);
        }

        float offsetX = (DRAW_WIDTH - transform.size.x) / 2f;

        float offsetY = (DRAW_HEIGHT - transform.size.y) / 2f;

        float drawX = transform.position.x - offsetX;
        float drawY = transform.position.y - offsetY;

        batch.draw(
                sprites[facing],
                drawX * Game.GdxGame.UNIT_SCALE,
                drawY * Game.GdxGame.UNIT_SCALE,
                DRAW_WIDTH * Game.GdxGame.UNIT_SCALE,
                DRAW_HEIGHT * Game.GdxGame.UNIT_SCALE);
        
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * Handles collision with the player.
     *
     * <p>
     * If the eraser is not on cooldown and the player is neither stunned nor
     * immune, it deals damage, plays a sound effect, teleports back to its spawn
     * position, clears its current path, and starts its attack cooldown.
     * </p>
     *
     * @param player the player that collided with this eraser
     */
    @Override
    public void playerCollide(Player player) {
        // Respect cooldown and immunity states
        if (player.isStunned || player.isImmune || respawnTimer > 0f) {
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

        respawnTimer = RESPAWN_TIME; // add a short delay before the eraser can move again
    }
}