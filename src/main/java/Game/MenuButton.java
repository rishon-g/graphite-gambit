package Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Instance of buttons for the main menu
 *
 * @author Luke McRae
 * @version 1.0
 * @since 2026-03-14
 */
public class MenuButton {
    private String label;
    private Texture normalTexture, hoverTexture;
    private Rectangle bounds;
    private final GlyphLayout layout = new GlyphLayout();

    /**
     * Constructor for a menu button
     * @param label the text to display on the button
     * @param normalTexture texture when not hovered (default)
     * @param hoverTexture texture when hovered
     * @param x x position on screen
     * @param y y position on screen
     * @param width button width
     * @param height button height
     */
    public MenuButton(String label, Texture normalTexture, Texture hoverTexture, float x, float y, float width, float height) {
        this.label = label;
        this.normalTexture = normalTexture;
        this.hoverTexture = hoverTexture;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void render(Batch batch, BitmapFont font, boolean selected) {
        Texture texture = selected ? hoverTexture : normalTexture;
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);

        // center contents
        layout.setText(font, label);
        font.draw(batch, label, bounds.x + (bounds.width - layout.width) / 2, bounds.y + (bounds.height + layout.height) / 2);
    }

    public boolean isHovered(Viewport viewport) {
        Vector3 mouse = viewport.getCamera().unproject(
                new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0), viewport.getScreenX(), viewport.getScreenY(),viewport.getScreenWidth(), viewport.getScreenHeight()
        );
        return bounds.contains(mouse.x, mouse.y);
    }

    public boolean isClicked(Viewport viewport) {
        return isHovered(viewport) && Gdx.input.isButtonPressed(com.badlogic.gdx.Input.Buttons.LEFT);
    }

    public void dispose() {
        normalTexture.dispose();
        hoverTexture.dispose();
    }
}
