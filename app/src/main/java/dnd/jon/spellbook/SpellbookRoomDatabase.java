package dnd.jon.spellbook;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.io.File;

public abstract class SpellbookRoomDatabase extends RoomDatabase {
    private static SpellbookRoomDatabase INSTANCE;
    private static final String DB_NAME = "spellbook";
    private static final String DB_ASSET_FILE = "spellbook.db";
    private static final String DB_DIR = "databases";
    private static final File dbPath = new File(DB_DIR, DB_NAME);

    // If the database file doesn't already exist
    private static void createDatabaseFileIfNeeded(Context context) {
        if ( !(dbPath.exists() && dbPath.isFile()) ) {
            System.out.println("Copying spellbook DB from assets to " + dbPath.getPath());
            AndroidUtils.copyAssetToData(context, DB_ASSET_FILE, dbPath.getPath());
        }
    }

    public static SpellbookRoomDatabase getDatabase(final Context context) {
        System.out.println("GETTING INSTANCE");
        if (INSTANCE == null) {
            System.out.println("CREATING INSTANCE");
            synchronized (SpellbookRoomDatabase.class) {
                if (INSTANCE == null) {

                    createDatabaseFileIfNeeded(context); // Create the database if we need to

                    // Then build it from the file
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SpellbookRoomDatabase.class, DB_NAME).allowMainThreadQueries().createFromFile(dbPath).build();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}