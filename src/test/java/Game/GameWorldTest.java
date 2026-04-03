package Game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.graphics.OrthographicCamera;

import Components.Transform;
import Components.Vec2;
import Entities.Entity;
import Entities.Eraser;
import Entities.PencilSharpener;
import Entities.Player;
import Objects.Door;
import Objects.Ink;
import Objects.Pickup;
import Objects.WhiteOut;
import Screens.GameScreen;
import utils.GameTest;

import static org.mockito.Mockito.*;

public class GameWorldTest extends GameTest {
    GameWorld world;
    GameScreen mockScreen;
    GdxGame game;
    AudioManager mockAudio;
    int tileSize = GameWorld.getTileSize();

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
        assert(equalsWithEpsilon(world.width - testTransform.size.x, testTransform.position.x, 0.01f));

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
        assert(equalsWithEpsilon(world.height - testTransform.size.y, testTransform.position.y, 0.01f));
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
        float wallLeft = wall.position.x;

        world.requestMove(mover, 1.0f);

        // Mover must not go through the wall
        assert(mover.position.x + mover.size.x <= wallLeft);

        // And it must not move left of its original position
        assert(mover.position.x >= startX);
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
        float wallRight = wall.position.x + wall.size.x;

        world.requestMove(mover, 1.0f);

        // Mover must not go through the wall
        assert(mover.position.x >= wallRight);

        // And it must not move right of its original position
        assert(mover.position.x <= startX);
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
        float wallTop = wall.position.y + wall.size.y;
        
        world.requestMove(mover, 1.0f);

        // Mover must not go through the wall
        assert(mover.position.y >= wallTop);

        // And it must not move above its original position
        assert(mover.position.y <= startY);
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
        float wallBottom = wall.position.y;

        world.requestMove(mover, 1.0f);

        // Mover must not go through the wall
        assert(mover.position.y + mover.size.y <= wallBottom);

        // And it must not move below its original position
        assert(mover.position.y >= startY);
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
        float wallBottom = wall.position.y;

        world.requestMove(mover, 1.0f);

        // Mover may still move right
        assert(mover.position.x >= startX);

        // Mover must not go through the wall from below
        assert(mover.position.y + mover.size.y <= wallBottom);

        // And it must not move below its original position
        assert(mover.position.y >= startY);
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
        float wallLeft = wall.position.x;

        world.requestMove(mover, 1.0f);

        // Mover must not go through the wall
        assert(mover.position.x + mover.size.x <= wallLeft);

        // And it must not move left of its original position
        assert(mover.position.x >= startX);

        // Vertical movement is still allowed or unchanged, but must not go below start
        assert(mover.position.y >= startY);
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
        float wallLeft = wall.position.x;
        float wallBottom = wall2.position.y;
        
        world.requestMove(mover, 1.0f);

        // Mover must not go through the right wall
        assert(mover.position.x + mover.size.x <= wallLeft);

        // Mover must not go through the top wall
        assert(mover.position.y + mover.size.y <= wallBottom);

