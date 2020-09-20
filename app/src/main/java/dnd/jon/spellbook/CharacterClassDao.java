package dnd.jon.spellbook;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CharacterClassDao extends DAO<CharacterClassEntry> {

    @Query("SELECT * FROM character_classes WHERE (character_id = :characterID AND class_id = :classID)")
    CharacterClassEntry getEntryByIds(long characterID, long classID);

    @Query("SELECT * FROM classes INNER JOIN (SELECT class_id FROM character_classes WHERE character_id = :characterID) AS res ON classes.id = res.class_id")
    List<CasterClass> getVisibleClasses(long characterID);

}
