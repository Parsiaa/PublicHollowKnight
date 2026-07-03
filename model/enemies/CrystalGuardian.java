package HollowKnight.hollowknight.model.enemies;

import com.badlogic.gdx.math.Rectangle;
import HollowKnight.hollowknight.model.Enemy;
import HollowKnight.hollowknight.model.Level;

public class CrystalGuardian extends Enemy {

    public enum State { IDLE, SHOOT, EVADE, TURN, DEATH_AIR, DEATH_LAND }

    private State state = State.IDLE;

    private static final float SIGHT_RANGE = 600f;
    private static final float ENRAGE_SPEED = 280f;
    private static final float GRAVITY = 1800f;
    private static final float SHOOT_DURATION = 0.56f;
    private static final float ENRAGE_DURATION = 3.0f;
    private static final float TURN_DURATION = 0.24f;
    private static final float LASER_DURATION = 0.3f;
    private static final float CLIFF_PROBE = 8f;

    private float stateLocalTimer = 0f;
    private boolean deathLanded = false;
    private Rectangle laserBox = null;
    private final Level level;

    public CrystalGuardian(float x, float y, Level level) {
        super(x, y, 72f, 72f, 2, 11);
        this.level = level;
    }

    public State getState() { return state; }
    public Rectangle getLaserBox() { return laserBox; }

    private void setState(State s) {
        if (state != s) { state = s; stateLocalTimer = 0f; resetStateTime(); }
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

        switch (state) {
            case IDLE:  updateIdle(dt, playerBounds);  break;
            case SHOOT: updateShoot(dt, playerBounds); break;
            case EVADE: updateEvade(dt, playerBounds); break;
            case TURN:  updateTurn(dt);                break;
            default:    break;
        }
        applyGravity(dt);
        tickStateTime(dt);
    }

    private void updateIdle(float dt, Rectangle playerBounds) {
        velocity.x = 0;
        if (seesPlayer(playerBounds)) setState(State.SHOOT);
    }

    private void updateShoot(float dt, Rectangle playerBounds) {
        stateLocalTimer += dt;
        velocity.x = 0;
        laserBox = (stateLocalTimer <= LASER_DURATION) ? buildLaser() : null;
        if (stateLocalTimer >= SHOOT_DURATION) {
            laserBox = null;
            setState(State.EVADE);
        }
    }

    private void updateEvade(float dt, Rectangle playerBounds) {
        stateLocalTimer += dt;
        float px = playerBounds.x + playerBounds.width / 2f;
        float ex = boundingBox.x + boundingBox.width / 2f;
        boolean playerIsRight = px > ex;
        if (playerIsRight != facingRight) { startTurn(); return; }

        float speed = facingRight ? ENRAGE_SPEED : -ENRAGE_SPEED;
        float nextX = boundingBox.x + speed * dt;

        if (hitWall(nextX) || atCliff(nextX)) { startTurn(); return; }
        boundingBox.x = nextX;
        velocity.x = speed;

        if (stateLocalTimer >= ENRAGE_DURATION) setState(State.IDLE);
    }

    private void updateTurn(float dt) {
        stateLocalTimer += dt;
        if (stateLocalTimer >= TURN_DURATION) {
            facingRight = !facingRight;
            setState(State.EVADE);
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

    private Rectangle buildLaser() {
        float laserLen = SIGHT_RANGE;
        float lx = facingRight ? boundingBox.x + boundingBox.width : boundingBox.x - laserLen;
        float ly = boundingBox.y + boundingBox.height / 2f - 6f;
        return new Rectangle(lx, ly, laserLen, 12f);
    }

    private boolean seesPlayer(Rectangle playerBounds) {
        return getLaserSightBox().overlaps(playerBounds);
    }

    private Rectangle getLaserSightBox() {
        float lx = facingRight ? boundingBox.x + boundingBox.width : boundingBox.x - SIGHT_RANGE;
        return new Rectangle(lx, boundingBox.y, SIGHT_RANGE, boundingBox.height);
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

    private void startTurn() {
        setState(State.TURN);
        velocity.x = 0;
    }

    @Override protected void onDeath()   { velocity.x = 0; deathLanded = false; setState(State.DEATH_AIR); }
    @Override protected void onRespawn() { state = State.IDLE; deathLanded = false; facingRight = true; laserBox = null; }
}