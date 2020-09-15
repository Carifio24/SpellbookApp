package dnd.jon.spellbook;

import android.app.Application;
import org.javatuples.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CreationManagementViewModel extends AndroidViewModel {

    private final SpellbookRepository repository;
    private Map<Source,List<Spell>> spellsBySource;

    public CreationManagementViewModel(@NonNull Application application) {
        super(application);
        repository = new SpellbookRepository(application);
        spellsBySource = makeSpellsMap();
    }

    List<Source> createdSources() { return repository.getCreatedSources(); }
    private Map<Source,List<Spell>> makeSpellsMap() {
        final List<Source> sources = createdSources();
        final Map<Source,List<Spell>> spellMap = new TreeMap<>((s1, s2) -> s1.getName().compareTo(s2.getName()));
        if (sources != null) {
            for (Source source : sources) {
                spellMap.put(source, repository.getSpellsFromSource(source));
            }
        }
        return spellMap;
    }

    void updateSpellsData() { spellsBySource = makeSpellsMap(); }

    List<List<Spell>> getSpellsForSources(List<Source> sources) {
        final List<List<Spell>> result = new ArrayList<>();
        for (Source source : sources) {
            final List<Spell> spells = repository.getSpellsFromSource(source);
            result.add(repository.getSpellsFromSource(source));
        }
        return result;
    }


    void updateSpellsForSource(Source source) { spellsBySource.put(source, repository.getSpellsFromSource(source));  }
    void addSource(Source source, List<Spell> spells) { spellsBySource.put(source, spells); }
    void addSource(Source source) { spellsBySource.put(source, new ArrayList<>()); }


    void update(Spell spell) { repository.update(spell); }
    void update(Source source) { repository.update(source); }
    void addNew(Source source) { repository.insert(source); }
    void addNew(Spell spell, int[] classIDs) {
        long spellID = repository.insert(spell);
        for (int classID : classIDs) {
            repository.insert(new SpellClassEntry(spellID, classID));
        }
    }
    void addSpellClassEntry(Spell spell, int classID) { repository.insert(new SpellClassEntry(spell.getId(), classID)); }


}
