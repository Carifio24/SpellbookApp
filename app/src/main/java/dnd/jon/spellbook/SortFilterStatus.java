package dnd.jon.spellbook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;
import org.parceler.ParcelConstructor;
import org.parceler.ParcelProperty;

import java.lang.reflect.Array;
import java.sql.Time;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;

@Parcel
public class SortFilterStatus {

    // Keys for loading/saving
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
    private static final String componentsFiltersKey = "ComponentsFilters";
    private static final String notComponentsFiltersKey = "NotComponentsFilters";
    private static final String useTCEExpandedListsKey = "UseTCEExpandedLists";
    private static final String applyFiltersToSpellListsKey = "ApplyFiltersToSpellLists";
    private static final String applyFiltersToSearchKey = "ApplyFiltersToSearch";

    private static final String sourcebooksKey = "Sourcebooks";
    private static final String schoolsKey = "Schools";
    private static final String classesKey = "Classes";
    private static final String castingTimeTypesKey = "CastingTimeTypes";
    private static final String durationTypesKey = "DurationTypes";
    private static final String rangeTypesKey = "RangeTypes";
    private static final String castingTimeBoundsKey = "CastingTimeBounds";
    private static final String durationBoundsKey = "DurationBounds";
    private static final String rangeBoundsKey = "RangeBounds";
    private static final String minUnitKey = "MinUnit";
    private static final String maxUnitKey = "MaxUnit";
    private static final String minValueKey = "MinValue";
    private static final String maxValueKey = "MaxValue";

    private static final int VERBAL_INDEX = 0;
    private static final int SOMATIC_INDEX = 1;
    private static final int MATERIAL_INDEX = 2;

    private SortField firstSortField = SortField.NAME;
    private SortField secondSortField = SortField.NAME;
    private boolean firstSortReverse = false;
    private boolean secondSortReverse = false;

    private StatusFilterField statusFilterField = StatusFilterField.ALL;

    private int minSpellLevel = Spellbook.MIN_SPELL_LEVEL;
    private int maxSpellLevel = Spellbook.MAX_SPELL_LEVEL;

    private boolean applyFiltersToLists = false;
    private boolean applyFiltersToSearch = false;
    private boolean useTashasExpandedLists = false;

    private boolean yesRitual = true;
    private boolean noRitual = true;
    private boolean yesConcentration = true;
    private boolean noConcentration = true;

    private boolean[] yesComponents = new boolean[]{true, true, true};
    private boolean[] noComponents = new boolean[]{true, true, true};

    private EnumSet<Sourcebook> visibleSourcebooks = EnumSet.allOf(Sourcebook.class);
    private EnumSet<CasterClass> visibleClasses = EnumSet.allOf(CasterClass.class);
    private EnumSet<School> visibleSchools = EnumSet.allOf(School.class);

    private EnumSet<CastingTime.CastingTimeType> visibleCastingTimeTypes = EnumSet.allOf(CastingTime.CastingTimeType.class);
    private int minCastingTimeValue = 0;
    private int maxCastingTimeValue = 24;
    private TimeUnit minCastingTimeUnit = TimeUnit.SECOND;
    private TimeUnit maxCastingTimeUnit = TimeUnit.HOUR;

    private EnumSet<Duration.DurationType> visibleDurationTypes = EnumSet.allOf(Duration.DurationType.class);
    private int minDurationValue = 0;
    private int maxDurationValue = 30;
    private TimeUnit minDurationUnit = TimeUnit.SECOND;
    private TimeUnit maxDurationUnit = TimeUnit.DAY;

    private EnumSet<Range.RangeType> visibleRangeTypes = EnumSet.allOf(Range.RangeType.class);
    private int minRangeValue = 0;
    private int maxRangeValue = 1;
    private LengthUnit minRangeUnit = LengthUnit.FOOT;
    private LengthUnit maxRangeUnit = LengthUnit.MILE;
    
    private static <T> T[] arrayOfSize(Class<T> type, int size) {
        return (T[]) Array.newInstance(type, size);
    }

