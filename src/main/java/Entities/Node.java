package Entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import Game.GameWorld;

public class Node extends Nonplayer {
    Texture texture;

    public Node(GameWorld world){
        super(world);
        texture = new Texture("src/main/java/Entities/Assets/Plot.png");
        transform.setScale(32, 32);
    }

    @Override
    public void updateInternal(float delta) {
        // No Internal Updates
    }

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
     * grants points, then dies.
     */
    @Override
    public void playerCollide(Player player) {
        world.plotPointCollected();
        dead = true;
    }
}
