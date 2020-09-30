package dnd.jon.spellbook;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = SpellbookRoomDatabase.CHARACTER_SOURCE_TABLE, primaryKeys = {"character_id", "source_id"},
    foreignKeys = {@ForeignKey(entity = CharacterProfile.class, parentColumns = "id", childColumns = "character_id", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
            @ForeignKey(entity = Source.class, parentColumns = "id", childColumns = "source_id", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)},
    indices = {@Index(name = "character_source_pk_index", value = {"character_id", "source_id"}, unique = true)}
)
class CharacterSourceEntry {

    @ColumnInfo(name = "character_id") final long characterID;
    @ColumnInfo(name = "source_id") final long sourceID;

    CharacterSourceEntry(long characterID, long sourceID) {
        this.characterID = characterID;
        this.sourceID = sourceID;
    }


}
