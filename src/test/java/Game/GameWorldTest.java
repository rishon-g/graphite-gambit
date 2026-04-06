package Game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.graphics.OrthographicCamera;

import Components.Transform;
import Components.Vec2;
import Entities.Entity;
import Entities.Eraser;
import Entities.Player;
import Objects.Door;
import Objects.Ink;
import Objects.Pickup;
import Screens.GameScreen;
import utils.GameTest;

import static org.mockito.Mockito.*;

public class GameWorldTest extends GameTest {
    GameWorld world;
    GameScreen mockScreen;
    GdxGame game;
    AudioManager mockAudio;
    int tileSize = GameWorld.getTileSize();



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
}
