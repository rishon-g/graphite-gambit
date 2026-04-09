package Objects;

import Entities.Player;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import Game.GameWorld;

/**
 * Non-player entity representing a node in the map, which the player must collect all to win.
 *
 * <p>This entity is rendered as a static node sprite in the world. When the
 * player collides with it, it gives them score and if all nodes are collected, the win
 * condition is activated.</p>
 */
public class Node extends Nonplayer {
    Texture texture;

    /**
     * Creates a node texture with the requested size.
     *
     * @param world the game world
     */
    public Node(GameWorld world){
        super(world);
        texture = new Texture("src/main/resources/sprites/Plot.png");
        transform.setScale(32, 32);
    }

    @Override
    public void updateInternal(float delta) {
        // No Internal Updates
    }

    /**
     * Renders the node.
     *
     * @param batch sprite batch used for drawing
     * @param delta time since last update
     */
    @Override
    public void render(SpriteBatch batch, float delta) {
        batch.draw(
                texture,
                transform.position.x * Game.GdxGame.UNIT_SCALE,
                transform.position.y * Game.GdxGame.UNIT_SCALE,
                transform.size.x * Game.GdxGame.UNIT_SCALE,
                transform.size.y * Game.GdxGame.UNIT_SCALE
        );
    }

    /**
     * Handles the event where the player collects the Node
     * grants points, then the Node dies.
     */
    @Override
    public void playerCollide(Player player) {
        world.plotPointCollected();
        dead = true;
    }
}
