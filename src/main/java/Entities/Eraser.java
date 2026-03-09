package Entities;
import Game.GameWorld;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * The Eraser class represents an eraser entity in the game, extending from Entity.
 * The eraser moves toward the player, damaging them on contact, then is destroyed.
 * 
 * @param world the game world the eraser belongs to
 */
public class Eraser extends Enemy {
    public Eraser(GameWorld world) {
        super(world);
    }

    /**
     * The update method is called every frame to update the state of the eraser entity.
     * @param delta time since last update (used for movement and animations)
     */
    @Override
    public void update(float delta) {
    }

    /**
     * The render method is called every frame after update to render the eraser entity on the screen.
     */
    @Override
    public void render(SpriteBatch batch) {
    }

    /**
     * The attack method is called when the eraser attacks the player.
     */
    @Override
    public void attack() {
    }
}
