package HollowKnight.hollowknight.view;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import HollowKnight.hollowknight.model.Enemy;
import HollowKnight.hollowknight.model.enemies.CrystalCrawler;
import HollowKnight.hollowknight.model.enemies.CrystalGuardian;
import HollowKnight.hollowknight.model.enemies.HuskHornhead;
import HollowKnight.hollowknight.model.enemies.Mossfly;
import HollowKnight.hollowknight.utils.EnemyAnimationType;
import HollowKnight.hollowknight.utils.EnemyAssetManager;

public class EnemyRenderer {

    private static final float CRAWLER_SCALE = 4.5f;
    private static final float GUARDIAN_SCALE = 3.6f;
    private static final float HORNHEAD_SCALE = 4.2f;
    private static final float MOSSFLY_SPRITE_SIZE = 310f;

    private final EnemyAssetManager assets;
    private ShapeRenderer shapeRenderer;

    public EnemyRenderer(EnemyAssetManager assets) {
        this.assets = assets;
        this.shapeRenderer = new ShapeRenderer();
    }

    public void render(SpriteBatch batch, Enemy enemy) {
        if (enemy instanceof CrystalCrawler) renderCrawler(batch, (CrystalCrawler) enemy);
        else if (enemy instanceof Mossfly) renderMossfly(batch, (Mossfly) enemy);
        else if (enemy instanceof HuskHornhead) renderHornhead(batch, (HuskHornhead) enemy);
        else if (enemy instanceof CrystalGuardian) renderGuardian(batch, (CrystalGuardian) enemy);
    }

    public void renderLasers(SpriteBatch batch) {
    }

    public void renderLaserShapes(ShapeRenderer sr) {
    }

    private void renderCrawler(SpriteBatch batch, CrystalCrawler c) {
        EnemyAnimationType type;
        switch (c.getState()) {
            case TURN: type = EnemyAnimationType.CRAWLER_TURN; break;
            case DEATH_AIR: type = EnemyAnimationType.CRAWLER_DEATH_AIR; break;
            case DEATH_LAND: type = EnemyAnimationType.CRAWLER_DEATH_LAND; break;
            default: type = EnemyAnimationType.CRAWLER_WALK; break;
        }
        draw(batch, c, type);
    }

    private void renderMossfly(SpriteBatch batch, Mossfly f) {
        EnemyAnimationType type;
        switch (f.getState()) {
            case TURN_TO_FLY: type = EnemyAnimationType.MOSSFLY_TURN_TO_FLY; break;
            case APPEAR: type = EnemyAnimationType.MOSSFLY_APPEAR; break;
            case CHASE: type = EnemyAnimationType.MOSSFLY_FLY; break;
            case DEATH_AIR: type = EnemyAnimationType.MOSSFLY_DEATH_AIR; break;
            case DEATH_LAND: type = EnemyAnimationType.MOSSFLY_DEATH_LAND; break;
            default: type = EnemyAnimationType.MOSSFLY_SHAKE; break;
        }
        draw(batch, f, type);
    }

    private void renderHornhead(SpriteBatch batch, HuskHornhead h) {
        EnemyAnimationType type;
        switch (h.getState()) {
            case REST: type = EnemyAnimationType.HORNHEAD_IDLE; break;
            case ANTICIPATE: type = EnemyAnimationType.HORNHEAD_ANTICIPATE; break;
            case LUNGE: type = EnemyAnimationType.HORNHEAD_LUNGE; break;
            case COOLDOWN: type = EnemyAnimationType.HORNHEAD_COOLDOWN; break;
            case TURN: type = EnemyAnimationType.HORNHEAD_TURN; break;
            case DEATH_AIR: type = EnemyAnimationType.HORNHEAD_DEATH_AIR; break;
            case DEATH_LAND: type = EnemyAnimationType.HORNHEAD_DEATH_LAND; break;
            default: type = EnemyAnimationType.HORNHEAD_WALK; break;
        }
        draw(batch, h, type);
    }

