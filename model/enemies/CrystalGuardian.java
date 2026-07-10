package HollowKnight.hollowknight.model.enemies;

import com.badlogic.gdx.math.Rectangle;
import HollowKnight.hollowknight.model.Enemy;
import HollowKnight.hollowknight.model.Level;

public class CrystalGuardian extends Enemy {

    public enum State { IDLE, SHOOT, RUN, EVADE, TURN, DEATH_AIR, DEATH_LAND }

    private State state = State.IDLE;

    private static final float SIGHT_RANGE = 900f;
    private static final float VERTICAL_SIGHT = 240f;
    private static final float AIM_TIME = 0.5f;
    private static final float FIRE_TIME = 0.45f;
    private static final float SHOOT_DURATION = AIM_TIME + FIRE_TIME;
    private static final float RUN_DURATION = 1.3f;
    private static final float RUN_SPEED = 340f;
    private static final float EVADE_DURATION = 0.5f;
    private static final float EVADE_SPEED = 200f;
    private static final float REENGAGE_COOLDOWN = 0.8f;
    private static final float LASER_THICKNESS = 48f;
    private static final float MUZZLE_OFFSET_Y = 200f;
    private static final float GRAVITY = 1800f;
    private static final float CLIFF_PROBE = 8f;

    private float stateTimer = 0f;
    private float reengageTimer = 0f;
    private boolean deathLanded = false;
    private Rectangle laserBox = null;
    private final Level level;

    public CrystalGuardian(float x, float y, Level level) {
        super(x, y, 150f, 150f, 8, 11);
        this.level = level;
    }

    public State getState() { return state; }
    public Rectangle getLaserBox() { return laserBox; }

    private void setState(State s) {
        if (state != s) { state = s; stateTimer = 0f; resetStateTime(); }
    }

    @Override
    public void update(float dt, Rectangle playerBounds) {
        if (dead) { updateDead(dt); tickStateTime(dt); return; }

        if (isHurt()) {
            laserBox = null;
            updateHurt(dt);
            applyGravity(dt);
            tickStateTime(dt);
            return;
        }

        if (reengageTimer > 0f) reengageTimer -= dt;

        switch (state) {
            case IDLE: updateIdle(dt, playerBounds); break;
            case SHOOT: updateShoot(dt, playerBounds); break;
            case RUN: updateRun(dt); break;
            case EVADE: updateEvade(dt); break;
            default: break;
        }
        applyGravity(dt);
        tickStateTime(dt);
    }

    private void updateIdle(float dt, Rectangle playerBounds) {
        velocity.x = 0;
        facePlayer(playerBounds);
        if (reengageTimer <= 0f && seesPlayer(playerBounds)) {
            laserBox = null;
            setState(State.SHOOT);
        }
    }

    private void updateShoot(float dt, Rectangle playerBounds) {
        velocity.x = 0;
        stateTimer += dt;
        if (stateTimer >= AIM_TIME && stateTimer < SHOOT_DURATION) laserBox = buildLaser();
        else laserBox = null;
        if (stateTimer >= SHOOT_DURATION) {
            laserBox = null;
            float px = playerBounds.x + playerBounds.width / 2f;
            float cx = boundingBox.x + boundingBox.width / 2f;
            facingRight = px > cx;
            setState(State.RUN);
        }
    }

    private void updateRun(float dt) {
        stateTimer += dt;
        float speed = facingRight ? RUN_SPEED : -RUN_SPEED;
        float nextX = boundingBox.x + speed * dt;
        if (!hitWall(nextX) && !atCliff(nextX)) {
            boundingBox.x = nextX;
            velocity.x = speed;
        } else {
            velocity.x = 0;
        }
        if (stateTimer >= RUN_DURATION) setState(State.EVADE);
    }

    private void updateEvade(float dt) {
        stateTimer += dt;
        float speed = facingRight ? -EVADE_SPEED : EVADE_SPEED;
        float nextX = boundingBox.x + speed * dt;
        if (!hitWall(nextX) && !atCliff(nextX)) {
            boundingBox.x = nextX;
            velocity.x = speed;
        } else {
            velocity.x = 0;
        }
        if (stateTimer >= EVADE_DURATION) {
            reengageTimer = REENGAGE_COOLDOWN;
            setState(State.IDLE);
        }
    }

    private void updateDead(float dt) {
        laserBox = null;
        if (!deathLanded) {
            velocity.y -= GRAVITY * dt;
            boundingBox.y += velocity.y * dt;
            for (Rectangle plat : level.getPlatforms()) {
                if (boundingBox.overlaps(plat) && velocity.y <= 0) {
                    boundingBox.y = plat.y + plat.height;
                    velocity.y = 0;
                    deathLanded = true;
                    setState(State.DEATH_LAND);
                }
            }
            if (!deathLanded && state != State.DEATH_AIR) setState(State.DEATH_AIR);
        }
    }

    private void applyGravity(float dt) {
        if (!onGround) {
            velocity.y -= GRAVITY * dt;
            boundingBox.y += velocity.y * dt;
        }
        onGround = false;
        for (Rectangle plat : level.getPlatforms()) {
            if (boundingBox.overlaps(plat) && velocity.y <= 0) {
                boundingBox.y = plat.y + plat.height;
                velocity.y = 0;
                onGround = true;
            }
        }
        for (Rectangle spike : level.getSpikes()) {
            if (boundingBox.overlaps(spike) && !dead) { takeDamage(currentHp); break; }
        }
    }

    private void facePlayer(Rectangle p) {
        float px = p.x + p.width / 2f;
        float cx = boundingBox.x + boundingBox.width / 2f;
        facingRight = px > cx;
    }

    private boolean seesPlayer(Rectangle p) {
        float sx = facingRight ? boundingBox.x + boundingBox.width : boundingBox.x - SIGHT_RANGE;
        float cy = boundingBox.y + MUZZLE_OFFSET_Y;
        Rectangle sight = new Rectangle(sx, cy - VERTICAL_SIGHT / 2f, SIGHT_RANGE, VERTICAL_SIGHT);
        return sight.overlaps(p);
    }

    private Rectangle buildLaser() {
        float cy = boundingBox.y + MUZZLE_OFFSET_Y;
        float lx = facingRight ? boundingBox.x + boundingBox.width : boundingBox.x - SIGHT_RANGE;
        return new Rectangle(lx, cy - LASER_THICKNESS / 2f, SIGHT_RANGE, LASER_THICKNESS);
    }

    private boolean hitWall(float nextX) {
        Rectangle probe = new Rectangle(nextX, boundingBox.y, boundingBox.width, boundingBox.height);
        for (Rectangle plat : level.getPlatforms())
            if (plat.height > 50f && probe.overlaps(plat)) return true;
        return false;
    }

    private boolean atCliff(float nextX) {
        float frontX = facingRight ? nextX + boundingBox.width + CLIFF_PROBE : nextX - CLIFF_PROBE;
        Rectangle foot = new Rectangle(frontX, boundingBox.y - 4f, 4f, 4f);
        for (Rectangle plat : level.getPlatforms())
            if (foot.overlaps(plat)) return false;
        return true;
    }

    @Override
    protected void onDeath() {
        velocity.x = 0;
        deathLanded = false;
        laserBox = null;
        setState(State.DEATH_AIR);
    }

    @Override
    protected void onRespawn() {
        state = State.IDLE;
        deathLanded = false;
        facingRight = true;
        laserBox = null;
        reengageTimer = 0f;
        stateTimer = 0f;
    }
}
