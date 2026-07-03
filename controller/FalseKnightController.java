package HollowKnight.hollowknight.controller;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

import HollowKnight.hollowknight.model.Entity;
import HollowKnight.hollowknight.model.FalseKnight;
import HollowKnight.hollowknight.model.Knight;
import HollowKnight.hollowknight.model.Level;
import HollowKnight.hollowknight.utils.AudioManager;
import HollowKnight.hollowknight.view.GameCamera;

public class FalseKnightController {
    private static final float GRAVITY = 2200f;
    private static final float CLOSE_RANGE = 280f;
    private static final float FAR_RANGE = 640f;
    private static final float THINK_TIME = 0.5f;

    private static final float MACE_WINDUP = 0.45f;
    private static final float MACE_ACTIVE = 0.22f;
    private static final float MACE_RECOVER = 0.5f;

    private static final float CHARGE_SPEED = 620f;
    private static final float CHARGE_MAX_TIME = 1.6f;
    private static final float CHARGE_RECOVER = 0.6f;

    private static final float LEAP_VY = 1150f;
    private static final float LEAP_VX = 360f;

    private static final float POWER_VY = 1500f;
    private static final float POWER_FALL_GRAVITY = 4200f;

    private static final float STUN_POP_VY = 760f;
    private static final float STUN_POP_VX = 240f;
    private static final float STUN_LAND_TIME = 0.22f;
    private static final float STUN_DAZED_TIME = 1.9f;
    private static final float STUN_RECOVER_TIME = 0.5f;
    private static final float CONTACT_IFRAMES = 1.0f;

    private final FalseKnight boss;
    private final Knight knight;
    private final Level level;
    private final GameCamera camera;
    private final AudioManager audio;
    private final Rectangle arena;

    private boolean active;
    private boolean started;
    private float thinkTimer;
    private boolean launched;
    private boolean powerSlamming;
    private FalseKnight.State lastMove = null;
    private Rectangle activeHitbox = null;
    private boolean wasAttacking = false;

    private float hitWindowTimer = 0f;
    private int hitsInWindow = 0;
    private boolean stunPopDone = false;

    public FalseKnightController(FalseKnight boss, Knight knight, Level level, GameCamera camera, AudioManager audio, Rectangle arena) {
        this.boss = boss;
        this.knight = knight;
        this.level = level;
        this.camera = camera;
        this.audio = audio;
        this.arena = arena;
        this.active = (arena == null);
        this.started = (arena == null);
        this.thinkTimer = THINK_TIME;
    }

    public FalseKnight getBoss() { return boss; }
    public boolean isActive() { return active && !boss.isDead(); }
    public boolean isStarted() { return started; }
    public boolean isDefeated() { return boss.isDead(); }
    public Rectangle getActiveHitbox() { return activeHitbox; }
    public Rectangle getArena() { return arena; }

    public void update(float dt, Rectangle nailHitbox, boolean attacking) {
        if (boss.isDead()) { applyDeathFall(dt); boss.tick(dt); return; }

        if (!active) {
            if (arena != null && knight.getBoundingBox().overlaps(arena)) {
                active = true;
                started = true;
                thinkTimer = THINK_TIME;
                if (camera != null) camera.lock(arena);
            } else {
                return;
            }
        }

        if (hitWindowTimer > 0f) {
            hitWindowTimer -= dt;
            if (hitWindowTimer <= 0f) hitsInWindow = 0;
        }

        faceKnight();
        runStateMachine(dt);
        applyPhysics(dt);
        handleNailHit(nailHitbox, attacking);
        handleContactDamage();
        boss.tick(dt);
    }

    private void faceKnight() {
        if (boss.getState() == FalseKnight.State.CHARGE_RUN) return;
        float bx = boss.getBounds().x + boss.getBounds().width / 2f;
        float kx = knight.getBoundingBox().x + knight.getBoundingBox().width / 2f;
        boss.setFacingRight(kx >= bx);
    }

    private float distanceToKnight() {
        float bx = boss.getBounds().x + boss.getBounds().width / 2f;
        float kx = knight.getBoundingBox().x + knight.getBoundingBox().width / 2f;
        return Math.abs(kx - bx);
    }

