package dnd.jon.spellbook;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import dnd.jon.spellbook.MainActivity;

class Settings {

    // Keys
    final static String favoriteKey = "Favorite";
    final static String preparedKey = "Prepared";
    final static String knownKey = "Known";
    final static String headerTextKey = "HeaderTextSize";
    final static String tableTextKey = "TableTextSize";
    final static String nRowsKey = "TableNRows";
    final static String spellTextKey = "SpellTextSize";
    final static String characterKey = "Character";
    final static String booksFilterKey = "BooksFilter";

    // Member values
    private boolean filterByFavorites;
    private boolean filterByPrepared;
    private boolean filterByKnown;
    private int tableSize;
    private int headerSize;
    private int nRows;
    private int spellSize;
    private String charName;
    private HashMap<Sourcebook, Boolean> filterByBooks;

    // Default values
    static final int defaultHeaderTextSize = 18;
    static final int defaultTextSize = 16;
    static final int defaultNTableRows = 10;
    static final int defaultSpellTextSize = 15;
    private static HashMap<Sourcebook, Boolean> defaultFilterMap = new HashMap<Sourcebook, Boolean>() {{
        put(Sourcebook.PLAYERS_HANDBOOK, true);
        put(Sourcebook.XANATHARS_GTE, false);
        put(Sourcebook.SWORD_COAST_AG, false);
    }};

    Settings(JSONObject json) {
        filterByFavorites = json.optBoolean(favoriteKey, false);
        filterByPrepared = json.optBoolean(preparedKey, false);
        filterByKnown = json.optBoolean(knownKey, false);
        tableSize = json.optInt(tableTextKey, defaultTextSize);
        nRows = json.optInt(nRowsKey, defaultNTableRows);
        spellSize = json.optInt(spellTextKey, defaultSpellTextSize);
        headerSize = json.optInt(headerTextKey, defaultHeaderTextSize);
        charName = json.optString(characterKey, null);
        JSONObject books = json.optJSONObject(booksFilterKey);
        filterByBooks = new HashMap<>();
        for (Sourcebook sb : Sourcebook.values()) {
            boolean tf = books.optBoolean(sb.code(), false);
            filterByBooks.put(sb, tf);
        }
    }

    Settings() {
        filterByFavorites = false;
        filterByPrepared = false;
        filterByKnown = false;
        tableSize = defaultTextSize;
        headerSize = defaultHeaderTextSize;
        nRows = defaultNTableRows;
        spellSize = defaultSpellTextSize;
        filterByBooks = defaultFilterMap;
        charName = null;
    }

    // Getters
    boolean filterFavorites() { return filterByFavorites; }
    boolean filterPrepared() { return filterByPrepared; }
    boolean filterKnown() { return filterByKnown; }
    boolean getFilter(Sourcebook sb) { return filterByBooks.get(sb); }
    String characterName() { return charName; }
    int headerTextSize() { return headerSize; }
    int spellTextSize() { return spellSize; }
    int tableTextSize() { return tableSize; }
    int nTableRows() { return nRows; }

    // Not quite getters, but retrieving some property
    boolean isStatusFilterSet() {
        return filterByFavorites || filterByPrepared || filterByKnown;
    }


    // Setters
    void setFilterFavorites(boolean fav) { filterByFavorites = fav; }
    void setFilterPrepared(boolean prep) { filterByPrepared = prep; }
    void setFilterKnown(boolean known) { filterByKnown = known; }
    void setBookFilter(Sourcebook sb, boolean tf) { filterByBooks.put(sb, tf); }
    void setCharacterName(String name) { charName = name; }
    void setHeaderTextSize(int size) { headerSize = size; }
    void setSpellTextSize(int size) { spellSize = size; }
    void setTableTextSize(int size) { tableSize = size; }
    void setNTableRows(int n) { nRows = n; }


    JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(favoriteKey, filterByFavorites);
        json.put(preparedKey, filterByPrepared);
        json.put(knownKey, filterByKnown);
        json.put(tableTextKey, tableSize);
        json.put(nRowsKey, nRows);
        json.put(spellTextKey, spellSize);
        json.put(headerTextKey, headerSize);
        if (charName != null) {
            json.put(characterKey, charName);
        }
        JSONObject books = new JSONObject();
        for (Sourcebook sb : Sourcebook.values()) {
            books.put(sb.code(), filterByBooks.get(sb));
        }
        json.put(booksFilterKey, books);
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
