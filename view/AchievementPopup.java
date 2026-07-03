package HollowKnight.hollowknight.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayDeque;
import java.util.Deque;

import HollowKnight.hollowknight.controller.AchievementListener;
import HollowKnight.hollowknight.model.Achievement;

public class AchievementPopup implements AchievementListener {

    private static final float W = 360f;
    private static final float H = 70f;
    private static final float MARGIN = 20f;
    private static final float SLIDE = 0.35f;
    private static final float HOLD = 2.8f;

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final OrthographicCamera cam = new OrthographicCamera();
    private final ShapeRenderer shape = new ShapeRenderer();
    private final GlyphLayout layout = new GlyphLayout();

    private final Deque<Achievement> queue = new ArrayDeque<>();
    private Achievement current = null;
    private float timer = 0f;

    public AchievementPopup(SpriteBatch batch, BitmapFont font) {
        this.batch = batch;
        this.font = font;
        cam.setToOrtho(false, 800, 600);
    }

    @Override
    public void onUnlocked(Achievement achievement) {
        queue.addLast(achievement);
    }

    public void update(float dt) {
        if (current == null && !queue.isEmpty()) {
            current = queue.pollFirst();
            timer = 0f;
        }
        if (current == null) return;
        timer += dt;
        if (timer >= SLIDE + HOLD + SLIDE) current = null;
    }

    public void render() {
        if (current == null) return;

        float restX = 800 - W - MARGIN;
        float x;
        if (timer < SLIDE) {
            x = lerp(800, restX, ease(timer / SLIDE));
        } else if (timer < SLIDE + HOLD) {
            x = restX;
        } else {
            x = lerp(restX, 800, ease((timer - SLIDE - HOLD) / SLIDE));
        }
        float y = 600 - H - MARGIN;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shape.setProjectionMatrix(cam.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.82f);
        shape.rect(x, y, W, H);
        shape.setColor(0.85f, 0.7f, 0.15f, 1f);
        shape.rect(x, y, 5f, H);
        shape.end();

        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        font.setColor(0.85f, 0.7f, 0.15f, 1f);
        font.draw(batch, "Achievement Unlocked", x + 18f, y + H - 16f);
        font.setColor(Color.WHITE);
        layout.setText(font, current.title);
        font.draw(batch, layout, x + 18f, y + 26f);
        font.setColor(Color.WHITE);
        batch.end();
    }

    private static float ease(float t) {
        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;
        return t * t * (3f - 2f * t);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public void dispose() {
        shape.dispose();
    }
}