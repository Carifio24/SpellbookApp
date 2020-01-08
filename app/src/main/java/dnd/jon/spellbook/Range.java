package dnd.jon.spellbook;

public class Range extends Quantity<Range.RangeType, LengthUnit> {

    enum RangeType {
        Special("Special"), Self("Self"), Touch("Touch"), Sight("Sight"), Ranged("Ranged"), Unlimited("Unlimited");

        final private String displayName;

        RangeType(String name) { this.displayName = name; }

        private static final RangeType[] unusualTypes = { Touch, Special, Sight, Unlimited };

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
        this(RangeType.Self, 0);
    }

    int lengthInFeet() { return baseValue(); }

    public String string() {
        if (!str.isEmpty()) { return str; }
        switch (type) {
            case Touch:
            case Special:
            case Unlimited:
            case Sight:
                return type.displayName;
            case Self: {
                if (value > 0) {
                    return type.displayName + " (" + value + " foot radius)";
                } else {
                    return type.displayName;
                }
            }
            case Ranged: {
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
            if (s.startsWith(RangeType.Self.displayName)) {
                final String[] sSplit = s.split(" ", 2);
                if (sSplit.length == 1) {
                    return new Range(RangeType.Self);
                } else {
                    String distStr = sSplit[1];
                    if (!(distStr.startsWith("(") && distStr.endsWith(")"))) {
                        throw new Exception("Error parsing radius of Self spell");
                    }
                    distStr = distStr.substring(1, distStr.length() - 2);
                    String[] distSplit = distStr.split(" ");
                    final int length = Integer.parseInt(distSplit[0]);
                    final LengthUnit unit = LengthUnit.fromString(distSplit[1]);
                    return new Range(RangeType.Self, length, unit, s);
                }
            } else {
                final String[] sSplit = s.split(" ");
                final int length = Integer.parseInt(sSplit[0]);
                final LengthUnit unit = LengthUnit.fromString(sSplit[1]);
                return new Range(RangeType.Ranged, length, unit, s);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Range();
        }
    }

}
