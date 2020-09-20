package dnd.jon.spellbook;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SpellClassDao extends DAO<SpellClassEntry> {

    @Query("SELECT class_id FROM spell_classes WHERE spell_id = :spellID")
    List<Long> getClassIDs(long spellID);

}
