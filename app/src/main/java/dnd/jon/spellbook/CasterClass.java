package dnd.jon.spellbook;

import java.util.Map;
import java.util.HashMap;

enum CasterClass {
    Bard(0), Cleric(1), Druid(2), Paladin(3), Ranger(4), Sorcerer(5), Warlock(6), Wizard(7);

    int value;
    CasterClass(int val) {value = val;}

    private static final Map<Integer,CasterClass> _map = new HashMap<Integer,CasterClass>();
    static {
        for (CasterClass cc : CasterClass.values()) {
            _map.put(cc.value, cc);
        }
    }

    private static final Map<String,CasterClass> _namemap = new HashMap<>();
    static {
        for (CasterClass cc : CasterClass.values()) {
            _namemap.put(cc.name(), cc);
        }
    }

    static CasterClass from(int value) {
        return _map.get(value);
    }

    static CasterClass fromName(String name) { return _namemap.get(name); }

}
