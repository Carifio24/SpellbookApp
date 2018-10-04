package dnd.jon.spellbook;

class SpellOneFieldComparator extends SpellComparator {

    private int index;

    public SpellOneFieldComparator(int i) {
        index = i;
    }

    @Override
    public int compare(Spell s1, Spell s2) {
        int r;
        if ((r = oneCompare(s1, s2, index)) != 0) {
            return r;
        } else {
            return oneCompare(s1, s2, 0); // If the primary comparator is the same, we sort by name
        }
    }
}
