package dnd.jon.spellbook;

import android.content.Context;

import androidx.annotation.Keep;

import java.util.HashMap;
import java.util.function.Function;

public class Range extends Quantity<Range.RangeType, LengthUnit> {

    public enum RangeType implements QuantityType {
        SPECIAL(R.string.special,"Special"), SELF(R.string.self, "Self"), TOUCH(R.string.touch,"Touch"), SIGHT(R.string.sight,"Sight"), RANGED(R.string.finite_range,"Finite range"), UNLIMITED(R.string.unlimited,"Unlimited");

        // Only property is the name
        final private int displayNameID;
        final private String internalName;
        public int getDisplayNameID() { return displayNameID; }
        String getInternalName() { return internalName; }

        // Constructor
        RangeType(int displayNameID, String internalName) {
            this.displayNameID = displayNameID;
            this.internalName = internalName;
        }

        // Used for lookup by name
        // Useful when parsing JSON
        private static final HashMap<String, RangeType> _nameMap = new HashMap<>();
        static {
            for (RangeType durationType : RangeType.values()) {
                _nameMap.put(durationType.internalName, durationType);
            }
        }

        // Create the instance from its name
        // Useful for parsing the spell JSON
        @Keep
        public static RangeType fromDisplayName(String name) { return _nameMap.get(name); }

        private static final RangeType[] unusualTypes = { TOUCH, SPECIAL, SIGHT, UNLIMITED };
        public boolean isSpanningType() { return this == RANGED; }
        public RangeType getSpanningType() { return RANGED; }

    }

    // Convenience constructors
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

    // A more descriptive version of baseValue
    int lengthInFeet() { return baseValue(); }

    // Return a string description
    private String makeString(Function<RangeType,String> stringGetter) {
        final String name = stringGetter.apply(type);
        switch (type) {
            case TOUCH:
            case SPECIAL:
            case UNLIMITED:
            case SIGHT:
                return name;
            case SELF: {
                if (value > 0) {
                    return name + " (" + value + " foot radius)";
                } else {
                    return name;
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
    String internalString() {
        if (!str.isEmpty()) { return str; }
        return makeString(type -> type.internalName);
    }
    public String string(Context context) {
        return makeString(type -> context.getString(type.displayNameID));
    }


    // Create a range from a string
    static Range fromInternalString(String s) {
        try {

            // The "unusual" range types
            for (Range.RangeType rangeType : RangeType.unusualTypes) {
                if (s.startsWith(rangeType.internalName)) {
                    return new Range(rangeType, 0, LengthUnit.FOOT, s);
                }
            }

            // Self and ranged types
            if (s.startsWith(RangeType.SELF.internalName)) {
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
            // Mostly for testing this out
            // If the user hits a garbled string description in the asset file,
            // not really much that can be done
            e.printStackTrace();
            return new Range();
        }
    }

}
