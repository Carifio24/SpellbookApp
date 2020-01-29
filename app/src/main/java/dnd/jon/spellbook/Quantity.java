package dnd.jon.spellbook;

import java.lang.reflect.ParameterizedType;

public abstract class Quantity<ValueType extends Enum<ValueType> & QuantityType, UnitType extends Unit> implements Comparable<Quantity<ValueType, UnitType>> {

    ValueType type;
    int value;
    UnitType unit;
    String str;

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

    public int compareTo(Quantity<ValueType, UnitType> other) {
        if (type == other.type) {
            return baseValue() - other.baseValue();
        }
        return type.ordinal() - other.type.ordinal();
    }

    abstract public String string();

}
