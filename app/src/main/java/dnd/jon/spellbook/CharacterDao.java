package dnd.jon.spellbook;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CharacterDao {

    @Insert
    void insert(CharacterProfile characterProfile);

    @Query("SELECT * FROM characters")
    LiveData<List<CharacterProfile>> getAllCharacters();

    @Query("SELECT COUNT(name) FROM characters")
    int getCharactersCount();

    @Query("SELECT name FROM characters")
    LiveData<List<String>> getAllCharacterNames();

    @Query("SELECT * FROM characters WHERE name = :name")
    CharacterProfile getCharacter(String name);

    @Delete
    void deleteCharacter(CharacterProfile profile);

    @Query("DELETE FROM characters WHERE name = :name")
    void deleteCharacterByName(String name);
}
