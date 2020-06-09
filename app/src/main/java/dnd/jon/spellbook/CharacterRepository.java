package dnd.jon.spellbook;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class CharacterRepository {

    private CharacterDao characterDao;

    CharacterRepository(Application application) {
        final CharacterRoomDatabase db = CharacterRoomDatabase.getDatabase(application);
        characterDao = db.characterDao();
    }

    LiveData<List<CharacterProfile>> getAllCharacters() { return characterDao.getAllCharacters(); }
    LiveData<List<String>> getAllCharacterNames() { return characterDao.getAllCharacterNames(); }
    int getCharactersCount() { return characterDao.getCharactersCount(); }

    void insert(CharacterProfile cp) { new AddCharacterAsyncTask(characterDao).execute(cp); }


    // AsyncTask for adding character profiles
    private static class AddCharacterAsyncTask extends AsyncTask<CharacterProfile,Void,Void> {

        private final CharacterDao dao;

        AddCharacterAsyncTask(CharacterDao dao) { this.dao = dao; }

        @Override
        protected Void doInBackground(CharacterProfile... profiles) {
            dao.insert(profiles[0]);
            return null;
        }

    }



}
