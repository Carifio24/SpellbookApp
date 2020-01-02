package dnd.jon.spellbook;

class SpellStatus {

    boolean favorite;
    boolean prepared;
    boolean known;

    SpellStatus(boolean favorite, boolean prepared, boolean known) {
        this.favorite = favorite;
        this.prepared = prepared;
        this.known = known;
    }

    SpellStatus() {
        this(false, false, false);
    }

    boolean noneTrue() {
        return !(favorite || prepared || known);
    }

}
