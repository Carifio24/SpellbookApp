package dnd.jon.spellbook;

import java.util.List;

import android.app.Application;

import androidx.lifecycle.LiveData;

public class SourceRepository {

    private final SourceDao sourceDao;
    private final AsyncDaoTaskFactory<Source,SourceDao> taskFactory;

    SourceRepository(Application application) {
        final SourceRoomDatabase db = SourceRoomDatabase.getDatabase(application);
        sourceDao = db.sourceDao();
        taskFactory = new AsyncDaoTaskFactory<>(sourceDao);
    }

    LiveData<List<Source>> getAllSources() { return sourceDao.getAllSources(); }
    Source getSourceFromCode(String code) { return sourceDao.getSourceFromCode(code); }
    Source playersHandbook() { return sourceDao.getSourceFromCode("PHB"); }

    // Modifiers (C/U/D)
    void insert(Source source) { taskFactory.makeInsertTask(source).execute(); }
    void update(Source source) { taskFactory.makeUpdateTask(source).execute(); }
    void delete(Source source) { taskFactory.makeDeleteTask(source).execute(); }



}
