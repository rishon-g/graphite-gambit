package Game;

import Entities.Entity;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import Entities.Player;

public class DummyEntity extends Entity {
    public DummyEntity(GameWorld world) {
        super(world);
    }

    public int updateCount = 0;
    public int collideCount = 0;

    public void updateInternal(float delta) {
        updateCount++;
    }

    public void render(SpriteBatch batch, float delta) {

    }

    public void playerCollide(Player player) {
        collideCount++;
    }
}