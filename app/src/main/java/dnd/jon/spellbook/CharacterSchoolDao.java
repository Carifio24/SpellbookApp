package dnd.jon.spellbook;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CharacterSchoolDao extends DAO<CharacterSchoolEntry> {

    @Query("SELECT * FROM character_schools WHERE (character_id = :characterID AND school_id = :schoolID)")
    CharacterSchoolEntry getEntryByIds(long characterID, long schoolID);

    @Query("SELECT * FROM schools INNER JOIN (SELECT school_id FROM character_schools WHERE character_id = :characterID) AS res ON schools.id = res.school_id")
    List<School> getVisibleSchools(long characterID);

}
