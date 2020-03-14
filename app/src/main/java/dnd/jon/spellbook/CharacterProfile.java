package dnd.jon.spellbook;

import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.javatuples.Quartet;
import org.javatuples.Sextet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;


import dnd.jon.spellbook.CastingTime.CastingTimeType;
import dnd.jon.spellbook.Duration.DurationType;
import dnd.jon.spellbook.Range.RangeType;

import org.apache.commons.lang3.SerializationUtils;

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
    private boolean ritualFilter;
    private boolean notRitualFilter;
    private boolean concentrationFilter;
    private boolean notConcentrationFilter;
    private HashMap<Class<? extends Enum<?>>, EnumMap<? extends Enum<?>, Boolean>> visibilitiesMap;
    private HashMap<Class<? extends QuantityType>, Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer>> quantityRangeFiltersMap;

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
    static private final String quantityRangesKey = "QuantityRanges";
    static private final String ritualKey = "Ritual";
    static private final String notRitualKey = "NotRitual";
    static private final String concentrationKey = "Concentration";
    static private final String notConcentrationKey = "NotConcentration";
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

    private static final HashMap<Class<? extends Enum<?>>, Quartet<Boolean,Function<Object,Boolean>, String, String>> enumInfo = new HashMap<Class<? extends Enum<?>>, Quartet<Boolean,Function<Object,Boolean>,String,String>>() {{
       put(Sourcebook.class, new Quartet<>(true, (sb) -> sb == Sourcebook.PLAYERS_HANDBOOK, "HiddenSourcebooks",""));
       put(CasterClass.class, new Quartet<>(false, (x) -> true, "HiddenCasters", ""));
       put(School.class, new Quartet<>(false, (x) -> true, "HiddenSchools", ""));
       put(CastingTimeType.class, new Quartet<>(false, (x) -> true, "HiddenCastingTimeTypes", "CastingTimeFilters"));
       put(DurationType.class, new Quartet<>(false, (x) -> true, "HiddenDurationTypes", "DurationFilters"));
       put(RangeType.class, new Quartet<>(false, (x) -> true, "HiddenRangeTypes", "RangeFilters"));
    }};
    private static final HashMap<String, Class<? extends QuantityType>> keyToQuantityTypeMap = new HashMap<>();
    static {
        for (Class<? extends Enum<?>> cls : enumInfo.keySet()) {
            if (QuantityType.class.isAssignableFrom(cls)) {
                final Class<? extends QuantityType> quantityType = (Class<? extends QuantityType>) cls;
                keyToQuantityTypeMap.put(enumInfo.get(cls).getValue3(), quantityType);
            }
        }
    }

    private static final HashMap<Class<? extends QuantityType>, Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer>> defaultQuantityRangeFiltersMap = new HashMap<Class<? extends QuantityType>, Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer>>() {{
       put(CastingTimeType.class, new Sextet<>(CastingTime.class, TimeUnit.class, TimeUnit.SECOND, TimeUnit.HOUR, 0, 24));
       put(DurationType.class, new Sextet<>(Duration.class, TimeUnit.class, TimeUnit.SECOND, TimeUnit.DAY, 0, 30));
       put(RangeType.class, new Sextet<>(Range.class, LengthUnit.class, LengthUnit.FOOT, LengthUnit.MILE, 0, 1));
    }};
    private static final String[] rangeFilterKeys = { "MinUnit", "MaxUnit", "MinText", "MaxText" };


    // There are some warnings about unchecked assignments and calls here, but it's fine the way it's being used
    // This creates the default visibilities map based on our filters
    // It's a bit hacky, and relies on the filters accepting any Object
    private static final HashMap<Class<? extends Enum<?>>, EnumMap<? extends Enum<?>, Boolean>> defaultVisibilitiesMap = new HashMap<>();
    static {
        for (HashMap.Entry<Class<? extends Enum<?>>, Quartet<Boolean, Function<Object,Boolean>, String, String>>  entry: enumInfo.entrySet()) {
            final Class<? extends Enum<?>> enumType = entry.getKey();
            final Function<Object, Boolean> filter = entry.getValue().getValue1();
            final EnumMap enumMap = new EnumMap(enumType);
            if (enumType.getEnumConstants() != null)
            {
                for (int i = 0; i < enumType.getEnumConstants().length; ++i) {
                    enumMap.put(enumType.getEnumConstants()[i], filter.apply(enumType.getEnumConstants()[i]));
                }
            }
            defaultVisibilitiesMap.put(enumType, enumMap);
        }
    }

    private CharacterProfile(String name, HashMap<String, SpellStatus> spellStatusesIn, SortField sf1, SortField sf2,  HashMap<Class<? extends Enum<?>>, EnumMap<? extends Enum<?>, Boolean>> visibilities, HashMap<Class<? extends QuantityType>, Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer>> rangeFilters, boolean rev1, boolean rev2, StatusFilterField filter, boolean ritualStatus, boolean notRitualStatus, boolean concentrationStatus, boolean notConcentrationStatus, int minLevel, int maxLevel) {
        charName = name;
        spellStatuses = spellStatusesIn;
        sortField1 = sf1;
        sortField2 = sf2;
        visibilitiesMap = visibilities;
        quantityRangeFiltersMap = rangeFilters;
        reverse1 = rev1;
        reverse2 = rev2;
        statusFilter = filter;
        minSpellLevel = minLevel;
        maxSpellLevel = maxLevel;
        ritualFilter = ritualStatus;
        notRitualFilter = notRitualStatus;
        concentrationFilter = concentrationStatus;
        notConcentrationFilter = notConcentrationStatus;
    }

    private CharacterProfile(String name, HashMap<String, SpellStatus> spellStatusesIn) {
        this(name, spellStatusesIn, SortField.NAME, SortField.NAME, SerializationUtils.clone(defaultVisibilitiesMap), SerializationUtils.clone(defaultQuantityRangeFiltersMap), false, false, StatusFilterField.ALL, true, true, true, true, Spellbook.MIN_SPELL_LEVEL, Spellbook.MAX_SPELL_LEVEL);
    }

    CharacterProfile(String nameIn) { this(nameIn, new HashMap<>()); }

    // Basic getters
    String getName() { return charName; }
    HashMap<String, SpellStatus> getStatuses() { return spellStatuses; }
    SortField getFirstSortField() { return sortField1; }
    SortField getSecondSortField() { return sortField2; }
    boolean getFirstSortReverse() { return reverse1; }
    boolean getSecondSortReverse() { return reverse2; }
    boolean getRitualFilter(boolean b) { return b ? ritualFilter : notRitualFilter; }
    boolean getConcentrationFilter(boolean b) { return b ? concentrationFilter : notConcentrationFilter; }
    public int getMinSpellLevel() { return minSpellLevel; }
    public int getMaxSpellLevel() { return maxSpellLevel; }
    StatusFilterField getStatusFilter() { return statusFilter; }

    // Get the visible values for the visibility enums
    // If we pass true, get the visible values
    // If we pass false, get the invisible ones
    // The generic function has an unchecked cast warning, but this won't ever be a problem
    @SuppressWarnings("unchecked")
     private <E extends Enum<E>, T> T[] getVisibleValues(Class<E> enumType, boolean b,  Class<T> resultType, Function<E,T> transform) {
        // The enumMap
        final EnumMap<E, Boolean> enumMap = (EnumMap<E, Boolean>) visibilitiesMap.get(enumType);
        // The filter to use. Gives us XNOR of b and the entry value
        final Predicate<EnumMap.Entry<E,Boolean>> filter = (entry) -> (b == entry.getValue());
        // The map. Get the key of the map entry, then apply the property
        final Function<EnumMap.Entry<E,Boolean>,T> map = (entry) -> transform.apply(entry.getKey());
        final IntFunction<T[]> generator = (int n) -> (T[]) Array.newInstance(resultType, n);
        return enumMap.entrySet().stream().filter(filter).map(map).toArray(generator);
    }

