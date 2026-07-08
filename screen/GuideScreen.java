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
import HollowKnight.hollowknight.utils.Lang;

public class GuideScreen extends ScreenAdapter {
    private static final float MARGIN = 50f;
    private static final float WRAP_W = 800f - MARGIN * 2f;

    private final HollowKnightGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final GlyphLayout layout = new GlyphLayout();

    public GuideScreen(HollowKnightGame game) {
        this.game = game;
        camera.setToOrtho(false, 800, 600);
    }

    private String[] buildLines() {
        Keybinds kb = Keybinds.get();
        String move = kb.keyName(Action.LEFT) + "/" + kb.keyName(Action.RIGHT);
        String look = kb.keyName(Action.UP) + "/" + kb.keyName(Action.DOWN);
        return new String[] {
            Lang.t("guide_controls_title"),
            Lang.t("lbl_move") + " " + move + " | " + Lang.t("lbl_look") + " " + look + " | " + Lang.t("lbl_jump") + " " + kb.keyName(Action.JUMP) + " (" + Lang.t("guide_double") + ")",
            Lang.t("lbl_attack") + " " + kb.keyName(Action.ATTACK) + " | " + Lang.t("lbl_dash") + " " + kb.keyName(Action.DASH) + " | " + Lang.t("lbl_focus") + " " + kb.keyName(Action.FOCUS),
            Lang.t("lbl_spirit") + " " + kb.keyName(Action.CAST_SPIRIT) + " | " + Lang.t("lbl_wraiths") + " " + kb.keyName(Action.CAST_WRAITHS),
            Lang.t("lbl_inventory") + " " + kb.keyName(Action.INVENTORY) + " | " + Lang.t("lbl_pause") + " Esc",
            "",
            Lang.t("guide_abilities_title"),
            Lang.t("guide_ability_desc"),
            "",
            Lang.t("guide_cheats_title"),
            Lang.t("guide_cheats_desc"),
            "",
            Lang.t("esc_back")
        };
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