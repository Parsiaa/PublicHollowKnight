package HollowKnight.hollowknight.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.HashSet;
import java.util.Set;

import HollowKnight.hollowknight.HollowKnightGame;
import HollowKnight.hollowknight.controller.AchievementManager;
import HollowKnight.hollowknight.controller.CheatController;
import HollowKnight.hollowknight.controller.CombatSystem;
import HollowKnight.hollowknight.controller.EffectManager;
import HollowKnight.hollowknight.controller.EnemyManager;
import HollowKnight.hollowknight.controller.FalseKnightController;
import HollowKnight.hollowknight.controller.KnightController;
import HollowKnight.hollowknight.controller.WallManager;
import HollowKnight.hollowknight.controller.ZoteController;
import HollowKnight.hollowknight.model.Achievement;
import HollowKnight.hollowknight.model.BreakableWall;
import HollowKnight.hollowknight.model.Enemy;
import HollowKnight.hollowknight.model.Entity;
import HollowKnight.hollowknight.model.FalseKnight;
import HollowKnight.hollowknight.model.GameData;
import HollowKnight.hollowknight.model.Knight;
import HollowKnight.hollowknight.model.Level;
import HollowKnight.hollowknight.model.Zote;
import HollowKnight.hollowknight.utils.EffectAnimationType;
import HollowKnight.hollowknight.utils.FalseKnightAssetManager;
import HollowKnight.hollowknight.view.AchievementPopup;
import HollowKnight.hollowknight.view.DialogueBox;
import HollowKnight.hollowknight.view.EnemyRenderer;
import HollowKnight.hollowknight.view.FalseKnightRenderer;
import HollowKnight.hollowknight.view.GameCamera;
import HollowKnight.hollowknight.view.HUD;
import HollowKnight.hollowknight.view.InventoryMenu;
import HollowKnight.hollowknight.view.KnightRenderer;
import HollowKnight.hollowknight.view.WallRenderer;
import HollowKnight.hollowknight.view.ZoteRenderer;

public class GameScreen extends ScreenAdapter {
    private static final float VIEW_W = 800f, VIEW_H = 600f;
    private static final String BGM = "audio/greenpath.ogg";

    private static final float BOSS_W = 260f;
    private static final float BOSS_H = 300f;
    private static final int BOSS_HP = 40;
    private static final long SPEEDRUN_LIMIT_MS = 300000L;
    private static final float VICTORY_DELAY = 2.0f;
    private static final float MAX_FRAME_TIME = 1f / 30f;

    private static final String SFX_SLASH = "audio/sfx/nail_swing.ogg";
    private static final String SFX_DASH = "audio/sfx/dash.wav";
    private static final String SFX_SPIRIT = "audio/sfx/vengeful_spirit.ogg";
    private static final String SFX_WRAITHS = "audio/sfx/howling_wraiths.ogg";
    private static final String SFX_JUMP = "audio/sfx/jump.ogg";
    private static final String SFX_LAND = "audio/sfx/land.ogg";
    private static final String SFX_HURT = "audio/sfx/player_hurt.ogg";
    private static final String SFX_DEATH = "audio/sfx/player_death.ogg";
    private static final String SFX_FOCUS = "audio/sfx/focus_heal.ogg";
    private static final String SFX_SOUL = "audio/sfx/soul_gain.ogg";
    private static final String SFX_ENEMY_DEATH = "audio/sfx/enemy_death.ogg";
    private static final String SFX_WALL_BREAK = "audio/sfx/wall_break.ogg";

    private final HollowKnightGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final GameCamera gameCamera;

    private final Level level;
    private final Knight player;
    private final KnightController controller;
    private final EnemyManager enemyManager;
    private final EffectManager effectManager;
    private final CombatSystem combat;
    private final CheatController cheats;
    private final WallManager wallManager;

    private final Zote zote;
    private final ZoteController zoteController;

    private FalseKnight boss;
    private FalseKnightController bossController;
    private FalseKnightRenderer falseKnightRenderer;
    private FalseKnightAssetManager falseKnightAssets;

    private final KnightRenderer knightRenderer;
    private final EnemyRenderer enemyRenderer;
    private final ZoteRenderer zoteRenderer;
    private final WallRenderer wallRenderer;
    private final HUD hud;
    private final InventoryMenu inventoryMenu;
    private final DialogueBox dialogueBox;
    private final AchievementPopup achievementPopup;

