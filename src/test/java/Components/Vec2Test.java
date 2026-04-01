package Components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for the Vec2 class.
 * Tests basic vector operations including construction, setting values, and addition.
 */
class Vec2Test {

    /**
     * Test default constructor initializes x and y to 0.
     */
    @Test
    void testDefaultConstructor() {
        Vec2 vec = new Vec2();
        assertEquals(0, vec.x);
        assertEquals(0, vec.y);
    }

    /**
     * Test parameterized constructor sets x and y to provided values.
     */
    @Test
    void testParameterizedConstructor() {
        Vec2 vec = new Vec2(5.5f, 10.2f);
        assertEquals(5.5f, vec.x);
        assertEquals(10.2f, vec.y);
    }

    /**
     * Test set method updates x and y values.
     */
    @Test
    void testSet() {
        Vec2 vec = new Vec2();
        vec.set(3.0f, 4.0f);
        assertEquals(3.0f, vec.x);
        assertEquals(4.0f, vec.y);
    }

    /**
     * Test set method with negative values.
     */
    @Test
    void testSetNegative() {
        Vec2 vec = new Vec2(1.0f, 1.0f);
        vec.set(-2.5f, -7.3f);
        assertEquals(-2.5f, vec.x);
        assertEquals(-7.3f, vec.y);
    }

    /**
     * Test add method adds another vector's components.
     */
    @Test
    void testAdd() {
        Vec2 vec1 = new Vec2(1.0f, 2.0f);
        Vec2 vec2 = new Vec2(3.0f, 4.0f);
        vec1.add(vec2);
        assertEquals(4.0f, vec1.x);
        assertEquals(6.0f, vec1.y);
    }

    /**
     * Test add method with negative values.
     */
    @Test
    void testAddNegative() {
        Vec2 vec1 = new Vec2(5.0f, 10.0f);
        Vec2 vec2 = new Vec2(-2.0f, -3.0f);
        vec1.add(vec2);
        assertEquals(3.0f, vec1.x);
        assertEquals(7.0f, vec1.y);
    }

    /**
     * Test add method with zero vector.
     */
    @Test
    void testAddZeroVector() {
        Vec2 vec1 = new Vec2(2.5f, 3.5f);
        Vec2 zero = new Vec2(0.0f, 0.0f);
        vec1.add(zero);
        assertEquals(2.5f, vec1.x);
        assertEquals(3.5f, vec1.y);
    }

    /**
     * Test add method doesn't modify the argument vector.
     */
    @Test
    void testAddDoesNotModifyArgument() {
        Vec2 vec1 = new Vec2(1.0f, 2.0f);
        Vec2 vec2 = new Vec2(3.0f, 4.0f);
        vec1.add(vec2);
        assertEquals(3.0f, vec2.x);
        assertEquals(4.0f, vec2.y);
    }

    /**
     * Test multiple sequential set operations.
     */
    @Test
    void testMultipleSets() {
        Vec2 vec = new Vec2(1.0f, 1.0f);
        vec.set(2.0f, 2.0f);
        vec.set(5.0f, 10.0f);
        assertEquals(5.0f, vec.x);
        assertEquals(10.0f, vec.y);
    }

    /**
     * Test multiple sequential add operations.
     */
    @Test
    void testMultipleAdds() {
        Vec2 vec = new Vec2(1.0f, 1.0f);
        vec.add(new Vec2(1.0f, 1.0f));
        vec.add(new Vec2(3.0f, 4.0f));
        assertEquals(5.0f, vec.x);
        assertEquals(6.0f, vec.y);
    }
}
