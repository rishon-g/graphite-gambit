package Entities;

import Game.AudioManager;
import Game.DrawWeight;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import Game.GameWorld;

/**
 * The Player class represents the player character in the game, extending from
 * Entity.
 * the player contains attibutes such as health and points.
 * 
 * @author Lane Jacobson
 * @version 1.0
 * @since 2026-2-26
 */
public class Player extends Entity {
    // health (graphite) of the player
    private int health;
    private int maxHealth;
    public final static int STARTING_HEALTH = 100; // pixels per second

    private float drainTimer = 0f;
    private float acceleration = 3200f;

    // textures
    private TextureRegion sprites[][];
    private int facing = 0;

    // stun & immunity
    public boolean isStunned = false;
    public float stunTimer = 0f;
    public boolean isImmune = false;
    public float immunityTimer = 0f;
    private float time;

    // speed
    private float speed;
    public final static float BASE_SPEED = 600f; // pixels per second

    // ink slowdown
    private float currentSpeedMultiplier = 1.0f;
    private final float INK_SLOW_FACTOR = 0.4f;

    // how much does the graphite drain everytime there is movement?
    public static final int MOVEMENT_HEALTH_LOSS = -2;

    // movement sound state
    private boolean moveSoundSlowed = false;
    private boolean shouldPlayMoveSound = false;

    DrawWeight weight = (x, y, brushsize) -> {
        // Manhattan distance (fast, no sqrt)
        float dist = Math.abs(x) + Math.abs(y);

        // Normalize distance
        float t = Math.min(dist / brushsize, 1.0f);

        // Linearly degrade weight based on distance
        return 5 + (int) (3 * (1.0f - t));
    };

    /**
     * Constructor for the Player class, initializes health and points to default
     * values.
     */
    public Player(GameWorld world) {
        super(world);
        this.health = STARTING_HEALTH;
        this.speed = BASE_SPEED;

        this.maxHealth = 100;
        this.time = 0;
        Texture png = new Texture("src/main/resources/sprites/PencilSheet.png");
        TextureRegion[][] sheet = TextureRegion.split(png, 32, 64);
        sprites = new TextureRegion[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                sprites[i][j] = sheet[i][j];
            }
        }

