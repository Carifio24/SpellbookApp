package dnd.jon.spellbook;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

@Dao
public interface SpellDao extends DAO<Spell> {

    @Query("SELECT * from spells where name = :name")
    Spell getSpellByName(String name);

    @Query("SELECT * from spells ORDER BY name")
    LiveData<List<Spell>> getAllSpells();

    @Query("SELECT * from spells ORDER BY name")
    List<Spell> getAllSpellsTest();

    @Query("SELECT * from spells WHERE source_id = :sourceID ORDER BY name")
    List<Spell> getSpellsFromSource(long sourceID);

//    @Query("SELECT * FROM spells " +
//            // Select only the source IDs for this character profile
//            "INNER JOIN (SELECT source_id FROM character_sources WHERE character_id = :characterID) AS csi ON spells.source_id = csi.source_id " +
//            // Select only the spells that are usable by the classes visible for this profile
//            "INNER JOIN (SELECT spell_id FROM spell_classes INNER JOIN (SELECT class_id FROM character_classes WHERE character_id = :characterID) AS cci ON spell_classes.class_id = cci.class_id) AS scci ON spells.id = scci.spell_id " +
//            // Filtering conditions
//            "WHERE casting_time_base_value BETWEEN :ctMin AND :ctMax"
//    )
    //LiveData<List<Spell>> getVisibleSpells(long characterID);

    @Query("SELECT * FROM " +
            "    (SELECT * FROM spells " +
            "    INNER JOIN " +
            "        (SELECT source_id FROM character_sources WHERE character_id = :characterID) AS cs " +
            "        ON spells.source_id = cs.source_id ) " +
            "    AS r1 " +
            "    INNER JOIN " +
            "    (SELECT id FROM spells " +
            "        INNER JOIN (SELECT spell_id FROM spell_classes " +
            "            INNER JOIN (SELECT class_id FROM character_classes WHERE character_id = :characterID) AS cci ON spell_classes.class_id = cci.class_id GROUP BY spell_id) AS scci ON spells.id = scci.spell_id) AS r2 " +
            "        ON r1.id = r2.id " +
            "    WHERE (casting_time_base_value BETWEEN :minCT AND :maxCT) AND (duration_base_value BETWEEN :minDur AND :maxDur) AND (range_base_value BETWEEN :minRg AND :maxRg) ORDER BY name")
    LiveData<List<Spell>> basicVisibleQuery(long characterID, int minCT, int maxCT, int minDur, int maxDur, int minRg, int maxRg);

    // This query is complicated, so we'll construct it at runtime as necessary
    @RawQuery(observedEntities = {Spell.class, CharacterSpellEntry.class})
    LiveData<List<Spell>> getVisibleSpells(SupportSQLiteQuery query);

}
