package HollowKnight.hollowknight.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import HollowKnight.hollowknight.HollowKnightGame;

public class GuideScreen extends ScreenAdapter {
    private static final float MARGIN = 50f;
    private static final float WRAP_W = 800f - MARGIN * 2f;

    private final HollowKnightGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final GlyphLayout layout = new GlyphLayout();

    private final String[] lines = {
        "CONTROLS",
        "Arrows - Move      Z - Jump / Double Jump      X - Attack      C - Dash",
        "A - Focus (heal)      Q - Vengeful Spirit      W - Howling Wraiths",
        "I - Inventory      Esc - Pause",
        "",
        "ABILITIES",
        "Hit enemies with the Nail to gain SOUL. Spend SOUL to Focus (heal) or cast spells.",
        "",
        "CHEATS (hold Left Ctrl)",
        "T Teleport   N Noclip   H Heal   R Refill Soul   G God Mode   K Kill All",
        "",
        "Esc - back"
    };

    public GuideScreen(HollowKnightGame game) {
        this.game = game;
        camera.setToOrtho(false, 800, 600);
    }

    @Override
    public void render(float dt) {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) { game.setScreen(new MainMenuScreen(game)); return; }

        ScreenUtils.clear(0.04f, 0.04f, 0.06f, 1f);
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.fontMedium.setColor(Color.WHITE);

        float y = 560f;
        for (String line : lines) {
            if (line.isEmpty()) { y -= 18f; continue; }
            layout.setText(game.fontMedium, line, Color.WHITE, WRAP_W, Align.left, true);
            game.fontMedium.draw(game.batch, layout, MARGIN, y);
            y -= layout.height + 14f;
        }
        game.batch.end();
    }
}