
package HollowKnight.hollowknight.utils;

public enum EnemyAnimationType {

    CRAWLER_WALK ("animation/Crystal_Crawler/Walk.png", 4, 1, 4),
    CRAWLER_TURN ("animation/Crystal_Crawler/Turn.png", 2, 1, 2),
    CRAWLER_DEATH_AIR ("animation/Crystal_Crawler/Death Air.png", 3, 1, 3),
    CRAWLER_DEATH_LAND("animation/Crystal_Crawler/Death Land.png", 2, 1, 2),

    MOSSFLY_SHAKE ("animation/Mossfly/Shake.png", 3, 1, 3),
    MOSSFLY_TURN_TO_FLY("animation/Mossfly/TurnToFly.png", 3, 1, 3),
    MOSSFLY_APPEAR ("animation/Mossfly/Appear.png", 6, 1, 6),
    MOSSFLY_FLY ("animation/Mossfly/Fly.png", 4, 1, 4),
    MOSSFLY_DEATH_AIR ("animation/Mossfly/Death Air.png", 4, 1, 4),
    MOSSFLY_DEATH_LAND ("animation/Mossfly/Death Land.png", 4, 1, 4),

    HORNHEAD_WALK ("animation/Husk_Hornhead/Walk.png", 7, 1, 7),
    HORNHEAD_IDLE ("animation/Husk_Hornhead/Idle.png", 6, 1, 6),
    HORNHEAD_ANTICIPATE ("animation/Husk_Hornhead/Attack Anticipate.png",5, 1, 5),
    HORNHEAD_LUNGE ("animation/Husk_Hornhead/Attack Lunge.png", 12, 1, 12),
    HORNHEAD_COOLDOWN ("animation/Husk_Hornhead/Attack Cooldown.png", 1, 1, 1),
    HORNHEAD_TURN ("animation/Husk_Hornhead/Turn.png", 2, 1, 2),
    HORNHEAD_DEATH_AIR ("animation/Husk_Hornhead/Death Air.png", 1, 1, 1),
    HORNHEAD_DEATH_LAND ("animation/Husk_Hornhead/Death Land.png", 8, 1, 8),

    GUARDIAN_IDLE ("animation/Crystallized/Idle.png", 5, 1, 5),
    GUARDIAN_SHOOT ("animation/Crystallized/Shoot.png", 7, 1, 7),
    GUARDIAN_EVADE ("animation/Crystallized/Evade.png", 7, 1, 7),
    GUARDIAN_RUN ("animation/Crystallized/Run.png", 6, 1, 6),
    GUARDIAN_TURN ("animation/Crystallized/Turn.png", 3, 1, 3),
    GUARDIAN_DEATH_AIR ("animation/Crystallized/Death Air.png", 3, 1, 3),
    GUARDIAN_DEATH_LAND ("animation/Crystallized/Death Land.png", 3, 1, 3),
    GUARDIAN_LASER ("animation/Effects/Laser.png", 15, 1, 15),
    GUARDIAN_LASER_CIRCLE ("animation/Effects/LaserCircle_003.png", 1, 1, 1);

    public final String path;
    public final int columnCount;
    public final int rowCount;
    public final int frameCount;

    EnemyAnimationType(String path, int columnCount, int rowCount, int frameCount) {
        this.path = path;
        this.columnCount = columnCount;
        this.rowCount = rowCount;
        this.frameCount = frameCount;
    }
}