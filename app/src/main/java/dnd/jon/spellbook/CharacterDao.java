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

    @Query("SELECT * from characters")
    LiveData<List<CharacterProfile>> getAllCharacters();

    @Query("SELECT COUNT(name) from characters")
    int getCharactersCount();

    @Query("SELECT name from characters")
    LiveData<List<String>> getAllCharacterNames();

    @Query("SELECT :name from characters")
    CharacterProfile getCharacter(String name);
}
