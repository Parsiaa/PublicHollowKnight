package HollowKnight.hollowknight.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import HollowKnight.hollowknight.HollowKnightGame;
import HollowKnight.hollowknight.model.Settings;

public class MainMenuScreen extends ScreenAdapter {
    private static final float ROW_H = 50f, TOP_Y = 420f, SPACING = 60f;
    private static final String BGM = "audio/menu.ogg";

    /**
     * Candidate background images. Drop your three files into assets/background/ using
     * these names; any that are missing are skipped, so the menu still works without them.
     */
    private static final String[] BACKGROUND_PATHS = {
        "background/bg1.png",
        "background/bg2.png",
        "background/bg3.png"
    };

    /** The HOLLOW KNIGHT: VOIDHEART title, always drawn on top of whichever background is active. */
    private static final String TITLE_PATH = "background/title.png";
    // Title is fit (aspect preserved) into this band above the menu items, centred on TITLE_CY.
    private static final float TITLE_MAX_W = 560f, TITLE_MAX_H = 150f, TITLE_CY = 512f;

    // On-screen "change background" button (virtual 800x600 coords, y-up).
    private static final Rectangle BG_BUTTON = new Rectangle(520f, 40f, 240f, 40f);

    private final HollowKnightGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector3 tmp = new Vector3();
    private final String[] items = {"Start Game", "Settings", "Guide", "Achievements", "Quit"};
    private int selected = 0;
    private int prevMx, prevMy;

    private final List<Texture> backgrounds = new ArrayList<>();
    private int background = 0;
    private Texture title;

    public MainMenuScreen(HollowKnightGame game) {
        this.game = game;
        camera.setToOrtho(false, 800, 600);
    }

    @Override
    public void show() {
        game.audio.playBgm(BGM);
        loadBackgrounds();
        background = backgrounds.isEmpty() ? 0
                : ((Settings.get().menuBackground % backgrounds.size()) + backgrounds.size()) % backgrounds.size();
    }

    private void loadBackgrounds() {
        disposeBackgrounds();
        for (String path : BACKGROUND_PATHS) {
            if (Gdx.files.internal(path).exists()) {
                backgrounds.add(new Texture(Gdx.files.internal(path)));
            }
        }
        if (Gdx.files.internal(TITLE_PATH).exists()) {
            title = new Texture(Gdx.files.internal(TITLE_PATH));
        }
    }

    @Override
    public void render(float dt) {
        if (Gdx.input.isKeyJustPressed(Keys.UP)) selected = (selected + items.length - 1) % items.length;
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) selected = (selected + 1) % items.length;

        int mx = Gdx.input.getX(), my = Gdx.input.getY();
        boolean moved = (mx != prevMx || my != prevMy);
        prevMx = mx; prevMy = my;
        tmp.set(mx, my, 0);
        camera.unproject(tmp);
        int hovered = rowAt(tmp.y);
        if (moved && hovered != -1) selected = hovered;

        boolean overBgButton = BG_BUTTON.contains(tmp.x, tmp.y);
        boolean cycleBackground = Gdx.input.isKeyJustPressed(Keys.B)
                        || (overBgButton && Gdx.input.justTouched());
        if (cycleBackground) cycleBackground();

        boolean activate = Gdx.input.isKeyJustPressed(Keys.ENTER)
                        || Gdx.input.isKeyJustPressed(Keys.Z)
                        || (hovered != -1 && Gdx.input.justTouched());

        ScreenUtils.clear(0.04f, 0.04f, 0.06f, 1f);
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        if (!backgrounds.isEmpty()) {
            game.batch.setColor(Color.WHITE);
            game.batch.draw(backgrounds.get(background), 0, 0, 800, 600);
        }

        if (title != null) {
            game.batch.setColor(Color.WHITE);
            float scale = Math.min(TITLE_MAX_W / title.getWidth(), TITLE_MAX_H / title.getHeight());
            float w = title.getWidth() * scale, h = title.getHeight() * scale;
            game.batch.draw(title, (800 - w) / 2f, TITLE_CY - h / 2f, w, h);
        }

        for (int i = 0; i < items.length; i++) {
            game.fontLarge.setColor(i == selected ? new Color(0.85f, 0.7f, 0.15f, 1f) : Color.WHITE);
            layout.setText(game.fontLarge, items[i]);
            game.fontLarge.draw(game.batch, layout, (800 - layout.width) / 2f, TOP_Y - i * SPACING);
        }

        if (!backgrounds.isEmpty()) {
            game.fontMedium.setColor(overBgButton ? new Color(0.85f, 0.7f, 0.15f, 1f)
                                                  : new Color(0.7f, 0.7f, 0.7f, 1f));
            layout.setText(game.fontMedium, "[ B ]  Background");
            game.fontMedium.draw(game.batch, layout,
                    BG_BUTTON.x + (BG_BUTTON.width - layout.width) / 2f,
                    BG_BUTTON.y + (BG_BUTTON.height + layout.height) / 2f);
        }

        game.batch.end();

        if (activate) select();
    }

    private void cycleBackground() {
        if (backgrounds.isEmpty()) return;
        background = (background + 1) % backgrounds.size();
        Settings.get().menuBackground = background;
        Settings.get().save();
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

    private void disposeBackgrounds() {
        for (Texture t : backgrounds) t.dispose();
        backgrounds.clear();
        if (title != null) { title.dispose(); title = null; }
    }

    @Override
    public void hide() {
        disposeBackgrounds();
    }

    @Override
    public void dispose() {
        disposeBackgrounds();
    }
}
