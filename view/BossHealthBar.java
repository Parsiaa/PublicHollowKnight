package HollowKnight.hollowknight.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class BossHealthBar {
    private static final float BAR_W = 520f, BAR_H = 18f;
    private static final float BAR_X = (800f - BAR_W) / 2f, BAR_Y = 40f;

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final OrthographicCamera cam = new OrthographicCamera();
    private final ShapeRenderer shape = new ShapeRenderer();
    private final GlyphLayout layout = new GlyphLayout();

    private float shown = 1f;

    public BossHealthBar(SpriteBatch batch, BitmapFont font) {
        this.batch = batch;
        this.font = font;
        cam.setToOrtho(false, 800, 600);
    }

    public void render(float fraction, String name, float dt) {
        if (fraction < shown) shown += (fraction - shown) * Math.min(1f, dt * 6f);
        else shown = fraction;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shape.setProjectionMatrix(cam.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.6f);
        shape.rect(BAR_X - 4f, BAR_Y - 4f, BAR_W + 8f, BAR_H + 8f);
        shape.setColor(0.15f, 0.15f, 0.18f, 1f);
        shape.rect(BAR_X, BAR_Y, BAR_W, BAR_H);
        shape.setColor(0.7f, 0.12f, 0.12f, 1f);
        shape.rect(BAR_X, BAR_Y, BAR_W * Math.max(0f, shown), BAR_H);
        shape.end();

        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        layout.setText(font, name);
        font.draw(batch, layout, (800 - layout.width) / 2f, BAR_Y + BAR_H + 26f);
        batch.end();
    }

    public void dispose() { shape.dispose(); }
}