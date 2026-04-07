package Objects;

import Entities.Entity;
import Entities.Player;
import Game.GameWorld;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Non-player entity representing an ink obstacle that slows the player on contact.
 *
 * <p>This entity is rendered as a static ink sprite in the world. When the
 * player collides with it, a slowdown effect is applied.</p>
 */
public class Ink extends Entity implements Nonplayer {

    /**
     * Texture used to render the ink obstacle.
     */
    private final Texture texture;


    public static final float DEFAULT_WIDTH = 468f;
    public static final float DEFAULT_HEIGHT = 256f;

    /**
     * Creates a new ink obstacle.
     *
     * @param world the game world
     */
    public Ink(GameWorld world) {
        super(world);
        this.transform.setScale(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.texture = new Texture(Gdx.files.internal("src/main/resources/sprites/ink.png"));
    }

    /**
     * Updates the internal state of the ink object.
     *
     * <p>This entity is static, no update logic is required.</p>
     *
     * @param delta time since last update
     */
    @Override
    public void updateInternal(float delta) {
    }

    /**
     * Renders the ink sprite.
     *
     * @param batch sprite batch used for drawing
     * @param delta time since last update
     */
    @Override
    public void render(SpriteBatch batch, float delta) {
        renderTexture(batch, texture);
    }

    /**
     * Applies the slowdown effect when the player collides with this object.
     *
     * @param player the player colliding with the ink
     */
    @Override
    public void playerCollide(Player player) {
        // Trigger the slowdown effect every frame the player is touching this box
        player.applyInkSlowdown();
    }
}