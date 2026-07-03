package HollowKnight.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Rectangle;

import HollowKnight.hollowknight.model.Entity;
import HollowKnight.hollowknight.model.Knight;
import HollowKnight.hollowknight.model.Level;
import HollowKnight.hollowknight.model.Zote;
import HollowKnight.hollowknight.utils.AudioManager;
import HollowKnight.hollowknight.view.DialogueBox;

public class ZoteController {
    private static final float TALK_RANGE = 220f;
    private static final float CHARS_PER_SEC = 38f;
    private static final float NAIL_REACH = 130f;
    private static final float GRAVITY = 1800f;

    private static final float ATTACK_DURATION = 0.55f;
    private static final float ROLL_DURATION = 0.45f;
    private static final float FALL_DURATION = 0.4f;
    private static final float GET_UP_DURATION = 0.55f;

    private static final float LUNGE_SPEED = 520f;
    private static final float ROLL_SPEED = 380f;

    private static final String[] GRUNTS = {
        "audio/zote/zote1.ogg", "audio/zote/zote2.ogg",
        "audio/zote/zote3.ogg", "audio/zote/zote4.ogg",
        "audio/zote/zote5.ogg"
    };

    private final String[] mainLines = {
        "Halt! I am Zote the Mighty, a knight of great renown.",
        "I am on a quest to slay a thousand beasts with my trusty nail, Life Ender.",
        "Do not get in my way, little ghost, or you shall be the next to fall."
    };

    private final String[] precepts = {
        "Precept One: 'Always Win Your Battles.' Losing earns you nothing and teaches you nothing.",
        "Precept Two: 'Never Let Them Laugh At You.' Mocking laughter is the weapon of cowards.",
        "Precept Three: 'Always Be Rested.' Fighting and adventuring take their toll on the body.",
        "Precept Four: 'Forget Your Past.' Thinking on your past can only bring you misery.",
        "Precept Five: 'Strength Beats Strength.' Match your enemy's strength with your own."
    };

    private final Zote zote;
    private final Knight knight;
    private final AudioManager audio;
    private final DialogueBox box;
    private final Level level;

    private boolean inRange = false;
    private boolean inDialogue = false;
    private boolean mainCompleted = false;
    private int preceptIndex = 0;

    private String[] activeLines = null;
    private int lineIndex = 0;
    private float charCount = 0f;

    private boolean reacting = false;
    private boolean wasAttacking = false;

    public ZoteController(Zote zote, Knight knight, AudioManager audio, DialogueBox box, Level level) {
        this.zote = zote;
        this.knight = knight;
        this.audio = audio;
        this.box = box;
        this.level = level;
    }

    public boolean isInDialogue() { return inDialogue; }

