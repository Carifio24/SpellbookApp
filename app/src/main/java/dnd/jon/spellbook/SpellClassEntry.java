package dnd.jon.spellbook;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = SpellbookRoomDatabase.SPELL_CLASS_TABLE, primaryKeys = {"spell_id", "class_id"},
        foreignKeys = {@ForeignKey(entity = Spell.class, parentColumns = "id", childColumns = "spell_id"), @ForeignKey(entity = CasterClass.class, parentColumns = "id", childColumns = "class_id")},
        indices = {@Index(name = "spell_class_pk_index", value = {"spell_id", "class_id"}, unique = true)}
)
public class SpellClassEntry {

    @ColumnInfo(name = "spell_id") final int spellID;
    @ColumnInfo(name = "class_id") final int classID;

    SpellClassEntry(int spellID, int classID) {
        this.spellID = spellID;
        this.classID = classID;
    }

}
