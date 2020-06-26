package dnd.jon.spellbook;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Database(entities = {SpellListRoomDatabase.SpellListEntry.class}, version = 1, exportSchema = true)
public abstract class SpellListRoomDatabase extends RoomDatabase {

    private static SpellListRoomDatabase FAVORITES;
    private static SpellListRoomDatabase KNOWN;
    private static SpellListRoomDatabase PREPARED;

    private static final String FAVORITES_DBNAME = "favorites";
    private static final String KNOWN_DBNAME = "known";
    private static final String PREPARED_DBNAME = "prepared";

    //public abstract SpellListDao spellListDao();

    private static SpellListRoomDatabase getDatabase(Context context, Supplier<SpellListRoomDatabase> dbGetter, Consumer<SpellListRoomDatabase> dbSetter, String dbName) {
        final SpellListRoomDatabase db = dbGetter.get();
        if (db == null) {
            synchronized (SpellListRoomDatabase.class) {
                if (db == null) {
                    dbSetter.accept(Room.databaseBuilder(context.getApplicationContext(), SpellListRoomDatabase.class, dbName).allowMainThreadQueries().build());
                }
            }
        }
        return db;
    }

    public static SpellListRoomDatabase getFavoritesDatabase(Context context) { return getDatabase(context, () -> SpellListRoomDatabase.FAVORITES, (db) -> SpellListRoomDatabase.FAVORITES = db, FAVORITES_DBNAME); }
    public static SpellListRoomDatabase getKnownDatabase(Context context) { return getDatabase(context, () -> SpellListRoomDatabase.KNOWN, (db) -> SpellListRoomDatabase.KNOWN = db, KNOWN_DBNAME); }
    public static SpellListRoomDatabase getPreparedDatabase(Context context) { return getDatabase(context, () -> SpellListRoomDatabase.PREPARED, (db) -> SpellListRoomDatabase.PREPARED = db, PREPARED_DBNAME); }


    // The class that lives inside this database
    @Entity
    static class SpellListEntry {
        @ColumnInfo(name = "character_id") final int characterID;
        @ColumnInfo(name = "spell_id") final int spellID;

        SpellListEntry(int characterID, int spellID) {
            this.characterID = characterID;
            this.spellID = spellID;
        }

    }

}
