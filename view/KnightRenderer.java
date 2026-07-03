package HollowKnight.hollowknight.view;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import HollowKnight.hollowknight.model.*;
import HollowKnight.hollowknight.utils.AnimationType;
import HollowKnight.hollowknight.utils.GameAssetManager;

public class KnightRenderer {
    private static final float SPRITE_SCALE = 1.85f;

    private final GameAssetManager assetManager;

    public KnightRenderer(GameAssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void render(SpriteBatch batch, Knight knight) {
        AnimationType type = AnimationType.HOLLOW_KNIGHT_IDLE;

        switch (knight.getCurrentState()) {
            case RUNNING: type = AnimationType.HOLLOW_KNIGHT_RUN; break;
            case RUN_TO_IDLE: type = AnimationType.HOLLOW_KNIGHT_RUN_TO_IDLE; break;
            case JUMPING: type = AnimationType.HOLLOW_KNIGHT_JUMP; break;
            case FALLING: type = AnimationType.HOLLOW_KNIGHT_FALL; break;
            case DASHING: type = AnimationType.HOLLOW_KNIGHT_DASH; break;
            case DOUBLE_JUMPING: type = AnimationType.HOLLOW_KNIGHT_DOUBLE_JUMP; break;
            case WALL_SLIDING: type = AnimationType.HOLLOW_KNIGHT_WALL_SLIDE; break;
            case WALL_JUMPING: type = AnimationType.HOLLOW_KNIGHT_WALL_JUMP; break;
            case ATTACKING: type = AnimationType.HOLLOW_KNIGHT_SLASH; break;
            case ATTACKING_ALT: type = AnimationType.HOLLOW_KNIGHT_SLASH_ALT; break;
            case UP_SLASH: type = AnimationType.HOLLOW_KNIGHT_UP_SLASH; break;
            case DOWN_SLASH: type = AnimationType.HOLLOW_KNIGHT_DOWN_SLASH; break;
            case FOCUS_START: type = AnimationType.HOLLOW_KNIGHT_FOCUS_START; break;
            case FOCUSING: type = AnimationType.HOLLOW_KNIGHT_FOCUS; break;
            case FOCUS_GET: type = AnimationType.HOLLOW_KNIGHT_FOCUS_GET; break;
            case FOCUS_END: type = AnimationType.HOLLOW_KNIGHT_FOCUS_END; break;
            case LOOKING_UP: type = AnimationType.HOLLOW_KNIGHT_LOOK_UP; break;
            case LOOKING_DOWN: type = AnimationType.HOLLOW_KNIGHT_LOOK_DOWN; break;
            case LANDING: type = AnimationType.HOLLOW_KNIGHT_LANDING; break;
            case CASTING_FIREBALL: type = AnimationType.HOLLOW_KNIGHT_FIREBALL_CAST; break;
            case CASTING_SCREAM: type = AnimationType.HOLLOW_KNIGHT_SCREAM; break;
            case DEAD: type = AnimationType.HOLLOW_KNIGHT_DEATH; break;
            default: type = AnimationType.HOLLOW_KNIGHT_IDLE; break;
        }

        boolean godMode = knight.invincibleTimer > 1000f;
        if (knight.invincibleTimer > 0.8f && !godMode && knight.getCurrentState() != Entity.State.DEAD) {
            type = AnimationType.HOLLOW_KNIGHT_IDLE_HURT;
        }

        Animation<TextureRegion> animation = assetManager.getAnimation(type);
        if (animation == null) return;

        TextureRegion currentFrame = animation.getKeyFrame(knight.getStateTime());
        Rectangle bounds = knight.getBoundingBox();
        boolean flipX = knight.isFacingRight();

        if (knight.invincibleTimer > 0 && !godMode) {
            float alpha = 0.5f + 0.5f * (float) Math.sin(knight.invincibleTimer * 25f);
            batch.setColor(1, 1, 1, alpha);
        } else {
            batch.setColor(1, 1, 1, 1);
        }

        float spriteHeight = bounds.height * SPRITE_SCALE;
        float spriteWidth = spriteHeight;
        float drawX = bounds.x - (spriteWidth - bounds.width) / 2f;
        float drawY = bounds.y;

        batch.draw(
            currentFrame.getTexture(),
            drawX, drawY,
            spriteWidth, spriteHeight,
            currentFrame.getRegionX(), currentFrame.getRegionY(),
            currentFrame.getRegionWidth(), currentFrame.getRegionHeight(),
            flipX, false
        );

        batch.setColor(1, 1, 1, 1);
    }
}