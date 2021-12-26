package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SortFilterStatus extends BaseObservable implements Named, Parcelable, JSONifiable {

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

    private String name = null;

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

    private Collection<Source> visibleSources = Stream.of(Source.PLAYERS_HANDBOOK, Source.TASHAS_COE, Source.XANATHARS_GTE).collect(Collectors.toCollection(HashSet::new));
    private Collection<CasterClass> visibleClasses = EnumSet.allOf(CasterClass.class);
    private Collection<School> visibleSchools = EnumSet.allOf(School.class);

    private Collection<CastingTime.CastingTimeType> visibleCastingTimeTypes = EnumSet.allOf(CastingTime.CastingTimeType.class);
    private int minCastingTimeValue = 0;
    private int maxCastingTimeValue = 24;
    private TimeUnit minCastingTimeUnit = TimeUnit.SECOND;
    private TimeUnit maxCastingTimeUnit = TimeUnit.HOUR;

    private Collection<Duration.DurationType> visibleDurationTypes = EnumSet.allOf(Duration.DurationType.class);
    private int minDurationValue = 0;
    private int maxDurationValue = 30;
    private TimeUnit minDurationUnit = TimeUnit.SECOND;
    private TimeUnit maxDurationUnit = TimeUnit.DAY;

    private Collection<Range.RangeType> visibleRangeTypes = EnumSet.allOf(Range.RangeType.class);
    private int minRangeValue = 0;
    private int maxRangeValue = 1;
    private LengthUnit minRangeUnit = LengthUnit.FOOT;
    private LengthUnit maxRangeUnit = LengthUnit.MILE;

    protected SortFilterStatus(Parcel in) {
        name = in.readString();
        firstSortField = SpellbookUtils.coalesce(ParcelUtils.readSortField(in), SortField.NAME);
        secondSortField = SpellbookUtils.coalesce(ParcelUtils.readSortField(in), SortField.NAME);
        firstSortReverse = in.readByte() != 0;
        secondSortReverse = in.readByte() != 0;
        statusFilterField = SpellbookUtils.coalesce(ParcelUtils.readStatusFilterField(in), StatusFilterField.ALL);
        minSpellLevel = in.readInt();
        maxSpellLevel = in.readInt();
        applyFiltersToLists = in.readByte() != 0;
        applyFiltersToSearch = in.readByte() != 0;
        useTashasExpandedLists = in.readByte() != 0;
        yesRitual = in.readByte() != 0;
        noRitual = in.readByte() != 0;
        yesConcentration = in.readByte() != 0;
        noConcentration = in.readByte() != 0;
        yesComponents = in.createBooleanArray();
        noComponents = in.createBooleanArray();
        visibleSources = ParcelUtils.readSourceSet(in);
        visibleSchools = ParcelUtils.readSchoolEnumSet(in);
        visibleClasses = ParcelUtils.readCasterClassEnumSet(in);
        visibleCastingTimeTypes = ParcelUtils.readCastingTimeTypeEnumSet(in);
        visibleDurationTypes = ParcelUtils.readDurationTypeEnumSet(in);
        visibleRangeTypes = ParcelUtils.readRangeTypeEnumSet(in);
        minCastingTimeValue = in.readInt();
        maxCastingTimeValue = in.readInt();
        minCastingTimeUnit = ParcelUtils.readTimeUnit(in);
        maxCastingTimeUnit = ParcelUtils.readTimeUnit(in);
        minDurationValue = in.readInt();
        maxDurationValue = in.readInt();
        minDurationUnit = ParcelUtils.readTimeUnit(in);
        maxDurationUnit = ParcelUtils.readTimeUnit(in);
        minRangeValue = in.readInt();
        maxRangeValue = in.readInt();
        minRangeUnit = ParcelUtils.readLengthUnit(in);
        maxRangeUnit = ParcelUtils.readLengthUnit(in);
    }

    public static final Creator<SortFilterStatus> CREATOR = new Creator<SortFilterStatus>() {
        @Override
        public SortFilterStatus createFromParcel(Parcel in) {
            return new SortFilterStatus(in);
        }

        @Override
        public SortFilterStatus[] newArray(int size) {
            return new SortFilterStatus[size];
        }
    };

    SortFilterStatus duplicate() {
        final Parcel parcel = Parcel.obtain();
        this.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        final SortFilterStatus sfs = new SortFilterStatus(parcel);
        parcel.recycle();
        return sfs;
    }

    private static <T> T[] arrayOfSize(Class<T> type, int size) {
        return (T[]) Array.newInstance(type, size);
    }

    private static <T> T[] hiddenValues(Collection<T> visibleValues, T[] allValues, Class<T> type) {
        if (allValues == null) { return arrayOfSize(type, 0); }
        final IntFunction<T[]> generator = (int n) -> arrayOfSize(type, n);
        return Arrays.stream(allValues).filter((T t) -> !visibleValues.contains(t)).toArray(generator);
    }

    private static <T extends Enum<T>> T[] hiddenValues(Collection<T> visibleValues, Class<T> type) {
        return hiddenValues(visibleValues, type.getEnumConstants(), type);
    }

    private static <T> T[] getVisibleValues(boolean b, Collection<T> visibleValues, T[] allValues, Class<T> type) {
        return b ? visibleValues.toArray(arrayOfSize(type, visibleValues.size()) ): hiddenValues(visibleValues, allValues, type);
    }

    private static <T extends Enum<T>> T[] getVisibleValues(boolean b, Collection<T> visibleValues, Class<T> type) {
        return getVisibleValues(b, visibleValues, type.getEnumConstants(), type);
    }

    private static <T> Set<T> createSetFromNames(Class<T> type, String[] names, Function<String,T> nameConstructor) {
        return Arrays.stream(names).map(nameConstructor).collect(Collectors.toSet());

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

    private <T> JSONArray collectionToJSONArray(Collection<T> collection, Function<T,String> nameGetter) {
        final JSONArray jsonArray = new JSONArray();
        for (T t : collection) {
            jsonArray.put(nameGetter.apply(t));
        }
        return jsonArray;
    }

    // Getters
    @Bindable public String getName() { return name; }
    @Bindable SortField getFirstSortField() { return firstSortField; }
    @Bindable SortField getSecondSortField() { return secondSortField; }
    @Bindable boolean getFirstSortReverse() { return firstSortReverse; }
    @Bindable boolean getSecondSortReverse() { return secondSortReverse; }
    @Bindable StatusFilterField getStatusFilterField() { return statusFilterField; }
    @Bindable public int getMinSpellLevel() { return minSpellLevel; }
    @Bindable public int getMaxSpellLevel() { return maxSpellLevel; }
    @Bindable boolean getApplyFiltersToSearch() { return applyFiltersToSearch; }
    @Bindable boolean getApplyFiltersToLists() { return applyFiltersToLists; }
    @Bindable boolean getUseTashasExpandedLists() { return useTashasExpandedLists; }
    @Bindable boolean getRitualFilter() { return yesRitual; }
    @Bindable boolean getNotRitualFilter() { return noRitual; }
    boolean getRitualFilter(boolean b) { return b ? yesRitual : noRitual; }
    @Bindable boolean getConcentrationFilter() { return yesConcentration; }
    @Bindable boolean getNotConcentrationFilter() { return noConcentration; }
    @Bindable boolean[] getComponents() { return yesComponents; }
    @Bindable boolean[] getNotComponents() { return noComponents; }
    boolean getConcentrationFilter(boolean b) { return b ? yesConcentration : noConcentration; }
    boolean getVerbalFilter(boolean b) { return b ? yesComponents[VERBAL_INDEX] : noComponents[VERBAL_INDEX]; }
    boolean getSomaticFilter(boolean b) { return b ? yesComponents[SOMATIC_INDEX] : noComponents[SOMATIC_INDEX]; }
    boolean getMaterialFilter(boolean b) { return b ? yesComponents[MATERIAL_INDEX] : noComponents[VERBAL_INDEX]; }
    boolean[] getComponents(boolean b) { return b ? yesComponents.clone() : noComponents.clone(); }
    Source[] getVisibleSourcebooks(boolean b) { return getVisibleValues(b, visibleSources, Source.values(), Source.class); }
    School[] getVisibleSchools(boolean b) { return getVisibleValues(b, visibleSchools, School.class); }
    CasterClass[] getVisibleClasses(boolean b) { return getVisibleValues(b, visibleClasses, CasterClass.class); }
    CastingTime.CastingTimeType[] getVisibleCastingTimeTypes(boolean b) { return getVisibleValues(b, visibleCastingTimeTypes, CastingTime.CastingTimeType.class); }
    Duration.DurationType[] getVisibleDurationTypes(boolean b) { return getVisibleValues(b, visibleDurationTypes, Duration.DurationType.class); }
    Range.RangeType[] getVisibleRangeTypes(boolean b) { return getVisibleValues(b, visibleRangeTypes, Range.RangeType.class); }

    // This is a dummy field that we 'update' (or tell BR that we did)
    // whenever any visibility is changed
    private final Void visibilityFlag = null;
    @Bindable private Void getVisibilityFlag() { return visibilityFlag; }

    private <T> boolean getVisibility(T item, Collection<T> collection) { return collection.contains(item); }
    boolean getVisibility(Source source) { return getVisibility(source, visibleSources); }
    boolean getVisibility(School school) { return getVisibility(school, visibleSchools); }
    boolean getVisibility(CasterClass casterClass) { return getVisibility(casterClass, visibleClasses); }
    boolean getVisibility(CastingTime.CastingTimeType castingTimeType) { return getVisibility(castingTimeType, visibleCastingTimeTypes); }
    boolean getVisibility(Duration.DurationType durationType) { return getVisibility(durationType, visibleDurationTypes); }
    boolean getVisibility(Range.RangeType rangeType) { return getVisibility(rangeType, visibleRangeTypes); }
    public <T extends NameDisplayable> boolean getVisibility(T item) {
        if (item instanceof Source) {
            return getVisibility((Source)item);
        } else if (item instanceof School) {
            return getVisibility((School) item);
        } else if (item instanceof CasterClass) {
            return getVisibility((CasterClass) item);
        } else if (item instanceof CastingTime.CastingTimeType) {
            return getVisibility((CastingTime.CastingTimeType)item);
        } else if (item instanceof Duration.DurationType) {
            return getVisibility((Duration.DurationType)item);
        } else if (item instanceof Range.RangeType) {
            return getVisibility((Range.RangeType)item);
        } else {
            return false;
        }
    }

    @Bindable int getMinCastingTimeValue() { return minCastingTimeValue; }
    @Bindable int getMaxCastingTimeValue() { return maxCastingTimeValue; }
    @Bindable TimeUnit getMinCastingTimeUnit() { return minCastingTimeUnit; }
    @Bindable TimeUnit getMaxCastingTimeUnit() { return maxCastingTimeUnit; }
    @Bindable int getMinDurationValue() { return minDurationValue; }
    @Bindable int getMaxDurationValue() { return maxDurationValue; }
    @Bindable TimeUnit getMinDurationUnit() { return minDurationUnit; }
    @Bindable TimeUnit getMaxDurationUnit() { return maxDurationUnit; }
    @Bindable int getMinRangeValue() { return minRangeValue; }
    @Bindable int getMaxRangeValue() { return maxRangeValue; }
    @Bindable LengthUnit getMinRangeUnit() { return minRangeUnit; }
    @Bindable LengthUnit getMaxRangeUnit() { return maxRangeUnit; }

    // We use getters (rather than just values themselves) for this setup
    // so that the bindable functions are called
    private static <T extends QuantityType, S> S getQuantityTypeValue(Class<T> type,
                                                               Supplier<S> castingTimeGetter,
                                                               Supplier<S> durationGetter,
                                                               Supplier<S> rangeGetter,
                                                               S defaultValue) {
        if (type.equals(CastingTime.CastingTimeType.class)) {
            return castingTimeGetter.get();
        } else if (type.equals(Duration.DurationType.class)) {
            return durationGetter.get();
        } else if (type.equals(Range.RangeType.class)) {
            return rangeGetter.get();
        } else {
            return defaultValue;
        }
    }

    // For defaults, we don't need the binding
    private static <T extends QuantityType, S> S getDefaultQuantityTypeValue(Class<T> type,
                                                                             S castingTimeValue,
                                                                             S durationValue,
                                                                             S rangeValue,
                                                                             S defaultValue) {
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

    <T extends QuantityType> int getMinValue(Class<T> type) { return getQuantityTypeValue(type, this::getMinCastingTimeValue, this::getMinDurationValue, this::getMinRangeValue, 0); }
    <T extends QuantityType> int getMaxValue(Class<T> type) { return getQuantityTypeValue(type, this::getMaxCastingTimeValue, this::getMaxDurationValue, this::getMaxRangeValue, 0); }
    <T extends QuantityType> Unit getMinUnit(Class<T> type) { return getQuantityTypeValue(type, this::getMinCastingTimeUnit, this::getMinDurationUnit, this::getMinRangeUnit, null); }
    <T extends QuantityType> Unit getMaxUnit(Class<T> type) { return getQuantityTypeValue(type, this::getMaxCastingTimeUnit, this::getMaxDurationUnit, this::getMaxRangeUnit, null); }

    <T extends QuantityType> boolean getSpanningTypeVisible(Class<T> type) { return getQuantityTypeValue(type, () -> this.getVisibility(CastingTime.CastingTimeType.TIME), () -> this.getVisibility(Duration.DurationType.SPANNING), () -> getVisibility(Range.RangeType.RANGED), false); }
    <T extends QuantityType> int getSpanningTypeVisibility(Class<T> type) { return getSpanningTypeVisible(type) ? View.VISIBLE : View.GONE; }

    static <T extends QuantityType> int getDefaultMinValue(Class<T> type) { return getDefaultQuantityTypeValue(type, 0, 0, 0, 0); }
    static <T extends QuantityType> int getDefaultMaxValue(Class<T> type) { return getDefaultQuantityTypeValue(type, 24, 30, 1, 1); }
    static <T extends QuantityType> Unit getDefaultMinUnit(Class<T> type) { return getDefaultQuantityTypeValue(type, TimeUnit.SECOND, TimeUnit.SECOND, LengthUnit.FOOT, null); }
    static <T extends QuantityType> Unit getDefaultMaxUnit(Class<T> type) { return getDefaultQuantityTypeValue(type, TimeUnit.HOUR, TimeUnit.DAY, LengthUnit.MILE, null); }

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
    public void setName(String name) { this.name = name; notifyPropertyChanged(BR.name); }
    void setFirstSortField(SortField sf) { firstSortField = sf; notifyPropertyChanged(BR.firstSortField); }
    void setSecondSortField(SortField sf) { secondSortField = sf; notifyPropertyChanged(BR.secondSortField); }
    void setFirstSortReverse(boolean b) { firstSortReverse = b; notifyPropertyChanged(BR.firstSortReverse); }
    void setSecondSortReverse(boolean b) { secondSortReverse = b; notifyPropertyChanged(BR.secondSortReverse); }
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
    void setStatusFilterField(StatusFilterField sff) { statusFilterField = sff; notifyPropertyChanged(BR.statusFilterField); }

    void setMinSpellLevel(int level) { minSpellLevel = level; notifyPropertyChanged(BR.minSpellLevel); }
    void setMaxSpellLevel(int level) { maxSpellLevel = level; notifyPropertyChanged(BR.maxSpellLevel); }
    void setApplyFiltersToLists(boolean b) { applyFiltersToLists = b; notifyPropertyChanged(BR.applyFiltersToLists); }
    void setApplyFiltersToSearch(boolean b) { applyFiltersToSearch = b; notifyPropertyChanged(BR.applyFiltersToSearch); }
    void setUseTashasExpandedLists(boolean b) { useTashasExpandedLists = b; notifyPropertyChanged(BR.useTashasExpandedLists); }
    void setRitualFilter(boolean tf, boolean b) {
        if (tf) {
            yesRitual = b;
            notifyPropertyChanged(BR.ritualFilter);
        } else {
            noRitual = b;
            notifyPropertyChanged(BR.notRitualFilter);
        }
    }
    void setConcentrationFilter(boolean tf, boolean b) {
        if (tf) {
            yesConcentration = b;
            notifyPropertyChanged(BR.concentrationFilter);
        } else {
            noConcentration = b;
            notifyPropertyChanged(BR.notConcentrationFilter);
        }
    }

    void toggleRitualFilter(boolean tf) { setRitualFilter(tf, !getRitualFilter(tf)); }
    void toggleConcentrationFilter(boolean tf) { setConcentrationFilter(tf, !getConcentrationFilter(tf)); }

    void setComponents(boolean tf, boolean[] components) {
        final boolean[] arr = components.clone();
        if (tf) {
            yesComponents = arr;
            notifyPropertyChanged(BR.components);
        } else {
            noComponents = arr;
            notifyPropertyChanged(BR.notComponents);
        }
    }

    private void setFilter(boolean tf, int index, boolean component) {
        if (tf) {
            yesComponents[index] = component;
            notifyPropertyChanged(BR.components);
        } else {
            noComponents[index] = component;
            notifyPropertyChanged(BR.notComponents);
        }
    }
    void setVerbalFilter(boolean tf, boolean component) { setFilter(tf, VERBAL_INDEX, component); }
    void setSomaticFilter(boolean tf, boolean component) { setFilter(tf, SOMATIC_INDEX, component); }
    void setMaterialFilter(boolean tf, boolean component) { setFilter(tf, MATERIAL_INDEX, component); }

    private void toggleFilter(boolean tf, int index) {
        if (tf) {
            yesComponents[index] = !yesComponents[index];
            notifyPropertyChanged(BR.components);
        } else {
            noComponents[index] = !noComponents[index];
            notifyPropertyChanged(BR.notComponents);
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
        notifyPropertyChanged(BR.visibilityFlag);
    }

    void setVisibility(Source source, boolean tf) { setVisibility(source, visibleSources, tf); }
    void setVisibility(School school, boolean tf) { setVisibility(school, visibleSchools, tf); }
    void setVisibility(CasterClass casterClass, boolean tf) { setVisibility(casterClass, visibleClasses, tf); }
    void setVisibility(CastingTime.CastingTimeType castingTimeType, boolean tf) { setVisibility(castingTimeType, visibleCastingTimeTypes, tf); }
    void setVisibility(Duration.DurationType durationType, boolean tf) { setVisibility(durationType, visibleDurationTypes, tf); }
    void setVisibility(Range.RangeType rangeType, boolean tf) { setVisibility(rangeType, visibleRangeTypes, tf); }
    <T extends NameDisplayable> void setVisibility(T item, boolean tf) {
        if (item instanceof Source) {
            setVisibility((Source)item, tf);
        } else if (item instanceof School) {
            setVisibility((School) item, tf);
        } else if (item instanceof CasterClass) {
            setVisibility((CasterClass) item, tf);
        } else if (item instanceof CastingTime.CastingTimeType) {
            setVisibility((CastingTime.CastingTimeType)item, tf);
        } else if (item instanceof Duration.DurationType) {
            setVisibility((Duration.DurationType)item, tf);
        } else if (item instanceof Range.RangeType) {
            setVisibility((Range.RangeType)item, tf);
        }
    }

    <T extends NameDisplayable> void toggleVisibility(T item) {
        setVisibility(item, !getVisibility(item));
    }

    private <E extends Enum<E>> void setVisibleEnumItems(Collection<E> items, Consumer<Collection<E>> setter) {
        setter.accept(EnumSet.copyOf(items));
    }

    private <T> void setVisibleItems(Collection<T> items, Consumer<Collection<T>> setter) {
        setter.accept(new HashSet<>(items));
    }

    void setVisibleSourcebooks(Collection<Source> sources) { setVisibleItems(sources, (items) -> { visibleSources = items; }); }
    void setVisibleSchools(Collection<School> schools) { setVisibleEnumItems(schools, (items) -> { visibleSchools = items; }); }
    void setVisibleClasses(Collection<CasterClass> classes) { setVisibleEnumItems(classes, (items) -> { visibleClasses = items; }); }
    void setVisibleCastingTimeTypes(Collection<CastingTime.CastingTimeType> castingTimeTypes) { setVisibleEnumItems(castingTimeTypes, (items) -> { visibleCastingTimeTypes = items; }); }
    void setVisibleDurationTypes(Collection<Duration.DurationType> durationTypes) { setVisibleEnumItems(durationTypes, (items) -> { visibleDurationTypes = items; }); }
    void setVisibleRangeTypes(Collection<Range.RangeType> rangeTypes) { setVisibleEnumItems(rangeTypes, (items) -> { visibleRangeTypes = items; }); }

    void setCastingTimeBounds(int minValue, TimeUnit minUnit, int maxValue, TimeUnit maxUnit) {
        minCastingTimeValue = minValue; maxCastingTimeValue = maxValue; minCastingTimeUnit = minUnit; maxCastingTimeUnit = maxUnit;
    }
    void setDurationBounds(int minValue, TimeUnit minUnit, int maxValue, TimeUnit maxUnit) {
        minDurationValue = minValue; maxDurationValue = maxValue; minDurationUnit = minUnit; maxDurationUnit = maxUnit;
    }
    void setRangeBounds(int minValue, LengthUnit minUnit, int maxValue, LengthUnit maxUnit) {
        minRangeValue = minValue; maxRangeValue = maxValue; minRangeUnit = minUnit; maxRangeUnit = maxUnit;
    }

    void setMinCastingTimeValue(int minCastingTimeValue) {
        this.minCastingTimeValue = minCastingTimeValue;
        notifyPropertyChanged(BR.minCastingTimeValue);
    }
    void setMaxCastingTimeValue(int maxCastingTimeValue) {
        this.maxCastingTimeValue = maxCastingTimeValue;
        notifyPropertyChanged(BR.maxCastingTimeValue);
    }
    void setMinCastingTimeUnit(TimeUnit minCastingTimeUnit) {
        this.minCastingTimeUnit = minCastingTimeUnit;
        notifyPropertyChanged(BR.minCastingTimeUnit);
    }
    void setMaxCastingTimeUnit(TimeUnit maxCastingTimeUnit) {
        this.maxCastingTimeUnit = maxCastingTimeUnit;
        notifyPropertyChanged(BR.maxCastingTimeUnit);
    }
    void setMinDurationValue(int minDurationValue) {
        this.minDurationValue = minDurationValue;
        notifyPropertyChanged(BR.minDurationValue);
    }
    void setMaxDurationValue(int maxDurationValue) {
        this.maxDurationValue = maxDurationValue;
        notifyPropertyChanged(BR.maxDurationValue);
    }
    void setMinDurationUnit(TimeUnit minDurationUnit) {
        this.minDurationUnit = minDurationUnit;
        notifyPropertyChanged(BR.minDurationUnit);
    }
    void setMaxDurationUnit(TimeUnit maxDurationUnit) {
        this.maxDurationUnit = maxDurationUnit;
        notifyPropertyChanged(BR.maxDurationUnit);
    }
    void setMinRangeValue(int minRangeValue) {
        this.minRangeValue = minRangeValue;
        notifyPropertyChanged(BR.minRangeValue);
    }
    void setMaxRangeValue(int maxRangeValue) {
        this.maxRangeValue = maxRangeValue;
        notifyPropertyChanged(BR.maxRangeValue);
    }
    void setMinRangeUnit(LengthUnit minRangeUnit) {
        this.minRangeUnit = minRangeUnit;
        notifyPropertyChanged(BR.minRangeUnit);
    }
    void setMaxRangeUnit(LengthUnit maxRangeUnit) {
        this.maxRangeUnit = maxRangeUnit;
        notifyPropertyChanged(BR.maxRangeUnit);
    }

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

    SortFilterStatus(StatusFilterField statusFilterField, SortField firstSortField, SortField secondSortField, boolean firstSortReverse,
                     boolean secondSortReverse, int minSpellLevel, int maxSpellLevel,
                     boolean applyFiltersToSearch, boolean applyFiltersToLists, boolean useTashasExpandedLists,
                     boolean yesRitual, boolean noRitual, boolean yesConcentration, boolean noConcentration,
                     boolean[] yesComponents, boolean[] noComponents, Collection<Source> visibleSources,
                     Collection<School> visibleSchools, Collection<CasterClass> visibleClasses,
                     Collection<CastingTime.CastingTimeType> visibleCastingTimeTypes,
                     Collection<Duration.DurationType> visibleDurationTypes,
                     Collection<Range.RangeType> visibleRangeTypes,
                     int minCastingTimeValue, int maxCastingTimeValue, TimeUnit minCastingTimeUnit,
                     TimeUnit maxCastingTimeUnit, int minDurationValue, int maxDurationValue,
                     TimeUnit minDurationUnit, TimeUnit maxDurationUnit, int minRangeValue, int maxRangeValue,
                     LengthUnit minRangeUnit, LengthUnit maxRangeUnit
                     ) {
        this.statusFilterField = statusFilterField;
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
        this.visibleSources = new HashSet<>(visibleSources);
        this.visibleSchools = EnumSet.copyOf(visibleSchools);
        this.visibleClasses = EnumSet.copyOf(visibleClasses);
        this.visibleCastingTimeTypes = EnumSet.copyOf(visibleCastingTimeTypes);
        this.visibleDurationTypes = EnumSet.copyOf(visibleDurationTypes);
        this.visibleRangeTypes = EnumSet.copyOf(visibleRangeTypes);
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

        status.setStatusFilterField(json.has(statusFilterKey) ? StatusFilterField.fromDisplayName(json.getString(statusFilterKey)) : StatusFilterField.ALL);

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

        status.setVisibleSourcebooks(createSetFromNames(Source.class, stringArrayFromJSON(json.getJSONArray(sourcebooksKey)), Source::fromInternalName));
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

    public JSONObject toJSON() throws JSONException {
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
        json.put(notComponentsFiltersKey, noComponentsJArr);

        json.put(sourcebooksKey, collectionToJSONArray(visibleSources, Source::getInternalName));
        json.put(classesKey, collectionToJSONArray(visibleClasses, CasterClass::getInternalName));
        json.put(schoolsKey, collectionToJSONArray(visibleSchools, School::getInternalName));
        json.put(castingTimeTypesKey, collectionToJSONArray(visibleCastingTimeTypes, CastingTime.CastingTimeType::getInternalName));
        json.put(durationTypesKey, collectionToJSONArray(visibleDurationTypes, Duration.DurationType::getInternalName));
        json.put(rangeTypesKey, collectionToJSONArray(visibleRangeTypes, Range.RangeType::getInternalName));

        json.put(castingTimeBoundsKey, boundsToJSON(minCastingTimeValue, maxCastingTimeValue, minCastingTimeUnit, maxCastingTimeUnit));
        json.put(durationBoundsKey, boundsToJSON(minDurationValue, maxDurationValue, minDurationUnit, maxDurationUnit));
        json.put(rangeBoundsKey, boundsToJSON(minRangeValue, maxRangeValue, minRangeUnit, maxRangeUnit));

        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        ParcelUtils.writeSortField(parcel, firstSortField);
        ParcelUtils.writeSortField(parcel, secondSortField);
        parcel.writeByte((byte) (firstSortReverse ? 1 : 0));
        parcel.writeByte((byte) (secondSortReverse ? 1 : 0));
        ParcelUtils.writeStatusFilterField(parcel, statusFilterField);
        parcel.writeInt(minSpellLevel);
        parcel.writeInt(maxSpellLevel);
        parcel.writeByte((byte) (applyFiltersToLists ? 1 : 0));
        parcel.writeByte((byte) (applyFiltersToSearch ? 1 : 0));
        parcel.writeByte((byte) (useTashasExpandedLists ? 1 : 0));
        parcel.writeByte((byte) (yesRitual ? 1 : 0));
        parcel.writeByte((byte) (noRitual ? 1 : 0));
        parcel.writeByte((byte) (yesConcentration ? 1 : 0));
        parcel.writeByte((byte) (noConcentration ? 1 : 0));
        parcel.writeBooleanArray(yesComponents);
        parcel.writeBooleanArray(noComponents);
        ParcelUtils.writeSourcebookCollection(parcel, visibleSources);
        ParcelUtils.writeSchoolCollection(parcel, visibleSchools);
        ParcelUtils.writeCasterClassCollection(parcel, visibleClasses);
        ParcelUtils.writeCastingTimeTypeCollection(parcel, visibleCastingTimeTypes);
        ParcelUtils.writeDurationTypeCollection(parcel, visibleDurationTypes);
        ParcelUtils.writeRangeTypeCollection(parcel, visibleRangeTypes);
        parcel.writeInt(minCastingTimeValue);
        parcel.writeInt(maxCastingTimeValue);
        ParcelUtils.writeTimeUnit(parcel, minCastingTimeUnit);
        ParcelUtils.writeTimeUnit(parcel, maxCastingTimeUnit);
        parcel.writeInt(minDurationValue);
        parcel.writeInt(maxDurationValue);
        ParcelUtils.writeTimeUnit(parcel, minDurationUnit);
        ParcelUtils.writeTimeUnit(parcel, maxDurationUnit);
        parcel.writeInt(minRangeValue);
        parcel.writeInt(maxRangeValue);
        ParcelUtils.writeLengthUnit(parcel, minRangeUnit);
        ParcelUtils.writeLengthUnit(parcel, maxRangeUnit);
    }

}
