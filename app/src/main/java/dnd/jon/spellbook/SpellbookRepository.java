package dnd.jon.spellbook;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;


public class SpellbookRepository {

    private final SpellbookRoomDatabase db;
    private final AsyncDaoTaskFactory<Spell, SpellDao> spellTaskFactory;
    private final AsyncDaoTaskFactory<Source, SourceDao> sourceTaskFactory;
    private final AsyncDaoTaskFactory<CasterClass, CasterClassDao> casterClassTaskFactory;
    private final AsyncDaoTaskFactory<School, SchoolDao> schoolTaskFactory;
    private final AsyncDaoTaskFactory<CharacterProfile, CharacterDao> characterTaskFactory;
    private final AsyncDaoTaskFactory<CharacterSpellEntry, CharacterSpellDao> characterSpellTaskFactory;
    private final AsyncDaoTaskFactory<CharacterSourceEntry, CharacterSourceDao> characterSourceTaskFactory;
    private final AsyncDaoTaskFactory<CharacterClassEntry, CharacterClassDao> characterClassTaskFactory;
    private final AsyncDaoTaskFactory<CharacterSchoolEntry, CharacterSchoolDao> characterSchoolTaskFactory;
    private final AsyncDaoTaskFactory<SpellClassEntry, SpellClassDao> spellClassTaskFactory;


    SpellbookRepository(SpellbookRoomDatabase db) {
        this.db = db;
        spellTaskFactory = new AsyncDaoTaskFactory<>(db.spellDao());
        sourceTaskFactory = new AsyncDaoTaskFactory<>(db.sourceDao());
        schoolTaskFactory = new AsyncDaoTaskFactory<>(db.schoolDao());
        characterTaskFactory = new AsyncDaoTaskFactory<>(db.characterDao());
        characterSpellTaskFactory = new AsyncDaoTaskFactory<>(db.characterSpellDao());
        characterSourceTaskFactory = new AsyncDaoTaskFactory<>(db.characterSourceDao());
        casterClassTaskFactory = new AsyncDaoTaskFactory<>(db.casterClassDao());
        characterClassTaskFactory = new AsyncDaoTaskFactory<>(db.characterClassDao());
        characterSchoolTaskFactory = new AsyncDaoTaskFactory<>(db.characterSchoolDao());
        spellClassTaskFactory = new AsyncDaoTaskFactory<>(db.spellClassDao());
    }

    SpellbookRepository(Context context) {
        this(SpellbookRoomDatabase.getDatabase(context));
    }

    ///// C/U/D functions
    private <T, D extends DAO<T>, F extends AsyncDaoTaskFactory<T,D>> long insert(T t, F factory) {
        try {
            return factory.makeInsertTask(t).execute().get();
        }
        catch (Exception e) {
            Log.e("Insert", e.toString());
            return 0;
        }
    }
    private <T, D extends DAO<T>, F extends AsyncDaoTaskFactory<T,D>> void update(T t, F factory) { factory.makeUpdateTask(t).execute(); }
    private <T, D extends DAO<T>, F extends AsyncDaoTaskFactory<T,D>> void delete(T t, F factory) { factory.makeDeleteTask(t).execute(); }

    ///// Spells
    long insert(Spell spell) { return insert(spell, spellTaskFactory); }
    void update(Spell spell) { update(spell, spellTaskFactory); }
    void delete(Spell spell) { delete(spell, spellTaskFactory); }
    LiveData<List<Spell>> getAllSpells() { return db.spellDao().getAllSpells(); }
    List<Spell> getAllSpellsTest() { return db.spellDao().getAllSpellsTest(); }
    Spell getSpellByName(String name) { return db.spellDao().getSpellByName(name); }
    List<Spell> getSpellsFromSource(Source source) { return db.spellDao().getSpellsFromSource(source.getId()); }

