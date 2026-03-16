package Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import Game.GameWorld;

public class WhiteOut extends Nonplayer {
    private Texture texture;
    private int damageAmount;
    private float damageCooldown = 0f;

    public WhiteOut(GameWorld world, String size) {
        super(world);

        // setup based on the requested size
        if (size.equals("Large")) {
            this.texture = new Texture(Gdx.files.internal("sprites/whiteout_large.png")); // large graphic
            this.transform.setScale(384, 256); // larger hitbox
            this.damageAmount = -25;
        } else {
            this.texture = new Texture(Gdx.files.internal("sprites/whiteout_small.png")); //  small graphic
            this.transform.setScale(256, 74);
            this.damageAmount = -15;
        }
    }

    @Override
    public void updateInternal(float delta) {
        if (damageCooldown > 0) {
            damageCooldown -= delta;
        }
    }

    @Override
    public void playerCollide(Player player) {

        // only deal damage if the cooldown timer has hit zero
        if (damageCooldown <= 0) {
            player.modifyHealth(damageAmount);
            damageCooldown = 1.0f;
        }
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        batch.setColor(Color.WHITE);
        batch.draw(texture,
                this.transform.position.x * Game.GdxGame.UNIT_SCALE,
                this.transform.position.y * Game.GdxGame.UNIT_SCALE,
                this.transform.size.x * Game.GdxGame.UNIT_SCALE,
                this.transform.size.y * Game.GdxGame.UNIT_SCALE
        );
    }
}