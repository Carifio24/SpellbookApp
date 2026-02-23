package dnd.jon.spellbook;

import android.util.SparseArray;

import java.util.Map;
import java.util.HashMap;

enum Subclass {
    private final int value;
    private final PlayableClass parentClass;
    private final int displayNameID;
    private final String internalName;
    int getValue() { return value; }

    Subclass(int value, int displayNameID,  String internalName, PlayableClass parentClass) {
        this.value = value;
        this.displayNameID = displayNameID;
        this.internalName = internalName;
        this.parentClass = parentClass;
    }

    private static final SparseArray<Subclass> _map = new SparseArray<>();
    static {
        for (Subclass sc : Subclass.values()) {
            _map.put(sc.value, sc);
        }
    }

    private static final Map<String, Subclass> _nameMap = new HashMap<>();
    static {
        for (Subclass sc : Subclass.values()) {
            _nameMap.put(sc.displayName, sc);
        }
    }

    static Subclass fromValue(int value) {
        return _map.get(value);
    }
    static Subclass fromDisplayName(String name) { return _nameMap.get(name); }
}
