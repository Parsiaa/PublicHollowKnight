package HollowKnight.hollowknight.model;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import HollowKnight.hollowknight.utils.EffectAnimationType;

public class VisualEffect {

    public final EffectAnimationType type;
    public final Rectangle bounds;
    public final Vector2 velocity = new Vector2();
    public boolean flipX;
    public boolean piercing;
    public int damage;
    public boolean damageDealt = false;

    public int lastTickApplied = -1;

    private float stateTime = 0f;
    private float duration;
    private boolean finished = false;

    public VisualEffect(EffectAnimationType type, float x, float y, float w, float h, float duration) {
        this.type = type;
        this.bounds = new Rectangle(x, y, w, h);
        this.duration = duration;
    }

    public void update(float dt) {
        stateTime += dt;
        bounds.x += velocity.x * dt;
        bounds.y += velocity.y * dt;
        if (stateTime >= duration) finished = true;
    }

    public float getStateTime() { return stateTime; }
    public boolean isFinished() { return finished; }
    public void finish() { finished = true; }
}
