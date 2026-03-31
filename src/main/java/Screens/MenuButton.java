package Screens;

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
    private boolean disabled;

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
    public MenuButton(String label, Texture normalTexture, Texture hoverTexture, float x, float y, float width, float height, boolean disabled) {
        this.label = label;
        this.normalTexture = normalTexture;
        this.hoverTexture = hoverTexture;
        this.bounds = new Rectangle(x, y, width, height);
        this.disabled = disabled;
    }

    /**
     * Renders the button on the screen using the provided batch and font.
     * Draws the appropriate texture based on selection state and centers the label text.
     *
     * @param batch the batch to draw with
     * @param font the font to use for the label
     * @param selected whether the button is currently selected (hovered)
     */
    public void render(Batch batch, BitmapFont font, boolean selected) {
        Texture texture = selected ? hoverTexture : normalTexture;
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);

        // center contents
        layout.setText(font, label);
        font.draw(batch, label, bounds.x + (bounds.width - layout.width) / 2, bounds.y + (bounds.height + layout.height) / 2);
    }

    /**
     * Checks if the mouse is currently hovering over the button.
     *
     * @param viewport the viewport to unproject mouse coordinates
     * @return true if the mouse is over the button, false otherwise
     */
    public boolean isHovered(Viewport viewport) {
        Vector3 mouse = viewport.getCamera().unproject(
                new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0), viewport.getScreenX(), viewport.getScreenY(),viewport.getScreenWidth(), viewport.getScreenHeight()
        );
        return bounds.contains(mouse.x, mouse.y);
    }

    /**
     * Checks if the button is clicked (hovered and left mouse button just pressed).
     *
     * @param viewport the viewport to check mouse position
     * @return true if the button is clicked, false otherwise
     */
    public boolean isClicked(Viewport viewport) {
        return isHovered(viewport) && Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT);
    }

    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Disposes of the textures used by the button.
     */
    public void dispose() {
        normalTexture.dispose();
        hoverTexture.dispose();
    }

    /**
     * Checks if the normal texture matches the given texture.
     *
     * @param texture the texture to compare
     * @return true if the normal texture is the same as the given texture, false otherwise
     */
    public boolean textureIs(Texture texture) {
        return (normalTexture == texture);
    }

    public String getLabel() {
        return label;
    }
}
