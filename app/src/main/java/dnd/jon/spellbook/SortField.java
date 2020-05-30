package dnd.jon.spellbook;

import android.util.SparseArray;

import java.util.HashMap;

enum SortField implements Named, CaseIterable {
    NAME(0, "Name"), SCHOOL(1, "School"), LEVEL(2, "Level"), RANGE(3, "Range"), DURATION(4, "Duration"), CASTING_TIME(5, "Casting Time");

    private final int index;
    private final String name;
    int getIndex() { return index; }
    public String getDisplayName() { return name; }

    SortField(int index, String name) {
        this.index = index;
        this.name = name;
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
            _nameMap.put(sf.name, sf);
        }
    }

    static SortField fromIndex(int index) {
        return _map.get(index);
    }
    static SortField fromDisplayName(String name) { return _nameMap.get(name); }
}
