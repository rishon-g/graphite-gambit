package Objects;

import Entities.Entity;
import Entities.Player;
import Game.GameWorld;

/**
 * The Enemy class represents an enemy entity in the game, extending the Entity abstract class.
 * Enemies have an attack method that defines their behavior when attacking the player.
 * 
 * @author Lane Jacobson
 * @version 1.0
 * @since 2026-2-26
 * @see Entity
 */
//TODO make interface
public abstract class Nonplayer extends Entity {
    public Nonplayer(GameWorld world) {
        super(world);
    }

    /**
     * The playerCollide function handles the event that this nonplayer entity collides with the player.
     * The specific behavior of the collision will depend on the type of enemy and should be implemented in the subclasses.
     */
    public abstract void playerCollide(Player player);
}
