package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.javatuples.Quintet;
import org.javatuples.Sextet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import dnd.jon.spellbook.CastingTime.CastingTimeType;
import dnd.jon.spellbook.Duration.DurationType;
import dnd.jon.spellbook.Range.RangeType;

import org.apache.commons.lang3.SerializationUtils;

public class CharacterProfile extends BaseObservable implements Named, Parcelable, JSONifiable {

    // Member values
    private String name;
    private final SpellFilterStatus spellFilterStatus;
    private SortFilterStatus sortFilterStatus;
    private final SpellSlotStatus spellSlotStatus;

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
    private static final String spellFilterStatusKey = "SpellFilterStatus";
    private static final String sortFilterStatusKey = "SortFilterStatus";
    private static final String spellSlotStatusKey = "SpellSlotStatus";

    private static final Version V2_10_0 = new Version(2,10,0);
    private static final Version V2_11_0 = new Version(2,11,0);
    private static final Version V3_0_0 = new Version(3,0,0);

    private static final Map<Class<?>, Quintet<Boolean,Function<Object,Boolean>, Collection<?>, String, String>> enumInfo = new HashMap<Class<?>, Quintet<Boolean,Function<Object,Boolean>, Collection<?>,String,String>>() {{
       put(Source.class, new Quintet<>(true, (sb) -> sb == Source.PLAYERS_HANDBOOK, Source.collection(), "HiddenSourcebooks",""));
       put(CasterClass.class, new Quintet<>(false, (x) -> true, null, "HiddenCasters", ""));
       put(School.class, new Quintet<>(false, (x) -> true, null, "HiddenSchools", ""));
       put(CastingTimeType.class, new Quintet<>(false, (x) -> true, null, "HiddenCastingTimeTypes", "CastingTimeFilters"));
       put(DurationType.class, new Quintet<>(false, (x) -> true, null, "HiddenDurationTypes", "DurationFilters"));
       put(RangeType.class, new Quintet<>(false, (x) -> true, null, "HiddenRangeTypes", "RangeFilters"));
    }};
    private static final Map<String, Class<? extends QuantityType>> keyToQuantityTypeMap = new HashMap<>();
    static {
        for (Class<?> cls : enumInfo.keySet()) {
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
    private static final Map<Class<?>, Map<?, Boolean>> defaultVisibilitiesMap = new HashMap<>();
    static {
        for (Map.Entry<Class<?>, Quintet<Boolean, Function<Object,Boolean>, Collection<?>, String, String>>  entry: enumInfo.entrySet()) {
            final Class<?> type = entry.getKey();
            final Function<Object, Boolean> filter = entry.getValue().getValue1();
            final Map<Object,Boolean> map = type.isEnum() ? new EnumMap((Class<? extends Enum<?>>) type) : new HashMap<>();
            final Collection<?> collection = entry.getValue().getValue2();
            final Collection<?> values = (collection != null) ? collection : Collections.singleton(type.getEnumConstants());
            for (Object item : values) {
                map.put(item, filter.apply(item));
            }
            defaultVisibilitiesMap.put(type, map);
        }
    }

    private CharacterProfile(String name, SpellFilterStatus spellFilterStatus,
                             SortFilterStatus sortFilterStatus, SpellSlotStatus spellSlotStatus
            ) {
        this.name = name;
        this.spellFilterStatus = spellFilterStatus;
        this.sortFilterStatus = sortFilterStatus;
        this.spellSlotStatus = spellSlotStatus;
    }

    private CharacterProfile(String name, SpellFilterStatus spellFilterStatus) {
        this(name, spellFilterStatus, new SortFilterStatus(), new SpellSlotStatus());
    }

    CharacterProfile(String nameIn) { this(nameIn, new SpellFilterStatus()); }

    protected CharacterProfile(Parcel in) {
        name = in.readString();
        spellFilterStatus = in.readParcelable(SpellFilterStatus.class.getClassLoader());
        sortFilterStatus = in.readParcelable(SortFilterStatus.class.getClassLoader());
        spellSlotStatus = in.readParcelable(SpellSlotStatus.class.getClassLoader());
    }

    public static final Creator<CharacterProfile> CREATOR = new Creator<CharacterProfile>() {
        @Override
        public CharacterProfile createFromParcel(Parcel in) {
            return new CharacterProfile(in);
        }

        @Override
        public CharacterProfile[] newArray(int size) {
            return new CharacterProfile[size];
        }
    };

    CharacterProfile duplicate() {
        final SpellFilterStatus newSpellFilterStatus = spellFilterStatus.duplicate();
        final SortFilterStatus newSortFilterStatus = sortFilterStatus.duplicate();
        final SpellSlotStatus newSpellSlotStatus = spellSlotStatus.duplicate();
        return new CharacterProfile(name, newSpellFilterStatus, newSortFilterStatus, newSpellSlotStatus);
    }

    // Basic getters
    public String getName() { return name; }
    SpellFilterStatus getSpellFilterStatus() { return spellFilterStatus; }
    @Bindable SortFilterStatus getSortFilterStatus() { return sortFilterStatus; }
    SpellSlotStatus getSpellSlotStatus() { return spellSlotStatus; }

    // Basic setters
    public void setName(String name) { this.name = name; }
    void setSortFilterStatus(SortFilterStatus sortFilterStatus) {
        this.sortFilterStatus = sortFilterStatus;
        notifyPropertyChanged(BR.sortFilterStatus);
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

    // Create a JSON object representing the profile
    // This is what we for saving
    // We can reconstruct the profile using fromJSON
    public JSONObject toJSON() throws JSONException {

        // The JSON object
        final JSONObject json = new JSONObject();

        // Store the data
        json.put(charNameKey, name);
        json.put(spellFilterStatusKey, spellFilterStatus.toJSON());
        json.put(sortFilterStatusKey, sortFilterStatus.toJSON());
        json.put(spellSlotStatusKey, spellSlotStatus.toJSON());
        json.put(versionCodeKey, GlobalInfo.VERSION_CODE);

        return json;
    }

    // Construct a profile from a JSON object
    // Basically the inverse to toJSON
    static CharacterProfile fromJSON(JSONObject json) throws JSONException {
        //System.out.println(json.toString(4));
        if (json.has(versionCodeKey)) {
            final String versionCode = json.getString(versionCodeKey);
            final Version version = SpellbookUtils.coalesce(Version.fromString(versionCode), GlobalInfo.VERSION);
            if (version.compareTo(V3_0_0) >= 0) {
                return fromJSONv3(json);
            } else if (version.compareTo(V2_10_0) >= 0) {
                return fromJSONNew(json, version);
            } else {
                return fromJSONPre2_10(json);
            }
        } else {
            return fromJSONOld(json);
        }
    }

    private static CharacterProfile fromJSONv3(JSONObject json) throws JSONException {
        final String name = json.getString(charNameKey);
        final SpellFilterStatus spellFilterStatus = SpellFilterStatus.fromJSON(json.getJSONObject(spellFilterStatusKey));
        final SortFilterStatus sortFilterStatus = SortFilterStatus.fromJSON(json.getJSONObject(sortFilterStatusKey));
        final SpellSlotStatus spellSlotStatus = SpellSlotStatus.fromJSON(json.getJSONObject(spellSlotStatusKey));
        return new CharacterProfile(name, spellFilterStatus, sortFilterStatus, spellSlotStatus);
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
        final SortField firstSortField = json.has(sort1Key) ? SortField.fromInternalName(json.getString(sort1Key)) : SortField.NAME;

        // Get the second sort field, if present
        final SortField secondSortField = json.has(sort2Key) ? SortField.fromInternalName(json.getString(sort2Key)) : SortField.NAME;

        // Get the sort reverse variables
        final boolean firstSortReverse = json.optBoolean(reverse1Key, false);
        final boolean secondSortReverse = json.optBoolean(reverse2Key, false);

        // If there was a filter class before, that's now the only visible class
        final CasterClass filterClass = json.has(classFilterKey) ? CasterClass.fromInternalName(json.getString(classFilterKey)) : null;
        final EnumSet<CasterClass> visibleCasterClasses = (filterClass != null) ? EnumSet.of(filterClass) : EnumSet.allOf(CasterClass.class);

        // What was the sourcebooks filter map is now the sourcebook entry of the visibilities map
        final Set<Source> visibleSources = new HashSet<>();
        if (json.has(booksFilterKey)) {
            final JSONObject booksJSON = json.getJSONObject(booksFilterKey);
            for (Source sb : Source.values()) {
                if (booksJSON.has(sb.getInternalName())) {
                    visibleSources.add(sb);
                }
            }
        } else {
            visibleSources.add(Source.PLAYERS_HANDBOOK);
        }

        // Get the status filter
        final StatusFilterField statusFilter = json.has(statusFilterKey) ? StatusFilterField.fromDisplayName(json.getString(statusFilterKey)) : StatusFilterField.ALL;

        // We no longer need the default filter statuses, and the spinners no longer have the default text

        // Everything else that the profiles have is new, so we'll use the defaults
        final int minLevel = Spellbook.MIN_SPELL_LEVEL;
        final int maxLevel = Spellbook.MAX_SPELL_LEVEL;

        // Return the profile
        final SpellSlotStatus spellSlotStatus = new SpellSlotStatus();
        final SortFilterStatus sortFilterStatus = new SortFilterStatus(statusFilter, firstSortField, secondSortField,
                firstSortReverse, secondSortReverse, minLevel, maxLevel, false, false, false,
                true, true, true, true, new boolean[]{true, true, true}, new boolean[]{true, true, true},
                visibleSources, EnumSet.allOf(School.class), visibleCasterClasses,
                EnumSet.allOf(CastingTimeType.class), EnumSet.allOf(DurationType.class), EnumSet.allOf(RangeType.class),
                SortFilterStatus.getDefaultMinValue(CastingTimeType.class), SortFilterStatus.getDefaultMaxValue(CastingTimeType.class),
                (TimeUnit) SortFilterStatus.getDefaultMinUnit(CastingTimeType.class), (TimeUnit) SortFilterStatus.getDefaultMaxUnit(CastingTimeType.class),
                SortFilterStatus.getDefaultMinValue(DurationType.class), SortFilterStatus.getDefaultMaxValue(DurationType.class),
                (TimeUnit) SortFilterStatus.getDefaultMinUnit(DurationType.class), (TimeUnit) SortFilterStatus.getDefaultMaxUnit(DurationType.class),
                SortFilterStatus.getDefaultMinValue(RangeType.class), SortFilterStatus.getDefaultMaxValue(RangeType.class),
                (LengthUnit) SortFilterStatus.getDefaultMinUnit(RangeType.class), (LengthUnit) SortFilterStatus.getDefaultMaxUnit(RangeType.class)
        );
        final SpellFilterStatus spellFilterStatus = new SpellFilterStatus(spellStatusMap);

        return new CharacterProfile(charName, spellFilterStatus, sortFilterStatus, spellSlotStatus);
    }

    // For character profiles from this version of the app
    private static CharacterProfile fromJSONNew(JSONObject json, Version version) throws JSONException {

        final String charName = json.getString(charNameKey);

        // Get the first sort field, if present
        final SortField firstSortField = json.has(sort1Key) ? SortField.fromInternalName(json.getString(sort1Key)) : SortField.NAME;

        // Get the second sort field, if present
        final SortField secondSortField = json.has(sort2Key) ? SortField.fromInternalName(json.getString(sort2Key)) : SortField.NAME;

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

        final EnumSet<CasterClass> visibleCasterClasses = visibleSetFromLegacyJSON(json, CasterClass.class);
        final EnumSet<School> visibleSchools = visibleSetFromLegacyJSON(json, School.class);
        final Set<Source> visibleSources = visibleSetFromLegacyJSON(json, Source.class, Source.values());
        final EnumSet<CastingTimeType> visibleCastingTimeTypes = visibleSetFromLegacyJSON(json, CastingTimeType.class);
        final EnumSet<DurationType> visibleDurationTypes = visibleSetFromLegacyJSON(json, DurationType.class);
        final EnumSet<RangeType> visibleRangeTypes = visibleSetFromLegacyJSON(json, RangeType.class);

        final Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> castingTimeSextet = quantityRangeFromLegacyJSON(json, CastingTimeType.class, enumInfo.get(CastingTimeType.class).getValue3());
        final Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> durationSextet = quantityRangeFromLegacyJSON(json, DurationType.class, enumInfo.get(DurationType.class).getValue3());
        final Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> rangeSextet = quantityRangeFromLegacyJSON(json, RangeType.class, enumInfo.get(RangeType.class).getValue3());

        // Get the sort reverse variables
        final boolean firstSortReverse = json.optBoolean(reverse1Key, false);
        final boolean secondSortReverse = json.optBoolean(reverse2Key, false);

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
        final boolean useTCEExpandedLists = json.optBoolean(useTCEExpandedListsKey, false);
        final boolean applyFiltersToLists = json.optBoolean(applyFiltersToSpellListsKey, false);
        final boolean applyFiltersToSearch = json.optBoolean(applyFiltersToSearchKey, false);

        final SpellSlotStatus spellSlotStatus = new SpellSlotStatus();
        final SortFilterStatus sortFilterStatus = new SortFilterStatus(statusFilter, firstSortField, secondSortField,
                firstSortReverse, secondSortReverse, minLevel, maxLevel, applyFiltersToSearch, applyFiltersToLists, useTCEExpandedLists,
                ritualFilter, notRitualFilter, concentrationFilter, notConcentrationFilter, componentsFilters, notComponentsFilters,
                visibleSources, visibleSchools, visibleCasterClasses,
                visibleCastingTimeTypes, visibleDurationTypes, visibleRangeTypes,
                castingTimeSextet.getValue4(), castingTimeSextet.getValue5(),
                (TimeUnit) castingTimeSextet.getValue2(), (TimeUnit) castingTimeSextet.getValue3(),
                durationSextet.getValue4(), durationSextet.getValue5(),
                (TimeUnit) durationSextet.getValue2(), (TimeUnit) durationSextet.getValue3(),
                rangeSextet.getValue4(), rangeSextet.getValue5(),
                (LengthUnit)rangeSextet.getValue2(), (LengthUnit) rangeSextet.getValue3()
        );
        final SpellFilterStatus spellFilterStatus = new SpellFilterStatus(spellStatusMap);
        return new CharacterProfile(charName, spellFilterStatus, sortFilterStatus, spellSlotStatus);

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
        final SortField firstSortField = json.has(sort1Key) ? SortField.fromInternalName(json.getString(sort1Key)) : SortField.NAME;

        // Get the second sort field, if present
        final SortField secondSortField = json.has(sort2Key) ? SortField.fromInternalName(json.getString(sort2Key)) : SortField.NAME;

        final EnumSet<CasterClass> visibleCasterClasses = visibleSetFromLegacyJSON(json, CasterClass.class);
        final EnumSet<School> visibleSchools = visibleSetFromLegacyJSON(json, School.class);
        final Set<Source> visibleSources = visibleSetFromLegacyJSON(json, Source.class, Source.values());
        final EnumSet<CastingTimeType> visibleCastingTimeTypes = visibleSetFromLegacyJSON(json, CastingTimeType.class);
        final EnumSet<DurationType> visibleDurationTypes = visibleSetFromLegacyJSON(json, DurationType.class);
        final EnumSet<RangeType> visibleRangeTypes = visibleSetFromLegacyJSON(json, RangeType.class);

        // If at least one class is hidden, hide the Artificer
        if (visibleCasterClasses.size() != CasterClass.values().length) {
            visibleCasterClasses.remove(CasterClass.ARTIFICER);
        }
        
        // Set newer sourcebooks to be not visible
        final List<Source> oldSources = Arrays.asList(Source.PLAYERS_HANDBOOK, Source.XANATHARS_GTE, Source.SWORD_COAST_AG);
        for (Source sb : oldSources) {
            visibleSources.remove(sb);
        }

        // Get the sort reverse variables
        final boolean firstSortReverse = json.optBoolean(reverse1Key, false);
        final boolean secondSortReverse = json.optBoolean(reverse2Key, false);

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
        final SpellSlotStatus spellSlotStatus = new SpellSlotStatus();
        final SortFilterStatus sortFilterStatus = new SortFilterStatus(statusFilter, firstSortField, secondSortField,
                firstSortReverse, secondSortReverse, minLevel, maxLevel, false, false, false,
                ritualFilter, notRitualFilter, concentrationFilter, notConcentrationFilter, componentsFilters, notComponentsFilters,
                visibleSources, visibleSchools, visibleCasterClasses,
                visibleCastingTimeTypes, visibleDurationTypes, visibleRangeTypes,
                SortFilterStatus.getDefaultMinValue(CastingTimeType.class), SortFilterStatus.getDefaultMaxValue(CastingTimeType.class),
                (TimeUnit) SortFilterStatus.getDefaultMinUnit(CastingTimeType.class), (TimeUnit) SortFilterStatus.getDefaultMaxUnit(CastingTimeType.class),
                SortFilterStatus.getDefaultMinValue(DurationType.class), SortFilterStatus.getDefaultMaxValue(DurationType.class),
                (TimeUnit) SortFilterStatus.getDefaultMinUnit(DurationType.class), (TimeUnit) SortFilterStatus.getDefaultMaxUnit(DurationType.class),
                SortFilterStatus.getDefaultMinValue(RangeType.class), SortFilterStatus.getDefaultMaxValue(RangeType.class),
                (LengthUnit) SortFilterStatus.getDefaultMinUnit(RangeType.class), (LengthUnit) SortFilterStatus.getDefaultMaxUnit(RangeType.class)
        );
        final SpellFilterStatus spellFilterStatus = new SpellFilterStatus(spellStatusMap);
        return new CharacterProfile(charName, spellFilterStatus, sortFilterStatus, spellSlotStatus);
    }

    private static Map<Integer,SpellStatus> convertStatusMap(Map<String,SpellStatus> oldMap) {
        final Set<String> scagCantrips = new HashSet<String>() {{
            add("Booming Blade");
            add("Green-Flame Blade");
            add("Lightning Lure");
            add("Sword Burst");
        }};
        final List<Spell> englishSpells = SpellbookViewModel.allEnglishSpells();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeParcelable(spellFilterStatus, i);
        parcel.writeParcelable(sortFilterStatus, i);
        parcel.writeParcelable(spellSlotStatus, i);
    }

    private static <T> List<T> visibleListFromLegacyJSON(JSONObject json, Class<T> type, T[] allValues) {
        try {
            final Quintet<Boolean, Function<Object, Boolean>, Collection<?>, String, String> entryValue = enumInfo.get(type);
            final String key = entryValue.getValue3();
            //final Function<Object, Boolean> filter = entryValue.getValue1();
            final boolean nonTrivialFilter = entryValue.getValue0();
            final Method constructorFromName = type.getDeclaredMethod("fromInternalName", String.class);
            final Map<T, Boolean> defaultMap = (Map<T, Boolean>) defaultVisibilitiesMap.get(type);
            final Map<T, Boolean> map = SpellbookUtils.copyOfMap(defaultMap, type);
            if (nonTrivialFilter) {
                for (T t : defaultMap.keySet()) {
                    map.put(t, true);
                }
            }
            if (json.has(key)) {
                final JSONArray jsonArray = json.getJSONArray(key);
                for (int i = 0; i < jsonArray.length(); ++i) {
                    final String name = jsonArray.getString(i);
                    final T value = (T) constructorFromName.invoke(null, name);
                    map.put(value, false);
                }
            }
            return map.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toList());
        } catch (Exception e) {
            return Arrays.asList(allValues.clone());
        }
    }

    private static <T> Set<T> visibleSetFromLegacyJSON(JSONObject json, Class<T> type, T[] allValues) {
        return new HashSet<>(visibleListFromLegacyJSON(json, type, allValues));
    }

    private static <E extends Enum<E>> EnumSet<E> visibleSetFromLegacyJSON(JSONObject json, Class<E> type) {
        return EnumSet.copyOf(visibleListFromLegacyJSON(json, type, type.getEnumConstants()));
    }

    private static <Q extends QuantityType> Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> quantityRangeFromLegacyJSON(JSONObject json, Class<Q> type, String key) {
        if (json.has(quantityRangesKey)) {
            try {
                final JSONObject quantityRangesJSON = json.getJSONObject(quantityRangesKey);
                final Class<? extends QuantityType> quantityType = keyToQuantityTypeMap.get(key);
                final Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> defaultData = defaultQuantityRangeFiltersMap.get(quantityType);
                final Class<? extends Quantity> quantityClass = defaultData.getValue0();
                final Class<? extends Unit> unitClass = defaultData.getValue1();
                final JSONObject rangeJSON = quantityRangesJSON.getJSONObject(key);
                final Method method = unitClass.getDeclaredMethod("fromInternalName", String.class);
                final Unit unit1 = SpellbookUtils.coalesce((Unit) method.invoke(null, rangeJSON.getString(rangeFilterKeys[0])), defaultData.getValue2());
                final Unit unit2 = SpellbookUtils.coalesce((Unit) method.invoke(null, rangeJSON.getString(rangeFilterKeys[1])), defaultData.getValue3());
                final Integer val1 = SpellbookUtils.coalesce(SpellbookUtils.intParse(rangeJSON.getString(rangeFilterKeys[2])), defaultData.getValue4());
                final Integer val2 = SpellbookUtils.coalesce(SpellbookUtils.intParse(rangeJSON.getString(rangeFilterKeys[3])), defaultData.getValue5());
                return new Sextet<>(quantityClass, unitClass, unit1, unit2, val1, val2);
            } catch (Exception e) {
                e.printStackTrace();
                return defaultQuantityRangeFiltersMap.get(type);
            }
        } else {
            return defaultQuantityRangeFiltersMap.get(type);
        }
    }
}
