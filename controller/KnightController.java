package HollowKnight.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Rectangle;
import HollowKnight.hollowknight.model.Entity;
import HollowKnight.hollowknight.model.Knight;
import HollowKnight.hollowknight.model.Level;

public class KnightController {

    private final Knight knight;
    private final Level level;
    private static final float SCALE = 4f;

    private static final float MOVE_SPEED = 300f * SCALE;
    private static final float JUMP_VELOCITY = 650f * SCALE;
    private static final float GRAVITY = 1800f * SCALE;
    private static final float DASH_SPEED = 800f * SCALE;
    private static final float DASH_DURATION = 0.2f;
    private static final float WALL_SLIDE_SPEED = -100f * SCALE;

    private static final int SPELL_SOUL_COST = 33;
    private static final float CAST_LOCK_DURATION = 0.4f;
    private static final float DEATH_ANIM_DURATION = 1.8f;
    private static final float VOID_Y = -400f;

    private float deathTimer = 0f;
    private float transitionTimer = 0f;
    private float focusTransitionTimer = 0f;

    private boolean wasOnGround = true;
    private boolean alternateSlash = false;
    private Entity.State previousState = Entity.State.IDLE;

    public KnightController(Knight knight, Level level) {
        this.knight = knight;
        this.level = level;
    }

    public void update(float deltaTime) {
        if (knight.getCurrentState() == Entity.State.DEAD) {
            handleDeathSequence(deltaTime);
            knight.updateStateTime(deltaTime);
            return;
        }

        previousState = knight.getCurrentState();
        updateTimers(deltaTime);

        handleInput(deltaTime);
        applyPhysics(deltaTime);
        checkSpikes();
        checkVoidOut();

        if (knight.getCurrentState() == Entity.State.DEAD) {
            knight.updateStateTime(deltaTime);
            return;
        }

        updateSafePosition();
        updateAnimationState(deltaTime);
        knight.updateStateTime(deltaTime);
        wasOnGround = knight.onGround;
    }

    private void handleDeathSequence(float deltaTime) {
        Rectangle bounds = knight.getBoundingBox();
        knight.getVelocity().x = 0f;

        for (Rectangle plat : level.getPlatforms()) {
            if (bounds.overlaps(plat)) {
                float bCenter = bounds.x + bounds.width / 2f;
                float pCenter = plat.x + plat.width / 2f;
                bounds.x = (bCenter < pCenter) ? plat.x - bounds.width : plat.x + plat.width;
            }
        }

        float prevBottom = bounds.y;
        knight.getVelocity().y -= GRAVITY * deltaTime;
        bounds.y += knight.getVelocity().y * deltaTime;
        knight.onGround = false;
        for (Rectangle plat : level.getPlatforms()) {
            if (bounds.overlaps(plat) && knight.getVelocity().y <= 0f
                    && prevBottom >= plat.y + plat.height - 1f) {
                bounds.y = plat.y + plat.height;
                knight.getVelocity().y = 0f;
                knight.onGround = true;
            }
        }
        deathTimer += deltaTime;
        if (deathTimer >= DEATH_ANIM_DURATION) {
            deathTimer = 0f;
            knight.respawnAt(level.getLastSafePosition().x, level.getLastSafePosition().y);
        }
    }

    private void updateTimers(float deltaTime) {
        if (knight.invincibleTimer > 0) knight.invincibleTimer -= deltaTime;
        if (knight.dashCooldown > 0) knight.dashCooldown -= deltaTime;
        if (knight.castLockTimer > 0) {
            knight.castLockTimer -= deltaTime;
            if (knight.castLockTimer <= 0
                    && (knight.getCurrentState() == Entity.State.CASTING_FIREBALL
                     || knight.getCurrentState() == Entity.State.CASTING_SCREAM)) {
                knight.setCurrentState(knight.onGround ? Entity.State.IDLE : Entity.State.FALLING);
            }
        }
        if (knight.attackTimer > 0) {
            knight.attackTimer -= deltaTime;
            if (knight.attackTimer <= 0) {
                knight.setCurrentState(knight.onGround ? Entity.State.IDLE : Entity.State.FALLING);
            }
        }
    }

