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

    public void dispose() { shape.dispose(); }
}