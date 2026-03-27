package Components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TransformTest {
    Transform input;
    Transform result;

    @BeforeEach
    void init(){
        input = new Transform();
        result = new Transform();
    }

    void transformEquals(){
        assertEquals(input.position.x, result.position.x);
        assertEquals(input.position.y, result.position.y);
        assertEquals(input.size.x, result.size.x);
        assertEquals(input.size.y, result.size.y);
        assertEquals(input.velocity.x, result.velocity.x);
        assertEquals(input.velocity.y, result.velocity.y);
    }

    void vec2Equals(Vec2 a, Vec2 b){
        assertEquals(a.x, b.x);
        assertEquals(a.y, b.y);
    }

    @Test
    void testMove(){
        input.setPosition(10, 10);
        input.move(new Vec2(5, 5));
        result.setPosition(15,15);
        transformEquals();
    }

    @Test
    void testMoveZero(){
        input.setPosition(0, 0);
        input.move(new Vec2(0, 0));
        result.setPosition(0, 0);
        transformEquals();
    }

    @Test
    void testMoveNegative(){
        input.setPosition(10, 10);
        input.move(new Vec2(-3, -2));
        result.setPosition(7, 8);
        transformEquals();
    }

    @Test
    void testMoveLargeValues(){
        input.setPosition(1000, 2000);
        input.move(new Vec2(500, -300));
        result.setPosition(1500, 1700);
        transformEquals();
    }

    @Test
    void testMoveMultipleTimes(){
        input.setPosition(0, 0);
        input.move(new Vec2(1, 1));
        input.move(new Vec2(2, 3));
        input.move(new Vec2(-1, -2));
        result.setPosition(2, 2);
        transformEquals();
    }
    
    @Test
    void testCollides(){
        input.setPosition(0, 0);
        input.setScale(100, 100);

        result.setPosition(50, 50);
        result.setScale(100, 100);

        assert(input.collides(result));
    }

    @Test
    void testCollidesNoOverlap(){
        input.setPosition(0, 0);
        input.setScale(10, 10);

        result.setPosition(20, 20);
        result.setScale(10, 10);

        assert(!input.collides(result));
    }

    @Test
    void testCollidesSideTouching(){
        input.setPosition(0, 0);
        input.setScale(10, 10);

        result.setPosition(10, 0);
        result.setScale(10, 10);

        assert(input.collides(result));
    }

    @Test
    void testCollidesCornerTouching(){
        input.setPosition(0, 0);
        input.setScale(10, 10);

        result.setPosition(10, 10);
        result.setScale(10, 10);

        assert(input.collides(result));
    }

    @Test
    void testCollidesInside(){
        input.setPosition(0, 0);
        input.setScale(20, 20);

        result.setPosition(5, 5);
        result.setScale(5, 5);

        assert(input.collides(result));
    }

    @Test
    void testCollidesPointInside(){
        input.setPosition(0, 0);
        input.setScale(10, 10);

        assert(input.collides(5, 5));
    }

    @Test
    void testCollidesPointOutside(){
        input.setPosition(0, 0);
        input.setScale(10, 10);

        assert(!input.collides(15, 15));
    }

    @Test
    void testCollidesPointOnEdge(){
        input.setPosition(0, 0);
        input.setScale(10, 10);

        assert(input.collides(10, 5)); // On right edge
    }

    @Test
    void testCollidesPointAtCorner(){
        input.setPosition(0, 0);
        input.setScale(10, 10);

        assert(input.collides(10, 10)); // Bottom-right corner
    }

    @Test
    void testGetCorner(){
        input.setPosition(100, 100);
        input.setScale(50, 50);

        vec2Equals(new Vec2(100, 150), input.getCorner(Corner.TL));
        vec2Equals(new Vec2(150, 150), input.getCorner(Corner.TR));
        vec2Equals(new Vec2(100, 100), input.getCorner(Corner.BL));
        vec2Equals(new Vec2(150, 100), input.getCorner(Corner.BR));
    }

    @Test
    void testGetCornerWithZeroSize(){
        input.setPosition(20, 30);
        input.setScale(0, 0);

        Vec2 expected = new Vec2(20, 30);
        vec2Equals(expected, input.getCorner(Corner.TL));
        vec2Equals(expected, input.getCorner(Corner.TR));
        vec2Equals(expected, input.getCorner(Corner.BL));
        vec2Equals(expected, input.getCorner(Corner.BR));
    }

    @Test
    void testGetCornerWithNegativePosition(){
        input.setPosition(-50, -20);
        input.setScale(10, 15);

        vec2Equals(new Vec2(-50, -5), input.getCorner(Corner.TL));
        vec2Equals(new Vec2(-40, -5), input.getCorner(Corner.TR));
        vec2Equals(new Vec2(-50, -20), input.getCorner(Corner.BL));
        vec2Equals(new Vec2(-40, -20), input.getCorner(Corner.BR));
    }
}
