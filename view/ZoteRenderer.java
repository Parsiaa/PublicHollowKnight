package HollowKnight.hollowknight.view;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import HollowKnight.hollowknight.model.Zote;
import HollowKnight.hollowknight.utils.ZoteAnimationType;
import HollowKnight.hollowknight.utils.ZoteAssetManager;

public class ZoteRenderer {
    private final ZoteAssetManager assets;

    public ZoteRenderer(ZoteAssetManager assets) {
        this.assets = assets;
    }

    public void render(SpriteBatch batch, Zote zote) {
        ZoteAnimationType type;
        switch (zote.getState()) {
            case TALK: type = ZoteAnimationType.ZOTE_TALK; break;
            case ATTACK: type = ZoteAnimationType.ZOTE_ATTACK; break;
            case ROLL: type = ZoteAnimationType.ZOTE_ROLL; break;
            case FALL: type = ZoteAnimationType.ZOTE_FALL; break;
            case GET_UP: type = ZoteAnimationType.ZOTE_GET_UP; break;
            default: type = ZoteAnimationType.ZOTE_IDLE; break;
        }
        Animation<TextureRegion> anim = assets.get(type);
        if (anim == null) return;
        TextureRegion frame = anim.getKeyFrame(zote.getStateTime());
        Rectangle b = zote.getBounds();

        float drawH = b.height * 1.4222f;
        float drawW = drawH * ((float) frame.getRegionWidth() / frame.getRegionHeight());
        float drawX = b.x + b.width / 2f - drawW / 2f;
        float drawY = b.y;

        batch.draw(frame.getTexture(), drawX, drawY, drawW, drawH,
                frame.getRegionX(), frame.getRegionY(),
                frame.getRegionWidth(), frame.getRegionHeight(),
                zote.facingRight, false);
    }
}