package Entities;

import Game.GameWorld;
import Pathfinding.AStar;

import java.util.Collections;
import java.util.List;

public abstract class MobileEnemy extends Nonplayer {
    protected static final float PATH_RECALC_TIME = 0.5f;
    protected static final float TARGET_DIF = 5f;
    protected static final int TARGET_SEARCH_RADIUS = 2;

    protected List<int[]> currentPath = Collections.emptyList();
    protected int pathIndex = 0;
    protected float pathTimer = 0f;

    public MobileEnemy(GameWorld world) {
        super(world);
    }
    /**
     * The update method is called every frame to update the state of the MobileEnemy entity.
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
            return;
        }

        transform.setVelocity(0, 0);
    }
    protected void beforeMovementUpdate(float delta) {
    }

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
     * Finds the best reachable tile to chase near the player.
     */
    protected int[] findBestTargetTile(Player player, int[][] map, int startX, int startY) {
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

    protected boolean isInsideMap(int[][] map, int x, int y) {
        return x >= 0 && y >= 0 && x < map.length && y < map[0].length;
    }

    protected boolean isBlocked(int[][] map, int x, int y) {
        return map[x][y] == 1;
    }

    /**
     * Conversion a world x-coordinate into a tile x-coordinate.
     *
     * @param worldX x position in world units
     * @return tile x index
     */
    protected int worldToTileX(float worldX) {
        return (int) (worldX / GameWorld.getTileSize());
    }

    /**
     * Conversion a world y-coordinate into a tile y-coordinate.
     *
     * @param worldY y position in world units
     * @return tile y index
     */
    protected int worldToTileY(float worldY) {
        return (int) (worldY / GameWorld.getTileSize());
    }

    /**
     * Conversion a tile x-coordinate into the world x-coordinate of the centered sprite position.
     * @param tileX tile x index
     * @param width sprite width
     * @return target world x position
     */
    protected float tileToWorldCenterX(int tileX, float width) {
        return tileX * GameWorld.getTileSize() + (GameWorld.getTileSize() - width) / 2f;
    }

    /**
     * Conversion a tile y-coordinate into the world y-coordinate of the centered sprite position.
     * @param tileY tile y index
     * @param height sprite height
     * @return target world y position
     */
    protected float tileToWorldCenterY(int tileY, float height) {
        return tileY * GameWorld.getTileSize() + (GameWorld.getTileSize() - height) / 2f;
    }
}

