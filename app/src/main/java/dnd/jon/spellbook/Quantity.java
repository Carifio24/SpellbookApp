package dnd.jon.spellbook;

public abstract class Quantity<ValueType extends Enum<ValueType> & QuantityType, UnitType extends Unit> implements Comparable<Quantity<ValueType, UnitType>> {

    final ValueType type;
    final int value;
    final UnitType unit;
    final String str;

    Quantity(ValueType type, int value, UnitType unit, String str) {
        this.type = type;
        this.value = value;
        this.unit = unit;
        this.str = str;
    }

    Quantity(ValueType type, int value, UnitType unit) {
        this(type, value, unit, "");
    }

    int baseValue() { return value * unit.value(); }

    public ValueType getType() { return type; }
    public UnitType getUnit() { return unit; }
    public int getValue() { return value; }

    boolean isTypeSpanning() { return type.isSpanningType(); }

    public int compareTo(Quantity<ValueType, UnitType> other) {
        if (type == other.type) {
            return baseValue() - other.baseValue();
        }
        return type.ordinal() - other.type.ordinal();
    }

    abstract public String string();

}
