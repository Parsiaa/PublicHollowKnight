package HollowKnight.hollowknight.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

import HollowKnight.hollowknight.model.Settings;


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

    public int current() {
        if (backgrounds.isEmpty()) return 0;
        int n = backgrounds.size();
        return ((Settings.get().menuBackground % n) + n) % n;
    }

    public void cycle() {
        if (backgrounds.isEmpty()) return;
        Settings.get().menuBackground = (current() + 1) % backgrounds.size();
        Settings.get().save();
    }

    public void drawBackground(SpriteBatch batch) {
        if (backgrounds.isEmpty()) return;
        batch.setColor(Color.WHITE);
        batch.draw(backgrounds.get(current()), 0, 0, 800, 600);
    }

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
