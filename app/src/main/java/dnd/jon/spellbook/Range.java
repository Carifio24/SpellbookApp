package dnd.jon.spellbook;

import java.util.HashMap;

public class Range extends Quantity<Range.RangeType, LengthUnit> {

    public enum RangeType implements QuantityType {
        SPECIAL("Special"), SELF("Self"), TOUCH("Touch"), SIGHT("Sight"), RANGED("Ranged"), UNLIMITED("Unlimited");

        final private String displayName;
        public String getDisplayName() { return displayName; }

        RangeType(String name) { this.displayName = name; }

        private static final HashMap<String, RangeType> _nameMap = new HashMap<>();
        static {
            for (RangeType durationType : RangeType.values()) {
                _nameMap.put(durationType.displayName, durationType);
            }
        }

        static RangeType fromDisplayName(String name) { return _nameMap.get(name); }

        private static final RangeType[] unusualTypes = { TOUCH, SPECIAL, SIGHT, UNLIMITED };

        public boolean isSpanningType() { return this == RANGED; }
        public RangeType getSpanningType() { return RANGED; }

    }

    Range(RangeType type, int value, LengthUnit unit, String str) {
        super(type, value, unit, str);
    }

    Range(RangeType type, int length) {
        super(type, length, LengthUnit.FOOT);
    }

    Range(RangeType type) {
        this(type, 0);
    }

    Range() {
        this(RangeType.SELF, 0);
    }

    int lengthInFeet() { return baseValue(); }

    public String string() {
        if (!str.isEmpty()) { return str; }
        switch (type) {
            case TOUCH:
            case SPECIAL:
            case UNLIMITED:
            case SIGHT:
                return type.displayName;
            case SELF: {
                if (value > 0) {
                    return type.displayName + " (" + value + " foot radius)";
                } else {
                    return type.displayName;
                }
            }
            case RANGED: {
                String ft = (value == 1) ? unit.singularName() : unit.pluralName();
                return value + " " + ft;
            }
            default:
                return ""; // We'll never get here, the above cases exhaust the enum
        }
    }

    static Range fromString(String s) {
        try {

            // The "unusual" range types
            for (Range.RangeType rangeType : RangeType.unusualTypes) {
                if (s.startsWith(rangeType.displayName)) {
                    return new Range(rangeType, 0, LengthUnit.FOOT, s);
                }
            }

            // Self and ranged types
            if (s.startsWith(RangeType.SELF.displayName)) {
                final String[] sSplit = s.split(" ", 2);
                if (sSplit.length == 1) {
                    return new Range(RangeType.SELF);
                } else {
                    String distStr = sSplit[1];
                    if (!(distStr.startsWith("(") && distStr.endsWith(")"))) {
                        throw new Exception("Error parsing radius of Self spell");
                    }
                    distStr = distStr.substring(1, distStr.length() - 2);
                    String[] distSplit = distStr.split(" ");
                    final int length = Integer.parseInt(distSplit[0]);
                    final LengthUnit unit = LengthUnit.fromString(distSplit[1]);
                    return new Range(RangeType.SELF, length, unit, s);
                }
            } else {
                final String[] sSplit = s.split(" ");
                final int length = Integer.parseInt(sSplit[0]);
                final LengthUnit unit = LengthUnit.fromString(sSplit[1]);
                return new Range(RangeType.RANGED, length, unit, s);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Range();
        }
    }

}
