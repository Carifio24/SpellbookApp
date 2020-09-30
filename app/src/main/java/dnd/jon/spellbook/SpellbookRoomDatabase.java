package dnd.jon.spellbook;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.io.File;

@Database(entities = {Spell.class, Source.class, CharacterProfile.class, CasterClass.class, School.class, CharacterSpellEntry.class, CharacterSourceEntry.class, CharacterClassEntry.class, CharacterSchoolEntry.class, SpellClassEntry.class}, version = 1, exportSchema = true)
@TypeConverters({SpellbookTypeConverters.class})
public abstract class SpellbookRoomDatabase extends RoomDatabase {

    private static SpellbookRoomDatabase INSTANCE;
    private static final String DB_NAME = "spellbook";
    private static final String DB_ASSET_FILE = "spellbook.db";
    private static final String DB_DIR = "databases";
    private static final File dbPath = new File(DB_DIR, DB_NAME);

    public abstract SpellDao spellDao();
    public abstract SourceDao sourceDao();
    public abstract CasterClassDao casterClassDao();
    public abstract SchoolDao schoolDao();
    public abstract CharacterDao characterDao();
    public abstract CharacterSpellDao characterSpellDao();
    public abstract CharacterSourceDao characterSourceDao();
    public abstract CharacterClassDao characterClassDao();
    public abstract CharacterSchoolDao characterSchoolDao();
    public abstract SpellClassDao spellClassDao();

    static final String SPELL_TABLE = "spells";
    static final String SOURCES_TABLE = "sources";
    static final String CLASSES_TABLE = "classes";
    static final String SCHOOLS_TABLE = "schools";
    static final String CHARACTERS_TABLE = "characters";
    static final String CHARACTER_SPELL_TABLE = "character_spells";
    static final String CHARACTER_SOURCE_TABLE = "character_sources";
    static final String CHARACTER_CLASS_TABLE = "character_classes";
    static final String CHARACTER_SCHOOL_TABLE = "character_schools";
    static final String SPELL_CLASS_TABLE = "spell_classes";

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

    // If the database file doesn't already exist
    private static void createDatabaseFileIfNeeded(Context context) {
        if ( !(dbPath.exists() && dbPath.isFile()) ) {
            System.out.println("Copying spellbook DB from assets to " + dbPath.getPath());
            AndroidUtils.copyAssetToData(context, DB_ASSET_FILE, dbPath.getPath());
        }
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

}
