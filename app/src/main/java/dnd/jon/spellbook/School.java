package dnd.jon.spellbook;

import java.util.Map;
import java.util.HashMap;

public enum School {
    Abjuration(0), Conjuration(1), Divination(2), Enchantment(3), Evocation(4), Illusion(5), Necromancy(6), Transmutation(7);

    final int value;
    School(int val) {value = val;}

    private static final Map<Integer,School> _map = new HashMap<Integer,School>();
    static {
        for (School school : School.values()) {
            _map.put(school.value, school);
        }
    }

    static School fromValue(int value) {
        return _map.get(value);
    }
}
