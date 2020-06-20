package dnd.jon.spellbook;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class CharacterRepository {

    // The DAO
    private final CharacterDao characterDao;

    // A factory for creating tasks
    private final AsyncDaoTaskFactory<CharacterDao> taskFactory;

    CharacterRepository(Application application) {
        final CharacterRoomDatabase db = CharacterRoomDatabase.getDatabase(application);
        characterDao = db.characterDao();
        taskFactory = new AsyncDaoTaskFactory<>(characterDao);
    }

    // Queries (R)
    LiveData<List<CharacterProfile>> getAllCharacters() { return characterDao.getAllCharacters(); }
    LiveData<List<String>> getAllCharacterNames() { return characterDao.getAllCharacterNames(); }
    int getCharactersCount() { return characterDao.getCharactersCount(); }
    CharacterProfile getCharacter(String name) { return characterDao.getCharacter(name); }

    // Modifiers (C/U/D)
    void insert(CharacterProfile cp) { taskFactory.createTask( (CharacterDao dao, CharacterProfile... cps) -> dao.insert(cps[0]) ).execute(cp); }
    void update(CharacterProfile cp) { taskFactory.createTask( (CharacterDao dao, CharacterProfile... cps) -> dao.update(cps[0]) ).execute(cp); }
    void delete(CharacterProfile cp) { taskFactory.createTask( (CharacterDao dao, CharacterProfile... cps) -> dao.delete(cps[0]) ).execute(cp); }
    void deleteByName(String name) { taskFactory.createTask( (CharacterDao dao, String... names) -> dao.deleteByName(names[0]) ).execute(name); }




}
