package HollowKnight.hollowknight.controller;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.HashSet;
import java.util.Set;

import HollowKnight.hollowknight.model.Enemy;
import HollowKnight.hollowknight.model.Knight;
import HollowKnight.hollowknight.model.Level;
import HollowKnight.hollowknight.model.VisualEffect;
import HollowKnight.hollowknight.utils.EffectAnimationType;
import HollowKnight.hollowknight.utils.EffectAssetManager;

public class EffectManager {

    private static final float SLASH_DURATION = 0.27f;
    private static final float DASH_DURATION = 0.36f; 
    private static final float SPIRIT_DURATION = 0.9f;
    private static final float SPIRIT_SPEED = 500f;
    private static final float WRAITHS_DURATION = 0.585f;
    
    private static final int SPIRIT_DAMAGE = 15;
    private static final int WRAITHS_DAMAGE = 8;

    private final List<VisualEffect> effects = new ArrayList<>();
    private final java.util.Map<VisualEffect, Set<Enemy>> alreadyHit = new java.util.HashMap<>();
    private final EffectAssetManager assets;
    private final Level level;

    public EffectManager(EffectAssetManager assets, Level level) {
        this.assets = assets;
        this.level = level;
    }

    public void spawnSlash(Knight knight, EffectAnimationType type) {
        Rectangle b = knight.getBoundingBox();
        float size = b.height * 1.4222f;
        float x, y;
        if (type == EffectAnimationType.NAIL_UP_SLASH) {
            x = b.x + b.width / 2f - size / 2f;
            y = b.y + b.height - size / 2f;
        } else if (type == EffectAnimationType.NAIL_DOWN_SLASH) {
            x = b.x + b.width / 2f - size / 2f;
            y = b.y - size / 2f;
        } else {
            x = knight.isFacingRight() ? b.x + b.width - size / 2f : b.x - size / 2f;
            y = b.y + b.height / 2f - size / 2f;
        }
        VisualEffect fx = new VisualEffect(type, x, y, size, size, SLASH_DURATION);
        fx.flipX = knight.isFacingRight();
        effects.add(fx);
    }

    public void spawnDash(Knight knight) {
        Rectangle b = knight.getBoundingBox();
        float w = b.height * 1.7f;
        float h = b.height * 1.25f;
        // Centre it on the knight and trail it behind the dash direction so it overlaps the body
        // (previously it sat entirely to the side and was too small to read).
        float y = b.y + b.height / 2f - h / 2f;
        // Centre on the knight, trailing only slightly behind the dash direction, so it hugs the body.
        float cx = b.x + b.width / 2f - w / 2f;
        float x = knight.isFacingRight() ? cx - w * 0.1f : cx + w * 0.1f;
        VisualEffect fx = new VisualEffect(EffectAnimationType.DASH_EFFECT, x, y, w, h, DASH_DURATION);
        fx.flipX = knight.isFacingRight();
        effects.add(fx);
    }

    public VisualEffect spawnVengefulSpirit(Knight knight) {
        Rectangle b = knight.getBoundingBox();
        float h = b.height * 1.1f;
        float w = h * 1.7f;
        // Start overlapping the knight (biased forward) instead of spawning a full width ahead.
        float cx = b.x + b.width / 2f - w / 2f;
        float x = knight.isFacingRight() ? cx + w * 0.25f : cx - w * 0.25f;
        float y = b.y + b.height / 2f - h / 2f;
        VisualEffect fx = new VisualEffect(EffectAnimationType.VENGEFUL_SPIRIT, x, y, w, h, SPIRIT_DURATION);
        fx.velocity.x = knight.isFacingRight() ? SPIRIT_SPEED : -SPIRIT_SPEED;
        fx.flipX = !knight.isFacingRight();
        fx.piercing = true;
        fx.damage = (int) (SPIRIT_DAMAGE * (knight.charmVoidHeart ? 1.5f : 1f));
        effects.add(fx);
        return fx;
    }

