package dnd.jon.spellbook;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SpellDAO extends DAO<Spell> {

    @Query("SELECT * from spells where name = :name")
    Spell getSpellByName(String name);

    @Query("SELECT * from spells ORDER BY name")
    LiveData<List<Spell>> getAllSpells();

    @Query("SELECT * from spells WHERE source_id = :sourceID ORDER BY name")
    List<Spell> getSpellsFromSource(long sourceID);

}