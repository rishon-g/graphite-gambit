package Entities;

import Game.GameWorld;
import Objects.Nonplayer;
import Pathfinding.AStar;

import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for enemy entities that move toward the player using
 * tile-based pathfinding.
 *
 * <p>This class provides shared movement logic for mobile enemies such as
 * {@link Eraser} and {@link PencilSharpener}. It periodically rebuilds a path
 * to the player.</p>
 *
 * <p>The path is computed on the world's tile map using the A* algorithm.
 * If the exact player tile is not reachable, the enemy searches for the best
 * reachable nearby tile within a small radius.</p>
 */
public abstract class MobileEnemy extends Nonplayer {

    /**
     * Time interval in seconds between path recalculations.
     */
    private static final float PATH_RECALC_TIME = 0.5f;

    /**
     * Distance threshold used to decide when the enemy has reached the current
     * target tile center.
     */
    private static final float TARGET_DIF = 5f;

    /**
     * Maximum radius, in tiles, around the player to search for an alternative
     * reachable target tile.
     */
    private static final int TARGET_SEARCH_RADIUS = 3;

    /**
     * Current path represented as a list of tile coordinates.
     * Each element is an {@code int[]} of the form {@code {x, y}}.
     */
    protected List<int[]> currentPath = Collections.emptyList();

    /**
     * Index of the next tile in {@link #currentPath} that the enemy should move toward.
     */
    protected int pathIndex = 0;

    /**
     * Accumulated time since the last path rebuild.
     */
    protected float pathTimer = 0f;

    /**
     * Creates a mobile enemy in the given game world.
     *
     * @param world the game world that owns this enemy
     */
    public MobileEnemy(GameWorld world) {
        super(world);
    }

    /**
     * The update method is called every frame to update the state of the MobileEnemy entity.
     *
     * <p>This method performs the following steps:</p>
     * <ol>
     *     <li>Gets the current player from the world.</li>
     *     <li>Allows subclasses to run custom per-frame logic before movement.</li>
     *     <li>Rebuilds the path if needed.</li>
     *     <li>Moves toward the next tile in the current path.</li>
     * </ol>
     *
     * <p>If no player exists or no valid path is available, the enemy stops moving.</p>
     *
     * @param delta time since last update (used for movement and animations)
     */
    @Override
    public void updateInternal(float delta) {
        Player player = world.getPlayer();
        if (player == null) {
            transform.setVelocity(0, 0);
            return;
        }

        beforeMovementUpdate(delta);
        tryAttackPlayer(player);
        pathTimer += delta;

        if (pathTimer >= PATH_RECALC_TIME || currentPath.isEmpty() || pathIndex >= currentPath.size()) {
            rebuildPath(player);
            pathTimer = 0f;
        }

        if (currentPath.isEmpty() || pathIndex >= currentPath.size()) {
            transform.setVelocity(0, 0);
            return;
        }

        while (pathIndex < currentPath.size()) {
            int[] nextTile = currentPath.get(pathIndex);

            float targetX = tileToWorldCenterX(nextTile[0], transform.size.x);
            float targetY = tileToWorldCenterY(nextTile[1], transform.size.y);

            float dx = targetX - transform.position.x;
            float dy = targetY - transform.position.y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist <= TARGET_DIF) {
                transform.setPosition(targetX, targetY);
                pathIndex++;
                continue;
            }
            transform.setVelocity((dx / dist) * getMoveSpeed(), (dy / dist) * getMoveSpeed());
            tryAttackPlayer(player);
            return;
        }

