package dnd.jon.spellbook;

import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

enum SortField {
    Name(0, "Name"), School(1, "School"), Level(2, "Level"), Range(3, "Range"), Duration(4, "Duration");

    private final int index;
    private final String displayName;
    int getIndex() { return index; }
    String getDisplayName() { return displayName; }

    SortField(int index, String name) {
        this.index = index;
        this.displayName = name;
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
            _nameMap.put(sf.displayName, sf);
        }
    }

    static SortField fromIndex(int index) {
        return _map.get(index);
    }
    static SortField fromDisplayName(String name) { return _nameMap.get(name); }
}
