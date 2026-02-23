package dnd.jon.spellbook;

public enum NonCasterClass implements NameDisplayable, PlayableClass {

    FIGHTER(0, 0, "Fighter"),
    MONK(1, 0, "Monk"),
    ROGUE(2, 0, "ROGUE");

    final private int value;
    final private int displayNameID;
    final private String internalName;

    int getValue() { return value; }
    public int getDisplayNameID() { return displayNameID; }
    public String getInternalName() { return internalName; }

    NonCasterClass(int value, int displayNameID, String internalName) {
        this.value = value;
        this.displayNameID = displayNameID;
        this.internalName = internalName;
    }
}
