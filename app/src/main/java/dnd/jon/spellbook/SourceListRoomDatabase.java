package dnd.jon.spellbook;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {SourceListEntry.class, CharacterProfile.class, Source.class}, version = 1, exportSchema = true)
@TypeConverters({SpellbookTypeConverters.class})
public abstract class SourceListRoomDatabase extends RoomDatabase {

    private static SourceListRoomDatabase INSTANCE;
    static final String DB_NAME = "source_lists";

    public abstract SourceListDao sourceListDao();

    public static SourceListRoomDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (SourceListRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SourceListRoomDatabase.class, DB_NAME).allowMainThreadQueries().build();
                }
            }
        }
        return INSTANCE;
    }


}
