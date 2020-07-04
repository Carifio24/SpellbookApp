package dnd.jon.spellbook;

import android.util.SparseArray;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(tableName = "sources", indices = {@Index(name = "index_sourcebooks_id", value = {"id"}, unique = true)})
public class Source implements Named {

    // Member values
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") final private int id;

    @NonNull @ColumnInfo(name = "name") final private String name;
    @NonNull @ColumnInfo(name = "code") final private String code;
    @ColumnInfo(name = "created") final private boolean created;

    static final Source PLAYERS_HANDBOOK =  new Source(0, "Player's Handbook", "PHB");
    static final Source XANATHARS_GTE = new Source(1, "Xanathar's Guide to Everything", "XGE");
    static final Source SWORD_COAST_AG = new Source(2, "Sword Coast Adv. Guide", "SCAG");
    static final Source LOST_LABORATORY_OK = new Source(3,"Lost Laboratory of Kwalish", "LLK");
    static final Source ACQUISITIONS_INCORPORATED = new Source(4, "Acquisitions Incorporated", "AI");


    // Constructors
    // For built-in values
    private Source(int id, String name, String code, boolean created) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.created = created;
    }

    // To be used for user-created values
    // Automatically increment the value field
    Source(String name, String code, boolean created) { this(0, name, code, created); }

    int getId() { return id; }
    public String getDisplayName() { return name; }
    String getCode() { return code; }

    static Source fromValue(int value) {
        return _map.get(value);
    }
    static Source fromCode(String code) { return _codeMap.get(code); }

    @Keep
    public static Source fromDisplayName(String name) { return _nameMap.get(name); }

}
