package dnd.jon.spellbook;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {SpellListEntry.class, CharacterProfile.class, Spell.class}, version = 1, exportSchema = true)
@TypeConverters({SpellbookTypeConverters.class})
public abstract class SpellListRoomDatabase extends RoomDatabase {

    private static SpellListRoomDatabase INSTANCE;

    static final String DB_NAME = "spell_lists";

    public abstract SpellListDao spellListDao();

    public static SpellListRoomDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (SpellListRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SpellListRoomDatabase.class, DB_NAME).allowMainThreadQueries().build();
                }
            }
        }
        return INSTANCE;
    }

}