    private void runStateMachine(float dt) {
        activeHitbox = null;
        switch (boss.getState()) {
            case IDLE: updateIdle(dt); break;
            case MACE_SLAM: updateMaceSlam(dt); break;
            case CHARGE_RUN: updateChargeRun(dt); break;
            case OFFENSIVE_LEAP: updateLeap(dt, false); break;
            case DEFENSIVE_LEAP: updateLeap(dt, true); break;
            case POWER_SLAM: updatePowerSlam(dt); break;
            case STUN: updateStun(dt); break;
            default: break;
        }
    }

    private void updateIdle(float dt) {
        boss.getVelocity().x = 0f;
        thinkTimer -= dt;
        if (thinkTimer <= 0f) decideNextMove();
    }

    private void decideNextMove() {
        float dist = distanceToKnight();
        List<FalseKnight.State> pool = new ArrayList<>();

        if (dist < CLOSE_RANGE) {
            addWeighted(pool, FalseKnight.State.MACE_SLAM, 5);
            addWeighted(pool, FalseKnight.State.DEFENSIVE_LEAP, 2);
            if (boss.getPhase() >= 2) addWeighted(pool, FalseKnight.State.POWER_SLAM, 3);
        } else if (dist < FAR_RANGE) {
            addWeighted(pool, FalseKnight.State.OFFENSIVE_LEAP, 4);
            addWeighted(pool, FalseKnight.State.MACE_SLAM, 2);
            if (boss.getPhase() >= 2) addWeighted(pool, FalseKnight.State.POWER_SLAM, 3);
        } else {
            addWeighted(pool, FalseKnight.State.CHARGE_RUN, 5);
            addWeighted(pool, FalseKnight.State.OFFENSIVE_LEAP, 2);
        }

        pool = filterAntiSpam(pool);
        enterState(pool.get(MathUtils.random(pool.size() - 1)));
    }

    private void addWeighted(List<FalseKnight.State> pool, FalseKnight.State s, int weight) {
        for (int i = 0; i < weight; i++) pool.add(s);
    }

    private List<FalseKnight.State> filterAntiSpam(List<FalseKnight.State> pool) {
        if (lastMove == null) return pool;
        List<FalseKnight.State> filtered = new ArrayList<>();
        for (FalseKnight.State s : pool) if (s != lastMove) filtered.add(s);
        return filtered.isEmpty() ? pool : filtered;
    }

    private void updateMaceSlam(float dt) {
        float scale = boss.getPhase() >= 2 ? 0.7f : 1f;
        float windup = MACE_WINDUP * scale;
        float active = windup + MACE_ACTIVE;
        float recover = active + MACE_RECOVER * scale;

        boss.getVelocity().x = 0f;
        float t = boss.getStateTime();
        if (t >= windup && t < active) {
            Rectangle b = boss.getBounds();
            float w = 170f;
            float x = boss.isFacingRight() ? b.x + b.width : b.x - w;
            activeHitbox = new Rectangle(x, b.y, w, b.height * 0.7f);
            if (t - dt < windup) {
                if (camera != null) camera.shake(0.18f, 7f);
                playHit();
            }
        }
        if (t >= recover) endMove(THINK_TIME);
    }

    private void updateChargeRun(float dt) {
        float speed = CHARGE_SPEED * (boss.getPhase() >= 2 ? 1.35f : 1f);
        boss.getVelocity().x = boss.isFacingRight() ? speed : -speed;
        activeHitbox = new Rectangle(boss.getBounds());

        boolean hitWall = clampToArenaX();
        if (hitWall || boss.getStateTime() >= CHARGE_MAX_TIME) {
            boss.getVelocity().x = 0f;
            if (hitWall && camera != null) camera.shake(0.25f, 9f);
            endMove(CHARGE_RECOVER);
        }
    }

    private void updateLeap(float dt, boolean defensive) {
        if (!launched) {
            float dir = defensive ? (boss.isFacingRight() ? -1f : 1f) : (boss.isFacingRight() ? 1f : -1f);
            float vx = LEAP_VX * (boss.getPhase() >= 2 ? 1.3f : 1f);
            boss.getVelocity().x = dir * vx;
            boss.getVelocity().y = LEAP_VY;
            boss.setOnGround(false);
            launched = true;
            return;
        }
        if (!defensive && boss.getVelocity().y <= 0f) {
            activeHitbox = new Rectangle(boss.getBounds());
        }
        if (boss.isOnGround()) {
            boss.getVelocity().x = 0f;
            if (!defensive) {
                if (camera != null) camera.shake(0.15f, 6f);
                playHit();
            }
            endMove(THINK_TIME);
        }
    }

