package dnd.jon.spellbook;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

public class SpellbookViewModel extends AndroidViewModel {

    private SpellRepository spellRepository;
    private CharacterRepository characterRepository;
    private LiveData<CharacterProfile> profile;
    private LiveData<List<Spell>> spells;
    private LiveData<List<CharacterProfile>> allCharacters;
    private final MutableLiveData<Boolean> sortNeeded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> filterNeeded = new MutableLiveData<>(false);
    private final MutableLiveData<Spell> currentSpell;
    private final MutableLiveData<Boolean> onTablet = new MutableLiveData<>();
    private final MutableLiveData<String> filterText = new MutableLiveData<>();

    public SpellbookViewModel(@NonNull Application application) {
        super(application);
        spellRepository = new SpellRepository(application);
        characterRepository = new CharacterRepository(application);
        spells = spellRepository.getAllSpells();
        currentSpell = new MutableLiveData<>();
    }



    LiveData<List<Spell>> getAllSpells() { return spellRepository.getAllSpells(); }
    LiveData<List<CharacterProfile>> getAllCharacters() { return allCharacters; }
    LiveData<Spell> getCurrentSpell() { return currentSpell; }

    LiveData<Boolean> isSortNeeded() { return sortNeeded; }
    LiveData<Boolean> isFilterNeeded() { return filterNeeded; }

    LiveData<String> getCharacterName() { return Transformations.map(profile, CharacterProfile::getName); }
    LiveData<SortField> getFirstSortField() { return Transformations.map(profile, CharacterProfile::getFirstSortField); }
    LiveData<SortField> getSecondSortField() { return Transformations.map(profile, CharacterProfile::getSecondSortField); }
    LiveData<Boolean> getFirstSortReverse() { return Transformations.map(profile, CharacterProfile::getFirstSortReverse); }
    LiveData<Boolean> getSecondSortReverse() { return Transformations.map(profile, CharacterProfile::getSecondSortReverse); }
    LiveData<Boolean> getVisibilityForItem(Named item) { return Transformations.map(profile, (cp) -> cp.getVisibility(item)); }
    LiveData<Integer> getSpanningTypeVisible(Class<? extends QuantityType> type) { return Transformations.map(profile, (cp) -> cp.getSpanningTypeVisible(type)); }
    LiveData<Unit> getMaxUnit(Class<? extends QuantityType> quantityType) { return Transformations.map(profile, (cp) -> cp.getMaxUnit(quantityType)); }
    LiveData<Unit> getMinUnit(Class<? extends QuantityType> quantityType) { return Transformations.map(profile, (cp) -> cp.getMinUnit(quantityType)); }
    LiveData<Integer> getMaxValue(Class<? extends QuantityType> quantityType) { return Transformations.map(profile, (cp) -> cp.getMaxValue(quantityType)); }
    LiveData<Integer> getMinValue(Class<? extends QuantityType> quantityType) { return Transformations.map(profile, (cp) -> cp.getMinValue(quantityType)); }
    LiveData<Integer> getMinLevel() { return Transformations.map(profile, CharacterProfile::getMinSpellLevel); }
    LiveData<Integer> getMaxLevel() { return Transformations.map(profile, CharacterProfile::getMaxSpellLevel); }
    LiveData<Boolean> getFilterNeeded() { return filterNeeded; }
    LiveData<Boolean> getSortNeeded() { return sortNeeded; }
    LiveData<String> getFilterText() { return filterText; }
    boolean areOnTablet() { return onTablet.getValue(); }


    void setSortField(SortField sortField, int level) { profile.getValue().setSortField(sortField, level); }
    void setSortReverse(boolean reverse, int level) { profile.getValue().setSortReverse(reverse, level); }

    void setSortNeeded(Boolean b) { sortNeeded.setValue(b); }
    void setFilterNeeded(Boolean b) { filterNeeded.setValue(b); }
    void setOnTablet(Boolean b) { onTablet.setValue(b); }
    void setMaxUnit(Class<? extends QuantityType> quantityType, Unit unit) { profile.getValue().setMaxUnit(quantityType, unit); }
    void setMinUnit(Class<? extends QuantityType> quantityType, Unit unit) { profile.getValue().setMinUnit(quantityType, unit); }
    void setMaxValue(Class<? extends QuantityType> quantityType, int value) { profile.getValue().setMaxValue(quantityType, value); }
    void setMinValue(Class<? extends QuantityType> quantityType, int value) { profile.getValue().setMinValue(quantityType, value); }
    void setRangeToDefaults(Class<? extends QuantityType> type) { profile.getValue().setRangeToDefaults(type); }
    void setMinLevel(int level) { profile.getValue().setMinSpellLevel(level); }
    void setMaxLevel(int level) { profile.getValue().setMaxSpellLevel(level); }
    void setFilterText(String text) { filterText.setValue(text); }


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


    //



}
