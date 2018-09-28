package dnd.jon.spellbook;

import java.util.Map;
import java.util.HashMap;

enum CasterClass {
    BARD(0), CLERIC(1), DRUID(2), PALADIN(3), RANGER(4), SORCERER(5), WARLOCK(6), WIZARD(7), ARCANETRICKSTER(8), ELDRITCHKNIGHT(9);

    int value;
    CasterClass(int val) {value = val;}

    private static final Map<Integer,CasterClass> _map = new HashMap<Integer,CasterClass>();
    static {
        for (CasterClass cc : CasterClass.values()) {
            _map.put(cc.value, cc);
        }
    }

    static CasterClass from(int value) {
        return _map.get(value);
    }
}
