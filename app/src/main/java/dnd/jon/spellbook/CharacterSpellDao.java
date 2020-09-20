package dnd.jon.spellbook;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface CharacterSpellDao extends DAO<CharacterSpellEntry> {

    @Query("UPDATE character_spells SET favorite = :favorite WHERE (character_id = :characterID AND spell_id = :spellID)")
    void updateFavorite(long characterID, long spellID, boolean favorite);

    @Query("UPDATE character_spells SET known = :known WHERE (character_id = :characterID AND spell_id = :spellID)")
    void updateKnown(long characterID, long spellID, boolean known);

    @Query("UPDATE character_spells SET prepared = :prepared WHERE (character_id = :characterID AND spell_id = :spellID)")
    void updatePrepared(long characterID, long spellID, boolean prepared);

    @Query("SELECT * FROM character_spells WHERE character_id = :characterID AND spell_id = :spellID")
    CharacterSpellEntry getEntryByIds(long characterID, long spellID);

    @Query("INSERT INTO character_spells (character_id, spell_id, favorite) VALUES (:characterID, :spellID, :favorite)")
    void insertFavorite(long characterID, long spellID, boolean favorite);

    @Query("INSERT INTO character_spells (character_id, spell_id, known) VALUES (:characterID, :spellID, :known)")
    void insertKnown(long characterID, long spellID, boolean known);

    @Query("INSERT INTO character_spells (character_id, spell_id, prepared) VALUES (:characterID, :spellID, :prepared)")
    void insertPrepared(long characterID, long spellID, boolean prepared);

    @Query("SELECT favorite FROM character_spells WHERE (character_id = :characterID AND spell_id = :spellID)")
    boolean isFavorite(long characterID, long spellID);

    @Query("SELECT known FROM character_spells WHERE (character_id = :characterID AND spell_id = :spellID)")
    boolean isKnown(long characterID, long spellID);

    @Query("SELECT prepared FROM character_spells WHERE (character_id = :characterID AND spell_id = :spellID)")
    boolean isPrepared(long characterID, long spellID);

}