    private final Set<Class<?>> requiredTypes = new HashSet<>();
    private final Set<Class<?>> killedTypes = new HashSet<>();
    private final Set<Enemy> countedDead = new HashSet<>();
    private int kills = 0;
    private int deaths = 0;
    private Entity.State prevPlayerState = Entity.State.IDLE;
    private int prevMasks;
    private boolean bossFightTookDamage = false;
    private boolean bossDefeated = false;
    private final boolean bossAlreadyCleared;
    private float victoryTimer = 0f;

    private Entity.State sfxPrevState = Entity.State.IDLE;
    private int sfxPrevMasks;
    private int sfxPrevSoul;
    private int prevBrokenWalls = 0;

    private final int saveSlot;
    private long playTimeMillis = 0L;
    private boolean disposed = false;

    public GameScreen(HollowKnightGame game) {
        this(game, -1);
    }

    public GameScreen(HollowKnightGame game, int saveSlot) {
        this.game = game;
        this.batch = game.batch;
        this.saveSlot = saveSlot;

        GameData loaded = (saveSlot >= 0) ? game.saveManager.load(saveSlot) : null;
        this.bossAlreadyCleared = (loaded != null && loaded.bossDefeated);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEW_W, VIEW_H);
        camera.zoom = 4f;
        viewport = new FitViewport(VIEW_W, VIEW_H, camera);
        gameCamera = new GameCamera(camera, VIEW_W, VIEW_H);

        level = new Level("GreenpathMap.tmx");
        player = new Knight(level.getPlayerSpawn().x, level.getPlayerSpawn().y, 96f, 288f);
        camera.position.set(player.getBoundingBox().x, player.getBoundingBox().y, 0);
        camera.update();

        controller = new KnightController(player, level);
        enemyRenderer = new EnemyRenderer(game.enemyAssets);
        enemyManager = new EnemyManager(enemyRenderer, level);
        effectManager = new EffectManager(game.effectAssets, level);
        combat = new CombatSystem(player, enemyManager, gameCamera, level);
        cheats = new CheatController(player, enemyManager);
        wallManager = new WallManager(level.getBreakableWalls(), player);

        zote = new Zote(level.getZoteSpawn().x, level.getZoteSpawn().y, 70f, 200f);
        zoteRenderer = new ZoteRenderer(game.zoteAssets);
        dialogueBox = new DialogueBox(batch, game.fontMedium);
        zoteController = new ZoteController(zote, player, game.audio, dialogueBox, level);

        if (level.getArena() != null && !bossAlreadyCleared) {
            boss = new FalseKnight(level.getFalseKnightSpawn().x, level.getFalseKnightSpawn().y,
                    BOSS_W, BOSS_H, BOSS_HP);
            falseKnightAssets = new FalseKnightAssetManager();
            falseKnightRenderer = new FalseKnightRenderer(falseKnightAssets);
            bossController = new FalseKnightController(boss, player, level, gameCamera,
                    game.audio, level.getArena());
            combat.setBoss(boss);
        }

        knightRenderer = new KnightRenderer(game.assetManager);
        wallRenderer = new WallRenderer();
        hud = new HUD(player, VIEW_W, VIEW_H,
                game.assetManager.getEmptyHealth(), game.assetManager.getFilledHealthShine(),
                game.assetManager.getBreakHealth(), game.assetManager.getHealthRefill(),
                game.assetManager.getSoulVesselFrame(), game.assetManager.getSoulLiquid());
        inventoryMenu = new InventoryMenu(player, VIEW_W, VIEW_H);

        achievementPopup = new AchievementPopup(batch, game.fontMedium);
        AchievementManager.get().addListener(achievementPopup);

        for (Enemy e : enemyManager.getEnemies()) requiredTypes.add(e.getClass());
        prevMasks = player.masks;
        prevPlayerState = player.getCurrentState();
        sfxPrevState = player.getCurrentState();
        sfxPrevMasks = player.masks;
        sfxPrevSoul = player.soul;

