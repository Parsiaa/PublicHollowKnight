package HollowKnight.hollowknight.model;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Zote {
    public enum State { IDLE, TALK, ATTACK, ROLL, FALL, GET_UP }

    private final Rectangle bounds;
    private final Vector2 velocity = new Vector2();
    private State state = State.IDLE;
    private float stateTime = 0f;
    public boolean facingRight = false;

    public Zote(float x, float y, float w, float h) {
        bounds = new Rectangle(x, y, w, h);
    }

    public Rectangle getBounds() { return bounds; }
    public Vector2 getVelocity() { return velocity; }
    public State getState() { return state; }
    public float getStateTime() { return stateTime; }
    public void tick(float dt) { stateTime += dt; }

    public void setState(State s) {
        if (state != s) { state = s; stateTime = 0f; }
    }
}