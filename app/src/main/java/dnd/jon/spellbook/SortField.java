package dnd.jon.spellbook;

import android.util.SparseArray;

import java.util.HashMap;

enum SortField implements NameDisplayable {
    NAME(0, R.string.name, "Name"),
    SCHOOL(1, R.string.school,"School"),
    LEVEL(2, R.string.level,"Level"),
    RANGE(3, R.string.range, "Range"),
    DURATION(4, R.string.duration,"Duration"),
    CASTING_TIME(5, R.string.casting_time,"Casting Time");

    private final int index;
    private final int displayNameID;
    private final String internalName;
    int getIndex() { return index; }
    public int getDisplayNameID() { return displayNameID; }
    public String getInternalName() { return internalName; }

    SortField(int index, int displayNameID, String internalName) {
        this.index = index;
        this.displayNameID = displayNameID;
        this.internalName = internalName;
    }

    private static final SparseArray<SortField> _map = new SparseArray<>();
    static {
        for (SortField sf : SortField.values()) {
            _map.put(sf.index, sf);
        }
    }

    private static final HashMap<String, SortField> _nameMap = new HashMap<>();
    static {
        for (SortField sf : SortField.values()) {
            _nameMap.put(sf.internalName, sf);
        }
    }

    static SortField fromIndex(int index) {
        return _map.get(index);
    }
    static SortField fromInternalName(String name) { return _nameMap.get(name); }
}
