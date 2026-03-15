package Entities;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import Game.GameWorld;

/**
 * The Player class represents the player character in the game, extending from Entity.
 * the player contains attibutes such as health and points.
 * 
 * @author Lane Jacobson
 * @version 1.0
 * @since 2026-2-26
 */
public class Player extends Entity {
    private Texture dummyTexture;
    // health (graphite) of the player
    private int health;
    private int maxHealth;
    private float drainTimer = 0f;
    private float speed = 600f; // pixels per second
    private float acceleration = 3200f;
    private int points;

    /**
     * Constructor for the Player class, initializes health and points to default values.
     */
    public Player(GameWorld world) {
        super(world);
        this.health = 100;
        this.maxHealth = 100;

        // TODO CHANGE AS SOON AS A SPRITE IS READY
        // generate a 1x1 red pixel STRICTLY for testing, stretches over the player's transform size
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();
        this.dummyTexture = new Texture(pixmap);
        pixmap.dispose();
        this.transform.setScale(100, 100);
    }

    /**
     * Gets the current health of the player.
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
            
        // If health drops to 0 or below, handle game end logic
        }else if (this.health <= 0) {
            dead = true;
        }
    }

    /**
     * Updates the player's state, such as movement and health. This method is called every frame.
     */
    @Override
    public void updateInternal(float delta) {
        // graphite drain logic
        // only tick if the player is moving (has velocity)
        if (Math.abs(this.transform.velocity.x) > 1f || Math.abs(this.transform.velocity.y) > 1f) {
            drainTimer += delta;

            // every 1 second of MOVEMENT, lose 2 health TODO change accordingly
            if (drainTimer >= 1.0f) {
                modifyHealth(-2);
                drainTimer -= 1.0f; // reset the timer, but keep leftover fractions
            }
        }


        boolean up, down, left, right;

        // 1. Check Keyboard Input
        up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        int xinput = 0, yinput = 0;
        if(right)xinput++;
        if(left)xinput--;
        if(up)yinput++;
        if(down)yinput--;

        // 2. Calculate new Y velocity
        if (yinput != 0) {
            // accelerate in the direction of input
            this.transform.velocity.y += acceleration * delta * yinput;
        } else {
            // decelerate towards zero
            if (this.transform.velocity.y > 0) {
                this.transform.velocity.y -= acceleration * delta;
                if (this.transform.velocity.y < 0) this.transform.velocity.y = 0; // Prevent overshoot
            } else if (this.transform.velocity.y < 0) {
                this.transform.velocity.y += acceleration * delta;
                if (this.transform.velocity.y > 0) this.transform.velocity.y = 0; // Prevent overshoot
            }
        }
        // cap y speed
        if (this.transform.velocity.y > speed) this.transform.velocity.y = speed;
        else if (this.transform.velocity.y < -speed) this.transform.velocity.y = -speed;


        // calculate new X velocity
        if (xinput != 0) {
            // accelerate in the direction of input
            this.transform.velocity.x += acceleration * delta * xinput;
        } else {
            // decelerate towards zero
            if (this.transform.velocity.x > 0) {
                this.transform.velocity.x -= acceleration * delta;
                if (this.transform.velocity.x < 0) this.transform.velocity.x = 0; // Prevent overshoot
            } else if (this.transform.velocity.x < 0) {
                this.transform.velocity.x += acceleration * delta;
                if (this.transform.velocity.x > 0) this.transform.velocity.x = 0; // Prevent overshoot
            }
        }
        // cap x speed
        if (this.transform.velocity.x > speed) this.transform.velocity.x = speed;
        else if (this.transform.velocity.x < -speed) this.transform.velocity.x = -speed;

        // fix for diagonal strafing
        // calculate the actual current diagonal speed using the Pythagorean theorem
        float currentSpeed = (float) Math.sqrt(
                (this.transform.velocity.x * this.transform.velocity.x) +
                        (this.transform.velocity.y * this.transform.velocity.y)
        );

        // if diagonal speed exceeds the max speed, scale it back down (aka normalize)
        if (currentSpeed > speed) {
            this.transform.velocity.x = (this.transform.velocity.x / currentSpeed) * speed;
            this.transform.velocity.y = (this.transform.velocity.y / currentSpeed) * speed;
        }

    }

    /**
     * Renders the player on the screen. This method is called every frame after update().
     */
    @Override
    public void render(SpriteBatch batch, float delta) {

        // TODO change to actual sprite
        // draw the red square at the player's scaled position (by scaled we mean the pixel size)
        batch.draw(dummyTexture,
                this.transform.position.x * Game.GdxGame.UNIT_SCALE,
                this.transform.position.y * Game.GdxGame.UNIT_SCALE,
                this.transform.size.x * Game.GdxGame.UNIT_SCALE,
                this.transform.size.y * Game.GdxGame.UNIT_SCALE
        );
    }
}
