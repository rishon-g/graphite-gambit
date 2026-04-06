package Game;

import java.util.Vector;

import com.badlogic.gdx.utils.Array;

import Components.Transform;
import Components.Vec2;
import Entities.Entity;
import Objects.Door;
import Objects.Ink;
import Objects.Nonplayer;
import Objects.WhiteOut;

public class PhysicsHandler {
    static PhysicsHandler instance;
    public Array<Transform> solidObjects;
    public Vector<Entity> entities;
    int width, height;

    private PhysicsHandler(Vector<Entity> entities, int width, int height) {
        solidObjects = new Array<Transform>();
        this.entities = entities;
        this.width = (int) width;
        this.height = (int) height;
    }

    public static void CreateHandler(Vector<Entity> entities, int width, int height) {
        instance = new PhysicsHandler(entities, width, height);
    }

    public static PhysicsHandler getInstance() {
        return instance;
    }

    /**
     * Called by and entity of the world in order to request to move.
     * Handles any collisions between the entity and the map, then moves it in an
     * allowed way.
     *
     * @param t the transform of the entity
     */
    public void requestMove(Entity e, float delta) {
        Transform t = e.transform;
        float dx = t.velocity.x * delta;
        float dy = t.velocity.y * delta;

        // grid boundary check, so we cant traverse outside of the map
        if (t.position.x + dx < 0)
            dx = -t.position.x;
        if (t.position.x + t.size.x + dx > width)
            dx = (width) - (t.position.x + t.size.x);
        if (t.position.y + dy < 0)
            dy = -t.position.y;
        if (t.position.y + t.size.y + dy > height)
            dy = (height) - (t.position.y + t.size.y);

        // phantom box to test movements before actually moving the player
        Transform testBox = new Transform();
        testBox.setScale(t.size.x, t.size.y);

        // horizontal movement
        if (dx != 0) {
            testBox.setPosition(t.position.x + dx, t.position.y); // lets see if this collides

            // collide with walls
            for (Transform wall : solidObjects) {
                if (testBox.collides(wall)) {
                    // snap exactly to the edge of the custom rectangle
                    if (dx > 0) { // move right
                        dx = wall.position.x - (t.position.x + t.size.x) - 0.01f;
                        if (dx < 0f) {
                            dx = 0f;
                        }
                    } else { // move left
                        dx = (wall.position.x + wall.size.x) - t.position.x + 0.01f;
                        if (dx > 0f) {
                            dx = 0f;
                        }
                    }
                    testBox.setPosition(t.position.x + dx, t.position.y); // update phantom box
                }
            }

            // collide with blocking entities
            for (Entity entity : entities) {
                if (!shouldBlockEntityMovement(e, entity)) {
                    continue;
                }

                if (testBox.collides(entity.transform)) {
                    if (dx > 0) {
                        dx = entity.transform.position.x - (t.position.x + t.size.x) - 0.01f;
                        if (dx < 0f) {
                            dx = 0f;
                        }
                    } else {
                        dx = (entity.transform.position.x + entity.transform.size.x) - t.position.x + 0.01f;
                        if (dx > 0f) {
                            dx = 0f;
                        }
                    }
                    testBox.setPosition(t.position.x + dx, t.position.y);
                }
            }

            t.move(new Vec2(dx, 0)); // if there is a collision, the testBox provides
            // us with a dx value that is flush with the collision rectangle, otherwise we
            // move as normal
        }

        // vertical movement
        if (dy != 0) {
            testBox.setPosition(t.position.x, t.position.y + dy); // test y move

            // collide with walls
            for (Transform wall : solidObjects) {
                if (testBox.collides(wall)) {
                    // snap exactly to the edge of the custom rectangle
                    if (dy > 0) { // move up
                        dy = wall.position.y - (t.position.y + t.size.y) - 0.01f;
                        if (dy < 0f) {
                            dy = 0f;
                        }
                    } else { // move down
                        dy = (wall.position.y + wall.size.y) - t.position.y + 0.01f;
                        if (dy < 0f) {
                            dy = 0f;
                        }
                    }
                    testBox.setPosition(t.position.x, t.position.y + dy); // update phantom box
                }
            }

            // collide with blocking entities
            for (Entity entity : entities) {
                if (!shouldBlockEntityMovement(e, entity)) {
                    continue;
                }

                if (testBox.collides(entity.transform)) {
                    if (dy > 0) {
                        dy = entity.transform.position.y - (t.position.y + t.size.y) - 0.01f;
                        if (dy < 0f) {
                            dy = 0f;
                        }
                    } else {
                        dy = (entity.transform.position.y + entity.transform.size.y) - t.position.y + 0.01f;
                        if (dy < 0f) {
                            dy = 0f;
                        }
                    }
                    testBox.setPosition(t.position.x, t.position.y + dy);
                }
            }

            t.move(new Vec2(0, dy)); // same thing as before but now for y
        }

        // never allow the object to end up outside the map
        if (t.position.x < 0f) {
            t.position.x = 0f;
        } else if (t.position.x + t.size.x > width) {
            t.position.x = width - t.size.x;
        }

        if (t.position.y < 0f) {
            t.position.y = 0f;
        } else if (t.position.y + t.size.y > height) {
            t.position.y = height - t.size.y;
        }
    }

    /**
     * returns whether or not a given entity should block the movement of a moving
     * entity.
     * 
     * @param mover the transform of the moving entity
     * @param other the entity that is coliding
     * @return true if the movement should be blocked, otherwise false.
     */
    public boolean shouldBlockEntityMovement(Entity mover, Entity other) {
        if (mover == null || other == null) {
            return false;
        }

        if (other instanceof Door) {
            return true;
        }

        if (other instanceof Ink) {
            return false;
        }

        if (other instanceof WhiteOut) {
            return false;
        }

        if (other == mover) {
            return false;
        }

        // Erasers block each other
        if (mover instanceof Nonplayer && other instanceof Nonplayer) {
            return true;
        }
        return false;
    }
}
