package dnd.jon.spellbook;

import java.util.Map;
import java.util.HashMap;

enum SubClass {
    LAND(0), LORE(1), DRACONIC(2), HUNTER(3), LIFE(4), DEVOTION(5), BERSERKER(6), EVOCATION(7), FIEND(8), THIEF(9), OPENHAND(10);

    int value;
    SubClass(int val) {value = val;}

    private static final Map<Integer,SubClass> _map = new HashMap<Integer,SubClass>();
    static {
        for (SubClass sc : SubClass.values()) {
            _map.put(sc.value, sc);
        }
    }

    static SubClass from(int value) {
        return _map.get(value);
    }
}
