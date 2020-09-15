package dnd.jon.spellbook;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CasterClassDao extends DAO<CasterClass> {

    @Query("SELECT * FROM classes")
    List<CasterClass> getAllClasses();

    @Query("SELECT name FROM classes")
    List<String> getAllClassNames();

    @Query("SELECT * FROM classes WHERE id = :id")
    CasterClass getClassByID(long id);

    @Query("SELECT * FROM classes WHERE name = :name")
    CasterClass getClassByName(String name);

    @Query("SELECT name FROM classes WHERE id = :classID")
    String getClassNameById(long classID);

}