    private void updatePowerSlam(float dt) {
        if (!launched) {
            boss.getVelocity().y = POWER_VY;
            boss.getVelocity().x = 0f;
            boss.setOnGround(false);
            launched = true;
            powerSlamming = false;
            return;
        }
        if (!boss.isOnGround() && boss.getVelocity().y <= 0f) powerSlamming = true;
        if (powerSlamming) activeHitbox = new Rectangle(boss.getBounds());
        if (boss.isOnGround()) {
            Rectangle b = boss.getBounds();
            float w = 260f;
            activeHitbox = new Rectangle(b.x - w, b.y, b.width + 2f * w, b.height * 0.4f);
            if (camera != null) camera.shake(0.35f, 12f);
            playHit();
            endMove(THINK_TIME * 1.3f);
        }
    }

    private void updateStun(float dt) {
        activeHitbox = null;
        switch (boss.getStunStage()) {
            case AIR: updateStunAir(dt); break;
            case LAND: updateStunLand(dt); break;
            case DAZED: updateStunDazed(dt); break;
            case RECOVER: updateStunRecover(dt); break;
            default: break;
        }
    }

    private void updateStunAir(float dt) {
        if (!stunPopDone) {
            float bx = boss.getBounds().x + boss.getBounds().width / 2f;
            float kx = knight.getBoundingBox().x + knight.getBoundingBox().width / 2f;
            float away = (bx >= kx) ? 1f : -1f;
            boss.getVelocity().x = away * STUN_POP_VX;
            boss.getVelocity().y = STUN_POP_VY;
            boss.setOnGround(false);
            boss.setVulnerable(false);
            stunPopDone = true;
            return;
        }
        if (boss.isOnGround()) {
            boss.getVelocity().x = 0f;
            boss.setStunStage(FalseKnight.StunStage.LAND);
            boss.restartStateTime();
            if (camera != null) camera.shake(0.45f, 13f);
            playHit();
        }
    }

    private void updateStunLand(float dt) {
        boss.getVelocity().x = 0f;
        boss.setVulnerable(false);
        if (boss.getStateTime() >= STUN_LAND_TIME) {
            boss.setStunStage(FalseKnight.StunStage.DAZED);
            boss.restartStateTime();
        }
    }

    private void updateStunDazed(float dt) {
        boss.getVelocity().x = 0f;
        boss.setVulnerable(true);
        if (boss.getStateTime() >= STUN_DAZED_TIME) {
            boss.setVulnerable(false);
            boss.setStunStage(FalseKnight.StunStage.RECOVER);
            boss.restartStateTime();
        }
    }

    private void updateStunRecover(float dt) {
        boss.getVelocity().x = 0f;
        boss.setVulnerable(false);
        if (boss.getStateTime() >= STUN_RECOVER_TIME) {
            boss.setPhase(2);
            endMove(0.2f);
        }
    }

    private void applyDeathFall(float dt) {
        boss.getVelocity().x = 0f;
        if (boss.isOnGround()) return;
        boss.getVelocity().y -= GRAVITY * dt;
        Rectangle b = boss.getBounds();
        b.y += boss.getVelocity().y * dt;
        clampToArenaX();
        boss.setOnGround(false);
        for (Rectangle plat : level.getPlatforms()) {
            if (boss.getVelocity().y <= 0f && b.overlaps(plat) && b.y + b.height * 0.5f > plat.y) {
                b.y = plat.y + plat.height;
                boss.getVelocity().y = 0f;
                boss.setOnGround(true);
                break;
            }
        }
    }

