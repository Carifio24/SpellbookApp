package dnd.jon.spellbook;

import android.util.SparseArray;

import java.util.HashMap;

enum StatusFilterField implements NameDisplayable {
    ALL(0,  R.string.all, "All"),
    FAVORITES(1, R.string.favorites, "Favorites"),
    PREPARED(2, R.string.prepared, "Prepared"),
    KNOWN(3, R.string.known,"Known");

    private final int index;
    private final int displayNameID;
    private final String internalName;
    int getIndex() { return index; }
    public int getDisplayNameID() { return displayNameID; }
    public String getInternalName() { return internalName; }

    StatusFilterField(int index, int displayNameID, String internalName) {
        this.index = index;
        this.displayNameID = displayNameID;
        this.internalName = internalName;
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
            _nameMap.put(sff.internalName, sff);
        }
    }

    static StatusFilterField fromIndex(int index) { return _indexMap.get(index); }
    static StatusFilterField fromDisplayName(String name) { return _nameMap.get(name); }
}
