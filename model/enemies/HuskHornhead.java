package HollowKnight.hollowknight.model.enemies;

import com.badlogic.gdx.math.Rectangle;
import HollowKnight.hollowknight.model.Enemy;
import HollowKnight.hollowknight.model.Level;

public class HuskHornhead extends Enemy {

    public enum State {
        WALK, REST, ANTICIPATE, LUNGE, COOLDOWN, TURN, DEATH_AIR, DEATH_LAND
    }

    private State state = State.WALK;

    private static final float WALK_SPEED = 100f;
    private static final float LUNGE_SPEED = 550f;
    private static final float GRAVITY = 1800f;
    private static final float WALK_DISTANCE = 200f;
    private static final float REST_DURATION = 1.2f;
    private static final float ANTICIPATE_DURATION = 0.4f;
    private static final float COOLDOWN_DURATION = 0.25f;
    private static final float TURN_DURATION = 0.16f;
    private static final float CLIFF_PROBE = 8f;

    private static final float SIGHT_WIDTH = 800f;
    private static final float SIGHT_HEIGHT = 200f;

    private float stateLocalTimer = 0f;
    private float walkedDistance = 0f;

    private boolean deathLanded = false;
    private final Level level;

    public HuskHornhead(float x, float y, Level level) {
        super(x, y, 110f, 120f, 4, 11);
        this.level = level;
    }

    public State getState() { return state; }

    private void setState(State s) {
        if (state != s) { state = s; stateLocalTimer = 0f; resetStateTime(); }
    }

    @Override
    public void update(float dt, Rectangle playerBounds) {
        if (dead) { updateDead(dt); tickStateTime(dt); return; }

        if (isHurt()) {
            updateHurt(dt);
            applyGravity(dt);
            tickStateTime(dt);
            return;
        }

        switch (state) {
            case WALK: updateWalk(dt, playerBounds); break;
            case REST: updateRest(dt, playerBounds); break;
            case ANTICIPATE: updateAnticipate(dt); break;
            case LUNGE: updateLunge(dt); break;
            case COOLDOWN: updateCooldown(dt); break;
            case TURN: updateTurn(dt); break;
            default: break;
        }

        applyGravity(dt);
        tickStateTime(dt);
    }

    private void updateWalk(float dt, Rectangle playerBounds) {
        if (seesPlayer(playerBounds)) { setState(State.ANTICIPATE); return; }

        float speed = facingRight ? WALK_SPEED : -WALK_SPEED;
        float nextX = boundingBox.x + speed * dt;

        if (hitWall(nextX) || atCliff(nextX)) { startTurn(); return; }

        boundingBox.x = nextX;
        velocity.x = speed;
        walkedDistance += Math.abs(speed * dt);

        if (walkedDistance >= WALK_DISTANCE) { walkedDistance = 0; setState(State.REST); }
    }

    private void updateRest(float dt, Rectangle playerBounds) {
        stateLocalTimer += dt;
        if (seesPlayer(playerBounds)) { setState(State.ANTICIPATE); return; }
        if (stateLocalTimer >= REST_DURATION) setState(State.WALK);
    }

    private void updateAnticipate(float dt) {
        stateLocalTimer += dt;
        velocity.x = 0;
        if (stateLocalTimer >= ANTICIPATE_DURATION) setState(State.LUNGE);
    }

    private void updateLunge(float dt) {
        float speed = facingRight ? LUNGE_SPEED : -LUNGE_SPEED;
        float nextX = boundingBox.x + speed * dt;

        if (hitWall(nextX) || atCliff(nextX)) { setState(State.COOLDOWN); return; }

        boundingBox.x = nextX;
        velocity.x = speed;
    }

    private void updateCooldown(float dt) {
        stateLocalTimer += dt;
        velocity.x = 0;
        if (stateLocalTimer >= COOLDOWN_DURATION) startTurn();
    }

    private void updateTurn(float dt) {
        stateLocalTimer += dt;
        if (stateLocalTimer >= TURN_DURATION) {
            facingRight = !facingRight;
            setState(State.WALK);
        }
    }

    private void updateDead(float dt) {
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

    private boolean seesPlayer(Rectangle playerBounds) {
        float sightX = facingRight ? boundingBox.x + boundingBox.width : boundingBox.x - SIGHT_WIDTH;
        Rectangle sightBox = new Rectangle(sightX, boundingBox.y, SIGHT_WIDTH, SIGHT_HEIGHT);
        return sightBox.overlaps(playerBounds);
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

    @Override
    protected void onDeath() {
        velocity.x = 0;
        deathLanded = false;
        setState(State.DEATH_AIR);
    }

    @Override
    protected void onRespawn() {
        state = State.WALK;
        deathLanded = false;
        walkedDistance = 0f;
        facingRight = true;
    }
}