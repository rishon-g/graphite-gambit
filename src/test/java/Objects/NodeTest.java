package Objects;

import Entities.Player;
import Game.GdxGame;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import utils.GameTest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NodeTest extends GameTest {

    @Test
    public void testConstructor_SetsHitboxSize() {
        Node node = new Node(mockWorld);

        assertEquals(32f, node.transform.size.x, "node should be 32 wide.");
        assertEquals(32f, node.transform.size.y, "node should be 32 tall.");
        assertFalse(node.dead, "a new node should be alive.");
    }

    @Test
    public void testPlayerCollide_CollectsPlotPointAndDies() {
        Node node = new Node(mockWorld);

        node.playerCollide(new Player(mockWorld));

        verify(mockWorld, times(1)).plotPointCollected();
        assertTrue(node.dead, "a collected node should be removed.");
    }

    @Test
    public void testRender_DrawsAtPositionAndSizeInWorldUnits() {
        Node node = new Node(mockWorld);
        node.transform.setPosition(64, 96);
        SpriteBatch batch = mock(SpriteBatch.class);

        node.render(batch, 0.1f);

        verify(batch).draw(any(Texture.class), eq(64 * GdxGame.UNIT_SCALE), eq(96 * GdxGame.UNIT_SCALE),
                eq(32 * GdxGame.UNIT_SCALE), eq(32 * GdxGame.UNIT_SCALE));
    }
}
