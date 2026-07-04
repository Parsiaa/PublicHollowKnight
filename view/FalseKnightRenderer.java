package HollowKnight.hollowknight.view;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import HollowKnight.hollowknight.model.FalseKnight;
import HollowKnight.hollowknight.utils.FalseKnightAnimationType;
import HollowKnight.hollowknight.utils.FalseKnightAssetManager;

public class FalseKnightRenderer {

    private static final float SPRITE_SCALE = 2.4f;

    private static final float FOOT_OFFSET = 0.05f;

    private final FalseKnightAssetManager assets;

    public FalseKnightRenderer(FalseKnightAssetManager assets) {
        this.assets = assets;
    }

    public void render(SpriteBatch batch, FalseKnight boss) {
        if (boss == null) return;

        FalseKnightAnimationType type = pickAnimation(boss);
        Animation<TextureRegion> anim = assets.get(type);
        if (anim == null) return;

        float sampleTime = (type == FalseKnightAnimationType.STUN_HIT)
                ? boss.getStunHitAnimTime() : boss.getStateTime();
        TextureRegion frame = anim.getKeyFrame(sampleTime);
        Rectangle b = boss.getBounds();

        float drawHeight = b.height * SPRITE_SCALE;
        float drawWidth = drawHeight * ((float) frame.getRegionWidth() / frame.getRegionHeight());
        float drawX = b.x + b.width / 2f - drawWidth / 2f;
        float drawY = b.y - drawHeight * FOOT_OFFSET;
        boolean flipX = boss.isFacingRight();

        if (boss.getHurtFlash() > 0f) {
            batch.setColor(1f, 0.55f, 0.55f, 1f);
        } else if (boss.isVulnerable()) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(boss.getStateTime() * 12f);
            batch.setColor(1f, 1f, 0.8f + 0.2f * pulse, 1f);
        }
        batch.draw(frame.getTexture(), drawX, drawY, drawWidth, drawHeight,
                frame.getRegionX(), frame.getRegionY(),
                frame.getRegionWidth(), frame.getRegionHeight(),
                flipX, false);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private FalseKnightAnimationType pickAnimation(FalseKnight boss) {
        switch (boss.getState()) {
            case MACE_SLAM: return FalseKnightAnimationType.SLAM;
            case CHARGE_RUN: return FalseKnightAnimationType.RUN;
            case OFFENSIVE_LEAP: return FalseKnightAnimationType.LEAP;
            case DEFENSIVE_LEAP: return FalseKnightAnimationType.LEAP;
            case POWER_SLAM: return FalseKnightAnimationType.POWER_SLAM;
            case STUN: return pickStunAnimation(boss);
            case DEAD: return FalseKnightAnimationType.DEATH;
            default: return FalseKnightAnimationType.IDLE;
        }
    }

    private FalseKnightAnimationType pickStunAnimation(FalseKnight boss) {
        switch (boss.getStunStage()) {
            case AIR: return FalseKnightAnimationType.STUN_AIR;
            case LAND: return FalseKnightAnimationType.STUN_DAZED;
            case RECOVER: return FalseKnightAnimationType.STUN_RECOVER;
            case DAZED:
            default:
                if (boss.isStunHitActive()) return FalseKnightAnimationType.STUN_HIT;
                return FalseKnightAnimationType.STUN_DAZED;
        }
    }
}