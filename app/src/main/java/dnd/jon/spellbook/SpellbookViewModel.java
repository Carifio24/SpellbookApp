package dnd.jon.spellbook;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellbookViewModel extends AndroidViewModel {

    private SpellRepository spellRepository;
    private CharacterRepository characterRepository;
    private MutableLiveData<String> currentCharacterName = new MutableLiveData<>();
    private LiveData<List<Spell>> spells;
    private LiveData<List<CharacterProfile>> allCharacters;
    private final MutableLiveData<Boolean> sortNeeded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> filterNeeded = new MutableLiveData<>(false);
    private final MutableLiveData<Spell> currentSpell = new MutableLiveData<>();
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
//    private final Map<Class<? extends Named>, LiveMap<? extends Named, Boolean>> classToFlagsMap = new HashMap<Class<? extends Named>, LiveMap<? extends Named, Boolean>>() {{
//       put(CasterClass.class, visibleClasses);
//       put(Sourcebook.class, visibleSourcebooks);
//       put(School.class, visibleSchools);
//       put(CastingTime.CastingTimeType.class, visibleCastingTimeTypes);
//       put(Duration.DurationType.class, visibleDurationTypes);
//       put(Range.RangeType.class, visibleRangeTypes);
//    }};
//    private static final List<Class<? extends Named>> filterTypes = Arrays.asList(CasterClass.class, Sourcebook.class, School.class, CastingTime.CastingTimeType.class, Duration.DurationType.class, Range.RangeType.class);

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
        return spellRepository.getVisibleSpells(minLevel.getValue(), maxLevel.getValue(), ritualFilter.getValue(), notRitualFilter.getValue(),
                concentrationFilter.getValue(), notConcentrationFilter.getValue(), verbalFilter.getValue(), notVerbalFilter.getValue(), somaticFilter.getValue(), notSomaticFilter.getValue(),
                materialFilter.getValue(), notMaterialFilter.getValue(), visibleSourcebooks.onValues(), visibleClasses.onValues(), visibleSchools.onValues(),
                visibleCastingTimeTypes.onValues(), minCastingTime.getBaseValue(), maxCastingTime.getBaseValue(),
                visibleDurationTypes.onValues(), minDuration.getBaseValue(), maxDuration.getBaseValue(),
                visibleRangeTypes.onValues(), minRange.getBaseValue(), maxRange.getBaseValue(),
                filterText.getValue(), firstSortField.getValue(), secondSortField.getValue(), firstSortReverse.getValue(), secondSortReverse.getValue()
        );
    };
    LiveData<List<CharacterProfile>> getAllCharacters() { return allCharacters; }
    LiveData<Spell> getCurrentSpell() { return currentSpell; }

    LiveData<Boolean> isSortNeeded() { return sortNeeded; }
    LiveData<Boolean> isFilterNeeded() { return filterNeeded; }

    public LiveData<String> getCharacterName() { return currentCharacterName; }
    public LiveData<SortField> getFirstSortField() { return firstSortField; }
    public LiveData<SortField> getSecondSortField() { return secondSortField; }
    public LiveData<Boolean> getFirstSortReverse() { return firstSortReverse; }
    public LiveData<Boolean> getSecondSortReverse() { return secondSortReverse; }
    public LiveData<Boolean> getVisibility(School school) { return visibleSchools.get(school); }
    public LiveData<Boolean> getVisibility(Sourcebook sourcebook) { return visibleSourcebooks.get(sourcebook); }
    public LiveData<Boolean> getVisibility(CasterClass casterClass) { return visibleClasses.get(casterClass); }
    public LiveData<Boolean> getVisibility(CastingTime.CastingTimeType castingTimeType) { return visibleCastingTimeTypes.get(castingTimeType); }
    public LiveData<Boolean> getVisibility(Duration.DurationType durationType) { return visibleDurationTypes.get(durationType); }
    public LiveData<Boolean> getVisibility(Range.RangeType rangeType) { return visibleRangeTypes.get(rangeType); }
    public LiveData<Boolean> isFavorite(Spell spell) { return spellStatuses.get(); }
    public LiveData<Boolean> isPrepared(Spell spell) { return Transformations.map(profile, (cp) -> cp.isPrepared(spell)); }
    LiveData<Boolean> isKnown(Spell spell) { return Transformations.map(profile, (cp) -> cp.isKnown(spell)); }
    LiveData<Integer> getSpanningTypeVisible(Class<? extends QuantityType> type) { return Transformations.map(profile, (cp) -> cp.getSpanningTypeVisible(type)); }
    LiveData<Unit> getMaxUnit(Class<? extends QuantityType> quantityType) { return Transformations.map(profile, (cp) -> cp.getMaxUnit(quantityType)); }
    LiveData<Unit> getMinUnit(Class<? extends QuantityType> quantityType) { return Transformations.map(profile, (cp) -> cp.getMinUnit(quantityType)); }
    LiveData<Integer> getMaxValue(Class<? extends QuantityType> quantityType) { return Transformations.map(profile, (cp) -> cp.getMaxValue(quantityType)); }
    LiveData<Integer> getMinValue(Class<? extends QuantityType> quantityType) { return Transformations.map(profile, (cp) -> cp.getMinValue(quantityType)); }
    LiveData<Integer> getMinLevel() { return minLevel; }
    LiveData<Integer> getMaxLevel() { return maxLevel; }
    LiveData<Boolean> getFilterNeeded() { return filterNeeded; }
    LiveData<Boolean> getSortNeeded() { return sortNeeded; }
    LiveData<String> getFilterText() { return filterText; }
    boolean areOnTablet() { return onTablet; }

    // Use this to set LiveData that a view might need to observe
    // This will avoid infinite loops in two-way data binding
    private <T> void setIfNeeded(MutableLiveData<T> liveData, T t) {
        if (t != liveData.getValue()) { liveData.setValue(t); }
    }

    void setFirstSortField(SortField sortField) { setIfNeeded(firstSortField, sortField); }
    void setSecondSortField(SortField sortField) { setIfNeeded(secondSortField, sortField); }
    void setSortNeeded(Boolean b) { setIfNeeded(sortNeeded, b); }
    void setFilterNeeded(Boolean b) { setIfNeeded(filterNeeded, b); }
    void setOnTablet(boolean onTablet) { this.onTablet = onTablet; }
    void setFilterText(String text) { setIfNeeded(filterText, text); }


    <E extends Enum<E>> void toggleVisibilityForItem(E item) { profile.getValue().toggleVisibility(item); }
    void toggleFavorite(Spell spell) {
        if (profile.getValue() != null) {
            profile.getValue().toggleFavorite(spell);
        }
    }

    void toggleKnown(Spell spell) {
        if (profile.getValue() != null) {
            profile.getValue().toggleKnown(spell);
        }
    }

    void togglePrepared(Spell spell) {
        if (profile.getValue() != null) {
            profile.getValue().togglePrepared(spell);
        }
    }

    void setCurrentSpell(Spell spell) {
        currentSpell.setValue(spell);
    }

    LiveData<SpellStatus> statusForSpell(String spellName) {
        return Transformations.map(profile, (cp) -> cp.getStatuses().get(spellName)); }

    LiveData<SpellStatus> statusForSpell(Spell spell) {
        return statusForSpell(spell.getName());
}

    <T extends Named> String[] namesArray(Collection<T> collection) {
        return collection.stream().map(T::getDisplayName).toArray(String[]::new);
    }

    <T extends Named> List<String> namesList(Collection<T> collection) {
        return Arrays.asList(namesArray(collection));
    }



}
