package Entities;

import utils.GameTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DoorTest extends GameTest {

    @Test
    public void testConstructor_LeftDoor_InitializesCorrectly() {
        // hits the first 'if' statement
        Door door = new Door(mockWorld, "Left");

        assertEquals(Door.DEFAULT_WIDTH, door.transform.size.x, "wrong door width.");
        assertEquals(Door.DEFAULT_HEIGHT, door.transform.size.y, "wrong door height.");
    }

    @Test
    public void testConstructor_RightDoor_InitializesCorrectly() {
        // bypasses the first 'if', hits the 'else if'
        Door door = new Door(mockWorld, "Right");

        assertEquals(Door.DEFAULT_WIDTH, door.transform.size.x, "door width incorrect.");
    }

    @Test
    public void testConstructor_InvalidDoor_HandlesSafely() {
        // bypasses both 'if' statements to clear the final false branch
        Door door = new Door(mockWorld, "TEST");

        // it should still have a physical hitbox even if the texture string was bad
        assertEquals(Door.DEFAULT_WIDTH, door.transform.size.x);
    }

    @Test
    public void testUpdateInternal_DoesNothing() {
        Door door = new Door(mockWorld, "Left");
        float initialX = door.transform.position.x;
        float initialY = door.transform.position.y;

        door.updateInternal(0.5f);

        // verify the door is statically locked in place
        assertEquals(initialX, door.transform.position.x, "Door should not move (X)");
        assertEquals(initialY, door.transform.position.y, "Door should not move (Y)");
    }
}