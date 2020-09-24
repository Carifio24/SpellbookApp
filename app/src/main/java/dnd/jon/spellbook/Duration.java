package dnd.jon.spellbook;

import androidx.annotation.Keep;

import java.util.HashMap;

public class Duration extends Quantity<Duration.DurationType, TimeUnit>{

    // Type of duration
    // Spanning is the default; just a regular time interval
    public enum DurationType implements QuantityType {
        SPECIAL("Special"), INSTANTANEOUS("Instantaneous"), SPANNING("Finite duration"), UNTIL_DISPELLED("Until dispelled");

        // Only property is the name
        final private String displayName;
        public String getDisplayName() { return displayName; }

        // Constructor
        DurationType(String name) { this.displayName = name; }

        // Used for lookup by name
        // Useful when parsing JSON
        private static final HashMap<String, DurationType> _nameMap = new HashMap<>();
        static {
            for (DurationType durationType : DurationType.values()) {
                _nameMap.put(durationType.displayName, durationType);
            }
        }

        // Create the instance from its name
        // Useful for parsing the spell JSON
        @Keep
        public static DurationType fromDisplayName(String name) { return _nameMap.get(name); }

        private static final DurationType[] nonSpanning = { SPECIAL, INSTANTANEOUS, UNTIL_DISPELLED };
        public boolean isSpanningType() { return this == SPANNING; }
        public DurationType getSpanningType() { return SPANNING; }

    }

    // Convenience constructors
    Duration(DurationType type, int value, TimeUnit unit, String str) { super(type, value, unit, str); }
    Duration() { this(DurationType.INSTANTANEOUS, 0, TimeUnit.SECOND, ""); }

    // A more descriptive version of baseValue
    int timeInSeconds() { return baseValue(); }

    // Return a string description
    public String string() {
        if (!str.isEmpty()) { return str; }
        switch (type) {
            case INSTANTANEOUS:
            case SPECIAL:
            case UNTIL_DISPELLED:
                return type.displayName;
            case SPANNING:
                final String unitStr = (value == 1) ? unit.singularName() : unit.pluralName();
                return value + " " + unitStr;
            default:
                return ""; // Unreachable, the above switch exhausts the enum
        }
    }

    // Create a duration from a string
    static Duration fromString(String s) {
        try {

            // For non-spanning duration types
            for (DurationType durationType : DurationType.nonSpanning) {
                if (s.startsWith(durationType.displayName)) {
                    return new Duration(durationType, 0, TimeUnit.SECOND, s);
                }
            }

            // If we have a real distance
            final String concentrationPrefix = "Up to ";
            String t = s;
            if (s.startsWith(concentrationPrefix)) {
                t = s.substring(concentrationPrefix.length());
            }
            final String[] sSplit = t.split(" ", 2);
            final int value = Integer.parseInt(sSplit[0]);
            final TimeUnit unit = TimeUnit.fromString(sSplit[1]);
            return new Duration(DurationType.SPANNING, value, unit, s);

        } catch (Exception e) {
            // Mostly for testing this out
            // If the user hits a garbled string description in the asset file,
            // not really much that can be done
            e.printStackTrace();
            return new Duration();
        }

    }

}
