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
    // Soul orb placement/size (on-screen, in the 800x600 UI space).
    private static final float ORB_X = 34f, ORB_Y = 466f, ORB_H = 120f;
    // Mask row: starts just right of the orb and runs flat.
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

        // Empty vessel underneath.
        batch.draw(soulVesselFrame, x, y, w, h);

        // Orb-shaped liquid on top, revealed from the bottom up to the fill level. Because the
        // liquid texture is the orb (transparent outside it), cropping it vertically gives a fill
        // that follows the orb's shape instead of a rectangle spilling past the edges.
        if (fill > 0f) {
            int regionH = soulLiquid.getRegionHeight();
            int srcHeight = Math.max(1, Math.round(regionH * fill));
            int srcY = soulLiquid.getRegionY() + (regionH - srcHeight);
            batch.draw(soulLiquid.getTexture(),
                    x, y, w, h * fill,
                    soulLiquid.getRegionX(), srcY, soulLiquid.getRegionWidth(), srcHeight,
                    false, false);
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