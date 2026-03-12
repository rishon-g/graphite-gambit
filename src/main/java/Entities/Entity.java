package Entities;

import Components.Transform;
import Game.GameWorld;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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

    // reference to the game world the entity belongs to (used for interactions with other entities)
    protected GameWorld world;

    protected boolean dead;

    /**
     * Constructor for the Entity class.
     */
    public Entity(GameWorld world) {
        this.transform = new Transform();
        this.world = world;
        dead = false;
    }

    /**
     * Updates the internal system of the entity, then requests the world to move.
     * Called every frame by the world this entity is contained within.
     * 
     * @param delta time since last update
     */
    public void update(float delta){
        updateInternal(delta);
        world.requestMove(transform, delta);
    }

    /**
     * Returns whether or not the entity is dead.
     * @return true if dead, false if not.
     */
    public boolean dead(){
        return dead;
    }

    /**
     * Updates any internal systems of the entity such as velocity.
     * Called every frame, and abstracted for descendants to implement.
     * 
     * @param delta time since last update
     */
    abstract public void updateInternal(float delta);

    /**
     * Adds the sprite, or animation frame of the entity to the spritebatch to be rendered.
     * Called every frame by the world this entity is contained within.
     * 
     * @param batch spritebatch to be rendered
     * @param delta time since last update
     */
    abstract public void render(SpriteBatch batch, float delta);
}
