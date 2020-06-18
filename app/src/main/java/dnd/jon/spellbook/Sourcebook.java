package dnd.jon.spellbook;

import android.util.SparseArray;

import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sourcebook implements Named {

    // Static values
    private static int nValues = 5;
    private static List<Sourcebook> _values = new ArrayList<>();
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
        _map.put(sourcebook.value, sourcebook);
        _nameMap.put(sourcebook.name, sourcebook);
        _codeMap.put(sourcebook.code, sourcebook);
        System.out.println("Added sourcebook: " + sourcebook.name);
    }

    // Constructors
    // For built-in values
    private Sourcebook(int value, String name, String code) {
        this.value = value;
        this.name = name;
        this.code = code;
        updateInternals(this);
    }

    // To be used for user-created values
    // Automatically increment the value field
    Sourcebook(String name, String code) { this(nValues++, name, code); }

    final private int value;
    final private String name;
    final private String code;

    int getValue() { return value; }
    public String getDisplayName() { return name; }
    String getCode() { return code; }

    static Sourcebook fromValue(int value) {
        return _map.get(value);
    }
    static Sourcebook fromCode(String code) { return _codeMap.get(code); }

    @Keep
    public static Sourcebook fromDisplayName(String name) { return _nameMap.get(name); }

}
