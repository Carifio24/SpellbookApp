package dnd.jon.spellbook;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;


public class SpellbookRepository {

    private final SpellbookRoomDatabase db;
    private final AsyncDaoTaskFactory<Spell, SpellDao> spellTaskFactory;
    private final AsyncDaoTaskFactory<Source, SourceDao> sourceTaskFactory;
    private final AsyncDaoTaskFactory<CharacterProfile, CharacterDao> characterTaskFactory;
    private final AsyncDaoTaskFactory<SpellListEntry, SpellListDao> spellListTaskFactory;
    private final AsyncDaoTaskFactory<SourceListEntry, SourceListDao> sourceListTaskFactory;

    SpellbookRepository(SpellbookRoomDatabase db) {
        this.db = db;
        spellTaskFactory = new AsyncDaoTaskFactory<>(db.spellDao());
        sourceTaskFactory = new AsyncDaoTaskFactory<>(db.sourceDao());
        characterTaskFactory = new AsyncDaoTaskFactory<>(db.characterDao());
        spellListTaskFactory = new AsyncDaoTaskFactory<>(db.spellListDao());
        sourceListTaskFactory = new AsyncDaoTaskFactory<>(db.sourceListDao());
    }

    SpellbookRepository(Context context) {
        this(SpellbookRoomDatabase.getDatabase(context));
    }

    ///// C/U/D functions
    private <T, D extends DAO<T>, F extends AsyncDaoTaskFactory<T,D>> void insert(T t, F factory) { factory.makeInsertTask(t).execute(); }
    private <T, D extends DAO<T>, F extends AsyncDaoTaskFactory<T,D>> void update(T t, F factory) { factory.makeUpdateTask(t).execute(); }
    private <T, D extends DAO<T>, F extends AsyncDaoTaskFactory<T,D>> void delete(T t, F factory) { factory.makeDeleteTask(t).execute(); }

    ///// Spells
    void insert(Spell spell) { insert(spell, spellTaskFactory); }
    void update(Spell spell) { update(spell, spellTaskFactory); }
    void delete(Spell spell) { delete(spell, spellTaskFactory); }
    LiveData<List<Spell>> getAllSpells() { return db.spellDao().getAllSpells(); }
    List<Spell> getAllSpellsTest() { return db.spellDao().getAllSpellsTest(); }
    Spell getSpellByName(String name) { return db.spellDao().getSpellByName(name); }
    List<Spell> getSpellsFromSource(Source source) { return db.spellDao().getSpellsFromSource(source.getId()); }

    // Get the currently visible spells
    LiveData<List<Spell>> getVisibleSpells(CharacterProfile profile, StatusFilterField statusFilter, int minLevel, int maxLevel, boolean ritualVisible, boolean notRitualVisible, boolean concentrationVisible, boolean notConcentrationVisible,
                                           boolean verbalVisible, boolean notVerbalVisible, boolean somaticVisible, boolean notSomaticVisible, boolean materialVisible, boolean notMaterialVisible,
                                           Collection<Source> visibleSources, Collection<CasterClass> visibleCasters, Collection<School> visibleSchools, Collection<CastingTime.CastingTimeType> visibleCastingTimeTypes,
                                           int minCastingTimeValue, int maxCastingTimeValue, Collection<Duration.DurationType> visibleDurationTypes, int minDurationValue, int maxDurationValue,
                                           Collection<Range.RangeType> visibleRangeTypes, int minRangeValue, int maxRangeValue, String filterText, SortField sortField1, SortField sortField2, boolean reverse1, boolean reverse2) {
        final SimpleSQLiteQuery query = QueryUtilities.getVisibleSpellsQuery(profile, statusFilter, minLevel, maxLevel, ritualVisible, notRitualVisible, concentrationVisible, notConcentrationVisible, verbalVisible, notVerbalVisible, somaticVisible, notSomaticVisible, materialVisible, notMaterialVisible, visibleSources, visibleCasters, visibleSchools, visibleCastingTimeTypes, minCastingTimeValue, maxCastingTimeValue, visibleDurationTypes, minDurationValue, maxDurationValue, visibleRangeTypes, minRangeValue, maxRangeValue, filterText, sortField1, sortField2, reverse1, reverse2);
        return db.spellDao().getVisibleSpells(query);
    }

