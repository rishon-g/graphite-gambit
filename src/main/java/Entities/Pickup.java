package Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import Game.GameWorld;

public class Pickup extends Entity {
    private Texture texture;
    private int healAmount = 20; // Restores 20 graphite points

    // animation variables
    private float stateTime = 0f;
    private float baseY = -1f; // Used to remember the exact spawn height

    public Pickup(GameWorld world) {
        super(world);

        // get texture
        this.texture = new Texture(Gdx.files.internal("sprites/graphite.png"));

        // set hitbox size
        this.transform.setScale(64, 64);
    }

    @Override
    public void updateInternal(float delta) {
        // lock in y position for first frame (meant for the animation)
        if (baseY == -1f) {
            baseY = this.transform.position.y;
        }

        // hover animation (going up and down)
        stateTime += delta;
        // math.sin oscillates smoothly
        float newY = baseY + (float) Math.sin(stateTime * 5f) * 10f;
        this.transform.position.y = newY;

        // collision Logic
        if (this.transform.collides(world.getPlayer().transform)) {
            world.getPlayer().modifyHealth(healAmount);
            this.dead = true; // Mark for deletion so it vanishes!
        }
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        batch.draw(texture,
                this.transform.position.x * Game.GdxGame.UNIT_SCALE,
                this.transform.position.y * Game.GdxGame.UNIT_SCALE,
                this.transform.size.x * Game.GdxGame.UNIT_SCALE,
                this.transform.size.y * Game.GdxGame.UNIT_SCALE
        );
    }
}