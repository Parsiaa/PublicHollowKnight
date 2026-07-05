package HollowKnight.hollowknight.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class EffectAssetManager {

    private final EnumMap<EffectAnimationType, Animation<TextureRegion>> animations = new EnumMap<>(EffectAnimationType.class);
    private final Map<String, Texture> textures = new HashMap<>();

    public void load() {
        for (EffectAnimationType type : EffectAnimationType.values()) {
            Animation<TextureRegion> anim = loadFromSheet(type);
            if (anim == null) continue;
            anim.setPlayMode(Animation.PlayMode.NORMAL);
            animations.put(type, anim);
        }
    }

    private Animation<TextureRegion> loadFromSheet(EffectAnimationType type) {
        if (!Gdx.files.internal(type.path).exists()) return null;
        Texture texture = textures.computeIfAbsent(
                type.path,
                path -> new Texture(Gdx.files.internal(path)));

        int frameW = texture.getWidth()  / type.columnCount;
        int frameH = texture.getHeight() / type.rowCount;

        TextureRegion[][] grid = TextureRegion.split(texture, frameW, frameH);

        TextureRegion[] frames = new TextureRegion[type.frameCount];
        int idx = 0;
        outer:
        for (int r = 0; r < type.rowCount; r++) {
            for (int c = 0; c < type.columnCount; c++) {
                if (idx >= type.frameCount) break outer;
                frames[idx++] = grid[r][c];
            }
        }
        return new Animation<>(0.045f, frames);
    }


    public Animation<TextureRegion> get(EffectAnimationType type) {
        return animations.get(type);
    }

    public void dispose() {
        for (Texture t : textures.values()) t.dispose();
        textures.clear();
        animations.clear();
    }
}
