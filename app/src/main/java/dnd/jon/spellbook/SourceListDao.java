package dnd.jon.spellbook;

import androidx.room.Query;

public interface SourceListDao extends DAO<SourceListDao> {

    @Query("SELECT * FROM source_lists WHERE (character_id = :characterID AND source_id = :sourceID)")
    SourceListDao getEntryByIds(int characterID, int sourceID);

}
