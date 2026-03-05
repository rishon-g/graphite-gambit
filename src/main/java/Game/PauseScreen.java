package Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Pause screen, mainly just for testing at this point
 * @author Luke McRae
 * @version 1.0
 */
public class PauseScreen extends ScreenAdapter {
    private final GdxGame game;
    private final Batch batch;
    private final BitmapFont font;
    private final Viewport viewport = new ScreenViewport();
    private final GlyphLayout layout = new GlyphLayout();
    ShapeRenderer shapeRenderer = new ShapeRenderer();

    public PauseScreen(GdxGame game) {
        this.game = game;
        this.batch = game.getBatch();
        this.font = game.getFont();
    }

    @Override
    public void render(float delta) {
        // unpause
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && game.numScreens() == 2) {
            game.popScreen();
        }

        viewport.apply();

        float panelX = viewport.getWorldWidth() / 2f - viewport.getWorldWidth() / 4f;
        float panelWidth = viewport.getWorldWidth() / 2f;
        float panelHeight = viewport.getWorldHeight();

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        // draw shape of pause menu
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.rect(panelX, 0, panelWidth, panelHeight);
        shapeRenderer.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        layout.setText(font, "Paused");
        font.draw(batch, layout,
                panelX + panelWidth / 2f - layout.width / 2f,
                panelHeight / 2f + layout.height / 2f);
        batch.end();

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
}
