package Entities;

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
public abstract class Enemy extends Entity {
    public Enemy(GameWorld world) {
        super(world);
    }

    /**
     * The attack method is called when the enemy attacks the player.
     * The specific behavior of the attack will depend on the type of enemy and should be implemented in the subclasses.
     */
    public abstract void attack();
}
