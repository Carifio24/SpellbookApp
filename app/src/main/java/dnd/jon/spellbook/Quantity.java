package dnd.jon.spellbook;

public abstract class Quantity<QuantityType extends Enum<QuantityType>, UnitType extends Unit> implements Comparable<Quantity<QuantityType, UnitType>> {

    protected QuantityType type;
    protected int value;
    protected UnitType unit;
    protected String str;

    Quantity(QuantityType type, int value, UnitType unit, String str) {
        this.type = type;
        this.value = value;
        this.unit = unit;
        this.str = str;
    }

    Quantity(QuantityType type, int value, UnitType unit) {
        this(type, value, unit, "");
    }

    protected int baseValue() { return value * unit.value(); }

    public int compareTo(Quantity<QuantityType, UnitType> other) {
        if (type == other.type) {
            return baseValue() - other.baseValue();
        }
        return type.ordinal() - other.type.ordinal();
    }

    abstract public String string();

}
