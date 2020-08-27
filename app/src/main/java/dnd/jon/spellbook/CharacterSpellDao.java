package dnd.jon.spellbook;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface CharacterSpellDao extends DAO<CharacterSpellEntry> {

    @Query("UPDATE character_spells SET favorite = :favorite WHERE (character_id = :characterID AND spell_id = :spellID)")
    void updateFavorite(int characterID, int spellID, boolean favorite);

    @Query("UPDATE character_spells SET known = :known WHERE (character_id = :characterID AND spell_id = :spellID)")
    void updateKnown(int characterID, int spellID, boolean known);

    @Query("UPDATE character_spells SET prepared = :prepared WHERE (character_id = :characterID AND spell_id = :spellID)")
    void updatePrepared(int characterID, int spellID, boolean prepared);

    @Query("SELECT * FROM character_spells WHERE character_id = :characterID AND spell_id = :spellID")
    CharacterSpellEntry getEntryByIds(int characterID, int spellID);

    @Query("INSERT INTO character_spells (character_id, spell_id, favorite) VALUES (:characterID, :spellID, :favorite)")
    void insertFavorite(int characterID, int spellID, boolean favorite);

    @Query("INSERT INTO character_spells (character_id, spell_id, known) VALUES (:characterID, :spellID, :known)")
    void insertKnown(int characterID, int spellID, boolean known);

    @Query("INSERT INTO character_spells (character_id, spell_id, prepared) VALUES (:characterID, :spellID, :prepared)")
    void insertPrepared(int characterID, int spellID, boolean prepared);

    @Query("SELECT favorite FROM character_spells WHERE (character_id = :characterID AND spell_id = :spellID)")
    boolean isFavorite(int characterID, int spellID);

    @Query("SELECT known FROM character_spells WHERE (character_id = :characterID AND spell_id = :spellID)")
    boolean isKnown(int characterID, int spellID);

    @Query("SELECT prepared FROM character_spells WHERE (character_id = :characterID AND spell_id = :spellID)")
    boolean isPrepared(int characterID, int spellID);

}
