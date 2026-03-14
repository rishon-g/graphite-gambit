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
    private int points;
    private float speed = 400f; // pixels per second
    private float drainTimer = 0f;

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
    }

    /**
     * Gets the current health of the player.
     * @return current health
     */
    public int getHealth() {
        return health;
    }

    /**
     * Gets the current points of the player.
     * @return current points
     */
    public int getPoints() {
        return points;
    }

    /**
     * Updates the player's points by the given amount.
     * @param amount amount to add to points (can be negative)
     */
    public void updatePoints(int amount) {
        this.points += amount;
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
            this.points += excess;
            
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
        drainTimer += delta;

        // every 1 second, lose 2 health #TODO WILL NEED ADJUSTING OVER TIME
        if (drainTimer >= 1.0f) {
            modifyHealth(-2);
            drainTimer -= 1.0f;
        }

        float vx = 0;
        float vy = 0;

        // check keyboard input
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) vy = speed;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) vy = -speed;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) vx = -speed;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) vx = speed;

        if (vx != 0 && vy != 0) {
            // calculate current diagonal speed
            float currentSpeed = (float) Math.sqrt((vx * vx) + (vy * vy));

            // scale vx and vy down so the total speed equals our limit
            vx = (vx / currentSpeed) * speed;
            vy = (vy / currentSpeed) * speed;
        }

        // set the velocity in the transform
        this.transform.setVelocity(vx, vy);

        // ask the GameWorld to move the player safely (this handles collisions)
        this.world.requestMove(this.transform, delta);


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
