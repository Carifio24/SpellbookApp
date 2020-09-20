package dnd.jon.spellbook;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = SpellbookRoomDatabase.CHARACTER_CLASS_TABLE, primaryKeys = {"character_id", "class_id"},
    foreignKeys = {@ForeignKey(entity = CharacterProfile.class, parentColumns = "id", childColumns = "character_id"), @ForeignKey(entity = CasterClass.class, parentColumns = "id", childColumns = "class_id")},
    indices = {@Index(name = "character_class_pk_index", value = {"character_id", "class_id"}, unique = true)}
)
public class CharacterClassEntry {

    @ColumnInfo(name = "character_id") final long characterID;
    @ColumnInfo(name = "class_id") final long classID;

    CharacterClassEntry(long characterID, long classID) {
        this.characterID = characterID;
        this.classID = classID;
    }

}