    ///// Sources
    void insert(Source source) { insert(source, sourceTaskFactory); }
    void update(Source source) { update(source, sourceTaskFactory); }
    void delete(Source source) { delete(source, sourceTaskFactory); }
    LiveData<List<Source>> getAllSources() { return db.sourceDao().getAllSources(); }
    List<Source> getAllSourcesStatic() { return db.sourceDao().getAllSourcesStatic(); }
    LiveData<List<Source>> getCreatedSources() { return db.sourceDao().getCreatedSources(); }
    Source getSourceFromCode(String code) { return db.sourceDao().getSourceFromCode(code); }
    Source getSourceByID(int id) { return db.sourceDao().getSourceByID(id); }
    Source playersHandbook() { return db.sourceDao().getSourceFromCode("PHB"); }


    ///// Characters
    // Queries (R)
    LiveData<List<CharacterProfile>> getAllCharacters() { return db.characterDao().getAllCharacters(); }
    LiveData<List<String>> getAllCharacterNames() { return db.characterDao().getAllCharacterNames(); }
    List<String> getAllCharacterNamesStatic() { return db.characterDao().getAllCharacterNamesStatic(); }
    int getCharactersCount() { return db.characterDao().getCharactersCount(); }
    CharacterProfile getCharacter(String name) { return db.characterDao().getCharacter(name); }

    // Modifiers (C/U/D)
    void insert(CharacterProfile cp) { insert(cp, characterTaskFactory); }
    void update(CharacterProfile cp) { update(cp, characterTaskFactory); }
    void delete(CharacterProfile cp) { delete(cp, characterTaskFactory); }
    void deleteByName(String name) { characterTaskFactory.createTask( (CharacterDao dao, String... names) -> dao.deleteByName(names[0]) ).execute(name); }



    ///// Characters and spells
    void insert(SpellListEntry entry) { insert(entry, spellListTaskFactory); }
    void update(SpellListEntry entry) { update(entry, spellListTaskFactory); }
    void delete(SpellListEntry entry) { delete(entry, spellListTaskFactory); }
    boolean isFavorite(CharacterProfile profile, Spell spell) { return db.spellListDao().isFavorite(profile.getId(), spell.getId()); }
    boolean isKnown(CharacterProfile profile, Spell spell) { return db.spellListDao().isKnown(profile.getId(), spell.getId()); }
    boolean isPrepared(CharacterProfile profile, Spell spell) { return db.spellListDao().isPrepared(profile.getId(), spell.getId()); }

    private void insertOrUpdate(CharacterProfile profile, Spell spell, boolean value, TriConsumer<Integer,Integer,Boolean> updater, TriConsumer<Integer,Integer,Boolean> inserter) {
        final int characterID = profile.getId();
        final int spellID = spell.getId();
        final SpellListEntry entry = db.spellListDao().getEntryByIds(characterID, spellID);
        if (entry != null) {
            updater.accept(characterID, spellID, value);
        } else {
            inserter.accept(characterID, spellID, value);
        }
    }

    void setFavorite(CharacterProfile profile, Spell spell, boolean favorite, Consumer<Void> postAction) {
        spellListTaskFactory.makeTask((SpellListDao dao) -> { insertOrUpdate(profile, spell, favorite, dao::updateFavorite, dao::insertFavorite); return null; }, postAction).execute();
    }

    void setKnown(CharacterProfile profile, Spell spell, boolean known, Consumer<Void> postAction) {
        spellListTaskFactory.makeTask((SpellListDao dao) -> { insertOrUpdate(profile, spell, known, dao::updateKnown, dao::insertKnown); return null; }, postAction).execute();
    }

    void setPrepared(CharacterProfile profile, Spell spell, boolean prepared, Consumer<Void> postAction) {
        spellListTaskFactory.makeTask((SpellListDao dao) -> { insertOrUpdate(profile, spell, prepared, dao::updatePrepared, dao::insertPrepared); return null; }, postAction).execute();
    }

    ///// Characters and sources
    void insert(SourceListEntry entry) { insert(entry, sourceListTaskFactory); }
    void update(SourceListEntry entry) { update(entry, sourceListTaskFactory); }
    void delete(SourceListEntry entry) { delete(entry, sourceListTaskFactory); }
    SourceListEntry getEntryByIDs(int characterID, int sourceID) { return db.sourceListDao().getEntryByIds(characterID, sourceID); }
    List<Source> getVisibleSources(int characterID) { return db.sourceListDao().getVisibleSources(characterID); }

}
