package HollowKnight.hollowknight.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

import HollowKnight.hollowknight.model.Settings;

/**
 * Shared menu backdrop, loaded once and reused by every menu screen so the chosen
 * background stays put as you navigate between them.
 *
 * Drop your images into assets/background/ as bg1.png, bg2.png, bg3.png (cycled with the
 * B key / on-screen button on the main menu) and title.png (the always-on VOIDHEART logo).
 * Any missing file is skipped, so the menus still work before the art is added.
 */
public class MenuBackground {

    private static final String[] PATHS = {
        "background/bg1.png",
        "background/bg2.png",
        "background/bg3.png"
    };
    private static final String TITLE_PATH = "background/title.png";


    private static final float TITLE_MAX_W = 560f, TITLE_MAX_H = 150f, TITLE_CY = 512f;

    private final List<Texture> backgrounds = new ArrayList<>();
    private Texture title;

    public MenuBackground() {
        for (String p : PATHS) {
            if (Gdx.files.internal(p).exists()) backgrounds.add(new Texture(Gdx.files.internal(p)));
        }
        if (Gdx.files.internal(TITLE_PATH).exists()) title = new Texture(Gdx.files.internal(TITLE_PATH));
    }

    public boolean hasBackgrounds() { return !backgrounds.isEmpty(); }

    /** Currently selected background index, clamped into range. */
    public int current() {
        if (backgrounds.isEmpty()) return 0;
        int n = backgrounds.size();
        return ((Settings.get().menuBackground % n) + n) % n;
    }

    /** Advance to the next background and persist the choice. */
    public void cycle() {
        if (backgrounds.isEmpty()) return;
        Settings.get().menuBackground = (current() + 1) % backgrounds.size();
        Settings.get().save();
    }

    /** Draws the active background stretched to the 800x600 virtual screen. Call inside batch.begin/end. */
    public void drawBackground(SpriteBatch batch) {
        if (backgrounds.isEmpty()) return;
        batch.setColor(Color.WHITE);
        batch.draw(backgrounds.get(current()), 0, 0, 800, 600);
    }

    /** Draws the VOIDHEART title in its band above the menu. Call inside batch.begin/end. */
    public void drawTitle(SpriteBatch batch) {
        if (title == null) return;
        batch.setColor(Color.WHITE);
        float scale = Math.min(TITLE_MAX_W / title.getWidth(), TITLE_MAX_H / title.getHeight());
        float w = title.getWidth() * scale, h = title.getHeight() * scale;
        batch.draw(title, (800 - w) / 2f, TITLE_CY - h / 2f, w, h);
    }

    public void dispose() {
        for (Texture t : backgrounds) t.dispose();
        backgrounds.clear();
        if (title != null) { title.dispose(); title = null; }
    }
}
