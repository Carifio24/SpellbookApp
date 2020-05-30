package dnd.jon.spellbook;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Spell.class}, version = 1, exportSchema = true)
@TypeConverters({TypeConverters.class})
public abstract class SpellRoomDatabase extends RoomDatabase {

    private static SpellRoomDatabase INSTANCE;
    private static final String DB_NAME = "spell_database";
    private static final String DB_FILE = DB_NAME + ".db";

    public abstract SpellDao spellDao();

    public static SpellRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SpellRoomDatabase.class) {
                if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SpellRoomDatabase.class, DB_NAME).createFromAsset(DB_FILE).build();
                }
            }
        }
        return INSTANCE;
    }
}