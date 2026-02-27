package Components;

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
    public float x, y;

    // x and y pixel scales from the origin
    public float w, h;

    // x and y velocities (used for movement)
    public float vx, vy;

    /**
     * Default constructor for the Transform component, initializes all values to 0.
     */
    public Transform() {
        this.x = 0;
        this.y = 0;
        this.w = 0;
        this.h = 0;
        this.vx = 0;
        this.vy = 0;
    }

    /**
     * Moves the transform by the given amounts.
     * @param dx x amount to move
     * @param dy y amount to move
     */

    public void move(float dx, float dy) {
        this.x += dx;
        this.y += dy;
    }

    /**
     * Sets the position of the transform to the given coordinates.
     * @param x x position
     * @param y y position
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets the scale of the transform to the given values.
     * @param w x scale
     * @param h y scale
     */
    public void setScale(float w, float h) {
        this.w = w;
        this.h = h;
    }

    /**
     * Sets the velocity of the transform to the given values.
     * @param vx x velocity
     * @param vy y velocity
     */
    public void setVelocity(float vx, float vy) {
        this.vx = vx;
        this.vy = vy;
    }

    /**
     * Checks if the given coordinates collide with the transform's area.
     * @param x x coordinate to check
     * @param y y coordinate to check
     * @return true if the coordinates collide with the transform's area, false otherwise
     */
    public boolean collides(float x, float y){
        if (x > this.x && x < this.x + this.w && y > this.y && y < this.y + this.h) {
            return true;
        } else {
            return false;
        }
    }
}
