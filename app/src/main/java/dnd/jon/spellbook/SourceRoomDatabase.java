package dnd.jon.spellbook;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.io.File;

@Database(entities = {Source.class}, version = 1, exportSchema = true)
@TypeConverters({SpellbookTypeConverters.class})
public abstract class SourceRoomDatabase extends RoomDatabase {

    private static SourceRoomDatabase INSTANCE;
    private static final String DB_NAME = "sources";
    private static final String DB_ASSET_FILE = DB_NAME + ".db";
    private static final String DB_DIR = "databases";
    private static final File dbPath = new File(DB_DIR, DB_NAME);


}
