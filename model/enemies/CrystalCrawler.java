package HollowKnight.hollowknight.model.enemies;

import com.badlogic.gdx.math.Rectangle;
import HollowKnight.hollowknight.model.Enemy;
import HollowKnight.hollowknight.model.Level;

public class CrystalCrawler extends Enemy {

    public enum State { WALK, TURN, DEATH_AIR, DEATH_LAND }

    private State state = State.WALK;

    private static final float WALK_SPEED = 120f;
    private static final float GRAVITY = 1800f;
    private static final float CLIFF_PROBE = 8f;
    private static final float TURN_DURATION = 0.22f;
    private static final float DEATH_DURATION = 0.22f;

    private float turnTimer = 0f;
    private float deathTimer = 0f;
    private boolean deathLandPlayed = false;

    private final Level level;

    public CrystalCrawler(float x, float y, Level level) {
        super(x, y, 64f, 64f, 2, 11);
        this.level = level;
        this.facingRight = true;
    }

    public State getState() { return state; }

    private void setState(State s) {
        if (state != s) { state = s; resetStateTime(); }
    }

    @Override
    public void update(float deltaTime, Rectangle playerBounds) {
        if (dead) {
            updateDead(deltaTime);
            tickStateTime(deltaTime);
            return;
        }

        if (isHurt()) {
            updateHurt(deltaTime);
            applyGravity(deltaTime);
            tickStateTime(deltaTime);
            return;
        }

        switch (state) {
            case WALK: updateWalk(deltaTime); break;
            case TURN: updateTurn(deltaTime); break;
            default: break;
        }

        applyGravity(deltaTime);
        tickStateTime(deltaTime);
    }

    private void updateWalk(float deltaTime) {
        float speed = facingRight ? WALK_SPEED : -WALK_SPEED;
        float nextX = boundingBox.x + speed * deltaTime;

        if (hitWall(nextX) || atCliff(nextX)) {
            startTurn();
            return;
        }
        boundingBox.x = nextX;
        velocity.x = speed;
    }

    private void updateTurn(float deltaTime) {
        turnTimer += deltaTime;
        if (turnTimer >= TURN_DURATION) {
            facingRight = !facingRight;
            turnTimer = 0f;
            setState(State.WALK);
        }
    }

    private void updateDead(float deltaTime) {
        if (state == State.DEATH_AIR) {
            velocity.y -= GRAVITY * deltaTime;
            boundingBox.y += velocity.y * deltaTime;

            onGround = false;
            for (Rectangle plat : level.getPlatforms()) {
                if (boundingBox.overlaps(plat) && velocity.y <= 0) {
                    boundingBox.y = plat.y + plat.height;
                    velocity.y = 0;
                    onGround = true;
                }
            }

            if (onGround) {
                deathTimer = 0f;
                setState(State.DEATH_LAND);
            }
        } else if (state == State.DEATH_LAND && !deathLandPlayed) {
            deathTimer += deltaTime;
            if (deathTimer >= DEATH_DURATION) deathLandPlayed = true;
        }
    }

    private void startTurn() {
        setState(State.TURN);
        turnTimer = 0f;
        velocity.x = 0;
    }

    private void applyGravity(float deltaTime) {
        if (!onGround) {
            velocity.y -= GRAVITY * deltaTime;
            boundingBox.y += velocity.y * deltaTime;
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
            if (boundingBox.overlaps(spike) && !dead) {
                takeDamage(currentHp);
                break;
            }
        }
    }

    private boolean hitWall(float nextX) {
        Rectangle probe = new Rectangle(nextX, boundingBox.y, boundingBox.width, boundingBox.height);
        for (Rectangle plat : level.getPlatforms()) {
            if (plat.height > 50f && probe.overlaps(plat)) return true;
        }
        return false;
    }

    private boolean atCliff(float nextX) {
        float frontX = facingRight ? nextX + boundingBox.width + CLIFF_PROBE : nextX - CLIFF_PROBE;
        Rectangle footProbe = new Rectangle(frontX, boundingBox.y - 4f, 4f, 4f);
        for (Rectangle plat : level.getPlatforms()) {
            if (footProbe.overlaps(plat)) return false;
        }
        return true;
    }

    @Override
    protected void onDeath() {
        velocity.set(0, 0);
        deathTimer = 0f;
        deathLandPlayed = false;
        setState(State.DEATH_AIR);
    }

    @Override
    protected void onRespawn() {
        state = State.WALK;
        deathLandPlayed = false;
        deathTimer = 0f;
        turnTimer = 0f;
        facingRight = true;
    }
}