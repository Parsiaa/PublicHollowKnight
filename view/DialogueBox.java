package HollowKnight.hollowknight.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

public class DialogueBox {
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final OrthographicCamera cam = new OrthographicCamera();
    private final ShapeRenderer shape = new ShapeRenderer();
    private final GlyphLayout layout = new GlyphLayout();
    private boolean disposed = false;

    public DialogueBox(SpriteBatch batch, BitmapFont font) {
        this.batch = batch;
        this.font = font;
        cam.setToOrtho(false, 800, 600);
    }

    public void renderPrompt(String text) {
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        font.setColor(0.9f, 0.85f, 0.4f, 1f);
        layout.setText(font, text);
        font.draw(batch, layout, (800 - layout.width) / 2f, 130);
        batch.end();
    }

    public void renderDialogue(String speaker, String visibleText) {
        float x = 80, y = 40, w = 640, h = 150;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shape.setProjectionMatrix(cam.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.8f);
        shape.rect(x, y, w, h);
        shape.setColor(0.85f, 0.7f, 0.15f, 1f);
        shape.rect(x, y + h - 4, w, 4);
        shape.rect(x, y, w, 4);
        shape.end();

        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        font.setColor(0.85f, 0.7f, 0.15f, 1f);
        font.draw(batch, speaker, x + 20, y + h - 18);
        font.setColor(Color.WHITE);
        layout.setText(font, visibleText, Color.WHITE, w - 40, Align.left, true);
        font.draw(batch, layout, x + 20, y + h - 52);
        font.setColor(0.5f, 0.5f, 0.5f, 1f);
        font.draw(batch, "Enter", x + w - 100, y + 28);
        batch.end();
    }

    public void dispose() {
        if (disposed) return;
        disposed = true;
        shape.dispose();
    }
}