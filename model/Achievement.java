package HollowKnight.hollowknight.model;

public enum Achievement {
    COMPLETION("Completion", "Finish the game"),
    SPEEDRUN("Speedrun", "Finish the game under the time limit"),
    TRUE_HUNTER("True Hunter", "Kill every type of enemy"),
    DEFEAT_FALSE_KNIGHT("False Knight Felled", "Defeat the False Knight"),
    NO_DAMACE_RUN("Untouchable", "Defeat the boss without taking a hit");

    public final String title;
    public final String description;

    Achievement(String title, String description) {
        this.title = title;
        this.description = description;
    }
}