package dnd.jon.spellbook;

class SpellStatus {

    boolean favorite;
    boolean prepared;
    boolean known;

    SpellStatus(boolean fav, boolean prep, boolean know) {
        favorite = fav;
        prepared = prep;
        known = know;
    }

    SpellStatus() {
        this(false, false, false);
    }

}
