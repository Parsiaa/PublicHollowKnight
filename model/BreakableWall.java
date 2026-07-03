package HollowKnight.hollowknight.model;

import com.badlogic.gdx.math.Rectangle;

public class BreakableWall {
    private final Rectangle bounds;
    private int hitsRemaining = 3;
    private boolean broken = false;
    private float shakeTime = 0f;

    public BreakableWall(float x, float y, float w, float h) {
        bounds = new Rectangle(x, y, w, h);
    }

    public Rectangle getBounds() { return bounds; }
    public boolean isBroken() { return broken; }
    public int getHitsRemaining() { return hitsRemaining; }
    public float getShakeTime() { return shakeTime; }

    public void hit() {
        if (broken) return;
        hitsRemaining--;
        shakeTime = 0.18f;
        if (hitsRemaining <= 0) broken = true;
    }

    public void update(float dt) {
        if (shakeTime > 0f) shakeTime -= dt;
    }
}