package HollowKnight.hollowknight.utils;

import java.util.HashMap;
import java.util.Map;

import HollowKnight.hollowknight.model.Settings;

public class Lang {
    public static final int EN = 0, FR = 1, ES = 2;
    public static final int COUNT = 3;

    private static final String[] LANG_NAMES = { "English", "Fran\u00e7ais", "Espa\u00f1ol" };
    private static final Map<String, String[]> T = new HashMap<>();

    static {
        put("start_game",    "Start Game",   "Commencer",        "Empezar");
        put("settings",      "Settings",     "Param\u00e8tres",  "Ajustes");
        put("guide",         "Guide",        "Guide",            "Gu\u00eda");
        put("achievements",  "Achievements", "Succ\u00e8s",      "Logros");
        put("quit",          "Quit",         "Quitter",          "Salir");

        put("paused",        "PAUSED",       "EN PAUSE",         "PAUSA");
        put("continue",      "Continue",     "Continuer",        "Continuar");
        put("cheat_codes",   "Cheat Codes",  "Codes de Triche",  "Trucos");
        put("save_quit",     "Save & Quit",  "Sauver & Quitter", "Guardar y Salir");

        put("settings_title","SETTINGS",     "PARAM\u00c8TRES",  "AJUSTES");
        put("music_volume",  "Music Volume", "Volume Musique",   "Volumen M\u00fasica");
        put("music",         "Music",        "Musique",          "M\u00fasica");
        put("sfx_volume",    "SFX Volume",   "Volume SFX",       "Volumen SFX");
        put("sfx",           "SFX",          "SFX",              "SFX");
        put("reset_sfx",     "Reset SFX",    "R\u00e9init. SFX", "Restablecer SFX");
        put("language",      "Language",     "Langue",           "Idioma");
        put("back",          "Back",         "Retour",           "Atr\u00e1s");
        put("on",            "On",           "Oui",              "S\u00ed");
        put("off",           "Off",          "Non",              "No");
        put("settings_hint",
                "Arrows: navigate / adjust    Enter / Click: select    Esc: back",
                "Fl\u00e8ches: naviguer / r\u00e9gler    Entr\u00e9e / Clic: choisir    \u00c9chap: retour",
                "Flechas: navegar / ajustar    Enter / Clic: elegir    Esc: volver");
    }

    private static void put(String key, String en, String fr, String es) {
        T.put(key, new String[] { en, fr, es });
    }

    public static int index() {
        int i = Settings.get().language;
        return ((i % COUNT) + COUNT) % COUNT;
    }

    public static String langName() { return LANG_NAMES[index()]; }

    public static void cycle() {
        Settings s = Settings.get();
        s.language = (s.language + 1) % COUNT;
        s.save();
    }

    public static String t(String key) {
        String[] v = T.get(key);
        return v == null ? key : v[index()];
    }
}
