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

    protected int compareRange(Spell s1, Spell s2) { return s1.getRange().compareTo(s2.getRange()); }

    protected int oneCompare(Spell s1, Spell s2, SortField sf) {
        switch (sf) {
            case Name:
                return compareName(s1, s2);
            case School:
                return compareSchool(s1, s2);
            case Level:
                return compareLevel(s1, s2);
            case Range:
                return compareRange(s1, s2);
        }
        return 0;
    }
}