    private void renderGuardian(SpriteBatch batch, CrystalGuardian g) {
        EnemyAnimationType type;
        switch (g.getState()) {
            case SHOOT: type = EnemyAnimationType.GUARDIAN_SHOOT; break;
            case RUN: type = EnemyAnimationType.GUARDIAN_RUN; break;
            case EVADE: type = EnemyAnimationType.GUARDIAN_EVADE; break;
            case TURN: type = EnemyAnimationType.GUARDIAN_TURN; break;
            case DEATH_AIR: type = EnemyAnimationType.GUARDIAN_DEATH_AIR; break;
            case DEATH_LAND: type = EnemyAnimationType.GUARDIAN_DEATH_LAND; break;
            default: type = EnemyAnimationType.GUARDIAN_IDLE; break;
        }
        draw(batch, g, type);

        Rectangle laser = g.getLaserBox();
        if (laser != null) {
            batch.end();
            float cy = laser.y + laser.height / 2f;
            float muzzleX = g.facingRight ? laser.x : laser.x + laser.width;
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.9f, 0.25f, 0.8f, 0.4f);
            shapeRenderer.rect(laser.x, laser.y, laser.width, laser.height);
            shapeRenderer.setColor(1f, 0.45f, 0.95f, 0.85f);
            shapeRenderer.rect(laser.x, cy - laser.height * 0.28f, laser.width, laser.height * 0.56f);
            shapeRenderer.setColor(1f, 0.92f, 1f, 1f);
            shapeRenderer.rect(laser.x, cy - laser.height * 0.12f, laser.width, laser.height * 0.24f);
            shapeRenderer.setColor(1f, 0.55f, 1f, 0.55f);
            shapeRenderer.circle(muzzleX, cy, laser.height * 1.1f);
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.circle(muzzleX, cy, laser.height * 0.55f);
            shapeRenderer.end();
            batch.begin();
        }
    }

    private void draw(SpriteBatch batch, Enemy enemy, EnemyAnimationType type) {
        Animation<TextureRegion> anim = assets.get(type);
        if (anim == null) return;
        TextureRegion frame = anim.getKeyFrame(enemy.getStateTime());
        Rectangle box = enemy.getBoundingBox();
        boolean flipX = enemy.facingRight;

        float drawWidth;
        float drawHeight;
        float drawX;
        float drawY;

        if (enemy instanceof Mossfly) {
            drawWidth = MOSSFLY_SPRITE_SIZE;
            drawHeight = MOSSFLY_SPRITE_SIZE;
            drawX = box.x - (MOSSFLY_SPRITE_SIZE - box.width) / 2f;
            drawY = box.y - (MOSSFLY_SPRITE_SIZE - box.height) / 2f;
        } else {
            float scale = scaleFor(enemy);
            drawWidth = box.width * scale;
            drawHeight = box.height * scale;
            drawX = box.x + box.width / 2f - drawWidth / 2f;
            drawY = box.y;
        }

        boolean flashOn = enemy.getHurtFlash() > 0f
                && ((int) (enemy.getHurtFlash() * 20f)) % 2 == 0;
        if (flashOn) batch.setColor(1f, 0.3f, 0.3f, 1f);

        batch.draw(frame.getTexture(), drawX, drawY, drawWidth, drawHeight,
                frame.getRegionX(), frame.getRegionY(),
                frame.getRegionWidth(), frame.getRegionHeight(),
                flipX, false);

        if (flashOn) batch.setColor(1f, 1f, 1f, 1f);
    }

    private float scaleFor(Enemy enemy) {
        if (enemy instanceof HuskHornhead) return HORNHEAD_SCALE;
        if (enemy instanceof CrystalGuardian) return GUARDIAN_SCALE;
        if (enemy instanceof CrystalCrawler) return CRAWLER_SCALE;
        return 1f;
    }

    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}