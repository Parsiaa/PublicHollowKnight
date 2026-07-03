package HollowKnight.hollowknight.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import HollowKnight.hollowknight.HollowKnightGame;
import HollowKnight.hollowknight.controller.SaveManager;
import HollowKnight.hollowknight.model.GameData;

public class SaveSlotScreen extends ScreenAdapter {
    private static final float TOP_Y = 430f, SPACING = 80f, ROW_H = 70f;
    private static final float ROW_X = 150f, ROW_W = 500f;

    private final HollowKnightGame game;
    private final OrthographicCamera cam = new OrthographicCamera();
    private final ShapeRenderer shape = new ShapeRenderer();
    private final Vector3 tmp = new Vector3();
    private final GameData[] slots = new GameData[SaveManager.SLOT_COUNT];

    private int selected = 0;
    private int prevMx, prevMy;
    private boolean disposed = false;

    public SaveSlotScreen(HollowKnightGame game) {
        this.game = game;
        cam.setToOrtho(false, 800, 600);
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < slots.length; i++) slots[i] = game.saveManager.load(i);
    }

    @Override
    public void render(float dt) {
        handleInput();
        if (game.getScreen() != this) return;
        draw();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) { goBack(); return; }
        if (Gdx.input.isKeyJustPressed(Keys.UP)) selected = (selected + slots.length - 1) % slots.length;
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) selected = (selected + 1) % slots.length;

        int mx = Gdx.input.getX(), my = Gdx.input.getY();
        boolean moved = (mx != prevMx || my != prevMy);
        prevMx = mx; prevMy = my;
        tmp.set(mx, my, 0); cam.unproject(tmp);
        int hov = rowAt(tmp.y);
        if (moved && hov != -1) selected = hov;

        if (Gdx.input.isKeyJustPressed(Keys.FORWARD_DEL)) {
            game.saveManager.delete(selected);
            refresh();
            return;
        }

        boolean activate = Gdx.input.isKeyJustPressed(Keys.ENTER)
                        || (hov != -1 && Gdx.input.justTouched());
        if (activate) {
            selected = (hov != -1 ? hov : selected);
            game.setScreen(new GameScreen(game, selected));
        }
    }

    private int rowAt(float worldY) {
        for (int i = 0; i < slots.length; i++) {
            float c = TOP_Y - i * SPACING;
            if (worldY >= c - ROW_H / 2f && worldY <= c + ROW_H / 2f) return i;
        }
        return -1;
    }

    private void draw() {
        ScreenUtils.clear(0.04f, 0.04f, 0.06f, 1f);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shape.setProjectionMatrix(cam.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < slots.length; i++) {
            float c = TOP_Y - i * SPACING;
            boolean sel = (i == selected);
            shape.setColor(sel ? 0.2f : 0.12f, sel ? 0.17f : 0.12f, sel ? 0.06f : 0.14f, 1f);
            shape.rect(ROW_X, c - ROW_H / 2f, ROW_W, ROW_H);
        }
        shape.end();

        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        game.fontLarge.setColor(0.85f, 0.7f, 0.15f, 1f);
        game.fontLarge.draw(game.batch, "SELECT SAVE", 250, 540);

        for (int i = 0; i < slots.length; i++) {
            float c = TOP_Y - i * SPACING;
            boolean sel = (i == selected);
            GameData d = slots[i];
            game.fontMedium.setColor(sel ? new Color(0.85f, 0.7f, 0.15f, 1f) : Color.WHITE);
            game.fontMedium.draw(game.batch, "Slot " + (i + 1), ROW_X + 20f, c + 18f);
            game.fontMedium.setColor(d == null ? new Color(0.5f, 0.5f, 0.5f, 1f)
                    : new Color(0.7f, 0.7f, 0.7f, 1f));
            String info = (d == null) ? "Empty - New Game"
                    : d.masks + " masks     " + d.soul + " soul     " + formatTime(d.playTimeMillis);
            game.fontMedium.draw(game.batch, info, ROW_X + 20f, c - 10f);
        }

        game.fontMedium.setColor(0.5f, 0.5f, 0.5f, 1f);
        game.fontMedium.draw(game.batch, "Enter/Click: play     Del: erase slot     Esc: back", 150, 70);
        game.batch.end();
    }

    private String formatTime(long millis) {
        long total = millis / 1000;
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    private void goBack() {
        dispose();
        game.setScreen(new MainMenuScreen(game));
    }

    @Override
    public void dispose() {
        if (disposed) return;
        disposed = true;
        shape.dispose();
    }
}