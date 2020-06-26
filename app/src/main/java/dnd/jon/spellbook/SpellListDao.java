package dnd.jon.spellbook;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface SpellListDao extends DAO<SpellListRoomDatabase.SpellListEntry> {

    @Query("INSERT INTO favorites (character_id, spell_id) VALUES (:characterID, :spellID)")
    void insertFavorite(String tableName, int spellID, int characterID);

}
