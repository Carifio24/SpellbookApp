package dnd.jon.spellbook;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Keep;

enum LengthUnit implements Unit {
    FOOT(1, R.string.foot, R.string.feet, R.string.ft, "foot"),
    METER(3, R.string.meter, R.string.meters, R.string.m, "meter"), // These values are close enough, since we'll never be mixing unit systems
    KILOMETER(3000, R.string.kilometer, R.string.kilometers, R.string.km, "kilometer"),
    MILE(5280, R.string.mile, R.string.miles, R.string.mi, "mile");

    private final int feet;
    private final int singularNameID;
    private final int pluralNameID;
    private final int abbreviationID;
    private final String internalName;

    LengthUnit(int feet, int singularNameID, int pluralNameID, int abbreviationID, String internalName) {
        this.feet = feet;
        this.singularNameID = singularNameID;
        this.pluralNameID = pluralNameID;
        this.abbreviationID = abbreviationID;
        this.internalName = internalName;
    }

    int inFeet() { return feet; }
    public int value() { return feet; }

    public int getSingularNameID() { return singularNameID; }
    public int getPluralNameID() { return pluralNameID; }
    public int getAbbreviationID() { return abbreviationID; }
    public String getInternalName() { return internalName; }

    // Used for lookup by name
    private static final HashMap<String, LengthUnit> _nameMap = new HashMap<>();
    static {
        for (LengthUnit lengthUnit : LengthUnit.values()) {
            _nameMap.put(lengthUnit.internalName, lengthUnit);
        }
    }

    @Keep
    public static LengthUnit fromInternalName(String name) {
        System.out.println("Name is " + name); return _nameMap.get(name); }

}
