package dnd.jon.spellbook;

import java.util.Comparator;

abstract class SpellComparator implements Comparator<Spell> {

    private int boolSgn(boolean b) {
        return b ? -1 : 1;
    }

    private int compareName(Spell s1, Spell s2, boolean reverse) { ;
        return boolSgn(reverse) * s1.getName().compareTo(s2.getName());
    }

    private int compareSchool(Spell s1, Spell s2, boolean reverse) {
        return boolSgn(reverse) * (s1.getSchool().value - s2.getSchool().value);
    }

    private int compareLevel(Spell s1, Spell s2, boolean reverse) {
        return boolSgn(reverse) * (s1.getLevel() - s2.getLevel());
    }

    private int compareRange(Spell s1, Spell s2, boolean reverse) {
        return boolSgn(reverse) * s1.getRange().compareTo(s2.getRange());
    }

    int oneCompare(Spell s1, Spell s2, SortField sf, boolean reverse) {
        switch (sf) {
            case Name:
                return compareName(s1, s2, reverse);
            case School:
                return compareSchool(s1, s2, reverse);
            case Level:
                return compareLevel(s1, s2, reverse);
            case Range:
                return compareRange(s1, s2, reverse);
            default:
                return 0; // Unreachable
        }
    }

}
