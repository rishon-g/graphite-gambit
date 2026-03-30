package Game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Audio;

import Components.Transform;
import Components.Vec2;
import Entities.Entity;
import Entities.Player;
import Objects.Door;
import Objects.Ink;
import Screens.GameScreen;
import utils.GameTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GameWorldTest extends GameTest {
    GameWorld world;
    GameScreen mockScreen;
    GdxGame game;
    AudioManager mockAudio;

    private boolean equalsWithEpsilon(float a, float b, float epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    @BeforeEach
    void init(){
        mockScreen = mock(GameScreen.class);
        mockAudio = mock(AudioManager.class);
        world = new GameWorld(-1, mockScreen);
    }
    
    @Test void testSetDimensions(){
        world.setDimensions(800, 600);
        assert(world.width == 800);
        assert(world.height == 600);
    }

    @Test void testScore(){
        world.score(10);
        assert(world.getScore() == 10);
    }

    @Test void negativeScore(){
        world.score(-5);
        assert(world.getScore() == -5);
    }

    @Test void multipleScore(){
        world.score(10);
        world.score(20);
        assert(world.getScore() == 30);
    }

    @Test void testFloorDraw(){
        world.setDimensions(100, 100);
        DrawWeight weight = (x, y, brushsize) -> 10;
        world.floorDraw(50, 50, false, 1, weight);
        int posx = (int) 50 / world.getDrawSize(), posy = (int) 50 / world.getDrawSize();
        short[][] floor = world.getDrawmap();
        assert(floor[posx][posy] == 10);
    }

    @Test void testFloorDrawZero(){
        world.setDimensions(100, 100);
        DrawWeight weight = (x, y, brushsize) -> 0;
        world.floorDraw(50, 50, false, 1, weight);
        int posx = (int) 50 / world.getDrawSize(), posy = (int) 50 / world.getDrawSize();
        short[][] floor = world.getDrawmap();
        assert(floor[posx][posy] == 0);
    }

    @Test void testFloorDrawNegative(){
        world.setDimensions(100, 100);
        DrawWeight weight = (x, y, brushsize) -> -5;
        world.floorDraw(50, 50, false, 1, weight);
        int posx = (int) 50 / world.getDrawSize(), posy = (int) 50 / world.getDrawSize();
        short[][] floor = world.getDrawmap();
        assert(floor[posx][posy] == 0);
    }

    @Test void testFloorDrawErase(){
        world.setDimensions(100, 100);
        DrawWeight weight = (x, y, brushsize) -> 10;
        world.floorDraw(50, 50, false, 1, weight);
        DrawWeight eraseWeight = (x, y, brushsize) -> 5;
        world.floorDraw(50, 50, true, 1, eraseWeight);
        int posx = (int) 50 / world.getDrawSize(), posy = (int) 50 / world.getDrawSize();
        short[][] floor = world.getDrawmap();
        assert(floor[posx][posy] == 5);
    }

    @Test void testFloorDrawLargerBrush(){
        world.setDimensions(100, 100);
        DrawWeight weight = (x, y, brushsize) -> 10;
        world.floorDraw(50, 50, false, 2, weight);
        int posx = (int) 50 / world.getDrawSize(), posy = (int) 50 / world.getDrawSize();
        short[][] floor = world.getDrawmap();
        assert(floor[posx][posy] == 10);
        assert(floor[posx+1][posy] == 10);
        assert(floor[posx][posy+1] == 10);
        assert(floor[posx+1][posy+1] == 10);
    }

    @Test void testFloorDrawLargerBrushErase(){
        world.setDimensions(100, 100);
        DrawWeight weight = (x, y, brushsize) -> 10;
        world.floorDraw(50, 50, false, 2, weight);
        DrawWeight eraseWeight = (x, y, brushsize) -> 5;
        world.floorDraw(50, 50, true, 2, eraseWeight);
        int posx = (int) 50 / world.getDrawSize(), posy = (int) 50 / world.getDrawSize();
        short[][] floor = world.getDrawmap();
        assert(floor[posx][posy] == 5);
        assert(floor[posx+1][posy] == 5);
        assert(floor[posx][posy+1] == 5);
        assert(floor[posx+1][posy+1] == 5);
    }

    @Test void testFloorDrawOutOfBoundsLeft(){
        world.setDimensions(100, 100);
        DrawWeight weight = (x, y, brushsize) -> 10;
        world.floorDraw(-10, 50, false, 1, weight);
        // Should not crash, just skip out-of-bounds cells
        assert(world.getDrawmap() != null);
    }

    @Test void testFloorDrawOutOfBoundsRight(){
        world.setDimensions(100, 100);
        DrawWeight weight = (x, y, brushsize) -> 10;
        world.floorDraw(200, 50, false, 1, weight);
        // Should not crash, just skip out-of-bounds cells
        assert(world.getDrawmap() != null);
    }

    @Test void testFloorDrawBrushSize0(){
        world.setDimensions(100, 100);
        DrawWeight weight = (x, y, brushsize) -> 10;
        world.floorDraw(50, 50, false, 0, weight);
        int posx = (int) 50 / world.getDrawSize(), posy = (int) 50 / world.getDrawSize();
        short[][] floor = world.getDrawmap();
        assert(floor[posx][posy] == 10);
    }

    @Test void testFloorDrawMultipleOverlapping(){
        world.setDimensions(100, 100);
        DrawWeight weight1 = (x, y, brushsize) -> 5;
        DrawWeight weight2 = (x, y, brushsize) -> 8;
        world.floorDraw(50, 50, false, 1, weight1);
        world.floorDraw(50, 50, false, 1, weight2);
        int posx = (int) 50 / world.getDrawSize(), posy = (int) 50 / world.getDrawSize();
        short[][] floor = world.getDrawmap();
        assert(floor[posx][posy] == 8); // Max value should be kept
    }

    @Test void testFloorDrawEraseFromZero(){
        world.setDimensions(100, 100);
        DrawWeight eraseWeight = (x, y, brushsize) -> 5;
        world.floorDraw(50, 50, true, 1, eraseWeight);
        int posx = (int) 50 / world.getDrawSize(), posy = (int) 50 / world.getDrawSize();
        short[][] floor = world.getDrawmap();
        assert(floor[posx][posy] == 0); // Should not go below 0
    }

    @Test void testFloorDrawVec2Overload(){
        world.setDimensions(100, 100);
        DrawWeight weight = (x, y, brushsize) -> 10;
        Vec2 position = new Vec2(50, 50);
        world.floorDraw(position, false, 1, weight);
        int posx = (int) 50 / world.getDrawSize(), posy = (int) 50 / world.getDrawSize();
        short[][] floor = world.getDrawmap();
        assert(floor[posx][posy] == 10);
    }

    @Test void testFloorDrawLargeBrushSize(){
        world.setDimensions(100, 100);
        DrawWeight weight = (x, y, brushsize) -> 10;
        world.floorDraw(50, 50, false, 3, weight);
        int posx = (int) 50 / world.getDrawSize(), posy = (int) 50 / world.getDrawSize();
        short[][] floor = world.getDrawmap();
        // Center should be drawn
        assert(floor[posx][posy] == 10);
        // Corners of larger brush should be drawn
        assert(floor[posx+3][posy] == 10);
        assert(floor[posx][posy+3] == 10);
    }

    @Test void testFloorDrawWeightFunction(){
        world.setDimensions(100, 100);
        DrawWeight weight = (x, y, brushsize) -> (short)(Math.abs(x) + Math.abs(y));
        world.floorDraw(50, 50, false, 2, weight);
        int posx = (int) 50 / world.getDrawSize(), posy = (int) 50 / world.getDrawSize();
        short[][] floor = world.getDrawmap();
        // Center (0,0) should have weight 0
        assert(floor[posx][posy] == 0);
        // Adjacent cells should have weight 1
        assert(floor[posx+1][posy] == 1);
        assert(floor[posx][posy+1] == 1);
    }

    @Test void testFloorDrawClamping(){
        world.setDimensions(100, 100);
        DrawWeight weight = (x, y, brushsize) -> 100;
        world.floorDraw(50, 50, false, 1, weight);
        int posx = (int) 50 / world.getDrawSize(), posy = (int) 50 / world.getDrawSize();
        short[][] floor = world.getDrawmap();
        assert(floor[posx][posy] == 10);
    }

    @Test void winGame(){
        world.winGame();
        verify(mockScreen, times(1)).gameEnd(true);
    }

    @Test void plotPointCollected(){
        world.plotpoints = 2;
        world.plotPointCollected();
        assert(world.plotpoints == 1);
        verify(mockScreen, times(1)).collectPlotPoint();
    }

    @Test void allPlotPointsCollected(){
        world.plotpoints = 1; // No plot points to collect
        world.addEntity(new Door(world, "left"));
        world.plotPointCollected();
        assert(world.plotpoints == 0);
        verify(mockScreen, times(1)).collectPlotPoint();
        assert(world.getEntities().get(0).dead == true); // Door should be removed when all plot points are collected
    }

    // ==================== requestMove Tests ====================
    
    @Test void requestMoveBasicMovement(){
        world.setDimensions(800, 600);
        Transform testTransform = new Transform();
        testTransform.setPosition(100, 100);
        testTransform.setScale(50, 50);
        testTransform.setVelocity(100, 0); // Move right at 100 units/sec
        float delta = 1.0f; // 1 second
        
        float startX = testTransform.position.x;
        world.requestMove(testTransform, delta);
        
        // Should move 100 units to the right
        assert(equalsWithEpsilon(startX + 100, testTransform.position.x, 0.01f));
    }

    @Test void requestMoveNoMovementWithZeroVelocity(){
        world.setDimensions(800, 600);
        Transform testTransform = new Transform();
        testTransform.setPosition(100, 100);
        testTransform.setScale(50, 50);
        testTransform.setVelocity(0, 0); // No velocity
        
        float startX = testTransform.position.x;
        float startY = testTransform.position.y;
        world.requestMove(testTransform, 1.0f);
        
        // Should not move at all
        assert(testTransform.position.x == startX);
        assert(testTransform.position.y == startY);
    }

    @Test void requestMoveLeftBound(){
        world.setDimensions(800, 600);
        Transform testTransform = new Transform();
        testTransform.setPosition(10, 100);
        testTransform.setScale(50, 50);
        testTransform.setVelocity(-100, 0); // Move left at 100 units/sec
        float delta = 1.0f;
        
        world.requestMove(testTransform, delta);
        
        // Should be clamped at the left boundary (x = 0)
        assert(testTransform.position.x == 0);
    }

    @Test void requestMoveRightBound(){
        world.setDimensions(800, 600); // width=800, height=600
        Transform testTransform = new Transform();
        testTransform.setPosition(700, 100);
        testTransform.setScale(50, 50);
        testTransform.setVelocity(1000, 0); // Move right
        float delta = 1.0f;
        
        world.requestMove(testTransform, delta);
        
        // Should be clamped at the right boundary (x + size.x = height)
        // Note: requestMove uses height for x boundary check (likely a bug, but we test current behavior)
        assert(testTransform.position.x + testTransform.size.x <= world.height);
    }

    @Test void requestMoveBottomBound(){
        world.setDimensions(800, 600); // width=800, height=600
        Transform testTransform = new Transform();
        testTransform.setPosition(100, 10);
        testTransform.setScale(50, 50);
        testTransform.setVelocity(0, -100); // Move down
        float delta = 1.0f;
        
        world.requestMove(testTransform, delta);
        
        // Should be clamped at bottom boundary (y = 0)
        assert(testTransform.position.y == 0);
    }

    @Test void requestMoveTopBound(){
        world.setDimensions(800, 600); // width=800, height=600
        Transform testTransform = new Transform();
        testTransform.setPosition(100, 500);
        testTransform.setScale(50, 50);
        testTransform.setVelocity(0, 1000); // Move up
        float delta = 1.0f;
        
        world.requestMove(testTransform, delta);
        
        // Should be clamped at top boundary (y + size.y = width)
        // Note: requestMove uses width for y boundary check (likely a bug, but we test current behavior)
        assert(testTransform.position.y + testTransform.size.y <= world.width);
    }

    @Test void requestMoveCollideFromLeft(){
        world.setDimensions(800, 600);
        
        // Create a solid wall
        Transform wall = new Transform();
        wall.setPosition(200, 100);
        wall.setScale(100, 100);
        world.solidObjects.add(wall);
        
        // Create a moving object
        Transform mover = new Transform();
        mover.setPosition(100, 100);
        mover.setScale(50, 50);
        mover.setVelocity(200, 0); // Move right toward the wall
        float startX = mover.position.x;
        
        world.requestMove(mover, 1.0f);
        
        // Should collide with the wall and stop before it
        assert(equalsWithEpsilon(startX+50,mover.position.x, 0.01f));
    }

    @Test void requestMoveCollideFromRight(){
        world.setDimensions(800, 600);
        
        // Create a solid wall
        Transform wall = new Transform();
        wall.setPosition(100, 100);
        wall.setScale(100, 100);
        world.solidObjects.add(wall);
        
        // Create a moving object from the right
        Transform mover = new Transform();
        mover.setPosition(250, 100);
        mover.setScale(50, 50);
        mover.setVelocity(-200, 0); // Move left toward the wall
        float startX = mover.position.x;
        
        world.requestMove(mover, 1.0f);
        
        // Should move about 50 units left (from 200 to wall at 150)
        assert(equalsWithEpsilon(startX - 50, mover.position.x, 0.01f));
    }

    @Test void requestMoveCollideFromTop(){
        world.setDimensions(800, 600);
        
        // Create a solid wall
        Transform wall = new Transform();
        wall.setPosition(100, 100);
        wall.setScale(100, 100);
        world.solidObjects.add(wall);
        
        // Create a moving object from above
        Transform mover = new Transform();
        mover.setPosition(100, 250);
        mover.setScale(50, 50);
        mover.setVelocity(0, -200); // Move down toward the wall
        float startY = mover.position.y;
        
        world.requestMove(mover, 1.0f);
        
        // Should move about 50 units down (from 200 to wall at 150)
        assert(equalsWithEpsilon(startY - 50, mover.position.y, 0.01f));
    }

    @Test void requestMoveCollideFromBottom(){
        world.setDimensions(800, 600);
        
        // Create a solid wall
        Transform wall = new Transform();
        wall.setPosition(100, 200);
        wall.setScale(100, 100);
        world.solidObjects.add(wall);
        
        // Create a moving object from below
        Transform mover = new Transform();
        mover.setPosition(100, 100);
        mover.setScale(50, 50);
        mover.setVelocity(0, 200); // Move up toward the wall
        float startY = mover.position.y;
        
        world.requestMove(mover, 1.0f);
        
        // Should move about 50 units up (from 100 to wall at 150)
        assert(equalsWithEpsilon(startY + 50, mover.position.y, 0.01f));
    }

    @Test void requestMoveNoCollisionWithInk(){
        world.setDimensions(800, 600);
        
        // Create an ink entity (won't collide with movement)
        Ink ink = new Ink(world);
        ink.transform.setScale(50, 50);
        ink.transform.setPosition(500, 500);
        world.addEntity(ink);
        
        // Create a moving object
        Transform mover = new Transform();
        mover.setPosition(50, 100);
        mover.setScale(50, 50);
        mover.setVelocity(100, 0); // Move right toward the ink
        
        float startX = mover.position.x;
        world.requestMove(mover, 1.0f);
        
        // Should move 100 units (ink doesn't block movement)
        assert(equalsWithEpsilon(startX + 100, mover.position.x, 0.01f));
    }

    @Test void requestMoveSmallDeltaTime(){
        world.setDimensions(800, 600);
        Transform testTransform = new Transform();
        testTransform.setPosition(100, 100);
        testTransform.setScale(50, 50);
        testTransform.setVelocity(100, 100); // 100 units/sec in both directions
        float delta = 0.1f; // 0.1 seconds
        
        float startX = testTransform.position.x;
        float startY = testTransform.position.y;
        world.requestMove(testTransform, delta);
        
        // Should move 10 units in both directions (100 * 0.1)
        assert(equalsWithEpsilon(startX + 10, testTransform.position.x, 0.01f));
        assert(equalsWithEpsilon(startY + 10, testTransform.position.y, 0.01f));
    }

    @Test void requestMoveDiagonalMovement(){
        world.setDimensions(800, 600);
        Transform testTransform = new Transform();
        testTransform.setPosition(100, 100);
        testTransform.setScale(50, 50);
        testTransform.setVelocity(100, 100); // Move diagonally
        float delta = 1.0f;
        
        float startX = testTransform.position.x;
        float startY = testTransform.position.y;
        world.requestMove(testTransform, delta);
        
        // Should move 100 units in both directions
        assert(equalsWithEpsilon(startX + 100, testTransform.position.x, 0.01f));
        assert(equalsWithEpsilon(startY + 100, testTransform.position.y, 0.01f));
    }

    @Test void requestMoveDiagonalWithTopCollision(){
        world.setDimensions(800, 600);
        
        // Create a solid wall blocking horizontal movement
        Transform wall = new Transform();
        wall.setPosition(100, 200);
        wall.setScale(200, 100);
        world.solidObjects.add(wall);
        
        // Create a moving object moving diagonally
        Transform mover = new Transform();
        mover.setPosition(100, 100);
        mover.setScale(50, 50);
        mover.setVelocity(100, 100); // Move diagonally right and up
        float startX = mover.position.x;
        float startY = mover.position.y;
        
        world.requestMove(mover, 1.0f);

        // Should stop at wall horizontally (about 50 units right)
        assert(equalsWithEpsilon(startX + 100, mover.position.x, 0.01f));
        // Should still move vertically (about 200 units up)
        assert(equalsWithEpsilon(startY + 50, mover.position.y, 0.01f));
    }

    @Test void requestMoveDiagonalWithRightCollision(){
        world.setDimensions(800, 600);
        
        // Create a solid wall blocking horizontal movement
        Transform wall = new Transform();
        wall.setPosition(200, 100);
        wall.setScale(100, 200);
        world.solidObjects.add(wall);
        
        // Create a moving object moving diagonally
        Transform mover = new Transform();
        mover.setPosition(100, 100);
        mover.setScale(50, 50);
        mover.setVelocity(100, 100); // Move diagonally right and up
        float startX = mover.position.x;
        float startY = mover.position.y;
        
        world.requestMove(mover, 1.0f);

        // Should stop at wall horizontally (about 50 units right)
        assert(equalsWithEpsilon(startX + 50, mover.position.x, 0.01f));
        // Should still move vertically (about 200 units up)
        assert(equalsWithEpsilon(startY + 100, mover.position.y, 0.01f));
    }

    @Test void requestMoveDiagonalWithTwoCollisions(){
        world.setDimensions(800, 600);
        
        // Create a solid wall blocking horizontal movement
        Transform wall = new Transform();
        wall.setPosition(200, 100);
        wall.setScale(100, 200);
        world.solidObjects.add(wall);

        Transform wall2 = new Transform();
        wall2.setPosition(100, 200);
        wall2.setScale(200, 100);
        world.solidObjects.add(wall2);
        
        // Create a moving object moving diagonally
        Transform mover = new Transform();
        mover.setPosition(100, 100);
        mover.setScale(50, 50);
        mover.setVelocity(100, 100); // Move diagonally right and up
        float startX = mover.position.x;
        float startY = mover.position.y;
        
        world.requestMove(mover, 1.0f);

        System.out.println("Final position: " + mover.position.x + ", " + mover.position.y);

        // Should stop at wall horizontally (about 50 units right)
        assert(equalsWithEpsilon(startX + 50, mover.position.x, 0.01f));
        // Should still move vertically (about 200 units up)
        assert(equalsWithEpsilon(startY + 50, mover.position.y, 0.01f));
    }

    
    /*
    TODO test:
    isTouchingPlayer
    update
    getEntityByTransform
     */
}
