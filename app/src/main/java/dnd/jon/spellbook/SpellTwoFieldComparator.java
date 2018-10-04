package dnd.jon.spellbook;

class SpellTwoFieldComparator extends SpellComparator {

    private int index1;
    private int index2;

    public SpellTwoFieldComparator(int i1, int i2) {
        index1 = i1;
        index2 = i2;
    }

    @Override
    public int compare(Spell s1, Spell s2) {
        int r;
        if ((r = oneCompare(s1, s2, index1)) != 0) {
            return r;
        } else if ((r = oneCompare(s1, s2, index2)) != 0){
            return r;
        } else {
            return oneCompare(s1, s2, 0); // If the other two are the same (would have to be Level and School), then we compare by name
        }
    }
}