    // Get the currently visible spells
    LiveData<List<Spell>> getVisibleSpells(CharacterProfile profile, String filterText) {
        if (profile == null) { return db.spellDao().getAllSpells(); }
        final SimpleSQLiteQuery query = QueryUtilities.getVisibleSpellsQuery(profile, filterText);
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
    Source getSourceByID(long id) { return db.sourceDao().getSourceByID(id); }
    Source playersHandbook() { return db.sourceDao().getSourceFromCode("PHB"); }
    String getSourceCodeByID(long id) { return db.sourceDao().getSourceCodeByID(id); }


    ///// Characters
    // Queries (R)
    LiveData<List<CharacterProfile>> getAllCharacters() { return db.characterDao().getAllCharacters(); }
    LiveData<List<String>> getAllCharacterNames() { return db.characterDao().getAllCharacterNames(); }
    List<String> getAllCharacterNamesStatic() { return db.characterDao().getAllCharacterNamesStatic(); }
    int getCharactersCount() { return db.characterDao().getCharactersCount(); }
    CharacterProfile getCharacter(String name) { return db.characterDao().getCharacter(name); }

    // For modifying the status of join table items
    private <T,U> void updateStatus(T t, U u, boolean status, BiFunction<T,U,Boolean> existence, BiConsumer<T,U> inserter, BiConsumer<T,U> deleter) {
        final Boolean exists = existence.apply(t, u);
        if (status && !exists) {
            inserter.accept(t, u);
        } else if (!status && exists) {
            deleter.accept(t, u);
        }
    }

    // Modifiers (C/U/D)
    void insert(CharacterProfile cp) { insert(cp, characterTaskFactory); }
    void update(CharacterProfile cp) { update(cp, characterTaskFactory); }
    void delete(CharacterProfile cp) { delete(cp, characterTaskFactory); }
    void deleteByName(String name) { characterTaskFactory.createTask( (CharacterDao dao, String... names) -> dao.deleteByName(names[0])).execute(name); }

    ///// Characters and spells
    void insert(CharacterSpellEntry entry) { insert(entry, characterSpellTaskFactory); }
    void update(CharacterSpellEntry entry) { update(entry, characterSpellTaskFactory); }
    void delete(CharacterSpellEntry entry) { delete(entry, characterSpellTaskFactory); }
    boolean isFavorite(CharacterProfile profile, Spell spell) { return db.characterSpellDao().isFavorite(profile.getId(), spell.getId()); }
    boolean isKnown(CharacterProfile profile, Spell spell) { return db.characterSpellDao().isKnown(profile.getId(), spell.getId()); }
    boolean isPrepared(CharacterProfile profile, Spell spell) { return db.characterSpellDao().isPrepared(profile.getId(), spell.getId()); }

    private void insertOrUpdate(CharacterProfile profile, Spell spell, boolean value, TriConsumer<Long,Long,Boolean> updater, TriConsumer<Long,Long,Boolean> inserter) {
        final long characterID = profile.getId();
        final long spellID = spell.getId();
        final CharacterSpellEntry entry = db.characterSpellDao().getEntryByIds(characterID, spellID);
        final TriConsumer<Long,Long,Boolean> executor = (entry != null) ? updater : inserter;
        executor.accept(characterID, spellID, value);
    }

    void setFavorite(CharacterProfile profile, Spell spell, boolean favorite, Consumer<Void> postAction) {
        characterSpellTaskFactory.createTask((CharacterSpellDao dao) -> { insertOrUpdate(profile, spell, favorite, dao::updateFavorite, dao::insertFavorite); return null; }, postAction).execute();
    }

    void setKnown(CharacterProfile profile, Spell spell, boolean known, Consumer<Void> postAction) {
        characterSpellTaskFactory.createTask((CharacterSpellDao dao) -> { insertOrUpdate(profile, spell, known, dao::updateKnown, dao::insertKnown); return null; }, postAction).execute();
    }

    void setPrepared(CharacterProfile profile, Spell spell, boolean prepared, Consumer<Void> postAction) {
        characterSpellTaskFactory.createTask((CharacterSpellDao dao) -> { insertOrUpdate(profile, spell, prepared, dao::updatePrepared, dao::insertPrepared); return null; }, postAction).execute();
    }

    ///// Characters and sources
    void insert(CharacterProfile cp, Source source) { insert(new CharacterSourceEntry(cp.getId(), source.getId()), characterSourceTaskFactory); }
    void delete(CharacterProfile cp, Source source) { delete(new CharacterSourceEntry(cp.getId(), source.getId()), characterSourceTaskFactory); }
    CharacterSourceEntry getEntryByIDs(long characterID, long sourceID) { return db.characterSourceDao().getEntryByIds(characterID, sourceID); }
    List<Source> getVisibleSources(long characterID) { return db.characterSourceDao().getVisibleSources(characterID); }
    private boolean entryExists(CharacterProfile cp, Source source) { return db.characterSourceDao().exists(cp.getId(), source.getId()); }
    void updateStatus(CharacterProfile cp, Source source, boolean status) { updateStatus(cp, source, status, this::entryExists, this::insert, this::delete); }

    ///// Classes
    // No modifying of classes yet
    List<CasterClass> getAllClasses() { return db.casterClassDao().getAllClasses(); }
    CasterClass getClassByName(String name) { return db.casterClassDao().getClassByName(name); }
    String getClassNameById(long id) { return db.casterClassDao().getClassNameById(id); }
    List<String> getAllClassNames() { return db.casterClassDao().getAllClassNames(); }


    ///// Characters and classes
    void insert(CharacterProfile cp, CasterClass cc) { insert(new CharacterClassEntry(cp.getId(), cc.getId()), characterClassTaskFactory); }
    void delete(CharacterProfile cp, CasterClass cc) { delete(new CharacterClassEntry(cp.getId(), cc.getId()), characterClassTaskFactory); }
    List<CasterClass> getVisibleClasses(CharacterProfile cp) { return db.characterClassDao().getVisibleClasses(cp.getId()); }
    private boolean entryExists(CharacterProfile cp, CasterClass cc) { return db.characterClassDao().exists(cp.getId(), cc.getId()); }
    void updateStatus(CharacterProfile cp, CasterClass cc, boolean status) { updateStatus(cp, cc, status, this::entryExists, this::insert, this::delete); }

    ///// Schools
    // No modifying of schools yet
    List<School> getAllSchools() { return db.schoolDao().getAllSchools(); }
    List<String> getAllSchoolNames() { return db.schoolDao().getAllSchoolNames(); }
    String getSchoolName(long schoolID) { return db.schoolDao().getSchoolNameByID(schoolID); }
    School getSchoolByID(long schoolID) { return db.schoolDao().getSchoolByID(schoolID); }

    ///// Characters and schools
    void insert(CharacterProfile cp, School school) { insert(new CharacterSchoolEntry(cp.getId(), school.getId()), characterSchoolTaskFactory); }
    void delete(CharacterProfile cp, School school) { delete(new CharacterSchoolEntry(cp.getId(), school.getId()), characterSchoolTaskFactory); }
    List<School> getVisibleSchools(CharacterProfile cp) { return db.characterSchoolDao().getVisibleSchools(cp.getId()); }
    private boolean entryExists(CharacterProfile cp, School school) { return db.characterSchoolDao().exists(cp.getId(), school.getId()); }
    void updateStatus(CharacterProfile cp, School school, boolean status) { updateStatus(cp, school, status, this::entryExists, this::insert, this::delete); }

    // Classes and spells
    void insert(SpellClassEntry entry) { insert(entry, spellClassTaskFactory); }
    void delete(SpellClassEntry entry) { delete(entry, spellClassTaskFactory); }
    List<Long> getClassIDs(Spell spell) { return db.spellClassDao().getClassIDs(spell.getId()); }


}
