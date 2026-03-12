package Components;

/**
 * a small class for the containment of coordinates
 * 
 * @author Lane Jacobson
 * @version 1.0
 */
public class Vec2 {
    public float x, y;

    /**
     * default constructor, set values to zero
     */
    public Vec2(){
        this.x = 0;
        this.y = 0;
    }

    /**
     * constructor with initial values
     * @param x initial x value
     * @param y initial y value
     */
    public Vec2(float x, float y){
        this.x = x;
        this.y = y;
    }

    /**
     * sets the values within the Vec2
     * @param x x value to set
     * @param y y value to set
     */
    public void set(float x, float y){
        this.x = x;
        this.y = y;
    }

    /**
     * add the parameters of one Vec2 to this one
     * @param translate the Vec2 to add to this
     */
    public void add(Vec2 translate){
        this.x += translate.x;
        this.y += translate.y;
    }
}
