package HollowKnight.hollowknight.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import HollowKnight.hollowknight.HollowKnightGame;
import HollowKnight.hollowknight.utils.Lang;

public class VictoryScreen extends ScreenAdapter {
    private static final float TOP_Y = 300f, SPACING = 60f, ROW_H = 50f;
    private static final int ITEM_COUNT = 2;

    private final HollowKnightGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector3 tmp = new Vector3();

    private final int deaths;
    private final int kills;
    private final long timeMillis;
    private final int saveSlot;

    private int selected = 0;
    private int prevMx, prevMy;

    public VictoryScreen(HollowKnightGame game, int deaths, int kills, long timeMillis, int saveSlot) {
        this.game = game;
        this.deaths = deaths;
        this.kills = kills;
        this.timeMillis = timeMillis;
        this.saveSlot = saveSlot;
        camera.setToOrtho(false, 800, 600);
    }

    @Override
    public void render(float dt) {
        if (Gdx.input.isKeyJustPressed(Keys.UP)) selected = (selected + ITEM_COUNT - 1) % ITEM_COUNT;
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) selected = (selected + 1) % ITEM_COUNT;

        int mx = Gdx.input.getX(), my = Gdx.input.getY();
        boolean moved = (mx != prevMx || my != prevMy);
        prevMx = mx; prevMy = my;
        tmp.set(mx, my, 0);
        camera.unproject(tmp);
        int hovered = rowAt(tmp.y);
        if (moved && hovered != -1) selected = hovered;

        boolean activate = Gdx.input.isKeyJustPressed(Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Keys.Z)
                || (hovered != -1 && Gdx.input.justTouched());

        ScreenUtils.clear(0.04f, 0.04f, 0.06f, 1f);
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        game.fontLarge.setColor(0.85f, 0.7f, 0.15f, 1f);
        layout.setText(game.fontLarge, Lang.t("victory"));
        game.fontLarge.draw(game.batch, layout, (800 - layout.width) / 2f, 520);

        game.fontMedium.setColor(Color.WHITE);
        drawCentered(Lang.t("lbl_deaths") + ": " + deaths, 430);
        drawCentered(Lang.t("lbl_enemies_slain") + ": " + kills, 395);
        drawCentered(Lang.t("lbl_time") + ": " + formatTime(timeMillis), 360);

        String[] labels = { Lang.t("restart"), Lang.t("main_menu") };
        for (int i = 0; i < ITEM_COUNT; i++) {
            game.fontMedium.setColor(i == selected ? new Color(0.85f, 0.7f, 0.15f, 1f) : Color.WHITE);
            layout.setText(game.fontMedium, labels[i]);
            game.fontMedium.draw(game.batch, layout, (800 - layout.width) / 2f, TOP_Y - i * SPACING);
        }
        game.batch.end();

        if (activate) select();
    }

    private void drawCentered(String text, float y) {
        layout.setText(game.fontMedium, text);
        game.fontMedium.draw(game.batch, layout, (800 - layout.width) / 2f, y);
    }

    private int rowAt(float worldY) {
        for (int i = 0; i < ITEM_COUNT; i++) {
            float center = TOP_Y - i * SPACING - 8f;
            if (worldY >= center - ROW_H / 2f && worldY <= center + ROW_H / 2f) return i;
        }
        return -1;
    }

    private void select() {
        if (selected == 0) game.setScreen(new GameScreen(game, saveSlot));
        else game.setScreen(new MainMenuScreen(game));
    }

    private String formatTime(long millis) {
        long total = millis / 1000;
        return String.format("%02d:%02d", total / 60, total % 60);
    }
}