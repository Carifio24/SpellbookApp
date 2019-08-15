package dnd.jon.spellbook;

class SpellOneFieldComparator extends SpellComparator {

    private SortField sf;
    private boolean rv;
    private SortField defaultField;

    SpellOneFieldComparator(SortField sf, boolean rv, SortField defaultField) {
        this.sf = sf;
        this.rv = rv;
        this.defaultField = defaultField;
    }

    SpellOneFieldComparator(SortField sf, boolean rv) { this(sf, rv, SortField.Name); }
    SpellOneFieldComparator(SortField sf) {
        this(sf, false);
    }

    @Override
    public int compare(Spell s1, Spell s2) {
        int r;
        if ((r = oneCompare(s1, s2, sf, rv)) != 0) {
            return r;
        } else {
            return oneCompare(s1, s2, defaultField, false); // If the primary comparator is the same, we sort by name
        }
    }
}
