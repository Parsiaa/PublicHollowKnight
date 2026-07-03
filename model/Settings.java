package HollowKnight.hollowknight.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Singleton settings, persisted via libGDX Preferences. */
public class Settings {
    private static Settings instance;
    public static Settings get() {
        if (instance == null) instance = new Settings();
        return instance;
    }

    private static final String PREFS = "hollowknight-settings";

    public float   musicVolume;
    public float   sfxVolume;
    public boolean musicEnabled;
    public boolean sfxEnabled;

    private Settings() {
        Preferences p = Gdx.app.getPreferences(PREFS);
        musicVolume  = p.getFloat("musicVolume", 0.7f);
        sfxVolume    = p.getFloat("sfxVolume", 0.8f);
        musicEnabled = p.getBoolean("musicEnabled", true);
        sfxEnabled   = p.getBoolean("sfxEnabled", true);
    }

    public void resetSfx() { sfxVolume = 0.8f; sfxEnabled = true; }

    public void save() {
        Preferences p = Gdx.app.getPreferences(PREFS);
        p.putFloat("musicVolume", musicVolume);
        p.putFloat("sfxVolume", sfxVolume);
        p.putBoolean("musicEnabled", musicEnabled);
        p.putBoolean("sfxEnabled", sfxEnabled);
        p.flush();
    }
}