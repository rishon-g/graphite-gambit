package Entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import Game.GameWorld;

public class Node extends Nonplayer {

    public Node(GameWorld world){
        super(world);
    }

    @Override
    public void updateInternal(float delta) {
        // No Internal Updates
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        // TODO
    }

    /**
     * Handles the event where the player collects the Node
     * grants points, then dies.
     */
    @Override
    public void playerCollide(Player player) {
        world.score(100);
        dead = true;
    }
}
