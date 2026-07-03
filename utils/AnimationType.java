package HollowKnight.hollowknight.utils;

public enum AnimationType {
    HOLLOW_KNIGHT_IDLE("animation/Idle.png", 9, 1, 9),
    HOLLOW_KNIGHT_RUN("animation/Run.png", 13, 1, 13),
    HOLLOW_KNIGHT_JUMP("animation/Airborne.png", 12, 1, 12),
    HOLLOW_KNIGHT_DASH("animation/Dash.png", 12, 1, 12),
    HOLLOW_KNIGHT_AIRBORNE("animation/Airborne.png", 12, 1, 12),
    HOLLOW_KNIGHT_DEATH("animation/Death.png", 18, 1, 18),
    HOLLOW_KNIGHT_DOUBLE_JUMP("animation/Double Jump.png", 8, 1, 8),
    HOLLOW_KNIGHT_DOWN_SLASH("animation/DownSlash.png", 5, 1, 5),
    HOLLOW_KNIGHT_FALL("animation/Fall.png", 6 , 1, 6),
    HOLLOW_KNIGHT_FIREBALL_CAST("animation/Fireball Cast.png", 9, 1, 9),
    HOLLOW_KNIGHT_FOCUS_END("animation/Focus End.png", 3 , 1 , 3),
    HOLLOW_KNIGHT_FOCUS_GET("animation/Focus Get.png", 6, 1, 6),
    HOLLOW_KNIGHT_FOCUS_START("animation/Focus Start.png", 3, 1 , 3),
    HOLLOW_KNIGHT_FOCUS("animation/Focus.png", 7, 1, 7),
    HOLLOW_KNIGHT_IDLE_HURT("animation/Idle Hurt.png", 12, 1 ,12),
    HOLLOW_KNIGHT_LANDING("animation/Landing.png", 4, 1, 4),
    HOLLOW_KNIGHT_LOOK_DOWN("animation/LookDown.png", 6, 1 ,6),
    HOLLOW_KNIGHT_LOOK_UP("animation/LookUp.png", 6 , 1 , 6),
    HOLLOW_KNIGHT_RUN_TO_IDLE("animation/Run to Idle.png", 6 , 1, 6),
    HOLLOW_KNIGHT_SCREAM("animation/Scream.png", 7, 1, 7),
    HOLLOW_KNIGHT_SLASH("animation/Slash.png", 5, 1, 5),
    HOLLOW_KNIGHT_SLASH_ALT("animation/SlashAlt.png", 5, 1, 5),
    HOLLOW_KNIGHT_UP_SLASH("animation/UpSlash.png", 5, 1, 5),
    HOLLOW_KNIGHT_WALL_SLIDE("animation/Wall Slide.png", 4, 1, 4),
    HOLLOW_KNIGHT_WALL_JUMP("animation/Walljump.png", 9, 1, 9),
    ;

    public final String path;
    public final int frameCount;
    public final int rowCount;
    public final int columnCount;

    AnimationType(String path, int frameCount, int rowCount, int columnCount) {
        this.path = path;
        this.frameCount = frameCount;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }
}