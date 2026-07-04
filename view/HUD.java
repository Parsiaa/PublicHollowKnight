package HollowKnight.hollowknight.view;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import HollowKnight.hollowknight.model.Knight;

public class HUD {
    private final Knight knight;
    private final OrthographicCamera uiCamera;

    private static final float UI_SCALE = 0.35f;
    private static final float ORB_X = 34f, ORB_Y = 466f, ORB_H = 120f;
    private static final float VESSEL_L = 0.077f, VESSEL_R = 0.923f, VESSEL_T = 0.056f, VESSEL_B = 0.936f;
    private static final float LIQUID_L = 0.062f, LIQUID_R = 0.527f, LIQUID_T = 0.244f, LIQUID_B = 0.963f;
    private static final float MASK_START_X = 168f;
    private static final float MASK_ROW_Y = 515f;
    private static final float MASK_PACK = 0.78f;

    private float visualSoul;
    private int lastMaskCount;
    private float maskAnimationTimer = 0f;
    private boolean isPlayingMaskAnimation = false;
    private Animation<TextureRegion> currentMaskAnimation;
    private int animatingMaskIndex = -1;

    private final TextureRegion emptyHealthTexture;
    private final Animation<TextureRegion> filledHealthShine;
    private final Animation<TextureRegion> breakHealth;
    private final Animation<TextureRegion> healthRefill;
    private final TextureRegion soulVesselFrame;
    private final TextureRegion soulLiquid;

    public HUD(Knight knight, float screenWidth, float screenHeight,
               TextureRegion empty, Animation<TextureRegion> shine,
               Animation<TextureRegion> breakAnim, Animation<TextureRegion> refillAnim,
               TextureRegion frame, TextureRegion liquid) {
        this.knight = knight;
        this.lastMaskCount = knight.masks;
        this.visualSoul = knight.soul;

        this.uiCamera = new OrthographicCamera();
        this.uiCamera.setToOrtho(false, screenWidth, screenHeight);

        this.emptyHealthTexture = empty;
        this.filledHealthShine = shine;
        this.breakHealth = breakAnim;
        this.healthRefill = refillAnim;
        this.soulVesselFrame = frame;
        this.soulLiquid = liquid;
    }

    public void syncSoul() {
        this.visualSoul = knight.soul;
    }

    public void update(float deltaTime) {
        visualSoul += (knight.soul - visualSoul) * 5f * deltaTime;

        if (knight.masks < lastMaskCount) {
            triggerMaskAnimation(breakHealth, knight.masks);
        } else if (knight.masks > lastMaskCount) {
            triggerMaskAnimation(healthRefill, knight.masks - 1);
        }

        lastMaskCount = knight.masks;

        if (isPlayingMaskAnimation) {
            maskAnimationTimer += deltaTime;
            if (currentMaskAnimation.isAnimationFinished(maskAnimationTimer)) {
                isPlayingMaskAnimation = false;
            }
        }
    }

    public void render(SpriteBatch batch, float stateTime) {
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        drawSoulVessel(batch);
        drawMasks(batch, stateTime);

        batch.end();
    }

    private void drawSoulVessel(SpriteBatch batch) {
        float w = ORB_H * ((float) soulVesselFrame.getRegionWidth() / soulVesselFrame.getRegionHeight());
        float h = ORB_H;
        float x = ORB_X, y = ORB_Y;

        float fill = visualSoul / knight.maxSoul;
        if (fill < 0f) fill = 0f;
        if (fill > 1f) fill = 1f;

        batch.draw(soulVesselFrame, x, y, w, h);

        if (fill > 0f) {
            float circleX = x + VESSEL_L * w;
            float circleY = y + (1f - VESSEL_B) * h;
            float circleW = (VESSEL_R - VESSEL_L) * w;
            float circleH = (VESSEL_B - VESSEL_T) * h;

            int fw = soulLiquid.getRegionWidth();
            int fh = soulLiquid.getRegionHeight();
            int ox = soulLiquid.getRegionX() + Math.round(LIQUID_L * fw);
            int oyTop = soulLiquid.getRegionY() + Math.round(LIQUID_T * fh);
            int ow = Math.round((LIQUID_R - LIQUID_L) * fw);
            int oh = Math.round((LIQUID_B - LIQUID_T) * fh);

            int srcH = Math.max(1, Math.round(oh * fill));
            int srcY = oyTop + (oh - srcH);
            batch.draw(soulLiquid.getTexture(),
                    circleX, circleY, circleW, circleH * fill,
                    ox, srcY, ow, srcH, false, false);
        }
    }

    private void drawMasks(SpriteBatch batch, float stateTime) {
        float maskW = emptyHealthTexture.getRegionWidth() * UI_SCALE;
        float rowStartX = MASK_START_X;
        float spacing = maskW * MASK_PACK;

        for (int i = 0; i < knight.maxMasks; i++) {
            float maskX = rowStartX + (i * spacing);
            float maskY = MASK_ROW_Y;

            TextureRegion frameToDraw = emptyHealthTexture;

            if (isPlayingMaskAnimation && i == animatingMaskIndex) {
                frameToDraw = currentMaskAnimation.getKeyFrame(maskAnimationTimer);
            } else if (i < knight.masks) {
                frameToDraw = filledHealthShine.getKeyFrame(stateTime, true);
            }

            batch.draw(frameToDraw, maskX, maskY,
                    frameToDraw.getRegionWidth() * UI_SCALE,
                    frameToDraw.getRegionHeight() * UI_SCALE);
        }
    }

    private void triggerMaskAnimation(Animation<TextureRegion> anim, int index) {
        currentMaskAnimation = anim;
        animatingMaskIndex = index;
        maskAnimationTimer = 0f;
        isPlayingMaskAnimation = true;
    }
}