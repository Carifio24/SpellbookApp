package dnd.jon.spellbook;

import android.os.AsyncTask;
import android.util.StatsLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.xml.transform.Source;

class CharacterProfile {

    // The map of spell statuses
    private String charName;
    private HashMap<String,SpellStatus> spellStatuses;
    private SortField sortField1;
    private SortField sortField2;
    private CasterClass filterClass = null;
    private boolean reverse1;
    private boolean reverse2;
    private HashMap<Sourcebook,Boolean> filterByBooks;
    private StatusFilterField statusFilter;

    // Keys for loading/saving
    static private final String charNameKey = "CharacterName";
    static private final String spellsKey = "Spells";
    static private final String spellNameKey = "SpellName";
    static private final String favoriteKey = "Favorite";
    static private final String preparedKey = "Prepared";
    static private final String knownKey = "Known";
    static private final String sort1Key = "SortField1";
    static private final String sort2Key = "SortField2";
    static private final String classFilterKey = "FilterClass";
    static private final String reverse1Key = "Reverse1";
    static private final String reverse2Key = "Reverse2";
    static private final String booksFilterKey = "BookFilters";
    static private final String statusFilterKey = "StatusFilter";

    private static HashMap<Sourcebook,Boolean> defaultFilterMap = new HashMap<Sourcebook,Boolean>() {{
        put(Sourcebook.PLAYERS_HANDBOOK, true);
        put(Sourcebook.XANATHARS_GTE, false);
        put(Sourcebook.SWORD_COAST_AG, false);
    }};


    CharacterProfile(String name, HashMap<String, SpellStatus> spellStatusesIn, SortField sf1, SortField sf2, CasterClass cc, boolean rev1, boolean rev2,  HashMap<Sourcebook, Boolean> bookFilters, StatusFilterField filter) {
        charName = name;
        spellStatuses = spellStatusesIn;
        sortField1 = sf1;
        sortField2 = sf2;
        filterClass = cc;
        reverse1 = rev1;
        reverse2 = rev2;
        filterByBooks = bookFilters;
        statusFilter = filter;
    }

    CharacterProfile(String name, HashMap<String, SpellStatus> spellStatusesIn) {
        this(name, spellStatusesIn, SortField.Name, SortField.Name, null, false, false, defaultFilterMap, StatusFilterField.All);
    }

    CharacterProfile(String nameIn) {
        this(nameIn, new HashMap<>());
    }

    // Getters
    String getName() { return charName; }
    HashMap<String, SpellStatus> getStatuses() { return spellStatuses; }
    SortField getFirstSortField() { return sortField1; }
    SortField getSecondSortField() { return sortField2; }
    CasterClass getFilterClass() { return filterClass; }
    boolean getFirstSortReverse() { return reverse1; }
    boolean getSecondSortReverse() { return reverse2; }
    boolean getSourcebookFilter(Sourcebook sb) { return filterByBooks.get(sb); }
    StatusFilterField getStatusFilter() { return statusFilter; }

    boolean filterFavorites() { return (statusFilter == StatusFilterField.Favorites); }
    boolean filterPrepared() { return (statusFilter == StatusFilterField.Prepared); }
    boolean filterKnown() { return (statusFilter == StatusFilterField.Known); }


    // Save to JSON
    JSONObject toJSON() throws JSONException {

        // The JSON object
        JSONObject json = new JSONObject();

        // Store the data
        json.put(charNameKey, charName);
        JSONArray spellStatusJA = new JSONArray();
        for (HashMap.Entry<String, SpellStatus> data : spellStatuses.entrySet()) {
            JSONObject statusJSON = new JSONObject();
            statusJSON.put(spellNameKey, data.getKey());
            SpellStatus status = data.getValue();
            statusJSON.put(favoriteKey, status.favorite);
            statusJSON.put(preparedKey, status.prepared);
            statusJSON.put(knownKey, status.known);
            spellStatusJA.put(statusJSON);
            System.out.println(statusJSON);
        }
        json.put(spellsKey, spellStatusJA);
        json.put(sort1Key, sortField1.name());
        json.put(sort2Key, sortField2.name());
        json.put(reverse1Key, reverse1);
        json.put(reverse2Key, reverse2);
        if (filterClass != null) {
            json.put(classFilterKey, filterClass.name());
        }

        JSONObject books = new JSONObject();
        for (Sourcebook sb : Sourcebook.values()) {
            books.put(sb.code(), filterByBooks.get(sb));
        }
        json.put(booksFilterKey, books);
        json.put(statusFilterKey, statusFilter.name());

        return json;
    }

    private boolean isProperty(Spell s, Function<SpellStatus,Boolean> property) {
        if (spellStatuses.containsKey(s.getName())) {
            SpellStatus status = spellStatuses.get(s.getName());
            return property.apply(status);
        }
        return false;
    }

    boolean isFavorite(Spell s) {
        return isProperty(s, (SpellStatus status) -> status.favorite);
    }

    boolean isPrepared(Spell s) {
        return isProperty(s, (SpellStatus status) -> status.prepared);
    }

