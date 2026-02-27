package Entities;

import Components.Transform;
import com.badlogic.gdx.graphics.Texture;
import Game.GameWorld;

/**
 * The Entity class represents an object or character in the game world.
 * It contains a Transform component for position and scale, and abstract methods for updating and rendering the entity.
 * 
 * @author Lane Jacobson
 * @version 1.0
 * @since 2026-2-26
 */
abstract public class Entity {
    // position and scale of the entity
    public Transform transform;

    // texture used for rendering the entity (can be null for invisible entities)
    public Texture sprite;

    // reference to the game world the entity belongs to (used for interactions with other entities)
    protected GameWorld world;

    /**
     * Constructor for the Entity class.
     */
    public Entity(GameWorld world) {
        this.transform = new Transform();
        this.sprite = null;
        this.world = world;
    }

    /**
     * Moves the entity by the given amounts.
     * @param dx x amount to move
     * @param dy y amount to move
     */

    abstract public void update(float delta);
    abstract public void render();
}
