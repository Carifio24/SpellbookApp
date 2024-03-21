package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CastingTime extends Quantity<CastingTime.CastingTimeType, TimeUnit> {

    public enum CastingTimeType implements QuantityType {
        ACTION(R.string.one_action, R.string.action, "action", "1 action"),
        BONUS_ACTION(R.string.one_bonus_action,R.string.bonus_action, "bonus action", "1 bonus action"),
        REACTION(R.string.one_reaction, R.string.reaction, "reaction", "1 reaction"),
        TIME(R.string.other, R.string.other, "time", "Other");

        // The parse name is used when parsing from the JSON, after the number is split off
        // No getter since it's not otherwise used
        private final String internalName;
        private final String internalDisplayName;
        private final int displayNameID;
        private final int parseNameID;
        public int getDisplayNameID() { return displayNameID; }
        public String getInternalName() { return internalName; }
        int getParseNameID() { return parseNameID; }

        // Constructor
        CastingTimeType(int displayNameID, int parseNameID, String internalName, String internalDisplayName) {
            this.parseNameID = parseNameID;
            this.displayNameID = displayNameID;
            this.internalName = internalName;
            this.internalDisplayName = internalDisplayName;
        }

        // Used for lookup by name
        // Useful when parsing JSON
        private static final Map<String, CastingTimeType> _nameMap = new HashMap<>();
        private static final Map<String, CastingTimeType> _displayNameMap = new HashMap<>();
        static {
            for (CastingTimeType ctt : CastingTimeType.values()) {
                _nameMap.put(ctt.internalName, ctt);
                _displayNameMap.put(ctt.internalDisplayName, ctt);
            }
        }

        // Create the instance from its name
        // Useful for parsing the spell JSON
        @Keep
        public static CastingTimeType fromInternalName(String name) {
            final CastingTimeType ctt = _nameMap.get(name);
            if (ctt != null) { return ctt; }
            return _displayNameMap.get(name);
        }

        static private final CastingTimeType[] actionTypes = { ACTION, BONUS_ACTION, REACTION };
        public boolean isSpanningType() { return this == TIME; }
        public CastingTimeType getSpanningType() { return TIME; }

    }

    // How many seconds in a round of combat?
    private static final int SECONDS_PER_ROUND = 6;

    // Convenience constructors
    CastingTime(CastingTimeType type, float value, TimeUnit unit, String str) { super(type, value, unit, str); }
    CastingTime(CastingTimeType type, float value) { super(type, value, TimeUnit.SECOND); }
    CastingTime(CastingTimeType type) { this(type, 1); }
    CastingTime() { this(CastingTimeType.ACTION, 1, TimeUnit.SECOND, ""); }

    // For Parcelable
    private CastingTime(Parcel in) {
        super(CastingTimeType.fromInternalName(in.readString()),
                in.readFloat(),
                TimeUnit.fromInternalName(in.readString()),
                in.readString());
    }

    // More descriptive version of baseValue
    float timeInSeconds() { return baseValue(); }

    // Return a string description
    String makeString(boolean useStored, Function<CastingTimeType,String> typeNameGetter, Function<TimeUnit,String> unitSingularNameGetter, Function<TimeUnit,String> unitPluralNameGetter) {
        if (useStored && !str.isEmpty()) { return str; }
        final String name = typeNameGetter.apply(type);
        if (type == CastingTimeType.TIME) {
            final String valueString = DisplayUtils.DECIMAL_FORMAT.format(value);
            final Function<TimeUnit,String> unitNameGetter = (value == 1) ? unitSingularNameGetter : unitPluralNameGetter;
            final String unitStr = unitNameGetter.apply(unit);
            return valueString + " " + unitStr;
        } else {
            return name;
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
            float value = 1;
            String typeStr = "";
            try {
                value = Float.parseFloat(sSplit[0]);
                typeStr = sSplit[1];
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            //System.out.println("sSplit0: " + sSplit[0]);
            //System.out.println("sSplit1: " + sSplit[1]);

            // If the type is one of the action types
            CastingTimeType type = null;
            for (CastingTimeType ct : CastingTimeType.actionTypes) {
                final String typeName = typeNameGetter.apply(ct);
                if (s.startsWith(typeName) || typeStr.startsWith(typeName)) {
                    type = ct;
                    break;
                }
            }
            if (type != null) {
                //final int inRounds = value * SECONDS_PER_ROUND;
                final String str = useForStr ? s : "";
                return new CastingTime(type, 1, TimeUnit.SECOND, str);
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

    @Override
    float baseValue() {
        if (type == CastingTimeType.TIME) {
            return super.baseValue();
        } else {
            return value * SECONDS_PER_ROUND;
        }
    }

    // Override the default Quantity comparison
    // We  want to compare by time in seconds, and THEN sort by type if necessary
    // The difference between CastingTime and Range, Duration, etc., is that all of the CastingTimeType instances have a real time value (i.e. 6 seconds)
    // Unlike e.g. SPECIAL, UNTIL_DISPELLED in DurationType
    @Override
    public int compareTo(Quantity<CastingTime.CastingTimeType, TimeUnit> other) {
        final float r = baseValue() - other.baseValue();
        if (r != 0) {
            return (int) Math.signum(r);
        }
        return type.ordinal() - other.type.ordinal();
    }

    public static final Parcelable.Creator<CastingTime> CREATOR = new Parcelable.Creator<CastingTime>() {
        public CastingTime createFromParcel(Parcel in) { return new CastingTime(in); }
        public CastingTime[] newArray(int size) { return new CastingTime[size]; }
    };

}
