package dnd.jon.spellbook;

import android.app.Application;

import java.util.List;

public class SourceListRepository extends Repository<SourceListEntry, SourceListDao> {

    SourceListRepository(Application application) {
        super(application, (app) -> SourceListRoomDatabase.getDatabase(app).sourceListDao());
    }

    SourceListEntry getEntryByIDs(int characterID, int sourceID) { return dao.getEntryByIds(characterID, sourceID); }
    List<Source> getVisibleSources(int characterID) { return dao.getVisibleSources(characterID); }

}