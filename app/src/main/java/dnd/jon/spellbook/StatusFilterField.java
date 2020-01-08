package dnd.jon.spellbook;

import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

enum StatusFilterField {
    ALL(0, "All"), FAVORITES(1, "Favorites"), PREPARED(2, "Prepared"), KNOWN(3, "Known");

    private final int index;
    private final String displayName;
    int getIndex() { return index; }
    String getDisplayName() { return displayName; }

    StatusFilterField(int index, String displayName) {
        this.index = index;
        this.displayName = displayName;
    }

    private static final SparseArray<StatusFilterField> _indexMap = new SparseArray<>();
    static {
        for (StatusFilterField sff : StatusFilterField.values()) {
            _indexMap.put(sff.index, sff);
        }
    }

    private static final HashMap<String,StatusFilterField> _nameMap = new HashMap<>();
    static {
        for (StatusFilterField sff : StatusFilterField.values()) {
            _nameMap.put(sff.displayName, sff);
        }
    }

    static StatusFilterField fromIndex(int index) { return _indexMap.get(index); }
    static StatusFilterField fromDisplayName(String name) { return _nameMap.get(name); }
}
