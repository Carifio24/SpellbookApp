package dnd.jon.spellbook;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class SpellbookViewModel extends AndroidViewModel {

    // The repositories of items
    private final SpellbookRepository repository;

    // Whether or not we're on a tablet
    private final boolean onTablet;

    // The character profile itself
    // We'll update this in the database when we save
    private CharacterProfile profile;

    // For logging
    private static final String LOGGING_TAG = "SpellbookViewModel";

    // For accessing and saving to shared preferences
    private static final String SHARED_PREFS_NAME = "spellbook";
    private static final String CHARACTER_NAME_KEY = "character";
    private final SharedPreferences preferences;

    // These fields describe the current sorting/filtering state for this profile
    // We keep them in the ViewModel so that it's easier to alert/receive changes from views
    // When we switch profiles, these values will get saved into the character database
    private final MutableLiveData<String> currentCharacterName = new MutableLiveData<>(null);
    private final MutableLiveData<SortField> firstSortField = new MutableLiveData<>(SortField.NAME);
    private final MutableLiveData<SortField> secondSortField = new MutableLiveData<>(SortField.NAME);
    private final MutableLiveData<Boolean> firstSortReverse = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> secondSortReverse = new MutableLiveData<>(false);
    private final MutableLiveData<StatusFilterField> statusFilter = new MutableLiveData<>(StatusFilterField.ALL);
    private final MutableLiveData<Integer> minLevel = new MutableLiveData<>(Spellbook.MIN_SPELL_LEVEL);
    private final MutableLiveData<Integer> maxLevel = new MutableLiveData<>(Spellbook.MAX_SPELL_LEVEL);
    private final MutableLiveData<Boolean> ritualFilter = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> notRitualFilter = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> concentrationFilter = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> notConcentrationFilter = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> verbalFilter = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> notVerbalFilter = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> somaticFilter = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> notSomaticFilter = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> materialFilter = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> notMaterialFilter = new MutableLiveData<>(true);
    private final LiveHashMap<Source,Boolean> visibleSources = new LiveHashMap<>();
    private final EnumLiveFlags<School> visibleSchools = new EnumLiveFlags<>(School.class);
    private final LiveHashMap<CasterClass,Boolean> visibleClasses = new LiveHashMap<>();
    private final EnumLiveFlags<CastingTime.CastingTimeType> visibleCastingTimeTypes = new EnumLiveFlags<>(CastingTime.CastingTimeType.class);
    private final EnumLiveFlags<Duration.DurationType> visibleDurationTypes = new EnumLiveFlags<>(Duration.DurationType.class);
    private final EnumLiveFlags<Range.RangeType> visibleRangeTypes = new EnumLiveFlags<>(Range.RangeType.class);
    private final MutableLiveData<String> filterText = new MutableLiveData<>("");

    // These maps store the current minimum and maximum quantities for each class
    private final Map<Class<? extends QuantityType>, Pair<MutableLiveData<Unit>, MutableLiveData<Integer>>> minQuantityValues = new HashMap<Class<? extends QuantityType>, Pair<MutableLiveData<Unit>, MutableLiveData<Integer>>>() {{
        for (Map.Entry<Class<? extends QuantityType>, Pair<Unit,Integer>> entry : defaultMinQuantityValues.entrySet()) {
            put(entry.getKey(), new Pair<>(new MutableLiveData<>(entry.getValue().getValue0()), new MutableLiveData<>(entry.getValue().getValue1())));
        }
    }};
    private final Map<Class<? extends QuantityType>, Pair<MutableLiveData<Unit>, MutableLiveData<Integer>>> maxQuantityValues = new HashMap<Class<? extends QuantityType>, Pair<MutableLiveData<Unit>, MutableLiveData<Integer>>>() {{
        for (Map.Entry<Class<? extends QuantityType>, Pair<Unit,Integer>> entry : defaultMaxQuantityValues.entrySet()) {
            put(entry.getKey(), new Pair<>(new MutableLiveData<>(entry.getValue().getValue0()), new MutableLiveData<>(entry.getValue().getValue1())));
        }
    }};

    // For loading legacy (i.e. pre-v3) data
    // Old setting stuff
    private static final String LEGACY_SETTINGS_FILENAME = "Settings.json";
    private static final String LEGACY_CHARACTERS_DIRECTORY = "Characters";
    private static final String LEGACY_CHARACTER_EXTENSION = ".json";

    // These fields describe the current spell and which of the favorite/prepared/known lists it's on
    private final MutableLiveData<Spell> currentSpell = new MutableLiveData<>();
    private Integer currentSpellIndex = -1;
    private final MutableLiveData<Void> currentSpellChange = new MutableLiveData<>();
    private final LiveData<Boolean> currentSpellFavorite = Transformations.map(currentSpell, this::isFavorite);
    private final LiveData<Boolean> currentSpellKnown = Transformations.map(currentSpell, this::isKnown);
    private final LiveData<Boolean> currentSpellPrepared = Transformations.map(currentSpell, this::isPrepared);

    // These fields control the execution of the sorting and filtering
    private boolean spellTableVisible = true; // Is the table of spells currently visible (i.e., do we need to sort/filter, or can it be delayed)?
    private boolean sortPending = false; // If true, we need to sort when the spells next become visible
    private boolean filterPending = false; // If true, we need to filter when the spells next become visible
    private final MutableLiveData<Void> spellWindowFragmentClosed = new MutableLiveData<>(null); // Whether or not the spell window fragment is visible (for phone)
    private final MutableLiveData<Void> sortEmitter = new MutableLiveData<>(null); // Emits when a sort action is needed
    private final MutableLiveData<Void> filterEmitter = new MutableLiveData<>(null); // Emit when a filter action is needed
    private final MutableLiveData<Void> sourceUpdateEmitter = new MutableLiveData<>(null);

    // The current list of spells
    // When filterSignal emits a signal, we get the updated spells from the database
    private final LiveData<List<Spell>> currentSpells = Transformations.switchMap(filterEmitter, (v) -> getVisibleSpells());

    // This map allows access to the item visibility flags by class
    private final Map<Class<? extends Named>, LiveMap<? extends Named, Boolean>> classToFlagsMap = new HashMap<Class<? extends Named>, LiveMap<? extends Named, Boolean>>() {{
       put(CasterClass.class, visibleClasses);
       put(Source.class, visibleSources);
       put(School.class, visibleSchools);
       put(CastingTime.CastingTimeType.class, visibleCastingTimeTypes);
       put(Duration.DurationType.class, visibleDurationTypes);
       put(Range.RangeType.class, visibleRangeTypes);
    }};

    // This map allows access to the spanning type visibility flags by class
    private final Map<Class<? extends QuantityType>, LiveData<Boolean>> spanningVisibilities = new HashMap<Class<? extends QuantityType>, LiveData<Boolean>>() {{
       put(CastingTime.CastingTimeType.class, getVisibility(CastingTime.CastingTimeType.spanningType()));
       put(Duration.DurationType.class, getVisibility(Duration.DurationType.spanningType()));
       put(Range.RangeType.class, getVisibility(Range.RangeType.spanningType()));
    }};

    // These static maps store the default minimum and maximum quantities for each relevant class
    private static final Map<Class<? extends QuantityType>, Pair<Unit, Integer>> defaultMinQuantityValues = new HashMap<Class<? extends QuantityType>, Pair<Unit, Integer>>() {{
        put(CastingTime.CastingTimeType.class, new Pair<>(CharacterProfile.defaultMinCastingTime.unit, CharacterProfile.defaultMinCastingTime.value));
        put(Duration.DurationType.class, new Pair<>(TimeUnit.SECOND, 0));
        put(Range.RangeType.class, new Pair<>(LengthUnit.FOOT, 0));
    }};
    private static final Map<Class<? extends QuantityType>, Pair<Unit, Integer>> defaultMaxQuantityValues = new HashMap<Class<? extends QuantityType>, Pair<Unit, Integer>>() {{
        put(CastingTime.CastingTimeType.class, new Pair<>(TimeUnit.HOUR, 24));
        put(Duration.DurationType.class, new Pair<>(TimeUnit.DAY, 30));
        put(Range.RangeType.class, new Pair<>(LengthUnit.MILE, 1));
    }};

    // For getting the base values of the min or max values of a certain quantity
    // This is used internally to get the correct values to pass to the function that gets the filtered values from the repository
    private int quantityBaseValue(Map<Class<? extends QuantityType>, Pair<MutableLiveData<Unit>, MutableLiveData<Integer>>> map, Class<? extends Named> quantityType) {
        final Pair<MutableLiveData<Unit>, MutableLiveData<Integer>> data = map.get(quantityType);
        if (data == null) { return 0; }
        final Unit unit = data.getValue0().getValue();
        final Integer value = data.getValue1().getValue();
        if (unit == null || value == null) { return 0; }
        return unit.value() * value;
    }
    private int minBaseValue(Class<? extends Named> quantityType) { return quantityBaseValue(minQuantityValues, quantityType); }
    private int maxBaseValue(Class<? extends Named> quantityType) { return quantityBaseValue(maxQuantityValues, quantityType); }

    // Constructor
    public SpellbookViewModel(Application application) {
        super(application);
        repository = new SpellbookRepository(application);
        onTablet = application.getResources().getBoolean(R.bool.isTablet);
        preferences = application.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        // Set up the sources map
        System.out.println("Setting up visibleSources");
        visibleSources.setFrom(repository.getAllSourcesStatic(), (src) -> src.getId() == 1);
        for (Source source : visibleSources.getKeys()) {
            System.out.println(source);
        }

        // If there's any legacy data floating around, take care of that now
        loadLegacyData();

        // Set values from shared preferences
        setValuesFromPreferences();

    }

    // Returns the current list of visible spells (for observation)
    LiveData<List<Spell>> getCurrentSpells() { return currentSpells; }

    // For internal use - gets the current spell list from the repository
    private LiveData<List<Spell>> getVisibleSpells() {
        final int minCastingTimeBaseValue = minBaseValue(CastingTime.CastingTimeType.class);
        final int maxCastingTimeBaseValue = maxBaseValue(CastingTime.CastingTimeType.class);
        final int minDurationBaseValue = minBaseValue(Duration.DurationType.class);
        final int maxDurationBaseValue = maxBaseValue(Duration.DurationType.class);
        final int minRangeBaseValue = minBaseValue(Range.RangeType.class);
        final int maxRangeBaseValue = maxBaseValue(Range.RangeType.class);
        return repository.getVisibleSpells(profile, statusFilter.getValue(), minLevel.getValue(), maxLevel.getValue(), ritualFilter.getValue(), notRitualFilter.getValue(),
                concentrationFilter.getValue(), notConcentrationFilter.getValue(), verbalFilter.getValue(), notVerbalFilter.getValue(), somaticFilter.getValue(), notSomaticFilter.getValue(),
                materialFilter.getValue(), notMaterialFilter.getValue(), visibleSources.getKeys((sb, flag) -> flag), visibleClasses.getKeys((c, flag) -> flag), visibleSchools.onValues(),
                visibleCastingTimeTypes.onValues(), minCastingTimeBaseValue, maxCastingTimeBaseValue,
                visibleDurationTypes.onValues(), minDurationBaseValue, maxDurationBaseValue,
                visibleRangeTypes.onValues(), minRangeBaseValue, maxRangeBaseValue,
                filterText.getValue(), firstSortField.getValue(), secondSortField.getValue(), firstSortReverse.getValue(), secondSortReverse.getValue()
        );
    }

    // For observing the currently selected spell and whether it's on one of the filtering lists
    LiveData<Spell> getCurrentSpell() { return currentSpell; }
    Integer getCurrentSpellIndex() { return currentSpellIndex; }
    LiveData<Boolean> isCurrentSpellFavorite() { return currentSpellFavorite; }
    LiveData<Boolean> isCurrentSpellPrepared() { return currentSpellPrepared; }
    LiveData<Boolean> isCurrentSpellKnown() { return currentSpellKnown; }
    LiveData<Void> getCurrentSpellChange() { return currentSpellChange; }

    // For detecting when the spell window fragment changes visibility, on a phone
    LiveData<Void> spellWindowFragmentClose() { return spellWindowFragmentClosed; }

    // Add, delete, and update spells
    void addSpell(Spell spell) { repository.insert(spell); }
    void deleteSpell(Spell spell) { repository.delete(spell); }
    void updateSpell(Spell spell) { repository.update(spell); }

    // For getting all of the current sources
    LiveData<List<Source>> getAllSources() { return repository.getAllSources(); }
    List<Source> getAllSourcesStatic() { return repository.getAllSourcesStatic(); }

    // For getting all of the classes
    List<CasterClass> getAllClasses() { return repository.getAllClasses(); }

    public String getCodeOrName(int sourceID) {
        final Source source = repository.getSourceByID(sourceID);
        return SpellbookUtils.coalesce(source.getCode(), source.getDisplayName());
    }

    // Get the location string for a spell
    String getLocationString(Spell spell) {
        String location = getCodeOrName(spell.getSourceID());
        final int page = spell.getPage();
        if (page > 0) {
            location += " " + page;
        }
        return location;
    }

    // For observing the list of character names
    LiveData<List<String>> getAllCharacterNames() { return repository.getAllCharacterNames(); }
    List<String> getAllCharacterNamesStatic() { return repository.getAllCharacterNamesStatic(); }

    // The current number of characters
    int getCharactersCount() { return repository.getCharactersCount(); }

    // Create a character with a given name and add them to the repository
    void createCharacter(String name) {
        final CharacterProfile cp = new CharacterProfile(name);
        addCharacter(cp);
    }

    // Add and remove characters from the repository
    void addCharacter(CharacterProfile cp) { repository.insert(cp); }
    void updateCharacter(CharacterProfile cp) { repository.update(cp); }
    void deleteCharacter(CharacterProfile cp) { repository.delete(cp); }

    // Adding a visible source for a character
    void addVisibleSource(CharacterProfile cp, Source source) {
        repository.insert(new CharacterSourceEntry(cp.getId(), source.getId()));
    }
    void removeVisibleSource(CharacterProfile cp, Source source) {
        repository.delete(new CharacterSourceEntry(cp.getId(), source.getId()));
    }

    // Set current state to reflect that of the profile with the given name
    void setCharacter(String name) {

        // First, let's save the current character
        saveCurrentCharacter();

        // Get the profile
        // If it's null (i.e. there's no character by this name), then do nothing)
        profile = repository.getCharacter(name);
        if (profile == null) {
            System.out.println("Got a null character");
            return;
        }

        // Update the character name in the settings
        preferences.edit().putString(CHARACTER_NAME_KEY, name).apply();

        // If the profile exists, then set the current values appropriately
        currentCharacterName.setValue(profile.getName());
        firstSortField.setValue(profile.getFirstSortField());
        secondSortField.setValue(profile.getSecondSortField());
        firstSortReverse.setValue(profile.getFirstSortReverse());
        secondSortReverse.setValue(profile.getSecondSortReverse());
        statusFilter.setValue(profile.getStatusFilter());
        minLevel.setValue(profile.getMinLevel());
        maxLevel.setValue(profile.getMaxLevel());
        visibleSchools.setItems(profile.getVisibleSchools());
        visibleClasses.setItems(profile.getVisibleClasses());
        visibleCastingTimeTypes.setItems(profile.getVisibleCastingTimeTypes());
        visibleDurationTypes.setItems(profile.getVisibleDurationTypes());
        visibleRangeTypes.setItems(profile.getVisibleRangeTypes());
        ritualFilter.setValue(profile.getRitualFilter());
        notRitualFilter.setValue(profile.getNotRitualFilter());
        concentrationFilter.setValue(profile.getConcentrationFilter());
        notConcentrationFilter.setValue(profile.getNotConcentrationFilter());
        verbalFilter.setValue(profile.getVerbalFilter());
        notVerbalFilter.setValue(profile.getNotVerbalFilter());
        somaticFilter.setValue(profile.getSomaticFilter());
        notSomaticFilter.setValue(profile.getNotSomaticFilter());
        materialFilter.setValue(profile.getMaterialFilter());
        notMaterialFilter.setValue(profile.getNotMaterialFilter());
        setQuantityBoundsFromProfile(CastingTime.CastingTimeType.class, CharacterProfile::getMinCastingTime, CharacterProfile::getMaxCastingTime);
        setQuantityBoundsFromProfile(Duration.DurationType.class, CharacterProfile::getMinDuration, CharacterProfile::getMaxDuration);
        setQuantityBoundsFromProfile(Range.RangeType.class, CharacterProfile::getMinRange, CharacterProfile::getMaxRange);
        final List<Source> visibleSourceList = repository.getVisibleSources(profile.getId());
        visibleSources.setFrom(repository.getAllSourcesStatic(), visibleSourceList::contains);
        System.out.println("In setCharacter");
        for (Source source : visibleSources.getKeys()) {
            System.out.println(source + "\t" + visibleSources.get(source));
        }

        // Filter after the update
        setFilterFlag.run();
    }

    void saveCurrentCharacter() {
        if (profile == null) { return; }
        profile.setFirstSortField(AndroidUtils.getValueWithDefault(firstSortField, SortField.NAME));
        profile.setSecondSortField(AndroidUtils.getValueWithDefault(secondSortField, SortField.NAME));
        profile.setFirstSortReverse(AndroidUtils.getValueWithDefault(firstSortReverse, false));
        profile.setSecondSortReverse(AndroidUtils.getValueWithDefault(secondSortReverse, false));
        profile.setStatusFilter(AndroidUtils.getValueWithDefault(statusFilter, StatusFilterField.ALL));
        profile.setMinLevel(AndroidUtils.getValueWithDefault(minLevel, Spellbook.MIN_SPELL_LEVEL));
        profile.setMaxLevel(AndroidUtils.getValueWithDefault(maxLevel, Spellbook.MAX_SPELL_LEVEL));
        //profile.setVisibleSources(visibleSourcebooks.getKeys((sb, flag) -> flag));
        profile.setVisibleSchools(visibleSchools.onValues());
        profile.setVisibleClasses(visibleClasses.onValues());
        profile.setVisibleCastingTimeTypes(visibleCastingTimeTypes.onValues());
        profile.setVisibleDurationTypes(visibleDurationTypes.onValues());
        profile.setVisibleRangeTypes(visibleRangeTypes.onValues());
        profile.setRitualFilter(AndroidUtils.getValueWithDefault(ritualFilter, true));
        profile.setNotRitualFilter(AndroidUtils.getValueWithDefault(notRitualFilter, true));
        profile.setConcentrationFilter(AndroidUtils.getValueWithDefault(concentrationFilter, true));
        profile.setNotConcentrationFilter(AndroidUtils.getValueWithDefault(notConcentrationFilter, true));
        profile.setVerbalFilter(AndroidUtils.getValueWithDefault(verbalFilter, true));
        profile.setNotVerbalFilter(AndroidUtils.getValueWithDefault(notVerbalFilter, true));
        profile.setSomaticFilter(AndroidUtils.getValueWithDefault(somaticFilter, true));
        profile.setNotSomaticFilter(AndroidUtils.getValueWithDefault(notSomaticFilter, true));
        profile.setMaterialFilter(AndroidUtils.getValueWithDefault(materialFilter, true));
        profile.setNotMaterialFilter(AndroidUtils.getValueWithDefault(notMaterialFilter, true));
        profile.setMinCastingTime(getMinCastingTime());
        profile.setMaxCastingTime(getMaxCastingTime());
        profile.setMinDuration(getMinDuration());
        profile.setMaxDuration(getMaxDuration());
        profile.setMinRange(getMinRange());
        profile.setMaxRange(getMaxRange());
        repository.update(profile);

        for (Pair<Source, Boolean> pair : visibleSources.getEntries()) {
            final BiConsumer<CharacterProfile, Source> addOrRemove = pair.getValue1() ? this::addVisibleSource : this::removeVisibleSource;
            addOrRemove.accept(profile, pair.getValue0());
        }

    }

    // Two generic helper functions for setCharacter above
    private <T extends QuantityType> void setQuantity(Class<T> type, Quantity quantity, BiConsumer<Class<? extends QuantityType>, Unit> unitSetter, BiConsumer<Class<? extends QuantityType>,Integer> valueSetter) {
        unitSetter.accept(type, quantity.unit);
        valueSetter.accept(type, quantity.value);
    }
    private <T extends QuantityType> void setQuantityBoundsFromProfile(Class<T> type, Function<CharacterProfile,Quantity> minQuantityGetter, Function<CharacterProfile,Quantity> maxQuantityGetter) {
        if (profile == null) { return; }
        setQuantity(type, minQuantityGetter.apply(profile), this::setMinUnit, this::setMinValue);
        setQuantity(type, maxQuantityGetter.apply(profile), this::setMaxUnit, this::setMaxValue);
    }

    // Delete the character profile with the given name
    void deleteCharacter(String name) { repository.deleteByName(name); }

    // For a view to observe emitters
    LiveData<Void> getSortSignal() { return sortEmitter; }
    LiveData<Void> getSourcesUpdateSignal() { return sourceUpdateEmitter; }

    // Get the LiveData for the current character name, sort options, status filter field, and min and max level
    LiveData<String> getCharacterName() { return currentCharacterName; }
    LiveData<SortField> getFirstSortField() { return firstSortField; }
    LiveData<SortField> getSecondSortField() { return secondSortField; }
    LiveData<Boolean> getFirstSortReverse() { return firstSortReverse; }
    LiveData<Boolean> getSecondSortReverse() { return secondSortReverse; }
    LiveData<Integer> getMinLevel() { return minLevel; }
    LiveData<Integer> getMaxLevel() { return maxLevel; }
    LiveData<StatusFilterField> getStatusFilter() { return statusFilter; }

    // Observe whether the visibility flag for a Named item is set
    LiveData<Boolean> getVisibility(Named named) {
        final Class<? extends Named> cls = named.getClass();
        final LiveMap map = classToFlagsMap.get(cls);
        if (map == null) { return null; }
        for (Object x : map.getKeys()) {
            if (x.equals(named)) {
                return map.get(x);
            }
        }
        return map.get(cls.cast(named));
    }

    // Get the filter text
    String getFilterText() { return filterText.getValue(); }

    // Observe one of the yes/no filters
    LiveData<Boolean> getRitualFilter(boolean b) { return b ? ritualFilter : notRitualFilter; }
    LiveData<Boolean> getConcentrationFilter(boolean b) { return b ? concentrationFilter : notConcentrationFilter; }
    LiveData<Boolean> getVerbalFilter(boolean b) { return b ? verbalFilter: notVerbalFilter; }
    LiveData<Boolean> getSomaticFilter(boolean b) { return b ? somaticFilter : notSomaticFilter; }
    LiveData<Boolean> getMaterialFilter(boolean b) { return b ? materialFilter : notMaterialFilter; }

    // Observe values for the min/max units and values for the quantity classes
    LiveData<Unit> getMaxUnit(Class<? extends QuantityType> quantityType) { return maxQuantityValues.get(quantityType).getValue0(); }
    LiveData<Unit> getMinUnit(Class<? extends QuantityType> quantityType) { return minQuantityValues.get(quantityType).getValue0(); }
    LiveData<Integer> getMaxValue(Class<? extends QuantityType> quantityType) { return maxQuantityValues.get(quantityType).getValue1(); }
    LiveData<Integer> getMinValue(Class<? extends QuantityType> quantityType) { return minQuantityValues.get(quantityType).getValue1(); }

    // Observe whether or not the spanning type is visible for a given QuantityType class
    LiveData<Boolean> getSpanningTypeVisible(Class<? extends QuantityType> quantityType){ return spanningVisibilities.get(quantityType); }

    // For internal use - getting the quantities from the maps
    private <V extends Enum<V> & QuantityType, U extends Unit, Q extends Quantity<V,U>> Q getQuantity(Class<V> quantityType, Class<U> unitType, BiFunction<Integer,U,Q> quantityConstructor, Map<Class<? extends QuantityType>, Pair<MutableLiveData<Unit>, MutableLiveData<Integer>>> map) {
        final Pair<MutableLiveData<Unit>, MutableLiveData<Integer>> pair = map.get(quantityType);
        final U unit = unitType.cast(pair.getValue0().getValue());
        final Integer value = SpellbookUtils.coalesce(pair.getValue1().getValue(), 0);
        return quantityConstructor.apply(value, unit);
    }
    private <V extends Enum<V> & QuantityType, U extends Unit, Q extends Quantity<V,U>> Q getMinQuantity(Class<V> quantityType, Class<U> unitType, BiFunction<Integer,U,Q> quantityConstructor) { return getQuantity(quantityType, unitType, quantityConstructor, minQuantityValues); }
    private <V extends Enum<V> & QuantityType, U extends Unit, Q extends Quantity<V,U>> Q getMaxQuantity(Class<V> quantityType, Class<U> unitType, BiFunction<Integer,U,Q> quantityConstructor) { return getQuantity(quantityType, unitType, quantityConstructor, maxQuantityValues); }
    CastingTime getMinCastingTime() { return getMinQuantity(CastingTime.CastingTimeType.class, TimeUnit.class, CastingTime::new); }
    CastingTime getMaxCastingTime() { return getMaxQuantity(CastingTime.CastingTimeType.class, TimeUnit.class, CastingTime::new); }
    Duration getMinDuration() { return getMinQuantity(Duration.DurationType.class, TimeUnit.class, Duration::new); }
    Duration getMaxDuration() { return getMaxQuantity(Duration.DurationType.class, TimeUnit.class, Duration::new); }
    Range getMinRange() { return getMinQuantity(Range.RangeType.class, LengthUnit.class, Range::new); }
    Range getMaxRange() { return getMaxQuantity(Range.RangeType.class, LengthUnit.class, Range::new); }

    // Are we on a tablet?
    boolean areOnTablet() { return onTablet; }

    // Get the default values for the min/max units and values for the quantity classes
    static Unit getDefaultMaxUnit(Class<? extends QuantityType> quantityType) { return defaultMaxQuantityValues.get(quantityType).getValue0(); }
    static Unit getDefaultMinUnit(Class<? extends QuantityType> quantityType) { return defaultMinQuantityValues.get(quantityType).getValue0(); }
    static Integer getDefaultMaxValue(Class<? extends QuantityType> quantityType) { return defaultMaxQuantityValues.get(quantityType).getValue1(); }
    static Integer getDefaultMinValue(Class<? extends QuantityType> quantityType) { return defaultMinQuantityValues.get(quantityType).getValue1(); }

    // Use this to set LiveData that a view might need to observe
    // This will avoid infinite loops
    // The second argument will perform any necessary actions after a change
    private <T> void setIfNeeded(MutableLiveData<T> liveData, T t, Runnable postChangeAction) {
        if (t != liveData.getValue()) {
            liveData.setValue(t);
            if (postChangeAction != null) {
                postChangeAction.run();
            }
        }
    }

//    // The same thing, but for live maps
//    private <K,V> void setIfNeeded(LiveMap<K,V> liveMap, K k, V v, Runnable postChangeAction) {
//        final LiveData<V> data = liveMap.get(k);
//        if (data != null && data.getValue() != v) {
//            liveMap.set(k, v);
//            if (postChangeAction != null) {
//                postChangeAction.run();
//            }
//        }
//    }
//
//    // With no runnable effect
//    private <K,V> void setIfNeeded(LiveMap<K,V> liveMap, K k, V v) {
//        setIfNeeded(liveMap, k, v, null);
//    }


    // These are to be used as the last argument in setIfNeeded above
    // These set a sort and filter, respectively, when next the table of spells is visible
    private final Runnable setSortFlag = this::setToSort;
    private final Runnable setFilterFlag = this::setToFilter;

    // Set the sorting parameters, level range, and status filter
    // The associated LiveData values are only updated if necessary
    void setFirstSortField(SortField sortField) { setIfNeeded(firstSortField, sortField, setSortFlag); }
    void setSecondSortField(SortField sortField) { setIfNeeded(secondSortField, sortField, setSortFlag); }
    void setFirstSortReverse(Boolean reverse) { setIfNeeded(firstSortReverse, reverse, setSortFlag); }
    void setSecondSortReverse(Boolean reverse) { setIfNeeded(secondSortReverse, reverse, setSortFlag); }
    void setStatusFilter(StatusFilterField sff) { setIfNeeded(statusFilter, sff, setFilterFlag); }
    void setMinLevel(Integer level) { setIfNeeded(minLevel, level, setFilterFlag); }
    void setMaxLevel(Integer level) { setIfNeeded(maxLevel, level, setFilterFlag); }

    // An alternative way to set the sort fields, where one can give the desired level to the function
    // The functions for the sort fields and reverses are partial specializations of this private generic function
    private <T> void setFieldByLevel(T t, int level, Consumer<T> firstSetter, Consumer<T> secondSetter) {
        switch (level) {
            case 1:
                firstSetter.accept(t);
            case 2:
                secondSetter.accept(t);
        }
    }
    void setSortField(SortField sortField, int level) { setFieldByLevel(sortField, level, this::setFirstSortField, this::setSecondSortField); }
    void setSortReverse(Boolean reverse,int level) { setFieldByLevel(reverse, level, this::setFirstSortReverse, this::setSecondSortReverse); }

    private void currentSpellChanged() { currentSpellChange.setValue(null); }
    private void spellWindowClosed() { if (onTablet) { spellWindowFragmentClosed.setValue(null); } }

    void setFilterText(String text) { setIfNeeded(filterText, text, setFilterFlag); }
    void setCurrentSpell(Spell spell, Integer index) { currentSpell.setValue(spell); currentSpellIndex = index; }

    void setToFilter() {
        System.out.println("In setFilterNeeded");
        if (spellTableVisible) {
            System.out.println("Setting filterNeeded to true");
            emitFilterSignal();
        } else {
            filterPending = true;
        }
    }
    void setToSort() {
        if (spellTableVisible) {
            emitSortSignal();
        } else {
            sortPending = true;
        }
    }
    private void liveEmit(MutableLiveData<Void> emitter) { emitter.setValue(null); }
    void emitSortSignal() { liveEmit(sortEmitter); }
    void emitFilterSignal() { liveEmit(filterEmitter); }
    void emitSourcesUpdateSignal() { liveEmit(sourceUpdateEmitter); }
    private void onTableBecomesVisible() {
        System.out.println("Table became visible");
        if (filterPending) {
            emitFilterSignal();
            filterPending = false;
        } else if (sortPending) {
            emitSortSignal();
            sortPending = false;
        }
    }
    void setSpellTableVisible(Boolean visible) {
        spellTableVisible = visible;
        if (visible) { onTableBecomesVisible(); }
    }

    // This function sets the value of the appropriate LiveData filter, specified by tf, to be b
    private void setYNFilter(MutableLiveData<Boolean> filterT, MutableLiveData<Boolean> filterF, boolean tf, Boolean b) {
        final MutableLiveData<Boolean> filter = tf ? filterT : filterF;
        setIfNeeded(filter, b, setFilterFlag);
    }

    // The specific cases for the ritual, concentration, and component filters
    void setRitualFilter(boolean tf, Boolean b) { setYNFilter(ritualFilter, notRitualFilter, tf, b); }
    void setConcentrationFilter(boolean tf, Boolean b) { setYNFilter(concentrationFilter, notConcentrationFilter, tf, b); }
    void setVerbalFilter(boolean tf, Boolean b) { setYNFilter(verbalFilter, notVerbalFilter, tf, b); }
    void setSomaticFilter(boolean tf, Boolean b) { setYNFilter(somaticFilter, notSomaticFilter, tf, b); }
    void setMaterialFilter(boolean tf, Boolean b) { setYNFilter(materialFilter, notMaterialFilter, tf, b); }

    // Set the visibility flag for the given item to the given value
    void setVisibility(Named named, Boolean visibility) {
        final Class<? extends Named> cls = named.getClass();
        final LiveMap map = classToFlagsMap.get(cls);
        if (map != null && map.get(named) != null) {
            if (map.get(named).getValue() != visibility) {
                map.set(named, visibility);
                setToFilter();
            }
        }
    }

    // Toggle the visibility of the given named item
    void toggleVisibility(Named named) {
        final LiveData<Boolean> data = getVisibility(named);
        System.out.println(named.getDisplayName() + " : " + data.getValue());
        if ( (data != null) && (data.getValue() != null) ) {
            setVisibility(named, !data.getValue());
        }
    }

    // Set the range values for a specific class to their defaults
    void setRangeToDefaults(Class<? extends QuantityType> quantityType) {
        final Pair<Unit,Integer> minDefaults = defaultMinQuantityValues.get(quantityType);
        final Pair<Unit,Integer> maxDefaults = defaultMaxQuantityValues.get(quantityType);
        final Pair<MutableLiveData<Unit>,MutableLiveData<Integer>> minValues = minQuantityValues.get(quantityType);
        final Pair<MutableLiveData<Unit>,MutableLiveData<Integer>> maxValues = maxQuantityValues.get(quantityType);
        minValues.getValue0().setValue(minDefaults.getValue0());
        minValues.getValue1().setValue(minDefaults.getValue1());
        maxValues.getValue0().setValue(maxDefaults.getValue0());
        maxValues.getValue1().setValue(maxDefaults.getValue1());
    }

    // These functions, and their specializations below, set the min/max units and values
    // setExtremeUnit and setExtremeValue can probably be combined into one function with a bit of work
    private void setExtremeUnit(Map<Class<? extends QuantityType>, Pair<MutableLiveData<Unit>, MutableLiveData<Integer>>> map, Class<? extends QuantityType> quantityType, Unit unit) {
        final Pair<MutableLiveData<Unit>, MutableLiveData<Integer>> pair = map.get(quantityType);
        if (pair == null) { return; }
        final MutableLiveData<Unit> liveData = pair.getValue0();
        if (liveData == null) { return; }
        setIfNeeded(liveData, unit, setFilterFlag);
    }
    private void setExtremeValue(Map<Class<? extends QuantityType>, Pair<MutableLiveData<Unit>, MutableLiveData<Integer>>> map, Class<? extends QuantityType> quantityType, Integer value) {
        final Pair<MutableLiveData<Unit>, MutableLiveData<Integer>> pair = map.get(quantityType);
        if (pair == null) { return; }
        final MutableLiveData<Integer> liveData = pair.getValue1();
        if (liveData == null) { return; }
        setIfNeeded(liveData, value, setFilterFlag);
    }
    void setMinUnit(Class<? extends QuantityType> quantityType, Unit unit) { setExtremeUnit(minQuantityValues, quantityType, unit); }
    void setMaxUnit(Class<? extends QuantityType> quantityType, Unit unit) { setExtremeUnit(maxQuantityValues, quantityType, unit); }
    void setMinValue(Class<? extends QuantityType> quantityType, Integer value) { setExtremeValue(minQuantityValues, quantityType, value); }
    void setMaxValue(Class<? extends QuantityType> quantityType, Integer value) { setExtremeValue(maxQuantityValues, quantityType, value); }

    // Check whether a given spell is on one of the spell lists
    boolean isFavorite(Spell spell) { return profile != null && repository.isFavorite(profile, spell); }
    boolean isPrepared(Spell spell) { return profile != null && repository.isPrepared(profile, spell); }
    boolean isKnown(Spell spell) { return profile != null && repository.isKnown(profile, spell); }

    // Setting whether a spell is on a given spell list
    private void updateIfCurrent(Spell spell) {
        final Spell currSpell = currentSpell.getValue();
        if (currSpell != null && spell.equals(currSpell)) {
            currentSpellChanged();
        }
    }
    void setFavorite(Spell spell, boolean favorite) { repository.setFavorite(profile, spell, favorite, (nothing) -> updateIfCurrent(spell)); }
    void setPrepared(Spell spell, boolean prepared) { repository.setPrepared(profile, spell, prepared, (nothing) -> updateIfCurrent(spell)); }
    void setKnown(Spell spell, boolean known) { repository.setKnown(profile, spell, known, (nothing) -> updateIfCurrent(spell)); }

    // Toggling whether a given property is set for a given spell
    // General function followed by specific cases
    private void toggleProperty(Spell spell, Function<Spell,Boolean> propGetter, BiConsumer<Spell,Boolean> propSetter) {
        propSetter.accept(spell, !propGetter.apply(spell));
        //updateIfCurrent(spell);
    }
    void toggleFavorite(Spell spell) { toggleProperty(spell, this::isFavorite, this::setFavorite); }
    void togglePrepared(Spell spell) { toggleProperty(spell, this::isPrepared, this::setPrepared); }
    void toggleKnown(Spell spell) { toggleProperty(spell,this::isKnown, this::setKnown); }

    // Get the names of a collection of named values, as either an array or a list
    <T extends Named> String[] namesArray(Collection<T> collection) { return collection.stream().map(T::getDisplayName).toArray(String[]::new); }
    <T extends Named> List<String> namesList(Collection<T> collection) { return Arrays.asList(namesArray(collection)); }

    // Load any settings from shared preferences
    // For now, it's just the character profile
    private void loadSettings() {
        final SharedPreferences sharedPrefs = getApplication().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        final String name = sharedPrefs.getString(CHARACTER_NAME_KEY, null);
        if (name != null) {
            setCharacter(name);
        }
    }

    // Load any legacy data that's still floating around, and then dispose of the JSON files
    // This function is kind of messy
    // It would be nice to move this out to LegacyUtilities
    private void loadLegacyData() {

        // The application's data directory
        final File dataDir = getApplication().getDataDir();

        // First, we want to load any old characters
        // Delete each file, and the directory, after
        final File charactersDir = new File(dataDir, LEGACY_CHARACTERS_DIRECTORY);
        if (charactersDir.exists() && charactersDir.isDirectory()) {
            for (File file : charactersDir.listFiles()) {
                final String filename = file.getName();
                if (filename.endsWith(LEGACY_CHARACTER_EXTENSION)) {
                    try {
                        final JSONObject json = JSONUtilities.loadJSONfromData(file);
                        final Triplet<CharacterProfile, Set<Source>, Map<String,SpellStatus>> data = LegacyUtilities.profileFromJSON(json);
                        final CharacterProfile cp = data.getValue0();
                        final Set<Source> visible = data.getValue1();
                        final Map<String, SpellStatus> spellStatusMap = data.getValue2();
                        addCharacter(cp);
                        for (Source source : visible) {
                            addVisibleSource(cp, source);
                        }
                        for (Map.Entry<String,SpellStatus> entry : spellStatusMap.entrySet()) {
                            final Spell spell = repository.getSpellByName(entry.getKey());
                            final SpellStatus status = entry.getValue();
                            repository.insert(new CharacterSpellEntry(cp.getId(), spell.getId(), status.isFavorite(), status.isKnown(), status.isPrepared()));
                        }
                    } catch (JSONException e) {
                        Log.e(LOGGING_TAG, SpellbookUtils.stackTrace(e));
                    }
                }
                file.delete();
            }
            charactersDir.delete();
        }

        // Then, look at the settings and get the name of the previously set character
        // Then delete the settings
        final File settingsFilePath = new File(dataDir, LEGACY_SETTINGS_FILENAME);
        if (settingsFilePath.exists()) {
            try {
                final JSONObject json = JSONUtilities.loadJSONfromData(settingsFilePath);
                final String characterName = LegacyUtilities.charNameFromSettingsJSON(json);
                setCharacter(characterName);
                settingsFilePath.delete();
            } catch (JSONException e) {
                Log.e(LOGGING_TAG, SpellbookUtils.stackTrace(e));
            }

        }

    }

    private void setValuesFromPreferences() {
        // Set the state with any values that we need
        // Currently, that's just the name of the character profile
        final String name = preferences.getString(CHARACTER_NAME_KEY, null);
        if (name != null) { setCharacter(name); }
    }

    private void saveSharedPreferences() {
        // Set any fields that we need to
        preferences.edit()
                .putString(CHARACTER_NAME_KEY, currentCharacterName.getValue())
                .apply();
    }

    void onShutdown() {
        // Save the current character
        saveCurrentCharacter();

        // Save any values to the shared preferences
        saveSharedPreferences();
    }

}
