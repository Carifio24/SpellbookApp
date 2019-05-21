package dnd.jon.spellbook;

class SpellOneFieldComparator extends SpellComparator {

    private SortField sf;

    public SpellOneFieldComparator(SortField sf) {
        this.sf = sf;
    }

    @Override
    public int compare(Spell s1, Spell s2) {
        int r;
        if ((r = oneCompare(s1, s2, sf)) != 0) {
            return r;
        } else {
            return oneCompare(s1, s2, SortField.Name); // If the primary comparator is the same, we sort by name
        }
    }
}
