package dnd.jon.spellbook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.EnumMap;;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dnd.jon.spellbook.CastingTime.CastingTimeType;

public class CharacterProfile {

    // Member values
    private String charName;
    private HashMap<String,SpellStatus> spellStatuses;
    private SortField sortField1;
    private SortField sortField2;
    private boolean reverse1;
    private boolean reverse2;
    private StatusFilterField statusFilter;
    private int minSpellLevel;
    private int maxSpellLevel;
    private EnumMap<School, Boolean> schoolVisibilities;
    private EnumMap<Sourcebook,Boolean> filterByBooks;
    private EnumMap<CasterClass, Boolean> classVisibilities;
    private EnumMap<CastingTimeType, Boolean> castingTimeTypeVisibilities;

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
    static private final String hiddenCastersKey = "HiddenClasses";
    static private final String hiddenSchoolsKey = "HiddenSchools";
    static private final String hiddenCastingTimeTypesKey = "HiddenCastingTimeTypes";
    static private final String minSpellLevelKey = "MinSpellLevel";
    static private final String maxSpellLevelKey = "MaxSpellLevel";
    static private final String versionCodeKey = "VersionCode";

    private static EnumMap<Sourcebook,Boolean> defaultFilterMap = new EnumMap<>(Sourcebook.class);
    static {
        for (Sourcebook sourcebook : Sourcebook.values()) {
            defaultFilterMap.put(sourcebook, sourcebook == Sourcebook.PLAYERS_HANDBOOK);
        }
    }

    private static EnumMap<CasterClass, Boolean> defaultClassFilterMap = new EnumMap<>(CasterClass.class);
    static {
        for (CasterClass casterClass : CasterClass.values()) {
            defaultClassFilterMap.put(casterClass, true);
        }
    }

    private static EnumMap<School, Boolean> defaultSchoolFilterMap = new EnumMap<>(School.class);
    static {
        for (School school : School.values()) {
            defaultSchoolFilterMap.put(school, true);
        }
    }

    private static EnumMap<CastingTimeType, Boolean> defaultCastingTimeTypeFilterMap = new EnumMap<>(CastingTimeType.class);
    static {
        for (CastingTimeType ctt : CastingTimeType.values()) {
            defaultCastingTimeTypeFilterMap.put(ctt, true);
        }
    }



    CharacterProfile(String name, HashMap<String, SpellStatus> spellStatusesIn, SortField sf1, SortField sf2, EnumMap<CasterClass,Boolean> visibilities, boolean rev1, boolean rev2,  EnumMap<Sourcebook, Boolean> bookFilters, StatusFilterField filter, EnumMap<School, Boolean> schoolFilters, EnumMap<CastingTimeType, Boolean> castingTimeTypeFilters, int minLevel, int maxLevel) {
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
        castingTimeTypeVisibilities = castingTimeTypeFilters;
        minSpellLevel = minLevel;
        maxSpellLevel = maxLevel;
    }

    CharacterProfile(String name, HashMap<String, SpellStatus> spellStatusesIn) {
        this(name, spellStatusesIn, SortField.NAME, SortField.NAME, new EnumMap<>(defaultClassFilterMap), false, false, new EnumMap<>(defaultFilterMap), StatusFilterField.ALL, new EnumMap<>(defaultSchoolFilterMap), new EnumMap<>(defaultCastingTimeTypeFilterMap), Spellbook.MIN_SPELL_LEVEL, Spellbook.MAX_SPELL_LEVEL);
    }

    CharacterProfile(String nameIn) {
        this(nameIn, new HashMap<>());
    }

    // Basic getters
    String getName() { return charName; }
    HashMap<String, SpellStatus> getStatuses() { return spellStatuses; }
    SortField getFirstSortField() { return sortField1; }
    SortField getSecondSortField() { return sortField2; }
    boolean getFirstSortReverse() { return reverse1; }
    boolean getSecondSortReverse() { return reverse2; }
    int getMinSpellLevel() { return minSpellLevel; }
    int getMaxSpellLevel() { return maxSpellLevel; }
    StatusFilterField getStatusFilter() { return statusFilter; }

