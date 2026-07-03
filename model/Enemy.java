package HollowKnight.hollowknight.model;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Enemy {

    protected int maxHp;
    protected int currentHp;
    protected boolean dead = false;

    protected final Rectangle boundingBox;
    protected final Vector2 velocity = new Vector2();
    public boolean onGround = false;
    public boolean facingRight = true;

    protected float stateTime = 0f;
    public float deathTimer = 0f;

    protected float hurtTimer = 0f;

    private static final float HURT_FLASH_DURATION = 0.3f;
    protected float hurtFlash = 0f;

    protected final float spawnX;
    protected final float spawnY;

    public final int soulReward;

    protected Enemy(float x, float y, float w, float h, int hp, int soulReward) {
        this.boundingBox = new Rectangle(x, y, w, h);
        this.maxHp = hp;
        this.currentHp = hp;
        this.soulReward = soulReward;
        this.spawnX = x;
        this.spawnY = y;
    }

    public boolean takeDamage(int amount) {
        if (dead) return false;
        hurtFlash = HURT_FLASH_DURATION;
        currentHp -= amount;
        if (currentHp <= 0) {
            currentHp = 0;
            dead = true;
            onDeath();
            return true;
        }
        return false;
    }

    public void applyKnockback(float vx, float vy, float duration) {
        if (dead) return;
        velocity.x = vx;
        velocity.y = vy;
        hurtTimer = duration;
    }

    public boolean isHurt() { return hurtTimer > 0f; }

    protected void updateHurt(float deltaTime) {
        if (hurtTimer <= 0f) return;
        hurtTimer -= deltaTime;
        boundingBox.x += velocity.x * deltaTime;
        boundingBox.y += velocity.y * deltaTime;
        velocity.x *= (1f - 6f * deltaTime);
    }

    protected void onDeath() {}

    public void respawn() {
        currentHp = maxHp;
        dead = false;
        hurtTimer = 0f;
        hurtFlash = 0f;
        boundingBox.setPosition(spawnX, spawnY);
        velocity.set(0, 0);
        stateTime = 0f;
        onRespawn();
    }

    protected void onRespawn() {}

    public Rectangle getBoundingBox() { return boundingBox; }
    public Vector2 getVelocity() { return velocity; }
    public boolean isDead() { return dead; }
    public int getCurrentHp() { return currentHp; }
    public float getStateTime() { return stateTime; }
    public float getHurtFlash() { return hurtFlash; }
    public void tickStateTime(float deltaTime) {
        stateTime += deltaTime;
        if (hurtFlash > 0f) hurtFlash -= deltaTime;
    }
    public void resetStateTime() { stateTime = 0f; }

    public abstract void update(float deltaTime, Rectangle playerBounds);
}