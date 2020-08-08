package dnd.jon.spellbook;

import android.app.Application;

import java.util.function.Consumer;

public class SpellListRepository extends Repository<SpellListEntry, SpellListDao> {

    SpellListRepository(Application application) {
        super(application, (app) -> SpellListRoomDatabase.getDatabase(app).spellListDao());
    }

    boolean isFavorite(CharacterProfile profile, Spell spell) { return dao.isFavorite(profile.getId(), spell.getId()); }
    boolean isKnown(CharacterProfile profile, Spell spell) { return dao.isKnown(profile.getId(), spell.getId()); }
    boolean isPrepared(CharacterProfile profile, Spell spell) { return dao.isPrepared(profile.getId(), spell.getId()); }

    private void insertOrUpdate(CharacterProfile profile, Spell spell, boolean value, TriConsumer<Integer,Integer,Boolean> updater, TriConsumer<Integer,Integer,Boolean> inserter) {
        final int characterID = profile.getId();
        final int spellID = spell.getId();
        final SpellListEntry entry = dao.getEntryByIds(characterID, spellID);
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
        taskFactory.makeTask((SpellListDao dao1) -> { insertOrUpdate(profile, spell, prepared, dao::updatePrepared, dao::insertPrepared); return null; }, postAction).execute();
    }

}
