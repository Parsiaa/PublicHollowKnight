package HollowKnight.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import HollowKnight.hollowknight.model.Achievement;

public class AchievementManager {
    private static final String PREFS = "hollowknight-achievements";

    private static AchievementManager instance;
    public static AchievementManager get() {
        if (instance == null) instance = new AchievementManager();
        return instance;
    }

    private final Set<Achievement> unlocked = EnumSet.noneOf(Achievement.class);
    private final List<AchievementListener> listeners = new ArrayList<>();

    private AchievementManager() {
        Preferences p = Gdx.app.getPreferences(PREFS);
        for (Achievement a : Achievement.values()) {
            if (p.getBoolean(a.name(), false)) unlocked.add(a);
        }
    }

    public void addListener(AchievementListener l) { listeners.add(l); }
    public void removeListener(AchievementListener l) { listeners.remove(l); }

    public boolean isUnlocked(Achievement a) { return unlocked.contains(a); }

    public void unlock(Achievement a) {
        if (!unlocked.add(a)) return;
        Gdx.app.getPreferences(PREFS).putBoolean(a.name(), true).flush();
        for (AchievementListener l : listeners) l.onUnlocked(a);
    }
}