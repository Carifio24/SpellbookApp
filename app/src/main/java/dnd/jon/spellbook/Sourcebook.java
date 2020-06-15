package dnd.jon.spellbook;

import android.util.SparseArray;

import androidx.annotation.Keep;

import java.util.HashMap;
import java.util.Map;

public enum Sourcebook implements Named {
    PLAYERS_HANDBOOK(0, "Player's Handbook", "PHB"), XANATHARS_GTE(1, "Xanathar's Guide to Everything", "XGE"), SWORD_COAST_AG(2, "Sword Coast Adv. Guide", "SCAG"), LOST_LABORATORY_OK(3,"Lost Laboratory of Kwalish", "LLK");

    // Constructor
    Sourcebook(int value, String name, String code) {
        this.value = value;
        this.name = name;
        this.code = code;
    }

    final private int value;
    final private String name;
    final private String code;

    int getValue() { return value; }
    public String getDisplayName() { return name; }
    String getCode() { return code; }

    private static final SparseArray<Sourcebook> _map = new SparseArray<>();
    static {
        for (Sourcebook s : Sourcebook.values()) {
            _map.put(s.value, s);
        }
    }

    private static final Map<String,Sourcebook> _nameMap = new HashMap<>();
    static {
        for (Sourcebook s : Sourcebook.values()) {
            _nameMap.put(s.name, s);
        }
    }

    private static final Map<String,Sourcebook> _codeMap = new HashMap<>();
    static {
        for (Sourcebook s : Sourcebook.values()) {
            _codeMap.put(s.code, s);
        }
    }

    static Sourcebook fromValue(int value) {
        return _map.get(value);
    }
    static Sourcebook fromCode(String code) { return _codeMap.get(code); }

    @Keep
    public static Sourcebook fromDisplayName(String name) { return _nameMap.get(name); }

}
