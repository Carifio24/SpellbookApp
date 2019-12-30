package dnd.jon.spellbook;

import java.util.HashMap;
import java.util.function.Function;
import java.util.Comparator;

abstract class SpellComparator implements Comparator<Spell> {

    private static int boolSign(boolean b) {
        return b ? -1 : 1;
    }

    private static int compareIntProperty(Spell s1, Spell s2, boolean reverse, Function<Spell,Integer> property) {
        return boolSign(reverse) * ( property.apply(s1) - property.apply(s2) );
    }

    private <T extends Comparable<T>> int compareProperty(Spell s1, Spell s2, boolean reverse, Function<Spell,T> property) {
        return boolSign(reverse) * property.apply(s1).compareTo(property.apply(s2));
    }

    private int compareName2(Spell s1, Spell s2, boolean reverse) {
        return compareProperty(s1, s2, reverse, Spell::getName);
    }

    private int compareName(Spell s1, Spell s2, boolean reverse) { ;
        return boolSign(reverse) * s1.getName().compareTo(s2.getName());
    }

    private int compareSchool(Spell s1, Spell s2, boolean reverse) {
        return boolSign(reverse) * (s1.getSchool().value - s2.getSchool().value);
    }

    private int compareLevel(Spell s1, Spell s2, boolean reverse) {
        return boolSign(reverse) * (s1.getLevel() - s2.getLevel());
    }

    private int compareRange(Spell s1, Spell s2, boolean reverse) {
        return boolSign(reverse) * s1.getRange().compareTo(s2.getRange());
    }

    private int compareDuration(Spell s1, Spell s2, boolean reverse) {
        return boolSign(reverse) * s1.getDuration().compareTo(s2.getDuration());
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
            case Duration:
                return compareDuration(s1, s2, reverse);
            default:
                return 0; // Unreachable
        }
    }

}
