package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "sources", indices = {@Index(name = "index_sourcebooks_code", value = {"code"}, unique = true), @Index(name = "index_sourcebooks_name", value = {"name"}, unique = true)})
public class Source implements Named {

    // Member values
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") private final int id;

    @NonNull @ColumnInfo(name = "name") final private String name;
    @NonNull @ColumnInfo(name = "code") final private String code;
    @ColumnInfo(name = "created") final private boolean created;

//    static final Source PLAYERS_HANDBOOK =  new Source(0, "Player's Handbook", "PHB");
//    static final Source XANATHARS_GTE = new Source(1, "Xanathar's Guide to Everything", "XGE");
//    static final Source SWORD_COAST_AG = new Source(2, "Sword Coast Adv. Guide", "SCAG");
//    static final Source LOST_LABORATORY_OK = new Source(3,"Lost Laboratory of Kwalish", "LLK");
//    static final Source ACQUISITIONS_INCORPORATED = new Source(4, "Acquisitions Incorporated", "AI");


    // Constructors
    // For built-in values
    Source(int id, @NonNull String name, @NonNull String code, boolean created) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.created = created;
    }


    // Getters
    public final int getId() { return id; }
    @NonNull public String getName() { return name; }
    @NonNull public String getCode() { return code; }
    public boolean isCreated() { return created; }

    // For Named protocol
    public String getDisplayName() { return name; }

}