    // Get the visible values for the visibility enums
    // If we pass true, get the visible values
    // If we pass false, get the invisible ones
    // The generic function has an unchecked cast warning, but this won't ever be a problem
    private <E extends Enum<E>, T> T[] getVisibleEnums(EnumMap<E,Boolean> enumMap, boolean b, Class<T> resultType, Function<E,T> transform) {
        // The filter to use. Gives us XNOR of b and the entry value
        Predicate<EnumMap.Entry<E,Boolean>> filter = (entry) -> (b == entry.getValue());
        // The map. Get the key of the map entry, then apply the property
        Function<EnumMap.Entry<E,Boolean>,T> map = (entry) -> transform.apply(entry.getKey());
        IntFunction<T[]> generator = (int n) -> (T[]) Array.newInstance(resultType, n);
        return enumMap.entrySet().stream().filter(filter).map(map).toArray(generator);
    }

    // Version with no transform application
    private <E extends Enum<E>> E[] getVisibleEnums(EnumMap<E,Boolean> enumMap, boolean b, Class<E> resultType) { return getVisibleEnums(enumMap, b, resultType, (x) -> x); }
    Sourcebook[] getVisibleSourcebooks(boolean b) { return getVisibleEnums(filterByBooks, b, Sourcebook.class); }
    CasterClass[] getVisibleCasters(boolean b) { return getVisibleEnums(classVisibilities, b, CasterClass.class); }
    School[] getVisibleSchools(boolean b) { return getVisibleEnums(schoolVisibilities, b, School.class); }
    CastingTimeType[] getVisibleCastingTimeTypes(boolean b) { return getVisibleEnums(castingTimeTypeVisibilities, b, CastingTimeType.class); }

    // Specifically for names
    private <E extends Enum<E>> String[] getVisibleEnumNames(EnumMap<E,Boolean> enumMap, boolean b, Function<E,String> nameGetter) { return getVisibleEnums(enumMap, b, String.class, nameGetter); }
    String[] getVisibleSourcebookClassNames(boolean b) { return getVisibleEnumNames(filterByBooks, b, Sourcebook::getDisplayName); }
    String[] getVisibleCasterNames(boolean b) { return getVisibleEnumNames(classVisibilities, b, CasterClass::getDisplayName); }
    String[] getVisibleSchoolNames(boolean b) { return getVisibleEnumNames(schoolVisibilities, b, School::getDisplayName); }
    String[] getVisibleCastingTimeTypeNames(boolean b) { return getVisibleEnumNames(castingTimeTypeVisibilities, b, CastingTimeType::getDisplayName); }


    // Getting visibilities from the maps
    private <E extends Enum<E>> boolean getVisibility(E e, EnumMap<E, Boolean> enumMap) { return Util.coalesce(enumMap.get(e), false); }
    public boolean getSourcebookVisibility(Sourcebook sourcebook) { return getVisibility(sourcebook, filterByBooks); }
    public boolean getCasterVisibility(CasterClass casterClass) { return getVisibility(casterClass, classVisibilities); }
    public boolean getSchoolVisibility(School school) { return getVisibility(school, schoolVisibilities); }
    public boolean getCastingTimeTypeVisibility(CastingTimeType castingTimeType) { return getVisibility(castingTimeType, castingTimeTypeVisibilities); }


    // Checking whether a not a specific filter (or any filter) is set
    boolean filterFavorites() { return (statusFilter == StatusFilterField.FAVORITES); }
    boolean filterPrepared() { return (statusFilter == StatusFilterField.PREPARED); }
    boolean filterKnown() { return (statusFilter == StatusFilterField.KNOWN); }
    boolean isStatusSet() { return ( filterFavorites() || filterKnown() || filterPrepared() ); }


