package HollowKnight.hollowknight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import HollowKnight.hollowknight.controller.SaveManager;
import HollowKnight.hollowknight.screen.MainMenuScreen;
import HollowKnight.hollowknight.utils.AudioManager;
import HollowKnight.hollowknight.utils.EffectAssetManager;
import HollowKnight.hollowknight.utils.EnemyAssetManager;
import HollowKnight.hollowknight.utils.FontFactory;
import HollowKnight.hollowknight.utils.GameAssetManager;
import HollowKnight.hollowknight.utils.ZoteAssetManager;

public class HollowKnightGame extends Game {

    private static final String FONT_PATH = "font/TrajanPro-Regular.ttf";

    public SpriteBatch batch;
    public GameAssetManager assetManager;
    public EnemyAssetManager enemyAssets;
    public EffectAssetManager effectAssets;
    public ZoteAssetManager zoteAssets;
    public AudioManager audio;
    public SaveManager saveManager;

    public BitmapFont fontLarge;
    public BitmapFont fontMedium;


    @Override
    public void create() {
        batch = new SpriteBatch();

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

        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        batch.dispose();
        assetManager.dispose();
        enemyAssets.dispose();
        effectAssets.dispose();
        zoteAssets.dispose();
        audio.dispose();
        fontLarge.dispose();
        fontMedium.dispose();
    }
}