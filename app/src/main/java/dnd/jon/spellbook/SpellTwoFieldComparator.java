package dnd.jon.spellbook;

class SpellTwoFieldComparator extends SpellComparator {

    private SortField sf1;
    private SortField sf2;

    SpellTwoFieldComparator(SortField sf1, SortField sf2) {
        this.sf1 = sf1;
        this.sf2 = sf2;
    }

    @Override
    public int compare(Spell s1, Spell s2) {
        int r;
        if ((r = oneCompare(s1, s2, sf1)) != 0) {
            return r;
        } else if ((r = oneCompare(s1, s2, sf2)) != 0){
            return r;
        } else {
            return oneCompare(s1, s2, SortField.Name); // If the other two are the same (would have to be Level and School), then we compare by name
        }
    }
}
