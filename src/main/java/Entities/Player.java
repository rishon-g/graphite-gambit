package Entities;
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
    // health (graphite) of the player
    private int health;
    private int maxHealth;
    private int points;

    /**
     * Constructor for the Player class, initializes health and points to default values.
     */
    public Player(GameWorld world) {
        super(world);
        this.health = 100;
        this.maxHealth = 100;
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
            //TODO: game end logic
        }
    }

    /**
     * Updates the player's state, such as movement and health. This method is called every frame.
     */
    @Override
    public void update(float delta) {}

    /**
     * Renders the player on the screen. This method is called every frame after update().
     */
    @Override
    public void render(SpriteBatch batch, float delta) {}
}
