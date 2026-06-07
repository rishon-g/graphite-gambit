package Game;


import Components.Transform;
import Components.Vec2;
import Entities.*;
import Objects.*;
import Game.PhysicsHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Vector;

import Objects.Ink;

public class PhysicsHandlerTest {
    private PhysicsHandler physics;
    private Vector<Entity> entities;

    private boolean equalsWithEpsilon(float a, float b, float epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    @BeforeEach void setup() {
        entities = new Vector<>();
        PhysicsHandler.CreateHandler(entities, 800, 600);
        physics = PhysicsHandler.getInstance();
    }

    private DummyEntity createDummyMover(float x, float y, float width, float height, float vx, float vy) {
        DummyEntity mover = new DummyEntity(null);
        mover.transform.setPosition(x, y);
        mover.transform.setScale(width, height);
        mover.transform.setVelocity(vx, vy);
        return mover;
    }

    @Test void requestMoveBasicMovement(){
        Entity mover = createDummyMover(100, 100, 50, 50, 100, 0);
        float delta = 1.0f; // 1 second
        
        float startX = mover.transform.position.x;

        physics.requestMove(mover, delta);
        
        // Should move 100 units to the right
        assert(equalsWithEpsilon(startX + 100, mover.transform.position.x, 0.01f));
    }

    @Test void requestMoveNoMovementWithZeroVelocity(){
        Entity mover = createDummyMover(100, 100, 50, 50, 0, 0);

        float startX = mover.transform.position.x;
        float startY = mover.transform.position.y;
        physics.requestMove(mover, 1.0f);
        
        // Should not move at all
        assert(mover.transform.position.x == startX);
        assert(mover.transform.position.y == startY);
    }

    @Test void requestMoveLeftBound(){
        Entity mover = createDummyMover(10, 100, 50, 50, -100, 0);
        
        physics.requestMove(mover, 1.0f);
        
        // Should be clamped at the left boundary (x = 0)
        assert(mover.transform.position.x == 0);
    }

    @Test void requestMoveRightBound(){
        Entity mover = createDummyMover(700, 100, 50, 50, 1000, 0);
        
        physics.requestMove(mover, 1.0f);
        
        // Should be clamped at the right boundary
        assert(mover.transform.position.x + mover.transform.size.x <= 800);
    }

    @Test void requestMoveBottomBound(){
        Entity mover = createDummyMover(100, 10, 50, 50, 0, -100);
        
        physics.requestMove(mover, 1.0f);
        
        // Should be clamped at bottom boundary (y = 0)
        assert(mover.transform.position.y == 0);
    }

    @Test void requestMoveTopBound(){
        Entity mover = createDummyMover(100, 500, 50, 50, 0, 1000);
        
        physics.requestMove(mover, 1.0f);
        
        // Should be clamped at top boundary
        assert(mover.transform.position.y + mover.transform.size.y <= 600);
    }

    @Test void requestMoveCollideFromLeft(){
        
        // Create a solid wall
        Transform wall = new Transform();
        wall.setPosition(200, 100);
        wall.setScale(100, 100);
        physics.solidObjects.add(wall);
        
        // Create a moving object
        Entity mover = createDummyMover(100, 100, 50, 50, 200, 0);
        float startX = mover.transform.position.x;
        float wallLeft = wall.position.x;

        physics.requestMove(mover, 1.0f);

        // Mover must not go through the wall
        assert(mover.transform.position.x + mover.transform.size.x <= wallLeft);

        // And it must not move left of its original position
        assert(mover.transform.position.x >= startX);
    }

    @Test void requestMoveCollideFromRight(){
        // Create a solid wall
        Transform wall = new Transform();
        wall.setPosition(100, 100);
        wall.setScale(100, 100);
        physics.solidObjects.add(wall);
        
        // Create a moving object from the right
        Entity mover = createDummyMover(250, 100, 50, 50, -200, 0);
        float startX = mover.transform.position.x;
        float wallRight = wall.position.x + wall.size.x;

        physics.requestMove(mover, 1.0f);

        // Mover must not go through the wall
        assert(mover.transform.position.x >= wallRight);

        // And it must not move right of its original position
        assert(mover.transform.position.x <= startX);
    }

    @Test void requestMoveCollideFromTop(){
        // Create a solid wall
        Transform wall = new Transform();
        wall.setPosition(100, 100);
        wall.setScale(100, 100);
        physics.solidObjects.add(wall);
        
        // Create a moving object from above
        Entity mover = createDummyMover(100, 250, 50, 50, 0, -200);
        float startY = mover.transform.position.y;
        float wallTop = wall.position.y + wall.size.y;
        
        physics.requestMove(mover, 1.0f);

        // Mover must not go through the wall
        assert(mover.transform.position.y >= wallTop);

        // And it must not move above its original position
        assert(mover.transform.position.y <= startY);
    }

    @Test void requestMoveCollideFromBottom(){
        // Create a solid wall
        Transform wall = new Transform();
        wall.setPosition(100, 200);
        wall.setScale(100, 100);
        physics.solidObjects.add(wall);
        
        // Create a moving object from below
        Entity mover = createDummyMover(100, 50, 50, 50, 0, 200);
        float startY = mover.transform.position.y;
        float wallBottom = wall.position.y;

        physics.requestMove(mover, 1.0f);

        // Mover must not go through the wall
        assert(mover.transform.position.y + mover.transform.size.y <= wallBottom);

        // And it must not move below its original position
        assert(mover.transform.position.y >= startY);
    }

    @Test void requestMoveNoCollisionWithInk(){
        // Create an ink entity (won't collide with movement)
        Ink ink = new Ink(null);
        ink.transform.setScale(50, 50);
        ink.transform.setPosition(500, 500);
        entities.add(ink);
        
        // Create a moving object
        Entity mover = createDummyMover(50, 100, 50, 50, 100, 0);
        
        float startX = mover.transform.position.x;
        physics.requestMove(mover, 1.0f);
        
        // Should move 100 units (ink doesn't block movement)
        assert(equalsWithEpsilon(startX + 100, mover.transform.position.x, 0.01f));
    }

    @Test void requestMoveSmallDeltaTime(){
        Entity mover = createDummyMover(100, 100, 50, 50, 100, 100);
        float delta = 0.1f; // 0.1 seconds
        
        float startX = mover.transform.position.x;
        float startY = mover.transform.position.y;
        physics.requestMove(mover, delta);
        
        // Should move 10 units in both directions (100 * 0.1)
        assert(equalsWithEpsilon(startX + 10, mover.transform.position.x, 0.01f));
        assert(equalsWithEpsilon(startY + 10, mover.transform.position.y, 0.01f));
    }

    @Test void requestMoveDiagonalMovement(){
        Entity mover = createDummyMover(100, 100, 50, 50, 100, 100);
        float delta = 1.0f;
        
        float startX = mover.transform.position.x;
        float startY = mover.transform.position.y;
        physics.requestMove(mover, delta);
        
        // Should move 100 units in both directions
        assert(equalsWithEpsilon(startX + 100, mover.transform.position.x, 0.01f));
        assert(equalsWithEpsilon(startY + 100, mover.transform.position.y, 0.01f));
    }

    @Test void requestMoveDiagonalWithTopCollision(){
        // Create a solid wall blocking horizontal movement
        Transform wall = new Transform();
        wall.setPosition(100, 200);
        wall.setScale(200, 100);
        physics.solidObjects.add(wall);
        
        // Create a moving object moving diagonally
        Entity mover = createDummyMover(100, 100, 50, 50, 100, 100);
        float startX = mover.transform.position.x;
        float startY = mover.transform.position.y;
        float wallBottom = wall.position.y;

        physics.requestMove(mover, 1.0f);

        // Mover may still move right
        assert(mover.transform.position.x >= startX);

        // Mover must not go through the wall from below
        assert(mover.transform.position.y + mover.transform.size.y <= wallBottom);

        // And it must not move below its original position
        assert(mover.transform.position.y >= startY);
    }

    @Test void requestMoveDiagonalWithRightCollision(){
        // Create a solid wall blocking horizontal movement
        Transform wall = new Transform();
        wall.setPosition(200, 100);
        wall.setScale(100, 200);
        physics.solidObjects.add(wall);
        
        // Create a moving object moving diagonally
        Entity mover = createDummyMover(100, 100, 50, 50, 100, 100);
        float startX = mover.transform.position.x;
        float startY = mover.transform.position.y;
        float wallLeft = wall.position.x;

        physics.requestMove(mover, 1.0f);

        // Mover must not go through the wall
        assert(mover.transform.position.x + mover.transform.size.x <= wallLeft);

        // And it must not move left of its original position
        assert(mover.transform.position.x >= startX);

        // Vertical movement is still allowed or unchanged, but must not go below start
        assert(mover.transform.position.y >= startY);
    }

    @Test void requestMoveDiagonalWithTwoCollisions(){
        // Create a solid wall blocking horizontal movement
        Transform wall = new Transform();
        wall.setPosition(200, 100);
        wall.setScale(100, 200);
        physics.solidObjects.add(wall);

        Transform wall2 = new Transform();
        wall2.setPosition(100, 200);
        wall2.setScale(200, 100);
        physics.solidObjects.add(wall2);
        
        // Create a moving object moving diagonally
        Entity mover = createDummyMover(100, 100, 50, 50, 100, 100);
        float startX = mover.transform.position.x;
        float startY = mover.transform.position.y;
        float wallLeft = wall.position.x;
        float wallBottom = wall2.position.y;
        
        physics.requestMove(mover, 1.0f);

        // Mover must not go through the right wall
        assert(mover.transform.position.x + mover.transform.size.x <= wallLeft);

        // Mover must not go through the top wall
        assert(mover.transform.position.y + mover.transform.size.y <= wallBottom);

        // And it must remain within the reachable corner region
        assert(mover.transform.position.x >= startX);
        assert(mover.transform.position.y >= startY);
    }

    // @Test
    void requestMoveWithBlockingEntity(){
        // Create a solid entity
        Entity solidEntity = createDummyMover(200, 100, 100, 100, 0, 0);
        entities.add(solidEntity);

        // Create a moving object
        Entity mover = createDummyMover(100, 100, 50, 50, 100, 0);
        entities.add(mover);

        float startX = mover.transform.position.x;
        float entityLeft = solidEntity.transform.position.x;

        physics.requestMove(mover, 1.0f);

        // Mover must not go through the blocking entity
        assert(mover.transform.position.x + mover.transform.size.x <= entityLeft);

        // And it must not move left of its original position
        assert(mover.transform.position.x >= startX);
    }

    // @Test
    public void shouldBlockEntity(){
        Door door = new Door(null, "left");
        Ink ink = new Ink(null);
        DummyEntity dummy = new DummyEntity(null);
        DummyEntity dummy2 = new DummyEntity(null);
        Player player = new Player(null);

        assert(physics.shouldBlockEntityMovement(player, door) == true);
        assert(physics.shouldBlockEntityMovement(player, ink) == false);
        assert(physics.shouldBlockEntityMovement(player, dummy) == false);
        assert(physics.shouldBlockEntityMovement(player, player) == false);
        assert(physics.shouldBlockEntityMovement(dummy, dummy2) == true);
    }
}
