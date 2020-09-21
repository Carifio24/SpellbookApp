package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(tableName = SpellbookRoomDatabase.SCHOOLS_TABLE, indices = {@Index(name = "index_schools_id", value = {"id"}, unique = true), @Index(name = "index_schools_name", value = {"name"}, unique = true)})
public class School implements Named {

    // Member values
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") private final int id;

    @NonNull @ColumnInfo(name = "name") final private String name;

    static School ABJURATION = new School(1, "Abjuration");

    // Getters
    @org.jetbrains.annotations.NotNull
    public String getName() { return name; }
    public int getId() { return id; }
    public String getDisplayName() { return name; }


    School(int id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof School) && ( ((School)other).getId() == id);
    }

}
