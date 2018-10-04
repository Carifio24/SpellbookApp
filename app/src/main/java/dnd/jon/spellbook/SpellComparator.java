package dnd.jon.spellbook;

import java.util.Comparator;

abstract class SpellComparator implements Comparator<Spell> {

    protected int compareName(Spell s1, Spell s2) {
        return s1.getName().compareTo(s2.getName());
    }

    protected int compareSchool(Spell s1, Spell s2) {
        return s1.getSchool().value - s2.getSchool().value;
    }

    protected int compareLevel(Spell s1, Spell s2) {
        return s1.getLevel() - s2.getLevel();
    }

    protected int oneCompare(Spell s1, Spell s2, int ind) {
        if (ind == 0) {
            return compareName(s1, s2);
        } else if (ind == 1) {
            return compareSchool(s1, s2);
        } else {
            return compareLevel(s1, s2);
        }
    }

}
