package dnd.jon.spellbook;

import java.util.HashMap;
import java.util.Map;

enum StatusFilterField {
    All(0), Favorites(1), Prepared(2), Known(3);

    int index;
    StatusFilterField(int index) { this.index = index; }

    private static final Map<Integer,StatusFilterField> _idxmap = new HashMap<>();
    static {
        for (StatusFilterField sff : StatusFilterField.values()) {
            _idxmap.put(sff.index, sff);
        }
    }

    static StatusFilterField fromIndex(int index) { return _idxmap.get(index); }
}