    private void handleInput(float deltaTime) {
        Entity.State state = knight.getCurrentState();
        if (knight.castLockTimer > 0) return;

        if (Gdx.input.isKeyPressed(Keys.A) && knight.onGround) {
            knight.getVelocity().x = 0;
            Entity.State cur = knight.getCurrentState();

            if (cur == Entity.State.FOCUS_GET) {
                focusTransitionTimer -= deltaTime;
                if (focusTransitionTimer <= 0f) {
                    knight.setCurrentState(Entity.State.FOCUSING);
                }
                return;
            }

            if (cur != Entity.State.FOCUS_START && cur != Entity.State.FOCUSING) {
                knight.setCurrentState(Entity.State.FOCUS_START);
                focusTransitionTimer = 0.2f;
            } else if (cur == Entity.State.FOCUS_START) {
                focusTransitionTimer -= deltaTime;
                if (focusTransitionTimer <= 0f) {
                    knight.setCurrentState(Entity.State.FOCUSING);
                }
            }

            knight.focusTimer += deltaTime;
            if (knight.focusTimer >= knight.focusRequiredTime
                    && knight.soul >= SPELL_SOUL_COST
                    && knight.masks < knight.maxMasks) {
                knight.soul -= SPELL_SOUL_COST;
                knight.masks++;
                knight.focusTimer = 0f;
                knight.setCurrentState(Entity.State.FOCUS_GET);
                focusTransitionTimer = 0.4f;
            }
            return;
        } else {
            if (knight.focusTimer > 0 || state == Entity.State.FOCUSING || state == Entity.State.FOCUS_START) {
                knight.setCurrentState(Entity.State.FOCUS_END);
                focusTransitionTimer = 0.2f;
            }
            knight.focusTimer = 0f;
        }

        if (state == Entity.State.FOCUS_GET || state == Entity.State.FOCUS_END) {
            focusTransitionTimer -= deltaTime;
            if (focusTransitionTimer > 0f) return;
            knight.setCurrentState(knight.onGround ? Entity.State.IDLE : Entity.State.FALLING);
        }

        if (Gdx.input.isKeyJustPressed(Keys.Q) && knight.soul >= SPELL_SOUL_COST) {
            knight.soul -= SPELL_SOUL_COST;
            knight.castLockTimer = CAST_LOCK_DURATION;
            knight.setCurrentState(Entity.State.CASTING_FIREBALL);
            knight.requestVengefulSpirit = true;
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.W) && knight.soul >= SPELL_SOUL_COST) {
            knight.soul -= SPELL_SOUL_COST;
            knight.castLockTimer = CAST_LOCK_DURATION;
            knight.setCurrentState(Entity.State.CASTING_SCREAM);
            knight.requestHowlingWraiths = true;
            return;
        }

        if (state != Entity.State.DASHING
                && Gdx.input.isKeyJustPressed(Keys.C)
                && knight.canDash && knight.dashCooldown <= 0) {
            knight.setCurrentState(Entity.State.DASHING);
            knight.dashTimer = DASH_DURATION;
            knight.dashCooldown = knight.dashCooldownBase;
            knight.canDash = false;
            knight.getVelocity().y = 0;
            knight.requestDashEffect = true;
            return;
        }

        if (state == Entity.State.DASHING) return;

