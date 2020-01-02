package dnd.jon.spellbook;

import java.util.Map;
import java.util.HashMap;

enum SubClass {
    Land(0), Lore(1), Draconic(2), Hunter(3), Life(4), Devotion(5), Berserker(6), Evocation(7), Fiend(8), Thief(9), OpenHand(10);

    int value;
    SubClass(int val) {value = val;}

    private static final Map<Integer,SubClass> _map = new HashMap<Integer,SubClass>();
    static {
        for (SubClass sc : SubClass.values()) {
            _map.put(sc.value, sc);
        }
    }

    static SubClass fromValue(int value) {
        return _map.get(value);
    }
}
