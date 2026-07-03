package HollowKnight.hollowknight.model;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class FalseKnight {
    public enum State { IDLE, MACE_SLAM, CHARGE_RUN, OFFENSIVE_LEAP, DEFENSIVE_LEAP, POWER_SLAM, STUN, DEAD }

    public enum StunStage { AIR, LAND, DAZED, RECOVER }

    private final Rectangle bounds;
    private final Vector2 velocity = new Vector2();
    private final Vector2 spawn = new Vector2();

    private State state = State.IDLE;
    private float stateTime = 0f;

    private final int maxHp;
    private int currentHp;

    private boolean facingRight = false;
    private boolean onGround = false;
    private boolean vulnerable = false;
    private int phase = 1;
    private boolean stunUsed = false;
    private StunStage stunStage = StunStage.AIR;
    private float hurtFlash = 0f;

    public static final float STUN_HIT_DURATION = 0.18f;
    private float stunHitTimer = 0f;

    public FalseKnight(float x, float y, float w, float h, int maxHp) {
        this.bounds = new Rectangle(x, y, w, h);
        this.spawn.set(x, y);
        this.maxHp = maxHp;
        this.currentHp = maxHp;
    }

    public Rectangle getBounds() { return bounds; }
    public Vector2 getVelocity() { return velocity; }
    public Vector2 getSpawn() { return spawn; }
    public State getState() { return state; }
    public float getStateTime() { return stateTime; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public float getHealthFraction() { return maxHp <= 0 ? 0f : (float) currentHp / maxHp; }
    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean v) { facingRight = v; }
    public boolean isOnGround() { return onGround; }
    public void setOnGround(boolean v) { onGround = v; }
    public boolean isVulnerable() { return vulnerable; }
    public void setVulnerable(boolean v) { vulnerable = v; }
    public int getPhase() { return phase; }
    public void setPhase(int p) { phase = p; }
    public boolean isStunUsed() { return stunUsed; }
    public void setStunUsed(boolean v) { stunUsed = v; }
    public StunStage getStunStage() { return stunStage; }
    public void setStunStage(StunStage s) { stunStage = s; }
    public boolean isStunHitActive() { return stunHitTimer > 0f; }
    public float getStunHitAnimTime() { return STUN_HIT_DURATION - stunHitTimer; }
    public void triggerStunHit() { stunHitTimer = STUN_HIT_DURATION; }
    public boolean isDead() { return state == State.DEAD; }
    public float getHurtFlash() { return hurtFlash; }

    public void setState(State s) {
        if (state == s) return;
        state = s;
        stateTime = 0f;
    }

    public void restartStateTime() {
        stateTime = 0f;
    }

    public void tick(float dt) {
        stateTime += dt;
        if (hurtFlash > 0f) hurtFlash -= dt;
        if (stunHitTimer > 0f) stunHitTimer -= dt;
    }

    public int takeDamage(int amount) {
        if (state == State.DEAD) return currentHp;
        currentHp -= amount;
        hurtFlash = 0.12f;
        if (currentHp <= 0) {
            currentHp = 0;
            setState(State.DEAD);
            velocity.set(0, 0);
        }
        return currentHp;
    }
}