    public void update(float dt) {
        zote.tick(dt);
        checkForHit();

        if (reacting) {
            updateReaction(dt);
            applyGravity(dt);
            return;
        }

        float dist = distanceToKnight();
        inRange = dist <= TALK_RANGE && knight.onGround;

        if (!inDialogue) {
            zote.facingRight = knight.getBoundingBox().x > zote.getBounds().x;
            if (inRange && Gdx.input.isKeyJustPressed(Keys.E)) startDialogue();
            applyGravity(dt);
            return;
        }

        knight.getVelocity().set(0, 0);
        String line = activeLines[lineIndex];
        if (charCount < line.length()) charCount += dt * CHARS_PER_SEC;

        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            if (charCount < line.length()) charCount = line.length();
            else advanceLine();
        }
        applyGravity(dt);
    }

    private void applyGravity(float dt) {
        zote.getVelocity().y -= GRAVITY * dt;
        zote.getBounds().y += zote.getVelocity().y * dt;
        Rectangle b = zote.getBounds();
        for (Rectangle plat : level.getPlatforms()) {
            if (b.overlaps(plat) && zote.getVelocity().y <= 0) {
                b.y = plat.y + plat.height;
                zote.getVelocity().y = 0;
                break;
            }
        }
    }

    private void startDialogue() {
        inDialogue = true;
        lineIndex = 0;
        charCount = 0f;
        activeLines = mainCompleted ? new String[]{ precepts[preceptIndex] } : mainLines;
        zote.setState(Zote.State.TALK);
        knight.getVelocity().set(0, 0);
        knight.setCurrentState(Entity.State.IDLE);
        playGrunt();
    }

    private void advanceLine() {
        lineIndex++;
        if (lineIndex >= activeLines.length) {
            endDialogue();
        } else {
            charCount = 0f;
            playGrunt();
        }
    }

    private void endDialogue() {
        inDialogue = false;
        zote.setState(Zote.State.IDLE);
        if (!mainCompleted) mainCompleted = true;
        else preceptIndex = (preceptIndex + 1) % precepts.length;
    }

    private void checkForHit() {
        boolean attacking = isAttackState(knight.getCurrentState());
        if (attacking && !wasAttacking && nailHitsZote() && !reacting) startReaction();
        wasAttacking = attacking;
    }

    private void startReaction() {
        reacting = true;
        inDialogue = false;
        inRange = false;
        zote.facingRight = knight.getBoundingBox().x > zote.getBounds().x;
        float lungeDir = zote.facingRight ? 1f : -1f;
        zote.setState(Zote.State.ATTACK);
        zote.getVelocity().x = lungeDir * LUNGE_SPEED;
        playGrunt();
    }

    private void updateReaction(float dt) {
        zote.getBounds().x += zote.getVelocity().x * dt;
        zote.getVelocity().x *= (1f - 4f * dt);

        switch (zote.getState()) {
            case ATTACK:
                if (zote.getStateTime() >= ATTACK_DURATION) {
                    float rollDir = zote.facingRight ? -1f : 1f;
                    zote.getVelocity().x = rollDir * ROLL_SPEED;
                    zote.setState(Zote.State.ROLL);
                }
                break;
            case ROLL:
                if (zote.getStateTime() >= ROLL_DURATION) {
                    zote.setState(Zote.State.FALL);
                }
                break;
            case FALL:
                if (zote.getStateTime() >= FALL_DURATION) {
                    zote.getVelocity().x = 0f;
                    zote.setState(Zote.State.GET_UP);
                }
                break;
            case GET_UP:
                if (zote.getStateTime() >= GET_UP_DURATION) {
                    reacting = false;
                    zote.setState(Zote.State.IDLE);
                }
                break;
            default:
                reacting = false;
                zote.setState(Zote.State.IDLE);
                break;
        }
    }

    private boolean isAttackState(Entity.State s) {
        return s == Entity.State.ATTACKING || s == Entity.State.ATTACKING_ALT
            || s == Entity.State.UP_SLASH || s == Entity.State.DOWN_SLASH;
    }

    private boolean nailHitsZote() {
        Rectangle b = knight.getBoundingBox();
        Rectangle nail = knight.isFacingRight()
            ? new Rectangle(b.x + b.width, b.y, NAIL_REACH, b.height)
            : new Rectangle(b.x - NAIL_REACH, b.y, NAIL_REACH, b.height);
        return nail.overlaps(zote.getBounds());
    }

    private void playGrunt() {
        audio.playSound(GRUNTS[(int) (Math.random() * GRUNTS.length)]);
    }

    private float distanceToKnight() {
        float kx = knight.getBoundingBox().x + knight.getBoundingBox().width / 2f;
        float ky = knight.getBoundingBox().y + knight.getBoundingBox().height / 2f;
        float zx = zote.getBounds().x + zote.getBounds().width / 2f;
        float zy = zote.getBounds().y + zote.getBounds().height / 2f;
        float dx = kx - zx, dy = ky - zy;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public void renderUI() {
        if (inDialogue) {
            String line = activeLines[lineIndex];
            int n = Math.min(line.length(), (int) charCount);
            box.renderDialogue("Zote the Mighty", line.substring(0, n));
        } else if (inRange) {
            box.renderPrompt("Press E to speak with Zote");
        }
    }
}