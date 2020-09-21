package dnd.jon.spellbook;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SchoolDao extends DAO<School> {

    @Query("INSERT INTO schools (name) VALUES (:name)")
    void createSchool(String name);

    @Query("SELECT * FROM schools ORDER BY name")
    List<School> getAllSchools();

    @Query("SELECT name FROM schools ORDER BY name")
    List<String> getAllSchoolNames();

    @Query("SELECT * FROM schools WHERE id = :id")
    School getSchoolByID(long id);

    @Query("SELECT name FROM schools WHERE id = :id")
    String getSchoolNameByID(long id);

    @Query("SELECT * FROM schools WHERE name = :name")
    School getSchoolByName(String name);


}
