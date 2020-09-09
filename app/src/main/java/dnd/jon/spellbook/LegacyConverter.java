package dnd.jon.spellbook;

/*
This class is designed to handle anything involving data storage from 'legacy' application versions
This mostly comes from the shift to the more featured filtering in v2.8, as well as the JSON -> SQL transition in v3
 */

import android.app.Application;

import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

class LegacyConverter {

    // The repository
    private final SpellbookRepository repository;

    // A data type for returning legacy results
    static class LegacyDataBundle {
        private final CharacterProfile profile;
        private final Collection<School> visibleSchools;
        private final Collection<CasterClass> visibleClasses;
        private final Collection<Source> visibleSources;
        private final Map<String,SpellStatus> spellStatusMap;

        LegacyDataBundle(CharacterProfile profile, Collection<School> visibleSchools, Collection<CasterClass> visibleClasses, Collection<Source> visibleSources, Map<String,SpellStatus> spellStatusMap) {
            this.profile = profile; this.visibleSchools = visibleSchools; this.visibleClasses = visibleClasses;
            this.visibleSources = visibleSources; this.spellStatusMap = spellStatusMap;
        }

        // Getters
        CharacterProfile getProfile() { return profile; }
        Collection<School> getVisibleSchools() { return visibleSchools; }
        Collection<CasterClass> getVisibleClasses() { return visibleClasses; }
        Collection<Source> getVisibleSources() { return visibleSources; }
        Map<String, SpellStatus> getSpellStatuses() { return spellStatusMap; }

    }


    // Keys for loading/saving

    // CharacterProfile
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
    static private final String hiddenSchoolsKey = "HiddenSchools";
    static private final String hiddenSourcebooksKey = "HiddenSourcebooks";
    static private final String hiddenCastersKey = "HiddenCasters";
    static private final String hiddenCastingTimeTypesKey = "HiddenCastingTimeTypes";
    static private final String hiddenDurationTypesKey = "HiddenDurationTypes";
    static private final String hiddenRangeTypesKey = "HiddenRangeTypes";
    static private final String ritualKey = "Ritual";
    static private final String notRitualKey = "NotRitual";
    static private final String concentrationKey = "Concentration";
    static private final String notConcentrationKey = "NotConcentration";
    static private final String minSpellLevelKey = "MinSpellLevel";
    static private final String maxSpellLevelKey = "MaxSpellLevel";
    static private final String versionCodeKey = "VersionCode";
    static private final String componentsFiltersKey = "ComponentsFilters";
    static private final String notComponentsFiltersKey = "NotComponentsFilters";
    static private final String minUnitKey = "MinUnit";
    static private final String maxUnitKey = "MaxUnit";
    static private final String minTextKey = "MinText";
    static private final String maxTextKey = "MaxText";
    static private final String castingTimeFiltersKey = "CastingTimeFilters";
    static private final String durationFiltersKey = "DurationFilters";
    static private final String rangeFiltersKey = "RangeFilters";

    // Settings
    private final static String headerTextKey = "HeaderTextSize";
    private final static String tableTextKey = "TableTextSize";
    private final static String nRowsKey = "TableNRows";
    private final static String spellTextKey = "SpellTextSize";
    private final static String characterKey = "Character";

    LegacyConverter(SpellbookRepository repository) { this.repository = repository; }
    LegacyConverter(Application application) { this(new SpellbookRepository(application)); }

    private static <T> List<T> listFromHiddenNames(JSONObject json, String key, Function<String,T> constructorFromName) throws JSONException {
        final List<T> list = new ArrayList<>();
        if (json.has(key)) {
            final JSONArray jsonArray = json.getJSONArray(key);
            for (int i = 0; i < jsonArray.length(); ++i) {
                final String name = jsonArray.getString(i);
                final T value = constructorFromName.apply(name);
                if (value != null) {
                    list.add(value);
                }
            }
        }
        return list;
    }

