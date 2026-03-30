package Objects;

import Entities.Player;
import utils.GameTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ExitPointTest extends GameTest {

    private Player mockPlayer;

    @BeforeEach
    public void setUp() {
        mockPlayer = mock(Player.class);
    }

    @Test
    public void testConstructor_InitializesScale() {
        ExitPoint exit = new ExitPoint(mockWorld);

        // make sure scale is correct
        assertEquals(ExitPoint.DEFAULT_WIDTH, exit.transform.size.x, "incorrect default width");
        assertEquals(ExitPoint.DEFAULT_HEIGHT, exit.transform.size.y, "incorrect default height");
    }

    @Test
    public void testPlayerCollide_TriggersWinAndDies() {
        ExitPoint exit = new ExitPoint(mockWorld);
        assertFalse(exit.dead, "exit point should start alive");

        exit.playerCollide(mockPlayer);
        // player should win
        verify(mockWorld, times(1)).winGame();

        // should successfully mark itself for garbage collection
        assertTrue(exit.dead, "Exit point should mark itself dead after being collected");
    }

    @Test
    public void testUpdateInternal_DoesNothing() {
        ExitPoint exit = new ExitPoint(mockWorld);
        float initialX = exit.transform.position.x;
        float initialY = exit.transform.position.y;

        exit.updateInternal(0.1f);

        // verify the object is completely static
        assertEquals(initialX, exit.transform.position.x, "exitpoint should not move");
        assertEquals(initialY, exit.transform.position.y, "exitpoint should not move");
    }
}