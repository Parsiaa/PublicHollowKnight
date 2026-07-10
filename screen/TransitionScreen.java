package HollowKnight.hollowknight.screen;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;

import HollowKnight.hollowknight.HollowKnightGame;
import HollowKnight.hollowknight.model.GameData;

public class TransitionScreen extends ScreenAdapter {
    private static final float DURATION = 2.0f;

    private final HollowKnightGame game;
    private final int saveSlot;
    private final String targetMap;
    private final GameData carry;
    private final OrthographicCamera camera = new OrthographicCamera();
    private float timer = DURATION;
    private boolean done = false;

    public TransitionScreen(HollowKnightGame game, int saveSlot, String targetMap, GameData carry) {
        this.game = game;
        this.saveSlot = saveSlot;
        this.targetMap = targetMap;
        this.carry = carry;
        camera.setToOrtho(false, 800, 600);
    }

    @Override
    public void show() {
        game.audio.stopBgm();
    }

    @Override
    public void render(float dt) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        if (done) return;
        timer -= dt;
        if (timer <= 0f) {
            done = true;
            game.setScreen(new GameScreen(game, saveSlot, targetMap, carry));
        }
    }
}