    private static <T extends Enum<T>> EnumSet<T> enumSetFromHiddenNames(JSONObject json, String key, Class<T> type, Function<String,T> constructorFromName) {
        final EnumSet<T> enumSet = EnumSet.allOf(type);
        try {
            enumSet.removeAll(listFromHiddenNames(json, key, constructorFromName));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return enumSet;
    }

    private static <V extends Enum<V> & QuantityType, U extends Unit, Q extends Quantity<V,U>> Q getQuantity(JSONObject json, String key, String unitKey, String valueKey, Function<String, U> unitParser, BiFunction<Integer,U,Q> quantityConstructor, Q defaultQuantity) {
        try {
            if (json.has(key)) {
                final JSONObject rangeJSON = json.getJSONObject(key);
                final U unit = unitParser.apply(rangeJSON.getString(unitKey));
                final Integer value = Integer.parseInt(rangeJSON.getString(valueKey));
                return quantityConstructor.apply(value, unit);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return quantityConstructor.apply(defaultQuantity.value, defaultQuantity.unit);
    }

    private static <V extends Enum<V> & QuantityType, U extends Unit, Q extends Quantity<V,U>> Q getMinQuantity(JSONObject json, String key, Function<String, U> unitParser, BiFunction<Integer,U,Q> quantityConstructor, Q defaultMinQuantity) {
        return getQuantity(json, key, minUnitKey, minTextKey, unitParser, quantityConstructor, defaultMinQuantity);
    }

    private static <V extends Enum<V> & QuantityType, U extends Unit, Q extends Quantity<V,U>> Q getMaxQuantity(JSONObject json, String key, Function<String, U> unitParser, BiFunction<Integer,U,Q> quantityConstructor, Q defaultMaxQuantity) {
        return getQuantity(json, key, maxUnitKey, maxTextKey, unitParser, quantityConstructor, defaultMaxQuantity);
    }

    // Construct a character profile from a JSON object
    LegacyDataBundle profileFromJSON(JSONObject json) throws JSONException {

        if (json.has(versionCodeKey)) {
            return profileFromJSONOld(json);
        } else {
            return profileFromJSONNew(json);
        }
    }

    LegacyDataBundle profileFromJSONOld(JSONObject json) throws JSONException {

        final String name = json.getString(charNameKey);

        // Get the spell map, assuming it exists
        // If it doesn't, we just get an empty map
        final Map<String, SpellStatus> spellStatusMap = new HashMap<>();
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

        // Get the sorting fields
        final SortField sortField1 = json.has(sort1Key) ? SortField.fromDisplayName(json.getString(sort1Key)) : SortField.NAME;
        final SortField sortField2 = json.has(sort2Key) ? SortField.fromDisplayName(json.getString(sort2Key)) : SortField.NAME;
        final boolean reverse1 = json.optBoolean(reverse1Key, false);
        final boolean reverse2 = json.optBoolean(reverse2Key, false);

        // Get the status filter
        final StatusFilterField statusFilter = json.has(statusFilterKey) ? StatusFilterField.fromDisplayName(json.getString(statusFilterKey)) : StatusFilterField.ALL;

        // What was the sourcebooks filter map is now the set of visible sourcebooks
        Collection<Source> visibleSources = new HashSet<>();
        if (json.has(booksFilterKey)) {
            final JSONObject booksJSON = json.getJSONObject(booksFilterKey);
            for (Source sb : repository.getAllSourcesStatic()) {
                if (booksJSON.has(sb.getCode()) && booksJSON.getBoolean(sb.getCode())) {
                    visibleSources.add(sb);
                }
            }
        } else {
            visibleSources.add(Source.PLAYERS_HANDBOOK);
        }

        // Schools weren't filterable before
        final Collection<School> visibleSchools = repository.getAllSchools();

        // If there was a filter class before, that's now the only visible class
        // Otherwise, they're all visible
        final CasterClass filterClass = json.has(classFilterKey) ? repository.getClassByName(json.getString(classFilterKey)) : null;
        final Collection<CasterClass> visibleClasses= (filterClass != null) ? Arrays.asList(filterClass) : repository.getAllClasses();

        // We no longer need the default filter statuses, as the spinners no longer have the default text

        // All of the other character profile fields didn't exist at this point
        return new LegacyDataBundle(new CharacterProfile(0, name, sortField1, sortField2, EnumSet.allOf(CastingTime.CastingTimeType.class), EnumSet.allOf(Duration.DurationType.class), EnumSet.allOf(Range.RangeType.class), reverse1, reverse2, statusFilter, true, true, true, true, true, true, true, true, true, true, Spellbook.MIN_SPELL_LEVEL, Spellbook.MAX_SPELL_LEVEL, CharacterProfile.defaultMinCastingTime, CharacterProfile.defaultMaxCastingTime, CharacterProfile.defaultMinDuration, CharacterProfile.defaultMaxDuration, CharacterProfile.defaultMinRange, CharacterProfile.defaultMaxRange), visibleSchools, visibleClasses, visibleSources, spellStatusMap);

    }

    LegacyDataBundle profileFromJSONNew(JSONObject json) throws JSONException {

        final String name = json.getString(charNameKey);

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

        final SortField sortField2 = json.has(sort2Key) ? SortField.fromDisplayName(json.getString(sort2Key)) : SortField.NAME;
        final boolean reverse1 = json.optBoolean(reverse1Key, false);
        final boolean reverse2 = json.optBoolean(reverse2Key, false);

        // Get the min and max spell levels
        final int minLevel = json.optInt(minSpellLevelKey, Spellbook.MIN_SPELL_LEVEL);
        final int maxLevel = json.optInt(maxSpellLevelKey, Spellbook.MAX_SPELL_LEVEL);

        // Get the status filter
        final StatusFilterField statusFilter = json.has(statusFilterKey) ? StatusFilterField.fromDisplayName(json.getString(statusFilterKey)) : StatusFilterField.ALL;

        // Get the filter statuses for ritual and concentration
        final boolean ritualFilter = json.optBoolean(ritualKey, true);
        final boolean notRitualFilter = json.optBoolean(notRitualKey, true);
        final boolean concentrationFilter = json.optBoolean(concentrationKey, true);
        final boolean notConcentrationFilter = json.optBoolean(notConcentrationKey, true);

        // Get the filter statuses for the components
        final JSONArray componentsJSON = json.optJSONArray(componentsFiltersKey);
        boolean verbalFilter = true;
        boolean somaticFilter = true;
        boolean materialFilter = true;
        if (componentsJSON != null && componentsJSON.length() == 3) {
            verbalFilter = componentsJSON.getBoolean(0);
            somaticFilter = componentsJSON.getBoolean(1);
            materialFilter = componentsJSON.getBoolean(2);
        }

        final JSONArray notComponentsJSON = json.optJSONArray(notComponentsFiltersKey);
        boolean notVerbalFilter = true;
        boolean notSomaticFilter = true;
        boolean notMaterialFilter = true;
        if (notComponentsJSON != null && notComponentsJSON.length() == 3) {
            notVerbalFilter = notComponentsJSON.getBoolean(0);
            notSomaticFilter = notComponentsJSON.getBoolean(1);
            notMaterialFilter = notComponentsJSON.getBoolean(2);
        }

        // Get the visible values of various types
        //final EnumSet<School> visibleSchools = enumSetFromHiddenNames(json, hiddenSchoolsKey, School.class, School::fromDisplayName);
        //final EnumSet<CasterClass> visibleCasters = enumSetFromHiddenNames(json, hiddenCastersKey, CasterClass.class, CasterClass::fromDisplayName);
        final EnumSet<CastingTime.CastingTimeType> visibleCastingTimeTypes = enumSetFromHiddenNames(json, hiddenCastingTimeTypesKey, CastingTime.CastingTimeType.class, CastingTime.CastingTimeType::fromDisplayName);
        final EnumSet<Duration.DurationType> visibleDurationTypes = enumSetFromHiddenNames(json, hiddenDurationTypesKey, Duration.DurationType.class, Duration.DurationType::fromDisplayName);
        final EnumSet<Range.RangeType> visibleRangeTypes = enumSetFromHiddenNames(json, hiddenRangeTypesKey, Range.RangeType.class, Range.RangeType::fromDisplayName);

        final Collection<Source> visibleSources = repository.getAllSourcesStatic();
        final List<String> hiddenCodeList = listFromHiddenNames(json, hiddenSourcebooksKey, (s) -> s);
        visibleSources.removeIf((src) -> hiddenCodeList.contains(src.getCode()));

        final List<String> hiddenClassNameList = listFromHiddenNames(json, hiddenCastersKey, (s) -> s);
        final Collection<CasterClass> visibleClasses = repository.getAllClasses();
        visibleClasses.removeIf((cls) -> hiddenClassNameList.contains(cls.getDisplayName()));

        final List<String> hiddenSchoolList = listFromHiddenNames(json, hiddenSchoolsKey, (s) -> s);
        final Collection<School> visibleSchools = repository.getAllSchools();
        visibleSchools.removeIf((sch) -> hiddenSchoolList.contains(sch.getDisplayName()));


        // Get the quantity type ranges
        final JSONObject quantityRangesJSON = json.getJSONObject(quantityRangesKey);
        final CastingTime minCastingTime = getMinQuantity(quantityRangesJSON, castingTimeFiltersKey, TimeUnit::fromString, CastingTime::new, CharacterProfile.defaultMinCastingTime);
        final CastingTime maxCastingTime = getMaxQuantity(quantityRangesJSON, castingTimeFiltersKey, TimeUnit::fromString, CastingTime::new, CharacterProfile.defaultMaxCastingTime);
        final Duration minDuration = getMinQuantity(quantityRangesJSON, durationFiltersKey, TimeUnit::fromString, Duration::new, CharacterProfile.defaultMinDuration);
        final Duration maxDuration = getMaxQuantity(quantityRangesJSON, durationFiltersKey, TimeUnit::fromString, Duration::new, CharacterProfile.defaultMaxDuration);
        final Range minRange = getMinQuantity(quantityRangesJSON, rangeFiltersKey, LengthUnit::fromString, Range::new, CharacterProfile.defaultMinRange);
        final Range maxRange = getMaxQuantity(quantityRangesJSON, rangeFiltersKey, LengthUnit::fromString, Range::new, CharacterProfile.defaultMaxRange);

        return new LegacyDataBundle(new CharacterProfile(0, name, sortField1, sortField2, visibleCastingTimeTypes, visibleDurationTypes, visibleRangeTypes, reverse1, reverse2, statusFilter, ritualFilter, notRitualFilter, concentrationFilter, notConcentrationFilter, verbalFilter, notVerbalFilter, somaticFilter, notSomaticFilter, materialFilter, notMaterialFilter, minLevel, maxLevel, minCastingTime, maxCastingTime, minDuration, maxDuration, minRange, maxRange), visibleSchools, visibleClasses, visibleSources, spellStatusMap);
    }

    static String charNameFromSettingsJSON(JSONObject json) { return json.optString(characterKey, null); }

}
