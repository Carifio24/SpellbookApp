package dnd.jon.spellbook;

public class Range extends Quantity<Range.RangeType, LengthUnit> {

    enum RangeType { Special, Self, Touch, Sight, Ranged, Unlimited }

    Range(RangeType type, int value, LengthUnit unit, String str) {
        super(type, value, unit, str);
    }

    Range(RangeType type, int length) {
        super(type, length, LengthUnit.foot);
    }

    Range(RangeType type) {
        this(type, 0);
    }

    Range() {
        this(RangeType.Self, 0);
    }

    public String string() {
        if (!str.isEmpty()) { return str; }
        switch (type) {
            case Touch:
                return "Touch";
            case Self:
                if (value > 0) {
                    return "Self (" + value + " foot radius)";
                } else {
                    return "Self";
                }
            case Ranged:
                String ft = (value == 1) ? " foot" : " feet";
                return value + ft;
            default:
                return ""; // We'll never get here, the above cases exhaust the enum
        }
    }

    static Range fromString(String s) throws Exception {
        if (s.startsWith("Touch")) {
            return new Range(RangeType.Touch, 0, LengthUnit.foot, s);
        } else if (s.startsWith("Special")) {
            return new Range(RangeType.Special, -1, LengthUnit.foot, s);
        } else if (s.startsWith("Sight")) {
            return new Range(RangeType.Sight, 0, LengthUnit.foot, s);
        } else if (s.startsWith("Unlimited")) {
            return new Range(RangeType.Unlimited, 0, LengthUnit.foot, s);
        } else if (s.startsWith("Self")) {
            String[] sSplit = s.split(" ", 2);
            if (sSplit.length == 1) {
                return new Range(RangeType.Self);
            } else {
                String distStr = sSplit[1];
                if (! (distStr.startsWith("(") && distStr.endsWith(")")) ) {
                    throw new Exception("Error parsing radius of Self spell");
                }
                distStr = distStr.substring(1, distStr.length()-2);
                String[] distSplit = distStr.split(" ");
                int length = Integer.parseInt(distSplit[0]);
                LengthUnit unit = LengthUnit.fromString(distSplit[1]);
                return new Range(RangeType.Self, length, unit, s);
            }
        } else {
            String[] sSplit = s.split(" ");
            int length = Integer.parseInt(sSplit[0]);
            LengthUnit unit = LengthUnit.fromString(sSplit[1]);
            return new Range(RangeType.Ranged, length, unit, s);
        }
    }

}
