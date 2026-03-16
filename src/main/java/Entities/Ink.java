package Entities;

import Game.GameWorld;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Ink extends Nonplayer {

    private Texture texture;


    public Ink(GameWorld world) {
        super(world);
        this.transform.setScale(468, 256);
        this.texture = new Texture(Gdx.files.internal("sprites/ink.png"));
    }

    @Override
    public void updateInternal(float delta) {
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

    @Override
    public void playerCollide(Player player) {
        // Trigger the slowdown effect every frame the player is touching this box
        player.applyInkSlowdown();
    }
}