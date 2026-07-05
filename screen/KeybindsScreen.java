package HollowKnight.hollowknight.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import HollowKnight.hollowknight.HollowKnightGame;
import HollowKnight.hollowknight.model.Keybinds;
import HollowKnight.hollowknight.model.Keybinds.Action;
import HollowKnight.hollowknight.utils.Lang;

public class KeybindsScreen extends ScreenAdapter {
    private static final float TOP = 500f, SPACING = 34f, ROW_H = 30f;
    private static final float LABEL_X = 190f, KEY_X = 500f;

    private final HollowKnightGame game;
    private final Screen back;
    private final OrthographicCamera cam = new OrthographicCamera();
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector3 tmp = new Vector3();
    private final Action[] actions = Action.values();
    private final int rows = actions.length + 2;

    private int selected = 0;
    private boolean rebinding = false;
    private int prevMx, prevMy;

    public KeybindsScreen(HollowKnightGame game, Screen back) {
        this.game = game;
        this.back = back;
        cam.setToOrtho(false, 800, 600);
    }

    @Override
    public void render(float dt) {
        handleInput();
        if (game.getScreen() != this) return;
        draw();
    }

    private void handleInput() {
        if (rebinding) {
            for (int k = 0; k <= 255; k++) {
                if (Gdx.input.isKeyJustPressed(k)) {
                    if (k != Keys.ESCAPE) Keybinds.get().set(actions[selected], k);
                    rebinding = false;
                    return;
                }
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) { goBack(); return; }
        if (Gdx.input.isKeyJustPressed(Keys.UP)) selected = (selected + rows - 1) % rows;
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) selected = (selected + 1) % rows;

        int mx = Gdx.input.getX(), my = Gdx.input.getY();
        boolean moved = (mx != prevMx || my != prevMy);
        prevMx = mx; prevMy = my;
        tmp.set(mx, my, 0); cam.unproject(tmp);
        int hov = rowAt(tmp.y);
        if (moved && hov != -1) selected = hov;

        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || (hov != -1 && Gdx.input.justTouched())) {
            if (hov != -1) selected = hov;
            activate();
        }
    }

    private void activate() {
        if (selected < actions.length) rebinding = true;
        else if (selected == actions.length) Keybinds.get().resetDefaults();
        else goBack();
    }

    private int rowAt(float worldY) {
        for (int i = 0; i < rows; i++) {
            float c = TOP - i * SPACING - 8f;
            if (worldY >= c - ROW_H / 2f && worldY <= c + ROW_H / 2f) return i;
        }
        return -1;
    }

    private void draw() {
        ScreenUtils.clear(0.04f, 0.04f, 0.06f, 1f);

        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        game.menuBackground.drawBackground(game.batch);

        game.fontLarge.setColor(0.85f, 0.7f, 0.15f, 1f);
        layout.setText(game.fontLarge, Lang.t("controls"));
        game.fontLarge.draw(game.batch, layout, (800 - layout.width) / 2f, 560);

        Keybinds kb = Keybinds.get();
        for (int i = 0; i < actions.length; i++) {
            boolean sel = (i == selected);
            game.fontMedium.setColor(sel ? new Color(0.85f, 0.7f, 0.15f, 1f) : Color.WHITE);
            game.fontMedium.draw(game.batch, actions[i].label, LABEL_X, TOP - i * SPACING);
            String key = (sel && rebinding) ? "..." : kb.keyName(actions[i]);
            game.fontMedium.setColor(sel && rebinding ? new Color(0.9f, 0.85f, 0.4f, 1f)
                    : (sel ? new Color(0.85f, 0.7f, 0.15f, 1f) : new Color(0.7f, 0.7f, 0.7f, 1f)));
            game.fontMedium.draw(game.batch, key, KEY_X, TOP - i * SPACING);
        }

        game.fontMedium.setColor(selected == actions.length ? new Color(0.85f, 0.7f, 0.15f, 1f) : Color.WHITE);
        game.fontMedium.draw(game.batch, "Reset Defaults", LABEL_X, TOP - actions.length * SPACING);
        game.fontMedium.setColor(selected == actions.length + 1 ? new Color(0.85f, 0.7f, 0.15f, 1f) : Color.WHITE);
        game.fontMedium.draw(game.batch, Lang.t("back"), LABEL_X, TOP - (actions.length + 1) * SPACING);

        game.fontMedium.setColor(0.5f, 0.5f, 0.5f, 1f);
        String hint = rebinding ? "Press a key to bind    (Esc: cancel)"
                : "Enter: rebind    Arrows: navigate    Esc: back";
        layout.setText(game.fontMedium, hint);
        game.fontMedium.draw(game.batch, layout, (800 - layout.width) / 2f, 48);

        game.batch.end();
    }

    private void goBack() {
        game.setScreen(back);
    }
}
