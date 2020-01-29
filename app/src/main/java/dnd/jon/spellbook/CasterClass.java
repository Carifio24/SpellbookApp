package dnd.jon.spellbook;

import android.util.SparseArray;

import java.util.Map;
import java.util.HashMap;

public enum CasterClass implements NameDisplayable {
    BARD(0, "Bard"), CLERIC(1, "Cleric"), DRUID(2, "Druid"), PALADIN(3, "Paladin"), RANGER(4, "Ranger"), SORCERER(5, "Sorcerer"), WARLOCK(6, "Warlock"), WIZARD(7, "Wizard");

    final private int value;
    final private String displayName;
    int getValue() { return value; }
    public String getDisplayName() { return displayName; }

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
    public static CasterClass fromDisplayName(String name) { return _nameMap.get(name); }

}
