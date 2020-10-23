package dnd.jon.spellbook;

interface Unit {
    int getSingularNameID();
    int getPluralNameID();
    int getAbbreviationID();
    String getInternalName();
    int value();
}
