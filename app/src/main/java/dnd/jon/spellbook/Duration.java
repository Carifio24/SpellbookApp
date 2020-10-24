package dnd.jon.spellbook;

import androidx.annotation.Keep;

import java.util.HashMap;
import java.util.function.Function;

public class Duration extends Quantity<Duration.DurationType, TimeUnit> {

    // Type of duration
    // Spanning is the default; just a regular time interval
    public enum DurationType implements QuantityType {
        SPECIAL(R.string.special, "Special"),
        INSTANTANEOUS(R.string.instantaneous,"Instantaneous"),
        SPANNING(R.string.spanning,"Finite duration"),
        UNTIL_DISPELLED(R.string.until_dispelled, "Until dispelled");

        // Only property is the name
        final private int displayNameID;
        final private String internalName;
        public int getDisplayNameID() { return displayNameID; }
        public String getInternalName() { return internalName; }

        // Constructor
        DurationType(int displayNameID, String internalName) {
            this.displayNameID = displayNameID;
            this.internalName = internalName;
        }

        // Used for lookup by name
        private static final HashMap<String, DurationType> _nameMap = new HashMap<>();
        static {
            for (DurationType durationType : DurationType.values()) {
                _nameMap.put(durationType.internalName, durationType);
            }
        }

        // Create the instance from its name
        // Useful for parsing the spell JSON
        @Keep
        public static DurationType fromInternalName(String name) { return _nameMap.get(name); }

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
    String makeString(Function<DurationType,String> typeNameGetter, Function<TimeUnit,String> unitSingularNameGetter, Function<TimeUnit,String> unitPluralNameGetter) {
        final String name = typeNameGetter.apply(type);
        if (!str.isEmpty()) { return str; }
        switch (type) {
            case INSTANTANEOUS:
            case SPECIAL:
            case UNTIL_DISPELLED:
                return name;
            case SPANNING:
                final Function<TimeUnit,String> unitNameGetter = (value == 1) ? unitSingularNameGetter : unitPluralNameGetter;
                final String unitStr = unitNameGetter.apply(unit);
                return value + " " + unitStr;
            default:
                return ""; // Unreachable, the above switch exhausts the enum
        }
    }

    String internalString() {
        return makeString(DurationType::getInternalName, TimeUnit::getInternalName, TimeUnit::getInternalName);
    }

    // Create a duration from a string
    static Duration fromString(String s, Function<DurationType,String> typeNameGetter, String concentrationPrefix, Function<String, TimeUnit> timeUnitMaker, boolean useForStr) {
        try {

            // For non-spanning duration types
            for (DurationType durationType : DurationType.nonSpanning) {
                if (s.startsWith(typeNameGetter.apply(durationType))) {
                    return new Duration(durationType, 0, TimeUnit.SECOND, s);
                }
            }

            // If we have a real distance
            String t = s;
            if (s.startsWith(concentrationPrefix)) {
                t = s.substring(concentrationPrefix.length());
            }
            final String[] tSplit = t.split(" ", 2);
            final int value = Integer.parseInt(tSplit[0]);
            System.out.println("tSplit0: " + tSplit[0]);
            System.out.println("tSplit1: " + tSplit[1]);
            final TimeUnit unit = timeUnitMaker.apply(tSplit[1]);
            final String str = useForStr ? s : "";
            return new Duration(DurationType.SPANNING, value, unit, str);

        } catch (Exception e) {
            // Mostly for testing this out
            // If the user hits a garbled string description in the asset file,
            // not really much that can be done
            e.printStackTrace();
            return new Duration();
        }

    }

    // Default is to use the input string as the string representation
    static Duration fromString(String s, Function<DurationType,String> typeNameGetter, String concentrationPrefix, Function<String, TimeUnit> timeUnitMaker) {
        return fromString(s, typeNameGetter, concentrationPrefix, timeUnitMaker, true);
    }

    static Duration fromInternalString(String s) {
        return fromString(s, DurationType::getInternalName, "Up to ", TimeUnit::fromInternalName, false);
    }

}
