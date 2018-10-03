package dnd.jon.spellbook;

import java.util.Comparator;

class SpellOneFieldComparator implements Comparator<Spell> {

    private int index;

    public SpellOneFieldComparator(int i) {
        index = i;
    }

    @Override
    public int compare(Spell s1, Spell s2) {
        if (index == 0) {
            return s1.getName().compareTo(s2.getName());
        } else if (index == 1) {
            return s1.getSchool().value - s2.getSchool().value;
        } else {
            return s1.getLevel() - s2.getLevel();
        }
    }
}