    private static <T extends Enum<T>> T[] hiddenValues(EnumSet<T> visibleValues, Class<T> type) {
        final T[] allValues = type.getEnumConstants();
        if (allValues == null) { return arrayOfSize(type, 0); }
        final IntFunction<T[]> generator = (int n) -> arrayOfSize(type, n);
        return (T[]) Arrays.stream(type.getEnumConstants()).filter(t -> !visibleValues.contains(t)).toArray(generator);
    }

    private static <T extends Enum<T>> T[] visibleValues(EnumSet<T> visibleValues, Class<T> type) {
        final T[] array = arrayOfSize(type, visibleValues.size());
        return visibleValues.toArray(array);
    }

    private static <T extends Enum<T>> T[] getVisibleValues(boolean b, EnumSet<T> visibleValues, Class<T> type) {
        return b ? visibleValues(visibleValues, type) : hiddenValues(visibleValues, type);
    }

    private static  <T extends Enum<T>> EnumSet<T> createEnumSetFromNames(Class<T> type, String[] names, Function<String,T> nameConstructor) {
        final EnumSet<T> enumSet = EnumSet.noneOf(type);
        for (String name : names) {
            final T t = nameConstructor.apply(name);
            if (t != null) {
                enumSet.add(t);
            }
        }
        return enumSet;
    }

