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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private Map<Integer,SpellStatus> spellStatuses;
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
    private final boolean[] componentsFilters;
    private final boolean[] notComponentsFilters;
    private final Map<Class<? extends Enum<?>>, EnumMap<? extends Enum<?>, Boolean>> visibilitiesMap;
    private final Map<Class<? extends QuantityType>, Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer>> quantityRangeFiltersMap;
    private boolean useTCEExpandedLists;
    private boolean applyFiltersToSpellLists;
    private boolean applyFiltersToSearch;

    // Keys for loading/saving
    private static final String charNameKey = "CharacterName";
    private static final String spellsKey = "Spells";
    private static final String spellNameKey = "SpellName";
    private static final String spellIDKey = "SpellID";
    private static final String favoriteKey = "Favorite";
    private static final String preparedKey = "Prepared";
    private static final String knownKey = "Known";
    private static final String sort1Key = "SortField1";
    private static final String sort2Key = "SortField2";
    private static final String classFilterKey = "FilterClass";
    private static final String reverse1Key = "Reverse1";
    private static final String reverse2Key = "Reverse2";
    private static final String booksFilterKey = "BookFilters";
    private static final String statusFilterKey = "StatusFilter";
    private static final String quantityRangesKey = "QuantityRanges";
    private static final String ritualKey = "Ritual";
    private static final String notRitualKey = "NotRitual";
    private static final String concentrationKey = "Concentration";
    private static final String notConcentrationKey = "NotConcentration";
    private static final String minSpellLevelKey = "MinSpellLevel";
    private static final String maxSpellLevelKey = "MaxSpellLevel";
    private static final String versionCodeKey = "VersionCode";
    private static final String componentsFiltersKey = "ComponentsFilters";
    private static final String notComponentsFiltersKey = "NotComponentsFilters";
    private static final String useTCEExpandedListsKey = "UseTCEExpandedLists";
    private static final String applyFiltersToSpellListsKey = "ApplyFiltersToSpellLists";
    private static final String applyFiltersToSearchKey = "ApplyFiltersToSearch";

    // Not currently needed
    // This function is the generic version of the map-creation piece of (wildcard-based) instantiation of the default visibilities map
