package dnd.jon.spellbook;

import androidx.lifecycle.LiveData;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

public interface SpellDao {

    @Insert
    void insert(Spell spell);

    @Query("SELECT * from spells")
    LiveData<List<Spell>> getAllSpells();

}
