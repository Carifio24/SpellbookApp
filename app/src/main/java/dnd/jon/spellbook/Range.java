package dnd.jon.spellbook;

import androidx.annotation.Keep;

import java.util.HashMap;
import java.util.function.Function;

public class Range extends Quantity<Range.RangeType, LengthUnit> {

    public enum RangeType implements QuantityType {
        SPECIAL(R.string.special,"Special"),
        SELF(R.string.self, "Self"),
        TOUCH(R.string.touch,"Touch"),
        SIGHT(R.string.sight,"Sight"),
        RANGED(R.string.finite_range,"Finite range"),
        UNLIMITED(R.string.unlimited,"Unlimited");

        // Only property is the name
        final private int displayNameID;
        final private String internalName;
        public int getDisplayNameID() { return displayNameID; }
        public String getInternalName() { return internalName; }

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
        public static RangeType fromInternalName(String name) { return _nameMap.get(name); }

        private static final RangeType[] unusualTypes = { TOUCH, SPECIAL, SIGHT, UNLIMITED };
        public boolean isSpanningType() { return this == RANGED; }
        public RangeType getSpanningType() { return RANGED; }

    }

    // Convenience constructors
    Range(RangeType type, float value, LengthUnit unit, String str) {
        super(type, value, unit, str);
    }
    Range(RangeType type, float length) {
        super(type, length, LengthUnit.FOOT);
    }
    Range(RangeType type) {
        this(type, 0);
    }
    Range() {
        this(RangeType.SELF, 0);
    }

    // A more descriptive version of baseValue
    float lengthInFeet() { return baseValue(); }

    // Return a string description
    String makeString(boolean useStored, Function<RangeType,String> typeNameGetter, Function<LengthUnit,String> unitSingularNameGetter, Function<LengthUnit,String> unitPluralNameGetter, String footRadius) {
        if (useStored && !str.isEmpty()) { return str; }
        final String name = typeNameGetter.apply(type);
        final String valueString = DisplayUtils.DECIMAL_FORMAT.format(value);
        switch (type) {
            case TOUCH:
            case SPECIAL:
            case UNLIMITED:
            case SIGHT:
                return name;
            case SELF: {
                if (value > 0) {
                    return name + " (" + valueString + " " + footRadius + ")";
                } else {
                    return name;
                }
            }
            case RANGED: {
                final Function<LengthUnit,String> unitNameGetter = (value == 1) ? unitSingularNameGetter : unitPluralNameGetter;
                final String ft = unitNameGetter.apply(unit);
                System.out.println("ft is " + ft);
                return valueString + " " + ft;
            }
            default:
                return ""; // We'll never get here, the above cases exhaust the enum
        }
    }

    String makeString(Function<RangeType,String> typeNameGetter, Function<LengthUnit,String> unitSingularNameGetter, Function<LengthUnit,String> unitPluralNameGetter, String footRadius) {
        return makeString(true, typeNameGetter, unitSingularNameGetter, unitPluralNameGetter, footRadius);
    }

    String internalString() {
        return makeString(false, RangeType::getInternalName, LengthUnit::getInternalName, LengthUnit::getInternalName, "foot radius");
    }


    // Create a range from a string
    static Range fromString(String s, Function<RangeType,String> typeNameGetter, Function<String, LengthUnit> lengthUnitMaker, boolean useForStr) {
        try {

            // The "unusual" range types
            for (Range.RangeType rangeType : RangeType.unusualTypes) {
                if (s.startsWith(typeNameGetter.apply(rangeType))) {
                    return new Range(rangeType, 0, LengthUnit.FOOT, s);
                }
            }

            // Self and ranged types
            if (s.startsWith(typeNameGetter.apply(RangeType.SELF))) {
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
                    float length;
                    LengthUnit unit;
                    try {
                        length = Float.parseFloat(distSplit[0].replace(",", "."));
                        unit = lengthUnitMaker.apply(distSplit[1]);
                    } catch (NumberFormatException e) {
                        length = Float.parseFloat(distSplit[2].replace(",", "."));
                        unit = lengthUnitMaker.apply(distSplit[3]);
                    }
                    return new Range(RangeType.SELF, length, unit, s);
                }
            } else {
                final String[] sSplit = s.split(" ");
                System.out.println("s is " + s);
                final float length = Float.parseFloat(sSplit[0].replace(",", "."));
                final LengthUnit unit = lengthUnitMaker.apply(sSplit[1]);
                final String str = useForStr ? s : "";
                return new Range(RangeType.RANGED, length, unit, str);
            }
        } catch (Exception e) {
            // Mostly for testing this out
            // If the user hits a garbled string description in the asset file,
            // not really much that can be done
            e.printStackTrace();
            return new Range();
        }
    }

    // Default is to use the input string as the string representation
    static Range fromString(String s, Function<RangeType,String> typeNameGetter, Function<String, LengthUnit> lengthUnitMaker) {
        return fromString(s, typeNameGetter, lengthUnitMaker, true);
    }

    static Range fromInternalString(String s) {
        return fromString(s, RangeType::getInternalName, LengthUnit::fromInternalName, false);
    }

}
