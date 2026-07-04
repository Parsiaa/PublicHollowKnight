package HollowKnight.hollowknight.controller;

import com.badlogic.gdx.math.Rectangle;
import java.util.List;

import HollowKnight.hollowknight.model.BreakableWall;
import HollowKnight.hollowknight.model.Knight;
import HollowKnight.hollowknight.model.Level;

public class WallManager {
    private final List<BreakableWall> walls;
    private final Knight knight;
    private final Level level;
    private boolean wasAttacking = false;

    public WallManager(List<BreakableWall> walls, Knight knight, Level level) {
        this.walls = walls;
        this.knight = knight;
        this.level = level;
    }

    public void update(float dt, Rectangle nailHitbox, boolean attacking) {
        for (BreakableWall wall : walls) {
            wall.update(dt);
            if (wall.isBroken()) continue;
            blockKnight(wall);
            if (attacking && !wasAttacking && nailHitbox != null
                    && nailHitbox.overlaps(wall.getBounds())) {
                wall.hit();
                if (wall.isBroken()) level.hideTilesIn(wall.getBounds());
            }
        }
        wasAttacking = attacking;
    }

    private void blockKnight(BreakableWall wall) {
        Rectangle b = knight.getBoundingBox();
        Rectangle w = wall.getBounds();
        if (!b.overlaps(w)) return;
        float overlapLeft = (b.x + b.width) - w.x;
        float overlapRight = (w.x + w.width) - b.x;
        if (overlapLeft < overlapRight) {
            b.x = w.x - b.width;
        } else {
            b.x = w.x + w.width;
        }
        knight.getVelocity().x = 0;
    }
}