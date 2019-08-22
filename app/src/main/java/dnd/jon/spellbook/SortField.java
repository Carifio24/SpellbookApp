package dnd.jon.spellbook;

import java.util.HashMap;
import java.util.Map;

enum SortField {
    Name(0), School(1), Level(2), Range(3), Duration(4);

    int index;
    SortField(int index) { this.index = index; }

    private static final Map<Integer,SortField> _idxmap = new HashMap<>();
    static {
        for (SortField sf : SortField.values()) {
            _idxmap.put(sf.index, sf);
        }
    }

    private static final Map<String,SortField> _namemap = new HashMap<>();
    static {
        for (SortField sf : SortField.values()) {
            _namemap.put(sf.name(), sf);
        }
    }

    static SortField fromIndex(int index) {
        return _idxmap.get(index);
    }
    static SortField fromName(String name) {
        return _namemap.get(name);
    }
}
