package HollowKnight.hollowknight.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;

import HollowKnight.hollowknight.HollowKnightGame;
import HollowKnight.hollowknight.controller.AchievementManager;
import HollowKnight.hollowknight.model.Achievement;

public class AchievementsScreen extends ScreenAdapter {
    private final HollowKnightGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final AchievementManager achievements = AchievementManager.get();

    public AchievementsScreen(HollowKnightGame game) {
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

        game.fontLarge.setColor(0.85f, 0.7f, 0.15f, 1f);
        game.fontLarge.draw(game.batch, "ACHIEVEMENTS", 230, 560);

        float y = 470f;
        for (Achievement a : Achievement.values()) {
            boolean got = achievements.isUnlocked(a);
            game.fontMedium.setColor(got ? new Color(0.85f, 0.7f, 0.15f, 1f)
                                         : new Color(0.4f, 0.4f, 0.4f, 1f));
            String title = got ? a.title : a.title + "  [LOCKED]";
            game.fontMedium.draw(game.batch, title, 120, y);

            if (got) {
                game.fontMedium.setColor(0.7f, 0.7f, 0.7f, 1f);
                game.fontMedium.draw(game.batch, a.description, 140, y - 26f);
            }
            y -= 70f;
        }

        game.fontMedium.setColor(0.5f, 0.5f, 0.5f, 1f);
        game.fontMedium.draw(game.batch, "Esc - back", 120, 60);
        game.batch.end();
    }
}