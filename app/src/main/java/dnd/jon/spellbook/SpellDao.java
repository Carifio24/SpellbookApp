package dnd.jon.spellbook;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

@Dao
public interface SpellDao extends DAO<Spell> {

    @Insert
    void insert(Spell spell);

    @Update
    void update(Spell... spell);

    @Delete
    void delete(Spell... spell);

    @Query("SELECT * from spells where name = :name")
    Spell getSpellByName(String name);

    @Query("SELECT * from spells")
    LiveData<List<Spell>> getAllSpells();

    @Query("SELECT * from spells")
    List<Spell> getAllSpellsTest();

    @Query("SELECT * from spells WHERE source_id = :sourceID ORDER BY name")
    List<Spell> getSpellsFromSource(int sourceID);

    // This query is complicated, so we'll construct it at runtime as necessary
    @RawQuery(observedEntities = {Spell.class, CharacterSpellEntry.class})
    LiveData<List<Spell>> getVisibleSpells(SupportSQLiteQuery query);


}
