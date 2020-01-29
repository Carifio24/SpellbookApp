package dnd.jon.spellbook;

import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.javatuples.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;


import dnd.jon.spellbook.CastingTime.CastingTimeType;
import dnd.jon.spellbook.Duration.DurationType;
import dnd.jon.spellbook.Range.RangeType;

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
    private HashMap<Class<? extends Enum<?>>, EnumMap<? extends Enum<?>, Boolean>> visibilitiesMap;

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
    static private final String hiddenDurationTypesKey = "HiddenDurationTypes";
    static private final String hiddenRangeTypesKey = "HiddenRangeTypes";
    static private final String minSpellLevelKey = "MinSpellLevel";
    static private final String maxSpellLevelKey = "MaxSpellLevel";
    static private final String versionCodeKey = "VersionCode";

    // Not currently needed
    // This function is the generic version of the map-creation piece of (wildcard-based) instantiation of the default visibilities map
    private static <E extends Enum<E>> EnumMap<E,Boolean> defaultFilterMap(Class<E> enumType, Function<E,Boolean> filter) {
        final EnumMap<E,Boolean> enumMap = new EnumMap<>(enumType);
        final E[] enumValues = enumType.getEnumConstants();
        if (enumValues == null) { return enumMap; }
        for (E e : enumValues) {
            enumMap.put(e, filter.apply(e));
        }
        return enumMap;
    }

    private static final HashMap<Class<? extends Enum<?>>, Pair<Function<Object,Boolean>, String>> enumInfo = new HashMap<Class<? extends Enum<?>>, Pair<Function<Object,Boolean>,String>>() {{
       put(Sourcebook.class, new Pair<>((sb) -> sb == Sourcebook.PLAYERS_HANDBOOK, "HiddenSourcebooks"));
       put(CasterClass.class, new Pair<>((x) -> true, "HiddenCasters"));
       put(School.class, new Pair<>((x) -> true, "HiddenSchools"));
       put(CastingTimeType.class, new Pair<>((x) -> true, "HiddenCastingTimeTypes"));
       put(DurationType.class, new Pair<>((x) -> true, "HiddenDurationTypes"));
       put(RangeType.class, new Pair<>((x) -> true, "HiddenRangeTypes"));
    }};


    // There are some warnings about unchecked assignments and calls here, but it's fine the way it's being used
    // This creates the default visibilities map based on our filters
    // It's a bit hacky, and relies on the filters accepting any Object
    @SuppressWarnings("unchecked")
    private static HashMap<Class<? extends Enum<?>>, EnumMap<? extends Enum<?>, Boolean>> defaultVisibilitiesMap = new HashMap<>();
    static {
        for (HashMap.Entry<Class<? extends Enum<?>>, Pair<Function<Object,Boolean>, String>>  entry: enumInfo.entrySet()) {
            Class<? extends Enum<?>> enumType = entry.getKey();
            Function<Object, Boolean> filter = entry.getValue().getValue0();
            EnumMap enumMap = new EnumMap(enumType);
            if (enumType.getEnumConstants() != null) {
                for (int i = 0; i < enumType.getEnumConstants().length; ++i) {
                    enumMap.put(enumType.getEnumConstants()[i], filter.apply(enumType.getEnumConstants()[i]));
                }
            }
            defaultVisibilitiesMap.put(enumType, enumMap);
        }
    }

    private CharacterProfile(String name, HashMap<String, SpellStatus> spellStatusesIn, SortField sf1, SortField sf2,  HashMap<Class<? extends Enum<?>>, EnumMap<? extends Enum<?>, Boolean>> visibilities, boolean rev1, boolean rev2, StatusFilterField filter, int minLevel, int maxLevel) {
        charName = name;
        spellStatuses = spellStatusesIn;
        sortField1 = sf1;
        sortField2 = sf2;
        visibilitiesMap = visibilities;
        reverse1 = rev1;
        reverse2 = rev2;
        statusFilter = filter;
        minSpellLevel = minLevel;
        maxSpellLevel = maxLevel;

    }

    private CharacterProfile(String name, HashMap<String, SpellStatus> spellStatusesIn) {
        this(name, spellStatusesIn, SortField.NAME, SortField.NAME, new HashMap<>(defaultVisibilitiesMap), false, false, StatusFilterField.ALL, Spellbook.MIN_SPELL_LEVEL, Spellbook.MAX_SPELL_LEVEL);
    }

    CharacterProfile(String nameIn) { this(nameIn, new HashMap<>()); }

    // Basic getters
    String getName() { return charName; }
    HashMap<String, SpellStatus> getStatuses() { return spellStatuses; }
    SortField getFirstSortField() { return sortField1; }
    SortField getSecondSortField() { return sortField2; }
    boolean getFirstSortReverse() { return reverse1; }
    boolean getSecondSortReverse() { return reverse2; }
    public int getMinSpellLevel() { return minSpellLevel; }
    public int getMaxSpellLevel() { return maxSpellLevel; }
    StatusFilterField getStatusFilter() { return statusFilter; }

    // Get the visible values for the visibility enums
    // If we pass true, get the visible values
    // If we pass false, get the invisible ones
    // The generic function has an unchecked cast warning, but this won't ever be a problem
    @SuppressWarnings("unchecked")
    private <E extends Enum<E>, T> T[] getVisibleEnums(Class<E> enumType, boolean b,  Class<T> resultType, Function<E,T> transform) {
        // The enumMap
        EnumMap<E, Boolean> enumMap = (EnumMap<E, Boolean>) visibilitiesMap.get(enumType);
        // The filter to use. Gives us XNOR of b and the entry value
        Predicate<EnumMap.Entry<E,Boolean>> filter = (entry) -> (b == entry.getValue());
        // The map. Get the key of the map entry, then apply the property
        Function<EnumMap.Entry<E,Boolean>,T> map = (entry) -> transform.apply(entry.getKey());
        IntFunction<T[]> generator = (int n) -> (T[]) Array.newInstance(resultType, n);
        return enumMap.entrySet().stream().filter(filter).map(map).toArray(generator);
    }

    // Version with no transform application
    private <E extends Enum<E>> E[] getVisibleEnums(Class<E> enumType, boolean b, Class<E> resultType) { return getVisibleEnums(enumType, b, resultType, (x) -> x); }

    // Specifically for names
    private <E extends Enum<E> & NameDisplayable> String[] getVisibleEnumNames(Class<E> enumType, boolean b) { return getVisibleEnums(enumType, b, String.class, E::getDisplayName); }

    // Getting the visibility of the spanning type
    boolean getSpanningTypeVisibility(Class<QuantityType> quantityType) {
        try {
            final QuantityType[] enums = quantityType.getEnumConstants();
            if (enums == null) { return false; }
            final Enum e = (Enum) enums[0];
            return getVisibility(e);
        } catch (NullPointerException e) {
            return false;
        }
    }

    // For databinding
    public int getSpanningTypeVisible(Class<QuantityType> quantityType) {
        return getSpanningTypeVisibility(quantityType) ? View.VISIBLE : View.GONE;
    }

    // This is the general function that the generated ItemFilterViewBinding class will call
    // We use getClass to get the correct map
    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> boolean getVisibility(E e) {
        Class<?> cls = e.getClass();
        EnumMap<E,Boolean> map = (EnumMap<E,Boolean>) visibilitiesMap.get(cls);
        if (map == null) { return false; }
        return SpellbookUtils.coalesce(map.get(e), false);
    }


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
    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> void setVisibility(E e, boolean tf) {
        Class<?> type = e.getClass();
        try {
            EnumMap<E, Boolean> enumMap = (EnumMap<E, Boolean>) visibilitiesMap.get(type);
            enumMap.put(e, !tf);
        } catch (NullPointerException npe) {
            return; // If we hit a null somewhere, do nothing
        }
    }

    // Toggling visibility in the maps
    private <E extends Enum<E>> void toggleVisibility(E e) {
        setVisibility(e, !getVisibility(e));
    }

    // Basic setters
    void setFirstSortField(SortField sf) { sortField1 = sf; }
    void setSecondSortField(SortField sf) { sortField2 = sf; }
    void setSortField(SortField sf, int level) {
        switch (level) {
            case 1:
                sortField1 = sf;
            case 2:
                sortField2 = sf;
        }
    }
    void setFirstSortReverse(boolean b) { reverse1 = b; }
    void setSecondSortReverse(boolean b) { reverse2 = b; }
    void setSortReverse(boolean b, int level) {
        switch (level) {
            case 1:
                reverse1 = b;
            case 2:
                reverse2 = b;
        }
    }
    void setMinSpellLevel(int level) { minSpellLevel = level; }
    void setMaxSpellLevel(int level) { maxSpellLevel = level; }
    void setStatusFilter(StatusFilterField sff) { statusFilter = sff; }

    // Constructing a map from a list of hidden values
    // Used for JSON decoding
    @SuppressWarnings("unchecked")
    private static EnumMap<?,Boolean> mapFromHiddenNames(EnumMap<? extends Enum<?>,Boolean> defaultMap, JSONObject json, String key, Method constructorFromName) throws JSONException, IllegalAccessException, InvocationTargetException {
        final EnumMap map = new EnumMap<>(defaultMap);
        if (json.has(key)) {
            final JSONArray jsonArray = json.getJSONArray(key);
            for (int i = 0; i < jsonArray.length(); ++i) {
                final String name = jsonArray.getString(i);
                final Enum<?> value = (Enum<?>) constructorFromName.invoke(name);
                map.put(value, false);
            }
        }
        return map;
    }

    // Save to a file
    void save(File filename) {
        try {
            final JSONObject cpJSON = toJSON();
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
        final JSONObject json = new JSONObject();

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

        // Put in the arrays of hidden enums
        for (HashMap.Entry<Class<? extends Enum<?>>, Pair<Function<Object,Boolean>, String>> entry : enumInfo.entrySet()) {
            final Class cls = entry.getKey();
            final String key = entry.getValue().getValue1();
            final JSONArray jsonArray = new JSONArray(getVisibleEnumNames(cls, false));
            json.put(key, jsonArray);
        }

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

        // Create the visibility maps
        HashMap<Class<? extends Enum<?>>, EnumMap<? extends Enum<?>, Boolean>> visibilitiesMap = new HashMap<>();
        for (HashMap.Entry<Class<? extends Enum<?>>, Pair<Function<Object,Boolean>, String>> entry : enumInfo.entrySet()) {
            final Class<? extends Enum<?>> cls = entry.getKey();
            final String key = entry.getValue().getValue1();
            EnumMap<? extends Enum<?>, Boolean> defaultMap = defaultVisibilitiesMap.get(cls);
            try {
                Method constructorFromName = cls.getMethod("fromDisplayName", String.class);
                EnumMap<? extends Enum<?>, Boolean> map = mapFromHiddenNames(defaultMap, json, key, constructorFromName);
                visibilitiesMap.put(cls, map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // Get the sort reverse variables
        final boolean reverse1 = json.optBoolean(reverse1Key, false);
        final boolean reverse2 = json.optBoolean(reverse2Key, false);

        // Get the min and max spell levels
        final int minLevel = json.optInt(minSpellLevelKey, Spellbook.MIN_SPELL_LEVEL);
        final int maxLevel = json.optInt(maxSpellLevelKey, Spellbook.MAX_SPELL_LEVEL);


        // Get the status filter
        StatusFilterField statusFilter = json.has(statusFilterKey) ? StatusFilterField.fromDisplayName(json.getString(statusFilterKey)) : StatusFilterField.ALL;

        // Return the profile
        return new CharacterProfile(charName, spellStatusMap, sortField1, sortField2, visibilitiesMap, reverse1, reverse2, statusFilter, minLevel, maxLevel);

    }


}
