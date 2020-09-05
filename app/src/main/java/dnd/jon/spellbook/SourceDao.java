package dnd.jon.spellbook;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SourceDao extends DAO<Source> {

    @Insert
    void insert(Source source);

    @Delete
    void delete(Source source);

    @Update
    void update(Source source);

    @Query("INSERT INTO sources (name, code, created) VALUES (:name, :abbreviation, :created)")
    void createSource(String name, String abbreviation, boolean created);

    @Query("SELECT * FROM sources ORDER BY id")
    LiveData<List<Source>> getAllSources();

    @Query("SELECT * FROM sources ORDER BY id")
    List<Source> getAllSourcesStatic();

    @Query("SELECT * FROM sources WHERE created = 1 ORDER BY id")
    List<Source> getCreatedSources();

    @Query("SELECT * FROM sources WHERE created = 0 ORDER BY name")
    List<Source> getBuiltInSources();

    @Query("SELECT * FROM sources where code = :code")
    Source getSourceFromCode(String code);

    @Query("SELECT * FROM sources where id = :id")
    Source getSourceByID(int id);

    @Query("SELECT code FROM sources where id = :id")
    String getSourceCodeByID(int id);

}
