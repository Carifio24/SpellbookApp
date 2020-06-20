package dnd.jon.spellbook;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.io.File;

@Database(entities = {Spell.class}, version = 1, exportSchema = true)
@TypeConverters({SpellbookTypeConverters.class})
public abstract class SpellRoomDatabase extends RoomDatabase {

    private static SpellRoomDatabase INSTANCE;
    private static final String DB_NAME = "spell_database";
    private static final String DB_FILE = DB_NAME + ".db";
    private static final String DB_DIR = "databases";

    public abstract SpellDao spellDao();

    public static SpellRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SpellRoomDatabase.class) {
                if (INSTANCE == null) {

                    createDatabaseFileIfNeeded(context); // Create the database if we need to

                    // Then build it from the file
                    final File dbPath = new File("databases", DB_FILE);
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SpellRoomDatabase.class, DB_NAME).createFromFile(dbPath).build();
                }
            }
        }
        return INSTANCE;
    }

    // If the database file doesn't
    private static void createDatabaseFileIfNeeded(Context context) {
        final File dbPath = new File(DB_DIR, DB_FILE);
        if ( !(dbPath.exists() && dbPath.isFile()) ) {
            AndroidUtils.copyAssetToData(context, DB_FILE, DB_FILE);
        }
    }

}