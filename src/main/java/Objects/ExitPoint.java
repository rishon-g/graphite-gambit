package Objects;

import Entities.Player;
import Game.GameWorld;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Non-player entity representing the level exit.
 *
 * <p>This entity is rendered as a static trophy sprite in the world. When the
 * player collides with it, the game win condition is triggered.</p>
 */
public class ExitPoint extends Nonplayer {

    public final static int DEFAULT_WIDTH = 128;
    public final static int DEFAULT_HEIGHT = 128;

    /**
     * Texture used to render the exit point.
     */
    private final Texture texture;

    /**
     * Creates a new exit point.
     *
     * @param world the game world
     */
    public ExitPoint(GameWorld world) {
        super(world);
        this.transform.setScale(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.texture = new Texture(Gdx.files.internal("src/main/resources/sprites/trophy.png"));
    }

    /**
     * Updates the internal state of the exit point.
     *
     * <p>This entity is static, no update logic is required.</p>
     *
     * @param delta time since last update
     */
    @Override
    public void updateInternal(float delta) { }

    /**
     * Renders the exit point.
     *
     * @param batch sprite batch used for drawing
     * @param delta time since last update
     */
    @Override
    public void render(SpriteBatch batch, float delta) {
        renderTexture(batch, texture);
    }

    /**
     * Handles collision between the player and the exit point.
     *
     * <p>When the player reaches this object, the win condition is triggered
     * and this entity is marked as dead.</p>
     *
     * @param player the player colliding with the exit point
     */
    @Override
    public void playerCollide(Player player) {
        world.winGame();
        this.dead = true;
    }
}