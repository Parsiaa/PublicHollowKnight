package HollowKnight.hollowknight.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import HollowKnight.hollowknight.HollowKnightGame;
import HollowKnight.hollowknight.model.Knight;
import HollowKnight.hollowknight.utils.FontFactory;


public class InventoryMenu {

    private static final int   CHARM_COUNT   = 8;
    private static final float SLOT_SIZE     = 90f;
    private static final float SLOT_PAD      = 20f;
    private static final float GRID_COLS     = 4f;
    private static final float ICON_SCALE    = 0.62f;

    private static final String[] CHARM_NAMES = {
        "Soul Catcher",        "Dashmaster",
        "Unbreakable Strength","Quick Slash",
        "Quick Focus",         "Heavy Blow",
        "Sharp Shadow",        "Void Heart"
    };

    private static final String[] CHARM_DESC = {
        "Gain more SOUL per nail hit",
        "Dash cooldown reduced",
        "Nail attacks deal extra damage",
        "Attack speed greatly increased",
        "Focus heals in half the time",
        "Knockback force increased",
        "Dash through enemies for damage",
        "Spell damage increased by 50%"
    };

    private final Knight  knight;
    private final Texture[] charmIcons = new Texture[CHARM_COUNT];
    private final OrthographicCamera uiCamera;
    private final ShapeRenderer sr;
    private final BitmapFont font;
    private final GlyphLayout layout;

    private boolean open        = false;
    private int     hoveredSlot = -1;


    private float gridOriginX, gridOriginY;

    public InventoryMenu(Knight knight, float screenW, float screenH) {
        this.knight   = knight;
        this.uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, screenW, screenH);
        this.sr     = new ShapeRenderer();
        this.font   = FontFactory.generate(HollowKnightGame.FONT_PATH, 18);
        this.layout = new GlyphLayout();

        font.setColor(Color.WHITE);

        float gridW = GRID_COLS * SLOT_SIZE + (GRID_COLS - 1) * SLOT_PAD;
        float gridH = 2 * SLOT_SIZE + SLOT_PAD;
        gridOriginX = (screenW - gridW) / 2f;
        gridOriginY = (screenH - gridH) / 2f - 20f;

