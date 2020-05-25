package dnd.jon.spellbook;

import androidx.lifecycle.LiveData;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

public interface CharacterDao {

    @Insert
    void insert(CharacterProfile characterProfile);

    @Query("SELECT * from characters")
    LiveData<List<CharacterProfile>> getAllCharacters();

}
