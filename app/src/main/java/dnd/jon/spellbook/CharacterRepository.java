package dnd.jon.spellbook;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class CharacterRepository {

    // The DAO
    private final CharacterDao characterDao;

    // A factory for creating tasks
    private final AsyncDaoTaskFactory<CharacterProfile,CharacterDao> taskFactory;

    CharacterRepository(Application application) {
        final CharacterRoomDatabase db = CharacterRoomDatabase.getDatabase(application);
        characterDao = db.characterDao();
        taskFactory = new AsyncDaoTaskFactory<>(characterDao);
    }

    // Queries (R)
    LiveData<List<CharacterProfile>> getAllCharacters() { return characterDao.getAllCharacters(); }
    LiveData<List<String>> getAllCharacterNames() { return characterDao.getAllCharacterNames(); }
    List<String> getAllCharacterNamesStatic() { return characterDao.getAllCharacterNamesStatic(); }
    int getCharactersCount() { return characterDao.getCharactersCount(); }
    CharacterProfile getCharacter(String name) { return characterDao.getCharacter(name); }

    // Modifiers (C/U/D)
    void insert(CharacterProfile cp) { taskFactory.makeInsertTask(cp).execute(); }
    void update(CharacterProfile cp) { taskFactory.makeUpdateTask(cp).execute(); }
    void delete(CharacterProfile cp) { taskFactory.makeDeleteTask(cp).execute(); }
    void deleteByName(String name) { taskFactory.createTask( (CharacterDao dao, String... names) -> dao.deleteByName(names[0]) ).execute(name); }




}