//    private <T> T[] getVisibleValues2(Class<? extends Enum<?>> enumType, boolean b, Class<T> resultType, Function<Enum<?>,T> transform) {
//        // The enumMap
//        final EnumMap<? extends Enum<?>, Boolean> enumMap = visibilitiesMap.get(enumType);
//        // The filter to use. Gives us XNOR of b and the entry value
//        final Predicate<EnumMap.Entry<? extends Enum<?>,Boolean>> filter = (entry) -> (b == entry.getValue());
//        // The map. Get the key of the map entry, then apply the property
//        final Function<EnumMap.Entry<? extends Enum<?>,Boolean>,T> map = (entry) -> transform.apply(entry.getKey());
//        final IntFunction<T[]> generator = (int n) -> (T[]) Array.newInstance(resultType, n);
//        return enumMap.entrySet().stream().filter(filter).map(map).toArray(generator);
//    }
//
//    <E extends Enum<E>> E[] getVisibleValues2(Class<E> enumType, boolean b) { return getVisibleValues2(enumType, b, enumType, enumType::cast); }
//    <E extends Enum<E>> E[] getVisibleValues2(Class<E> enumType) { return getVisibleValues2(enumType, true, enumType, enumType::cast); }

    // Version with no transform application
    <E extends Enum<E>> E[] getVisibleValues(Class<E> enumType, boolean b) { return getVisibleValues(enumType, b, enumType, x -> x); }
    <E extends Enum<E>> E[] getVisibleValues(Class<E> enumType) { return getVisibleValues(enumType, true, enumType, x-> x); }

    // Specifically for names
    private <E extends Enum<E> & NameDisplayable> String[] getVisibleValueNames(Class<E> enumType, boolean b) { return getVisibleValues(enumType, b, String.class, E::getDisplayName); }

    // Getting the visibility of the spanning type
    private <E extends QuantityType> boolean getSpanningTypeVisibility(Class<E> quantityType) {
        try {
            final QuantityType[] enums = quantityType.getEnumConstants();
            if (enums == null) { return false; }
            final Enum e = (Enum) enums[0].getSpanningType();
            return getVisibility(e);
        } catch (NullPointerException e) {
            return false;
        }
    }


    int getSpanningTypeVisible(Class<? extends QuantityType> quantityType) {
        return getSpanningTypeVisibility(quantityType) ? View.VISIBLE : View.GONE;
    }

    // This is the general function that the generated ItemFilterViewBinding class will call
    // We use getClass to get the correct map
    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> boolean getVisibility(E e) {
        final Class<?> cls = e.getClass();
        final EnumMap<E,Boolean> map = (EnumMap<E,Boolean>) visibilitiesMap.get(cls);
        if (map == null) { return false; }
        return SpellbookUtils.coalesce(map.get(e), false);
    }

    public boolean getVisibility(NameDisplayable e) {
        if (Enum.class.isAssignableFrom(e.getClass())) {
            return getVisibility((Enum) e);
        }
        return false;
    }

    // Getting the range filter values
    Unit getMinUnit(Class<? extends QuantityType> quantityType) { return quantityRangeFiltersMap.get(quantityType).getValue2(); }
    Unit getMaxUnit(Class<? extends QuantityType> quantityType) { return quantityRangeFiltersMap.get(quantityType).getValue3(); }
    int getMinValue(Class<? extends QuantityType> quantityType) { return quantityRangeFiltersMap.get(quantityType).getValue4(); }
    int getMaxValue(Class<? extends QuantityType> quantityType) { return quantityRangeFiltersMap.get(quantityType).getValue5(); }

    // For getting the defaults
    static Unit getDefaultMinUnit(Class<? extends QuantityType> quantityType) { return defaultQuantityRangeFiltersMap.get(quantityType).getValue2(); }
    static Unit getDefaultMaxUnit(Class<? extends QuantityType> quantityType) { return defaultQuantityRangeFiltersMap.get(quantityType).getValue3(); }
    static int getDefaultMinValue(Class<? extends QuantityType> quantityType) { return defaultQuantityRangeFiltersMap.get(quantityType).getValue4(); }
    static int getDefaultMaxValue(Class<? extends QuantityType> quantityType) { return defaultQuantityRangeFiltersMap.get(quantityType).getValue5(); }

    // Restoring a range to the default values
    void setRangeToDefaults(Class<? extends QuantityType> type) {
        quantityRangeFiltersMap.put(type, defaultQuantityRangeFiltersMap.get(type));
    }

    // Checking whether a not a specific filter (or any filter) is set
    boolean filterFavorites() { return (statusFilter == StatusFilterField.FAVORITES); }
    boolean filterPrepared() { return (statusFilter == StatusFilterField.PREPARED); }
    boolean filterKnown() { return (statusFilter == StatusFilterField.KNOWN); }
    boolean isStatusSet() { return (statusFilter != StatusFilterField.ALL); }

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
    void setFavorite(Spell s, Boolean fav) { setProperty(s, fav, (SpellStatus status, Boolean tf) -> status.favorite = tf); }
    void setPrepared(Spell s, Boolean prep) { setProperty(s, prep, (SpellStatus status, Boolean tf) -> status.prepared = tf); }
    void setKnown(Spell s, Boolean known) { setProperty(s, known, (SpellStatus status, Boolean tf) -> status.known = tf); }

    // Setting whether or not the ritual and concentration filters are set
    void setRitualFilter(boolean f, boolean b) {
        if (f) {
            ritualFilter = b;
        } else {
            notRitualFilter = b;
        }
    }
    void setConcentrationFilter(boolean f, boolean b) {
        if (f) {
            concentrationFilter = b;
        } else {
            notConcentrationFilter = b;
        }
    }

    // Toggling whether or not the ritual and concentration filters are set
    void toggleRitualFilter(boolean f) { setRitualFilter(f, !getRitualFilter(f)); }
    void toggleConcentrationFilter(boolean f) { setConcentrationFilter(f, !getConcentrationFilter(f)); }

    // Toggling whether a given property is set for a given spell
    private void toggleProperty(Spell s, Function<SpellStatus,Boolean> property, BiConsumer<SpellStatus,Boolean> propSetter) { setProperty(s, !isProperty(s, property), propSetter); }
    void toggleFavorite(Spell s) { toggleProperty(s, (SpellStatus status) -> status.favorite, (SpellStatus status, Boolean tf) -> status.favorite = tf); }
    void togglePrepared(Spell s) { toggleProperty(s, (SpellStatus status) -> status.prepared, (SpellStatus status, Boolean tf) -> status.prepared = tf); }
    void toggleKnown(Spell s) { toggleProperty(s, (SpellStatus status) -> status.known, (SpellStatus status, Boolean tf) -> status.known = tf); }

    // Setting visibilities in the maps
    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> void setVisibility(E e, boolean tf) {
        final Class<?> type = e.getClass();
        try {
            final EnumMap<E, Boolean> enumMap = (EnumMap<E, Boolean>) visibilitiesMap.get(type);
            enumMap.put(e, tf);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    // Toggling visibility in the maps
    <E extends Enum<E>> void toggleVisibility(E e) {
        setVisibility(e, !getVisibility(e));
    }

    // Get the range info
    Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> getQuantityRangeInfo(Class<? extends QuantityType> quantityType) {
        return quantityRangeFiltersMap.get(quantityType);
    }


    // Basic setters
    void setFirstSortField(SortField sf) { sortField1 = sf; }
    void setSecondSortField(SortField sf) { sortField2 = sf; }
    void setSortField(SortField sf, int level) {
        switch (level) {
            case 1:
                sortField1 = sf;
                break;
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
                break;
            case 2:
                reverse2 = b;
        }
    }
    void setMinSpellLevel(int level) { minSpellLevel = level; }
    void setMaxSpellLevel(int level) { maxSpellLevel = level; }
    void setStatusFilter(StatusFilterField sff) { statusFilter = sff; }

    // For setting range filter data
    void setMinValue(Class<? extends QuantityType> quantityType, Integer min) {
        Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> newSextet = quantityRangeFiltersMap.get(quantityType).setAt4(min);
        quantityRangeFiltersMap.put(quantityType, newSextet);
    }
    void setMaxValue(Class<? extends QuantityType> quantityType, Integer max) {
        Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> newSextet = quantityRangeFiltersMap.get(quantityType).setAt5(max);
        quantityRangeFiltersMap.put(quantityType, newSextet);
    }
    void setMinUnit(Class<? extends QuantityType> quantityType, Unit minUnit) {
        Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> newSextet = quantityRangeFiltersMap.get(quantityType).setAt2(minUnit);
        quantityRangeFiltersMap.put(quantityType, newSextet);
    }
    void setMaxUnit(Class<? extends QuantityType> quantityType, Unit maxUnit) {
        Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> newSextet = quantityRangeFiltersMap.get(quantityType).setAt3(maxUnit);
        quantityRangeFiltersMap.put(quantityType, newSextet);
    }


    // Constructing a map from a list of hidden values
    // Used for JSON decoding
    @SuppressWarnings("unchecked")
    private static EnumMap<?,Boolean> mapFromHiddenNames(EnumMap<? extends Enum<?>,Boolean> defaultMap, boolean nonTrivialFilter, Function<Object,Boolean> filter, JSONObject json, String key, Method constructorFromName) throws JSONException, IllegalAccessException, InvocationTargetException {
        final EnumMap map = SerializationUtils.clone(defaultMap);
        if (nonTrivialFilter) {
            for (Enum<?> e : defaultMap.keySet()) {
                map.put(e, true);
            }
        }
        if (json.has(key)) {
            final JSONArray jsonArray = json.getJSONArray(key);
            for (int i = 0; i < jsonArray.length(); ++i) {
                final String name = jsonArray.getString(i);
                final Enum<?> value = (Enum<?>) constructorFromName.invoke(null, name);
                map.put(value, false);
            }
        }
        return map;
    }

    // Save to a file
    void save(File filename) {
        try {
            final JSONObject cpJSON = toJSON();
            System.out.println("Saving JSON: " + cpJSON.toString());
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
        for (HashMap.Entry<Class<? extends Enum<?>>, Quartet<Boolean, Function<Object,Boolean>, String, String>> entry : enumInfo.entrySet()) {
            final Class cls = entry.getKey();
            final String key = entry.getValue().getValue2();
            final JSONArray jsonArray = new JSONArray(getVisibleValueNames(cls, false));
            json.put(key, jsonArray);
        }

        // Put in the map of the quantity range filter info
        final JSONObject quantityRangesJSON = new JSONObject();
        for (HashMap.Entry<Class<? extends QuantityType>, Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer>> entry : quantityRangeFiltersMap.entrySet()) {
            final Class<? extends QuantityType> quantityType = entry.getKey();
            final Class<? extends Enum<?>> quantityAsEnum = (Class<? extends Enum<?>>) quantityType;
            final Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> data = entry.getValue();
            final String key = enumInfo.get(quantityAsEnum).getValue3();
            final JSONObject rangeJSON = new JSONObject();
            for (int i = 2; i < data.getSize(); ++i) {
                String toPut = "";
                final Object obj = data.getValue(i);
                if (obj instanceof Unit) {
                    toPut = ((Unit) obj).pluralName();
                } else if (obj instanceof Integer){
                    toPut = Integer.toString((Integer) obj );
                }
                rangeJSON.put(rangeFilterKeys[i-2], toPut);
            }
            quantityRangesJSON.put(key, rangeJSON);
        }
        json.put(quantityRangesKey, quantityRangesJSON);


        json.put(statusFilterKey, statusFilter.getDisplayName());

        json.put(ritualKey, ritualFilter);
        json.put(notRitualKey, notRitualFilter);
        json.put(concentrationKey, concentrationFilter);
        json.put(notConcentrationKey, notConcentrationFilter);

        json.put(minSpellLevelKey, minSpellLevel);
        json.put(maxSpellLevelKey, maxSpellLevel);

        json.put(versionCodeKey, GlobalInfo.VERSION_CODE);

        return json;
    }


    // Construct a profile from a JSON object
    // Basically the inverse to toJSON
    static CharacterProfile fromJSON(JSONObject json) throws JSONException {
        if (json.has(versionCodeKey)) {
            return fromJSONNew(json);
        } else {
            return fromJSONOld(json);
        }
    }

    // For backwards compatibility
    // so that when people update, their old profiles are still usable
    static private CharacterProfile fromJSONOld(JSONObject json) throws JSONException {

        final String charName = json.getString(charNameKey);

        // Get the spell map, assuming it exists
        // If it doesn't, we just get an empty map
        final HashMap<String, SpellStatus> spellStatusMap = new HashMap<>();
        if (json.has(spellsKey)) {
            final JSONArray jsonArray = json.getJSONArray(spellsKey);
            for (int i = 0; i < jsonArray.length(); ++i) {
                final JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Get the name and array of statuses
                final String spellName = jsonObject.getString(spellNameKey);

                // Load the spell statuses
                final boolean fav = jsonObject.getBoolean(favoriteKey);
                final boolean prep = jsonObject.getBoolean(preparedKey);
                final boolean known = jsonObject.getBoolean(knownKey);
                final SpellStatus status = new SpellStatus(fav, prep, known);

                // Add to the map
                spellStatusMap.put(spellName, status);
            }
        }

        // Get the first sort field, if present
        final SortField sortField1 = json.has(sort1Key) ? SortField.fromDisplayName(json.getString(sort1Key)) : SortField.NAME;

        // Get the second sort field, if present
        final SortField sortField2 = json.has(sort2Key) ? SortField.fromDisplayName(json.getString(sort2Key)) : SortField.NAME;

        // Get the sort reverse variables
        final boolean reverse1 = json.optBoolean(reverse1Key, false);
        final boolean reverse2 = json.optBoolean(reverse2Key, false);

        // Set up the visibility map
        final HashMap<Class<? extends Enum<?>>, EnumMap<? extends Enum<?>, Boolean>> visibilitiesMap = SerializationUtils.clone(defaultVisibilitiesMap);

        // If there was a filter class before, that's now the only visible class
        final CasterClass filterClass = json.has(classFilterKey) ? CasterClass.fromDisplayName(json.getString(classFilterKey)) : null;
        if (filterClass != null) {
            final EnumMap<? extends Enum<?>, Boolean> casterMap = SerializationUtils.clone(defaultVisibilitiesMap.get(CasterClass.class));
            for (EnumMap.Entry<? extends Enum<?>, Boolean> entry : casterMap.entrySet()) {
                if (entry.getKey() != filterClass) {
                    entry.setValue(false);
                }
            }
            visibilitiesMap.put(CasterClass.class, casterMap);
        }

        // What was the sourcebooks filter map is now the sourcebook entry of the visibilities map
        if (json.has(booksFilterKey)) {
            final EnumMap<Sourcebook, Boolean> sourcebookMap = new EnumMap<>(Sourcebook.class);
            final JSONObject booksJSON = json.getJSONObject(booksFilterKey);
            for (Sourcebook sb : Sourcebook.values()) {
                if (booksJSON.has(sb.getCode())) {
                    sourcebookMap.put(sb, booksJSON.getBoolean(sb.getCode()));
                } else {
                    final boolean b = (sb == Sourcebook.PLAYERS_HANDBOOK); // True if PHB, false otherwise
                    sourcebookMap.put(sb, b);
                }
            }
            visibilitiesMap.put(Sourcebook.class, sourcebookMap);
        }

        // Get the status filter
        final StatusFilterField statusFilter = json.has(statusFilterKey) ? StatusFilterField.fromDisplayName(json.getString(statusFilterKey)) : StatusFilterField.ALL;

        // We no longer need the default filter statuses, and the spinners no longer have the default text

        // Everything else that the profiles have is new, so we'll use the defaults
        final HashMap<Class<? extends QuantityType>, Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer>> quantityRangesMap = SerializationUtils.clone(defaultQuantityRangeFiltersMap);
        final int minLevel = Spellbook.MIN_SPELL_LEVEL;
        final int maxLevel = Spellbook.MAX_SPELL_LEVEL;

        // Return the profile
        return new CharacterProfile(charName, spellStatusMap, sortField1, sortField2, visibilitiesMap, quantityRangesMap, reverse1, reverse2, statusFilter, true, true, true, true, minLevel, maxLevel);

    }


    // For character profiles from this version of the app
    static private CharacterProfile fromJSONNew(JSONObject json) throws JSONException {

        System.out.println("The JSON is " + json.toString());

        final String charName = json.getString(charNameKey);

        // Get the spell map, assuming it exists
        // If it doesn't, we just get an empty map
        final HashMap<String, SpellStatus> spellStatusMap = new HashMap<>();
        if (json.has(spellsKey)) {
            final JSONArray jarr = json.getJSONArray(spellsKey);
            for (int i = 0; i < jarr.length(); ++i) {
                final JSONObject jobj = jarr.getJSONObject(i);

                // Get the name and array of statuses
                final String spellName = jobj.getString(spellNameKey);

                // Load the spell statuses
                final boolean fav = jobj.getBoolean(favoriteKey);
                final boolean prep = jobj.getBoolean(preparedKey);
                final boolean known = jobj.getBoolean(knownKey);
                final SpellStatus status = new SpellStatus(fav, prep, known);

                // Add to the map
                spellStatusMap.put(spellName, status);
            }
        }

        // Get the first sort field, if present
        final SortField sortField1 = json.has(sort1Key) ? SortField.fromDisplayName(json.getString(sort1Key)) : SortField.NAME;

        // Get the second sort field, if present
        final SortField sortField2 = json.has(sort2Key) ? SortField.fromDisplayName(json.getString(sort2Key)) : SortField.NAME;

        // Create the visibility maps
        final HashMap<Class<? extends Enum<?>>, EnumMap<? extends Enum<?>, Boolean>> visibilitiesMap = SerializationUtils.clone(defaultVisibilitiesMap);
        for (HashMap.Entry<Class<? extends Enum<?>>, Quartet<Boolean, Function<Object,Boolean>, String, String>> entry : enumInfo.entrySet()) {
            final Class<? extends Enum<?>> cls = entry.getKey();
            Quartet<Boolean, Function<Object,Boolean>, String, String> entryValue = entry.getValue();
            final String key = entryValue.getValue2();
            final Function<Object,Boolean> filter = entryValue.getValue1();
            final boolean nonTrivialFilter = entryValue.getValue0();
            final EnumMap<? extends Enum<?>, Boolean> defaultMap = defaultVisibilitiesMap.get(cls);
            try {
                final Method constructorFromName = cls.getDeclaredMethod("fromDisplayName", String.class);
                final EnumMap<? extends Enum<?>, Boolean> map = mapFromHiddenNames(defaultMap, nonTrivialFilter, filter, json, key, constructorFromName);
                visibilitiesMap.put(cls, map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Create the range filter map
        final HashMap<Class<? extends QuantityType>, Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer>> quantityRangesMap = SerializationUtils.clone(defaultQuantityRangeFiltersMap);
        if (json.has(quantityRangesKey)) {
            try {
                final JSONObject quantityRangesJSON = json.getJSONObject(quantityRangesKey);
                final Iterator<String> it = quantityRangesJSON.keys();
                while (it.hasNext()) {
                    final String key = it.next();
                    final Class<? extends QuantityType> quantityType = keyToQuantityTypeMap.get(key);
                    final Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> defaultData = quantityRangesMap.get(quantityType);
                    final Class<? extends Quantity> quantityClass = defaultData.getValue0();
                    final Class<? extends Unit> unitClass = defaultData.getValue1();
                    final JSONObject rangeJSON = quantityRangesJSON.getJSONObject(key);
                    final Method method = unitClass.getDeclaredMethod("fromString", String.class);
                    final Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> sextet =
                        new Sextet<>(
                                quantityClass, unitClass,
                                (Unit) method.invoke(null, rangeJSON.getString(rangeFilterKeys[0])),
                                (Unit) method.invoke(null, rangeJSON.getString(rangeFilterKeys[1])),
                                Integer.parseInt(rangeJSON.getString(rangeFilterKeys[2])), Integer.parseInt(rangeJSON.getString(rangeFilterKeys[3]))
                        );
                    quantityRangesMap.put(quantityType, sextet);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Get the sort reverse variables
        final boolean reverse1 = json.optBoolean(reverse1Key, false);
        final boolean reverse2 = json.optBoolean(reverse2Key, false);

        // Get the filter statuses for ritual and concentration
        final boolean ritualFilter = json.optBoolean(ritualKey, true);
        final boolean notRitualFilter = json.optBoolean(notRitualKey, true);
        final boolean concentrationFilter = json.optBoolean(concentrationKey, true);
        final boolean notConcentrationFilter = json.optBoolean(notConcentrationKey, true);


        // Get the min and max spell levels
        final int minLevel = json.optInt(minSpellLevelKey, Spellbook.MIN_SPELL_LEVEL);
        final int maxLevel = json.optInt(maxSpellLevelKey, Spellbook.MAX_SPELL_LEVEL);

        // Get the status filter
        final StatusFilterField statusFilter = json.has(statusFilterKey) ? StatusFilterField.fromDisplayName(json.getString(statusFilterKey)) : StatusFilterField.ALL;

        // Return the profile
        return new CharacterProfile(charName, spellStatusMap, sortField1, sortField2, visibilitiesMap, quantityRangesMap, reverse1, reverse2, statusFilter, ritualFilter, notRitualFilter, concentrationFilter, notConcentrationFilter, minLevel, maxLevel);

    }


}
