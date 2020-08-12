package dnd.jon.spellbook;

import android.util.SparseArray;

import java.util.Map;
import java.util.HashMap;

enum Subclass implements Named {
    LAND(0, "Land"), LORE(1, "Lore"), DRACONIC(2, "Draconic"), HUNTER(3, "Hunter"), LIFE(4, "Life"), DEVOTION(5, "Devotion"), BERSERKER(6, "Berserker"), EVOCATION(7, "Evocation"), FIEND(8, "Fiend"), THIEF(9, "Thief"), OPEN_HAND(10, "Open Hand");

    private final int value;
    private final String displayName;
    int getValue() { return value; }
    public String getDisplayName() { return displayName; }

    Subclass(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
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