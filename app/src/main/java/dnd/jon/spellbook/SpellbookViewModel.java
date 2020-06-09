package dnd.jon.spellbook;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import org.apache.commons.lang3.mutable.MutableShort;
import org.javatuples.Pair;
import org.javatuples.Sextet;
import org.javatuples.Triplet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpellbookViewModel extends AndroidViewModel {

    private final SpellRepository spellRepository;
    private final CharacterRepository characterRepository;
    private final MutableLiveData<String> currentCharacterName = new MutableLiveData<>();
    private LiveData<List<Spell>> spells;
    private LiveData<List<CharacterProfile>> allCharacters;
    private final MutableLiveData<Boolean> sortNeeded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> filterNeeded = new MutableLiveData<>(false);
    private boolean onTablet;
    private final MutableLiveData<String> filterText = new MutableLiveData<>();

    // These fields describe the current sorting/filtering state for this profile
    // We keep them in the ViewModel so that it's easier to alert/receive changes from views
    // When we switch profiles, these values will get saved into the character database
    private final Map<String,SpellStatus> spellStatuses = new HashMap<>();
    private final MutableLiveData<SortField> firstSortField = new MutableLiveData<>();
    private final MutableLiveData<SortField> secondSortField = new MutableLiveData<>();
    private final MutableLiveData<Boolean> firstSortReverse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> secondSortReverse = new MutableLiveData<>();
    private final MutableLiveData<StatusFilterField> statusFilter = new MutableLiveData<>();
    private final MutableLiveData<Integer> minLevel = new MutableLiveData<>();
    private final MutableLiveData<Integer> maxLevel = new MutableLiveData<>();
    private final MutableLiveData<Boolean> ritualFilter = new MutableLiveData<>();
    private final MutableLiveData<Boolean> notRitualFilter = new MutableLiveData<>();
    private final MutableLiveData<Boolean> concentrationFilter = new MutableLiveData<>();
    private final MutableLiveData<Boolean> notConcentrationFilter = new MutableLiveData<>();
    private final MutableLiveData<Boolean> verbalFilter = new MutableLiveData<>();
    private final MutableLiveData<Boolean> notVerbalFilter = new MutableLiveData<>();
    private final MutableLiveData<Boolean> somaticFilter = new MutableLiveData<>();
    private final MutableLiveData<Boolean> notSomaticFilter = new MutableLiveData<>();
    private final MutableLiveData<Boolean> materialFilter = new MutableLiveData<>();
    private final MutableLiveData<Boolean> notMaterialFilter = new MutableLiveData<>();
    private final EnumLiveFlags<Sourcebook> visibleSourcebooks = new EnumLiveFlags<>(Sourcebook.class, (sb) -> sb == Sourcebook.PLAYERS_HANDBOOK);
    private final EnumLiveFlags<School> visibleSchools = new EnumLiveFlags<>(School.class);
    private final EnumLiveFlags<CasterClass> visibleClasses = new EnumLiveFlags<>(CasterClass.class);
    private final EnumLiveFlags<CastingTime.CastingTimeType> visibleCastingTimeTypes = new EnumLiveFlags<>(CastingTime.CastingTimeType.class);
    private final EnumLiveFlags<Duration.DurationType> visibleDurationTypes = new EnumLiveFlags<>(Duration.DurationType.class);
    private final EnumLiveFlags<Range.RangeType> visibleRangeTypes = new EnumLiveFlags<>(Range.RangeType.class);
    private final MutableLiveData<CastingTime> minCastingTime = new MutableLiveData<>();
    private final MutableLiveData<CastingTime> maxCastingTime = new MutableLiveData<>();
    private final MutableLiveData<Duration> minDuration = new MutableLiveData<>();
    private final MutableLiveData<Duration> maxDuration = new MutableLiveData<>();
    private final MutableLiveData<Range> minRange = new MutableLiveData<>();
    private final MutableLiveData<Range> maxRange = new MutableLiveData<>();

    private final MutableLiveData<Spell> currentSpell = new MutableLiveData<>();
    private LiveData<Boolean> currentSpellFavorite = Transformations.map(currentSpell, (spell) -> getStatusForSpell(spell).isFavorite());
    private LiveData<Boolean> currentSpellKnown = Transformations.map(currentSpell, (spell) -> getStatusForSpell(spell).isKnown());
    private LiveData<Boolean> currentSpellPrepared = Transformations.map(currentSpell, (spell) -> getStatusForSpell(spell).isPrepared());


    private final Map<Class<? extends Named>, LiveMap<? extends Named, Boolean>> classToFlagsMap = new HashMap<Class<? extends Named>, LiveMap<? extends Named, Boolean>>() {{
       put(CasterClass.class, visibleClasses);
       put(Sourcebook.class, visibleSourcebooks);
       put(School.class, visibleSchools);
       put(CastingTime.CastingTimeType.class, visibleCastingTimeTypes);
       put(Duration.DurationType.class, visibleDurationTypes);
       put(Range.RangeType.class, visibleRangeTypes);
    }};

    private final Map<Class<? extends QuantityType>, LiveData<Boolean>> spanningVisibilities = new HashMap<Class<? extends QuantityType>, LiveData<Boolean>>() {{
       put(CastingTime.CastingTimeType.class, getVisibility(CastingTime.CastingTimeType.spanningType()));
       put(Duration.DurationType.class, getVisibility(Duration.DurationType.spanningType()));
       put(Range.RangeType.class, getVisibility(Range.RangeType.spanningType()));
    }};

    private final Map<Class<? extends Named>, Pair<MutableLiveData<Unit>, MutableLiveData<Integer>>> minQuantityValues = new HashMap<>();
    private final Map<Class<? extends Named>, Pair<MutableLiveData<Unit>, MutableLiveData<Integer>>> maxQuantityValues = new HashMap<>();
    private static final Map<Class<? extends Named>, Pair<Unit, Integer>> defaultMinQuantityValues = new HashMap<Class<? extends Named>, Pair<Unit, Integer>>() {{
       put(CastingTime.CastingTimeType.class, new Pair<>(TimeUnit.SECOND, 0));
       put(Duration.DurationType.class, new Pair<>(TimeUnit.SECOND, 0));
       put(Range.RangeType.class, new Pair<>(LengthUnit.FOOT, 0));
    }};
    private static final Map<Class<? extends Named>, Pair<Unit, Integer>> defaultMaxQuantityValues = new HashMap<Class<? extends Named>, Pair<Unit, Integer>>() {{
        put(CastingTime.CastingTimeType.class, new Pair<>(TimeUnit.HOUR, 24));
        put(Duration.DurationType.class, new Pair<>(TimeUnit.DAY, 30));
        put(Range.RangeType.class, new Pair<>(LengthUnit.MILE, 1));
    }};

    public SpellbookViewModel(@NonNull Application application) {
        super(application);
        spellRepository = new SpellRepository(application);
        characterRepository = new CharacterRepository(application);
        spells = spellRepository.getAllSpells();
    }



    LiveData<List<Spell>> getAllSpells() { return spellRepository.getAllSpells(); }
    LiveData<List<Spell>> getVisibleSpells() {
        final CastingTime minCastingTime = this.minCastingTime.getValue();
        final CastingTime maxCastingTime = this.maxCastingTime.getValue();
        final Range minRange = this.minRange.getValue();
        final Range maxRange = this.maxRange.getValue();
        final Duration minDuration = this.minDuration.getValue();
        final Duration maxDuration = this.maxDuration.getValue();
        final Collection<String> filterNames = getCurrentFilterNames();
        return spellRepository.getVisibleSpells(filterNames, minLevel.getValue(), maxLevel.getValue(), ritualFilter.getValue(), notRitualFilter.getValue(),
                concentrationFilter.getValue(), notConcentrationFilter.getValue(), verbalFilter.getValue(), notVerbalFilter.getValue(), somaticFilter.getValue(), notSomaticFilter.getValue(),
                materialFilter.getValue(), notMaterialFilter.getValue(), visibleSourcebooks.onValues(), visibleClasses.onValues(), visibleSchools.onValues(),
                visibleCastingTimeTypes.onValues(), minCastingTime.getBaseValue(), maxCastingTime.getBaseValue(),
                visibleDurationTypes.onValues(), minDuration.getBaseValue(), maxDuration.getBaseValue(),
                visibleRangeTypes.onValues(), minRange.getBaseValue(), maxRange.getBaseValue(),
                filterText.getValue(), firstSortField.getValue(), secondSortField.getValue(), firstSortReverse.getValue(), secondSortReverse.getValue()
        );
    };
    LiveData<List<CharacterProfile>> getAllCharacters() { return characterRepository.getAllCharacters(); }
    LiveData<List<String>> getAllCharacterNames() { return characterRepository.getAllCharacterNames(); }
    int getCharactersCount() { return characterRepository.getCharactersCount(); }
    LiveData<Spell> getCurrentSpell() { return currentSpell; }

    void addCharacter(CharacterProfile cp) { characterRepository.insert(cp); }

    void setCharacter(String name) {
        // TODO : Add implementation
    }

    void deleteCharacter(String name) {
        // TODO : Add implementation
    }



    private Collection<String> getFilterNames(Predicate<SpellStatus> propertyGetter) { return spellStatuses.entrySet().stream().filter((e) -> propertyGetter.test(e.getValue())).map(Map.Entry::getKey).collect(Collectors.toList()); }
    Collection<String> getFavoriteNames() { return getFilterNames(SpellStatus::isFavorite); }
    Collection<String> getKnownNames() { return getFilterNames(SpellStatus::isKnown); }
    Collection<String> getPreparedNames() { return getFilterNames(SpellStatus::isPrepared); }
    Collection<String> getCurrentFilterNames() {
        final StatusFilterField sf = statusFilter.getValue();
        if (sf == null) { return null; }
        switch (sf) {
            case ALL:
                return null;
            case FAVORITES:
                return getFavoriteNames();
            case PREPARED:
                return getPreparedNames();
            case KNOWN:
                return getKnownNames();
        }
        return null;
    }

    LiveData<Boolean> isSortNeeded() { return sortNeeded; }
    LiveData<Boolean> isFilterNeeded() { return filterNeeded; }

    public LiveData<String> getCharacterName() { return currentCharacterName; }
    public LiveData<SortField> getFirstSortField() { return firstSortField; }
    public LiveData<SortField> getSecondSortField() { return secondSortField; }
    public LiveData<Boolean> getFirstSortReverse() { return firstSortReverse; }
    public LiveData<Boolean> getSecondSortReverse() { return secondSortReverse; }
    public LiveData<Boolean> getVisibility(Named named) {
        final Class<? extends Named> cls = named.getClass();
        final LiveMap map = classToFlagsMap.get(cls);
        return (map != null) ? map.get(cls.cast(named)) : null;
    }
//    public LiveData<Boolean> getVisibility(School school) { return visibleSchools.get(school); }
//    public LiveData<Boolean> getVisibility(Sourcebook sourcebook) { return visibleSourcebooks.get(sourcebook); }
//    public LiveData<Boolean> getVisibility(CasterClass casterClass) { return visibleClasses.get(casterClass); }
//    public LiveData<Boolean> getVisibility(CastingTime.CastingTimeType castingTimeType) { return visibleCastingTimeTypes.get(castingTimeType); }
//    public LiveData<Boolean> getVisibility(Duration.DurationType durationType) { return visibleDurationTypes.get(durationType); }
//    public LiveData<Boolean> getVisibility(Range.RangeType rangeType) { return visibleRangeTypes.get(rangeType); }
    LiveData<Boolean> getRitualFilter(boolean b) { return b ? ritualFilter : notRitualFilter; }
    LiveData<Boolean> getConcentrationFilter(boolean b) { return b ? concentrationFilter : notConcentrationFilter; }
    LiveData<Boolean> getVerbalFilter(boolean b) { return b ? verbalFilter: notVerbalFilter; }
    LiveData<Boolean> getSomaticFilter(boolean b) { return b ? somaticFilter : notSomaticFilter; }
    LiveData<Boolean> getMaterialFilter(boolean b) { return b ? materialFilter : notMaterialFilter; }
    LiveData<Unit> getMaxUnit(Class<? extends QuantityType> quantityType) { return maxQuantityValues.get(quantityType).getValue0(); }
    LiveData<Unit> getMinUnit(Class<? extends QuantityType> quantityType) { return minQuantityValues.get(quantityType).getValue0(); }
    LiveData<Integer> getMaxValue(Class<? extends QuantityType> quantityType) { return maxQuantityValues.get(quantityType).getValue1(); }
    LiveData<Integer> getMinValue(Class<? extends QuantityType> quantityType) { return minQuantityValues.get(quantityType).getValue1(); }
    LiveData<Integer> getMinLevel() { return minLevel; }
    LiveData<Integer> getMaxLevel() { return maxLevel; }
    LiveData<Boolean> getFilterNeeded() { return filterNeeded; }
    LiveData<Boolean> getSortNeeded() { return sortNeeded; }
    LiveData<String> getFilterText() { return filterText; }
    LiveData<Boolean> getSpanningTypeVisible(Class<? extends QuantityType> quantityType){ return spanningVisibilities.get(quantityType); }
    LiveData<Boolean> isCurrentSpellFavorite() { return currentSpellFavorite; }
    LiveData<Boolean> isCurrentSpellPrepared() { return currentSpellPrepared; }
    LiveData<Boolean> isCurrentSpellKnown() { return currentSpellKnown; }
    boolean areOnTablet() { return onTablet; }
    SpellStatus getStatusForSpell(Spell spell) {
        final String spellName = spell.getName();
        if (spellStatuses.containsKey(spellName)) {
            return spellStatuses.get(spellName);
        } else {
            return new SpellStatus();
        }
    }

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

    // A version with no runnable effect
    private <T> void setIfNeeded(MutableLiveData<T> liveData, T t) {
        setIfNeeded(liveData, t, null);
    }

//    // The same thing, but for live maps
//    private <K,V> void setIfNeeded(LiveMap<K,V> liveMap, K k, V v, Runnable postChangeAction) {
//        final LiveData<V> data = liveMap.get(k);
//        if (data != null && data.getValue() != v) {
//            liveMap.set(k, v);
//            postChangeAction.run();
//        }
//    }
//
//    // With no runnable effect
//    private <K,V> void setIfNeeded(LiveMap<K,V> liveMap, K k, V v) {
//        setIfNeeded(liveMap, k, v, () -> {});
//    }

    private final Runnable setSortFlag = () -> setSortNeeded(true);
    private final Runnable setFilterFlag = () -> setFilterNeeded(true);

    void setMinLevel(Integer level) { setIfNeeded(minLevel, level, setFilterFlag); }
    void setMaxLevel(Integer level) { setIfNeeded(maxLevel, level, setFilterFlag); }
    void setFirstSortField(SortField sortField) { setIfNeeded(firstSortField, sortField, setSortFlag); }
    void setSecondSortField(SortField sortField) { setIfNeeded(secondSortField, sortField, setSortFlag); }
    void setFirstSortReverse(Boolean reverse) { setIfNeeded(firstSortReverse, reverse, setSortFlag); }
    void setSecondSortReverse(Boolean reverse) { setIfNeeded(secondSortReverse, reverse, setSortFlag); }
    <T> void setFieldByLevel(T t, int level, Consumer<T> firstSetter, Consumer<T> secondSetter) {
        switch (level) {
            case 1:
                firstSetter.accept(t);
            case 2:
                secondSetter.accept(t);
        }
    }
    void setSortField(SortField sortField, int level) { setFieldByLevel(sortField, level, this::setFirstSortField, this::setSecondSortField); }
    void setSortReverse(Boolean reverse,int level) { setFieldByLevel(reverse, level, this::setFirstSortReverse, this::setSecondSortReverse); }
    void setSortNeeded(Boolean b) { setIfNeeded(sortNeeded, b); }
    void setFilterNeeded(Boolean b) { setIfNeeded(filterNeeded, b); }
    void setOnTablet(boolean onTablet) { this.onTablet = onTablet; }
    void setFilterText(String text) { setIfNeeded(filterText, text); }
    void setCurrentSpell(Spell spell) { setIfNeeded(currentSpell, spell); }
    private void setYNFilter(MutableLiveData<Boolean> filterT, MutableLiveData<Boolean> filterF, boolean tf, Boolean b) {
        final MutableLiveData<Boolean> filter = tf ? filterT : filterF;
        setIfNeeded(filter, b, setFilterFlag);
    }
    void setRitualFilter(boolean tf, Boolean b) { setYNFilter(ritualFilter, notRitualFilter, tf, b); }
    void setConcentrationFilter(boolean tf, Boolean b) { setYNFilter(concentrationFilter, notConcentrationFilter, tf, b); }
    void setVerbalFilter(boolean tf, Boolean b) { setYNFilter(verbalFilter, notVerbalFilter, tf, b); }
    void setSomaticFilter(boolean tf, Boolean b) { setYNFilter(somaticFilter, notSomaticFilter, tf, b); }
    void setMaterialFilter(boolean tf, Boolean b) { setYNFilter(materialFilter, notMaterialFilter, tf, b); }
    void setVisibility(Named named, Boolean visibility) {
        final Class<? extends Named> cls = named.getClass();
        final LiveMap map = classToFlagsMap.get(cls);
        if (map != null && map.get(named) != null) {
            if (map.get(named).getValue() != visibility) {
                map.set(named, visibility);
                setFilterNeeded(true);
            }
        }
    }


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

    private void setExtremeUnit(Map<Class<? extends Named>, Pair<MutableLiveData<Unit>, MutableLiveData<Integer>>> map, Class<? extends QuantityType> quantityType, Unit unit) {
        final Pair<MutableLiveData<Unit>, MutableLiveData<Integer>> pair = map.get(quantityType);
        if (pair == null) { return; }
        final MutableLiveData<Unit> liveData = pair.getValue0();
        if (liveData == null) { return; }
        setIfNeeded(liveData, unit, setFilterFlag);
    }
    private void setExtremeValue(Map<Class<? extends Named>, Pair<MutableLiveData<Unit>, MutableLiveData<Integer>>> map, Class<? extends QuantityType> quantityType, Integer value) {
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

    void toggleVisibility(Named named) {
        final LiveData<Boolean> data = getVisibility(named);
        if ( (data != null) && (data.getValue() != null) ) {
            setVisibility(named, !data.getValue());
        }
    }

    // Check whether a given spell is on one of the spell lists
    // It's the same for each list, so the specific lists just call this general function
    private boolean isProperty(Spell s, Function<SpellStatus,Boolean> property) {
        if (spellStatuses.containsKey(s.getName())) {
            SpellStatus status = spellStatuses.get(s.getName());
            return property.apply(status);
        }
        return false;
    }

    boolean isFavorite(Spell spell) { return isProperty(spell, SpellStatus::isFavorite); }
    boolean isPrepared(Spell spell) { return isProperty(spell, SpellStatus::isPrepared); }
    boolean isKnown(Spell spell) { return isProperty(spell, SpellStatus::isKnown); }

    // Setting whether a spell is on a given spell list
    private void setProperty(Spell s, Boolean val, BiConsumer<SpellStatus,Boolean> propSetter) {
        String spellName = s.getName();
        if (spellStatuses.containsKey(spellName)) {
            SpellStatus status = spellStatuses.get(spellName);
            if (status != null) {
                propSetter.accept(status, val);
                // spellStatuses.put(spellName, status);
                if (status.noneTrue()) { // We can remove the key if all three are false
                    spellStatuses.remove(spellName);
                }
            }
        } else if (val) { // If the key doesn't exist, we only need to modify if val is true
            SpellStatus status = new SpellStatus();
            propSetter.accept(status, true);
            spellStatuses.put(spellName, status);
        }
    }
    void setFavorite(Spell s, Boolean fav) { setProperty(s, fav, SpellStatus::setFavorite); }
    void setPrepared(Spell s, Boolean prep) { setProperty(s, prep, SpellStatus::setPrepared); }
    void setKnown(Spell s, Boolean known) { setProperty(s, known, SpellStatus::setKnown); }

    // Toggling whether a given property is set for a given spell
    private void toggleProperty(Spell s, Function<SpellStatus,Boolean> property, BiConsumer<SpellStatus,Boolean> propSetter) { setProperty(s, !isProperty(s, property), propSetter); }
    void toggleFavorite(Spell s) { toggleProperty(s, SpellStatus::isFavorite, SpellStatus::setFavorite); }
    void togglePrepared(Spell s) { toggleProperty(s, SpellStatus::isPrepared, SpellStatus::setPrepared); }
    void toggleKnown(Spell s) { toggleProperty(s, SpellStatus::isKnown, SpellStatus::setKnown); }

    <T extends Named> String[] namesArray(Collection<T> collection) {
        return collection.stream().map(T::getDisplayName).toArray(String[]::new);
    }

    <T extends Named> List<String> namesList(Collection<T> collection) {
        return Arrays.asList(namesArray(collection));
    }



}
