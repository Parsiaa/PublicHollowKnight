package HollowKnight.hollowknight.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

import HollowKnight.hollowknight.HollowKnightGame;
import HollowKnight.hollowknight.utils.Lang;

public class PauseScreen extends ScreenAdapter {
    private static final float ROW_H = 50f, TOP_Y = 380f, SPACING = 60f;

    private final HollowKnightGame game;
    private final GameScreen gameScreen;
    private final OrthographicCamera cam = new OrthographicCamera();
    private final ShapeRenderer shape = new ShapeRenderer();
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector3 tmp = new Vector3();

    private final String[] items = {"continue", "settings", "cheat_codes", "save_quit"};
    private final String[] cheatLines = {
        "Hold Left Ctrl +", "T Teleport to boss   N Noclip",
        "H Heal   R Refill Soul   G God Mode   K Kill All"
    };
    private int selected = 0;
    private int prevMx, prevMy;
    private boolean showCheats = false;
    private boolean disposed = false;

    public PauseScreen(HollowKnightGame game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
        cam.setToOrtho(false, 800, 600);
    }

    @Override
    public void render(float dt) {
        handleInput();
        if (game.getScreen() != this) return;

        gameScreen.renderWorld();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shape.setProjectionMatrix(cam.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.65f);
        shape.rect(0, 0, 800, 600);
        shape.end();

        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();

        game.fontLarge.setColor(0.85f, 0.7f, 0.15f, 1f);
        layout.setText(game.fontLarge, Lang.t("paused"));
        game.fontLarge.draw(game.batch, layout, (800 - layout.width) / 2f, 480);

        for (int i = 0; i < items.length; i++) {
            game.fontMedium.setColor(i == selected ? new Color(0.85f, 0.7f, 0.15f, 1f) : Color.WHITE);
            layout.setText(game.fontMedium, Lang.t(items[i]));
            game.fontMedium.draw(game.batch, layout, (800 - layout.width) / 2f, TOP_Y - i * SPACING);
        }

        if (showCheats) {
            game.fontMedium.setColor(0.7f, 0.7f, 0.7f, 1f);
            for (int i = 0; i < cheatLines.length; i++)
                game.fontMedium.draw(game.batch, cheatLines[i], 120, 150 - i * 26f);
        }
        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) { resumeGame(); return; }
        if (Gdx.input.isKeyJustPressed(Keys.UP)) selected = (selected + items.length - 1) % items.length;
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) selected = (selected + 1) % items.length;

        int mx = Gdx.input.getX(), my = Gdx.input.getY();
        boolean moved = (mx != prevMx || my != prevMy);
        prevMx = mx; prevMy = my;
        tmp.set(mx, my, 0); cam.unproject(tmp);
        int hov = rowAt(tmp.y);
        if (moved && hov != -1) selected = hov;

        if (hov != -1 && Gdx.input.justTouched()) { selected = hov; activate(selected); }
        else if (Gdx.input.isKeyJustPressed(Keys.ENTER)) activate(selected);
    }

    private int rowAt(float worldY) {
        for (int i = 0; i < items.length; i++) {
            float c = TOP_Y - i * SPACING - 8f;
            if (worldY >= c - ROW_H / 2f && worldY <= c + ROW_H / 2f) return i;
        }
        return -1;
    }

    private void activate(int row) {
        switch (row) {
            case 0: resumeGame(); break;
            case 1: game.setScreen(new SettingsScreen(game, this)); break;
            case 2: showCheats = !showCheats; break;
            case 3: quitToMenu(); break;
        }
    }

    private void resumeGame() {
        game.setScreen(gameScreen);
    }

    private void quitToMenu() {
        int slot = gameScreen.getSaveSlot();
        if (slot < 0) slot = 0;
        game.saveManager.save(slot, gameScreen.captureState());
        gameScreen.dispose();
        game.setScreen(new MainMenuScreen(game));
    }

    @Override
    public void dispose() {
        if (disposed) return;
        disposed = true;
        shape.dispose();
    }
}