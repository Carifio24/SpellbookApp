package dnd.jon.spellbook;

import dnd.jon.spellbook.Spell;

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

    SpellStatus(Spell s) {
        favorite = s.isFavorite();
        prepared = s.isPrepared();
        known = s.isKnown();
    }

}
