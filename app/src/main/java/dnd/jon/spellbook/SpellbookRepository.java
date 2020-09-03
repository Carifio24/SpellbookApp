package dnd.jon.spellbook;

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
    private final AsyncDaoTaskFactory<CasterClass, CasterClassDao> casterClassTaskFactory;
    private final AsyncDaoTaskFactory<CharacterSpellEntry, CharacterSpellDao> characterSpellTaskFactory;
    private final AsyncDaoTaskFactory<CharacterSourceEntry, CharacterSourceDao> characterSourceTaskFactory;
    private final AsyncDaoTaskFactory<CharacterClassEntry, CharacterClassDao> characterClassTaskFactory;


    SpellbookRepository(SpellbookRoomDatabase db) {
        this.db = db;
        spellTaskFactory = new AsyncDaoTaskFactory<>(db.spellDao());
        sourceTaskFactory = new AsyncDaoTaskFactory<>(db.sourceDao());
        characterTaskFactory = new AsyncDaoTaskFactory<>(db.characterDao());
        characterSpellTaskFactory = new AsyncDaoTaskFactory<>(db.characterSpellDao());
        characterSourceTaskFactory = new AsyncDaoTaskFactory<>(db.characterSourceDao());
        casterClassTaskFactory = new AsyncDaoTaskFactory<>(db.casterClassDao());
        characterClassTaskFactory = new AsyncDaoTaskFactory<>(db.characterClassDao());
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
        if (profile == null) { return db.spellDao().getAllSpells(); }
        final SimpleSQLiteQuery query = QueryUtilities.getVisibleSpellsQuery(profile, statusFilter, minLevel, maxLevel, ritualVisible, notRitualVisible, concentrationVisible, notConcentrationVisible, verbalVisible, notVerbalVisible, somaticVisible, notSomaticVisible, materialVisible, notMaterialVisible, visibleSources, visibleCasters, visibleSchools, visibleCastingTimeTypes, minCastingTimeValue, maxCastingTimeValue, visibleDurationTypes, minDurationValue, maxDurationValue, visibleRangeTypes, minRangeValue, maxRangeValue, filterText, sortField1, sortField2, reverse1, reverse2);
        return db.spellDao().getVisibleSpells(query);
    }

    ///// Sources
    void insert(Source source) { insert(source, sourceTaskFactory); }
    void update(Source source) { update(source, sourceTaskFactory); }
    void delete(Source source) { delete(source, sourceTaskFactory); }
    void addSource(Source source) { db.sourceDao().createSource(source.getName(), source.getCode(), source.isCreated()); }
    LiveData<List<Source>> getAllSources() { return db.sourceDao().getAllSources(); }
    List<Source> getAllSourcesStatic() { return db.sourceDao().getAllSourcesStatic(); }
    List<Source> getCreatedSources() { return db.sourceDao().getCreatedSources(); }
    List<Source> getBuiltInSources() { return db.sourceDao().getBuiltInSources(); }
    Source getSourceFromCode(String code) { return db.sourceDao().getSourceFromCode(code); }
    Source getSourceByID(int id) { return db.sourceDao().getSourceByID(id); }
    Source playersHandbook() { return db.sourceDao().getSourceFromCode("PHB"); }
    String getSourceCodeByID(int id) { return db.sourceDao().getSourceCodeByID(id); }


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
    void insert(CharacterSpellEntry entry) { insert(entry, characterSpellTaskFactory); }
    void update(CharacterSpellEntry entry) { update(entry, characterSpellTaskFactory); }
    void delete(CharacterSpellEntry entry) { delete(entry, characterSpellTaskFactory); }
    boolean isFavorite(CharacterProfile profile, Spell spell) { return db.characterSpellDao().isFavorite(profile.getId(), spell.getId()); }
    boolean isKnown(CharacterProfile profile, Spell spell) { return db.characterSpellDao().isKnown(profile.getId(), spell.getId()); }
    boolean isPrepared(CharacterProfile profile, Spell spell) { return db.characterSpellDao().isPrepared(profile.getId(), spell.getId()); }

    private void insertOrUpdate(CharacterProfile profile, Spell spell, boolean value, TriConsumer<Integer,Integer,Boolean> updater, TriConsumer<Integer,Integer,Boolean> inserter) {
        final int characterID = profile.getId();
        final int spellID = spell.getId();
        final CharacterSpellEntry entry = db.characterSpellDao().getEntryByIds(characterID, spellID);
        if (entry != null) {
            updater.accept(characterID, spellID, value);
        } else {
            inserter.accept(characterID, spellID, value);
        }
    }

    void setFavorite(CharacterProfile profile, Spell spell, boolean favorite, Consumer<Void> postAction) {
        characterSpellTaskFactory.makeTask((CharacterSpellDao dao) -> { insertOrUpdate(profile, spell, favorite, dao::updateFavorite, dao::insertFavorite); return null; }, postAction).execute();
    }

    void setKnown(CharacterProfile profile, Spell spell, boolean known, Consumer<Void> postAction) {
        characterSpellTaskFactory.makeTask((CharacterSpellDao dao) -> { insertOrUpdate(profile, spell, known, dao::updateKnown, dao::insertKnown); return null; }, postAction).execute();
    }

    void setPrepared(CharacterProfile profile, Spell spell, boolean prepared, Consumer<Void> postAction) {
        characterSpellTaskFactory.makeTask((CharacterSpellDao dao) -> { insertOrUpdate(profile, spell, prepared, dao::updatePrepared, dao::insertPrepared); return null; }, postAction).execute();
    }

    ///// Characters and sources
    void insert(CharacterSourceEntry entry) { insert(entry, characterSourceTaskFactory); }
    void update(CharacterSourceEntry entry) { update(entry, characterSourceTaskFactory); }
    void delete(CharacterSourceEntry entry) { delete(entry, characterSourceTaskFactory); }
    CharacterSourceEntry getEntryByIDs(int characterID, int sourceID) { return db.characterSourceDao().getEntryByIds(characterID, sourceID); }
    List<Source> getVisibleSources(int characterID) { return db.characterSourceDao().getVisibleSources(characterID); }

    ///// Classes
    // No modifying of classes yet
    List<CasterClass> getAllClasses() { return db.casterClassDao().getAllClasses(); }
    CasterClass getClassByName(String name) { return db.casterClassDao().getClassByName(name); }
    String getClassNameById(int id) { return db.casterClassDao().getClassNameById(id); }
    List<String> getAllClassNames() { return db.casterClassDao().getAllClassNames(); }


    ///// Characters and classes
    void insert(CharacterClassEntry entry) { insert(entry, characterClassTaskFactory); }
    void update(CharacterClassEntry entry) { update(entry, characterClassTaskFactory); }
    void delete(CharacterClassEntry entry) { delete(entry, characterClassTaskFactory); }
    List<CasterClass> getVisibleClasses(int characterID) { return db.characterClassDao().getVisibleClasses(characterID); }
}
