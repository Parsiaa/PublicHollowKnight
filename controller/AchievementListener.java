package HollowKnight.hollowknight.controller;

import HollowKnight.hollowknight.model.Achievement;

public interface AchievementListener {
    void onUnlocked(Achievement achievement);
}