package HollowKnight.hollowknight.controller;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

import HollowKnight.hollowknight.model.Enemy;
import HollowKnight.hollowknight.model.Entity;
import HollowKnight.hollowknight.model.Knight;
import HollowKnight.hollowknight.model.Level;
import HollowKnight.hollowknight.model.enemies.CrystalCrawler;
import HollowKnight.hollowknight.model.enemies.CrystalGuardian;
import HollowKnight.hollowknight.model.enemies.HuskHornhead;
import HollowKnight.hollowknight.model.enemies.Mossfly;
import HollowKnight.hollowknight.view.EnemyRenderer;

public class EnemyManager {

    private static final float RESPAWN_DISTANCE = 600f;
    private static final float PLAYER_HIT_IFRAMES = 1.0f;
    private static final float HURT_DURATION = 0.18f;

    private final List<Enemy> enemies = new ArrayList<>();
    private final EnemyRenderer renderer;
    private final Level level;

    public EnemyManager(EnemyRenderer renderer, Level level) {
        this.renderer = renderer;
        this.level = level;
        spawnEnemies();
    }

    private void spawnEnemies() {
        for (Vector2 pos : level.getMossflySpawns())  enemies.add(new Mossfly(pos.x, pos.y, level));
        for (Vector2 pos : level.getCrawlerSpawns())  enemies.add(new CrystalCrawler(pos.x, pos.y, level));
        for (Vector2 pos : level.getHornheadSpawns()) enemies.add(new HuskHornhead(pos.x, pos.y, level));
        for (Vector2 pos : level.getGuardianSpawns()) enemies.add(new CrystalGuardian(pos.x, pos.y, level));
    }

    public void update(float dt, Knight player) {
        Rectangle playerBox = player.getBoundingBox();

        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                if (distanceToSpawn(enemy, playerBox) > RESPAWN_DISTANCE) enemy.respawn();
                enemy.update(dt, playerBox);
                continue;
            }
            enemy.update(dt, playerBox);

            boolean shadowDash = player.charmSharpShadow
                    && player.getCurrentState() == Entity.State.DASHING;
            if (player.invincibleTimer <= 0
                    && !shadowDash
                    && player.getCurrentState() != Entity.State.DEAD
                    && !enemy.isHurt()
                    && enemy.getBoundingBox().overlaps(playerBox)) {
                hitPlayer(player, enemy);
            }
        }
    }

    public void onPlayerNailHit(Enemy enemy, Knight player, int damage) {
        if (enemy.isDead()) return;
        enemy.takeDamage(damage);
        player.soul = Math.min(player.maxSoul, player.soul + player.soulPerHit);

        float ex = enemy.getBoundingBox().x + enemy.getBoundingBox().width / 2f;
        float px = player.getBoundingBox().x + player.getBoundingBox().width / 2f;
        float dir = (ex >= px) ? 1f : -1f;
        float force = player.charmHeavyBlow ? 520f : 360f;
        enemy.applyKnockback(dir * force, 120f, HURT_DURATION);
    }

    private void hitPlayer(Knight player, Enemy enemy) {
        player.masks--;
        player.focusTimer = 0f;

        float ex = enemy.getBoundingBox().x + enemy.getBoundingBox().width / 2f;
        float px = player.getBoundingBox().x + player.getBoundingBox().width / 2f;
        float kbDir = (px >= ex) ? 1f : -1f;
        float kbForce = player.charmHeavyBlow ? 450f : 300f;
        player.getVelocity().x = kbDir * kbForce;
        player.getVelocity().y = 350f;
        player.invincibleTimer = PLAYER_HIT_IFRAMES;

        if (player.masks <= 0) {
            player.setCurrentState(Entity.State.DEAD);
            player.getVelocity().set(0, 0);
        }
    }

    public void render(SpriteBatch batch) {
        for (Enemy enemy : enemies) renderer.render(batch, enemy);
    }

    public List<Enemy> getEnemies() { return enemies; }

    private float distanceToSpawn(Enemy enemy, Rectangle playerBox) {
        float px = playerBox.x + playerBox.width / 2f;
        float py = playerBox.y + playerBox.height / 2f;
        float dx = px - enemy.getBoundingBox().x;
        float dy = py - enemy.getBoundingBox().y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}