package dnd.jon.spellbook;

public class Range implements Comparable<Range> {

    private enum RangeType { Self, Touch, Ranged }

    private RangeType type;
    private int distance;

    Range(RangeType type, int distance) {
        this.type = type;
        this.distance = distance > 0 ? distance : 0;
    }

    Range(RangeType type) {
        this(type, 0);
    }

    Range() {
        this(RangeType.Self, 0);
    }

    int getDistance() { return distance; }
    RangeType getRangeType() { return type; }

    public int compareTo(Range other) {
        if (type != other.type) {
            return distance - other.distance;
        }
        return type.ordinal() - other.type.ordinal();
    }

    public String string() {
        switch (type) {
            case Touch:
                return "Touch";
            case Self:
                if (distance > 0) {
                    return "Self (" + distance + " foot radius)";
                } else {
                    return "Self";
                }
            case Ranged:
                String ft = (distance == 1) ? " foot" : " feet";
                return distance + ft;
        }
    return ""; // We'll never get here, the switch exhausts the enum
    }

    static Range fromString(String s) throws Exception {
        if (s.startsWith("Touch")) {
            return new Range(RangeType.Touch, 0);
        } else if (s.startsWith("Self")) {
            String sSplit[] = s.split(" ", 2);
            if (sSplit.length == 1) {
                return new Range(RangeType.Self, 0);
            } else {
                String distStr = sSplit[1];
                if (! (distStr.startsWith("(") && distStr.endsWith(")")) ) {
                    throw new Exception("Error parsing radius of Self spell");
                }
                distStr = distStr.substring(1, distStr.length()-2);
                String distSplit[] = distStr.split(" ");
                int distance = Integer.parseInt(distSplit[0]);
                return new Range(RangeType.Self, distance);
            }
        } else {
            String sSplit[] = s.split(" ");
            int distance = Integer.parseInt(sSplit[0]);
            if (!sSplit[1].equals("feet")) {
                throw new Exception("Error parsing ranged spell");
            }
            return new Range(RangeType.Ranged, distance);
        }

    }



}
