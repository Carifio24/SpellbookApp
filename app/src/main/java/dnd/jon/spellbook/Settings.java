package dnd.jon.spellbook;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import dnd.jon.spellbook.MainActivity;

class Settings {

    // Keys
    static String favoriteKey = "Favorite";
    static String preparedKey = "Prepared";
    static String knownKey = "Known";
    static String headerTextKey = "HeaderTextSize";
    static String tableTextKey = "TableTextSize";
    static String nRowsKey = "TableNRows";
    static String spellTextKey = "SpellTextSize";
    static String characterKey = "Character";
    static String booksFilterKey = "BooksFilter";

    boolean filterByFavorites;
    boolean filterByPrepared;
    boolean filterByKnown;
    int tableTextSize;
    int headerTextSize;
    int nTableRows;
    int spellTextSize;
    String characterName;
    HashMap<Sourcebook, Boolean> filterByBooks;

    static int defaultHeaderTextSize = 18;
    static int defaultTextSize = 16;
    static int defaultNTableRows = 10;
    static int defaultSpellTextSize = 15;

    Settings(JSONObject json) {
        filterByFavorites = json.optBoolean(favoriteKey, false);
        filterByPrepared = json.optBoolean(preparedKey, false);
        filterByKnown = json.optBoolean(knownKey, false);
        tableTextSize = json.optInt(tableTextKey, defaultTextSize);
        nTableRows = json.optInt(nRowsKey, defaultNTableRows);
        spellTextSize = json.optInt(spellTextKey, defaultSpellTextSize);
        headerTextSize = json.optInt(headerTextKey, defaultHeaderTextSize);
        characterName = json.optString(characterKey, null);
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
        tableTextSize = defaultTextSize;
        headerTextSize = defaultHeaderTextSize;
        nTableRows = defaultNTableRows;
        spellTextSize = defaultSpellTextSize;
        filterByBooks = new HashMap<>();
        characterName = null;
    }

    JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(favoriteKey, filterByFavorites);
        json.put(preparedKey, filterByPrepared);
        json.put(knownKey, filterByKnown);
        json.put(tableTextKey, tableTextSize);
        json.put(nRowsKey, nTableRows);
        json.put(spellTextKey, spellTextSize);
        json.put(headerTextKey, headerTextSize);
        if (characterName != null) {
            json.put(characterKey, characterName);
        }
        JSONObject books = new JSONObject();
        for (Sourcebook sb : Sourcebook.values()) {
            books.put(sb.code(), filterByBooks.get(sb));
        }
        json.put(booksFilterKey, books);
        return json;
    }
}
