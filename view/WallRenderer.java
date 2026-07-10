package HollowKnight.hollowknight.view;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import java.util.List;

import HollowKnight.hollowknight.model.BreakableWall;

public class WallRenderer {
    private final ShapeRenderer shape = new ShapeRenderer();

    public void render(com.badlogic.gdx.graphics.OrthographicCamera camera, List<BreakableWall> walls) {
    }

    public void renderSecrets(com.badlogic.gdx.graphics.OrthographicCamera camera, List<Rectangle> hidden) {
        if (hidden.isEmpty()) return;
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 1f);
        for (Rectangle r : hidden) shape.rect(r.x, r.y, r.width, r.height);
        shape.end();
    }

    public void dispose() { shape.dispose(); }
}