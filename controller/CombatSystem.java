package HollowKnight.hollowknight.controller;

import com.badlogic.gdx.math.Rectangle;

import java.util.HashSet;
import java.util.Set;

import HollowKnight.hollowknight.model.Enemy;
import HollowKnight.hollowknight.model.Entity;
import HollowKnight.hollowknight.model.FalseKnight;
import HollowKnight.hollowknight.model.Knight;
import HollowKnight.hollowknight.model.Level;
import HollowKnight.hollowknight.model.enemies.CrystalGuardian;
import HollowKnight.hollowknight.view.GameCamera;

public class CombatSystem {
    private static final float NAIL_REACH = 250f;
    private static final float HIT_IFRAMES = 1.0f;

    private final Knight knight;
    private final EnemyManager enemyManager;
    private final GameCamera camera;
    private final Level level;
    private FalseKnight boss;

    private boolean wasAttacking = false;
    private boolean pogoedThisSwing = false;
    private boolean wasShadowDashing = false;
    private final Set<Enemy> hitThisSwing = new HashSet<>();
    private final Set<Enemy> hitThisDash = new HashSet<>();

    public CombatSystem(Knight knight, EnemyManager enemyManager, GameCamera camera, Level level) {
        this.knight = knight;
        this.enemyManager = enemyManager;
        this.camera = camera;
        this.level = level;
    }

    /** Register the arena boss so down-slashes can pogo off it too. */
    public void setBoss(FalseKnight boss) { this.boss = boss; }

    public void update() {
        applyNailHits();
        applyShadowDash();
        applyLaserHits();
    }

    private void applyNailHits() {
        Entity.State s = knight.getCurrentState();
        boolean attacking = s == Entity.State.ATTACKING || s == Entity.State.ATTACKING_ALT
                         || s == Entity.State.UP_SLASH || s == Entity.State.DOWN_SLASH;

        boolean newSwing = attacking && !wasAttacking;
        wasAttacking = attacking;
        if (newSwing) { hitThisSwing.clear(); pogoedThisSwing = false; }
        if (!attacking) return;

        Rectangle nail = buildNailHitbox(s, knight.getBoundingBox());
        boolean nailHitSomething = false;
        for (Enemy e : enemyManager.getEnemies()) {
            if (e.isDead()) continue;
            if (!nail.overlaps(e.getBoundingBox())) continue;
            nailHitSomething = true;
            if (hitThisSwing.contains(e)) continue;
            hitThisSwing.add(e);
            enemyManager.onPlayerNailHit(e, knight, knight.nailDamage);
            camera.shake(0.08f, 3f);
        }

        if (s == Entity.State.DOWN_SLASH && !knight.onGround && !pogoedThisSwing) {
            boolean pogoTarget = nailHitSomething;
            if (!pogoTarget && boss != null && !boss.isDead() && nail.overlaps(boss.getBounds())) {
                pogoTarget = true;
            }
            if (!pogoTarget) {
                for (Rectangle spike : level.getSpikes()) {
                    if (nail.overlaps(spike)) { pogoTarget = true; break; }
                }
            }
            if (pogoTarget) {
                knight.pogoBounce();
                pogoedThisSwing = true;
                camera.shake(0.1f, 4f);
            }
        }
    }

    private void applyShadowDash() {
        boolean dashing = knight.charmSharpShadow && knight.getCurrentState() == Entity.State.DASHING;
        if (dashing && !wasShadowDashing) hitThisDash.clear();
        wasShadowDashing = dashing;
        if (!dashing) return;
        for (Enemy e : enemyManager.getEnemies()) {
            if (e.isDead() || hitThisDash.contains(e)) continue;
            if (knight.getBoundingBox().overlaps(e.getBoundingBox())) {
                hitThisDash.add(e);
                enemyManager.onPlayerNailHit(e, knight, knight.nailDamage);
                camera.shake(0.06f, 2f);
            }
        }
    }

    private void applyLaserHits() {
        if (knight.invincibleTimer > 0 || knight.getCurrentState() == Entity.State.DEAD) return;
        for (Enemy e : enemyManager.getEnemies()) {
            if (!(e instanceof CrystalGuardian)) continue;
            Rectangle laser = ((CrystalGuardian) e).getLaserBox();
            if (laser != null && laser.overlaps(knight.getBoundingBox())) {
                hurtKnight();
                camera.shake(0.15f, 6f);
                break;
            }
        }
    }

    public void hurtKnight() {
        knight.masks--;
        knight.focusTimer = 0f;
        knight.invincibleTimer = HIT_IFRAMES;
        float kbDir = knight.isFacingRight() ? -1f : 1f;
        knight.getVelocity().x = kbDir * 400f;
        knight.getVelocity().y = 300f;
        if (knight.masks <= 0) {
            knight.setCurrentState(Entity.State.DEAD);
            knight.getVelocity().set(0, 0);
        }
    }

    public Rectangle buildNailHitbox(Entity.State state, Rectangle b) {
        float vPad = b.height * 0.6f;
        float hPad = b.width * 0.6f;
        switch (state) {
            case UP_SLASH:
                return new Rectangle(b.x - hPad, b.y + b.height * 0.5f, b.width + 2f * hPad, NAIL_REACH);
            case DOWN_SLASH:
                return new Rectangle(b.x - hPad, b.y - NAIL_REACH, b.width + 2f * hPad, NAIL_REACH);
            default:
                return knight.isFacingRight()
                        ? new Rectangle(b.x + b.width * 0.5f, b.y - vPad, NAIL_REACH, b.height + 2f * vPad)
                        : new Rectangle(b.x + b.width * 0.5f - NAIL_REACH, b.y - vPad, NAIL_REACH, b.height + 2f * vPad);
        }
    }
}