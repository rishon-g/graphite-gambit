package Entities;

import utils.GameTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InkTest extends GameTest {

    private Player mockPlayer;

    @BeforeEach
    public void setUp() {
        mockPlayer = mock(Player.class);
    }

    @Test
    public void testConstructor_InitializesScale() {
        Ink ink = new Ink(mockWorld);

        // verify the static scale is set correctly
        assertEquals(Ink.DEFAULT_WIDTH, ink.transform.size.x, "Ink should have a correct default width.");
        assertEquals(Ink.DEFAULT_HEIGHT, ink.transform.size.y, "Ink should have a correct default height.");
    }

    @Test
    public void testPlayerCollide_CallsApplyInkSlowdown() {
        Ink ink = new Ink(mockWorld);

        // player collision
        ink.playerCollide(mockPlayer);

        // verify the ink successfully communicated with the player
        verify(mockPlayer, times(1)).applyInkSlowdown();
    }

    @Test
    public void testUpdateInternal_DoesNothing() {
        Ink ink = new Ink(mockWorld);
        float initialX = ink.transform.position.x;
        float initialY = ink.transform.position.y;

        // one frame goes by
        ink.updateInternal(0.1f);

        // verify the object is truly static and hasn't moved
        assertEquals(initialX, ink.transform.position.x, "ink should not move in X during update.");
        assertEquals(initialY, ink.transform.position.y, "ink should not move in Y during update.");
    }
}