        // And it must remain within the reachable corner region
        assert(mover.position.x >= startX);
        assert(mover.position.y >= startY);
    }

    @Test void requestMoveWithBlockingEntities(){
        world.setDimensions(800, 600);
        
        // Create a solid entity
        DummyEntity solidEntity = new DummyEntity(world);
        solidEntity.transform.setPosition(200, 100);
        solidEntity.transform.setScale(100, 100);
        world.addEntity(solidEntity);
        DummyEntity solidEntity2 = new DummyEntity(world);
        solidEntity2.transform.setPosition(100, 200);
        solidEntity2.transform.setScale(100, 100);
        world.addEntity(solidEntity2);

        // Create a moving object
        DummyEntity mover = new DummyEntity(world);
        mover.transform.setPosition(100, 100);
        mover.transform.setScale(50, 50);
        mover.transform.setVelocity(100, 100); // Move right toward the solid entity
        world.addEntity(mover);

        float startX = mover.transform.position.x;
        float startY = mover.transform.position.y;

        world.requestMove(mover.transform, 1.0f);

        // Mover must not go through the blocking entity
        assert(equalsWithEpsilon(startX + 50, mover.transform.position.x, 0.01f));
        assert(equalsWithEpsilon(startY + 50, mover.transform.position.y, 0.01f));
    }

    @Test void isTouchingPlayer(){
        world.setDimensions(800, 600);
        world.player = new Player(world);
        world.player.transform.setPosition(100, 100);
        world.player.transform.setScale(100, 100);

        Transform other = new Transform();
        other.setPosition(140, 140); // Overlaps with player 
        other.setScale(20, 20);

        assert(world.isTouchingPlayer(other) == true);
    }

    @Test void isNotTouchingPlayer(){
        world.setDimensions(800, 600);
        world.player = new Player(world);
        world.player.transform.setPosition(100, 100);
        world.player.transform.setScale(100, 100);

        Transform other = new Transform();
        other.setPosition(300, 300); // Does not overlap with player
        other.setScale(100, 100);

        assert(world.isTouchingPlayer(other) == false);
    }

    @Test void isTouchingPlayerRightEdge(){
        world.setDimensions(800, 600);
        world.player = new Player(world);
        world.player.transform.setPosition(100, 100);
        world.player.transform.setScale(100, 100);

        Transform other = new Transform();
        other.setPosition(200, 100); // Touching right edge of player
        other.setScale(100, 100);

        Transform other2 = new Transform();
        other2.setPosition(201, 100); // Just past right edge of player
        other2.setScale(100, 100);

        assert(world.isTouchingPlayer(other) == true);
        assert(world.isTouchingPlayer(other2) == false);
    }

    @Test void isTouchingPlayerTopEdge(){
        world.setDimensions(800, 600);
        world.player = new Player(world);
        world.player.transform.setPosition(100, 100);
        world.player.transform.setScale(100, 100);

        Transform other = new Transform();
        other.setPosition(100, 200); // Touching top edge of player
        other.setScale(100, 100);

        Transform other2 = new Transform();
        other2.setPosition(100, 201); // Just past top edge of player
        other2.setScale(100, 100);

        assert(world.isTouchingPlayer(other) == true);
        assert(world.isTouchingPlayer(other2) == false);
    }

    @Test void isTouchingPlayerLeftEdge(){
        world.setDimensions(800, 600);
        world.player = new Player(world);
        world.player.transform.setPosition(200, 100);
        world.player.transform.setScale(100, 100);

        Transform other = new Transform();
        other.setPosition(100, 100); // Touching left edge of player
        other.setScale(100, 100);

        Transform other2 = new Transform();
        other2.setPosition(99, 100); // Just past left edge of player
        other2.setScale(100, 100);

        assert(world.isTouchingPlayer(other) == true);
        assert(world.isTouchingPlayer(other2) == false);
    }


    @Test void isTouchingPlayerBottomEdge(){
        world.setDimensions(800, 600);
        world.player = new Player(world);
        world.player.transform.setPosition(100, 200);
        world.player.transform.setScale(100, 100);

        Transform other = new Transform();
        other.setPosition(100, 100); // Touching bottom edge of player
        other.setScale(100, 100);

        Transform other2 = new Transform();
        other2.setPosition(100, 99); // Just past bottom edge of player
        other2.setScale(100, 100);

        assert(world.isTouchingPlayer(other) == true);
        assert(world.isTouchingPlayer(other2) == false);
    }

    @Test void getEntityByTransform(){
        world.setDimensions(800, 600);
        Entity entity = new Eraser(world);
        entity.transform.setPosition(100, 100);
        entity.transform.setScale(50, 50);
        world.addEntity(entity);

        Entity entity2 = new Player(world);
        entity2.transform.setPosition(0, 0);
        entity2.transform.setScale(50, 50);
        world.addEntity(entity2);

        Entity result = world.getEntityByTransform(entity.transform);
        assert(result == entity);
        assert(result instanceof Eraser);
    }
    
    @Test void getEntityByTransformNoMatch(){
        world.setDimensions(800, 600);
        Entity entity = new Eraser(world);
        entity.transform.setPosition(100, 100);
        entity.transform.setScale(50, 50);
        world.addEntity(entity);

        Transform nonExistentTransform = new Transform();
        nonExistentTransform.setPosition(200, 200);
        nonExistentTransform.setScale(50, 50);

        Entity result = world.getEntityByTransform(nonExistentTransform);
        assert(result == null);
    }

    @Test
    public void testUpdateTimeUp(){
        world.setDimensions(800, 600);
        world.time = 10; // Set time limit to 10 seconds
        world.update(20); // Simulate 11 seconds passing
        verify(mockScreen, times(1)).gameEnd(false); // Should trigger game end with loss
    }

    @Test
    public void testUpdateTimeNotUp(){
        world.setDimensions(800, 600);
        world.time = 10; // Set time limit to 10 seconds
        world.update(5); // Simulate 5 seconds passing
        verify(mockScreen, times(0)).gameEnd(anyBoolean()); // Should not trigger game end
    }

    @Test
    public void testPickupSpawner(){
        for(int i = 0; i < 5; i++){
            world.spawnPoints.add(new Vec2(i, i));
        }

        world.setDimensions(800, 600);
        world.update(6);
        
        // Find at least one pickup spawned
        int pickupCount = 0;
        for(Entity e : world.getEntities()){
            if(e instanceof Pickup){
                pickupCount++;
            }
        }
        assert(pickupCount == 1);
    }

    @Test
    public void testFullPickups(){
        int maxPickups = 5;
        for(int i = 0; i < 10; i++){
            world.spawnPoints.add(new Vec2(i*50, i*50)); // Spread spawn points to avoid occupancy conflicts
        }
        world.setDimensions(800, 600);

        // extra coverage
        DummyEntity d = new DummyEntity(world);
        world.addEntity(d);

        // simulate enough to spawn all pickups (6 updates of 5 seconds each)
        for(int i = 0; i < maxPickups*10; i++){
            world.update(6);
        }

        int pickupCount = 0;
        for(Entity e : world.getEntities()){
            if(e instanceof Pickup){
                pickupCount++;  
            }
        }

        System.out.println("Pickup count: " + pickupCount);
        assert(pickupCount == maxPickups); // Should not exceed max pickups
    }

    @Test
    public void testEntityUpdates(){
        world.setDimensions(800, 600);
        DummyEntity e = new DummyEntity(world);
        world.addEntity(e);
        world.update(1); // Should call update on the eraser entity
        assert(e.updateCount == 1);
    }
    
    @Test
    public void testEntityCollidesPlayer(){
        world.setDimensions(800, 600);
        world.player = new Player(world);
        world.player.transform.setPosition(100, 100);
        world.player.transform.setScale(50, 50);
        
        DummyEntity enemy = new DummyEntity(world);
        enemy.transform.setPosition(120, 120); // Overlaps with player
        enemy.transform.setScale(50, 50);
        world.addEntity(enemy);

        DummyEntity enemy2 = new DummyEntity(world);
        enemy2.transform.setPosition(300, 300); // Does not overlap with player
        enemy2.transform.setScale(50, 50);
        world.addEntity(enemy2);
        
        world.update(1); // Should detect collision and end game
        assert(enemy.collideCount == 1);
        assert(enemy2.collideCount == 0); // Should not collide with player
    }
    
    @Test
    public void testEntityDeaths(){
        world.setDimensions(800, 600);
        DummyEntity e1 = new DummyEntity(world);
        world.addEntity(e1);
        DummyEntity e2 = new DummyEntity(world);
        world.addEntity(e2);
        e1.dead = true; // Mark e1 as dead
        world.update(1); // Should remove dead entities
        assert(world.getEntities().size() == 1);
        assert(world.getEntityByTransform(e1.transform) == null);
        assert(world.getEntityByTransform(e2.transform) == e2);
    }

    @Test public void testPlayerDeath(){
        world.setDimensions(800, 600);
        world.player = new Player(world);
        world.player.transform.setPosition(100, 100);
        world.player.transform.setScale(50, 50);

        Player player = new Player(world);
        world.addEntity(player);
        player.dead = true; // Simulate player death
        
        world.update(1); // Should remove player and call to end game
        verify(mockScreen, times(1)).gameEnd(false); // Should trigger game end with loss
    }

    @Test
    public void testPlayerReplaced(){
        world.setDimensions(800, 600);
        Player player1 = new Player(world);
        world.addEntity(player1);

        Player player2 = new Player(world);
        world.addEntity(player2);

        // Should have both players in entities list, but only the second one is the current player
        assert(!world.getEntities().contains(player1));
        assert(world.getEntities().contains(player2));
    }

    @Test
    public void testCameraFollowPlayer(){
        OrthographicCamera camera = new OrthographicCamera();
        world.setDimensions(800, 600);
        Player player = new Player(world);
        world.addEntity(player);
        player.transform.setPosition(100, 100);
        world.update(1);
        float priorX = camera.position.x;
        world.updateCamera(camera);
        assert(camera.position.x != priorX);
        // Camera should be centered on player (assuming camera logic centers on player position)
    }

    @Test
    public void shouldBlockEntity(){
        Door door = new Door(world, "left");
        Ink ink = new Ink(world);
        DummyEntity dummy = new DummyEntity(world);
        DummyEntity dummy2 = new DummyEntity(world);
        Player player = new Player(world);
        WhiteOut whiteOut = new WhiteOut(world, "");

        world.addEntity(player);
        world.addEntity(dummy);

        assert(world.shouldBlockEntityMovement(player.transform, door) == true);
        assert(world.shouldBlockEntityMovement(player.transform, ink) == false);
        assert(world.shouldBlockEntityMovement(player.transform, dummy) == false);
        assert(world.shouldBlockEntityMovement(player.transform, player) == false);
        assert(world.shouldBlockEntityMovement(player.transform, whiteOut) == false);
        assert(world.shouldBlockEntityMovement(dummy.transform, dummy2) == true);
    }

    @Test public void testEraserCollision(){
        world.setDimensions(800, 600);
        Player player = new Player(world);
        player.transform.setPosition(100, 100);
        world.addEntity(player);

        Eraser eraser = new Eraser(world);
        eraser.transform.setPosition(100, 100);
        world.addEntity(eraser);

        world.update(1);
        assert(player.getHealth() < 100);
    }

    @Test public void testSharpenerCollision(){
        world.setDimensions(800, 600);
        Player player = new Player(world);
        player.transform.setPosition(100, 100);
        world.addEntity(player);

        PencilSharpener sharpener = new PencilSharpener(world);
        sharpener.transform.setPosition(100, 100);
        world.addEntity(sharpener);

        world.update(1);
        assert(player.isStunned == true);
    }
}
