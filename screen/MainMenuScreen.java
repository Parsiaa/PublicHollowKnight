package HollowKnight.hollowknight.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import HollowKnight.hollowknight.HollowKnightGame;
import HollowKnight.hollowknight.utils.Lang;

public class MainMenuScreen extends ScreenAdapter {
    private static final float ROW_H = 50f, TOP_Y = 420f, SPACING = 60f;
    private static final String BGM = "audio/menu.ogg";
    private static final String SFX_MOVE = "audio/sfx/ui_change_selection.wav";
    private static final String SFX_CONFIRM = "audio/sfx/ui_button_confirm.wav";

    private static final Rectangle BG_BUTTON = new Rectangle(520f, 40f, 240f, 40f);

    private final HollowKnightGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector3 tmp = new Vector3();
    private final String[] items = {"start_game", "settings", "guide", "achievements", "quit"};
    private int selected = 0;
    private int prevMx, prevMy;

    public MainMenuScreen(HollowKnightGame game) {
        this.game = game;
        camera.setToOrtho(false, 800, 600);
    }

    @Override
    public void show() {
        game.audio.playBgm(BGM);
    }

    @Override
    public void render(float dt) {
        int prevSelected = selected;
        if (Gdx.input.isKeyJustPressed(Keys.UP)) selected = (selected + items.length - 1) % items.length;
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) selected = (selected + 1) % items.length;

        int mx = Gdx.input.getX(), my = Gdx.input.getY();
        boolean moved = (mx != prevMx || my != prevMy);
        prevMx = mx; prevMy = my;
        tmp.set(mx, my, 0);
        camera.unproject(tmp);
        int hovered = rowAt(tmp.y);
        if (moved && hovered != -1) selected = hovered;

        if (selected != prevSelected) game.audio.playSound(SFX_MOVE);

        boolean hasBg = game.menuBackground.hasBackgrounds();
        boolean overBgButton = BG_BUTTON.contains(tmp.x, tmp.y);
        if ((Gdx.input.isKeyJustPressed(Keys.B) || (overBgButton && Gdx.input.justTouched())) && hasBg) {
            game.menuBackground.cycle();
        }

        boolean activate = Gdx.input.isKeyJustPressed(Keys.ENTER)
                        || Gdx.input.isKeyJustPressed(Keys.Z)
                        || (hovered != -1 && Gdx.input.justTouched());

        ScreenUtils.clear(0.04f, 0.04f, 0.06f, 1f);
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        game.menuBackground.drawBackground(game.batch);
        game.menuBackground.drawTitle(game.batch);

        for (int i = 0; i < items.length; i++) {
            game.fontLarge.setColor(i == selected ? new Color(0.85f, 0.7f, 0.15f, 1f) : Color.WHITE);
            layout.setText(game.fontLarge, Lang.t(items[i]));
            game.fontLarge.draw(game.batch, layout, (800 - layout.width) / 2f, TOP_Y - i * SPACING);
        }

        if (hasBg) {
            game.fontMedium.setColor(overBgButton ? new Color(0.85f, 0.7f, 0.15f, 1f)
                                                  : new Color(0.7f, 0.7f, 0.7f, 1f));
            layout.setText(game.fontMedium, "[ B ]  Background");
            game.fontMedium.draw(game.batch, layout,
                    BG_BUTTON.x + (BG_BUTTON.width - layout.width) / 2f,
                    BG_BUTTON.y + (BG_BUTTON.height + layout.height) / 2f);
        }

        game.batch.end();

        if (activate) { game.audio.playSound(SFX_CONFIRM); select(); }
    }

    private int rowAt(float worldY) {
        for (int i = 0; i < items.length; i++) {
            float center = TOP_Y - i * SPACING - 8f;
            if (worldY >= center - ROW_H / 2f && worldY <= center + ROW_H / 2f) return i;
        }
        return -1;
    }

    private void select() {
        switch (selected) {
            case 0: game.setScreen(new SaveSlotScreen(game)); break;
            case 1: game.setScreen(new SettingsScreen(game, this)); break;
            case 2: game.setScreen(new GuideScreen(game)); break;
            case 3: game.setScreen(new AchievementsScreen(game)); break;
            case 4: Gdx.app.exit(); break;
        }
    }
}
