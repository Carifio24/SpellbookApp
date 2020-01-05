package dnd.jon.spellbook;

import android.util.SparseArray;

import java.util.Map;
import java.util.HashMap;

enum SubClass {
    Land(0, "Land"), Lore(1, "Lore"), Draconic(2, "Draconic"), Hunter(3, "Hunter"), Life(4, "Life"), Devotion(5, "Devotion"), Berserker(6, "Berserker"), Evocation(7, "Evocation"), Fiend(8, "Fiend"), Thief(9, "Thief"), OpenHand(10, "Open Hand");

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
