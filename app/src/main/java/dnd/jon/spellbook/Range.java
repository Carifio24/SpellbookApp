package dnd.jon.spellbook;

public class Range implements Comparable<Range> {

    private enum RangeType { Special, Self, Touch, Sight, Ranged, Unlimited }

    private RangeType type;
    private int length;
    private LengthUnit unit;
    private String str;

    Range(RangeType type, int length, LengthUnit unit, String str) {
        this.type = type;
        this.length = length > 0 ? length : 0;
        this.unit = unit;
        this.str = str;
    }

    Range(RangeType type, int length, LengthUnit unit) {
        this(type, length, unit, "");
    }

    Range(RangeType type, int length) {
        this(type, length, LengthUnit.Foot);
    }

    Range(RangeType type) {
        this(type, 0);
    }

    Range() {
        this(RangeType.Self, 0);
    }

    int lengthInFeet() { return length * unit.inFeet(); }

    public int compareTo(Range other) {
        if (type == other.type) {
            return lengthInFeet() - other.lengthInFeet();
        }
        return type.ordinal() - other.type.ordinal();
    }

    public String string() {
        if (!str.isEmpty()) { return str; }
        switch (type) {
            case Touch:
                return "Touch";
            case Self:
                if (length > 0) {
                    return "Self (" + length + " foot radius)";
                } else {
                    return "Self";
                }
            case Ranged:
                String ft = (length == 1) ? " foot" : " feet";
                return length + ft;
            default:
                return ""; // We'll never get here, the above cases exhaust the enum
        }
    }

    static Range fromString(String s) throws Exception {
        if (s.startsWith("Touch")) {
            return new Range(RangeType.Touch, 0, LengthUnit.Foot, s);
        } else if (s.startsWith("Special")) {
            return new Range(RangeType.Special, -1, LengthUnit.Foot, s);
        } else if (s.startsWith("Sight")) {
            return new Range(RangeType.Sight, 0, LengthUnit.Foot, s);
        } else if (s.startsWith("Unlimited")) {
            return new Range(RangeType.Unlimited, 0, LengthUnit.Foot, s);
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
