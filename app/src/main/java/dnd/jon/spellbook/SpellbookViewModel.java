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

    public SpellbookViewModel(@NonNull Application application) {
        super(application);
        spellRepository = new SpellRepository(application);
        characterRepository = new CharacterRepository(application);
        spells = spellRepository.getAllSpells();
    }

    LiveData<List<Spell>> getAllSpells() { return spellRepository.getAllSpells(); }
    LiveData<List<CharacterProfile>> getAllCharacters() { return allCharacters; }

    LiveData<String> getCharacterName() { return Transformations.map(profile, CharacterProfile::getName); }
    LiveData<SortField> getFirstSortField() { return Transformations.map(profile, CharacterProfile::getFirstSortField); }
    LiveData<SortField> getSecondSortField() { return Transformations.map(profile, CharacterProfile::getSecondSortField); }
    LiveData<Boolean> getVisibilityForItem(Named item) { return Transformations.map(profile, (cp) -> cp.getVisibility(item)); }
    LiveData

    // TODO - Add a real implementation; this is dummy code
    LiveData<SpellStatus> statusForSpell(Spell spell) {
        final MutableLiveData<SpellStatus> status = new MutableLiveData<>();
        status.setValue(new SpellStatus(false, false, false));
        return status;
    }


    //



}