    private void applyPhysics(float dt) {
        float g = powerSlamming ? POWER_FALL_GRAVITY : GRAVITY;
        boss.getVelocity().y -= g * dt;

        Rectangle b = boss.getBounds();
        b.x += boss.getVelocity().x * dt;
        b.y += boss.getVelocity().y * dt;
        clampToArenaX();

        boss.setOnGround(false);
        for (Rectangle plat : level.getPlatforms()) {
            if (boss.getVelocity().y <= 0f && b.overlaps(plat) && b.y + b.height * 0.5f > plat.y) {
                b.y = plat.y + plat.height;
                boss.getVelocity().y = 0f;
                boss.setOnGround(true);
                powerSlamming = false;
                break;
            }
        }
    }

    private boolean clampToArenaX() {
        if (arena == null) return false;
        Rectangle b = boss.getBounds();
        boolean hit = false;
        if (b.x <= arena.x) { b.x = arena.x; hit = true; }
        if (b.x + b.width >= arena.x + arena.width) { b.x = arena.x + arena.width - b.width; hit = true; }
        return hit;
    }

    private void handleNailHit(Rectangle nailHitbox, boolean attacking) {
        boolean newAttack = attacking && !wasAttacking;
        wasAttacking = attacking;
        if (!newAttack || nailHitbox == null) return;
        if (!nailHitbox.overlaps(boss.getBounds())) return;

        int dmg = knight.nailDamage * (boss.isVulnerable() ? 2 : 1);
        boss.takeDamage(dmg);
        if (boss.isVulnerable()) boss.triggerStunHit();
        knight.soul = Math.min(knight.maxSoul, knight.soul + knight.soulPerHit);
        if (camera != null) camera.shake(0.08f, 4f);
        playHit();

        hitWindowTimer = 0.9f;
        hitsInWindow++;
        maybeTriggerStun();
        maybeDefensiveLeap();
    }

    private void maybeTriggerStun() {
        if (boss.isStunUsed() || boss.isDead()) return;
        if (boss.getHealthFraction() > 0.5f) return;
        boss.setStunUsed(true);
        boss.setStunStage(FalseKnight.StunStage.AIR);
        stunPopDone = false;
        enterState(FalseKnight.State.STUN);
        boss.setVulnerable(false);
        boss.getVelocity().set(0, 0);
        activeHitbox = null;
        hitsInWindow = 0;
        if (camera != null) camera.shake(0.3f, 8f);
    }

    private void maybeDefensiveLeap() {
        if (hitsInWindow < 3) return;
        FalseKnight.State s = boss.getState();
        boolean interruptible = boss.isOnGround()
                && s != FalseKnight.State.STUN
                && s != FalseKnight.State.DEAD
                && s != FalseKnight.State.OFFENSIVE_LEAP
                && s != FalseKnight.State.DEFENSIVE_LEAP
                && s != FalseKnight.State.POWER_SLAM;
        if (!interruptible) return;
        hitsInWindow = 0;
        enterState(FalseKnight.State.DEFENSIVE_LEAP);
    }

    private void handleContactDamage() {
        if (activeHitbox == null) return;
        if (knight.invincibleTimer > 0f) return;
        if (knight.getCurrentState() == Entity.State.DEAD) return;
        if (!activeHitbox.overlaps(knight.getBoundingBox())) return;
        hurtKnight();
    }

    private void hurtKnight() {
        knight.masks--;
        knight.focusTimer = 0f;
        float bx = boss.getBounds().x + boss.getBounds().width / 2f;
        float kx = knight.getBoundingBox().x + knight.getBoundingBox().width / 2f;
        float dir = (kx >= bx) ? 1f : -1f;
        knight.getVelocity().x = dir * 420f;
        knight.getVelocity().y = 360f;
        knight.invincibleTimer = CONTACT_IFRAMES;
        if (knight.masks <= 0) {
            knight.setCurrentState(Entity.State.DEAD);
            knight.getVelocity().set(0, 0);
        }
    }

    private void endMove(float recover) {
        lastMove = boss.getState();
        boss.setState(FalseKnight.State.IDLE);
        boss.getVelocity().x = 0f;
        thinkTimer = recover * (boss.getPhase() >= 2 ? 0.7f : 1f);
        launched = false;
        powerSlamming = false;
    }

    private void enterState(FalseKnight.State s) {
        boss.setState(s);
        launched = false;
        powerSlamming = false;
    }

    private void playHit() {
        if (audio != null) audio.playSound("audio/sfx/boss_hit.ogg");
    }
}