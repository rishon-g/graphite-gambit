package Screens;

import Game.GdxGame;
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
    private Texture normalTexture, hoverTexture, disabledTexture;
    private Rectangle bounds;
    private final GlyphLayout layout = new GlyphLayout();
    private boolean disabled;
    private Runnable action;
    private double scale;
    static int screenWidth = 1920;

    /**
     * Constructor for a menu button
     * @param label the text to display on the button
     * @param x x position on screen
     * @param y y position on screen
     */

    public MenuButton(String label, float x, float y, double scale, boolean disabled, Runnable action) {
        if (!GdxGame.isTestMode()) {
            this.disabledTexture = new Texture(Gdx.files.internal("images/menu-button-disabled.png"));
            this.hoverTexture = new Texture(Gdx.files.internal("images/menu-button-highlighted.png"));
            this.normalTexture = new Texture(Gdx.files.internal("images/menu-button.png"));
        }
        this.label = label;
        this.scale = scale;
        this.action = action;
        this.bounds = new Rectangle(x, y, (int)(800 * scale), (int)(80 * scale));
        this.disabled = disabled;
    }

    /**
     * sets the action to run when the button is clicked
     * @param action the action to run
     */
    public void setAction(Runnable action) {
        this.action = action;
    }

    /**
     * runs the action associated with the button
     */
    public void click() {
        if (!disabled && action != null) {
            action.run();
        }
    }
    /**
     * helper function to create uniform, centered buttons on the screen that are
     * spaced out by deltaY automatically, in the order they are created
     * change variables as needed to adjust spacing and size
     *
     * @param text the text to display on the button
     * @return a new MenuButton object to add to the buttons array
     */
    public static MenuButton createCenteredButton(String text, int ID, double scale, boolean isDisabled, Runnable action) {
        // global parameters for buttons
        int startingY = 700;
        int spacing = 20;

        int deltaY = -((int)(80 * scale) + spacing);
        return new MenuButton(text, (screenWidth >> 1) - ((int)(800 * scale) >> 1), startingY + (deltaY * ID), scale, isDisabled, action);
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
        Texture texture;
        if (disabled) {
            texture = disabledTexture;
        } else {
            texture = selected ? hoverTexture : normalTexture;
        }
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

    public String getLabel() {
        return label;
    }
}
