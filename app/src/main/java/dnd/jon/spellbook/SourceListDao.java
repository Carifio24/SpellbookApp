package dnd.jon.spellbook;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SourceListDao extends DAO<SourceListEntry> {

    @Query("SELECT * FROM source_lists WHERE (character_id = :characterID AND source_id = :sourceID)")
    SourceListEntry getEntryByIds(int characterID, int sourceID);

    @Query("SELECT * FROM sources INNER JOIN (SELECT source_id FROM source_lists WHERE character_id = :characterID) res ON sources.id = res.source_id")
    List<Source> getVisibleSources(int characterID);

}
