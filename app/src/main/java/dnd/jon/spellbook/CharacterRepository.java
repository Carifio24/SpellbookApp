package dnd.jon.spellbook;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class CharacterRepository {

    private CharacterDao characterDao;
    private LiveData<List<CharacterProfile>> allCharacters;

    CharacterRepository(Application application) {
        final CharacterRoomDatabase db = CharacterRoomDatabase.getDatabase(application);
        characterDao = db.characterDao();
        allCharacters = characterDao.getAllCharacters();
    }

    LiveData<List<CharacterProfile>> getAllCharacters() { return allCharacters; }

}
