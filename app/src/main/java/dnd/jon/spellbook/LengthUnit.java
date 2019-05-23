package dnd.jon.spellbook;

import java.util.HashMap;
import java.util.Map;

enum LengthUnit {
    Foot(1), Mile(5280);
    private int feet;

    LengthUnit(int feet) {
        this.feet = feet;
    }

    int inFeet() {
        return feet;
    }

    private static final Map<LengthUnit, String[]> names = new HashMap<LengthUnit, String[]>() {{
        put(Foot, new String[]{"feet", "foot", "ft", "ft."});
        put(Mile, new String[]{"mile", "miles", "mi", "mi."});
    }};

    String pluralName() {
        switch (this) {
            case Foot:
                return "feet";
            case Mile:
                return "miles";
            default:
                return "";// Unreachable
        }
    }

    String abbreviation() {
        switch (this) {
            case Foot:
                return "ft";
            case Mile:
                return "mi";
            default:
                return ""; // Unreachable
        }
    }

    static LengthUnit fromString(String s) throws Exception {
        for (HashMap.Entry<LengthUnit, String[]> entry : names.entrySet()) {
            for (String t : entry.getValue()) {
                if (s.equals(t)) { return entry.getKey(); }
            }
        }
        throw new Exception("Not a valid unit string");
    }
}
