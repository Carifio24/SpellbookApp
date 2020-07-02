package dnd.jon.spellbook;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CharacterDao extends DAO<CharacterProfile> {

    @Insert
    void insert(CharacterProfile characterProfile);

    @Delete
    void delete(CharacterProfile profile);

    @Update
    void update(CharacterProfile profile);

    @Query("SELECT * FROM characters")
    LiveData<List<CharacterProfile>> getAllCharacters();

    @Query("SELECT COUNT(id) FROM characters")
    int getCharactersCount();

    @Query("SELECT name FROM characters")
    LiveData<List<String>> getAllCharacterNames();

    @Query("SELECT name FROM characters")
    List<String> getAllCharacterNamesStatic(); // Maybe think of a better name

    @Query("SELECT * FROM characters WHERE name = :name")
    CharacterProfile getCharacter(String name);

    @Query("DELETE FROM characters WHERE name = :name")
    void deleteByName(String name);
}
