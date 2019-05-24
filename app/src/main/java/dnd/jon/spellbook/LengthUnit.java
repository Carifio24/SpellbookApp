package dnd.jon.spellbook;

import java.util.HashMap;
import java.util.Map;

enum LengthUnit implements Unit {
    foot(1), mile(5280);
    private int feet;

    LengthUnit(int feet) {
        this.feet = feet;
    }

    int inFeet() {
        return feet;
    }
    public int value() { return feet; }

    private static final Map<LengthUnit, String[]> names = new HashMap<LengthUnit, String[]>() {{
        put(foot, new String[]{"feet", "foot", "ft", "ft."});
        put(mile, new String[]{"mile", "miles", "mi", "mi."});
    }};

    String pluralName() {
        return this.name() + "s";
    }

    String abbreviation() {
        switch (this) {
            case foot:
                return "ft";
            case mile:
                return "mi";
            default:
                return ""; // Unreachable
        }
    }

    static LengthUnit fromString(String s) throws Exception {
        s = s.toLowerCase();
        for (HashMap.Entry<LengthUnit, String[]> entry : names.entrySet()) {
            for (String t : entry.getValue()) {
                if (s.equals(t)) { return entry.getKey(); }
            }
        }
        throw new Exception("Not a valid unit string");
    }
}
