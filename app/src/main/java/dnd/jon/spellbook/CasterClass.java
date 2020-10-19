package dnd.jon.spellbook;

import android.util.SparseArray;
import androidx.annotation.Keep;

import java.util.Map;
import java.util.HashMap;

public enum CasterClass implements NameDisplayable {
    BARD(0, R.string.bard, "Bard"),
    CLERIC(1, R.string.cleric, "Cleric"),
    DRUID(2, R.string.druid, "Druid"),
    PALADIN(3, R.string.paladin, "Paladin"),
    RANGER(4, R.string.ranger, "Ranger"),
    SORCERER(5, R.string.sorcerer, "Sorcerer"),
    WARLOCK(6, R.string.warlock, "Warlock"),
    WIZARD(7, R.string.wizard, "Wizard");

    final private int value;
    final private int displayNameID;
    final private String internalName;
    int getValue() { return value; }
    public int getDisplayNameID() { return displayNameID; }
    String getInternalName() { return internalName; }

    CasterClass(int value, int displayNameID, String internalName) {
        this.value = value;
        this.displayNameID = displayNameID;
        this.internalName = internalName;
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
            _nameMap.put(cc.internalName, cc);
        }
    }

    static CasterClass fromValue(int value) {
        return _map.get(value);
    }

    @Keep
    public static CasterClass fromInternalName(String name) { return _nameMap.get(name); }

}
