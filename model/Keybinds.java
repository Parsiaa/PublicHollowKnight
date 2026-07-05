package HollowKnight.hollowknight.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;

import java.util.EnumMap;

public class Keybinds {

    public enum Action {
        LEFT("Move Left", Keys.LEFT),
        RIGHT("Move Right", Keys.RIGHT),
        UP("Look Up", Keys.UP),
        DOWN("Look Down", Keys.DOWN),
        JUMP("Jump", Keys.Z),
        ATTACK("Attack", Keys.X),
        DASH("Dash", Keys.C),
        FOCUS("Focus / Heal", Keys.A),
        CAST_SPIRIT("Vengeful Spirit", Keys.Q),
        CAST_WRAITHS("Howling Wraiths", Keys.W),
        INVENTORY("Inventory", Keys.I);

        public final String label;
        public final int def;
        Action(String label, int def) { this.label = label; this.def = def; }
    }

    private static final String PREFS = "hollowknight-keybinds";
    private static Keybinds instance;
    public static Keybinds get() {
        if (instance == null) instance = new Keybinds();
        return instance;
    }

    private final EnumMap<Action, Integer> keys = new EnumMap<>(Action.class);

    private Keybinds() {
        Preferences p = Gdx.app.getPreferences(PREFS);
        for (Action a : Action.values()) keys.put(a, p.getInteger(a.name(), a.def));
    }

    public int key(Action a) { return keys.get(a); }
    public String keyName(Action a) { return Keys.toString(keys.get(a)); }

    public void set(Action a, int keycode) { keys.put(a, keycode); save(); }

    public void resetDefaults() {
        for (Action a : Action.values()) keys.put(a, a.def);
        save();
    }

    public void save() {
        Preferences p = Gdx.app.getPreferences(PREFS);
        for (Action a : Action.values()) p.putInteger(a.name(), keys.get(a));
        p.flush();
    }
}
