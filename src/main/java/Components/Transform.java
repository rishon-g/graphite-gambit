package Components;

import Components.Corner;

import com.badlogic.gdx.math.Vector2;

/**
 * The Transform component is responsible for storing the position and scale of an entity.
 * It is primarily used in rendering and collisions.
 * 
 * @author Lane Jacobson
 * @version 1.0
 * @since 2026-2-26
 */
public class Transform {
    // x and y positions (using top left corner as origin)
    public Vec2 position;

    // x and y pixel scales from the origin
    public Vec2 size;

    // x and y velocities (used for movement)
    public Vec2 velocity;

    /**
     * Default constructor for the Transform component, initializes all values to 0.
     */
    public Transform() {
        position = new Vec2();
        size = new Vec2();
        velocity = new Vec2();
    }

    /**
     * Moves the transform by the given amounts.
     * @param translate the coordinates to move the position by
     */
    public void move(Vec2 translate) {
        position.add(translate);
    }

    /**
     * Sets the position of the transform to the given coordinates.
     * @param x x position
     * @param y y position
     */
    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    /**
     * Sets the size of the transform to the given values.
     * @param w width (x size) of the transform
     * @param h height (y size) of the transform
     */
    public void setScale(float w, float h) {
       size.set(w, h);
    }

    /**
     * Sets the velocity of the transform to the given values.
     * @param vx x velocity
     * @param vy y velocity
     */
    public void setVelocity(float vx, float vy) {
        velocity.set(vx, vy);
    }

    /**
     * Checks if the given coordinates collide with the transform's area.
     * @param x x coordinate to check
     * @param y y coordinate to check
     * @return true if the coordinates collide with the transform's area, false otherwise
     */
    public boolean collides(float x, float y){
        if (x > this.position.x && x < this.position.x + this.size.x && y > this.position.y && y < this.position.y + this.size.y) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * gets the position of one corner of the transform.
     * @param c the corner in question
     * @return the position of the corner
     */
    public Vec2 getCorner(Corner c){
        float x = position.x;
        float y = position.y;

        Vec2 pos = new Vec2();

        switch(c){
            case TL:
                // no change to position
                break;
            case TR:
                x += size.x;
                break;
            case BL:
                y += size.y;
                break;
            case BR:
                x += size.x;
                y += size.y;
                break;
            default:
                // return zeroes as temp debug
                return pos;
        }
        pos.set(x, y);
        return pos;
    }
}
