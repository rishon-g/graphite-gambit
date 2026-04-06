package Entities;

import Game.GameWorld;
import Objects.Nonplayer;
import Pathfinding.AStar;
import Pathfinding.RandomPatrol;

import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for enemy entities that move toward the player using
 * tile-based pathfinding.
 *
 * <p>
 * This class provides shared movement logic for mobile enemies such as
 * {@link Eraser} and {@link PencilSharpener}. It periodically rebuilds a path
 * to the player.
 * </p>
 *
 * <p>
 * The path is computed on the world's tile map using the A* algorithm.
 * If the exact player tile is not reachable, the enemy searches for the best
 * reachable nearby tile within a small radius.
 * </p>
 */
public abstract class MobileEnemy extends Nonplayer {

    /**
     * Time interval in seconds between path recalculations.
     */
    private static final float PATH_RECALC_TIME = 0.5f;

    /**
     * Time interval in seconds between patrol path recalculations.
     */
    private static final float PATROL_RECALC_TIME = 1.0f;

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
     * Search radius, in tiles, used when choosing a random patrol target.
     */
    private static final int PATROL_SEARCH_RADIUS = 3;

    /**
     * Detection radius in world units.
     * If the player is within this radius, the enemy switches to chase mode.
     */
    private static final float VISION_RADIUS = 700f;
    private static final float CHASE_LOSE_RADIUS = 1000f;

    /**
     * Enemy movement state.
     */
    protected enum MovementState {
        PATROL,
        CHASE
    }

    /**
     * Range within which the enemy can attack the player.
     * set by subclasses in their constructors.
     */
    protected float ATTACK_RANGE;

    /**
     * Current movement state of the enemy.
     */
    protected MovementState movementState = MovementState.PATROL;

    /**
     * Current path represented as a list of tile coordinates.
     * Each element is an {@code int[]} of the form {@code {x, y}}.
     */
    protected List<int[]> currentPath = Collections.emptyList();

    /**
     * Index of the next tile in {@link #currentPath} that the enemy should move
     * toward.
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
     * The update method is called every frame to update the state of the
     * MobileEnemy entity.
     *
     * <p>
     * This method performs the following steps:
     * </p>
     * <ol>
     * <li>Gets the current player from the world.</li>
     * <li>Allows subclasses to run custom per-frame logic before movement.</li>
     * <li>Rebuilds the path if needed.</li>
     * <li>Moves toward the next tile in the current path.</li>
     * </ol>
     *
     * <p>
     * If no player exists or no valid path is available, the enemy stops moving.
     * </p>
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

        boolean move = beforeMovementUpdate(delta);
        if(!move) {
            transform.setVelocity(0, 0);
            return;
        }

        tryAttackPlayer(player);

        updateMovementState(player);
        pathTimer += delta;

        handlePathRebuild(player);
        followCurrentPath(player);
    }

    /**
     * Hook method for subclasses to run custom logic before movement is processed.
     *
     * @param delta time since last update
     */
    protected abstract boolean beforeMovementUpdate(float delta);

    /**
     * Returns the movement speed of this enemy.
     *
     * @return movement speed in world units per second
     */
    protected abstract float getMoveSpeed();

    /**
     * Updates the current movement state based on player distance.
     *
     * @param player the current player
     */
    protected void updateMovementState(Player player) {
        if (player == null) {
            movementState = MovementState.PATROL;
            return;
        }

        float distance = getCenterDistanceToPlayer(player);

        if (movementState == MovementState.PATROL) {
            if (distance <= VISION_RADIUS) {
                movementState = MovementState.CHASE;
            }
        } else {
            if (distance > CHASE_LOSE_RADIUS) {
                movementState = MovementState.PATROL;
            }
        }
    }

    /**
     * Returns true if the path should be rebuilt for the current movement state.
     *
     * @return true if a rebuild is needed
     */
    protected boolean shouldRebuildPath() {
        if (currentPath.isEmpty() || pathIndex >= currentPath.size()) {
            return true;
        }

        if (movementState == MovementState.CHASE) {
            return pathTimer >= PATH_RECALC_TIME;
        }

        return pathTimer >= PATROL_RECALC_TIME;
    }