//    private static <E extends Enum<E>> EnumMap<E,Boolean> defaultFilterMap(Class<E> enumType, Function<E,Boolean> filter) {
//        final EnumMap<E,Boolean> enumMap = new EnumMap<>(enumType);
//        final E[] enumValues = enumType.getEnumConstants();
//        if (enumValues == null) { return enumMap; }
//        for (E e : enumValues) {
//            enumMap.put(e, filter.apply(e));
//        }
//        return enumMap;
//    }

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

    private CharacterProfile(String name, Map<Integer, SpellStatus> spellStatusesIn, SortField sf1, SortField sf2,  Map<Class<? extends Enum<?>>,
            EnumMap<? extends Enum<?>, Boolean>> visibilities, Map<Class<? extends QuantityType>, Sextet<Class<? extends Quantity>,
            Class<? extends Unit>, Unit, Unit, Integer, Integer>> rangeFilters, boolean rev1, boolean rev2, StatusFilterField filter,
                             boolean ritualStatus, boolean notRitualStatus, boolean concentrationStatus, boolean notConcentrationStatus,
                             boolean[] componentsFiltersIn, boolean[] notComponentsFiltersIn, int minLevel, int maxLevel,
                             boolean useTGEExpandedListsIn, boolean applyFiltersToSpellListsIn, boolean applyFiltersToSearchIn) {
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
        componentsFilters = componentsFiltersIn;
        notComponentsFilters = notComponentsFiltersIn;
        useTCEExpandedLists = useTGEExpandedListsIn;
        applyFiltersToSpellLists = applyFiltersToSpellListsIn;
        applyFiltersToSearch = applyFiltersToSearchIn;
    }

    private CharacterProfile(String name, Map<Integer, SpellStatus> spellStatusesIn) {
        this(name, spellStatusesIn, SortField.NAME, SortField.NAME, SerializationUtils.clone(defaultVisibilitiesMap), SerializationUtils.clone(defaultQuantityRangeFiltersMap), false, false, StatusFilterField.ALL, true, true, true, true, new boolean[]{true,true,true}, new boolean[]{true,true,true}, Spellbook.MIN_SPELL_LEVEL, Spellbook.MAX_SPELL_LEVEL, false, false, false);
    }

    CharacterProfile(String nameIn) { this(nameIn, new HashMap<>()); }

    // Basic getters
    String getName() { return charName; }
    Map<Integer, SpellStatus> getStatuses() { return spellStatuses; }
    SortField getFirstSortField() { return sortField1; }
    SortField getSecondSortField() { return sortField2; }
    boolean getFirstSortReverse() { return reverse1; }
    boolean getSecondSortReverse() { return reverse2; }
    boolean getRitualFilter(boolean b) { return b ? ritualFilter : notRitualFilter; }
    boolean getConcentrationFilter(boolean b) { return b ? concentrationFilter : notConcentrationFilter; }
    private boolean getComponentFilter(int i, boolean b) { return b ? componentsFilters[i] : notComponentsFilters[i]; }
    boolean getVerbalComponentFilter(boolean b) { return getComponentFilter(0, b); }
    boolean getSomaticComponentFilter(boolean b) { return getComponentFilter(1, b); }
    boolean getMaterialComponentFilter(boolean b) { return getComponentFilter(2, b); }
    public int getMinSpellLevel() { return minSpellLevel; }
    public int getMaxSpellLevel() { return maxSpellLevel; }
    StatusFilterField getStatusFilter() { return statusFilter; }
    boolean getUseTCEExpandedLists() { return useTCEExpandedLists; }
    boolean getApplyFiltersToSpellLists() { return applyFiltersToSpellLists; }
    boolean getApplyFiltersToSearch() { return applyFiltersToSearch; }

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
    private <E extends Enum<E> & NameDisplayable> String[] getVisibleValueNames(Class<E> enumType, boolean b, Function<E,String> namingFunction) {
        return getVisibleValues(enumType, b, String.class, namingFunction);
    }
    private <E extends Enum<E> & NameDisplayable> String[] getVisibleValueInternalNames(Class<E> enumType, boolean b) {
        return getVisibleValueNames(enumType, b, E::getInternalName);
    }

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
    private boolean isProperty(Spell spell, Function<SpellStatus,Boolean> property) {
        if (spellStatuses.containsKey(spell.getID())) {
            SpellStatus status = spellStatuses.get(spell.getID());
            return property.apply(status);
        }
        return false;
    }

    boolean satisfiesFilter(Spell spell, StatusFilterField sff) {
        switch (sff) {
            case FAVORITES:
                return isFavorite(spell);
            case PREPARED:
                return isPrepared(spell);
            case KNOWN:
                return isKnown(spell);
            default:
                return true;
        }
    }
    boolean isFavorite(Spell spell) { return isProperty(spell, (SpellStatus status) -> status.favorite); }
    boolean isPrepared(Spell spell) { return isProperty(spell, (SpellStatus status) -> status.prepared); }
    boolean isKnown(Spell spell) { return isProperty(spell, (SpellStatus status) -> status.known); }


    // Setting whether a spell is on a given spell list
    private void setProperty(Spell spell, Boolean val, BiConsumer<SpellStatus,Boolean> propSetter) {
        final int spellID = spell.getID();
        if (spellStatuses.containsKey(spellID)) {
            SpellStatus status = spellStatuses.get(spellID);
            propSetter.accept(status, val);
            // spellStatuses.put(spellName, status);
            if (status.noneTrue()) { // We can remove the key if all three are false
                spellStatuses.remove(spellID);
            }
        } else if (val) { // If the key doesn't exist, we only need to modify if val is true
            SpellStatus status = new SpellStatus();
            propSetter.accept(status, true);
            spellStatuses.put(spellID, status);
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
    private void setComponentFilter(int i, boolean f, boolean b) {
        if (f) {
            componentsFilters[i] = b;
        } else {
            notComponentsFilters[i] = b;
        }
    }
    void setVerbalComponentFilter(boolean f, boolean b) { setComponentFilter(0, f, b); }
    void setSomaticComponentFilter(boolean f, boolean b) { setComponentFilter(1, f, b); }
    void setMaterialComponentFilter(boolean f, boolean b) { setComponentFilter(2, f, b); }

    // Toggling whether or not filters are set
    void toggleRitualFilter(boolean f) { setRitualFilter(f, !getRitualFilter(f)); }
    void toggleConcentrationFilter(boolean f) { setConcentrationFilter(f, !getConcentrationFilter(f)); }
    private void toggleComponentFilter(int i, boolean f) { setComponentFilter(i, f, !getComponentFilter(i, f)); }
    void toggleVerbalComponentFilter(boolean f) { toggleComponentFilter(0, f); }
    void toggleSomaticComponentFilter(boolean f) { toggleComponentFilter(1, f); }
    void toggleMaterialComponentFilter(boolean f) { toggleComponentFilter(2, f); }

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
    void setName(String name) { charName = name; }
    void setMinSpellLevel(int level) { minSpellLevel = level; }
    void setMaxSpellLevel(int level) { maxSpellLevel = level; }
    void setStatusFilter(StatusFilterField sff) { statusFilter = sff; }
    void setUseTCEExpandedLists(boolean tf) { useTCEExpandedLists = tf; }
    void setApplyFiltersToSpellLists(boolean tf) { applyFiltersToSpellLists = tf; }
    void setApplyFiltersToSearch(boolean tf) { applyFiltersToSearch = tf; }

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
    boolean save(File filename) {
        try {
            final JSONObject cpJSON = toJSON();
            //System.out.println("Saving JSON: " + cpJSON.toString());
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
                bw.write(cpJSON.toString());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
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
        for (HashMap.Entry<Integer, SpellStatus> data : spellStatuses.entrySet()) {
            JSONObject statusJSON = new JSONObject();
            statusJSON.put(spellIDKey, data.getKey());
            SpellStatus status = data.getValue();
            statusJSON.put(favoriteKey, status.favorite);
            statusJSON.put(preparedKey, status.prepared);
            statusJSON.put(knownKey, status.known);
            spellStatusJA.put(statusJSON);
        }
        json.put(spellsKey, spellStatusJA);

        json.put(sort1Key, sortField1.getInternalName());
        json.put(sort2Key, sortField2.getInternalName());
        json.put(reverse1Key, reverse1);
        json.put(reverse2Key, reverse2);

        // Put in the arrays of hidden enums
        for (HashMap.Entry<Class<? extends Enum<?>>, Quartet<Boolean, Function<Object,Boolean>, String, String>> entry : enumInfo.entrySet()) {
            final Class cls = entry.getKey();
            final String key = entry.getValue().getValue2();
            final JSONArray jsonArray;
            if (cls.equals(Sourcebook.class)) {
                jsonArray = new JSONArray(getVisibleValueNames(Sourcebook.class, false, Sourcebook::getInternalCode));
            } else {
                jsonArray = new JSONArray(getVisibleValueInternalNames(cls, false));
            }
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
                    toPut = ((Unit) obj).getInternalName();
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
        json.put(componentsFiltersKey, new JSONArray(componentsFilters));
        json.put(notComponentsFiltersKey, new JSONArray(notComponentsFilters));

        json.put(minSpellLevelKey, minSpellLevel);
        json.put(maxSpellLevelKey, maxSpellLevel);

        json.put(applyFiltersToSpellListsKey, applyFiltersToSpellLists);
        json.put(applyFiltersToSearchKey, applyFiltersToSearch);
        json.put(useTCEExpandedListsKey, useTCEExpandedLists);

        json.put(versionCodeKey, GlobalInfo.VERSION_CODE);

        return json;
    }


    // Construct a profile from a JSON object
    // Basically the inverse to toJSON
    static CharacterProfile fromJSON(JSONObject json) throws JSONException {
        if (json.has(versionCodeKey)) {
            final String versionCode = json.getString(versionCodeKey);
            if (versionCode.equals(GlobalInfo.VERSION_CODE)) {
                return fromJSONNew(json);
            } else {
                return fromJSONPre2_10(json);
            }
        } else {
            return fromJSONOld(json);
        }
    }

    // For backwards compatibility
    // so that when people update, their old profiles are still usable
    private static CharacterProfile fromJSONOld(JSONObject json) throws JSONException {

        final String charName = json.getString(charNameKey);

        // Get the spell map, assuming it exists
        // If it doesn't, we just get an empty map
        final Map<String, SpellStatus> spellStatusNameMap = new HashMap<>();
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
                spellStatusNameMap.put(spellName, status);
            }
        }
        final Map<Integer,SpellStatus> spellStatusMap = convertStatusMap(spellStatusNameMap);

        // Get the first sort field, if present
        final SortField sortField1 = json.has(sort1Key) ? SortField.fromInternalName(json.getString(sort1Key)) : SortField.NAME;

        // Get the second sort field, if present
        final SortField sortField2 = json.has(sort2Key) ? SortField.fromInternalName(json.getString(sort2Key)) : SortField.NAME;

        // Get the sort reverse variables
        final boolean reverse1 = json.optBoolean(reverse1Key, false);
        final boolean reverse2 = json.optBoolean(reverse2Key, false);

        // Set up the visibility map
        final HashMap<Class<? extends Enum<?>>, EnumMap<? extends Enum<?>, Boolean>> visibilitiesMap = SerializationUtils.clone(defaultVisibilitiesMap);

        // If there was a filter class before, that's now the only visible class
        final CasterClass filterClass = json.has(classFilterKey) ? CasterClass.fromInternalName(json.getString(classFilterKey)) : null;
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
                if (booksJSON.has(sb.getInternalName())) {
                    sourcebookMap.put(sb, booksJSON.getBoolean(sb.getInternalName()));
                } else {
                    final boolean b = (sb == Sourcebook.PLAYERS_HANDBOOK); // True if PHB, false otherwise
                    sourcebookMap.put(sb, b);
                }
            }
            sourcebookMap.put(Sourcebook.TASHAS_COE, false);
            visibilitiesMap.put(Sourcebook.class, sourcebookMap);
        }

        // Get the status filter
        final StatusFilterField statusFilter = json.has(statusFilterKey) ? StatusFilterField.fromDisplayName(json.getString(statusFilterKey)) : StatusFilterField.ALL;

        // We no longer need the default filter statuses, and the spinners no longer have the default text

        // Everything else that the profiles have is new, so we'll use the defaults
        final Map<Class<? extends QuantityType>, Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer>> quantityRangesMap = SerializationUtils.clone(defaultQuantityRangeFiltersMap);
        final int minLevel = Spellbook.MIN_SPELL_LEVEL;
        final int maxLevel = Spellbook.MAX_SPELL_LEVEL;

        // Return the profile
        return new CharacterProfile(charName, spellStatusMap, sortField1, sortField2, visibilitiesMap, quantityRangesMap, reverse1, reverse2, statusFilter, true, true, true, true, new boolean[]{true,true,true}, new boolean[]{true,true,true}, minLevel, maxLevel, false, false, false);

    }

    // For character profiles from this version of the app
    private static CharacterProfile fromJSONNew(JSONObject json) throws JSONException {

        final String charName = json.getString(charNameKey);

        // Get the first sort field, if present
        final SortField sortField1 = json.has(sort1Key) ? SortField.fromInternalName(json.getString(sort1Key)) : SortField.NAME;

        // Get the second sort field, if present
        final SortField sortField2 = json.has(sort2Key) ? SortField.fromInternalName(json.getString(sort2Key)) : SortField.NAME;

        final Map<Integer, SpellStatus> spellStatusMap = new HashMap<>();
        if (json.has(spellsKey)) {
            final JSONArray jsonArray = json.getJSONArray(spellsKey);
            for (int i = 0; i < jsonArray.length(); ++i) {
                final JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Get the name and array of statuses
                final Integer spellID = jsonObject.getInt(spellIDKey);

                // Load the spell statuses
                final boolean fav = jsonObject.getBoolean(favoriteKey);
                final boolean prep = jsonObject.getBoolean(preparedKey);
                final boolean known = jsonObject.getBoolean(knownKey);
                final SpellStatus status = new SpellStatus(fav, prep, known);

                // Add to the map
                spellStatusMap.put(spellID, status);
            }
        }

        // Create the visibility maps
        final Map<Class<? extends Enum<?>>, EnumMap<? extends Enum<?>, Boolean>> visibilitiesMap = SerializationUtils.clone(defaultVisibilitiesMap);
        for (Map.Entry<Class<? extends Enum<?>>, Quartet<Boolean, Function<Object,Boolean>, String, String>> entry : enumInfo.entrySet()) {
            final Class<? extends Enum<?>> cls = entry.getKey();
            Quartet<Boolean, Function<Object,Boolean>, String, String> entryValue = entry.getValue();
            final String key = entryValue.getValue2();
            final Function<Object,Boolean> filter = entryValue.getValue1();
            final boolean nonTrivialFilter = entryValue.getValue0();
            final EnumMap<? extends Enum<?>, Boolean> defaultMap = defaultVisibilitiesMap.get(cls);
            try {
                final String constructorName = "fromInternalName";
                final Method constructorFromName = cls.getDeclaredMethod(constructorName, String.class);
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
                    final Method method = unitClass.getDeclaredMethod("fromInternalName", String.class);
                    final Unit unit1 = SpellbookUtils.coalesce((Unit) method.invoke(null, rangeJSON.getString(rangeFilterKeys[0])), defaultData.getValue2());
                    final Unit unit2 = SpellbookUtils.coalesce((Unit) method.invoke(null, rangeJSON.getString(rangeFilterKeys[1])), defaultData.getValue3());
                    final Integer val1 = SpellbookUtils.coalesce(SpellbookUtils.intParse(rangeJSON.getString(rangeFilterKeys[2])), defaultData.getValue4());
                    final Integer val2 = SpellbookUtils.coalesce(SpellbookUtils.intParse(rangeJSON.getString(rangeFilterKeys[3])), defaultData.getValue5());
                    final Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> sextet =
                            new Sextet<>(quantityClass, unitClass, unit1, unit2, val1, val2);
                    //System.out.println("min unit is " + ((Unit) method.invoke(null, rangeJSON.getString(rangeFilterKeys[0]))).getInternalName());
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
        final JSONArray componentsJSON = json.optJSONArray(componentsFiltersKey);
        final boolean[] componentsFilters = new boolean[]{true, true, true};
        if (componentsJSON != null && componentsJSON.length() == 3) {
            for (int i = 0; i < componentsJSON.length(); ++i) {
                componentsFilters[i] = componentsJSON.getBoolean(i);
            }
        }
        final JSONArray notComponentsJSON = json.optJSONArray(notComponentsFiltersKey);
        final boolean[] notComponentsFilters = new boolean[]{true, true, true};
        if (notComponentsJSON != null && notComponentsJSON.length() == 3) {
            for (int i = 0; i < notComponentsJSON.length(); ++i) {
                notComponentsFilters[i] = notComponentsJSON.getBoolean(i);
            }
        }

        // Get the min and max spell levels
        final int minLevel = json.optInt(minSpellLevelKey, Spellbook.MIN_SPELL_LEVEL);
        final int maxLevel = json.optInt(maxSpellLevelKey, Spellbook.MAX_SPELL_LEVEL);

        // Get the status filter
        final StatusFilterField statusFilter = json.has(statusFilterKey) ? StatusFilterField.fromDisplayName(json.getString(statusFilterKey)) : StatusFilterField.ALL;

        // Get the other toggle settings, if present
        final boolean useExpLists = json.optBoolean(useTCEExpandedListsKey, false);
        final boolean applyFilters = json.optBoolean(applyFiltersToSpellListsKey, false);
        final boolean applyToSearch = json.optBoolean(applyFiltersToSearchKey, false);

        // Return the profile
        return new CharacterProfile(charName, spellStatusMap, sortField1, sortField2, visibilitiesMap, quantityRangesMap, reverse1, reverse2, statusFilter, ritualFilter, notRitualFilter, concentrationFilter, notConcentrationFilter, componentsFilters, notComponentsFilters, minLevel, maxLevel, useExpLists, applyFilters, applyToSearch);


    }

    // For character profiles from versions of the app before 2.10 that have a version code
    private static CharacterProfile fromJSONPre2_10(JSONObject json) throws JSONException {

        //System.out.println("The JSON is " + json.toString());

        final String charName = json.getString(charNameKey);

        // Get the spell map, assuming it exists
        // If it doesn't, we just get an empty map
        final Map<String, SpellStatus> spellStatusNameMap = new HashMap<>();
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
                spellStatusNameMap.put(spellName, status);
            }
        }
        final Map<Integer,SpellStatus> spellStatusMap = convertStatusMap(spellStatusNameMap);

        // Get the first sort field, if present
        final SortField sortField1 = json.has(sort1Key) ? SortField.fromInternalName(json.getString(sort1Key)) : SortField.NAME;

        // Get the second sort field, if present
        final SortField sortField2 = json.has(sort2Key) ? SortField.fromInternalName(json.getString(sort2Key)) : SortField.NAME;

        // Create the visibility maps
        final Map<Class<? extends Enum<?>>, EnumMap<? extends Enum<?>, Boolean>> visibilitiesMap = SerializationUtils.clone(defaultVisibilitiesMap);
        for (Map.Entry<Class<? extends Enum<?>>, Quartet<Boolean, Function<Object,Boolean>, String, String>> entry : enumInfo.entrySet()) {
            final Class<? extends Enum<?>> cls = entry.getKey();
            Quartet<Boolean, Function<Object,Boolean>, String, String> entryValue = entry.getValue();
            final String key = entryValue.getValue2();
            final Function<Object,Boolean> filter = entryValue.getValue1();
            final boolean nonTrivialFilter = entryValue.getValue0();
            final EnumMap<? extends Enum<?>, Boolean> defaultMap = defaultVisibilitiesMap.get(cls);
            try {
                final Method constructorFromName = cls.getDeclaredMethod("fromInternalName", String.class);
                final EnumMap<? extends Enum<?>, Boolean> map = mapFromHiddenNames(defaultMap, nonTrivialFilter, filter, json, key, constructorFromName);
                visibilitiesMap.put(cls, map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // If at least one class is hidden, hide the Artificer
        final EnumMap<CasterClass,Boolean> ccMap = (EnumMap<CasterClass,Boolean>) visibilitiesMap.get(CasterClass.class);
        if (ccMap != null) {
            boolean hasOneHidden = false;
            for (boolean vis : ccMap.values()) {
                if (!vis) {
                    hasOneHidden = true;
                    break;
                }
            }
            if (hasOneHidden) {
                ccMap.put(CasterClass.ARTIFICER, false);
            }
        }
        
        // Set Tasha's to be not visible
        final EnumMap<Sourcebook,Boolean> sbMap = (EnumMap<Sourcebook,Boolean>) visibilitiesMap.get(Sourcebook.class);
        if (sbMap != null) {
            sbMap.put(Sourcebook.TASHAS_COE, false);
        }

        // Create the range filter map
        final Map<Class<? extends QuantityType>, Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer>> quantityRangesMap = SerializationUtils.clone(defaultQuantityRangeFiltersMap);
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
                    final Method method = unitClass.getDeclaredMethod("fromInternalName", String.class);
                    final Unit unit1 = SpellbookUtils.coalesce((Unit) method.invoke(null, rangeJSON.getString(rangeFilterKeys[0])), defaultData.getValue2());
                    final Unit unit2 = SpellbookUtils.coalesce((Unit) method.invoke(null, rangeJSON.getString(rangeFilterKeys[1])), defaultData.getValue3());
                    final Integer val1 = SpellbookUtils.coalesce(SpellbookUtils.intParse(rangeJSON.getString(rangeFilterKeys[2])), defaultData.getValue4());
                    final Integer val2 = SpellbookUtils.coalesce(SpellbookUtils.intParse(rangeJSON.getString(rangeFilterKeys[3])), defaultData.getValue5());
                    final Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> sextet =
                            new Sextet<>(quantityClass, unitClass, unit1, unit2, val1, val2);
                    //System.out.println("min unit is " + ((Unit) method.invoke(null, rangeJSON.getString(rangeFilterKeys[0]))).getInternalName());
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
        final JSONArray componentsJSON = json.optJSONArray(componentsFiltersKey);
        final boolean[] componentsFilters = new boolean[]{true, true, true};
        if (componentsJSON != null && componentsJSON.length() == 3) {
            for (int i = 0; i < componentsJSON.length(); ++i) {
                componentsFilters[i] = componentsJSON.getBoolean(i);
            }
        }
        final JSONArray notComponentsJSON = json.optJSONArray(notComponentsFiltersKey);
        final boolean[] notComponentsFilters = new boolean[]{true, true, true};
        if (notComponentsJSON != null && notComponentsJSON.length() == 3) {
            for (int i = 0; i < notComponentsJSON.length(); ++i) {
                notComponentsFilters[i] = notComponentsJSON.getBoolean(i);
            }
        }

        // Get the min and max spell levels
        final int minLevel = json.optInt(minSpellLevelKey, Spellbook.MIN_SPELL_LEVEL);
        final int maxLevel = json.optInt(maxSpellLevelKey, Spellbook.MAX_SPELL_LEVEL);

        // Get the status filter
        final StatusFilterField statusFilter = json.has(statusFilterKey) ? StatusFilterField.fromDisplayName(json.getString(statusFilterKey)) : StatusFilterField.ALL;

        // Return the profile
        return new CharacterProfile(charName, spellStatusMap, sortField1, sortField2, visibilitiesMap, quantityRangesMap, reverse1, reverse2, statusFilter, ritualFilter, notRitualFilter, concentrationFilter, notConcentrationFilter, componentsFilters, notComponentsFilters, minLevel, maxLevel, false, false, false);

    }

    private static Map<Integer,SpellStatus> convertStatusMap(Map<String,SpellStatus> oldMap) {
        final Set<String> scagCantrips = new HashSet<String>() {{
            add("Booming Blade");
            add("Green-Flame Blade");
            add("Lightning Lure");
            add("Sword Burst");
        }};
        final List<Spell> englishSpells = MainActivity.englishSpells;
        final Map<String,Integer> idMap = new HashMap<>();
        for (Spell spell : englishSpells) {
            idMap.put(spell.getName(), spell.getID());
        }
        final Map<Integer,SpellStatus> newMap = new HashMap<>();
        for (Map.Entry<String,SpellStatus> entry : oldMap.entrySet()) {
            String name = entry.getKey();
            final SpellStatus status = entry.getValue();
            if (scagCantrips.contains(name)) {
                name = name + " (SCAG)";
            }
            final Integer id = idMap.get(name);
            if (id != null) {
                newMap.put(id, status);
            }
        }
        return newMap;
    }


}
