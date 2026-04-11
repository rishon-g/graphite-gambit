package Objects;

import Entities.Player;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * The Nonplayer interface represents an entity in the game that the player can interact or collide with.
 * Classes implementing this interface must define their specific collision, logic, and rendering behaviors.
 */
public interface Nonplayer {

    /**
     * Updates the internal state and logic of the nonplayer entity.
     *
     * @param delta time since last update
     */
    void updateInternal(float delta);

    /**
     * Handles the event that this nonplayer entity collides with the player.
     * The specific behavior of the collision will depend on the type of object.
     * * @param player the player that collided with this entity
     */
    void playerCollide(Player player);

    /**
     * Renders the nonplayer entity on the screen.
     *
     * @param batch sprite batch used for drawing
     * @param delta time since last update
     */
    void render(SpriteBatch batch, float delta);

}