        float targetVx = 0;
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            targetVx = -MOVE_SPEED;
            knight.setFacingRight(false);
            if (isBreakableTransition()) transitionTimer = 0;
        } else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            targetVx = MOVE_SPEED;
            knight.setFacingRight(true);
            if (isBreakableTransition()) transitionTimer = 0;
        }
        knight.getVelocity().x = targetVx;

        if (Gdx.input.isKeyJustPressed(Keys.Z)) {
            knight.pogoActive = false;
            if (knight.onGround) {
                knight.getVelocity().y = JUMP_VELOCITY;
                knight.onGround = false;
                transitionTimer = 0;
            } else if (knight.isAgainstWall) {
                knight.getVelocity().y = JUMP_VELOCITY;
                knight.getVelocity().x = knight.isFacingRight() ? -MOVE_SPEED : MOVE_SPEED;
                knight.setFacingRight(!knight.isFacingRight());
                knight.isAgainstWall = false;
                knight.setCurrentState(Entity.State.WALL_JUMPING);
                transitionTimer = 0.3f;
            } else if (knight.canDoubleJump) {
                knight.getVelocity().y = JUMP_VELOCITY * 0.8f;
                knight.setCurrentState(Entity.State.DOUBLE_JUMPING);
                knight.canDoubleJump = false;
                transitionTimer = 0;
            }
        }

        if (!Gdx.input.isKeyPressed(Keys.Z) && knight.getVelocity().y > 0 && !knight.pogoActive)
            knight.getVelocity().y *= 0.5f;

        if (Gdx.input.isKeyJustPressed(Keys.X) && knight.attackTimer <= 0) {
            knight.attackTimer = knight.attackCooldown;
            transitionTimer = 0;
            knight.requestSlashEffect = true;
            if (!knight.onGround && Gdx.input.isKeyPressed(Keys.DOWN)) {
                knight.setCurrentState(Entity.State.DOWN_SLASH);
            } else if (Gdx.input.isKeyPressed(Keys.UP)) {
                knight.setCurrentState(Entity.State.UP_SLASH);
            } else {
                alternateSlash = !alternateSlash;
                knight.setCurrentState(alternateSlash ? Entity.State.ATTACKING_ALT : Entity.State.ATTACKING);
            }
        }

        if (knight.attackTimer <= 0 && knight.onGround) {
            if (Gdx.input.isKeyJustPressed(Keys.UP)) knight.setCurrentState(Entity.State.LOOKING_UP);
            if (Gdx.input.isKeyJustPressed(Keys.DOWN)) knight.setCurrentState(Entity.State.LOOKING_DOWN);
        }
    }

    private boolean isBreakableTransition() {
        Entity.State s = knight.getCurrentState();
        return s == Entity.State.LANDING || s == Entity.State.RUN_TO_IDLE;
    }

    private void applyPhysics(float deltaTime) {
        Rectangle bounds = knight.getBoundingBox();

        if (knight.getCurrentState() == Entity.State.DASHING) {
            knight.dashTimer -= deltaTime;
            float spd = DASH_SPEED * (knight.charmSharpShadow ? 1.2f : 1f);
            knight.getVelocity().x = knight.isFacingRight() ? spd : -spd;
            knight.getVelocity().y = 0;
            if (knight.dashTimer <= 0) knight.setCurrentState(Entity.State.FALLING);
        } else {
            boolean wallSliding = knight.isAgainstWall && knight.getVelocity().y < 0
                    && ((knight.isFacingRight() && Gdx.input.isKeyPressed(Keys.RIGHT))
                     || (!knight.isFacingRight() && Gdx.input.isKeyPressed(Keys.LEFT)));
            knight.getVelocity().y = wallSliding
                    ? WALL_SLIDE_SPEED
                    : knight.getVelocity().y - GRAVITY * deltaTime;
        }

        bounds.x += knight.getVelocity().x * deltaTime;
        knight.isAgainstWall = false;
        for (Rectangle plat : level.getPlatforms()) {
            if (bounds.overlaps(plat)) {
                if (knight.getVelocity().x > 0) { bounds.x = plat.x - bounds.width; knight.isAgainstWall = true; }
                else if (knight.getVelocity().x < 0) { bounds.x = plat.x + plat.width; knight.isAgainstWall = true; }
                knight.getVelocity().x = 0;
            }
        }
        bounds.x = Math.max(0, Math.min(bounds.x, level.getWidth() - bounds.width));

        bounds.y += knight.getVelocity().y * deltaTime;
        knight.onGround = false;
        for (Rectangle plat : level.getPlatforms()) {
            if (bounds.overlaps(plat)) {
                if (knight.getVelocity().y <= 0) {
                    bounds.y = plat.y + plat.height;
                    knight.onGround = true;
                    knight.canDoubleJump = true;
                    knight.canDash = true;
                } else {
                    bounds.y = plat.y - bounds.height;
                }
                knight.getVelocity().y = 0;
            }
        }
        if (knight.getVelocity().y <= 0 || knight.onGround) knight.pogoActive = false;
    }

    private void checkSpikes() {
        if (knight.getCurrentState() == Entity.State.DEAD) return;
        if (knight.invincibleTimer > 0) return;

        for (Rectangle spike : level.getSpikes()) {
            if (!knight.getBoundingBox().overlaps(spike)) continue;
            knight.masks--;
            knight.focusTimer = 0f;
            if (knight.masks <= 0) {
                knight.setCurrentState(Entity.State.DEAD);
                deathTimer = 0f;
                knight.getVelocity().set(0, 0);
            } else {
                knight.invincibleTimer = 1.0f;
                knight.getVelocity().y = 400f;
                knight.getVelocity().x = knight.isFacingRight() ? -300f : 300f;
                knight.getBoundingBox().setPosition(
                        level.getLastSafePosition().x, level.getLastSafePosition().y);
            }
            break;
        }
    }

    private void checkVoidOut() {
        if (knight.getCurrentState() == Entity.State.DEAD) return;
        if (knight.getBoundingBox().y > VOID_Y) return;

        knight.masks--;
        knight.focusTimer = 0f;
        if (knight.masks <= 0) {
            knight.setCurrentState(Entity.State.DEAD);
            deathTimer = 0f;
            knight.getVelocity().set(0, 0);
        } else {
            knight.invincibleTimer = 1.0f;
            knight.getVelocity().set(0, 0);
            knight.getBoundingBox().setPosition(
                    level.getLastSafePosition().x, level.getLastSafePosition().y);
        }
    }

    private void updateSafePosition() {
        if (!knight.onGround) return;
        Rectangle bounds = knight.getBoundingBox();
        for (Rectangle spike : level.getSpikes())
            if (bounds.overlaps(spike)) return;
        level.updateSafePosition(bounds.x, bounds.y);
    }

    private void updateAnimationState(float deltaTime) {
        Entity.State state = knight.getCurrentState();
        switch (state) {
            case FOCUS_START: case FOCUSING: case FOCUS_GET: case FOCUS_END: return;
            default: break;
        }

        if (transitionTimer > 0) {
            transitionTimer -= deltaTime;
            if (transitionTimer > 0) return;
        }

        switch (state) {
            case DASHING: case ATTACKING: case ATTACKING_ALT:
            case UP_SLASH: case DOWN_SLASH:
            case CASTING_FIREBALL: case CASTING_SCREAM: return;
            default: break;
        }

        if (knight.onGround) {
            if (!wasOnGround) {
                knight.setCurrentState(Entity.State.LANDING);
                transitionTimer = 0.15f;
            } else if (Math.abs(knight.getVelocity().x) > 0) {
                knight.setCurrentState(Entity.State.RUNNING);
            } else if (previousState == Entity.State.RUNNING) {
                knight.setCurrentState(Entity.State.RUN_TO_IDLE);
                transitionTimer = 0.15f;
            } else {
                knight.setCurrentState(Entity.State.IDLE);
            }
        } else {
            if (knight.getVelocity().y > 0
                    && state != Entity.State.DOUBLE_JUMPING
                    && state != Entity.State.WALL_JUMPING) {
                knight.setCurrentState(Entity.State.JUMPING);
            } else if (knight.getVelocity().y <= 0) {
                knight.setCurrentState(knight.isAgainstWall ? Entity.State.WALL_SLIDING : Entity.State.FALLING);
            }
        }
    }
}