package HollowKnight.hollowknight.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.EnumMap;
import java.util.Map;

public class FalseKnightAssetManager {

    private static final String DIR = "animation/False_Knight/";
    private static final int FRAME_W = 1095;
    private static final int FRAME_H = 636;

    private final Array<Texture> textures = new Array<>();
    private final Map<FalseKnightAnimationType, Animation<TextureRegion>> anims =
            new EnumMap<>(FalseKnightAnimationType.class);

    public FalseKnightAssetManager() {
        Animation<TextureRegion> idle = strip("Idle.png", 0.10f, Animation.PlayMode.LOOP);
        anims.put(FalseKnightAnimationType.IDLE, idle);
        anims.put(FalseKnightAnimationType.RUN,
                or(tryStrip("Run.png", 0.07f, Animation.PlayMode.LOOP), idle));

        anims.put(FalseKnightAnimationType.STUN_AIR,
                or(tryStrip("DeathFall.png", 0.06f, Animation.PlayMode.NORMAL), idle));
        anims.put(FalseKnightAnimationType.STUN_LAND,
                or(tryStrip("Body.png", 0.09f, Animation.PlayMode.NORMAL), idle));
        anims.put(FalseKnightAnimationType.STUN_DAZED,
                or(tryStrip("DeathLand.png", 0.06f, Animation.PlayMode.NORMAL), idle));
        anims.put(FalseKnightAnimationType.STUN_HIT,
                or(tryStrip("DeathHit.png", 0.06f, Animation.PlayMode.NORMAL),
                        anims.get(FalseKnightAnimationType.STUN_DAZED)));
        anims.put(FalseKnightAnimationType.STUN_RECOVER,
                or(tryStrip("Stun Recover.png", 0.085f, Animation.PlayMode.NORMAL), idle));

        anims.put(FalseKnightAnimationType.SLAM,
                or(tryCombined(0.06f, Animation.PlayMode.NORMAL,
                        "Attack Antic.png", "Attack.png", "Attack Recover.png"), idle));
        anims.put(FalseKnightAnimationType.LEAP,
                or(tryCombined(0.07f, Animation.PlayMode.NORMAL,
                        "Jump Antic.png", "Jump.png"), idle));
        anims.put(FalseKnightAnimationType.POWER_SLAM,
                or(tryCombined(0.07f, Animation.PlayMode.NORMAL,
                        "Jump Antic.png", "Jump.png", "Land.png"), idle));
        anims.put(FalseKnightAnimationType.DEATH,
                or(tryCombined(0.09f, Animation.PlayMode.NORMAL,
                        "DeathFall.png", "DeathLand.png"), idle));
    }

    private static Animation<TextureRegion> or(Animation<TextureRegion> primary,
                                               Animation<TextureRegion> fallback) {
        return primary != null ? primary : fallback;
    }

    private Animation<TextureRegion> tryStrip(String file, float frameDuration, Animation.PlayMode mode) {
        try {
            return strip(file, frameDuration, mode);
        } catch (Exception e) {
            Gdx.app.error("FalseKnightAssets", "Missing/!loadable sprite sheet: " + DIR + file
                    + " - falling back. (" + e.getMessage() + ")");
            return null;
        }
    }

    private Animation<TextureRegion> tryCombined(float frameDuration, Animation.PlayMode mode, String... files) {
        try {
            return combined(frameDuration, mode, files);
        } catch (Exception e) {
            Gdx.app.error("FalseKnightAssets", "Missing/!loadable sprite sheet in combo "
                    + java.util.Arrays.toString(files) + " - falling back. (" + e.getMessage() + ")");
            return null;
        }
    }

    private Array<TextureRegion> frames(String file) {
        Texture tex = new Texture(Gdx.files.internal(DIR + file));
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.add(tex);
        int cols = tex.getWidth() / FRAME_W;
        TextureRegion[][] grid = TextureRegion.split(tex, FRAME_W, FRAME_H);
        Array<TextureRegion> out = new Array<>();
        for (int i = 0; i < cols; i++) out.add(grid[0][i]);
        return out;
    }

    private Animation<TextureRegion> strip(String file, float frameDuration, Animation.PlayMode mode) {
        Animation<TextureRegion> a = new Animation<>(frameDuration, frames(file));
        a.setPlayMode(mode);
        return a;
    }

    private Animation<TextureRegion> combined(float frameDuration, Animation.PlayMode mode, String... files) {
        Array<TextureRegion> all = new Array<>();
        for (String f : files) all.addAll(frames(f));
        Animation<TextureRegion> a = new Animation<>(frameDuration, all);
        a.setPlayMode(mode);
        return a;
    }

    public Animation<TextureRegion> get(FalseKnightAnimationType type) {
        return anims.get(type);
    }

    public void dispose() {
        for (Texture t : textures) t.dispose();
    }
}