        transform.setVelocity(0, 0);
        tryAttackPlayer(player);
    }

    /**
     * Hook method for subclasses to run custom logic before movement is processed.
     *
     * @param delta time since last update
     */
    protected void beforeMovementUpdate(float delta) {
    }

    /**
     * Returns the movement speed of this enemy.
     *
     * @return movement speed in world units per second
     */
    protected abstract float getMoveSpeed();

    /**
     * Rebuilds the path from the MobileEntity to the player's current tile.
     * @param player the current player target
     */
    protected void rebuildPath(Player player) {
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
     * Finds the best reachable tile near the player.
     *
     * <p>The search starts at the player's tile and expands outward up to
     * {@link #TARGET_SEARCH_RADIUS}. For each candidate tile, the method checks
     * whether it is inside the map, not blocked, and reachable from the enemy's
     * current tile. The shortest valid path found is preferred.</p>
     *
     * @param player the player being chased
     * @param map the tile map where 1 represents a blocked tile
     * @param startX the enemy's current tile x-coordinate
     * @param startY the enemy's current tile y-coordinate
     * @return the chosen target tile or null if no target is reachable
     */
    protected int[] findBestTargetTile(Player player, int[][] map, int startX, int startY) {
        float minX = player.transform.position.x;
        float maxX = player.transform.position.x + player.transform.size.x - 0.01f;
        float minY = player.transform.position.y;
        float maxY = player.transform.position.y + player.transform.size.y - 0.01f;

        int playerMinTileX = worldToTileX(minX);
        int playerMaxTileX = worldToTileX(maxX);
        int playerMinTileY = worldToTileY(minY);
        int playerMaxTileY = worldToTileY(maxY);

        List<int[]> bestPath = Collections.emptyList();
        int[] bestTile = null;

        for (int radius = 0; radius <= TARGET_SEARCH_RADIUS; radius++) {
            int searchMinX = playerMinTileX - radius;
            int searchMaxX = playerMaxTileX + radius;
            int searchMinY = playerMinTileY - radius;
            int searchMaxY = playerMaxTileY + radius;

            for (int targetX = searchMinX; targetX <= searchMaxX; targetX++) {
                for (int targetY = searchMinY; targetY <= searchMaxY; targetY++) {
                    if (!isInsideMap(map, targetX, targetY) || isBlocked(map, targetX, targetY)) {
                        continue;
                    }

                    boolean onBorder =
                            targetX == searchMinX || targetX == searchMaxX ||
                                    targetY == searchMinY || targetY == searchMaxY;

                    if (!onBorder) {
                        continue;
                    }

                    if (targetX == startX && targetY == startY) {
                        if (isPlayerWithinAttackRange(player)) {
                            return new int[]{targetX, targetY};
                        }
                        continue;
                    }

                    List<int[]> path = AStar.findPath(map, startX, startY, targetX, targetY);
                    if (!path.isEmpty() && (bestTile == null || path.size() < bestPath.size())) {
                        bestTile = new int[]{targetX, targetY};
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

    /**
     * Checks whether a tile coordinate lies inside the map bounds.
     *
     * @param map the tile map
     * @param x tile x-coordinate
     * @param y tile y-coordinate
     * @return True if the tile is inside the map bounds; False otherwise
     */
    private boolean isInsideMap(int[][] map, int x, int y) {
        return x >= 0 && y >= 0 && x < map.length && y < map[0].length;
    }

    /**
     * Checks whether the specified tile is blocked.
     *
     * @param map the tile map
     * @param x tile x-coordinate
     * @param y tile y-coordinate
     * @return True if the tile is blocked; False otherwise
     */
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
        return (int) (worldX / GameWorld.getTileSize());
    }

    /**
     * Conversion a world y-coordinate into a tile y-coordinate.
     *
     * @param worldY y position in world units
     * @return tile y index
     */
    private int worldToTileY(float worldY) {
        return (int) (worldY / GameWorld.getTileSize());
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

    protected abstract float getAttackRange();

    /**
     * Attempts to attack the player if they are within this enemy's attack range.
     *
     * @param player the player target
     */
    protected void tryAttackPlayer(Player player) {
        if (player == null) {
            return;
        }

        float enemyLeft = transform.position.x;
        float enemyRight = transform.position.x + transform.size.x;
        float enemyBottom = transform.position.y;
        float enemyTop = transform.position.y + transform.size.y;

        float playerLeft = player.transform.position.x;
        float playerRight = player.transform.position.x + player.transform.size.x;
        float playerBottom = player.transform.position.y;
        float playerTop = player.transform.position.y + player.transform.size.y;

        float gapX = 0f;
        if (enemyRight < playerLeft) {
            gapX = playerLeft - enemyRight;
        } else if (playerRight < enemyLeft) {
            gapX = enemyLeft - playerRight;
        }

        float gapY = 0f;
        if (enemyTop < playerBottom) {
            gapY = playerBottom - enemyTop;
        } else if (playerTop < enemyBottom) {
            gapY = enemyBottom - playerTop;
        }

        float edgeDistance = (float) Math.sqrt(gapX * gapX + gapY * gapY);

        if (edgeDistance <= getAttackRange()) {
            playerCollide(player);
        }
    }

    protected boolean isPlayerWithinAttackRange(Player player) {
        if (player == null) {
            return false;
        }

        float enemyLeft = transform.position.x;
        float enemyRight = transform.position.x + transform.size.x;
        float enemyBottom = transform.position.y;
        float enemyTop = transform.position.y + transform.size.y;

        float playerLeft = player.transform.position.x;
        float playerRight = player.transform.position.x + player.transform.size.x;
        float playerBottom = player.transform.position.y;
        float playerTop = player.transform.position.y + player.transform.size.y;

        float gapX = 0f;
        if (enemyRight < playerLeft) {
            gapX = playerLeft - enemyRight;
        } else if (playerRight < enemyLeft) {
            gapX = enemyLeft - playerRight;
        }

        float gapY = 0f;
        if (enemyTop < playerBottom) {
            gapY = playerBottom - enemyTop;
        } else if (playerTop < enemyBottom) {
            gapY = enemyBottom - playerTop;
        }

        float edgeDistance = (float) Math.sqrt(gapX * gapX + gapY * gapY);
        return edgeDistance <= getAttackRange();
    }
}

