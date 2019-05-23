package dnd.jon.spellbook;

class SpellOneFieldComparator extends SpellComparator {

    private SortField sf;
    private boolean rv;

    SpellOneFieldComparator(SortField sf, boolean rv) {
        this.sf = sf;
        this.rv = rv;
    }

    SpellOneFieldComparator(SortField sf) {
        this(sf, false);
    }

    @Override
    public int compare(Spell s1, Spell s2) {
        int r;
        if ((r = oneCompare(s1, s2, sf, rv)) != 0) {
            return r;
        } else {
            return oneCompare(s1, s2, SortField.Name, false); // If the primary comparator is the same, we sort by name
        }
    }
}
