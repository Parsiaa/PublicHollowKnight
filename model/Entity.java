package HollowKnight.hollowknight.model;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Entity {
    public enum State {
        IDLE, RUNNING, JUMPING, FALLING, DASHING, DOUBLE_JUMPING, WALL_JUMPING,
        WALL_SLIDING, ATTACKING, ATTACKING_ALT, FOCUSING, DOWN_SLASH,
        LOOKING_UP, LOOKING_DOWN, UP_SLASH, LANDING, RUN_TO_IDLE,
        FOCUS_START, FOCUS_GET, FOCUS_END, CASTING_FIREBALL, CASTING_SCREAM, DEAD
    }

    private State currentState = State.IDLE;
    private Rectangle boundingBox;
    private final Vector2 velocity;
    private float stateTime;
    private boolean facingRight = true;

    public boolean onGround = false;
    public boolean isAgainstWall = false;

    public Entity(float x, float y, float width, float height) {
        this.boundingBox = new Rectangle(x, y, width, height);
        this.velocity = new Vector2(0, 0);
        this.stateTime = 0f;
    }

    public void updateStateTime(float deltaTime) { this.stateTime += deltaTime; }
    public Rectangle getBoundingBox() { return boundingBox; }
    public void setBoundingBox(float x, float y, float width, float height) { this.boundingBox = new Rectangle(x, y, width, height); }
    public Vector2 getVelocity() { return velocity; }
    public float getStateTime() { return stateTime; }

    public State getCurrentState() { return currentState; }
    public void setCurrentState(State newState) {
        if (this.currentState != newState) {
            this.currentState = newState;
            this.stateTime = 0f;
        }
    }

    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
}
