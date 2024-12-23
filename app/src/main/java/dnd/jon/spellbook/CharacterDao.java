package dnd.jon.spellbook;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CharacterDao extends DAO<CharacterProfile> {

    @Query("SELECT * FROM characters ORDER BY name")
    LiveData<List<CharacterProfile>> getAllCharacters();

    @Query("SELECT COUNT(id) FROM characters")
    int getCharactersCount();

    @Query("SELECT name FROM characters ORDER BY name")
    LiveData<List<String>> getAllCharacterNames();

    @Query("SELECT name FROM characters ORDER BY name")
    List<String> getAllCharacterNamesStatic(); // Maybe think of a better name

    @Query("SELECT * FROM characters WHERE name = :name")
    CharacterProfile getCharacter(String name);

    @Query("SELECT * FROM characters WHERE id = :characterID")
    CharacterProfile getCharacter(long characterID);

    @Query("DELETE FROM characters WHERE name = :name")
    void deleteByName(String name);
}