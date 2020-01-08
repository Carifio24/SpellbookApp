package dnd.jon.spellbook;

import android.util.SparseArray;

import java.util.Map;
import java.util.HashMap;

enum SubClass {
    LAND(0, "Land"), LORE(1, "Lore"), DRACONIC(2, "Draconic"), HUNTER(3, "Hunter"), LIFE(4, "Life"), DEVOTION(5, "Devotion"), BERSERKER(6, "Berserker"), EVOCATION(7, "Evocation"), FIEND(8, "Fiend"), THIEF(9, "Thief"), OPEN_HAND(10, "Open Hand");

    private final int value;
    private final String displayName;
    int getValue() { return value; }
    String getDisplayName() { return displayName; }

    SubClass(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    private static final SparseArray<SubClass> _map = new SparseArray<>();
    static {
        for (SubClass sc : SubClass.values()) {
            _map.put(sc.value, sc);
        }
    }

    private static final Map<String,SubClass> _nameMap = new HashMap<>();
    static {
        for (SubClass sc : SubClass.values()) {
            _nameMap.put(sc.displayName, sc);
        }
    }

    static SubClass fromValue(int value) {
        return _map.get(value);
    }
    static SubClass fromDisplayName(String name) { return _nameMap.get(name); }
}