        for (int i = 0; i < CHARM_COUNT; i++) {
            charmIcons[i] = new Texture(
                Gdx.files.internal("animation/Charms/charm_" + (i + 1) + ".png"));
        }
    }

    public boolean isOpen() { return open; }

    public void toggle() { open = !open; hoveredSlot = -1; }

    public void update() {
        if (!open) return;

        if (Gdx.input.isKeyJustPressed(Keys.I) || Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            open = false;
            return;
        }

        com.badlogic.gdx.math.Vector3 m = new com.badlogic.gdx.math.Vector3(
                Gdx.input.getX(), Gdx.input.getY(), 0);
        uiCamera.unproject(m);

        hoveredSlot = -1;
        for (int i = 0; i < CHARM_COUNT; i++) {
            Rectangle slot = slotBounds(i);
            if (slot.contains(m.x, m.y)) {
                hoveredSlot = i;
                if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
                    toggleCharm(i + 1);
                }
                break;
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (!open) return;

        batch.setProjectionMatrix(uiCamera.combined);

        batch.end();
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.78f);
        sr.rect(0, 0, uiCamera.viewportWidth, uiCamera.viewportHeight);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < CHARM_COUNT; i++) {
            Rectangle slot = slotBounds(i);
            boolean equipped = knight.isCharmEquipped(i + 1);
            boolean hovered  = (hoveredSlot == i);
            boolean full     = (!equipped && knight.usedNotches >= Knight.MAX_NOTCHES);

            if (equipped) {
                sr.setColor(0.85f, 0.7f, 0.15f, 1f);
            } else if (hovered && !full) {
                sr.setColor(0.55f, 0.55f, 0.55f, 1f);
            } else {
                sr.setColor(0.2f, 0.2f, 0.2f, 1f);
            }
            sr.rect(slot.x - 4, slot.y - 4, slot.width + 8, slot.height + 8);

            sr.setColor(0.08f, 0.08f, 0.12f, 1f);
            sr.rect(slot.x, slot.y, slot.width, slot.height);
        }

        drawNotchBar(sr);
        sr.end();

        batch.begin();
        for (int i = 0; i < CHARM_COUNT; i++) {
            Rectangle slot = slotBounds(i);
            boolean equipped = knight.isCharmEquipped(i + 1);
            boolean full     = (!equipped && knight.usedNotches >= Knight.MAX_NOTCHES);

            float alpha = full ? 0.35f : 1f;
            batch.setColor(1f, 1f, 1f, alpha);

            float iconSize = SLOT_SIZE * ICON_SCALE;
            float ix = slot.x + (SLOT_SIZE - iconSize) / 2f;
            float iy = slot.y + (SLOT_SIZE - iconSize) / 2f;
            batch.draw(charmIcons[i], ix, iy, iconSize, iconSize);
        }
        batch.setColor(Color.WHITE);


        layout.setText(font, "C H A R M S");
        font.setColor(0.85f, 0.7f, 0.15f, 1f);
        font.draw(batch, layout,
            (uiCamera.viewportWidth - layout.width) / 2f,
            gridOriginY + 2 * SLOT_SIZE + SLOT_PAD + 60f);
        font.setColor(Color.WHITE);

        if (hoveredSlot >= 0) {
            String name = CHARM_NAMES[hoveredSlot];
            String desc = CHARM_DESC[hoveredSlot];
            boolean full = (!knight.isCharmEquipped(hoveredSlot + 1)
                            && knight.usedNotches >= Knight.MAX_NOTCHES);
            font.setColor(full ? Color.RED : Color.WHITE);
            layout.setText(font, name);
            font.draw(batch, layout,
                (uiCamera.viewportWidth - layout.width) / 2f,
                gridOriginY - 20f);
            font.setColor(0.75f, 0.75f, 0.75f, 1f);
            layout.setText(font, desc);
            font.draw(batch, layout,
                (uiCamera.viewportWidth - layout.width) / 2f,
                gridOriginY - 44f);
        }


        font.setColor(Color.WHITE);
        String notchText = "Notches: " + knight.usedNotches + " / " + Knight.MAX_NOTCHES;
        layout.setText(font, notchText);
        font.draw(batch, layout,
            (uiCamera.viewportWidth - layout.width) / 2f, 60f);

        font.setColor(0.5f, 0.5f, 0.5f, 1f);
        layout.setText(font, "[ I ]  Close");
        font.draw(batch, layout,
            (uiCamera.viewportWidth - layout.width) / 2f, 30f);

        font.setColor(Color.WHITE);
    }

    private void drawNotchBar(ShapeRenderer sr) {
        float barW = 200f;
        float barH = 14f;
        float bx   = (uiCamera.viewportWidth - barW) / 2f;
        float by   = gridOriginY - 80f;

        sr.setColor(0.3f, 0.3f, 0.3f, 1f);
        sr.rect(bx, by, barW, barH);

        float filled = (Knight.MAX_NOTCHES > 0) ? (float) knight.usedNotches / Knight.MAX_NOTCHES : 0f;
        sr.setColor(0.85f, 0.7f, 0.15f, 1f);
        sr.rect(bx, by, barW * filled, barH);
    }

    private Rectangle slotBounds(int index) {
        int col = index % (int) GRID_COLS;
        int row = index / (int) GRID_COLS;
        float x = gridOriginX + col * (SLOT_SIZE + SLOT_PAD);
        float y = gridOriginY + (1 - row) * (SLOT_SIZE + SLOT_PAD);
        return new Rectangle(x, y, SLOT_SIZE, SLOT_SIZE);
    }

    private void toggleCharm(int index) {
        if (knight.isCharmEquipped(index)) {
            knight.unequipCharm(index);
        } else {
            if (knight.usedNotches >= Knight.MAX_NOTCHES) return;
            knight.equipCharm(index);
        }
    }

    public void dispose() {
        for (Texture t : charmIcons) if (t != null) t.dispose();
        sr.dispose();
        font.dispose();
    }
}