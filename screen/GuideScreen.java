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
import HollowKnight.hollowknight.model.Keybinds;
import HollowKnight.hollowknight.model.Keybinds.Action;

public class GuideScreen extends ScreenAdapter {
    private static final float MARGIN = 50f;
    private static final float WRAP_W = 800f - MARGIN * 2f;

    private final HollowKnightGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final GlyphLayout layout = new GlyphLayout();

    private String[] buildLines() {
        Keybinds kb = Keybinds.get();
        String move = kb.keyName(Action.LEFT) + "/" + kb.keyName(Action.RIGHT);
        return new String[] {
            "CONTROLS",
            move + " - Move      " + kb.keyName(Action.JUMP) + " - Jump / Double Jump      "
                + kb.keyName(Action.ATTACK) + " - Attack      " + kb.keyName(Action.DASH) + " - Dash",
            kb.keyName(Action.FOCUS) + " - Focus (heal)      " + kb.keyName(Action.CAST_SPIRIT)
                + " - Vengeful Spirit      " + kb.keyName(Action.CAST_WRAITHS) + " - Howling Wraiths",
            kb.keyName(Action.INVENTORY) + " - Inventory      Esc - Pause",
            "",
            "ABILITIES",
            "Hit enemies with the Nail to gain SOUL. Spend SOUL to Focus (heal) or cast spells.",
            "",
            "CHEATS (hold Left Ctrl)",
            "T Teleport   N Noclip   H Heal   R Refill Soul   G God Mode   K Kill All",
            "",
            "Esc - back"
        };
    }

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
        game.menuBackground.drawBackground(game.batch);
        game.fontMedium.setColor(Color.WHITE);

        float y = 560f;
        for (String line : buildLines()) {
            if (line.isEmpty()) { y -= 18f; continue; }
            layout.setText(game.fontMedium, line, Color.WHITE, WRAP_W, Align.left, true);
            game.fontMedium.draw(game.batch, layout, MARGIN, y);
            y -= layout.height + 14f;
        }
        game.batch.end();
    }
}