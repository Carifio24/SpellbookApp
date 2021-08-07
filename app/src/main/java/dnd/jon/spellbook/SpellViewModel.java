package dnd.jon.spellbook;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;

public class SpellViewModel extends ViewModel {

    private final Application application;
    private final List<Spell> englishSpells;
    private final List<Spell> spells;
    private final MutableLiveData<Spell> currentSpellLD;
    private static final String englishSpellsFilename = "Spells.json";

    public SpellViewModel(Application application) {
        this.application = application;
        final String spellsFilename = application.getResources().getString(R.string.spells_filename);
        this.englishSpells = loadSpellsFromFile(englishSpellsFilename, true);
        this.spells = loadSpellsFromFile(spellsFilename, false);
        this.currentSpellLD = new MutableLiveData<>();
    }

    private List<Spell> loadSpellsFromFile(String filename, boolean useInternalParse) {
        try {
            final JSONArray jsonArray = JSONUtils.loadJSONArrayfromAsset(application, filename);
            final SpellCodec codec = new SpellCodec(application);
            return codec.parseSpellList(jsonArray, useInternalParse);
        } catch (Exception e) {
            //TODO: Better error handling?
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    LiveData<Spell> getCurrentSpell() { return currentSpellLD; }

    void setCurrentSpell(Spell spell) { currentSpellLD.setValue(spell); }

    List<Spell> getAllSpells() { return spells; }

}
