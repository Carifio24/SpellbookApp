package dnd.jon.spellbook;

import androidx.annotation.Keep;

import java.util.EnumMap;
import java.util.HashMap;

public class Duration extends Quantity<Duration.DurationType, TimeUnit>{

    public enum DurationType implements QuantityType {
        SPECIAL("Special"), INSTANTANEOUS("Instantaneous"), SPANNING("Finite duration"), UNTIL_DISPELLED("Until dispelled");

        final private String displayName;
        public String getDisplayName() { return displayName; }

        DurationType(String name) { this.displayName = name; }

        private static final HashMap<String, DurationType> _nameMap = new HashMap<>();
        static {
            for (DurationType durationType : DurationType.values()) {
                _nameMap.put(durationType.displayName, durationType);
            }
        }

        @Keep
        public static DurationType fromDisplayName(String name) { return _nameMap.get(name); }

        private static final DurationType[] nonSpanning = { SPECIAL, INSTANTANEOUS, UNTIL_DISPELLED };

        public boolean isSpanningType() { return this == SPANNING; }
        public DurationType getSpanningType() { return SPANNING; }

    }

    Duration(DurationType type, int value, TimeUnit unit, String str) {
        super(type, value, unit, str);
    }

    Duration() { this(DurationType.INSTANTANEOUS, 0, TimeUnit.SECOND, ""); }

    int timeInSeconds() { return baseValue(); }

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
            e.printStackTrace();
            return new Duration();
        }

    }

}
