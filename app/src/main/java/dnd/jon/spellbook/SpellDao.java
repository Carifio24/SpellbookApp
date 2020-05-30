package dnd.jon.spellbook;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

@Dao
public interface SpellDao {

    @Insert
    void insert(Spell spell);

    @Query("SELECT * from spells")
    LiveData<List<Spell>> getAllSpells();

    // This query is complicated, so we'll construct it at runtime as necessary
    @RawQuery
    LiveData<List<Spell>> getVisibleSpells(SupportSQLiteQuery query);


}