    boolean isKnown(Spell s) {
        return isProperty(s, (SpellStatus status) -> status.known);
    }

    private void setProperty(Spell s, Boolean val, BiConsumer<SpellStatus,Boolean> propSetter) {
        String spellName = s.getName();
        if (spellStatuses.containsKey(spellName)) {
            SpellStatus status = spellStatuses.get(spellName);
            propSetter.accept(status, val);
            // spellStatuses.put(spellName, status);
            if (status.noneTrue()) { // We can remove the key if all three are false
                spellStatuses.remove(spellName);
            }
        } else if (val) { // If the key doesn't exist, we only need to modify if val is true
            SpellStatus status = new SpellStatus();
            propSetter.accept(status, val);
            spellStatuses.put(spellName, status);
        }
    }

    void setFavorite(Spell s, Boolean fav) {
        setProperty(s, fav, (SpellStatus status, Boolean tf) -> {status.favorite = tf;});
    }

    void setPrepared(Spell s, Boolean prep) {
        setProperty(s, prep, (SpellStatus status, Boolean tf) -> {status.prepared = tf;});
    }

    void setKnown(Spell s, Boolean known) {
        setProperty(s, known, (SpellStatus status, Boolean tf) -> {status.known = tf;});
    }

    boolean isStatusSet() { return ( filterFavorites() || filterKnown() || filterPrepared() ); }

    void setSourcebookFilter(Sourcebook sb, boolean tf) { filterByBooks.put(sb, tf); System.out.println("Setting " + sb.code() + " to " + tf); }
    void setStatusFilter(StatusFilterField sff) { statusFilter = sff; }
    void setFilterClass(CasterClass cc) { filterClass = cc; }
    void setFirstSortField(SortField sf) { sortField1 = sf; System.out.println("Changing sf1 to " + sortField1.name());}
    void setSecondSortField(SortField sf) { sortField2 = sf; }
    void setFirstSortReverse(boolean b) { reverse1 = b; }
    void setSecondSortReverse(boolean b) { reverse2 = b; }

    // Save to a file
    void save(File filename) {
        try {
            JSONObject cpJSON = toJSON();
            System.out.println("The character JSON is:");
            System.out.println(cpJSON.toString());
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
                bw.write(cpJSON.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // Load from JSON
    static CharacterProfile fromJSON(JSONObject json) throws JSONException {

        // The map
        HashMap<String, SpellStatus> spellStatusMap = new HashMap<>();

        String charName = json.getString(charNameKey);

        JSONArray jarr = json.getJSONArray(spellsKey);
        for (int i = 0; i < jarr.length(); ++i) {
            JSONObject jobj = jarr.getJSONObject(i);

            // Get the name and array of statuses
            String spellName = jobj.getString(spellNameKey);

            // Load the spell statuses
            boolean fav = jobj.getBoolean(favoriteKey);
            boolean prep = jobj.getBoolean(preparedKey);
            boolean known = jobj.getBoolean(knownKey);
            SpellStatus status = new SpellStatus(fav, prep, known);

            // Add to the map
            spellStatusMap.put(spellName, status);
        }

        // Get the first sort field, if present
        SortField sortField1 = json.has(sort1Key) ? SortField.fromName(json.getString(sort1Key)) : SortField.Name;

        // Get the second sort field, if present
        SortField sortField2 = json.has(sort2Key) ? SortField.fromName(json.getString(sort2Key)) : SortField.Name;

        // Get the class filter, if applicable
        CasterClass filterClass = json.has(classFilterKey) ? CasterClass.fromName(json.getString(classFilterKey)) : null;

        // Get the sort reverse variables
        boolean reverse1 = json.has(reverse1Key) && json.getBoolean(reverse1Key);
        boolean reverse2 = json.has(reverse2Key) && json.getBoolean(reverse2Key);

        // Get the sourcebook filter map
        HashMap<Sourcebook,Boolean> filterByBooks = new HashMap<>();

        // If the filter map is present
        if (json.has(booksFilterKey)) {
            System.out.println("Has books");
            JSONObject booksJSON = json.getJSONObject(booksFilterKey);
            for (Sourcebook sb : Sourcebook.values()) {
                if (booksJSON.has(sb.code())) {
                    System.out.println(sb + "\t" + booksJSON.getBoolean(sb.code()));
                    filterByBooks.put(sb, booksJSON.getBoolean(sb.code()));
                } else {
                    boolean b = (sb == Sourcebook.PLAYERS_HANDBOOK); // True if PHB, false otherwise
                    filterByBooks.put(sb, b);
                }
            }
            // If it's not, use the default
        } else {
            filterByBooks = defaultFilterMap;
        }

        // Get the status filter
        StatusFilterField statusFilter = json.has(statusFilterKey) ? StatusFilterField.fromName(json.getString(statusFilterKey)) : StatusFilterField.All;

        // Return the profile
        return new CharacterProfile(charName, spellStatusMap, sortField1, sortField2, filterClass, reverse1, reverse2, filterByBooks, statusFilter);

    }


}
