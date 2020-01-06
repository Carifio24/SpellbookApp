package dnd.jon.spellbook;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

class Settings {

    // Keys
    private final static String headerTextKey = "HeaderTextSize";
    private final static String tableTextKey = "TableTextSize";
    private final static String nRowsKey = "TableNRows";
    private final static String spellTextKey = "SpellTextSize";
    private final static String characterKey = "Character";

    // Member values
    private int tableSize;
    private int headerSize;
    private int nRows;
    private int spellSize;
    private String charName;

    // Default values
    static final int defaultHeaderTextSize = 18;
    static final int defaultTextSize = 16;
    static final int defaultNTableRows = 10;
    static final int defaultSpellTextSize = 15;

    Settings(JSONObject json) {
        tableSize = json.optInt(tableTextKey, defaultTextSize);
        nRows = json.optInt(nRowsKey, defaultNTableRows);
        spellSize = json.optInt(spellTextKey, defaultSpellTextSize);
        headerSize = json.optInt(headerTextKey, defaultHeaderTextSize);
        charName = json.optString(characterKey, null);
    }

    Settings() {
        tableSize = defaultTextSize;
        headerSize = defaultHeaderTextSize;
        nRows = defaultNTableRows;
        spellSize = defaultSpellTextSize;
        charName = null;
    }

    // Getters
    String characterName() { return charName; }
    int headerTextSize() { return headerSize; }
    int spellTextSize() { return spellSize; }
    int tableTextSize() { return tableSize; }
    int nTableRows() { return nRows; }

    // Setters
    void setCharacterName(String name) { charName = name; }
    void setHeaderTextSize(int size) { headerSize = size; }
    void setSpellTextSize(int size) { spellSize = size; }
    void setTableTextSize(int size) { tableSize = size; }
    void setNTableRows(int n) { nRows = n; }


    JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(tableTextKey, tableSize);
        json.put(nRowsKey, nRows);
        json.put(spellTextKey, spellSize);
        json.put(headerTextKey, headerSize);
        if (charName != null) {
            json.put(characterKey, charName);
        }
        return json;
    }

    boolean save(File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            JSONObject json = toJSON();
            bw.write(json.toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
