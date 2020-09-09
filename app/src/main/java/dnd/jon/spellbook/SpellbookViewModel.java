package dnd.jon.spellbook;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.databinding.BaseObservable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import org.javatuples.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private PropertyAwareLiveData<CharacterProfile> profile;

    // For logging
    private static final String LOGGING_TAG = "SpellbookViewModel";

    // For accessing and saving to shared preferences
    private static final String SHARED_PREFS_NAME = "spellbook";
    private static final String CHARACTER_NAME_KEY = "character";
    private final SharedPreferences preferences;

    // The current text in the SearchView used for filtering
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

    // Visibility map getters for CharacterProfile
    private final Map<Class<? extends Named>, Function<CharacterProfile,Collection<? extends Named>>> visibleItemGetters = new HashMap<Class<? extends Named>, Function<CharacterProfile,Collection<? extends Named>>>() {{
       put(CastingTime.CastingTimeType.class, CharacterProfile::getVisibleCastingTimeTypes);
       put(Duration.DurationType.class, CharacterProfile::getVisibleDurationTypes);
       put(Range.RangeType.class, CharacterProfile::getVisibleRangeTypes);
    }};

    // This map allows access to the item visibility flags by class
//    private final Map<Class<? extends Named>, LiveMap<? extends Named, Boolean>> classToFlagsMap = new HashMap<Class<? extends Named>, LiveMap<? extends Named, Boolean>>() {{
//       put(CasterClass.class, visibleClasses);
//       put(Source.class, visibleSources);
//       put(School.class, visibleSchools);
//       put(CastingTime.CastingTimeType.class, visibleCastingTimeTypes);
//       put(Duration.DurationType.class, visibleDurationTypes);
//       put(Range.RangeType.class, visibleRangeTypes);
//    }};

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
        visibleClasses.setFrom(repository.getAllClasses(), (cls) -> true);
        for (Source source : visibleSources.getKeys()) {
            System.out.println(source);
        }
        for (CasterClass cc: visibleClasses.getKeys()) {
            System.out.println(cc.getDisplayName());
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
    List<String> getAllClassNames() { return repository.getAllClassNames(); }

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

    // Adding a visible item (source, class, school, etc.) for a character
    void addVisibleSource(CharacterProfile cp, Source source) {
        repository.insert(new CharacterSourceEntry(cp.getId(), source.getId()));
    }
    void removeVisibleSource(CharacterProfile cp, Source source) {
        repository.delete(new CharacterSourceEntry(cp.getId(), source.getId()));
    }
    void addVisibleClass(CharacterProfile cp, CasterClass casterClass) {
        repository.insert(new CharacterClassEntry(cp.getId(), casterClass.getId()));
    }
    void removeVisibleClass(CharacterProfile cp, CasterClass casterClass) {
        repository.delete(new CharacterClassEntry(cp.getId(), casterClass.getId()));
    }
    void addVisibleSchool(CharacterProfile cp, School school) {
        repository.insert(new CharacterSchoolEntry(cp.getId(), school.getId()));
    }
    void removeVisibleSchool(CharacterProfile cp, School school) {
        repository.delete(new CharacterSchoolEntry(cp.getId(), school.getId()));
    }

    // Set current state to reflect that of the profile with the given name
    void setCharacter(String name) {

        // First, let's save the current character
        saveCurrentCharacter();

        // Get the profile
        // If it's null (i.e. there's no character by this name), then do nothing)
        CharacterProfile cp = repository.getCharacter(name);
        if (cp == null) {
            System.out.println("Got a null character");
            return;
        }
        profile.setValue(cp);

        // Update the character name in the settings
        preferences.edit().putString(CHARACTER_NAME_KEY, name).apply();

        // If the profile exists, then set the current values appropriately
        setQuantityBoundsFromProfile(CastingTime.CastingTimeType.class, CharacterProfile::getMinCastingTime, CharacterProfile::getMaxCastingTime);
        setQuantityBoundsFromProfile(Duration.DurationType.class, CharacterProfile::getMinDuration, CharacterProfile::getMaxDuration);
        setQuantityBoundsFromProfile(Range.RangeType.class, CharacterProfile::getMinRange, CharacterProfile::getMaxRange);

        // Filter after the update
        setFilterFlag.run();
    }

    void saveCurrentCharacter() {

        // Null checks
        if (profile == null) { return; }
        final CharacterProfile cp = profile.getValue();
        if (cp == null) { return; }

        // Update the databases
        repository.update(cp);

    }

    // Two generic helper functions for setCharacter above
    private <T extends QuantityType> void setQuantity(Class<T> type, Quantity quantity, BiConsumer<Class<? extends QuantityType>, Unit> unitSetter, BiConsumer<Class<? extends QuantityType>,Integer> valueSetter) {
        unitSetter.accept(type, quantity.unit);
        valueSetter.accept(type, quantity.value);
    }
    private <T extends QuantityType> void setQuantityBoundsFromProfile(Class<T> type, Function<CharacterProfile,Quantity> minQuantityGetter, Function<CharacterProfile,Quantity> maxQuantityGetter) {
        if (profile == null || profile.getValue() == null) { return; }
        setQuantity(type, minQuantityGetter.apply(profile.getValue()), this::setMinUnit, this::setMinValue);
        setQuantity(type, maxQuantityGetter.apply(profile.getValue()), this::setMaxUnit, this::setMaxValue);
    }

    // Delete the character profile with the given name
    void deleteCharacter(String name) { repository.deleteByName(name); }

    // For a view to observe emitters
    LiveData<Void> getSortSignal() { return sortEmitter; }
    LiveData<Void> getSourcesUpdateSignal() { return sourceUpdateEmitter; }

    // Get the LiveData for the current character name, sort options, status filter field, and min and max level
    LiveData<String> getCharacterName() { return Transformations.map(profile, CharacterProfile::getName); }
    LiveData<SortField> getFirstSortField() { return Transformations.map(profile, CharacterProfile::getFirstSortField); }
    LiveData<SortField> getSecondSortField() { return Transformations.map(profile, CharacterProfile::getSecondSortField); }
    LiveData<Boolean> getFirstSortReverse() { return Transformations.map(profile, CharacterProfile::getFirstSortReverse); }
    LiveData<Boolean> getSecondSortReverse() { return Transformations.map(profile, CharacterProfile::getSecondSortReverse); }
    LiveData<Integer> getMinLevel() { return Transformations.map(profile, CharacterProfile::getMinLevel); }
    LiveData<Integer> getMaxLevel() { return Transformations.map(profile, CharacterProfile::getMaxLevel); }
    LiveData<StatusFilterField> getStatusFilter() { return Transformations.map(profile, CharacterProfile::getStatusFilter); }

    // Observe whether the visibility flag for a Named item is set
    LiveData<Boolean> getVisibility(Named named) {
//        final Class<? extends Named> cls = named.getClass();
//        final LiveMap map = classToFlagsMap.get(cls);
//        if (map == null) { return null; }
//        for (Object x : map.getKeys()) {
//            if (x.equals(named)) {
//                return map.get(x);
//            }
//        }
//        return map.get(cls.cast(named));
        Function<CharacterProfile, Collection<? extends Named>> getter = visibleItemGetters.get(named.getClass());
        if (getter == null) { return null; }
        return Transformations.map(profile, (profile) -> getter.apply(profile).contains(named));
    }

    // Get the filter text
    String getFilterText() { return filterText.getValue(); }

    // Observe one of the yes/no filters
    private LiveData<Boolean> getProfileFilter(boolean b, Function<CharacterProfile,Boolean> tFilter, Function<CharacterProfile,Boolean> fFilter) {
        final Function<CharacterProfile,Boolean> filter = b ? tFilter : fFilter;
        return Transformations.map(profile, filter::apply);
    }
    LiveData<Boolean> getRitualFilter(boolean b) { return getProfileFilter(b, CharacterProfile::getRitualFilter, CharacterProfile::getNotRitualFilter); }
    LiveData<Boolean> getConcentrationFilter(boolean b) { return getProfileFilter(b, CharacterProfile::getConcentrationFilter, CharacterProfile::getNotConcentrationFilter); }
    LiveData<Boolean> getVerbalFilter(boolean b) { return getProfileFilter(b, CharacterProfile::getVerbalFilter, CharacterProfile::getNotVerbalFilter); }
    LiveData<Boolean> getSomaticFilter(boolean b) { return getProfileFilter(b, CharacterProfile::getSomaticFilter, CharacterProfile::getNotSomaticFilter); }
    LiveData<Boolean> getMaterialFilter(boolean b) { return getProfileFilter(b, CharacterProfile::getMaterialFilter, CharacterProfile::getNotMaterialFilter); }

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

    // Same thing, but for properties with PropertyAwareLiveData
    private <T extends BaseObservable, U> void setIfNeeded(PropertyAwareLiveData<T> liveData, Function<T,U> propertyGetter, BiConsumer<T,U> propertySetter, U u, Runnable postChangeAction) {
        if (u != propertyGetter.apply(liveData.getValue())) {
            propertySetter.accept(liveData.getValue(), u);
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
    void setFirstSortField(SortField sortField) { setIfNeeded(profile, CharacterProfile::getFirstSortField, CharacterProfile::setFirstSortField, sortField, setSortFlag); }
    void setSecondSortField(SortField sortField) { setIfNeeded(profile, CharacterProfile::getSecondSortField, CharacterProfile::setSecondSortField, sortField, setSortFlag); }
    void setFirstSortReverse(Boolean reverse) { setIfNeeded(profile, CharacterProfile::getFirstSortReverse, CharacterProfile::setFirstSortReverse, reverse, setSortFlag); }
    void setSecondSortReverse(Boolean reverse) { setIfNeeded(profile, CharacterProfile::getSecondSortReverse, CharacterProfile::setSecondSortReverse, reverse, setSortFlag); }
    void setStatusFilter(StatusFilterField sff) { setIfNeeded(profile, CharacterProfile::getStatusFilter, CharacterProfile::setStatusFilter, sff, setFilterFlag); }
    void setMinLevel(Integer level) { setIfNeeded(profile, CharacterProfile::getMinLevel, CharacterProfile::setMinLevel, level, setFilterFlag); }
    void setMaxLevel(Integer level) { setIfNeeded(profile, CharacterProfile::getMaxLevel, CharacterProfile::setMaxLevel, level, setFilterFlag); }

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

    public String classesString(Spell spell) {
        final List<String> names = new ArrayList<>();
        for (int id : spell.getClassIDs()) {
            names.add(repository.getClassNameById(id));
        }
        return TextUtils.join(", ", names);
    }
    String sourceCode(Spell spell) {
        return repository.getSourceCodeByID(spell.getSourceID());
    }

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
    boolean isFavorite(Spell spell) { return profile != null && profile.getValue() != null && repository.isFavorite(profile.getValue(), spell); }
    boolean isPrepared(Spell spell) { return profile != null && profile.getValue() != null && repository.isPrepared(profile.getValue(), spell); }
    boolean isKnown(Spell spell) { return profile != null && profile.getValue() != null && repository.isKnown(profile.getValue(), spell); }

    // Setting whether a spell is on a given spell list
    private void updateIfCurrent(Spell spell) {
        final Spell currSpell = currentSpell.getValue();
        if (currSpell != null && spell.equals(currSpell)) {
            currentSpellChanged();
        }
    }
    void setFavorite(Spell spell, boolean favorite) { repository.setFavorite(profile.getValue(), spell, favorite, (nothing) -> updateIfCurrent(spell)); }
    void setPrepared(Spell spell, boolean prepared) { repository.setPrepared(profile.getValue(), spell, prepared, (nothing) -> updateIfCurrent(spell)); }
    void setKnown(Spell spell, boolean known) { repository.setKnown(profile.getValue(), spell, known, (nothing) -> updateIfCurrent(spell)); }

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
                        if (json == null) { return; }
                        final LegacyConverter converter = new LegacyConverter(getApplication());
                        final LegacyConverter.LegacyDataBundle data = converter.profileFromJSON(json);
                        final CharacterProfile cp = data.getProfile();
                        final Collection<Source> visibleSources = data.getVisibleSources();
                        final Collection<CasterClass> visibleClasses = data.getVisibleClasses();
                        final Collection<School> visibleSchools = data.getVisibleSchools();
                        final Map<String, SpellStatus> spellStatusMap = data.getSpellStatuses();
                        addCharacter(cp);
                        for (Source source : visibleSources) {
                            addVisibleSource(cp, source);
                        }
                        for (CasterClass casterClass : visibleClasses) {
                            addVisibleClass(cp, casterClass);
                        }
                        for (School school : visibleSchools) {
                            addVisibleSchool(cp, school);
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
                final String characterName = LegacyConverter.charNameFromSettingsJSON(json);
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
                .putString(CHARACTER_NAME_KEY, profile.getValue().getName())
                .apply();
    }

    void onShutdown() {
        // Save the current character
        saveCurrentCharacter();

        // Save any values to the shared preferences
        saveSharedPreferences();
    }

}
