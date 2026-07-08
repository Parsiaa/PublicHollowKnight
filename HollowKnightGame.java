package HollowKnight.hollowknight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import HollowKnight.hollowknight.controller.SaveManager;
import HollowKnight.hollowknight.model.Settings;
import HollowKnight.hollowknight.screen.MainMenuScreen;
import HollowKnight.hollowknight.utils.AudioManager;
import HollowKnight.hollowknight.utils.EffectAssetManager;
import HollowKnight.hollowknight.utils.EnemyAssetManager;
import HollowKnight.hollowknight.utils.FontFactory;
import HollowKnight.hollowknight.utils.GameAssetManager;
import HollowKnight.hollowknight.utils.ZoteAssetManager;
import HollowKnight.hollowknight.view.MenuBackground;

public class HollowKnightGame extends Game {

    public static final String FONT_PATH = "font/TrajanPro-Regular.ttf";

    public SpriteBatch batch;
    public GameAssetManager assetManager;
    public EnemyAssetManager enemyAssets;
    public EffectAssetManager effectAssets;
    public ZoteAssetManager zoteAssets;
    public AudioManager audio;
    public SaveManager saveManager;

    public BitmapFont fontLarge;
    public BitmapFont fontMedium;

    public MenuBackground menuBackground;

    private ShapeRenderer brightnessOverlay;
    private OrthographicCamera overlayCamera;

    @Override
    public void create() {
        batch = new SpriteBatch();
        brightnessOverlay = new ShapeRenderer();
        overlayCamera = new OrthographicCamera();

        assetManager = new GameAssetManager();
        assetManager.loadAssets();
        enemyAssets = new EnemyAssetManager();
        enemyAssets.load();
        effectAssets = new EffectAssetManager();
        effectAssets.load();
        zoteAssets = new ZoteAssetManager();
        zoteAssets.load();

        audio = new AudioManager();
        saveManager = new SaveManager();

        fontLarge = FontFactory.generate(FONT_PATH, 36);
        fontMedium = FontFactory.generate(FONT_PATH, 20);

        menuBackground = new MenuBackground();

        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
        drawBrightnessOverlay();
    }

    private void drawBrightnessOverlay() {
        float b = Settings.get().brightness;
        if (b == 1f) return;
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        overlayCamera.setToOrtho(false, w, h);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        brightnessOverlay.setProjectionMatrix(overlayCamera.combined);
        brightnessOverlay.begin(ShapeRenderer.ShapeType.Filled);
        if (b < 1f) brightnessOverlay.setColor(0f, 0f, 0f, 1f - b);
        else brightnessOverlay.setColor(1f, 1f, 1f, (b - 1f) * 0.8f);
        brightnessOverlay.rect(0f, 0f, w, h);
        brightnessOverlay.end();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        if (brightnessOverlay != null) brightnessOverlay.dispose();
        batch.dispose();
        assetManager.dispose();
        enemyAssets.dispose();
        effectAssets.dispose();
        zoteAssets.dispose();
        audio.dispose();
        fontLarge.dispose();
        fontMedium.dispose();
        if (menuBackground != null) menuBackground.dispose();
    }
}