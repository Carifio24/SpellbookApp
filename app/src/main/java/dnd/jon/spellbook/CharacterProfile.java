package dnd.jon.spellbook;

import androidx.databinding.InverseMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CharacterProfile {

    // Member values
    private String charName;
    private HashMap<String,SpellStatus> spellStatuses;
    private SortField sortField1;
    private SortField sortField2;
    private HashMap<CasterClass, Boolean> classVisibilities;
    private boolean reverse1;
    private boolean reverse2;
    private HashMap<Sourcebook,Boolean> filterByBooks;
    private StatusFilterField statusFilter;
    private HashMap<School, Boolean> schoolVisibilities;
    private int minSpellLevel;
    private int maxSpellLevel;

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
    static private final String hiddenClassesKey = "HiddenClasses";
    static private final String hiddenSchoolsKey = "HiddenSchools";
    static private final String minSpellLevelKey = "MinSpellLevel";
    static private final String maxSpellLevelKey = "MaxSpellLevel";

    private static HashMap<Sourcebook,Boolean> defaultFilterMap = new HashMap<>();
    static {
        for (Sourcebook sourcebook : Sourcebook.values()) {
            defaultFilterMap.put(sourcebook, sourcebook == Sourcebook.PLAYERS_HANDBOOK);
        }
    }

    private static HashMap<CasterClass, Boolean> defaultClassFilterMap = new HashMap<>();
    static {
        for (CasterClass casterClass : CasterClass.values()) {
            defaultClassFilterMap.put(casterClass, true);
        }
    }

    private static HashMap<School, Boolean> defaultSchoolFilterMap = new HashMap<>();
    static {
        for (School school : School.values()) {
            defaultSchoolFilterMap.put(school, true);
        }
    }


    CharacterProfile(String name, HashMap<String, SpellStatus> spellStatusesIn, SortField sf1, SortField sf2, HashMap<CasterClass,Boolean> visibilities, boolean rev1, boolean rev2,  HashMap<Sourcebook, Boolean> bookFilters, StatusFilterField filter, HashMap<School, Boolean> schoolFilters, int minLevel, int maxLevel) {
        charName = name;
        spellStatuses = spellStatusesIn;
        sortField1 = sf1;
        sortField2 = sf2;
        classVisibilities = visibilities;
        reverse1 = rev1;
        reverse2 = rev2;
        filterByBooks = bookFilters;
        statusFilter = filter;
        schoolVisibilities = schoolFilters;
        minSpellLevel = minLevel;
        maxSpellLevel = maxLevel;
    }

    CharacterProfile(String name, HashMap<String, SpellStatus> spellStatusesIn) {
        this(name, spellStatusesIn, SortField.NAME, SortField.NAME, new HashMap<>(defaultClassFilterMap), false, false, new HashMap<>(defaultFilterMap), StatusFilterField.ALL, new HashMap<>(defaultSchoolFilterMap), Spellbook.MIN_SPELL_LEVEL, Spellbook.MAX_SPELL_LEVEL);
    }

    CharacterProfile(String nameIn) {
        this(nameIn, new HashMap<>());
    }

    // Getters
    String getName() { return charName; }
    HashMap<String, SpellStatus> getStatuses() { return spellStatuses; }
    SortField getFirstSortField() { return sortField1; }
    SortField getSecondSortField() { return sortField2; }
    boolean getFirstSortReverse() { return reverse1; }
    boolean getSecondSortReverse() { return reverse2; }
    public boolean getSourcebookFilter(Sourcebook sourcebook) { return filterByBooks.get(sourcebook); }
    StatusFilterField getStatusFilter() { return statusFilter; }
    public boolean getCasterFilter(CasterClass casterClass) { return classVisibilities.get(casterClass); }
    public boolean getSchoolFilter(School school) { return schoolVisibilities.get(school); }
    int getMinSpellLevel() { return minSpellLevel; }
    int getMaxSpellLevel() { return maxSpellLevel; }
    CasterClass[] getVisibleClasses() { return classVisibilities.entrySet().stream().filter(HashMap.Entry::getValue).map(HashMap.Entry::getKey).toArray(CasterClass[]::new); }
    School[] getVisibleSchools() { return schoolVisibilities.entrySet().stream().filter(HashMap.Entry::getValue).map(HashMap.Entry::getKey).toArray(School[]::new); }

    boolean filterFavorites() { return (statusFilter == StatusFilterField.FAVORITES); }
    boolean filterPrepared() { return (statusFilter == StatusFilterField.PREPARED); }
    boolean filterKnown() { return (statusFilter == StatusFilterField.KNOWN); }

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
        }
        json.put(spellsKey, spellStatusJA);

        json.put(sort1Key, sortField1.getDisplayName());
        json.put(sort2Key, sortField2.getDisplayName());
        json.put(reverse1Key, reverse1);
        json.put(reverse2Key, reverse2);

        JSONArray classesArray = new JSONArray(classVisibilities.entrySet().stream().filter(HashMap.Entry::getValue).map(HashMap.Entry::getKey).toArray(CasterClass[]::new));
        json.put(hiddenClassesKey, classesArray);

        JSONArray schoolsArray = new JSONArray(schoolVisibilities.entrySet().stream().filter(HashMap.Entry::getValue).map(HashMap.Entry::getKey).toArray(School[]::new));
        json.put(hiddenSchoolsKey, schoolsArray);

        JSONObject books = new JSONObject();
        for (Sourcebook sb : Sourcebook.values()) {
            books.put(sb.getCode(), filterByBooks.get(sb));
        }
        json.put(booksFilterKey, books);
        json.put(statusFilterKey, statusFilter.getDisplayName());

        json.put(minSpellLevelKey, minSpellLevel);
        json.put(maxSpellLevelKey, maxSpellLevel);

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


    private void toggleProperty(Spell s, Function<SpellStatus,Boolean> property, BiConsumer<SpellStatus,Boolean> propSetter) {
        setProperty(s, !isProperty(s, property), propSetter);
    }

    void toggleFavorite(Spell s) {
        toggleProperty(s, (SpellStatus status) -> status.favorite, (SpellStatus status, Boolean tf) -> {status.favorite = tf;});
    }

    void togglePrepared(Spell s) {
        toggleProperty(s, (SpellStatus status) -> status.prepared, (SpellStatus status, Boolean tf) -> {status.prepared = tf;});
    }

    void toggleKnown(Spell s) {
        toggleProperty(s, (SpellStatus status) -> status.known, (SpellStatus status, Boolean tf) -> {status.known = tf;});
    }


    boolean isStatusSet() { return ( filterFavorites() || filterKnown() || filterPrepared() ); }
    void setSourcebookFilter(Sourcebook sb, boolean tf) { filterByBooks.put(sb, tf); }
    void setStatusFilter(StatusFilterField sff) { statusFilter = sff; }
    public void setCasterVisibility(CasterClass casterClass, boolean tf) { classVisibilities.put(casterClass, tf); }

    void toggleCasterVisibility(CasterClass cc) {
        classVisibilities.put(cc, !classVisibilities.get(cc));
    }
    void toggleSourcebookVisibility(Sourcebook sb) {
        filterByBooks.put(sb, !filterByBooks.get(sb));
    }
    void toggleSchoolVisibility(School school) {
        schoolVisibilities.put(school, !schoolVisibilities.get(school));
    }
    void setFirstSortField(SortField sf) { sortField1 = sf; }
    void setSecondSortField(SortField sf) { sortField2 = sf; }
    void setFirstSortReverse(boolean b) { reverse1 = b; }
    void setSecondSortReverse(boolean b) { reverse2 = b; }
    void setMinSpellLevel(int level) { minSpellLevel = level; }
    void setMaxSpellLevel(int level) { maxSpellLevel = level; }

    // Save to a file
    void save(File filename) {
        try {
            JSONObject cpJSON = toJSON();
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

        String charName = json.getString(charNameKey);

        // Get the spell map, assuming it exists
        // If it doesn't, we just get an empty map
        HashMap<String, SpellStatus> spellStatusMap = new HashMap<>();
        if (json.has(spellsKey)) {
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
        }

        // Get the first sort field, if present
        SortField sortField1 = json.has(sort1Key) ? SortField.fromDisplayName(json.getString(sort1Key)) : SortField.NAME;

        // Get the second sort field, if present
        SortField sortField2 = json.has(sort2Key) ? SortField.fromDisplayName(json.getString(sort2Key)) : SortField.NAME;

        // Get the hidden caster classes
        HashMap<CasterClass, Boolean> classesMap = new HashMap<>(defaultClassFilterMap);
        if (json.has(hiddenClassesKey)) {
            JSONArray classesArray = json.getJSONArray(hiddenClassesKey);
            for (int i = 0; i < classesArray.length(); ++i) {
                String className = classesArray.getString(i);
                CasterClass casterClass = CasterClass.fromDisplayName(className);
                classesMap.put(casterClass, false);
            }
        }

        // Get the hidden schools
        HashMap<School, Boolean> schoolsMap = new HashMap<>(defaultSchoolFilterMap);
        if (json.has(hiddenSchoolsKey)) {
            JSONArray schoolsArray = json.getJSONArray(hiddenSchoolsKey);
            for (int i = 0; i < schoolsArray.length(); ++i) {
                String schoolName = schoolsArray.getString(i);
                School school = School.fromDisplayName(schoolName);
                schoolsMap.put(school, false);
            }
        }

        // Get the sort reverse variables
        final boolean reverse1 = json.optBoolean(reverse1Key, false);
        final boolean reverse2 = json.optBoolean(reverse2Key, false);

        // Get the min and max spell levels
        final int minLevel = json.optInt(minSpellLevelKey, Spellbook.MIN_SPELL_LEVEL);
        final int maxLevel = json.optInt(maxSpellLevelKey, Spellbook.MAX_SPELL_LEVEL);

        // Get the sourcebook filter map
        HashMap<Sourcebook,Boolean> filterByBooks = new HashMap<>(defaultFilterMap);

        // If the filter map is present
        if (json.has(booksFilterKey)) {
            JSONObject booksJSON = json.getJSONObject(booksFilterKey);
            for (Sourcebook sb : Sourcebook.values()) {
                if (booksJSON.has(sb.getCode())) {
                    filterByBooks.put(sb, booksJSON.getBoolean(sb.getCode()));
                } else {
                    final boolean b = (sb == Sourcebook.PLAYERS_HANDBOOK); // True if PHB, false otherwise
                    filterByBooks.put(sb, b);
                }
            }
        }

        // Get the status filter
        StatusFilterField statusFilter = json.has(statusFilterKey) ? StatusFilterField.fromDisplayName(json.getString(statusFilterKey)) : StatusFilterField.ALL;

        // Return the profile
        return new CharacterProfile(charName, spellStatusMap, sortField1, sortField2, classesMap, reverse1, reverse2, filterByBooks, statusFilter, schoolsMap, minLevel, maxLevel);

    }


}
