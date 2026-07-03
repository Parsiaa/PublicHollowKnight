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
    private static final float VESSEL_SCALE = 0.55f;
    private static final float HUD_LEFT = 30f;
    private static final float MASK_ROW_Y = 515f;
    private static final float MASK_PACK = 0.78f;
    private static final float MASK_OVER_ORB = 0.60f;
    private static final float ORB_CX_FRAC = 0.33f;
    private static final float ORB_CY_FRAC = 0.48f;
    private static final float ORB_DIAM_FRAC = 0.34f;

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
        float vesselW = soulVesselFrame.getRegionWidth() * VESSEL_SCALE;
        float vesselH = soulVesselFrame.getRegionHeight() * VESSEL_SCALE;
        float vesselX = HUD_LEFT;
        float vesselY = MASK_ROW_Y - vesselH * 0.62f;

        float fill = visualSoul / knight.maxSoul;
        if (fill < 0f) fill = 0f;
        if (fill > 1f) fill = 1f;

        if (fill > 0f) {
            float orbDiameter = vesselW * ORB_DIAM_FRAC;
            float orbCenterX = vesselX + vesselW * ORB_CX_FRAC;
            float orbBottomY = vesselY + vesselH * ORB_CY_FRAC - orbDiameter * 0.5f;

            int regionH = soulLiquid.getRegionHeight();
            int srcHeight = Math.max(1, Math.round(regionH * fill));
            int srcX = soulLiquid.getRegionX();
            int srcWidth = soulLiquid.getRegionWidth();
            int srcY = soulLiquid.getRegionY() + (regionH - srcHeight);

            float drawWidth = orbDiameter;
            float drawHeight = orbDiameter * fill;

            batch.draw(soulLiquid.getTexture(),
                    orbCenterX - drawWidth / 2f, orbBottomY,
                    drawWidth, drawHeight,
                    srcX, srcY, srcWidth, srcHeight,
                    false, false);
        }

        batch.draw(soulVesselFrame, vesselX, vesselY, vesselW, vesselH);
    }

    private void drawMasks(SpriteBatch batch, float stateTime) {
        float maskW = emptyHealthTexture.getRegionWidth() * UI_SCALE;
        float vesselW = soulVesselFrame.getRegionWidth() * VESSEL_SCALE;
        float rowStartX = HUD_LEFT + vesselW * MASK_OVER_ORB;
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