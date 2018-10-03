package dnd.jon.spellbook;

import java.util.Comparator;

class SpellTwoFieldComparator implements Comparator<Spell> {

    private int index1;
    private int index2;

    public SpellTwoFieldComparator(int i1, int i2) {
        index1 = i1;
        index2 = i2;
    }

    private int compareName(Spell s1, Spell s2) {
        return s1.getName().compareTo(s2.getName());
    }

    private int compareSchool(Spell s1, Spell s2) {
        return s1.getSchool().value - s2.getSchool().value;
    }

    private int compareLevel(Spell s1, Spell s2) {
        return s1.getLevel() - s2.getLevel();
    }

    private int oneCompare(Spell s1, Spell s2, int ind) {
        if (ind == 0) {
            return compareName(s1, s2);
        } else if (ind == 1) {
            return compareSchool(s1, s2);
        } else {
            return compareLevel(s1, s2);
        }
    }

    @Override
    public int compare(Spell s1, Spell s2) {
        int r;
        if ((r = oneCompare(s1, s2, index1)) != 0) {
            return r;
        } else {
            return oneCompare(s1, s2, index2);
        }
    }
}