        if (loaded != null) {
            applyState(loaded);
            hud.syncSoul();
        }
    }

    public int getSaveSlot() { return saveSlot; }

    @Override
    public void show() {
        game.audio.playBgm(BGM);
    }

    @Override
    public void render(float dt) {
        dt = Math.min(dt, MAX_FRAME_TIME);
        update(dt);
        if (disposed) return;
        renderWorld();
    }

    private void update(float dt) {
        playTimeMillis += (long) (dt * 1000);
        achievementPopup.update(dt);

        if (inventoryMenu.isOpen()) {
            inventoryMenu.update();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Keys.I) && player.getCurrentState() != Entity.State.DEAD) {
            inventoryMenu.toggle();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            game.setScreen(new PauseScreen(game, this));
            return;
        }

        zoteController.update(dt);

        if (!zoteController.isInDialogue()) {
            cheats.handle();
            if (cheats.isNoclip()) cheats.updateNoclip(dt);
            else controller.update(dt);
            consumeEffectRequests();
        }

        Entity.State s = player.getCurrentState();
        boolean attacking = s == Entity.State.ATTACKING || s == Entity.State.ATTACKING_ALT
                || s == Entity.State.UP_SLASH || s == Entity.State.DOWN_SLASH;
        Rectangle nail = attacking ? combat.buildNailHitbox(s, player.getBoundingBox()) : null;
        wallManager.update(dt, nail, attacking);

        boolean bossActive = bossController != null
                && bossController.isStarted() && !bossController.isDefeated();
        enemyManager.update(dt, player, bossActive);
        effectManager.update(dt, enemyManager.getEnemies(), player);
        combat.update();

        if (bossController != null) {
            bossController.update(dt, nail, attacking);
            if (!bossDefeated && bossController.isDefeated()) {
                bossDefeated = true;
                victoryTimer = VICTORY_DELAY;
                handleBossAchievements();
            }
        }

        trackStats();
        playEventSounds();

        hud.update(dt);
        gameCamera.follow(player.getBoundingBox(), level.getWidth(), level.getHeight(), dt);

        if (bossDefeated) {
            victoryTimer -= dt;
            if (victoryTimer <= 0f) {
                goToVictory();
                return;
            }
        }
    }

    private void trackStats() {
        for (Enemy e : enemyManager.getEnemies()) {
            if (e.isDead()) {
                if (countedDead.add(e)) {
                    kills++;
                    sfx(SFX_ENEMY_DEATH);
                }
                killedTypes.add(e.getClass());
            } else {
                countedDead.remove(e);
            }
        }
        if (!requiredTypes.isEmpty() && killedTypes.containsAll(requiredTypes)) {
            AchievementManager.get().unlock(Achievement.TRUE_HUNTER);
        }
        if (player.getCurrentState() == Entity.State.DEAD && prevPlayerState != Entity.State.DEAD) {
            deaths++;
        }
        prevPlayerState = player.getCurrentState();
        if (bossController != null && bossController.isStarted() && !bossController.isDefeated()
                && player.masks < prevMasks) {
            bossFightTookDamage = true;
        }
        prevMasks = player.masks;
    }

    private void playEventSounds() {
        Entity.State s = player.getCurrentState();
        if (s != sfxPrevState) {
            switch (s) {
                case JUMPING: case DOUBLE_JUMPING: case WALL_JUMPING: sfx(SFX_JUMP); break;
                case LANDING: sfx(SFX_LAND); break;
                case FOCUS_GET: sfx(SFX_FOCUS); break;
                case DEAD: sfx(SFX_DEATH); break;
                default: break;
            }
        }
        sfxPrevState = s;

        if (player.masks < sfxPrevMasks && s != Entity.State.DEAD) sfx(SFX_HURT);
        sfxPrevMasks = player.masks;

        if (player.soul > sfxPrevSoul) sfx(SFX_SOUL);
        sfxPrevSoul = player.soul;

        int broken = 0;
        for (BreakableWall w : level.getBreakableWalls()) if (w.isBroken()) broken++;
        if (broken > prevBrokenWalls) sfx(SFX_WALL_BREAK);
        prevBrokenWalls = broken;
    }

    private void sfx(String path) {
        try {
            game.audio.playSound(path);
        } catch (Exception e) {
        }
    }

    private void handleBossAchievements() {
        AchievementManager am = AchievementManager.get();
        am.unlock(Achievement.DEFEAT_FALSE_KNIGHT);
        am.unlock(Achievement.COMPLETION);
        if (playTimeMillis <= SPEEDRUN_LIMIT_MS) am.unlock(Achievement.SPEEDRUN);
        if (!bossFightTookDamage) am.unlock(Achievement.NO_DAMACE_RUN);
        gameCamera.unlock();
    }

    private void goToVictory() {
        VictoryScreen vs = new VictoryScreen(game, deaths, kills, playTimeMillis, saveSlot);
        dispose();
        game.setScreen(vs);
    }

    public void renderWorld() {
        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1f);
        level.renderBackground(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        enemyManager.render(batch);
        if (falseKnightRenderer != null && boss != null) falseKnightRenderer.render(batch, boss);
        knightRenderer.render(batch, player);
        zoteRenderer.render(batch, zote);
        effectManager.render(batch);
        batch.end();

        wallRenderer.render(camera, level.getBreakableWalls());

        level.renderForeground(camera);

        hud.render(batch, player.getStateTime());
        batch.begin();
        inventoryMenu.render(batch);
        batch.end();

        zoteController.renderUI();
        achievementPopup.render();
    }

    public GameData captureState() {
        GameData d = new GameData();
        d.masks = player.masks;
        d.soul = player.soul;
        d.spawnX = level.getLastSafePosition().x;
        d.spawnY = level.getLastSafePosition().y;
        d.bossDefeated = bossDefeated || bossAlreadyCleared;
        d.charmSoulCatcher = player.charmSoulCatcher;
        d.charmDashmaster = player.charmDashmaster;
        d.charmUnbreakableStrength = player.charmUnbreakableStrength;
        d.charmQuickSlash = player.charmQuickSlash;
        d.charmQuickFocus = player.charmQuickFocus;
        d.charmHeavyBlow = player.charmHeavyBlow;
        d.charmSharpShadow = player.charmSharpShadow;
        d.charmVoidHeart = player.charmVoidHeart;
        d.playTimeMillis = playTimeMillis;
        return d;
    }

    public void applyState(GameData d) {
        player.masks = d.masks;
        player.soul = d.soul;
        float sx = d.spawnX;
        float sy = d.spawnY;
        if (sx <= 0f && sy <= 0f) {
            sx = level.getPlayerSpawn().x;
            sy = level.getPlayerSpawn().y;
        }
        player.getBoundingBox().setPosition(sx, sy);
        level.updateSafePosition(sx, sy);
        player.charmSoulCatcher = d.charmSoulCatcher;
        player.charmDashmaster = d.charmDashmaster;
        player.charmUnbreakableStrength = d.charmUnbreakableStrength;
        player.charmQuickSlash = d.charmQuickSlash;
        player.charmQuickFocus = d.charmQuickFocus;
        player.charmHeavyBlow = d.charmHeavyBlow;
        player.charmSharpShadow = d.charmSharpShadow;
        player.charmVoidHeart = d.charmVoidHeart;
        player.recalculateCharmStats();
        playTimeMillis = d.playTimeMillis;
        prevMasks = player.masks;
    }

    private void consumeEffectRequests() {
        if (player.requestSlashEffect) {
            player.requestSlashEffect = false;
            Entity.State s = player.getCurrentState();
            EffectAnimationType type;
            switch (s) {
                case UP_SLASH: type = EffectAnimationType.NAIL_UP_SLASH; break;
                case DOWN_SLASH: type = EffectAnimationType.NAIL_DOWN_SLASH; break;
                case ATTACKING_ALT: type = EffectAnimationType.NAIL_SLASH_ALT; break;
                default: type = EffectAnimationType.NAIL_SLASH; break;
            }
            effectManager.spawnSlash(player, type);
            sfx(SFX_SLASH);
        }
        if (player.requestDashEffect) {
            player.requestDashEffect = false;
            effectManager.spawnDash(player);
            sfx(SFX_DASH);
        }
        if (player.requestVengefulSpirit) {
            player.requestVengefulSpirit = false;
            effectManager.spawnVengefulSpirit(player);
            sfx(SFX_SPIRIT);
        }
        if (player.requestHowlingWraiths) {
            player.requestHowlingWraiths = false;
            effectManager.spawnHowlingWraiths(player);
            sfx(SFX_WRAITHS);
        }
    }

    @Override
    public void resize(int w, int h) {
        viewport.update(w, h);
    }

    @Override
    public void dispose() {
        if (disposed) return;
        disposed = true;
        AchievementManager.get().removeListener(achievementPopup);
        achievementPopup.dispose();
        if (falseKnightAssets != null) falseKnightAssets.dispose();
        level.dispose();
        enemyRenderer.dispose();
        inventoryMenu.dispose();
        dialogueBox.dispose();
        wallRenderer.dispose();
    }
}