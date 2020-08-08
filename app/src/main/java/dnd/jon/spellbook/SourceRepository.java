package dnd.jon.spellbook;

import java.util.List;

import android.app.Application;

import androidx.lifecycle.LiveData;

public class SourceRepository extends Repository<Source, SourceDao> {

    SourceRepository(Application application) {
        super(application, (app) -> SourceRoomDatabase.getDatabase(app).sourceDao());
    }

    LiveData<List<Source>> getAllSources() { return dao.getAllSources(); }
    Source getSourceFromCode(String code) { return dao.getSourceFromCode(code); }
    Source playersHandbook() { return dao.getSourceFromCode("PHB"); }

}
