package dnd.jon.spellbook;

import androidx.annotation.Keep;

import java.util.HashMap;

public class CastingTime extends Quantity<CastingTime.CastingTimeType, TimeUnit> {

    public enum CastingTimeType implements QuantityType {
        ACTION("action", "1 action"), BONUS_ACTION("bonus action", "1 bonus action"), REACTION("reaction", "1 reaction"), TIME("time", "Other");

        // The parse name is used when parsing from the JSON, after the number is split off
        // No getter since it's not otherwise used
        private final String parseName;
        private final String displayName;
        public String getDisplayName() { return displayName; }

        // Constructor
        CastingTimeType(String parseName, String displayName) {
            this.parseName = parseName;
            this.displayName = displayName;
        }

        // Used for lookup by name
        // Useful when parsing JSON
        private static final HashMap<String, CastingTimeType> _nameMap = new HashMap<>();
        static {
            for (CastingTimeType ctt : CastingTimeType.values()) {
                _nameMap.put(ctt.displayName, ctt);
            }
        }

        // Create the instance from its name
        // Useful for parsing the spell JSON
        @Keep
        public static CastingTimeType fromDisplayName(String name) { return _nameMap.get(name); }

        static private final CastingTimeType[] actionTypes = { ACTION, BONUS_ACTION, REACTION };
        public boolean isSpanningType() { return this == TIME; }
        public CastingTimeType getSpanningType() { return TIME; }

    }

    // How many seconds in a round of combat?
    private static final int SECONDS_PER_ROUND = 6;

    CastingTime(CastingTimeType type, int value, TimeUnit unit, String str) { super(type, value, unit, str); }
    CastingTime() { this(CastingTimeType.ACTION, SECONDS_PER_ROUND, TimeUnit.SECOND, ""); }

    // More descriptive version of baseValue
    int timeInSeconds() { return baseValue(); }

    // Return a string description
    public String string() {
        if (!str.isEmpty()) { return str; }
        if (type == CastingTimeType.TIME) {
            String unitStr = (value == 1) ? unit.singularName() : unit.pluralName();
            return value + " " + unitStr;
        } else {
            String typeStr = " " + type.parseName;
            if (value != 1) {
                typeStr += "s";
            }
            return value + typeStr;
        }
    }

    // Create a range from a string
    static CastingTime fromString(String s) {
        try {
            String[] sSplit = s.split(" ", 2);
            final int value = Integer.parseInt(sSplit[0]);
            final String typeStr = sSplit[1];

            // If the type is one of the action types
            CastingTimeType type = null;
            for (CastingTimeType ct : CastingTimeType.actionTypes) {
                if (typeStr.startsWith(ct.parseName)) {
                    type = ct;
                    break;
                }
            }
            if (type != null) {
                final int inRounds = value * SECONDS_PER_ROUND;
                return new CastingTime(type, inRounds, TimeUnit.SECOND, s);
            }

            // Otherwise, get the time unit
            final TimeUnit unit = TimeUnit.fromString(sSplit[1]);
            return new CastingTime(CastingTimeType.TIME, value, unit, s);

        } catch (Exception e) {
            e.printStackTrace();
            return new CastingTime();
        }

    }


    // Override the default Quantity comparison
    // We  want to compare by time in seconds, and THEN sort by type if necessary
    // The difference between CastingTime and Range, Duration, etc., is that all of the CastingTimeType instances have a real time value (i.e. 6 seconds)
    // Unlike e.g. SPECIAL, UNTIL_DISPELLED in DurationType
    @Override
    public int compareTo(Quantity<CastingTime.CastingTimeType, TimeUnit> other) {
        final int r = baseValue() - other.baseValue();
        if (r != 0) { return r; }
        return type.ordinal() - other.type.ordinal();
    }


}
