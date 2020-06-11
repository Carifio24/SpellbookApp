package dnd.jon.spellbook;

import androidx.annotation.Keep;
import androidx.room.Ignore;

import java.util.HashMap;
import java.util.Map;

public class CastingTime extends Quantity<CastingTime.CastingTimeType, TimeUnit> {

    public enum CastingTimeType implements QuantityType {
        ACTION("action", "1 action",0), BONUS_ACTION("bonus action", "1 bonus action", 1), REACTION("reaction", "1 reaction", 2), TIME("time", "Other", 3);

        private final String parseName;
        private final String displayName;
        private final int index;
        public String getDisplayName() { return displayName; }
        public String getParseName() { return parseName; }
        int getIndex() { return index; }

        CastingTimeType(String parseName, String displayName, int index) {
            this.parseName = parseName;
            this.displayName = displayName;
            this.index = index;
        }

        private static final Map<String, CastingTimeType> _displayNameMap = new HashMap<>();
        private static final Map<String, CastingTimeType> _parseNameMap = new HashMap<>();
        static {
            for (CastingTimeType ctt : CastingTimeType.values()) {
                _displayNameMap.put(ctt.displayName, ctt);
                _parseNameMap.put(ctt.parseName, ctt);
            }
        }

        @Keep public static CastingTimeType fromDisplayName(String name) { return _displayNameMap.get(name); }
        public static CastingTimeType fromParseName(String name) { return _parseNameMap.get(name); }

        static private final CastingTimeType[] actionTypes = { ACTION, BONUS_ACTION, REACTION };

        public boolean isSpanningType() { return this == TIME; }
        public CastingTimeType getSpanningType() { return TIME; }
        public static CastingTimeType spanningType() { return TIME; }

    }

    private static final int SECONDS_PER_ROUND = 6;

    CastingTime(CastingTimeType type, int value, TimeUnit unit, String str) { super(type, value, unit, str); }

    @Ignore
    CastingTime(int value, TimeUnit unit) { super(CastingTimeType.TIME, value, unit); }

    @Ignore
    CastingTime() { super(CastingTimeType.ACTION, SECONDS_PER_ROUND, TimeUnit.SECOND); }

    int timeInSeconds() { return getBaseValue(); }

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
        final int r = getBaseValue() - other.getBaseValue();
        if (r != 0) { return r; }
        return type.ordinal() - other.type.ordinal();
    }


}
