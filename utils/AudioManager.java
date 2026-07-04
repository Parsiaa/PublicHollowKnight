package HollowKnight.hollowknight.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Map;

import HollowKnight.hollowknight.model.Settings;

public class AudioManager {
    private Music currentBgm;
    private String currentPath;
    private final Map<String, Sound> sounds = new HashMap<>();

    public void playBgm(String path) {
        if (path != null && path.equals(currentPath) && currentBgm != null) return;
        stopBgm();
        if (path == null || !Gdx.files.internal(path).exists()) return;
        currentBgm = Gdx.audio.newMusic(Gdx.files.internal(path));
        currentBgm.setLooping(true);
        currentPath = path;
        applySettings();
        currentBgm.play();
    }

    public void stopBgm() {
        if (currentBgm != null) { currentBgm.stop(); currentBgm.dispose(); currentBgm = null; }
        currentPath = null;
    }

    public void applySettings() {
        Settings s = Settings.get();
        if (currentBgm != null) currentBgm.setVolume(s.musicEnabled ? s.musicVolume : 0f);
    }

    public void playSound(String path) {
        Settings s = Settings.get();
        if (!s.sfxEnabled || path == null || !Gdx.files.internal(path).exists()) return;
        Sound snd = sounds.get(path);
        if (snd == null) { snd = Gdx.audio.newSound(Gdx.files.internal(path)); sounds.put(path, snd); }
        snd.play(s.sfxVolume);
    }

    public void dispose() {
        stopBgm();
        for (Sound s : sounds.values()) s.dispose();
        sounds.clear();
    }
}