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

@Entity(tableName = "sourcebook", indices = {@Index(name = "index_sourcebooks_id", value = {"id"}, unique = true)})
public class Sourcebook implements Named {

    // Member values
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") final private int id;

    @NonNull @ColumnInfo(name = "name") final private String name;
    @NonNull @ColumnInfo(name = "code") final private String code;

    // Static values
    private static int nValues = 0;
    private static final List<Sourcebook> _values = new ArrayList<>();
    private static final SparseArray<Sourcebook> _map = new SparseArray<>();
    private static final Map<String,Sourcebook> _nameMap = new HashMap<>();
    private static final Map<String,Sourcebook> _codeMap = new HashMap<>();

    // Access the current set of values
    public static List<Sourcebook> values() { return _values; }

    static final Sourcebook PLAYERS_HANDBOOK =  new Sourcebook(0, "Player's Handbook", "PHB");
    static final Sourcebook XANATHARS_GTE = new Sourcebook(1, "Xanathar's Guide to Everything", "XGE");
    static final Sourcebook SWORD_COAST_AG = new Sourcebook(2, "Sword Coast Adv. Guide", "SCAG");
    static final Sourcebook LOST_LABORATORY_OK = new Sourcebook(3,"Lost Laboratory of Kwalish", "LLK");
    static final Sourcebook ACQUISITIONS_INCORPORATED = new Sourcebook(4, "Acquisitions Incorporated", "AI");



    private static void updateInternals(Sourcebook sourcebook) {
        _values.add(sourcebook);
        _map.put(sourcebook.id, sourcebook);
        _nameMap.put(sourcebook.name, sourcebook);
        _codeMap.put(sourcebook.code, sourcebook);
        System.out.println("Added sourcebook: " + sourcebook.name);
        System.out.println("Values length is now: " + _values.size());
    }

    // Constructors
    // For built-in values
    private Sourcebook(int id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
        nValues++;
        updateInternals(this);
    }

    // To be used for user-created values
    // Automatically increment the value field
    Sourcebook(String name, String code) { this(nValues++, name, code); }

    int getId() { return id; }
    public String getDisplayName() { return name; }
    String getCode() { return code; }

    static Sourcebook fromValue(int value) {
        return _map.get(value);
    }
    static Sourcebook fromCode(String code) { return _codeMap.get(code); }

    @Keep
    public static Sourcebook fromDisplayName(String name) { return _nameMap.get(name); }

}
