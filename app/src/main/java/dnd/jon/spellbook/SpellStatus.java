package dnd.jon.spellbook;

class SpellStatus {

    private boolean favorite;
    private boolean prepared;
    private boolean known;

    SpellStatus(boolean favorite, boolean prepared, boolean known) {
        this.favorite = favorite;
        this.prepared = prepared;
        this.known = known;
    }

    boolean isFavorite() { return favorite; }
    boolean isPrepared() { return prepared; }
    boolean isKnown() { return known; }

    void setFavorite(boolean favorite) { this.favorite = favorite; }
    void setKnown(boolean known) { this.known = known; }
    void setPrepared(boolean prepared){ this.prepared = prepared; }

    SpellStatus() {
        this(false, false, false);
    }

    boolean noneTrue() {
        return !(favorite || prepared || known);
    }

}
