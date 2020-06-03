package dnd.jon.spellbook;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity
public abstract class Quantity<ValueType extends Enum<ValueType> & QuantityType, UnitType extends Unit> implements Comparable<Quantity<ValueType, UnitType>> {

    @ColumnInfo(name = "type") final ValueType type;
    @ColumnInfo(name = "value") final int value;
    @ColumnInfo(name = "unit_type") final UnitType unit;
    @ColumnInfo(name = "base_value") final int baseValue;
    @ColumnInfo(name = "description") final String str;

    Quantity(ValueType type, int value, UnitType unit, String str) {
        this.type = type;
        this.value = value;
        this.unit = unit;
        this.str = str;
        this.baseValue = value * unit.value();
    }

    Quantity(ValueType type, int value, UnitType unit) {
        this(type, value, unit, "");
    }

    public int getBaseValue() { return baseValue; }

    public ValueType getType() { return type; }
    public UnitType getUnit() { return unit; }
    public int getValue() { return value; }

    boolean isTypeSpanning() { return type.isSpanningType(); }

    public int compareTo(Quantity<ValueType, UnitType> other) {
        if (type == other.type) {
            return baseValue - other.baseValue;
        }
        return type.ordinal() - other.type.ordinal();
    }

    abstract public String string();

}
