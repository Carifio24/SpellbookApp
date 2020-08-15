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

    @Query("SELECT * FROM sources")
    LiveData<List<Source>> getAllSources();

    @Query("SELECT * FROM sources")
    List<Source> getAllSourcesStatic();

    @Query("SELECT * FROM sources WHERE created = 1")
    LiveData<List<Source>> getCreatedSources();

    @Query("SELECT * FROM sources WHERE created = 0")
    List<Source> getOriginalSources();

    @Query("SELECT * FROM sources where code = :code")
    Source getSourceFromCode(String code);

    @Query("SELECT * FROM sources where id = :id")
    Source getSourceByID(int id);

}
