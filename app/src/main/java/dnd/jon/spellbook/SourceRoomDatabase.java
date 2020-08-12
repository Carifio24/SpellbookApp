package dnd.jon.spellbook;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.io.File;

@Database(entities = {Source.class}, version = 1, exportSchema = true)
@TypeConverters({SpellbookTypeConverters.class})
public abstract class SourceRoomDatabase extends RoomDatabase {

    private static SourceRoomDatabase INSTANCE;
    private static final String DB_NAME = "sources";
    private static final String DB_ASSET_FILE = "sources.db";
    private static final String DB_DIR = "databases";
    private static final File dbPath = new File(DB_DIR, DB_NAME);

    public abstract SourceDao sourceDao();

    public static SourceRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SourceRoomDatabase.class) {
                if (INSTANCE == null) {

                    createDatabaseFileIfNeeded(context); // Create the database if we need to

                    // Then build it from the file
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SourceRoomDatabase.class, DB_NAME).allowMainThreadQueries().createFromFile(dbPath).build();
                }
            }
        }
        return INSTANCE;
    }

    // If the database file doesn't already exist
    private static void createDatabaseFileIfNeeded(Context context) {
        if ( !(dbPath.exists() && dbPath.isFile()) ) {
            System.out.println("Copying sources DB from assets to " + dbPath.getPath());
            AndroidUtils.copyAssetToData(context, DB_ASSET_FILE, dbPath.getPath());
        }
    }


}
