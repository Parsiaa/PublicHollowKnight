package HollowKnight.hollowknight.model;

public class Knight extends Entity {

    public int masks = 5;
    public final int maxMasks = 5;
    public int soul = 0;
    public final int maxSoul = 99;

    public float invincibleTimer = 0f;
    public float dashCooldown = 0f;
    public float dashTimer = 0f;
    public float focusTimer = 0f;
    public float attackTimer = 0f;
    public float castLockTimer = 0f;

    public boolean canDoubleJump = false;
    public boolean canDash = true;

    public boolean charmSoulCatcher = false;
    public boolean charmDashmaster = false;
    public boolean charmUnbreakableStrength = false;
    public boolean charmQuickSlash = false;
    public boolean charmQuickFocus = false;
    public boolean charmHeavyBlow = false;
    public boolean charmSharpShadow = false;
    public boolean charmVoidHeart = false;

    public int usedNotches = 0;
    public static final int MAX_NOTCHES = 3;

    public int soulPerHit = 11;
    public float dashCooldownBase = 0.8f;
    public int nailDamage = 1;
    public float attackCooldown = 0.3f;
    public float focusRequiredTime = 1.5f;

    public boolean requestVengefulSpirit = false;
    public boolean requestHowlingWraiths = false;
    public boolean requestDashEffect = false;
    public boolean requestSlashEffect = false;
    public boolean requestWings = false;

    public Knight(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    public void recalculateCharmStats() {
        soulPerHit = charmSoulCatcher ? 22 : 11;
        dashCooldownBase = charmDashmaster ? 0.4f : 0.8f;
        nailDamage = charmUnbreakableStrength ? 2 : 1;
        attackCooldown = charmQuickSlash ? 0.15f : 0.3f;
        focusRequiredTime = charmQuickFocus ? 0.75f : 1.5f;
    }

    public boolean equipCharm(int index) {
        if (usedNotches >= MAX_NOTCHES) return false;
        switch (index) {
            case 1: if (!charmSoulCatcher) { charmSoulCatcher = true; usedNotches++; } break;
            case 2: if (!charmDashmaster) { charmDashmaster = true; usedNotches++; } break;
            case 3: if (!charmUnbreakableStrength) { charmUnbreakableStrength = true; usedNotches++; } break;
            case 4: if (!charmQuickSlash) { charmQuickSlash = true; usedNotches++; } break;
            case 5: if (!charmQuickFocus) { charmQuickFocus = true; usedNotches++; } break;
            case 6: if (!charmHeavyBlow) { charmHeavyBlow = true; usedNotches++; } break;
            case 7: if (!charmSharpShadow) { charmSharpShadow = true; usedNotches++; } break;
            case 8: if (!charmVoidHeart) { charmVoidHeart = true; usedNotches++; } break;
        }
        recalculateCharmStats();
        return true;
    }

    public void unequipCharm(int index) {
        switch (index) {
            case 1: if (charmSoulCatcher) { charmSoulCatcher = false; usedNotches--; } break;
            case 2: if (charmDashmaster) { charmDashmaster = false; usedNotches--; } break;
            case 3: if (charmUnbreakableStrength) { charmUnbreakableStrength = false; usedNotches--; } break;
            case 4: if (charmQuickSlash) { charmQuickSlash = false; usedNotches--; } break;
            case 5: if (charmQuickFocus) { charmQuickFocus = false; usedNotches--; } break;
            case 6: if (charmHeavyBlow) { charmHeavyBlow = false; usedNotches--; } break;
            case 7: if (charmSharpShadow) { charmSharpShadow = false; usedNotches--; } break;
            case 8: if (charmVoidHeart) { charmVoidHeart = false; usedNotches--; } break;
        }
        recalculateCharmStats();
    }

    public boolean isCharmEquipped(int index) {
        switch (index) {
            case 1: return charmSoulCatcher;
            case 2: return charmDashmaster;
            case 3: return charmUnbreakableStrength;
            case 4: return charmQuickSlash;
            case 5: return charmQuickFocus;
            case 6: return charmHeavyBlow;
            case 7: return charmSharpShadow;
            case 8: return charmVoidHeart;
        }
        return false;
    }

    public void respawnAt(float x, float y) {
        getBoundingBox().setPosition(x, y);
        getVelocity().set(0, 0);
        masks = maxMasks;
        invincibleTimer = 2.0f;
        focusTimer = 0f;
        attackTimer = 0f;
        castLockTimer = 0f;
        canDash = true;
        canDoubleJump = false;
        setCurrentState(State.IDLE);
    }

    public static final float POGO_VELOCITY = 2400f;

    public boolean pogoActive = false;

    public void pogoBounce() {
        getVelocity().y = POGO_VELOCITY;
        canDoubleJump = true;
        canDash = true;
        pogoActive = true;
    }
}