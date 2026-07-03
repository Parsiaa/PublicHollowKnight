package HollowKnight.hollowknight.utils;

public enum ZoteAnimationType {
    ZOTE_IDLE("animation/Zote/Idle.png", 5),
    ZOTE_TALK("animation/Zote/Talk.png", 5),
    ZOTE_ATTACK("animation/Zote/Attack.png", 4),
    ZOTE_FALL("animation/Zote/Fall.png", 5),
    ZOTE_GET_UP("animation/Zote/Get Up.png", 4),
    ZOTE_ROLL("animation/Zote/Roll.png", 3),
    ZOTE_TURN("animation/Zote/Turn.png", 2);

    public final String path;
    public final int frameCount;

    ZoteAnimationType(String path, int frameCount) {
        this.path = path;
        this.frameCount = frameCount;
    }
}