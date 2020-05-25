package dnd.jon.spellbook;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {CharacterProfile.class}, version = 1, exportSchema = true)
@androidx.room.TypeConverters({TypeConverters.class})
public abstract class CharacterRoomDatabase extends RoomDatabase {

    private static CharacterRoomDatabase INSTANCE;
    private static final String DB_NAME = "character_database";
    private static final String DB_FILE = DB_NAME + ".db";

    public abstract CharacterDao characterDao();

    public static CharacterRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CharacterRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), CharacterRoomDatabase.class, DB_NAME).createFromAsset(DB_FILE).build();
                }
            }
        }
        return INSTANCE;
    }

}
