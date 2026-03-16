package Entities;

import Game.GameWorld;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Door extends Entity {
    private Texture texture;

    public Door(GameWorld world, String part) {
        super(world);
        // Keep the standard 1-tile physical hitbox for each half!
        this.transform.setScale(128, 128);

        if (part.equals("Left")) {
            this.texture = new Texture(Gdx.files.internal("sprites/keypixel1.png"));
        } else if (part.equals("Right")) {
            this.texture = new Texture(Gdx.files.internal("sprites/keypixel2.png"));
        }
    }

    @Override
    public void updateInternal(float delta) {
        // Doors don't need to do any math
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