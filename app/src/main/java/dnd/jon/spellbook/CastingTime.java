package dnd.jon.spellbook;

import androidx.annotation.Keep;

import java.util.HashMap;
import java.util.function.Function;

public class CastingTime extends Quantity<CastingTime.CastingTimeType, TimeUnit> {

    public enum CastingTimeType implements QuantityType {
        ACTION(R.string.one_action, R.string.action, "action"),
        BONUS_ACTION(R.string.one_bonus_action,R.string.bonus_action, "bonus action"),
        REACTION(R.string.one_reaction, R.string.reaction, "reaction"),
        TIME(R.string.other, R.string.other, "time");

        // The parse name is used when parsing from the JSON, after the number is split off
        // No getter since it's not otherwise used
        private final String internalName;
        private final int displayNameID;
        private final int parseNameID;
        public int getDisplayNameID() { return displayNameID; }
        public String getInternalName() { return internalName; }
        int getParseNameID() { return parseNameID; }

        // Constructor
        CastingTimeType(int displayNameID, int parseNameID, String internalName) {
            this.parseNameID = parseNameID;
            this.displayNameID = displayNameID;
            this.internalName = internalName;
        }

        // Used for lookup by name
        // Useful when parsing JSON
        private static final HashMap<String, CastingTimeType> _nameMap = new HashMap<>();
        static {
            for (CastingTimeType ctt : CastingTimeType.values()) {
                _nameMap.put(ctt.internalName, ctt);
            }
        }

        // Create the instance from its name
        // Useful for parsing the spell JSON
        @Keep
        public static CastingTimeType fromInternalName(String name) { return _nameMap.get(name); }

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
    String makeString(boolean useStored, Function<CastingTimeType,String> typeNameGetter, Function<TimeUnit,String> unitSingularNameGetter, Function<TimeUnit,String> unitPluralNameGetter) {
        if (useStored && !str.isEmpty()) { return str; }
        final String name = typeNameGetter.apply(type);
        if (type == CastingTimeType.TIME) {
            final Function<TimeUnit,String> unitNameGetter = (value == 1) ? unitSingularNameGetter : unitPluralNameGetter;
            final String unitStr = unitNameGetter.apply(unit);
            return value + " " + unitStr;
        } else {
            String typeStr = " " + name;
            if (value != 1) {
                typeStr += "s";
            }
            return value + typeStr;
        }
    }

    String makeString(Function<CastingTimeType,String> typeNameGetter, Function<TimeUnit,String> unitSingularNameGetter, Function<TimeUnit,String> unitPluralNameGetter) {
        return makeString(true, typeNameGetter, unitSingularNameGetter, unitPluralNameGetter);
    }

    String internalString() {
        return makeString(false, CastingTimeType::getInternalName, TimeUnit::getInternalName, TimeUnit::getInternalName);
    }

    // Create a range from a string
    static CastingTime fromString(String s, Function<CastingTimeType,String> typeNameGetter, Function<String, TimeUnit> timeUnitMaker, boolean useForStr) {
        try {
            String[] sSplit = s.split(" ", 2);
            final int value = Integer.parseInt(sSplit[0]);
            final String typeStr = sSplit[1];

            // If the type is one of the action types
            CastingTimeType type = null;
            for (CastingTimeType ct : CastingTimeType.actionTypes) {
                if (typeStr.startsWith(typeNameGetter.apply(ct))) {
                    type = ct;
                    break;
                }
            }
            if (type != null) {
                final int inRounds = value * SECONDS_PER_ROUND;
                return new CastingTime(type, inRounds, TimeUnit.SECOND, s);
            }

            // Otherwise, get the time unit
            final TimeUnit unit = timeUnitMaker.apply(typeStr);
            final String str = useForStr ? s : "";
            return new CastingTime(CastingTimeType.TIME, value, unit, str);

        } catch (Exception e) {
            e.printStackTrace();
            return new CastingTime();
        }

    }

    // Default is to use the input string as the string representation
    static CastingTime fromInternalString(String s) {
        return fromString(s, CastingTimeType::getInternalName, TimeUnit::fromInternalName, false);
    }

    static CastingTime fromString(String s, Function<CastingTimeType,String> typeNameGetter, Function<String, TimeUnit> timeUnitMaker) {
        return fromString(s, typeNameGetter, timeUnitMaker, true);
    }


    // Override the default Quantity comparison
    // We  want to compare by time in seconds, and THEN sort by type if necessary
    // The difference between CastingTime and Range, Duration, etc., is that all of the CastingTimeType instances have a real time value (i.e. 6 seconds)
    // Unlike e.g. SPECIAL, UNTIL_DISPELLED in DurationType
    @Override
    public int compareTo(Quantity<CastingTime.CastingTimeType, TimeUnit> other) {
        System.out.println(internalString());
        System.out.println(other.internalString());
        System.out.println(unit);
        System.out.println(other.unit);
        final int r = baseValue() - other.baseValue();
        if (r != 0) { return r; }
        return type.ordinal() - other.type.ordinal();
    }


}
