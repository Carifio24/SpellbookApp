package dnd.jon.spellbook;

import android.util.SparseArray;

import java.util.Map;
import java.util.HashMap;

enum CasterClass {
    Bard(0, "Bard"), Cleric(1, "Cleric"), Druid(2, "Druid"), Paladin(3, "Paladin"), Ranger(4, "Ranger"), Sorcerer(5, "Sorcerer"), Warlock(6, "Warlock"), Wizard(7, "Wizard");

    final private int value;
    final private String displayName;
    int getValue() { return value; }
    String getDisplayName() { return displayName; }

    CasterClass(int value, String name) {
        this.value = value;
        this.displayName = name;
    }

    private static final SparseArray<CasterClass> _map = new SparseArray<>();
    static {
        for (CasterClass cc : CasterClass.values()) {
            _map.put(cc.value, cc);
        }
    }

    private static final Map<String,CasterClass> _nameMap = new HashMap<>();
    static {
        for (CasterClass cc : CasterClass.values()) {
            _nameMap.put(cc.displayName, cc);
        }
    }

    static CasterClass fromValue(int value) {
        return _map.get(value);
    }
    static CasterClass fromDisplayName(String name) { return _nameMap.get(name); }

}
