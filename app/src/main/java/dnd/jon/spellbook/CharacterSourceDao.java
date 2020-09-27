package dnd.jon.spellbook;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CharacterSourceDao extends DAO<CharacterSourceEntry> {

    @Query("SELECT EXISTS (SELECT 1 FROM character_sources WHERE character_id = :characterID AND source_id = :sourceID)")
    boolean exists(long characterID, long sourceID);

    @Query("SELECT * FROM character_sources WHERE (character_id = :characterID AND source_id = :sourceID)")
    CharacterSourceEntry getEntryByIds(long characterID, long sourceID);

    @Query("SELECT * FROM sources INNER JOIN (SELECT source_id FROM character_sources WHERE character_id = :characterID) AS res ON sources.id = res.source_id")
    List<Source> getVisibleSources(long characterID);

}
