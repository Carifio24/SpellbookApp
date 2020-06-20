package dnd.jon.spellbook;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Keep;

enum LengthUnit implements Unit {
    FOOT(1), MILE(5280);
    private final int feet;

    LengthUnit(int feet) {
        this.feet = feet;
    }

    int inFeet() {
        return feet;
    }
    public int value() { return feet; }

    private static final Map<LengthUnit, String[]> names = new HashMap<LengthUnit, String[]>() {{
        put(FOOT, new String[]{"foot", "feet", "ft", "ft."});
        put(MILE, new String[]{"mile", "miles", "mi", "mi."});
    }};

    public String singularName() { return names.get(this)[0]; }
    public String pluralName() { return names.get(this)[1]; }

    public String abbreviation() { return names.get(this)[2]; }

    @Keep
    static LengthUnit fromString(String s) {
        s = s.toLowerCase();
        for (HashMap.Entry<LengthUnit, String[]> entry : names.entrySet()) {
            for (String t : entry.getValue()) {
                if (s.equals(t)) { return entry.getKey(); }
            }
        }
        return null;
    }
}
