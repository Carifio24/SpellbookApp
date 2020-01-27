package dnd.jon.spellbook;

import android.util.SparseArray;

import java.util.HashMap;

public enum School implements NameDisplayable {
    ABJURATION(0, "Abjuration"), CONJURATION(1, "Conjuration"), DIVINATION(2, "Divination"), ENCHANTMENT(3, "Enchantment"), EVOCATION(4, "Evocation"), ILLUSION(5, "Illusion"), NECROMANCY(6, "Necromancy"), TRANSMUTATION(7, "Transmutation");

    final private int value;
    final private String displayName;
    int getValue() { return value; }
    public String getDisplayName() { return displayName; }


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
