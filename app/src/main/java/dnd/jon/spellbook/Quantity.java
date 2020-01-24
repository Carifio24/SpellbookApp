package dnd.jon.spellbook;

public abstract class Quantity<QuantityType extends Enum<QuantityType>, UnitType extends Unit> implements Comparable<Quantity<QuantityType, UnitType>> {

    QuantityType type;
    int value;
    UnitType unit;
    String str;

    Quantity(QuantityType type, int value, UnitType unit, String str) {
        this.type = type;
        this.value = value;
        this.unit = unit;
        this.str = str;
    }

    Quantity(QuantityType type, int value, UnitType unit) {
        this(type, value, unit, "");
    }

    int baseValue() { return value * unit.value(); }

    public int compareTo(Quantity<QuantityType, UnitType> other) {
        if (type == other.type) {
            return baseValue() - other.baseValue();
        }
        return type.ordinal() - other.type.ordinal();
    }

    abstract public String string();

}