    private static String[] stringArrayFromJSON(JSONArray jsonArray) {
        final String[] strings = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            strings[i] = jsonArray.optString(i, "");
        }
        return strings;
    }

    private static <U extends Unit> void setBoundsFromJSON(JSONObject json, Function<String,U> unitNameConstructor, U defaultUnit, QuadConsumer<Integer, U, Integer, U> setter) {
        final int minValue = json.optInt(minValueKey);
        final int maxValue = json.optInt(maxValueKey);
        U minUnit = unitNameConstructor.apply(json.optString(minUnitKey));
        U maxUnit = unitNameConstructor.apply(json.optString(maxUnitKey));
        if (minUnit == null) { minUnit = defaultUnit; }
        if (maxUnit == null) { maxUnit = defaultUnit; }
        setter.accept(minValue, minUnit, maxValue, maxUnit);
    }

    private static <U extends Unit> JSONObject boundsToJSON(int minValue, int maxValue, U minUnit, U maxUnit) throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(minValueKey, minValue);
        json.put(maxValueKey, maxValue);
        json.put(minUnitKey, minUnit.getInternalName());
        json.put(maxUnitKey, maxUnit.getInternalName());
        return json;
    }

    private <E extends Enum<E>> JSONArray enumSetToJSONArray(EnumSet<E> enumSet, Function<E,String> nameGetter) {
        final JSONArray jsonArray = new JSONArray();
        for (E e : enumSet) {
            jsonArray.put(nameGetter.apply(e));
        }
        return jsonArray;
    }

    // Getters
    SortField getFirstSortField() { return firstSortField; }
    SortField getSecondSortField() { return secondSortField; }
    boolean getFirstSortReverse() { return firstSortReverse; }
    boolean getSecondSortReverse() { return secondSortReverse; }
    StatusFilterField getStatusFilterField() { return statusFilterField; }
    public int getMinSpellLevel() { return minSpellLevel; }
    public int getMaxSpellLevel() { return maxSpellLevel; }
    boolean getApplyFiltersToSearch() { return applyFiltersToSearch; }
    boolean getApplyFiltersToLists() { return applyFiltersToLists; }
    boolean getUseTashasExpandedLists() { return useTashasExpandedLists; }
    boolean getRitualFilter(boolean b) { return b ? yesRitual : noRitual; }
    boolean getConcentrationFilter(boolean b) { return b ? yesConcentration : noConcentration; }
    boolean getVerbalFilter(boolean b) { return b ? yesComponents[VERBAL_INDEX] : noComponents[VERBAL_INDEX]; }
    boolean getSomaticFilter(boolean b) { return b ? yesComponents[SOMATIC_INDEX] : noComponents[SOMATIC_INDEX]; }
    boolean getMaterialFilter(boolean b) { return b ? yesComponents[MATERIAL_INDEX] : noComponents[VERBAL_INDEX]; }
    boolean[] getComponents(boolean b) { return b ? yesComponents.clone() : noComponents.clone(); }
    Sourcebook[] getVisibleSourcebooks(boolean b) { return getVisibleValues(b, visibleSourcebooks, Sourcebook.class); }
    School[] getVisibleSchools(boolean b) { return getVisibleValues(b, visibleSchools, School.class); }
    CasterClass[] getVisibleClasses(boolean b) { return getVisibleValues(b, visibleClasses, CasterClass.class); }
    CastingTime.CastingTimeType[] getVisibleCastingTimeTypes(boolean b) { return getVisibleValues(b, visibleCastingTimeTypes, CastingTime.CastingTimeType.class); }
    Duration.DurationType[] getVisibleDurationTypes(boolean b) { return getVisibleValues(b, visibleDurationTypes, Duration.DurationType.class); }
    Range.RangeType[] getVisibleRangeTypes(boolean b) { return getVisibleValues(b, visibleRangeTypes, Range.RangeType.class); }

    private <T> boolean getVisibility(T item, Collection<T> collection) { return collection.contains(item); }
    boolean getVisibility(Sourcebook sourcebook) { return getVisibility(sourcebook, visibleSourcebooks); }
    boolean getVisibility(School school) { return getVisibility(school, visibleSchools); }
    boolean getVisibility(CasterClass casterClass) { return getVisibility(casterClass, visibleClasses); }
    boolean getVisibility(CastingTime.CastingTimeType castingTimeType) { return getVisibility(castingTimeType, visibleCastingTimeTypes); }
    boolean getVisibility(Duration.DurationType durationType) { return getVisibility(durationType, visibleDurationTypes); }
    boolean getVisibility(Range.RangeType rangeType) { return getVisibility(rangeType, visibleRangeTypes); }

    int getMinCastingTimeValue() { return minCastingTimeValue; }
    int getMaxCastingTimeValue() { return maxCastingTimeValue; }
    TimeUnit getMinCastingTimeUnit() { return minCastingTimeUnit; }
    TimeUnit getMaxCastingTimeUnit() { return maxCastingTimeUnit; }
    int getMinDurationValue() { return minDurationValue; }
    int getMaxDurationValue() { return maxDurationValue; }
    TimeUnit getMinDurationUnit() { return minDurationUnit; }
    TimeUnit getMaxDurationUnit() { return maxDurationUnit; }
    int getMinRangeValue() { return minRangeValue; }
    int getMaxRangeValue() { return maxRangeValue; }
    LengthUnit getMinRangeUnit() { return minRangeUnit; }
    LengthUnit getMaxRangeUnit() { return maxRangeUnit; }

    static private <T extends QuantityType, S> S getQuantityTypeValue(Class<T> type, S castingTimeValue, S durationValue, S rangeValue, S defaultValue) {
        if (type.equals(CastingTime.CastingTimeType.class)) {
            return castingTimeValue;
        } else if (type.equals(Duration.DurationType.class)) {
            return durationValue;
        } else if (type.equals(Range.RangeType.class)) {
            return rangeValue;
        } else {
            return defaultValue;
        }
    }

    <T extends QuantityType> int getMinValue(Class<T> type) { return getQuantityTypeValue(type, minCastingTimeValue, minDurationValue, minRangeValue, 0); }
    <T extends QuantityType> int getMaxValue(Class<T> type) { return getQuantityTypeValue(type, maxCastingTimeValue, maxDurationValue, maxRangeValue, 0); }
    <T extends QuantityType> Unit getMinUnit(Class<T> type) { return getQuantityTypeValue(type, minCastingTimeUnit, minDurationUnit, minRangeUnit, null); }
    <T extends QuantityType> Unit getMaxUnit(Class<T> type) { return getQuantityTypeValue(type, maxCastingTimeUnit, maxDurationUnit, maxRangeUnit, null); }

    static <T extends QuantityType> int getDefaultMinValue(Class<T> type) { return getQuantityTypeValue(type, 0, 0, 0, 0); }
    static <T extends QuantityType> int getDefaultMaxValue(Class<T> type) { return getQuantityTypeValue(type, 24, 30, 1, 1); }
    static <T extends QuantityType> Unit getDefaultMinUnit(Class<T> type) { return getQuantityTypeValue(type, TimeUnit.SECOND, TimeUnit.SECOND, LengthUnit.FOOT, null); }
    static <T extends QuantityType> Unit getDefaultMaxUnit(Class<T> type) { return getQuantityTypeValue(type, TimeUnit.HOUR, TimeUnit.DAY, LengthUnit.MILE, null); }

    <T extends QuantityType> void setRangeBoundsToDefault(Class<T> type) {
        setMinValue(type, getDefaultMinValue(type));
        setMaxValue(type, getDefaultMaxValue(type));
        setMinUnit(type, getDefaultMinUnit(type));
        setMaxUnit(type, getDefaultMaxUnit(type));
    }

    // Checking whether a not a specific filter (or any filter) is set
    boolean filterFavorites() { return (statusFilterField == StatusFilterField.FAVORITES); }
    boolean filterPrepared() { return (statusFilterField == StatusFilterField.PREPARED); }
    boolean filterKnown() { return (statusFilterField == StatusFilterField.KNOWN); }
    boolean isStatusSet() { return (statusFilterField != StatusFilterField.ALL); }

    // Setters
    void setFirstSortField(SortField sf) { firstSortField = sf; }
    void setSecondSortField(SortField sf) { secondSortField = sf; }
    void setFirstSortReverse(boolean b) { firstSortReverse = b; }
    void setSecondSortReverse(boolean b) { secondSortReverse = b; }
    void setSortField(int level, SortField sf) {
        switch (level) {
            case 1:
                setFirstSortField(sf);
            case 2:
                setSecondSortField(sf);
        }
    }
    void setSortReverse(int level, boolean b) {
        switch (level) {
            case 1:
                setFirstSortReverse(b);
            case 2:
                setSecondSortReverse(b);
        }
    }
    void setStatusFilterField(StatusFilterField sff) { statusFilterField = sff; }

    void setMinSpellLevel(int level) { minSpellLevel = level; }
    void setMaxSpellLevel(int level) { maxSpellLevel = level; }
    void setApplyFiltersToLists(boolean b) { applyFiltersToLists = b; }
    void setApplyFiltersToSearch(boolean b) { applyFiltersToSearch = b; }
    void setUseTashasExpandedLists(boolean b) { useTashasExpandedLists = b; }
    void setRitualFilter(boolean tf, boolean b) {
        if (tf) {
            yesRitual = b;
        } else {
            noRitual = b;
        }
    }
    void setConcentrationFilter(boolean tf, boolean b) {
        if (tf) {
            yesConcentration = b;
        } else {
            noConcentration = b;
        }
    }

    void toggleRitualFilter(boolean tf) { setRitualFilter(tf, !getRitualFilter(tf)); }
    void toggleConcentrationFilter(boolean tf) { setConcentrationFilter(tf, !getConcentrationFilter(tf)); }

    void setComponents(boolean tf, boolean[] components) {
        final boolean[] arr = components.clone();
        if (tf) {
            yesComponents = arr;
        } else {
            noComponents = arr;
        }
    }

    private void setFilter(boolean tf, int index, boolean component) {
        if (tf) {
            yesComponents[index] = component;
        } else {
            noComponents[index] = component;
        }
    }
    void setVerbalFilter(boolean tf, boolean component) { setFilter(tf, VERBAL_INDEX, component); }
    void setSomaticFilter(boolean tf, boolean component) { setFilter(tf, SOMATIC_INDEX, component); }
    void setMaterialFilter(boolean tf, boolean component) { setFilter(tf, MATERIAL_INDEX, component); }

    private void toggleFilter(boolean tf, int index) {
        if (tf) {
            yesComponents[index] = !yesComponents[index];
        } else {
            noComponents[index] = !noComponents[index];
        }
    }
    void toggleVerbalFilter(boolean tf) { toggleFilter(tf, VERBAL_INDEX); }
    void toggleSomaticFilter(boolean tf) { toggleFilter(tf, SOMATIC_INDEX); }
    void toggleMaterialFilter(boolean tf) { toggleFilter(tf, MATERIAL_INDEX); }

    private <T> void setVisibility(T item, Collection<T> collection, boolean tf) {
        if (tf) {
            collection.add(item);
        } else {
            collection.remove(item);
        }
    }

    void setVisibility(Sourcebook sourcebook, boolean tf) { setVisibility(sourcebook, visibleSourcebooks, tf); }
    void setVisibility(School school, boolean tf) { setVisibility(school, visibleSchools, tf); }
    void setVisibility(CasterClass casterClass, boolean tf) { setVisibility(casterClass, visibleClasses, tf); }
    void setVisibility(CastingTime.CastingTimeType castingTimeType, boolean tf) { setVisibility(castingTimeType, visibleCastingTimeTypes, tf); }
    void setVisibility(Duration.DurationType durationType, boolean tf) { setVisibility(durationType, visibleDurationTypes, tf); }
    void setVisibility(Range.RangeType rangeType, boolean tf) { setVisibility(rangeType, visibleRangeTypes, tf); }

    private <T> void toggleVisibility(T item, Collection<T> collection) {
        setVisibility(item, collection, !getVisibility(item, collection));
    }
    void toggleVisibility(Sourcebook sourcebook) { toggleVisibility(sourcebook, visibleSourcebooks); }
    void toggleVisibility(School school) { toggleVisibility(school, visibleSchools); }
    void toggleVisibility(CasterClass casterClass) { toggleVisibility(casterClass); }
    void toggleVisibility(CastingTime.CastingTimeType castingTimeType) { toggleVisibility(castingTimeType); }
    void toggleVisibility(Duration.DurationType durationType) { toggleVisibility(durationType); }
    void toggleVisibility(Range.RangeType rangeType) { toggleVisibility(rangeType); }

    private <T extends Enum<T>> void setVisibleItems(Collection<T> items, Consumer<EnumSet<T>> setter) {
        if (items instanceof EnumSet) {
            setter.accept((EnumSet<T>)items);
        } else {
            setter.accept(EnumSet.copyOf(items));
        }
    }

    void setVisibleSourcebooks(Collection<Sourcebook> sourcebooks) { setVisibleItems(sourcebooks, (items) -> { visibleSourcebooks = items; }); }
    void setVisibleSchools(Collection<School> schools) { setVisibleItems(schools, (items) -> { visibleSchools = items; }); }
    void setVisibleClasses(Collection<CasterClass> classes) { setVisibleItems(classes, (items) -> { visibleClasses = items; }); }
    void setVisibleCastingTimeTypes(Collection<CastingTime.CastingTimeType> castingTimeTypes) { setVisibleItems(castingTimeTypes, (items) -> { visibleCastingTimeTypes = items; }); }
    void setVisibleDurationTypes(Collection<Duration.DurationType> durationTypes) { setVisibleItems(durationTypes, (items) -> { visibleDurationTypes = items; }); }
    void setVisibleRangeTypes(Collection<Range.RangeType> rangeTypes) { setVisibleItems(rangeTypes, (items) -> { visibleRangeTypes = items; }); }

    void setCastingTimeBounds(int minValue, TimeUnit minUnit, int maxValue, TimeUnit maxUnit) {
        minCastingTimeValue = minValue; maxCastingTimeValue = maxValue; minCastingTimeUnit = minUnit; maxCastingTimeUnit = maxUnit;
    }
    void setDurationBounds(int minValue, TimeUnit minUnit, int maxValue, TimeUnit maxUnit) {
        minDurationValue = minValue; maxDurationValue = maxValue; minDurationUnit = minUnit; maxDurationUnit = maxUnit;
    }
    void setRangeBounds(int minValue, LengthUnit minUnit, int maxValue, LengthUnit maxUnit) {
        minRangeValue = minValue; maxRangeValue = maxValue; minRangeUnit = minUnit; maxRangeUnit = maxUnit;
    }

    void setMinCastingTimeValue(int minCastingTimeValue) { this.minCastingTimeValue = minCastingTimeValue; }
    void setMaxCastingTimeValue(int maxCastingTimeValue) { this.maxCastingTimeValue = maxCastingTimeValue; }
    void setMinCastingTimeUnit(TimeUnit minCastingTimeUnit) { this.minCastingTimeUnit = minCastingTimeUnit; }
    void setMaxCastingTimeUnit(TimeUnit maxCastingTimeUnit) { this.maxCastingTimeUnit = maxCastingTimeUnit; }
    void setMinDurationValue(int minDurationValue) { this.minDurationValue = minDurationValue; }
    void setMaxDurationValue(int maxDurationValue) { this.maxDurationValue = maxDurationValue; }
    void setMinDurationUnit(TimeUnit minDurationUnit) { this.minDurationUnit = minDurationUnit; }
    void setMaxDurationUnit(TimeUnit maxDurationUnit) { this.maxDurationUnit = maxDurationUnit; }
    void setMinRangeValue(int minRangeValue) { this.minRangeValue = minRangeValue; }
    void setMaxRangeValue(int maxRangeValue) { this.maxRangeValue = maxRangeValue; }
    void setMinRangeUnit(LengthUnit minRangeUnit) { this.minRangeUnit = minRangeUnit; }
    void setMaxRangeUnit(LengthUnit maxRangeUnit) { this.maxRangeUnit = maxRangeUnit; }

    private <T extends QuantityType, S> void setQuantityTypeValue(Class<T> type, S value, Consumer<S> castingTimeSetter, Consumer<S> durationSetter, Consumer<S> rangeSetter) {
        if (type.equals(CastingTime.CastingTimeType.class)) {
            castingTimeSetter.accept(value);
        } else if (type.equals(Duration.DurationType.class)) {
            durationSetter.accept(value);
        } else if (type.equals(Range.RangeType.class)) {
            rangeSetter.accept(value);
        }
    }

    <T extends QuantityType> void setMinValue(Class<T> type, int value) { setQuantityTypeValue(type, value, this::setMinCastingTimeValue, this::setMinDurationValue, this::setMinRangeValue); }
    <T extends QuantityType> void setMaxValue(Class<T> type, int value) { setQuantityTypeValue(type, value, this::setMaxCastingTimeValue, this::setMaxDurationValue, this::setMaxRangeValue); }
    private <T extends QuantityType> void setExtremeUnit(Class<T> type, Unit unit, Consumer<TimeUnit> castingTimeTypeSetter, Consumer<TimeUnit> durationTypeSetter, Consumer<LengthUnit> rangeTypeSetter) {
        if (unit instanceof TimeUnit) {
            final TimeUnit timeUnit = (TimeUnit) unit;
            if (type.equals(CastingTime.CastingTimeType.class)) {
                castingTimeTypeSetter.accept(timeUnit);
            } else if (type.equals(Duration.DurationType.class)) {
                durationTypeSetter.accept(timeUnit);
            }
        } else if (unit instanceof LengthUnit) {
            final LengthUnit lengthUnit = (LengthUnit) unit;
            if (type.equals(Range.RangeType.class)) {
                rangeTypeSetter.accept(lengthUnit);
            }
        }
    }
    <T extends QuantityType> void setMinUnit(Class<T> type, Unit unit) { setExtremeUnit(type, unit, this::setMinCastingTimeUnit, this::setMinDurationUnit, this::setMinRangeUnit); }
    <T extends QuantityType> void setMaxUnit(Class<T> type, Unit unit) { setExtremeUnit(type, unit, this::setMaxCastingTimeUnit, this::setMaxDurationUnit, this::setMaxRangeUnit); }

    SortFilterStatus() { }

    @ParcelConstructor
    SortFilterStatus(SortField firstSortField, SortField secondSortField, boolean firstSortReverse,
                     boolean secondSortReverse, int minSpellLevel, int maxSpellLevel,
                     boolean applyFiltersToSearch, boolean applyFiltersToLists, boolean useTashasExpandedLists,
                     boolean yesRitual, boolean noRitual, boolean yesConcentration, boolean noConcentration,
                     boolean[] yesComponents, boolean[] noComponents, EnumSet<Sourcebook> visibleSourcebooks,
                     EnumSet<School> visibleSchools, EnumSet<CasterClass> visibleClasses,
                     EnumSet<CastingTime.CastingTimeType> visibleCastingTimeTypes,
                     EnumSet<Duration.DurationType> visibleDurationTypes,
                     EnumSet<Range.RangeType> visibleRangeTypes,
                     int minCastingTimeValue, int maxCastingTimeValue, TimeUnit minCastingTimeUnit,
                     TimeUnit maxCastingTimeUnit, int minDurationValue, int maxDurationValue,
                     TimeUnit minDurationUnit, TimeUnit maxDurationUnit, int minRangeValue, int maxRangeValue,
                     LengthUnit minRangeUnit, LengthUnit maxRangeUnit
                     ) {
        this.firstSortField = firstSortField;
        this.secondSortField = secondSortField;
        this.firstSortReverse = firstSortReverse;
        this.secondSortReverse = secondSortReverse;
        this.minSpellLevel = minSpellLevel;
        this.maxSpellLevel = maxSpellLevel;
        this.applyFiltersToSearch = applyFiltersToSearch;
        this.applyFiltersToLists = applyFiltersToLists;
        this.useTashasExpandedLists = useTashasExpandedLists;
        this.yesRitual = yesRitual;
        this.noRitual = noRitual;
        this.yesConcentration = yesConcentration;
        this.noConcentration = noConcentration;
        this.yesComponents = yesComponents.clone();
        this.noComponents = noComponents.clone();
        this.visibleSourcebooks = visibleSourcebooks.clone();
        this.visibleSchools = visibleSchools.clone();
        this.visibleClasses = visibleClasses.clone();
        this.visibleCastingTimeTypes = visibleCastingTimeTypes.clone();
        this.visibleDurationTypes = visibleDurationTypes.clone();
        this.visibleRangeTypes = visibleRangeTypes.clone();
        this.minCastingTimeValue = minCastingTimeValue;
        this.maxCastingTimeValue = maxCastingTimeValue;
        this.minCastingTimeUnit = minCastingTimeUnit;
        this.maxCastingTimeUnit = maxCastingTimeUnit;
        this.minDurationValue = minDurationValue;
        this.maxDurationValue = maxDurationValue;
        this.minDurationUnit = minDurationUnit;
        this.maxDurationUnit = maxDurationUnit;
        this.minRangeValue = minRangeValue;
        this.maxRangeValue = maxRangeValue;
        this.minRangeUnit = minRangeUnit;
        this.maxRangeUnit = maxRangeUnit;
    }

    static SortFilterStatus fromJSON(JSONObject json) throws JSONException {
        final SortFilterStatus status = new SortFilterStatus();

        status.setFirstSortField(json.has(sort1Key) ? SortField.fromInternalName(json.getString(sort1Key)) : SortField.NAME);
        status.setSecondSortField(json.has(sort2Key) ? SortField.fromInternalName(json.getString(sort2Key)) : SortField.NAME);
        status.setFirstSortReverse(json.optBoolean(reverse1Key));
        status.setSecondSortReverse(json.optBoolean(reverse2Key));

        status.setMinSpellLevel(json.optInt(minSpellLevelKey, Spellbook.MIN_SPELL_LEVEL));
        status.setMaxSpellLevel(json.optInt(maxSpellLevelKey, Spellbook.MAX_SPELL_LEVEL));

        status.setApplyFiltersToSearch(json.optBoolean(applyFiltersToSearchKey, false));
        status.setApplyFiltersToLists(json.optBoolean(applyFiltersToSpellListsKey, false));
        status.setUseTashasExpandedLists(json.optBoolean(useTCEExpandedListsKey, false));

        status.setRitualFilter(true, json.optBoolean(ritualKey, true));
        status.setRitualFilter(false, json.optBoolean(notRitualKey, true));
        status.setConcentrationFilter(true, json.optBoolean(concentrationKey, true));
        status.setConcentrationFilter(false, json.optBoolean(notConcentrationKey, true));

        final boolean[] yesComponents = new boolean[3];
        final JSONArray componentsJSON = json.getJSONArray(componentsFiltersKey);
        for (int i = 0; i < componentsJSON.length(); i++) {
            yesComponents[i] = componentsJSON.getBoolean(i);
        }
        status.setComponents(true, yesComponents);

        final boolean[] noComponents = new boolean[3];
        final JSONArray notComponentsJSON = json.getJSONArray(notComponentsFiltersKey);
        for (int i = 0; i < notComponentsJSON.length(); i++) {
            noComponents[i] = notComponentsJSON.getBoolean(i);
        }
        status.setComponents(false, noComponents);

        status.setVisibleSourcebooks(createEnumSetFromNames(Sourcebook.class, stringArrayFromJSON(json.getJSONArray(sourcebooksKey)), Sourcebook::fromInternalName));
        status.setVisibleSchools(createEnumSetFromNames(School.class, stringArrayFromJSON(json.getJSONArray(schoolsKey)), School::fromInternalName));
        status.setVisibleClasses(createEnumSetFromNames(CasterClass.class, stringArrayFromJSON(json.getJSONArray(classesKey)), CasterClass::fromInternalName));
        status.setVisibleCastingTimeTypes(createEnumSetFromNames(CastingTime.CastingTimeType.class, stringArrayFromJSON(json.getJSONArray(castingTimeTypesKey)), CastingTime.CastingTimeType::fromInternalName));
        status.setVisibleDurationTypes(createEnumSetFromNames(Duration.DurationType.class, stringArrayFromJSON(json.getJSONArray(durationTypesKey)), Duration.DurationType::fromInternalName));
        status.setVisibleRangeTypes(createEnumSetFromNames(Range.RangeType.class, stringArrayFromJSON(json.getJSONArray(rangeTypesKey)), Range.RangeType::fromInternalName));

        setBoundsFromJSON(json.getJSONObject(castingTimeBoundsKey), TimeUnit::fromInternalName, TimeUnit.SECOND, status::setCastingTimeBounds);
        setBoundsFromJSON(json.getJSONObject(durationBoundsKey), TimeUnit::fromInternalName, TimeUnit.SECOND, status::setDurationBounds);
        setBoundsFromJSON(json.getJSONObject(rangeBoundsKey), LengthUnit::fromInternalName, LengthUnit.FOOT, status::setRangeBounds);

        return status;
    }

    JSONObject toJSON() throws JSONException {
        final JSONObject json = new JSONObject();

        json.put(sort1Key, firstSortField.getInternalName());
        json.put(sort2Key, secondSortField.getInternalName());
        json.put(reverse1Key, firstSortReverse);
        json.put(reverse2Key, secondSortReverse);

        json.put(minSpellLevelKey, minSpellLevel);
        json.put(maxSpellLevelKey, maxSpellLevel);

        json.put(applyFiltersToSearchKey, applyFiltersToSearch);
        json.put(applyFiltersToSpellListsKey, applyFiltersToLists);
        json.put(useTCEExpandedListsKey, useTashasExpandedLists);

        json.put(ritualKey, yesRitual);
        json.put(notRitualKey, noRitual);
        json.put(concentrationKey, yesConcentration);
        json.put(notConcentrationKey, noConcentration);

        final JSONArray yesComponentsJArr = new JSONArray();
        for (boolean component : yesComponents) {
            yesComponentsJArr.put(component);
        }
        json.put(componentsFiltersKey, yesComponentsJArr);

        final JSONArray noComponentsJArr = new JSONArray();
        for (boolean component : noComponents) {
            noComponentsJArr.put(component);
        }
        json.put(notComponentsFiltersKey, new JSONArray(noComponentsJArr));

        json.put(sourcebooksKey, enumSetToJSONArray(visibleSourcebooks, Sourcebook::getInternalName));
        json.put(classesKey, enumSetToJSONArray(visibleClasses, CasterClass::getInternalName));
        json.put(schoolsKey, enumSetToJSONArray(visibleSchools, School::getInternalName));
        json.put(castingTimeTypesKey, enumSetToJSONArray(visibleCastingTimeTypes, CastingTime.CastingTimeType::getInternalName));
        json.put(durationTypesKey, enumSetToJSONArray(visibleDurationTypes, Duration.DurationType::getInternalName));
        json.put(rangeTypesKey, enumSetToJSONArray(visibleRangeTypes, Range.RangeType::getInternalName));

        json.put(castingTimeBoundsKey, boundsToJSON(minCastingTimeValue, maxCastingTimeValue, minCastingTimeUnit, maxCastingTimeUnit));
        json.put(durationBoundsKey, boundsToJSON(minDurationValue, maxDurationValue, minDurationUnit, maxDurationUnit));
        json.put(rangeBoundsKey, boundsToJSON(minRangeValue, maxRangeValue, minRangeUnit, maxRangeUnit));

        return json;
    }

}
