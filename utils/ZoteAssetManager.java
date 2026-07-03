package HollowKnight.hollowknight.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ZoteAssetManager {
    private final EnumMap<ZoteAnimationType, Animation<TextureRegion>> animations = new EnumMap<>(ZoteAnimationType.class);
    private final Map<String, Texture> textures = new HashMap<>();

    public void load() {
        for (ZoteAnimationType type : ZoteAnimationType.values()) {
            Texture texture = textures.computeIfAbsent(type.path, p -> new Texture(Gdx.files.internal(p)));
            int frameW = texture.getWidth() / type.frameCount;
            int frameH = texture.getHeight();
            TextureRegion[][] grid = TextureRegion.split(texture, frameW, frameH);
            TextureRegion[] frames = new TextureRegion[type.frameCount];
            for (int i = 0; i < type.frameCount; i++) frames[i] = grid[0][i];
            Animation<TextureRegion> anim = new Animation<>(0.11f, frames);
            boolean loop = type == ZoteAnimationType.ZOTE_IDLE
                        || type == ZoteAnimationType.ZOTE_TALK
                        || type == ZoteAnimationType.ZOTE_ATTACK;
            anim.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
            animations.put(type, anim);
        }
    }

    public Animation<TextureRegion> get(ZoteAnimationType type) { return animations.get(type); }

    public void dispose() {
        for (Texture t : textures.values()) t.dispose();
        textures.clear();
        animations.clear();
    }
}