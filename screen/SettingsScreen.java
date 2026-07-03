package HollowKnight.hollowknight.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import HollowKnight.hollowknight.HollowKnightGame;
import HollowKnight.hollowknight.model.Settings;

public class SettingsScreen extends ScreenAdapter {
    private static final float TOP = 470f, SPACING = 64f, ROW_H = 52f;
    private static final float TRACK_X = 420f, TRACK_W = 280f, TRACK_H = 10f;
    private static final int ROWS = 6;       // 0 musicVol, 1 musicToggle, 2 sfxVol, 3 sfxToggle, 4 resetSfx, 5 back

    private final HollowKnightGame game;
    private final Screen back;
    private final OrthographicCamera cam = new OrthographicCamera();
    private final ShapeRenderer shape = new ShapeRenderer();
    private final Vector3 tmp = new Vector3();
    private final Settings s = Settings.get();
    
    private int selected = 0;
    private int prevMx, prevMy;
    private boolean disposed = false;

    public SettingsScreen(HollowKnightGame game, Screen back) {
        this.game = game;
        this.back = back;
        cam.setToOrtho(false, 800, 600);
    }

    private float rowY(int i) { return TOP - i * SPACING; }

    private int rowAt(float worldY) {
        for (int i = 0; i < ROWS; i++) {
            float c = rowY(i) - 8f;
            if (worldY >= c - ROW_H / 2f && worldY <= c + ROW_H / 2f) return i;
        }
        return -1;
    }

    @Override
    public void render(float dt) {
        handleInput();
        draw();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) { goBack(); return; }
        if (Gdx.input.isKeyJustPressed(Keys.UP))   selected = (selected + ROWS - 1) % ROWS;
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) selected = (selected + 1) % ROWS;

        int mx = Gdx.input.getX(), my = Gdx.input.getY();
        boolean moved = (mx != prevMx || my != prevMy);
        prevMx = mx; prevMy = my;
        tmp.set(mx, my, 0); cam.unproject(tmp);
        int hov = rowAt(tmp.y);
        if (moved && hov != -1) selected = hov;

        // Slider keyboard nudge
        if (selected == 0 || selected == 2) {
            if (Gdx.input.isKeyJustPressed(Keys.LEFT))  setVolume(selected, volume(selected) - 0.05f);
            if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) setVolume(selected, volume(selected) + 0.05f);
        }

        if (Gdx.input.justTouched() && hov != -1) {
            selected = hov;
            if (hov == 0 || hov == 2) setVolume(hov, (tmp.x - TRACK_X) / TRACK_W);
            else activate(hov);
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) activate(selected);
    }

    private float volume(int row) { return row == 0 ? s.musicVolume : s.sfxVolume; }

    private void setVolume(int row, float v) {
        v = Math.max(0f, Math.min(1f, v));
        if (row == 0) s.musicVolume = v; else s.sfxVolume = v;
        s.save();
        game.audio.applySettings();
    }

    private void activate(int row) {
        switch (row) {
            case 1: s.musicEnabled = !s.musicEnabled; break;
            case 3: s.sfxEnabled = !s.sfxEnabled; break;
            case 4: s.resetSfx(); break;
            case 5: goBack(); return;
            default: return;
        }
        s.save();
        game.audio.applySettings();
    }

    private void draw() {
        ScreenUtils.clear(0.04f, 0.04f, 0.06f, 1f);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shape.setProjectionMatrix(cam.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        drawSlider(0, s.musicVolume);
        drawSlider(2, s.sfxVolume);
        shape.end();

        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();

        game.fontLarge.setColor(0.85f, 0.7f, 0.15f, 1f);
        game.fontLarge.draw(game.batch, "SETTINGS", 300, 560);

        label(0, "Music Volume", Math.round(s.musicVolume * 100) + "%");
        label(1, "Music", s.musicEnabled ? "On" : "Off");
        label(2, "SFX Volume", Math.round(s.sfxVolume * 100) + "%");
        label(3, "SFX", s.sfxEnabled ? "On" : "Off");
        label(4, "Reset SFX", "");
        label(5, "Back", "");

        game.fontMedium.setColor(0.5f, 0.5f, 0.5f, 1f);
        game.fontMedium.draw(game.batch, "Arrows: navigate / adjust    Enter / Click: select    Esc: back", 120, 70);
        game.batch.end();
    }

    private void drawSlider(int row, float value) {
        float y = rowY(row) - 8f - TRACK_H / 2f;
        shape.setColor(0.25f, 0.25f, 0.3f, 1f);
        shape.rect(TRACK_X, y, TRACK_W, TRACK_H);
        shape.setColor(0.85f, 0.7f, 0.15f, 1f);
        shape.rect(TRACK_X, y, TRACK_W * value, TRACK_H);
        shape.setColor(Color.WHITE);
        shape.rect(TRACK_X + TRACK_W * value - 4f, y - 6f, 8f, TRACK_H + 12f);
    }

    private void label(int row, String text, String value) {
        boolean sel = (row == selected);
        game.fontMedium.setColor(sel ? new Color(0.85f, 0.7f, 0.15f, 1f) : Color.WHITE);
        game.fontMedium.draw(game.batch, text, 120, rowY(row));
        if (!value.isEmpty()) game.fontMedium.draw(game.batch, value, TRACK_X, rowY(row));
    }


    private void goBack() {
        s.save();
        game.setScreen(back);
    }

    @Override
    public void dispose() {
        if (disposed) return;
        disposed = true;
        shape.dispose();
    }
}