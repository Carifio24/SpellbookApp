package dnd.jon.spellbook;

import android.app.Application;

import java.util.function.Consumer;

public class SpellListRepository {

    private final SpellListDao spellListDao;
    private final AsyncDaoTaskFactory<SpellListEntry,SpellListDao> taskFactory;

    SpellListRepository(Application application) {
        final SpellListRoomDatabase db = SpellListRoomDatabase.getDatabase(application);
        spellListDao = db.spellListDao();
        taskFactory = new AsyncDaoTaskFactory<>(spellListDao);
    }

    boolean isFavorite(CharacterProfile profile, Spell spell) {
        return spellListDao.isFavorite(profile.getId(), spell.getId());
    }

    boolean isKnown(CharacterProfile profile, Spell spell) {
        return spellListDao.isKnown(profile.getId(), spell.getId());
    }

    boolean isPrepared(CharacterProfile profile, Spell spell) {
        return spellListDao.isPrepared(profile.getId(), spell.getId());
    }

    private void insertOrUpdate(CharacterProfile profile, Spell spell, boolean value, TriConsumer<Integer,Integer,Boolean> updater, TriConsumer<Integer,Integer,Boolean> inserter) {
        final int characterID = profile.getId();
        final int spellID = spell.getId();
        final SpellListEntry entry = spellListDao.getEntryByIds(characterID, spellID);
        if (entry != null) {
            updater.accept(characterID, spellID, value);
        } else {
            inserter.accept(characterID, spellID, value);
        }
    }

    void setFavorite(CharacterProfile profile, Spell spell, boolean favorite, Consumer<Void> postAction) {
        taskFactory.makeTask((SpellListDao dao1) -> { insertOrUpdate(profile, spell, favorite, dao1::updateFavorite, dao1::insertFavorite); return null; }, postAction ).execute();
    }

    void setKnown(CharacterProfile profile, Spell spell, boolean known, Consumer<Void> postAction) {
        taskFactory.makeTask((SpellListDao dao1) -> { insertOrUpdate(profile, spell, known, dao1::updateKnown, dao1::insertKnown); return null; }, postAction ).execute();
    }

    void setPrepared(CharacterProfile profile, Spell spell, boolean prepared, Consumer<Void> postAction) {
        taskFactory.makeTask((SpellListDao dao1) -> { insertOrUpdate(profile, spell, prepared, spellListDao::updatePrepared, spellListDao::insertPrepared); return null; }, postAction).execute();
    }

}
