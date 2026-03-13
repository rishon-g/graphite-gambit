package Entities;
import Game.GameWorld;
import Pathfinding.AStar;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Collections;
import java.util.List;
/**
 * The Eraser class represents an eraser entity in the game, extending from Entity.
 * The eraser moves toward the player, damaging them on contact, then is destroyed.
 */
public class Eraser extends Nonplayer {
    /** Movement speed in world units per second. */
    private static final float MOVE_SPEED = 100f;
    /** Visual size of the eraser sprite in world units. */
    private static final float DRAW_SIZE = 40f;
    private static final Texture TEXTURE = new Texture(Gdx.files.internal("images/eraser.png"));
    /** How often the eraser rebuilds its path to the player, in seconds. */
    private static final float PATH_RECALC_TIME = 0.25f;
    /** Distance threshold used to decide whether the eraser has already reached the target tile center. */
    private static final float TARGET_DIF = 5f;

    private List<int[]> currentPath = Collections.emptyList();
    private int pathIndex = 0;
    private float pathTimer = 0f;

    public Eraser(GameWorld world) {
        super(world);
        transform.setScale(DRAW_SIZE, DRAW_SIZE);
    }

    /**
     * The update method is called every frame to update the state of the eraser entity.
     * @param delta time since last update (used for movement and animations)
     */
    @Override
    public void updateInternal(float delta) {
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
        float dist = (float)Math.sqrt(dx * dx + dy * dy);

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
            dist = (float)Math.sqrt(dx * dx + dy * dy);
        }

        if (dist > 0f) {
            float vx = (dx / dist) * MOVE_SPEED;
            float vy = (dy / dist) * MOVE_SPEED;
            transform.setVelocity(vx, vy);
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
        if (map == null) {
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
        batch.draw(
                TEXTURE,
                transform.position.x,
                transform.position.y,
                transform.size.x,
                transform.size.y
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
