package dnd.jon.spellbook;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = SpellbookRoomDatabase.SPELL_LISTS_TABLE, primaryKeys = {"character_id", "spell_id"},
        foreignKeys = {@ForeignKey(entity = Spell.class, parentColumns = "id", childColumns = "spell_id"), @ForeignKey(entity = CharacterProfile.class, parentColumns = "id", childColumns = "character_id")},
        indices = {@Index(name = "spell_list_pk_index", value = {"character_id", "spell_id"}, unique = true)}
        )
class SpellListEntry {

    @ColumnInfo(name = "character_id") final int characterID;
    @ColumnInfo(name = "spell_id") final int spellID;
    @ColumnInfo(name = "favorite", defaultValue = "0") final boolean favorite;
    @ColumnInfo(name = "known", defaultValue = "0") final boolean known;
    @ColumnInfo(name = "prepared", defaultValue = "0") final boolean prepared;

    SpellListEntry(int characterID, int spellID, boolean favorite, boolean known, boolean prepared) {
        this.characterID = characterID;
        this.spellID = spellID;
        this.favorite = favorite;
        this.known = known;
        this.prepared = prepared;
    }

}
