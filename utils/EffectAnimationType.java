
package HollowKnight.hollowknight.utils;

public enum EffectAnimationType {

    NAIL_SLASH      ("animation/Effects/SlashEffect.png",     6, 1, 6,  false),
    NAIL_SLASH_ALT  ("animation/Effects/SlashEffectAlt.png",  6, 1, 6,  false),
    NAIL_UP_SLASH   ("animation/Effects/UpSlashEffect.png",   6, 1, 6,  false),
    NAIL_DOWN_SLASH ("animation/Effects/DownSlashEffect.png", 6, 1, 6,  false),
    DASH_EFFECT     ("animation/Effects/Dash Effect.png",     8, 1, 8,  false),
    VENGEFUL_SPIRIT ("animation/Effects/SoulBall.png",         4, 1, 4,  false),
    HOWLING_WRAITHS ("animation/Effects/ShadowScream.png",    14, 1, 14, false),
    HIT_SHOCKWAVE   ("animation/Effects/Shockwave.png",        8, 1, 8,  false);

    public final String  path;
    public final int     columnCount;
    public final int     rowCount;
    public final int     frameCount;
    public final boolean individualFrames;

    EffectAnimationType(String path, int cols, int rows, int frames, boolean individual) {
        this.path             = path;
        this.columnCount      = cols;
        this.rowCount         = rows;
        this.frameCount       = frames;
        this.individualFrames = individual;
    }
}