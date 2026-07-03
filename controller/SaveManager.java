package HollowKnight.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import HollowKnight.hollowknight.model.GameData;

public class SaveManager {
    public static final int SLOT_COUNT = 4;
    private static final String DIR = "saves/";

    private final Json json = new Json();

    public SaveManager() {
        json.setOutputType(JsonWriter.OutputType.json);
    }

    private FileHandle slotFile(int slot) {
        return Gdx.files.local(DIR + "slot" + slot + ".json");
    }

    public boolean slotExists(int slot) {
        return slotFile(slot).exists();
    }

    public void save(int slot, GameData data) {
        data.used = true;
        slotFile(slot).writeString(json.prettyPrint(data), false);
    }

    public GameData load(int slot) {
        FileHandle f = slotFile(slot);
        if (!f.exists()) return null;
        try {
            return json.fromJson(GameData.class, f.readString());
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Corrupt save in slot " + slot, e);
            return null;
        }
    }

    public void delete(int slot) {
        FileHandle f = slotFile(slot);
        if (f.exists()) f.delete();
    }
}