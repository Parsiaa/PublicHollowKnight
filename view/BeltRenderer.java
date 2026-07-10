package HollowKnight.hollowknight.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

public class BeltRenderer {
    private static final String PATH = "Crystal Peak/SpriteAtlasTexture-Mines_Core-512x1024-fmt12-conveyor.png";
    private static final int FRAMES = 7;
    private static final float FRAME_TIME = 0.1f;
    private static final float ROTATION = 90f;

    private Texture texture;
    private Animation<TextureRegion> anim;

    public BeltRenderer() {
        try {
            texture = new Texture(Gdx.files.internal(PATH));
            int frameW = texture.getWidth();
            int frameH = texture.getHeight() / FRAMES;
            TextureRegion[][] grid = TextureRegion.split(texture, frameW, frameH);
            TextureRegion[] frames = new TextureRegion[FRAMES];
            for (int i = 0; i < FRAMES; i++) frames[i] = grid[i][0];
            anim = new Animation<>(FRAME_TIME, frames);
            anim.setPlayMode(Animation.PlayMode.LOOP);
        } catch (Exception e) {
            Gdx.app.error("BeltRenderer", "Failed to load " + PATH, e);
        }
    }

    public void render(SpriteBatch batch, List<Rectangle> belts, float time) {
        if (anim == null || belts.isEmpty()) return;
        TextureRegion frame = anim.getKeyFrame(time);
        for (Rectangle b : belts) {
            batch.draw(frame, b.x + b.width, b.y, 0f, 0f, b.height, b.width, 1f, 1f, ROTATION);
        }
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
