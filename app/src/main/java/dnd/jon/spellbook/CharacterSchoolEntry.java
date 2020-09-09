package dnd.jon.spellbook;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = SpellbookRoomDatabase.CHARACTER_SCHOOL_TABLE, primaryKeys = {"character_id, school_id"},
    foreignKeys = {@ForeignKey(entity = CharacterProfile.class, parentColumns = "id", childColumns = "character_id"), @ForeignKey(entity = School.class, parentColumns = "id", childColumns = "school_id")},
        indices = {@Index(name = "character_school_pk_index", value = {"character_id", "school_id"}, unique = true)}
)
public class CharacterSchoolEntry {

    @ColumnInfo(name = "character_id") final int characterID;
    @ColumnInfo(name = "school_id") final int schoolID;

    CharacterSchoolEntry(int characterID, int schoolID) {
        this.characterID = characterID;
        this.schoolID = schoolID;
    }

}
