package Entities;

import Components.Transform;
import Game.AudioManager;
import Game.GameWorld;
import Pathfinding.AStar;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Collections;
import java.util.List;
/**
 * The Eraser class represents an eraser entity in the game, extending from Entity.
 * The eraser moves toward the player and damages them on contact.
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

    /** How far around the player we search for a reachable chase tile. */
    private static final int TARGET_SEARCH_RADIUS = 2;

    private static final int ATTACK_DAMAGE = 10;
    private static final float ATTACK_COOLDOWN = 1.0f;

    private static Texture TEXTURE;


    // respawning
    private float startX = -1;
    private float startY = -1;

    private List<int[]> currentPath = Collections.emptyList();
    private int pathIndex = 0;
    private float pathTimer = 0f;
    private float attackCooldownTimer = 0f;

    public Eraser(GameWorld world) {
        super(world);
        transform.setScale(DRAW_SIZE, DRAW_SIZE);
        TestTexture();
    }

    /**
     * Creates testTexture for eraser.
     */
    private static void TestTexture() {
        if (TEXTURE == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.RED);
            pixmap.fill();
            TEXTURE = new Texture(pixmap);
            pixmap.dispose();
        }
    }

    /**
     * The update method is called every frame to update the state of the eraser entity.
     * @param delta time since last update (used for movement and animations)
     */
    @Override
    public void updateInternal(float delta) {
        if (startX == -1) {
            startX = this.transform.position.x;
            startY = this.transform.position.y;
        }

        Player player = world.getPlayer();
        if (player == null) {
            transform.setVelocity(0, 0);
            return;
        }

        if (attackCooldownTimer > 0f) {
            attackCooldownTimer -= delta;
            if (attackCooldownTimer < 0f) {
                attackCooldownTimer = 0f;
            }
        }

        if (Math.abs(transform.velocity.x) > 1f || Math.abs(transform.velocity.y) > 1f) {
            eraseWithHitbox();
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

        int startX = worldToTileX(transform.position.x + transform.size.x / 2f);
        int startY = worldToTileY(transform.position.y + transform.size.y / 2f);

        int[] targetTile = findBestTargetTile(player, map, startX, startY);
        if (targetTile == null) {
            currentPath = Collections.emptyList();
            pathIndex = 0;
            return;
        }

        int endX = targetTile[0];
        int endY = targetTile[1];

        if (startX == endX && startY == endY) {
            currentPath = Collections.emptyList();
            pathIndex = 0;
            return;
        }

        currentPath = AStar.findPath(map, startX, startY, endX, endY);
        pathIndex = 0;
    }

    /**
     * Finds the best reachable tile to chase near the player.
     */
    private int[] findBestTargetTile(Player player, int[][] map, int startX, int startY) {
        float playerCenterX = player.transform.position.x + player.transform.size.x / 2f;
        float playerCenterY = player.transform.position.y + player.transform.size.y / 2f;
        int baseX = worldToTileX(playerCenterX);
        int baseY = worldToTileY(playerCenterY);

        List<int[]> bestPath = Collections.emptyList();
        int[] bestTile = null;

        for (int radius = 0; radius <= TARGET_SEARCH_RADIUS; radius++) {
            for (int tx = baseX - radius; tx <= baseX + radius; tx++) {
                for (int ty = baseY - radius; ty <= baseY + radius; ty++) {
                    if (!isInsideMap(map, tx, ty) || isBlocked(map, tx, ty)) {
                        continue;
                    }

                    if (Math.max(Math.abs(tx - baseX), Math.abs(ty - baseY)) != radius) {
                        continue;
                    }

                    if (tx == startX && ty == startY) {
                        return new int[]{tx, ty};
                    }

                    List<int[]> path = AStar.findPath(map, startX, startY, tx, ty);
                    if (!path.isEmpty() && (bestTile == null || path.size() < bestPath.size())) {
                        bestTile = new int[]{tx, ty};
                        bestPath = path;
                    }
                }
            }

            if (bestTile != null) {
                return bestTile;
            }
        }

        return null;
    }

    private boolean isInsideMap(int[][] map, int x, int y) {
        return x >= 0 && y >= 0 && x < map.length && y < map[0].length;
    }

    private boolean isBlocked(int[][] map, int x, int y) {
        return map[x][y] == 1;
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
        // Respect cooldown and immunity states
        if (attackCooldownTimer > 0f || player.isStunned || player.isImmune) {
            return;
        }

        // play damage sound
        AudioManager.getInstance().playDamage();

        player.modifyHealth(-ATTACK_DAMAGE);

        // despawn: we teleport the eraser back to where it first appeared in the world
        this.transform.setPosition(startX, startY);


        // reset: we clear the path so it has to re-calculate from the start position
        this.currentPath = java.util.Collections.emptyList();
        this.pathIndex = 0;
        this.pathTimer = 0f;

        // start the cooldown immediately so it doesn't double-attack if it respawns near the player
        attackCooldownTimer = ATTACK_COOLDOWN;
    }

    /**
     * Erases player-drawn floor tiles across the eraser's whole hitbox.
     */
    private void eraseWithHitbox() {
        float left = transform.position.x;
        float bottom = transform.position.y;
        float width = transform.size.x;
        float height = transform.size.y;

        int brushSize = 1;
        float step = 8f;

        for (float x = left; x <= left + width; x += step) {
            for (float y = bottom; y <= bottom + height; y += step) {
                world.floorDraw(x, y, true, brushSize);
            }
        }

        world.floorDraw(left + width / 2f, bottom + height / 2f, true, brushSize);
    }
}