package dnd.jon.spellbook;

import android.util.SparseArray;

import androidx.annotation.Keep;

import java.util.HashMap;

public enum School implements NameDisplayable {
    ABJURATION(0, R.string.abjuration,"Abjuration"),
    CONJURATION(1, R.string.conjuration, "Conjuration"),
    DIVINATION(2, R.string.divination, "Divination"),
    ENCHANTMENT(3, R.string.enchantment, "Enchantment"),
    EVOCATION(4, R.string.evocation, "Evocation"),
    ILLUSION(5, R.string.illusion,"Illusion"),
    NECROMANCY(6, R.string.necromancy, "Necromancy"),
    TRANSMUTATION(7, R.string.transmutation,"Transmutation");

    final private int value;
    final private int displayNameID;
    final private String internalName;
    int getValue() { return value; }
    public int getDisplayNameID() { return displayNameID; }
    String getInternalName() { return internalName; }

    School(int value, int displayNameID, String internalName) {
        this.value = value;
        this.displayNameID = displayNameID;
        this.internalName = internalName;
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
            _nameMap.put(school.internalName, school);
        }
    }

    static School fromValue(int value) { return _map.get(value); }

    @Keep
    public static School fromInternalName(String name) { return _nameMap.get(name); }
}
