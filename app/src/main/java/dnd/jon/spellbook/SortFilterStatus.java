package dnd.jon.spellbook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;

@Parcel
class SortFilterStatus {

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

    private SortField firstSortField;
    private SortField secondSortField;
    private boolean firstSortReverse;
    private boolean secondSortReverse;

    private int minSpellLevel;
    private int maxSpellLevel;

    private boolean applyFiltersToLists;
    private boolean applyFiltersToSearch;
    private boolean useTashasExpandedLists;

    private boolean yesRitual;
    private boolean noRitual;
    private boolean yesConcentration;
    private boolean noConcentration;

    private boolean[] yesComponents;
    private boolean[] noComponents;

    private EnumSet<Sourcebook> visibleSourcebooks;
    private EnumSet<CasterClass> visibleClasses;
    private EnumSet<School> visibleSchools;

    private EnumSet<CastingTime.CastingTimeType> visibleCastingTimeTypes;
    private int minCastingTimeValue;
    private int maxCastingTimeValue;
    private TimeUnit minCastingTimeUnit;
    private TimeUnit maxCastingTimeUnit;

    private EnumSet<Duration.DurationType> visibleDurationTypes;
    private int minDurationValue;
    private int maxDurationValue;
    private TimeUnit minDurationUnit;
    private TimeUnit maxDurationUnit;

    private EnumSet<Range.RangeType> visibleRangeTypes;
    private int minRangeValue;
    private int maxRangeValue;
    private LengthUnit minRangeUnit;
    private LengthUnit maxRangeUnit;

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

    // Getters
    SortField getFirstSortField() { return firstSortField; }
    SortField getSecondSortField() { return secondSortField; }
    boolean getFirstSortReverse() { return firstSortReverse; }
    boolean getSecondSortReverse() { return secondSortReverse; }
    int getMinSpellLevel() { return minSpellLevel; }
    int getMaxSpellLevel() { return maxSpellLevel; }
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

    boolean getVisibility(Sourcebook sourcebook) { return visibleSourcebooks.contains(sourcebook); }
    boolean getVisibility(School school) { return visibleSchools.contains(school); }
    boolean getVisibility(CasterClass casterClass) { return visibleClasses.contains(casterClass); }
    boolean getVisibility(CastingTime.CastingTimeType castingTimeType) { return visibleCastingTimeTypes.contains(castingTimeType); }
    boolean getVisibility(Duration.DurationType durationType) { return visibleDurationTypes.contains(durationType); }
    boolean getVisibility(Range.RangeType rangeType) { return visibleRangeTypes.contains(rangeType); }

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

    private <T extends Enum<T> & QuantityType, S> S getQuantityTypeValue(Class<T> type, S castingTimeValue, S durationValue, S rangeValue, S defaultValue) {
        if (type == CastingTime.CastingTimeType.class) {
            return castingTimeValue;
        } else if (type == Duration.DurationType.class) {
            return durationValue;
        } else if (type == Range.RangeType.class) {
            return rangeValue;
        } else {
            return defaultValue;
        }
    }

    <T extends Enum<T> & QuantityType> int getMinValue(Class<T> type) { return getQuantityTypeValue(type, minCastingTimeValue, minDurationValue, minRangeValue, 0); }
    <T extends Enum<T> & QuantityType> int getMaxValue(Class<T> type) { return getQuantityTypeValue(type, maxCastingTimeValue, maxDurationValue, maxRangeValue, 0); }
    <T extends Enum<T> & QuantityType> Unit getMinUnit(Class<T> type) { return getQuantityTypeValue(type, minCastingTimeUnit, minDurationUnit, minRangeUnit, null); }
    <T extends Enum<T> & QuantityType> Unit getMaxUnit(Class<T> type) { return getQuantityTypeValue(type, maxCastingTimeUnit, maxDurationUnit, maxRangeUnit, null); }

    // Setters
    void setFirstSortField(SortField sf) { firstSortField = sf; }
    void setSecondSortField(SortField sf) { secondSortField = sf; }
    void setFirstSortReverse(boolean b) { firstSortReverse = b; }
    void setSecondSortReverse(boolean b) { secondSortReverse = b; }
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

    void setComponents(boolean tf, boolean[] components) {
        final boolean[] arr = components.clone();
        if (tf) {
            yesComponents = arr;
        } else {
            noComponents = arr;
        }
    }

    private void setComponent(boolean tf, int index, boolean component) {
        if (tf) {
            yesComponents[index] = component;
        } else {
            noComponents[index] = component;
        }
    }

    void setVerbalComponent(boolean tf, boolean component) { setComponent(tf, VERBAL_INDEX, component); }
    void setSomaticComponent(boolean tf, boolean component) { setComponent(tf, SOMATIC_INDEX, component); }
    void setMaterialComponent(boolean tf, boolean component) { setComponent(tf, MATERIAL_INDEX, component); }

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

    private <T extends Enum<T> & QuantityType, S> void setQuantityTypeValue(Class<T> type, S value, Consumer<S> castingTimeSetter, Consumer<S> durationSetter, Consumer<S> rangeSetter) {
        if (type == CastingTime.CastingTimeType.class) {
            castingTimeSetter.accept(value);
        } else if (type == Duration.DurationType.class) {
            durationSetter.accept(value);
        } else if (type == Range.RangeType.class) {
            rangeSetter.accept(value);
        }
    }

    <T extends Enum<T> & QuantityType> void setMinValue(Class<T> type, int value) { setQuantityTypeValue(type, value, this::setMinCastingTimeValue, this::setMinDurationValue, this::setMinRangeValue); }
    <T extends Enum<T> & QuantityType> void setMaxValue(Class<T> type, int value) { setQuantityTypeValue(type, value, this::setMaxCastingTimeValue, this::setMaxDurationValue, this::setMaxRangeValue); }


    private SortFilterStatus() {}

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

}
