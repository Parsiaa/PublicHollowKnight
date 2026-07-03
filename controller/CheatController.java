package HollowKnight.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Rectangle;
import HollowKnight.hollowknight.model.Enemy;
import HollowKnight.hollowknight.model.Entity;
import HollowKnight.hollowknight.model.Knight;

public class CheatController {
    private static final float NOCLIP_SPEED = 1800f;

    private final Knight knight;
    private final EnemyManager enemyManager;
    private boolean godMode = false;
    private boolean noclip = false;

    public CheatController(Knight knight, EnemyManager enemyManager) {
        this.knight = knight;
        this.enemyManager = enemyManager;
    }

    public boolean isNoclip() { return noclip; }

    public void handle() {
        if (!Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) return;
        if (Gdx.input.isKeyJustPressed(Keys.T)) {
            knight.getBoundingBox().setPosition(2000f, 200f);
            knight.getVelocity().set(0, 0);
        }
        if (Gdx.input.isKeyJustPressed(Keys.N)) noclip = !noclip;
        if (Gdx.input.isKeyJustPressed(Keys.H) && knight.masks < knight.maxMasks) knight.masks++;
        if (Gdx.input.isKeyJustPressed(Keys.R)) knight.soul = knight.maxSoul;
        if (Gdx.input.isKeyJustPressed(Keys.G)) {
            godMode = !godMode;
            knight.invincibleTimer = godMode ? Float.MAX_VALUE : 0f;
        }
        if (Gdx.input.isKeyJustPressed(Keys.K) && enemyManager != null) {
            for (Enemy e : enemyManager.getEnemies()) {
                if (!e.isDead()) e.takeDamage(Integer.MAX_VALUE);
            }
        }
    }

    public void updateNoclip(float dt) {
        float speed = NOCLIP_SPEED;
        Rectangle b = knight.getBoundingBox();
        if (Gdx.input.isKeyPressed(Keys.LEFT)) b.x -= speed * dt;
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) b.x += speed * dt;
        if (Gdx.input.isKeyPressed(Keys.UP)) b.y += speed * dt;
        if (Gdx.input.isKeyPressed(Keys.DOWN)) b.y -= speed * dt;
        knight.getVelocity().set(0, 0);
        knight.setCurrentState(Entity.State.IDLE);
    }
}