    // Check whether a given spell is on one of the spell lists
    // It's the same for each list, so the specific lists just call this general function
    private boolean isProperty(Spell s, Function<SpellStatus,Boolean> property) {
        if (spellStatuses.containsKey(s.getName())) {
            SpellStatus status = spellStatuses.get(s.getName());
            return property.apply(status);
        }
        return false;
    }

    boolean isFavorite(Spell spell) { return isProperty(spell, (SpellStatus status) -> status.favorite); }
    boolean isPrepared(Spell spell) { return isProperty(spell, (SpellStatus status) -> status.prepared); }
    boolean isKnown(Spell spell) { return isProperty(spell, (SpellStatus status) -> status.known); }


    // Setting whether a spell is on a given spell list
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
            propSetter.accept(status, true);
            spellStatuses.put(spellName, status);
        }
    }
    void setFavorite(Spell s, Boolean fav) { setProperty(s, fav, (SpellStatus status, Boolean tf) -> {status.favorite = tf;}); }
    void setPrepared(Spell s, Boolean prep) { setProperty(s, prep, (SpellStatus status, Boolean tf) -> {status.prepared = tf;}); }
    void setKnown(Spell s, Boolean known) { setProperty(s, known, (SpellStatus status, Boolean tf) -> {status.known = tf;}); }


    // Toggling whether a given property is set for a given spell
    private void toggleProperty(Spell s, Function<SpellStatus,Boolean> property, BiConsumer<SpellStatus,Boolean> propSetter) { setProperty(s, !isProperty(s, property), propSetter); }
    void toggleFavorite(Spell s) { toggleProperty(s, (SpellStatus status) -> status.favorite, (SpellStatus status, Boolean tf) -> {status.favorite = tf;}); }
    void togglePrepared(Spell s) { toggleProperty(s, (SpellStatus status) -> status.prepared, (SpellStatus status, Boolean tf) -> {status.prepared = tf;}); }
    void toggleKnown(Spell s) { toggleProperty(s, (SpellStatus status) -> status.known, (SpellStatus status, Boolean tf) -> {status.known = tf;}); }

    // Setting visibilities in the maps
    private <E extends Enum<E>> void setVisibility(E e, boolean tf, EnumMap<E,Boolean> enumMap) { enumMap.put(e, tf); }
    void setSourcebookVisibility(Sourcebook sourcebook, boolean tf) { setVisibility(sourcebook, tf, filterByBooks);}
    void setCasterVisibility(CasterClass casterClass, boolean tf) { setVisibility(casterClass, tf, classVisibilities); }
    void setSchoolVisibility(School school, boolean tf) { setVisibility(school, tf, schoolVisibilities); }
    void setCastingTimeTypeVisibility(CastingTime.CastingTimeType castingTimeType, boolean tf) { setVisibility(castingTimeType, tf, castingTimeTypeVisibilities); }

    // Toggling visibility in the maps
    private <E extends Enum<E>> void toggleVisibility( E e, EnumMap<E,Boolean> enumMap) { enumMap.put(e, !Util.coalesce(enumMap.get(e), false)); }
    void toggleCasterVisibility(CasterClass casterClass) { toggleVisibility(casterClass, classVisibilities); }
    void toggleSourcebookVisibility(Sourcebook sourcebook) { toggleVisibility(sourcebook, filterByBooks); }
    void toggleSchoolVisibility(School school) { toggleVisibility(school, schoolVisibilities); }
    void toggleCastingTimeTypeVisibility(CastingTime.CastingTimeType castingTimeType) { toggleVisibility(castingTimeType, castingTimeTypeVisibilities); }

    // Basic setters
    void setFirstSortField(SortField sf) { sortField1 = sf; }
    void setSecondSortField(SortField sf) { sortField2 = sf; }
    void setFirstSortReverse(boolean b) { reverse1 = b; }
    void setSecondSortReverse(boolean b) { reverse2 = b; }
    void setMinSpellLevel(int level) { minSpellLevel = level; }
    void setMaxSpellLevel(int level) { maxSpellLevel = level; }
    void setStatusFilter(StatusFilterField sff) { statusFilter = sff; }

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

    // Create a JSON object representing the profile
    // This is what we for saving
    // We can reconstruct the profile using fromJSON
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

        System.out.println("CastersArray");
        JSONArray castersArray = new JSONArray(getVisibleCasterNames(false));
        json.put(hiddenCastersKey, castersArray);

        System.out.println("SchoolsArray");
        JSONArray schoolsArray = new JSONArray(getVisibleSchoolNames(false));
        json.put(hiddenSchoolsKey, schoolsArray);

        JSONArray castingTimeTypesArray = new JSONArray(getVisibleCastingTimeTypeNames(false));
        json.put(hiddenCastingTimeTypesKey, castingTimeTypesArray);

        JSONObject books = new JSONObject();
        for (Sourcebook sb : Sourcebook.values()) {
            books.put(sb.getCode(), filterByBooks.get(sb));
        }
        json.put(booksFilterKey, books);
        json.put(statusFilterKey, statusFilter.getDisplayName());

        json.put(minSpellLevelKey, minSpellLevel);
        json.put(maxSpellLevelKey, maxSpellLevel);

        json.put(versionCodeKey, GlobalInfo.VERSION_CODE);

        return json;
    }


    // Construct a profile from a JSON object
    // Basically the inverse to toJSON

    static CharacterProfile fromJSON(JSONObject json) throws JSONException {
        return fromJSONNew(json);
    }


    static private CharacterProfile fromJSONNew(JSONObject json) throws JSONException {

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
        EnumMap<CasterClass, Boolean> classesMap = new EnumMap<>(defaultClassFilterMap);
        if (json.has(hiddenCastersKey)) {
            JSONArray classesArray = json.getJSONArray(hiddenCastersKey);
            for (int i = 0; i < classesArray.length(); ++i) {
                String className = classesArray.getString(i);
                CasterClass casterClass = CasterClass.fromDisplayName(className);
                classesMap.put(casterClass, false);
            }
        }

        // Get the hidden schools
        EnumMap<School, Boolean> schoolsMap = new EnumMap<>(defaultSchoolFilterMap);
        if (json.has(hiddenSchoolsKey)) {
            JSONArray schoolsArray = json.getJSONArray(hiddenSchoolsKey);
            for (int i = 0; i < schoolsArray.length(); ++i) {
                String schoolName = schoolsArray.getString(i);
                School school = School.fromDisplayName(schoolName);
                schoolsMap.put(school, false);
            }
        }

        // Get the hidden casting time types
        EnumMap<CastingTimeType, Boolean> castingTimeTypesMap = new EnumMap<>(defaultCastingTimeTypeFilterMap);
        if (json.has(hiddenCastingTimeTypesKey)) {
            JSONArray castingTimeTypesArray = json.getJSONArray(hiddenCastingTimeTypesKey);
            for (int i = 0; i < castingTimeTypesArray.length(); ++i) {
                String castingTimeTypeName = castingTimeTypesArray.getString(i);
                CastingTimeType castingTimeType = CastingTimeType.fromDisplayName(castingTimeTypeName);
                castingTimeTypesMap.put(castingTimeType, false);
            }
        }

        // Get the sort reverse variables
        final boolean reverse1 = json.optBoolean(reverse1Key, false);
        final boolean reverse2 = json.optBoolean(reverse2Key, false);

        // Get the min and max spell levels
        final int minLevel = json.optInt(minSpellLevelKey, Spellbook.MIN_SPELL_LEVEL);
        final int maxLevel = json.optInt(maxSpellLevelKey, Spellbook.MAX_SPELL_LEVEL);

        // Get the sourcebook filter map
        EnumMap<Sourcebook,Boolean> filterByBooks = new EnumMap<>(defaultFilterMap);

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
        return new CharacterProfile(charName, spellStatusMap, sortField1, sortField2, classesMap, reverse1, reverse2, filterByBooks, statusFilter, schoolsMap, castingTimeTypesMap, minLevel, maxLevel);

    }


}
