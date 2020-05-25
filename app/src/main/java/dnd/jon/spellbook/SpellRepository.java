package dnd.jon.spellbook;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class SpellRepository {

    private SpellDao spellDao;
    private LiveData<List<Spell>> allSpells;

    SpellRepository(Application application) {
        final SpellRoomDatabase db = SpellRoomDatabase.getDatabase(application);
        spellDao = db.spellDao();
        allSpells = spellDao.getAllSpells();
    }

    LiveData<List<Spell>> getAllSpells() { return allSpells; }

}
