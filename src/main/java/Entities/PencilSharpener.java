package Entities;

import Game.GameWorld;
import Pathfinding.AStar;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Collections;
import java.util.List;

public class PencilSharpener extends Nonplayer {
    private static final float MOVE_SPEED = 150f;
    private static final float DRAW_SIZE = 64f;
    private static final float PATH_RECALC_TIME = 0.5f;
    private static final float TARGET_DIF = 5f;
    private static final int TARGET_SEARCH_RADIUS = 2;

    private static Texture TEXTURE;

    private List<int[]> currentPath = Collections.emptyList();
    private int pathIndex = 0;
    private float pathTimer = 0f;

    private float damageTimer = 0f;

    public PencilSharpener(GameWorld world) {
        super(world);
        transform.setScale(DRAW_SIZE, DRAW_SIZE);

        // TODO Temporary Blue square until we add a real sprite
        if (TEXTURE == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLUE);
            pixmap.fill();
            TEXTURE = new Texture(pixmap);
            pixmap.dispose();
        }
    }

    @Override
    public void updateInternal(float delta) {
        Player player = world.getPlayer();
        if (player == null) {
            transform.setVelocity(0, 0);
            return;
        }

        if (damageTimer > 0) {
            damageTimer -= delta;
        }

        pathTimer += delta;

        // rebuild path logic
        if (pathTimer >= PATH_RECALC_TIME || currentPath.isEmpty() || pathIndex >= currentPath.size()) {
            rebuildPath(player);
            pathTimer = 0f;
        }

        if (currentPath.isEmpty() || pathIndex >= currentPath.size()) {
            transform.setVelocity(0, 0);
            return;
        }

        int[] nextTile = currentPath.get(pathIndex);
        float targetX = (nextTile[0] * GameWorld.getTileSize()) + (GameWorld.getTileSize() - transform.size.x) / 2f;
        float targetY = (nextTile[1] * GameWorld.getTileSize()) + (GameWorld.getTileSize() - transform.size.y) / 2f;

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
            targetX = (nextTile[0] * GameWorld.getTileSize()) + (GameWorld.getTileSize() - transform.size.x) / 2f;
            targetY = (nextTile[1] * GameWorld.getTileSize()) + (GameWorld.getTileSize() - transform.size.y) / 2f;
            dx = targetX - transform.position.x;
            dy = targetY - transform.position.y;
            dist = (float) Math.sqrt(dx * dx + dy * dy);
        }

        if (dist > 0f) {
            transform.setVelocity((dx / dist) * MOVE_SPEED, (dy / dist) * MOVE_SPEED);
        } else {
            transform.setVelocity(0, 0);
        }
    }

    private void rebuildPath(Player player) {
        int[][] map = world.getTilemap();
        if (map == null) return;

        int startX = (int)((transform.position.x + transform.size.x / 2f) / GameWorld.getTileSize());
        int startY = (int)((transform.position.y + transform.size.y / 2f) / GameWorld.getTileSize());

        int endX = (int)((player.transform.position.x + player.transform.size.x / 2f) / GameWorld.getTileSize());
        int endY = (int)((player.transform.position.y + player.transform.size.y / 2f) / GameWorld.getTileSize());

        currentPath = AStar.findPath(map, startX, startY, endX, endY);
        pathIndex = 0;
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        batch.draw(TEXTURE,
                transform.position.x * Game.GdxGame.UNIT_SCALE,
                transform.position.y * Game.GdxGame.UNIT_SCALE,
                transform.size.x * Game.GdxGame.UNIT_SCALE,
                transform.size.y * Game.GdxGame.UNIT_SCALE
        );
    }

    @Override
    public void playerCollide(Player player) {
        // if the player is immune, the sharpener can't grab them!
        if (player.isImmune) {
            return;
        }

        // if the player is NOT stunned yet, trap them
        if (!player.isStunned) {
            player.stun(8f);
            damageTimer = 0.5f; // Wait half a second before the first tick of damage
        }
        // if they ARE trapped, grind away their health
        else {
            if (damageTimer <= 0) {
                player.modifyHealth(-5); // Drain 5 graphite points
                damageTimer = 0.5f;      // Reset the timer to wait another half-second
            }
        }
    }
}