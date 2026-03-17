package Entities;

import Components.Transform;
import Game.DrawWeight;
import Game.GameWorld;
import Pathfinding.AStar;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Collections;
import java.util.List;
/**
 * The Eraser class represents an eraser entity in the game, extending from Entity.
 * The eraser moves toward the player, damaging them on contact, then is destroyed.
 */
public class Eraser extends Nonplayer {
    /** Movement speed in world units per second. */
    private static final float MOVE_SPEED = 300f;

    /** Visual size of the eraser sprite in world units. */
    private static final float DRAW_SIZE = 60f;

    /** How often the eraser rebuilds its path to the player, in seconds. */
    private static final float PATH_RECALC_TIME = 0.5f;

    /** Distance threshold used to decide whether the eraser reached the next tile target. */
    private static final float TARGET_DIF = 5f;

    // sprites for rendering
    private TextureRegion sprites[];
    int facing = 0;

    // constants for sprite rendering
    private final int DOWN = 0;
    private final int UP = 1;
    private final int RIGHT = 2;
    private final int LEFT = 3;

    private List<int[]> currentPath = Collections.emptyList();
    private int pathIndex = 0;
    private float pathTimer = 0f;

    // erase weighting
    private DrawWeight weight = (x, y, brushsize) -> {
        // Manhattan distance (diamond brush)
        float dist = Math.abs(x) + Math.abs(y);

        // Normalize distance
        float t = Math.min(dist / brushsize, 1.0f);

        // Weight calculation
        return Math.min(5 + (int)(5 * (1.5f - t)), 10);
    };

    public Eraser(GameWorld world) {
        super(world);
        transform.setScale(64, 128);
        Texture png = new Texture("src/main/java/Entities/Assets/Eraser.png");
        TextureRegion[][] sheet = TextureRegion.split(png, 32, 64);
        sprites = new TextureRegion[4];
        for(int i = 0; i < 4; i++){
            sprites[i] = sheet[0][i];
        }
    }

    /**
     * The update method is called every frame to update the state of the eraser entity.
     * @param delta time since last update (used for movement and animations)
     */
    @Override
    public void updateInternal(float delta) {
        world.floorDraw(transform.position.x + transform.size.x/2, transform.position.y, true, 7, weight);

        Player player = world.getPlayer();
        if (player == null) {
            transform.setVelocity(0, 0);
            return;
        }

        pathTimer += delta;

        if (pathTimer >= PATH_RECALC_TIME || currentPath.isEmpty() || pathIndex >= currentPath.size()) {
            rebuildPath(player);
            pathTimer = 0f;
        }

        if (currentPath.isEmpty() || pathIndex >= currentPath.size()) {
            transform.setVelocity(0, 0);
            return;
        }

        // Getting the next tile that we want to go.
        int[] nextTile = currentPath.get(pathIndex);

        // Conversion tile coordinates into the center position of that tile in world space.
        float targetX = tileToWorldCenterX(nextTile[0], transform.size.x);
        float targetY = tileToWorldCenterY(nextTile[1], transform.size.y);

        float dx = targetX - transform.position.x;
        float dy = targetY - transform.position.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist <= TARGET_DIF) {
            transform.setPosition(targetX, targetY);
            pathIndex++;

            if (pathIndex >= currentPath.size()) {
                transform.setVelocity(0, 0);
                return;
            }

            nextTile = currentPath.get(pathIndex);
            targetX = tileToWorldCenterX(nextTile[0], transform.size.x);
            targetY = tileToWorldCenterY(nextTile[1], transform.size.y);

            dx = targetX - transform.position.x;
            dy = targetY - transform.position.y;
            dist = (float) Math.sqrt(dx * dx + dy * dy);
        }

        if (dist > 0f) {
            float vx = (dx / dist) * MOVE_SPEED;
            float vy = (dy / dist) * MOVE_SPEED;
            transform.setVelocity(vx, vy);

            // set new facing
            if(Math.abs(dx) > Math.abs(dy)){
                if(dx > 0){
                    facing = RIGHT;
                }else{
                    facing = LEFT;
                }
            }else{
                if(dy > 0){
                    facing = UP;
                }else{
                    facing = DOWN;
                }
            }
        } else {
            transform.setVelocity(0, 0);
        }
    }

    /**
     * Rebuilds the path from the eraser to the player's current tile.
     * @param player the current player target
     */
    private void rebuildPath(Player player) {
        int[][] map = world.getTilemap();
        if (map == null || map.length == 0 || map[0].length == 0) {
            currentPath = Collections.emptyList();
            pathIndex = 0;
            return;
        }

        int startX = worldToTileX(transform.position.x);
        int startY = worldToTileY(transform.position.y);
        int endX = worldToTileX(player.transform.position.x);
        int endY = worldToTileY(player.transform.position.y);

        currentPath = AStar.findPath(map, startX, startY, endX, endY);
        pathIndex = 0;
    }

    /**
     * Conversion a world x-coordinate into a tile x-coordinate.
     *
     * @param worldX x position in world units
     * @return tile x index
     */
    private int worldToTileX(float worldX) {
        return (int)(worldX / GameWorld.getTileSize());
    }

    /**
     * Conversion a world y-coordinate into a tile y-coordinate.
     *
     * @param worldY y position in world units
     * @return tile y index
     */
    private int worldToTileY(float worldY) {
        return (int)(worldY / GameWorld.getTileSize());
    }

    /**
     * Conversion a tile x-coordinate into the world x-coordinate of the centered sprite position.
     * @param tileX tile x index
     * @param width sprite width
     * @return target world x position
     */
    private float tileToWorldCenterX(int tileX, float width) {
        return tileX * GameWorld.getTileSize() + (GameWorld.getTileSize() - width) / 2f;
    }

    /**
     * Conversion a tile y-coordinate into the world y-coordinate of the centered sprite position.
     * @param tileY tile y index
     * @param height sprite height
     * @return target world y position
     */
    private float tileToWorldCenterY(int tileY, float height) {
        return tileY * GameWorld.getTileSize() + (GameWorld.getTileSize() - height) / 2f;
    }

    /**
     * The render method is called every frame after update to render the eraser entity on the screen.
     */
    @Override
    public void render(SpriteBatch batch, float delta) {
        TextureRegion sprite = sprites[facing];
        batch.draw(
                sprite,
                transform.position.x * Game.GdxGame.UNIT_SCALE,
                transform.position.y * Game.GdxGame.UNIT_SCALE,
                transform.size.x * Game.GdxGame.UNIT_SCALE,
                transform.size.y * Game.GdxGame.UNIT_SCALE
        );
    }

    /**
     * The attack method is called when the eraser attacks the player.
     */
    @Override
    public void playerCollide(Player player) {
        player.modifyHealth(-10);
        dead = true;
    }
}