        this.transform.setScale(64, 128);
    }

    /**
     * Gets the current health of the player.
     * 
     * @return current health
     */
    public int getHealth() {
        return health;
    }

    /**
     * Modifies the player's health by the given amount.
     * Excess healing is converted to points.
     *
     * @param amount amount to modify health by (positive or negative)
     */
    public void modifyHealth(int amount) {
        this.health += amount;

        // If health exceeds maxHealth, convert excess to points
        if (this.health > maxHealth) {
            int excess = this.health - maxHealth;
            this.health = maxHealth;
            world.score(excess);

        }

        // If health drops to 0 or below, handle game end logic
        if (this.health <= 0) {
            this.health = 0; // Bug fix: prevent negative health values!! TODO mention in report
            dead = true;
        }
    }

    /**
     * Traps the player for a set duration.
     */
    public void stun(float duration) {
        if (!isImmune && !isStunned) {
            this.isStunned = true;
            this.stunTimer = duration;
            this.transform.velocity.set(0, 0); // Instantly stop movement
        }
    }

    /**
     * Called by the Ink entity when colliding.
     */
    public void applyInkSlowdown() {
        this.currentSpeedMultiplier = INK_SLOW_FACTOR;
    }

    /**
     * Overrides the default entity update so we can detect whether the player
     * actually moved after collision handling.
     */
    @Override
    public void update(float delta) {
        float oldX = transform.position.x;
        float oldY = transform.position.y;

        updateInternal(delta);
        physics.requestMove(this, delta);

        float movedX = transform.position.x - oldX;
        float movedY = transform.position.y - oldY;

        boolean actuallyMoved = Math.abs(movedX) > 0.01f || Math.abs(movedY) > 0.01f;

        shouldPlayMoveSound = actuallyMoved;
        AudioManager.getInstance().updateMoveSound(shouldPlayMoveSound, moveSoundSlowed);

        if (actuallyMoved) {
            drainTimer += delta;

            if (drainTimer >= 1.0f) {
                modifyHealth(MOVEMENT_HEALTH_LOSS);
                drainTimer -= 1.0f;
            }
        }
    }

    /**
     * Updates the player's state, such as movement and health. This method is
     * called every frame.
     */
    @Override
    public void updateInternal(float delta) {
        // update time
        time += delta;

        // Draw on floor in the middle of the feet of the sprite
        world.floorDraw(transform.position.x + transform.size.x / 2, transform.position.y, false, 2, weight);

        // play sharpener sound
        Game.AudioManager.getInstance().updateSharpenerSound(isStunned);

        // handle immunity
        if (isImmune) {
            immunityTimer -= delta;
            if (immunityTimer <= 0) {
                isImmune = false;
            }
        }

        // handle stun state
        if (isStunned) {
            stunTimer -= delta;

            // player mashes space to escape faster
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                stunTimer -= 0.5f;
            }

            // did we escape
            if (stunTimer <= 0) {
                stunTimer = 0f; // Bug fix: PREVENT NEGATIVE TIMER TODO mention in report!
                isStunned = false;
                isImmune = true;
                immunityTimer = 1.0f;
            }

            shouldPlayMoveSound = false;
            moveSoundSlowed = false;
            // skip the rest of the update method so the player can't move
            return;
        }

        // calculate this frame's actual speed based on the multiplier
        speed = BASE_SPEED * currentSpeedMultiplier;

        moveSoundSlowed = (currentSpeedMultiplier == INK_SLOW_FACTOR);

        // immediately reset the multiplier back to normal for the next frame
        currentSpeedMultiplier = 1.0f;

        handleInput(delta);
    }

    /**
     * Handles player input for movement.
     * 
     * @param delta time since last update
     */
    private void handleInput(float delta) {
        boolean up, down, left, right;

        // check Keyboard Input
        up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        // calculate input direction
        int xinput = 0, yinput = 0;
        if (right)
            xinput++;
        if (left)
            xinput--;
        if (up)
            yinput++;
        if (down)
            yinput--;

        // recalibrate animation direction
        if (yinput < 0) {
            facing = 0;
        } else if (yinput > 0) {
            facing = 1;
        } else if (xinput > 0) {
            facing = 2;
        } else if (xinput < 0) {
            facing = 3;
        }

        // move player based on directional input
        move(delta, xinput, yinput);
    }

    /**
     * Moves the player based on input and applies acceleration and deceleration.
     * 
     * @param delta  time since last update
     * @param xinput x directional input (-1 for left, 1 for right, 0 for none)
     * @param yinput y directional input (-1 for down, 1 for up, 0 for none)
     */
    private void move(float delta, int xinput, int yinput) {
        int xclamp = 0, yclamp = 0;

        // Calculate new y velocity
        if (transform.velocity.y != 0) {
            float direction = Math.signum(this.transform.velocity.y);
            if (direction > 0 && !(yinput > 0)) {
                yclamp = 1;
                yinput--;
            } else if (direction < 0 && !(yinput < 0)) {
                yclamp = -1;
                yinput++;
            }
        }
        // apply acceleration
        this.transform.velocity.y += (acceleration * delta * yinput);
        if (yclamp > 0)
            this.transform.velocity.y = Math.max(0, this.transform.velocity.y);
        else if (yclamp < 0)
            this.transform.velocity.y = Math.min(0, this.transform.velocity.y);

        // Calculate new x velocity
        if (transform.velocity.x != 0) {
            float direction = Math.signum(this.transform.velocity.x);
            if (direction > 0 && !(xinput > 0)) {
                xclamp = 1;
                xinput--;
            } else if (direction < 0 && !(xinput < 0)) {
                xclamp = -1;
                xinput++;
            }
        }
        // apply acceleration
        this.transform.velocity.x += (acceleration * delta * xinput);
        if (xclamp > 0)
            this.transform.velocity.x = Math.max(0, this.transform.velocity.x);
        else if (xclamp < 0)
            this.transform.velocity.x = Math.min(0, this.transform.velocity.x);

        // Check capped boundaries, and apply speed cap
        if (this.transform.velocity.y > speed)
            this.transform.velocity.y = speed;
        else if (this.transform.velocity.y < -speed)
            this.transform.velocity.y = -speed;
        if (this.transform.velocity.x > speed)
            this.transform.velocity.x = speed;
        else if (this.transform.velocity.x < -speed)
            this.transform.velocity.x = -speed;
    }

    /**
     * Renders the player on the screen. This method is called every frame after
     * update().
     */
    @Override
    public void render(SpriteBatch batch, float delta) {
        int healthIndex = (health - 1) / 25;
        TextureRegion frame = sprites[facing][healthIndex];
        batch.setColor(1, 1, 1, 1);

        if (isImmune) {
            // scale it so the alpha (transparency) rapidly bounces between 0.3 (faint) and
            // 1.0 (solid)
            float flashAlpha = 0.65f + (float) (Math.sin(time * 20f) * 0.35f);
            batch.setColor(1, 1, 1, flashAlpha);
        } else {
            // draw normally if not immune
            batch.setColor(1, 1, 1, 1);
        }

        // how big the sprite should actually look on screen
        float visualWidth = 200f;
        float visualHeight = 256f;

        // calculate the offset to center the 256x256 image over the 128x128 hitbox
        float offsetX = (visualWidth - this.transform.size.x) / 2f;
        float offsetY = (visualHeight - this.transform.size.y) / 2f - 65; // tweaked to perfection

        // draw the sprite using the offset and the new visual dimensions
        batch.draw(frame,
                (this.transform.position.x - offsetX) * Game.GdxGame.UNIT_SCALE,
                (this.transform.position.y - offsetY) * Game.GdxGame.UNIT_SCALE,
                visualWidth * Game.GdxGame.UNIT_SCALE,
                visualHeight * Game.GdxGame.UNIT_SCALE);

        batch.setColor(Color.WHITE);
    }
}
