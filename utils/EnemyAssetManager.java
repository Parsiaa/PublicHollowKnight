package HollowKnight.hollowknight.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class EnemyAssetManager {

    private final EnumMap<EnemyAnimationType, Animation<TextureRegion>> animations = new EnumMap<>(EnemyAnimationType.class);
    private final Map<String, Texture> textures = new HashMap<>();

    public void load() {
        for (EnemyAnimationType type : EnemyAnimationType.values()) {

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

            Animation<TextureRegion> anim = new Animation<>(0.08f, frames);
            anim.setPlayMode(Animation.PlayMode.LOOP);
            animations.put(type, anim);
        }

        setNormal(EnemyAnimationType.CRAWLER_TURN);
        setNormal(EnemyAnimationType.CRAWLER_DEATH_AIR);
        setNormal(EnemyAnimationType.CRAWLER_DEATH_LAND);
        setNormal(EnemyAnimationType.MOSSFLY_DEATH_AIR);
        setNormal(EnemyAnimationType.MOSSFLY_DEATH_LAND);
        setNormal(EnemyAnimationType.MOSSFLY_APPEAR);
        setNormal(EnemyAnimationType.MOSSFLY_TURN_TO_FLY);
        setNormal(EnemyAnimationType.HORNHEAD_DEATH_LAND);
    }

    private void setNormal(EnemyAnimationType type) {
        Animation<TextureRegion> a = animations.get(type);
        if (a != null) a.setPlayMode(Animation.PlayMode.NORMAL);
    }

    public Animation<TextureRegion> get(EnemyAnimationType type) {
        return animations.get(type);
    }

    public void dispose() {
        for (Texture t : textures.values()) t.dispose();
        textures.clear();
        animations.clear();
    }
}
