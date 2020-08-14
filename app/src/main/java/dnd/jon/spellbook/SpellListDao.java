package dnd.jon.spellbook;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface SpellListDao extends DAO<SpellListEntry> {

    @Query("UPDATE spell_lists SET favorite = :favorite WHERE (character_id = :characterID AND spell_id = :spellID)")
    void updateFavorite(int characterID, int spellID, boolean favorite);

    @Query("UPDATE spell_lists SET known = :known WHERE (character_id = :characterID AND spell_id = :spellID)")
    void updateKnown(int characterID, int spellID, boolean known);

    @Query("UPDATE spell_lists SET prepared = :prepared WHERE (character_id = :characterID AND spell_id = :spellID)")
    void updatePrepared(int characterID, int spellID, boolean prepared);

    @Query("SELECT * FROM spell_lists WHERE character_id = :characterID AND spell_id = :spellID")
    SpellListEntry getEntryByIds(int characterID, int spellID);

    @Query("INSERT INTO spell_lists (character_id, spell_id, favorite) VALUES (:characterID, :spellID, :favorite)")
    void insertFavorite(int characterID, int spellID, boolean favorite);

    @Query("INSERT INTO spell_lists (character_id, spell_id, known) VALUES (:characterID, :spellID, :known)")
    void insertKnown(int characterID, int spellID, boolean known);

    @Query("INSERT INTO spell_lists (character_id, spell_id, prepared) VALUES (:characterID, :spellID, :prepared)")
    void insertPrepared(int characterID, int spellID, boolean prepared);

    @Query("SELECT favorite FROM spell_lists WHERE (character_id = :characterID AND spell_id = :spellID)")
    boolean isFavorite(int characterID, int spellID);

    @Query("SELECT known FROM spell_lists WHERE (character_id = :characterID AND spell_id = :spellID)")
    boolean isKnown(int characterID, int spellID);

    @Query("SELECT prepared FROM spell_lists WHERE (character_id = :characterID AND spell_id = :spellID)")
    boolean isPrepared(int characterID, int spellID);

}
