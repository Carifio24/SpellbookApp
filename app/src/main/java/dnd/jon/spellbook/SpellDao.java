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
public interface SpellDao {

    @Insert
    void insert(Spell spell);

    @Query("SELECT * from spells")
    LiveData<List<Spell>> getAllSpells();

    // This query is complicated, so we'll construct it at runtime as necessary
    @RawQuery(observedEntities = Spell.class)
    LiveData<List<Spell>> getVisibleSpells(SupportSQLiteQuery query);

    @Update
    void update(Spell... spell);

    @Delete
    void delete(Spell...spell);


}
