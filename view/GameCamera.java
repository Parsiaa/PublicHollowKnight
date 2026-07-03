package HollowKnight.hollowknight.view;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class GameCamera {
    private final OrthographicCamera camera;
    private float shakeTimer, shakeStrength;
    private Rectangle lockBounds;

    public GameCamera(OrthographicCamera camera, float viewW, float viewH) {
        this.camera = camera;
    }

    public void shake(float duration, float strength) {
        shakeTimer = duration;
        shakeStrength = strength;
    }

    public void lock(Rectangle bounds) { this.lockBounds = bounds; }
    public void unlock() { this.lockBounds = null; }
    public boolean isLocked() { return lockBounds != null; }

    public void follow(Rectangle target, float levelW, float levelH, float dt) {
        float tx = target.x + target.width / 2f;
        float ty = target.y + target.height / 2f;
        camera.position.x += (tx - camera.position.x) * 8f * dt;
        camera.position.y += (ty - camera.position.y) * 8f * dt;

        if (shakeTimer > 0) {
            shakeTimer -= dt;
            camera.position.x += (MathUtils.random() * 2f - 1f) * shakeStrength;
            camera.position.y += (MathUtils.random() * 2f - 1f) * shakeStrength;
        }

        float hw = camera.viewportWidth * camera.zoom / 2f;
        float hh = camera.viewportHeight * camera.zoom / 2f;

        float minX, maxX, minY, maxY;
        if (lockBounds != null) {
            minX = lockBounds.x + hw;
            maxX = lockBounds.x + lockBounds.width - hw;
            minY = lockBounds.y + hh;
            maxY = lockBounds.y + lockBounds.height - hh;
        } else {
            minX = hw; maxX = levelW - hw;
            minY = hh; maxY = levelH - hh;
        }

        if (maxX > minX) camera.position.x = MathUtils.clamp(camera.position.x, minX, maxX);
        else if (lockBounds != null) camera.position.x = lockBounds.x + lockBounds.width / 2f;
        if (maxY > minY) camera.position.y = MathUtils.clamp(camera.position.y, minY, maxY);
        else if (lockBounds != null) camera.position.y = lockBounds.y + lockBounds.height / 2f;

        camera.position.x = MathUtils.round(camera.position.x);
        camera.position.y = MathUtils.round(camera.position.y);
        camera.update();
    }

    public OrthographicCamera raw() { return camera; }
}