    /**
     * Updates the current path if a rebuild is needed for the current movement state.
     *
     * @param player the current player target
     */
    private void handlePathRebuild(Player player) {
        if (!shouldRebuildPath()) {
            return;
        }

        if (movementState == MovementState.CHASE) {
            rebuildChasePath(player);
        } else {
            rebuildPatrolPath();
        }

        pathTimer = 0f;
    }

    /**
     * Moves this enemy along its current path toward the next target tile.
     *
     * <p>If there is no valid path, the enemy stops moving. If the enemy reaches
     * the current target tile, it advances to the next one. If the player comes
     * within attack range while moving, an attack is attempted.</p>
     *
     * @param player the player target
     */
    private void followCurrentPath(Player player) {
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
     * Rebuilds the chase path toward the best reachable tile near the player.
     *
     * @param player the current player target
     */
    protected void rebuildChasePath(Player player) {
        int[][] map = getValidTileMap();
        if (map == null) {
            return;
        }

        int[] startTile = getCurrentTilePosition();
        int startX = startTile[0];
        int startY = startTile[1];

        int[] targetTile = findBestTargetTile(player, map, startX, startY);
        rebuildPathToTarget(startX, startY, targetTile);
    }

    /**
     * Rebuilds a random patrol path when the player is not in vision range.
     */
    protected void rebuildPatrolPath() {
        int[][] map = getValidTileMap();
        if (map == null) {
            return;
        }

        int[] startTile = getCurrentTilePosition();
        int startX = startTile[0];
        int startY = startTile[1];

        for (int attempt = 0; attempt < 4; attempt++) {
            int[] patrolTarget = RandomPatrol.choosePatrolTarget(map, startX, startY, PATROL_SEARCH_RADIUS);
            if (patrolTarget == null) {
                clearCurrentPath();
                transform.setVelocity(0, 0);
                return;
            }

            List<int[]> path = AStar.findPath(map, startX, startY, patrolTarget[0], patrolTarget[1]);
            if (!path.isEmpty()) {
                currentPath = path;
                pathIndex = 0;
                return;
            }
        }

        clearCurrentPath();
        transform.setVelocity(0, 0);
    }

    /**
     * Rebuilds the current path from the given start tile to the given target tile.
     *
     * <p>If the target is {@code null}, or if the start and target tiles are the
     * same, the current path is cleared.</p>
     *
     * @param startX the starting tile x-coordinate
     * @param startY the starting tile y-coordinate
     * @param targetTile the destination tile as {@code {x, y}}, or {@code null}
     */
    private void rebuildPathToTarget(int startX, int startY, int[] targetTile) {
        if (targetTile == null) {
            clearCurrentPath();
            return;
        }

        int endX = targetTile[0];
        int endY = targetTile[1];

        if (startX == endX && startY == endY) {
            clearCurrentPath();
            return;
        }

        currentPath = AStar.findPath(getValidTileMap(), startX, startY, endX, endY);
        pathIndex = 0;
    }

    /**
     * Finds the best reachable tile near the player.
     *
     * <p>
     * The search starts at the player's tile and expands outward up to
     * {@link #TARGET_SEARCH_RADIUS}. For each candidate tile, the method checks
     * whether it is inside the map, not blocked, and reachable from the enemy's
     * current tile. The shortest valid path found is preferred.
     * </p>
     *
     * @param player the player being chased
     * @param map    the tile map where 1 represents a blocked tile
     * @param startX the enemy's current tile x-coordinate
     * @param startY the enemy's current tile y-coordinate
     * @return the chosen target tile or null if no target is reachable
     */
    protected int[] findBestTargetTile(Player player, int[][] map, int startX, int startY) {
        int[] playerBounds = getPlayerTileBounds(player);
        int playerMinTileX = playerBounds[0];
        int playerMaxTileX = playerBounds[1];
        int playerMinTileY = playerBounds[2];
        int playerMaxTileY = playerBounds[3];

        for (int radius = 0; radius <= TARGET_SEARCH_RADIUS; radius++) {
            int searchMinX = playerMinTileX - radius;
            int searchMaxX = playerMaxTileX + radius;
            int searchMinY = playerMinTileY - radius;
            int searchMaxY = playerMaxTileY + radius;

            int[] bestTile = null;
            int bestHeuristic = Integer.MAX_VALUE;

            for (int targetX = searchMinX; targetX <= searchMaxX; targetX++) {
                for (int targetY = searchMinY; targetY <= searchMaxY; targetY++) {
                    if (!isValidTargetCandidate(map, targetX, targetY,
                            searchMinX, searchMaxX, searchMinY, searchMaxY)) {
                        continue;
                    }

                    if (targetX == startX && targetY == startY) {
                        if (isPlayerWithinAttackRange(player)) {
                            return new int[]{targetX, targetY};
                        }
                        continue;
                    }

                    int heuristic = Math.abs(targetX - startX) + Math.abs(targetY - startY);
                    if (heuristic < bestHeuristic) {
                        bestHeuristic = heuristic;
                        bestTile = new int[]{targetX, targetY};
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
     * Returns the tile bounds where is the player.
     *
     * <p>The returned array has the form
     * {@code {minTileX, maxTileX, minTileY, maxTileY}}.</p>
     *
     * @param player the player whose occupied tile bounds are computed
     * @return the tile bounds where is the player
     */
    private int[] getPlayerTileBounds(Player player) {
        float minX = player.transform.position.x;
        float maxX = player.transform.position.x + player.transform.size.x - 0.01f;
        float minY = player.transform.position.y;
        float maxY = player.transform.position.y + player.transform.size.y - 0.01f;

        return new int[]{worldToTileX(minX), worldToTileX(maxX), worldToTileY(minY), worldToTileY(maxY)};
    }

    /**
     * Checks whether a candidate tile is valid for consideration during target search.
     *
     * @param map the tile map
     * @param targetX candidate tile x-coordinate
     * @param targetY candidate tile y-coordinate
     * @param searchMinX minimum x-coordinate of the current search area
     * @param searchMaxX maximum x-coordinate of the current search area
     * @param searchMinY minimum y-coordinate of the current search area
     * @param searchMaxY maximum y-coordinate of the current search area
     * @return true if the tile is inside the map, not blocked, and on the search border
     */
    private boolean isValidTargetCandidate(int[][] map, int targetX, int targetY,
                                           int searchMinX, int searchMaxX,
                                           int searchMinY, int searchMaxY) {
        return isInsideMap(map, targetX, targetY)
                && !isBlocked(map, targetX, targetY)
                && isOnSearchBorder(targetX, targetY, searchMinX, searchMaxX, searchMinY, searchMaxY);
    }

    /**
     * Checks whether a candidate path is a better choice than the current best path.
     *
     * <p>A path is considered better if it is non-empty and either no best tile has
     * been chosen yet or it is shorter than the current best path.</p>
     *
     * @param candidatePath the candidate path to evaluate
     * @param bestTile the current best target tile, or {@code null} if none exists
     * @param bestPath the current best path
     * @return true if the candidate path should replace the current best path
     */
    private boolean isBetterTargetPath(List<int[]> candidatePath, int[] bestTile, List<int[]> bestPath) {
        return !candidatePath.isEmpty() && (bestTile == null || candidatePath.size() < bestPath.size());
    }

    /**
     * Checks whether a target tile lies on the border of the current search ring.
     *
     * @param targetX current candidate tile x-coordinate
     * @param targetY current candidate tile y-coordinate
     * @param searchMinX minimum x-coordinate of the current search area
     * @param searchMaxX maximum x-coordinate of the current search area
     * @param searchMinY minimum y-coordinate of the current search area
     * @param searchMaxY maximum y-coordinate of the current search area
     * @return true if the tile lies on the border of the current search ring
     */
    private boolean isOnSearchBorder(int targetX, int targetY,
                                     int searchMinX, int searchMaxX,
                                     int searchMinY, int searchMaxY) {
        return targetX == searchMinX || targetX == searchMaxX
                || targetY == searchMinY || targetY == searchMaxY;
    }

    /**
     * Attempts to attack the player if they are currently within this enemy's
     * attack range.
     *
     * @param player the player to attack
     */
    protected void tryAttackPlayer(Player player) {
        if (isPlayerWithinAttackRange(player)) {
            playerCollide(player);
        }
    }

    /**
     * Checks whether the player is within this enemy's attack range.
     *
     * <p>The distance is measured as the edge-to-edge distance between
     * the enemy's bounding hitbox and the player's bounding hitbox.</p>
     *
     * @param player the player to test
     * @return true if the player is within {@code ATTACK_RANGE}; false otherwise
     */
    protected boolean isPlayerWithinAttackRange(Player player) {
        if (player == null) {
            return false;
        }
        return getEdgeDistanceToPlayer(player) <= ATTACK_RANGE;
    }

    /**
     * Computes the center-to-center distance between this enemy and the player.
     *
     * @param player the player whose distance from this enemy is measured
     * @return the distance between the centers of the enemy and player
     */
    protected float getCenterDistanceToPlayer(Player player) {
        float enemyCenterX = transform.position.x + transform.size.x / 2f;
        float enemyCenterY = transform.position.y + transform.size.y / 2f;

        float playerCenterX = player.transform.position.x + player.transform.size.x / 2f;
        float playerCenterY = player.transform.position.y + player.transform.size.y / 2f;

        float dx = playerCenterX - enemyCenterX;
        float dy = playerCenterY - enemyCenterY;

        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Computes the edge-to-edge distance between this enemy and the player.
     *
     * <p>If the enemy and player overlap on one axis, the gap on that axis is
     * treated as zero. The final distance is computed from the horizontal and
     * vertical gaps.</p>
     *
     * @param player the player whose distance from this enemy is measured
     * @return the edge-to-edge distance between the enemy and player
     */
    private float getEdgeDistanceToPlayer(Player player) {
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

        return (float) Math.sqrt(gapX * gapX + gapY * gapY);
    }

    /**
     * Clears the current movement path and resets the path index.
     */
    private void clearCurrentPath() {
        currentPath = Collections.emptyList();
        pathIndex = 0;
    }

    /**
     * Returns the current world tile map if it is valid.
     *
     * <p>If the tile map is missing or empty, the current path is cleared and
     * {@code null} is returned.</p>
     *
     * @return the current valid tile map, or {@code null} if unavailable
     */
    private int[][] getValidTileMap() {
        int[][] map = world.getTilemap();
        if (map == null || map.length == 0 || map[0].length == 0) {
            clearCurrentPath();
            return null;
        }
        return map;
    }

    /**
     * Returns this enemy's current tile position.
     *
     * <p>The returned array has the form {@code {x, y}}.</p>
     *
     * @return the current tile coordinates of this enemy
     */
    private int[] getCurrentTilePosition() {
        int startX = worldToTileX(transform.position.x + transform.size.x / 2f);
        int startY = worldToTileY(transform.position.y + transform.size.y / 2f);
        return new int[]{startX, startY};
    }

    /**
     * Checks whether a tile coordinate lies inside the map bounds.
     *
     * @param map the tile map
     * @param x   tile x-coordinate
     * @param y   tile y-coordinate
     * @return True if the tile is inside the map bounds; False otherwise
     */
    private boolean isInsideMap(int[][] map, int x, int y) {
        return x >= 0 && y >= 0 && x < map.length && y < map[0].length;
    }

    /**
     * Checks whether the specified tile is blocked.
     *
     * @param map the tile map
     * @param x   tile x-coordinate
     * @param y   tile y-coordinate
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
     * Conversion a tile x-coordinate into the world x-coordinate of the centered
     * sprite position.
     * 
     * @param tileX tile x index
     * @param width sprite width
     * @return target world x position
     */
    private float tileToWorldCenterX(int tileX, float width) {
        return tileX * GameWorld.getTileSize() + (GameWorld.getTileSize() - width) / 2f;
    }

    /**
     * Conversion a tile y-coordinate into the world y-coordinate of the centered
     * sprite position.
     * 
     * @param tileY  tile y index
     * @param height sprite height
     * @return target world y position
     */
    private float tileToWorldCenterY(int tileY, float height) {
        return tileY * GameWorld.getTileSize() + (GameWorld.getTileSize() - height) / 2f;
    }
}
