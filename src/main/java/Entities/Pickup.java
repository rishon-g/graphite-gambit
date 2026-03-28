package Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import Game.GameWorld;

public class Pickup extends Entity {
    private Texture graphiteTexture;
    private Texture glowTexture;
    private int healAmount = 20;

    private float stateTime = 0f;
    private float baseY = -1f;

    public Pickup(GameWorld world) {
        super(world);

        // load textures
        this.graphiteTexture = new Texture(Gdx.files.internal("sprites/graphite.png"));
        this.glowTexture = new Texture(Gdx.files.internal("sprites/glow.png"));

        // keep the 64x64 hitbox so the math and centering stays perfect
        this.transform.setScale(64, 64);
    }

    @Override
    public void updateInternal(float delta) {
        // lock in the spawn floor height (placed in correct spot)
        if (baseY == -1f) {
            baseY = this.transform.position.y;
        }

        stateTime += delta;

        // the graphite oscillates up and down
        float newY = baseY + (float) Math.sin(stateTime * 5f) * 10f;
        this.transform.position.y = newY;

        // collision Logic
        if (this.transform.collides(world.getPlayer().transform)) {
            world.getPlayer().modifyHealth(healAmount);
            this.dead = true;
        }
    }

    @Override
    public void render(SpriteBatch batch, float delta) {

        // glow
        batch.draw(glowTexture,
                this.transform.position.x * Game.GdxGame.UNIT_SCALE,
                baseY * Game.GdxGame.UNIT_SCALE, // static
                this.transform.size.x * Game.GdxGame.UNIT_SCALE,
                this.transform.size.y * Game.GdxGame.UNIT_SCALE
        );

        // graphite
        batch.draw(graphiteTexture,
                this.transform.position.x * Game.GdxGame.UNIT_SCALE,
                this.transform.position.y * Game.GdxGame.UNIT_SCALE,
                this.transform.size.x * Game.GdxGame.UNIT_SCALE,
                this.transform.size.y * Game.GdxGame.UNIT_SCALE
        );
    }
}