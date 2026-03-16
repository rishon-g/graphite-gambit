package Entities;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import Game.GameWorld;

/**
 * The Player class represents the player character in the game, extending from Entity.
 * the player contains attibutes such as health and points.
 * 
 * @author Lane Jacobson
 * @version 1.0
 * @since 2026-2-26
 */
public class Player extends Entity {
    // health (graphite) of the player
    private int health;
    private int maxHealth;
    private float drainTimer = 0f;
    private float speed = 600f; // pixels per second
    private float acceleration = 3200f;
    private TextureRegion sprites[][];
    private int facing = 0;

    /**
     * Constructor for the Player class, initializes health and points to default values.
     */
    public Player(GameWorld world) {
        super(world);
        this.health = 100;
        this.maxHealth = 100;
        Texture png = new Texture("src/main/java/Entities/Assets/PencilSheet.png");
        TextureRegion[][] sheet = TextureRegion.split(png, 32, 64);
        sprites = new TextureRegion[4][4];
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                sprites[i][j] = sheet[i][j];
            }
        }

        // TODO CHANGE AS SOON AS A SPRITE IS READY
        // generate a 1x1 red pixel STRICTLY for testing, stretches over the player's transform size
        // Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        // pixmap.setColor(Color.RED);
        // pixmap.fill();
        // this.dummyTexture = new Texture(pixmap);
        // pixmap.dispose();
        this.transform.setScale(64, 128);
    }

    /**
     * Gets the current health of the player.
     * @return current health
     */
    public int getHealth() {
        return health;
    }

    /**
     * Modifies the player's health by the given amount.
     * Excess healing is converted to points.
     *
     * @param amount amount to modify health by (positive or negative)
     */
    public void modifyHealth(int amount) {
        this.health += amount;

        // If health exceeds maxHealth, convert excess to points
        if (this.health > maxHealth) {
            int excess = this.health - maxHealth;
            this.health = maxHealth;
            world.score(excess);
            
        // If health drops to 0 or below, handle game end logic
        }else if (this.health <= 0) {
            dead = true;
        }
    }

    /**
     * Updates the player's state, such as movement and health. This method is called every frame.
     */
    @Override
    public void updateInternal(float delta) {
        // Draw on floor in the middle of the feet of the sprite
        world.floorDraw(transform.position.x + transform.size.x/2, transform.position.y, false, 2);

        // Movement
        // graphite drain logic
        // only tick if the player is moving (has velocity)
        if (Math.abs(this.transform.velocity.x) > 1f || Math.abs(this.transform.velocity.y) > 1f) {
            drainTimer += delta;

            // every 1 second of MOVEMENT, lose 2 health TODO change accordingly
            if (drainTimer >= 1.0f) {
                modifyHealth(-2);
                drainTimer -= 1.0f; // reset the timer, but keep leftover fractions
            }
        }

        boolean up, down, left, right;

        // 1. Check Keyboard Input
        up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        int xinput = 0, yinput = 0;
        if(right)xinput++;
        if(left)xinput--;
        if(up)yinput++;
        if(down)yinput--;

        // recalibrate animation direction
        if(yinput < 0){
            facing = 0;
        }else if(yinput > 0){
            facing = 1;
        }else if(xinput > 0){
            facing = 2;
        }else if(xinput < 0){
            facing = 3;
        }

        int xclamp = 0, yclamp = 0;

        // 2. Calculate new y velocity
        if(transform.velocity.y != 0){
        float direction = Math.signum(this.transform.velocity.y);
            if(direction > 0 && !(yinput > 0)){yclamp = 1; yinput--;}
            else if(direction < 0 && !(yinput < 0)){yclamp = -1; yinput++;}
        }
        this.transform.velocity.y += (acceleration * delta * yinput);
        if(yclamp > 0)
            this.transform.velocity.y = Math.max(0, this.transform.velocity.y);
        else if(yclamp < 0)
            this.transform.velocity.y = Math.min(0, this.transform.velocity.y);

        // 3. Calculate new x velocity
        if(transform.velocity.x != 0){
        float direction = Math.signum(this.transform.velocity.x);
            if(direction > 0 && !(xinput > 0)){xclamp = 1; xinput--;}
            else if(direction < 0 && !(xinput < 0)){xclamp = -1; xinput++;}
        }
        this.transform.velocity.x += (acceleration * delta * xinput);
        if(xclamp > 0)
            this.transform.velocity.x = Math.max(0, this.transform.velocity.x);
        else if(xclamp < 0)
            this.transform.velocity.x = Math.min(0, this.transform.velocity.x);

        // 4. Check boundaries
        if(this.transform.velocity.y > speed)this.transform.velocity.y = speed;
        else if(this.transform.velocity.y < -speed)this.transform.velocity.y = -speed;
        if(this.transform.velocity.x > speed)this.transform.velocity.x = speed;
        else if(this.transform.velocity.x < -speed)this.transform.velocity.x = -speed;

    }

    /**
     * Renders the player on the screen. This method is called every frame after update().
     */
    @Override
    public void render(SpriteBatch batch, float delta) {
        int healthIndex = (health - 1) / 25;
        TextureRegion frame = sprites[facing][healthIndex];
        batch.setColor(1,1,1,1);

        // how big the sprite should actually look on screen
        float visualWidth = 200f;
        float visualHeight = 256f;

        // calculate the offset to center the 256x256 image over the 128x128 hitbox
        float offsetX = (visualWidth - this.transform.size.x) / 2f;
        float offsetY = (visualHeight - this.transform.size.y) / 2f - 65; // tweaked to perfection

        // draw the sprite using the offset and the new visual dimensions
        batch.draw(frame,
                (this.transform.position.x - offsetX) * Game.GdxGame.UNIT_SCALE,
                (this.transform.position.y - offsetY) * Game.GdxGame.UNIT_SCALE,
                visualWidth * Game.GdxGame.UNIT_SCALE,
                visualHeight * Game.GdxGame.UNIT_SCALE
        );
    }
}
