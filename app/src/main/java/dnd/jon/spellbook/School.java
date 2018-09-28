package dnd.jon.spellbook;

import java.util.Map;
import java.util.HashMap;

enum School {
    ABJURATION(0), CONJURATION(1), DIVINATION(2), ENCHANTMENT(3), EVOCATION(4), ILLUSION(5), NECROMANCY(6), TRANSMUTATION(7);

    int value;
    School(int val) {value = val;}

    private static final Map<Integer,School> _map = new HashMap<Integer,School>();
    static {
        for (School school : School.values()) {
            _map.put(school.value, school);
        }
    }

    static School from(int value) {
        return _map.get(value);
    }
}
