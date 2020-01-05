package dnd.jon.spellbook;

import android.util.SparseArray;

import java.util.Map;
import java.util.HashMap;

public enum School {
    Abjuration(0, "Abjuration"), Conjuration(1, "Conjuration"), Divination(2, "Divination"), Enchantment(3, "Enchantment"), Evocation(4, "Evocation"), Illusion(5, "Illusion"), Necromancy(6, "Necromancy"), Transmutation(7, "Transmutation");

    final private int value;
    final private String displayName;
    int getValue() { return value; }
    String getDisplayName() { return displayName; }


    School(int value, String name) {
        this.value = value;
        this.displayName = name;
    }

    private static final SparseArray<School> _map = new SparseArray<>();
    static {
        for (School school : School.values()) {
            _map.put(school.value, school);
        }
    }

    private static final HashMap<String,School> _nameMap = new HashMap<>();
    static {
        for (School school : School.values()) {
            _nameMap.put(school.displayName, school);
        }
    }

    static School fromValue(int value) { return _map.get(value); }
    static School fromDisplayName(String name) { return _nameMap.get(name); }
}
