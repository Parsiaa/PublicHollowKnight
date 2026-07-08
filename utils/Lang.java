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
        put("start_game", "Start Game", "Commencer", "Empezar");
        put("settings", "Settings", "Param\u00e8tres", "Ajustes");
        put("guide", "Guide", "Guide", "Gu\u00eda");
        put("achievements", "Achievements", "Succ\u00e8s", "Logros");
        put("quit", "Quit", "Quitter", "Salir");
        put("paused", "PAUSED", "EN PAUSE", "PAUSA");
        put("continue", "Continue", "Continuer", "Continuar");
        put("cheat_codes", "Cheat Codes", "Codes de Triche", "Trucos");
        put("save_quit", "Save & Quit", "Sauver & Quitter", "Guardar y Salir");
        put("settings_title", "SETTINGS", "PARAM\u00c8TRES", "AJUSTES");
        put("music_volume", "Music Volume", "Volume Musique", "Volumen M\u00fasica");
        put("music", "Music", "Musique", "M\u00fasica");
        put("sfx_volume", "SFX Volume", "Volume SFX", "Volumen SFX");
        put("sfx", "SFX", "SFX", "SFX");
        put("reset_sfx", "Reset SFX", "R\u00e9init. SFX", "Restablecer SFX");
        put("language", "Language", "Langue", "Idioma");
        put("controls", "Controls", "Contr\u00f4les", "Controles");
        put("back", "Back", "Retour", "Atr\u00e1s");
        put("on", "On", "Oui", "S\u00ed");
        put("off", "Off", "Non", "No");
        put("settings_hint",
                "Arrows: navigate / adjust    Enter / Click: select    Esc: back",
                "Fl\u00e8ches: naviguer / r\u00e9gler    Entr\u00e9e / Clic: choisir    \u00c9chap: retour",
                "Flechas: navegar / ajustar    Enter / Clic: elegir    Esc: volver");
        put("brightness", "Brightness", "Luminosit\u00e9", "Brillo");
        put("esc_back", "Esc - back", "\u00c9chap - retour", "Esc - volver");
        put("guide_controls_title", "CONTROLS", "CONTR\u00d4LES", "CONTROLES");
        put("guide_abilities_title", "ABILITIES", "CAPACIT\u00c9S", "HABILIDADES");
        put("guide_cheats_title",
                "CHEATS (hold Left Ctrl)",
                "TRICHES (maintenir Ctrl gauche)",
                "TRUCOS (mant\u00e9n Ctrl izq.)");
        put("guide_ability_desc",
                "Hit enemies with the Nail to gain SOUL. Spend SOUL to Focus (heal) or cast spells.",
                "Frappez les ennemis avec le Clou pour gagner de l'\u00c2ME. D\u00e9pensez l'\u00c2ME pour vous concentrer (soigner) ou lancer des sorts.",
                "Golpea a los enemigos con el Aguij\u00f3n para ganar ALMA. Gasta ALMA para concentrarte (curar) o lanzar hechizos.");
        put("guide_cheats_desc",
                "T Teleport | N Noclip | H Heal | R Refill Soul | G God Mode | K Kill All",
                "T T\u00e9l\u00e9port | N Noclip | H Soin | R Recharge \u00c2me | G Invincible | K Tout Tuer",
                "T Teleport | N Noclip | H Curar | R Rellenar Alma | G Modo Dios | K Matar Todo");
        put("lbl_move", "Move", "D\u00e9placer", "Mover");
        put("lbl_look", "Look", "Regarder", "Mirar");
        put("lbl_jump", "Jump", "Saut", "Salto");
        put("guide_double", "x2 double jump", "x2 double saut", "x2 doble salto");
        put("lbl_attack", "Attack", "Attaque", "Ataque");
        put("lbl_dash", "Dash", "Ru\u00e9e", "Impulso");
        put("lbl_focus", "Focus/Heal", "Concentrer/Soigner", "Concentrar/Curar");
        put("lbl_spirit", "Vengeful Spirit", "Esprit Vengeur", "Esp\u00edritu Vengativo");
        put("lbl_wraiths", "Howling Wraiths", "Spectres Hurlants", "Espectros Aulladores");
        put("lbl_inventory", "Inventory", "Inventaire", "Inventario");
        put("lbl_pause", "Pause", "Pause", "Pausa");
        put("achievements_title", "ACHIEVEMENTS", "SUCC\u00c8S", "LOGROS");
        put("locked", "[LOCKED]", "[VERROUILL\u00c9]", "[BLOQUEADO]");
        put("victory", "VICTORY", "VICTOIRE", "VICTORIA");
        put("lbl_deaths", "Deaths", "Morts", "Muertes");
        put("lbl_enemies_slain", "Enemies slain", "Ennemis vaincus", "Enemigos abatidos");
        put("lbl_time", "Time", "Temps", "Tiempo");
        put("restart", "Restart", "Recommencer", "Reiniciar");
        put("main_menu", "Main Menu", "Menu Principal", "Men\u00fa Principal");
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
