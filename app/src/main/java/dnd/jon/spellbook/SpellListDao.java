package dnd.jon.spellbook;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface SpellListDao extends DAO<SpellListRoomDatabase.SpellListEntry> {

    @Insert
    void insert(SpellListRoomDatabase.SpellListEntry entry);

    @Query("INSERT INTO :tableName (characted_id, spell_id) VALUES (:characterID, :spellID)")
    void insert(String tableName, int spellID, int characterID);

}
