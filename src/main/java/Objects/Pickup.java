package Objects;

import Entities.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import Game.GameWorld;

public class Pickup extends Entity {
    private Texture graphiteTexture;
    private Texture glowTexture;

    private float stateTime = 0f;
    private float baseY = -1f;

    public static final float DEFAULT_WIDTH = 64f;
    public static final float DEFAULT_HEIGHT = 64f;
    public static final int HEAL_AMOUNT = 20;

    public Pickup(GameWorld world) {
        super(world);

        // load textures
        this.graphiteTexture = new Texture(Gdx.files.internal("src/main/resources/sprites/graphite.png"));
        this.glowTexture = new Texture(Gdx.files.internal("src/main/resources/sprites/glow.png"));

        // keep the 64x64 hitbox so the math and centering stays perfect
        this.transform.setScale(DEFAULT_WIDTH, DEFAULT_HEIGHT);
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
            world.getPlayer().modifyHealth(HEAL_AMOUNT);
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