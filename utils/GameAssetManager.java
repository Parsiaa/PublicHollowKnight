package HollowKnight.hollowknight.utils;

import java.util.HashMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GameAssetManager {
    private final HashMap<AnimationType, Animation<TextureRegion>> animationMap = new HashMap<>();
    private final HashMap<String, Texture> loadedTextures = new HashMap<>();

    private TextureRegion emptyHealth;
    private Animation<TextureRegion> filledHealthShine;
    private Animation<TextureRegion> breakHealth;
    private Animation<TextureRegion> healthRefill;
    private TextureRegion soulVesselFrame;
    private TextureRegion soulLiquid;

    public void loadAssets() {
        for (AnimationType type : AnimationType.values()) {
            if (!loadedTextures.containsKey(type.path)) {
                loadedTextures.put(type.path, new Texture(Gdx.files.internal(type.path)));
            }
            Texture texture = loadedTextures.get(type.path);

            int frameWidth = texture.getWidth() / type.columnCount;
            int frameHeight = texture.getHeight() / type.rowCount;
            TextureRegion[][] textureRegion2D = TextureRegion.split(texture, frameWidth, frameHeight);
            
            TextureRegion[] textureRegion1D = new TextureRegion[type.frameCount];
            int index = 0;

            for (int i = 0; i < type.rowCount; i++) {
                for (int j = 0; j < type.columnCount; j++) {
                    if (index >= type.frameCount) break;
                    textureRegion1D[index++] = textureRegion2D[i][j];
                }
            }

            Animation<TextureRegion> animation = new Animation<>(0.08f, textureRegion1D);
            animation.setPlayMode(Animation.PlayMode.LOOP);
            animationMap.put(type, animation);
        }

        emptyHealth = handleUiRegion("animation/HUD/EmptyHealth.png", 1, 1, 1).getKeyFrame(0);
        filledHealthShine = handleUiRegion("animation/HUD/FilledHealthShine.png", 5, 1, 5);
        filledHealthShine.setPlayMode(Animation.PlayMode.LOOP);
        
        breakHealth = handleUiRegion("animation/HUD/BreakHealth.png", 6, 1, 6);
        breakHealth.setPlayMode(Animation.PlayMode.NORMAL);
        
        healthRefill = handleUiRegion("animation/HUD/HealthRefill.png", 5, 1, 5);
        healthRefill.setPlayMode(Animation.PlayMode.NORMAL);
        
        // Clean soul-orb art: Empty is the vessel, Full is the orb-shaped liquid we crop for the fill.
        soulVesselFrame = handleUiRegion("animation/HUD/SoulOrb_Empty.png", 1, 1, 1).getKeyFrame(0);
        soulLiquid = handleUiRegion("animation/HUD/SoulOrb_Full.png", 1, 1, 1).getKeyFrame(0);
    }

    private Animation<TextureRegion> handleUiRegion(String path, int cols, int rows, int frameCount) {
        if (!loadedTextures.containsKey(path)) {
            loadedTextures.put(path, new Texture(Gdx.files.internal(path)));
        }
        Texture texture = loadedTextures.get(path);
        int fWidth = texture.getWidth() / cols;
        int fHeight = texture.getHeight() / rows;
        TextureRegion[][] sample2D = TextureRegion.split(texture, fWidth, fHeight);
        TextureRegion[] sample1D = new TextureRegion[frameCount];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (index >= frameCount) break;
                sample1D[index++] = sample2D[i][j];
            }
        }
        return new Animation<>(0.08f, sample1D);
    }

    public Animation<TextureRegion> getAnimation(AnimationType type) {
        return animationMap.get(type);
    }

    public TextureRegion getEmptyHealth() {
        return emptyHealth;
    }

    public Animation<TextureRegion> getFilledHealthShine() {
        return filledHealthShine;
    }

    public Animation<TextureRegion> getBreakHealth() {
        return breakHealth;
    }

    public Animation<TextureRegion> getHealthRefill() {
        return healthRefill;
    }

    public TextureRegion getSoulVesselFrame() {
        return soulVesselFrame;
    }

    public TextureRegion getSoulLiquid() {
        return soulLiquid;
    }

    public void dispose() {
        for (Texture texture : loadedTextures.values()) {
            texture.dispose();
        }
        loadedTextures.clear();
        animationMap.clear();
    }
}