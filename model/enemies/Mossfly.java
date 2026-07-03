package HollowKnight.hollowknight.model.enemies;

import com.badlogic.gdx.math.Rectangle;
import HollowKnight.hollowknight.model.Enemy;
import HollowKnight.hollowknight.model.Level;

public class Mossfly extends Enemy {

    public enum State { SHAKE, TURN_TO_FLY, APPEAR, CHASE, DEATH_AIR, DEATH_LAND }

    private State state = State.SHAKE;

    private static final float GRAVITY = -800f;
    private static final float DETECTION_RANGE = 280f * 4f;
    private static final float CHASE_SPEED = 160f * 4f;
    private static final float TURN_DURATION = 0.24f;
    private static final float APPEAR_DURATION = 0.48f;

    private float transitionTimer = 0f;
    private final Level level;

    public Mossfly(float x, float y, Level level) {
        super(x, y, 48f, 48f, 2, 11);
        this.level = level;
    }

    public State getState() { return state; }

    private void setState(State s) {
        if (state != s) { state = s; resetStateTime(); transitionTimer = 0f; }
    }

    @Override
    public void update(float deltaTime, Rectangle playerBounds) {
        if (dead) { updateDead(deltaTime); tickStateTime(deltaTime); return; }

        if (isHurt()) { updateHurt(deltaTime); tickStateTime(deltaTime); return; }

        switch (state) {
            case SHAKE: updateShake(deltaTime, playerBounds); break;
            case TURN_TO_FLY: updateTransition(deltaTime, TURN_DURATION, State.APPEAR); break;
            case APPEAR: updateTransition(deltaTime, APPEAR_DURATION, State.CHASE); break;
            case CHASE: updateChase(deltaTime, playerBounds); break;
            default: break;
        }
        tickStateTime(deltaTime);
    }

    private void updateShake(float deltaTime, Rectangle playerBounds) {
        if (distanceTo(playerBounds) <= DETECTION_RANGE) setState(State.TURN_TO_FLY);
    }

    private void updateTransition(float deltaTime, float duration, State nextState) {
        transitionTimer += deltaTime;
        if (transitionTimer >= duration) setState(nextState);
    }

    private void updateChase(float deltaTime, Rectangle playerBounds) {
        float px = playerBounds.x + playerBounds.width / 2f;
        float py = playerBounds.y + playerBounds.height / 2f;
        float ex = boundingBox.x + boundingBox.width / 2f;
        float ey = boundingBox.y + boundingBox.height / 2f;
        float dx = px - ex, dy = py - ey;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            velocity.x = (dx / dist) * CHASE_SPEED;
            velocity.y = (dy / dist) * CHASE_SPEED;
        }
        boundingBox.x += velocity.x * deltaTime;
        boundingBox.y += velocity.y * deltaTime;
        keepOutOfArena();
        facingRight = (dx >= 0);
    }

    private void keepOutOfArena() {
        Rectangle arena = level.getArena();
        if (arena == null || !boundingBox.overlaps(arena)) return;

        float pushLeft = (boundingBox.x + boundingBox.width) - arena.x;
        float pushRight = (arena.x + arena.width) - boundingBox.x;
        float pushDown = (boundingBox.y + boundingBox.height) - arena.y;
        float pushUp = (arena.y + arena.height) - boundingBox.y;

        float minX = Math.min(pushLeft, pushRight);
        float minY = Math.min(pushDown, pushUp);

        if (minX <= minY) {
            if (pushLeft < pushRight) boundingBox.x = arena.x - boundingBox.width;
            else boundingBox.x = arena.x + arena.width;
            velocity.x = 0;
        } else {
            if (pushDown < pushUp) boundingBox.y = arena.y - boundingBox.height;
            else boundingBox.y = arena.y + arena.height;
            velocity.y = 0;
        }
    }

    private void updateDead(float deltaTime) {
        if (state == State.DEATH_AIR) {
            velocity.y += GRAVITY * deltaTime;
            boundingBox.y += velocity.y * deltaTime;
            for (Rectangle plat : level.getPlatforms()) {
                if (boundingBox.overlaps(plat) && velocity.y <= 0) {
                    boundingBox.y = plat.y + plat.height;
                    velocity.y = 0;
                    setState(State.DEATH_LAND);
                    break;
                }
            }
        }
    }

    private float distanceTo(Rectangle playerBounds) {
        float px = playerBounds.x + playerBounds.width / 2f;
        float py = playerBounds.y + playerBounds.height / 2f;
        float ex = boundingBox.x + boundingBox.width / 2f;
        float ey = boundingBox.y + boundingBox.height / 2f;
        float dx = px - ex, dy = py - ey;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    protected void onDeath() {
        velocity.set(0, 0);
        transitionTimer = 0f;
        setState(State.DEATH_AIR);
    }

    @Override
    protected void onRespawn() {
        state = State.SHAKE;
        transitionTimer = 0f;
        velocity.set(0, 0);
    }
}