    public VisualEffect spawnHowlingWraiths(Knight knight) {
        Rectangle b = knight.getBoundingBox();
        float w = b.width * 2.6f;
        float h = b.height * 1.3f;
        float x = b.x + b.width / 2f - w / 2f;
        float y = b.y + b.height * 0.4f;
        VisualEffect fx = new VisualEffect(EffectAnimationType.HOWLING_WRAITHS, x, y, w, h, WRAITHS_DURATION);
        fx.damage = (int) (WRAITHS_DAMAGE * (knight.charmVoidHeart ? 1.5f : 1f));
        effects.add(fx);
        return fx;
    }

    public void spawnHitShockwave(float x, float y) {
        float size = 160f;
        VisualEffect fx = new VisualEffect(EffectAnimationType.HIT_SHOCKWAVE, x - size / 2f, y - size / 2f, size, size, 0.3f);
        effects.add(fx);
    }

    public void update(float dt, List<Enemy> enemies, Knight knight) {
        Iterator<VisualEffect> it = effects.iterator();
        while (it.hasNext()) {
            VisualEffect fx = it.next();
            fx.update(dt);

            if (fx.damage > 0) {
                applyEffectDamage(fx, enemies, knight);
            }

            if (fx.type == EffectAnimationType.VENGEFUL_SPIRIT && level != null) {
                // Test a small central core rather than the full (large) visual bounds, so the
                // spirit isn't deleted the instant its art grazes the floor it flies over - it
                // only ends when that core actually meets a wall.
                float cw = fx.bounds.width * 0.5f, ch = fx.bounds.height * 0.35f;
                Rectangle core = new Rectangle(
                        fx.bounds.x + (fx.bounds.width - cw) / 2f,
                        fx.bounds.y + (fx.bounds.height - ch) / 2f, cw, ch);
                for (Rectangle plat : level.getPlatforms()) {
                    if (core.overlaps(plat)) { fx.finish(); break; }
                }
            }

            if (fx.isFinished()) {
                alreadyHit.remove(fx);
                it.remove();
            }
        }
    }

    private void applyEffectDamage(VisualEffect fx, List<Enemy> enemies, Knight knight) {
        boolean isSpirit  = fx.type == EffectAnimationType.VENGEFUL_SPIRIT;
        boolean isWraiths = fx.type == EffectAnimationType.HOWLING_WRAITHS;

        for (Enemy enemy : enemies) {
            if (enemy.isDead()) continue;
            if (!fx.bounds.overlaps(enemy.getBoundingBox())) continue;

            if (isSpirit) {
                Set<Enemy> hitSet = alreadyHit.computeIfAbsent(fx, k -> new HashSet<>());
                if (hitSet.contains(enemy)) continue;
                hitSet.add(enemy);
                enemy.takeDamage(fx.damage);
                knight.soul = Math.min(knight.maxSoul, knight.soul + knight.soulPerHit);
            } else if (isWraiths) {
                float tickWindow = WRAITHS_DURATION / 3f;
                int currentTick = (int) (fx.getStateTime() / tickWindow);
                if (currentTick != fx.lastTickApplied) {
                    fx.lastTickApplied = currentTick;
                    enemy.takeDamage(fx.damage);
                    knight.soul = Math.min(knight.maxSoul, knight.soul + knight.soulPerHit);
                }
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (VisualEffect fx : effects) {
            Animation<TextureRegion> anim = assets.get(fx.type);
            if (anim == null) continue;

            TextureRegion frame = anim.getKeyFrame(fx.getStateTime());
            batch.draw(
                frame.getTexture(),
                fx.bounds.x, fx.bounds.y,
                fx.bounds.width, fx.bounds.height,
                frame.getRegionX(), frame.getRegionY(),
                frame.getRegionWidth(), frame.getRegionHeight(),
                fx.flipX, false
            );
        }
    }

    public List<VisualEffect> getEffects() { return effects; }
}
