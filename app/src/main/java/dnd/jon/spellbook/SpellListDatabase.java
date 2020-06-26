package dnd.jon.spellbook;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class SpellListDatabase extends RoomDatabase {

    private static SpellListDatabase FAVORITES;
    private static SpellListDatabase KNOWN;
    private static SpellListDatabase PREPARED;

    private static final String FAVORITES_DBNAME = "favorites";
    private static final String KNOWN_DBNAME = "known";
    private static final String PREPARED_DBNAME = "prepared";

    //public abstract SpellDataDao spellDataDao();

    private static SpellListDatabase getDatabase(Context context, Supplier<SpellListDatabase> dbGetter, Consumer<SpellListDatabase> dbSetter, String dbName) {
        final SpellListDatabase db = dbGetter.get();
        if (db == null) {
            synchronized (SpellListDatabase.class) {
                if (db == null) {
                    dbSetter.accept(Room.databaseBuilder(context.getApplicationContext(), SpellListDatabase.class, dbName).allowMainThreadQueries().build());
                }
            }
        }
        return db;
    }

    public static SpellListDatabase getFavoritesDatabase(Context context) { return getDatabase(context, () -> SpellListDatabase.FAVORITES, (db) -> SpellListDatabase.FAVORITES = db, FAVORITES_DBNAME); }
    public static SpellListDatabase getKnownDatabase(Context context) { return getDatabase(context, () -> SpellListDatabase.KNOWN, (db) -> SpellListDatabase.KNOWN = db, KNOWN_DBNAME); }
    public static SpellListDatabase getPreparedDatabase(Context context) { return getDatabase(context, () -> SpellListDatabase.PREPARED, (db) -> SpellListDatabase.PREPARED = db, PREPARED_DBNAME); }

}
