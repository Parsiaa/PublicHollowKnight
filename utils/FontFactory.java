package HollowKnight.hollowknight.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class FontFactory {
    public static BitmapFont generate(String path, int size) {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(path));
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size = size;
        p.color = Color.WHITE;
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                + "\u00e1\u00e0\u00e2\u00e4\u00e7\u00e9\u00e8\u00ea\u00eb\u00ed\u00ee\u00ef\u00f1\u00f3\u00f4\u00f6\u00fa\u00f9\u00fb\u00fc\u0153\u00e6" + "\u00c1\u00c0\u00c2\u00c4\u00c7\u00c9\u00c8\u00ca\u00cb\u00cd\u00ce\u00cf\u00d1\u00d3\u00d4\u00d6\u00da\u00d9\u00db\u00dc\u0152\u00c6" + "\u00bf\u00a1";
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        BitmapFont font = gen.generateFont(p);
        gen.dispose();
        return font;
    }
}