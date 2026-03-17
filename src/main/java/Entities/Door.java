package Entities;

import Game.GameWorld;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Static world entity representing one half of a door.
 *
 * <p>A door is created as either the left or right part.</p>
 */
public class Door extends Entity {

    /**
     * Texture used to render this door half.
     */
    private Texture texture;

    /**
     * Creates a door entity for the specified door part.
     * <ul>
     *     <li>"Left" loads the left-half door sprite</li>
     *     <li>"Right" loads the right-half door sprite</li>
     * </ul>
     *
     * @param world the game world
     * @param part the door half
     */
    public Door(GameWorld world, String part) {
        super(world);
        // Keep the standard 1-tile physical hitbox for each half!
        this.transform.setScale(128, 128);

        if (part.equals("Left")) {
            this.texture = new Texture(Gdx.files.internal("sprites/keypixel1.png"));
        } else if (part.equals("Right")) {
            this.texture = new Texture(Gdx.files.internal("sprites/keypixel2.png"));
        }
    }

    /**
     * Updates the internal state of the door.
     *
     * <p>This entity is static, no update logic is required.</p>
     *
     * @param delta time since last update
     */
    @Override
    public void updateInternal(float delta) {
    }

    /**
     * Renders the door.
     *
     * @param batch sprite batch used for drawing
     * @param delta time since last update
     */
    @Override
    public void render(SpriteBatch batch, float delta) {
        renderTexture(batch, texture);
    }
}