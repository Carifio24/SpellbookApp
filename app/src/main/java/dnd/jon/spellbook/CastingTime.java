package dnd.jon.spellbook;

public class CastingTime extends Quantity<CastingTime.CastingType, TimeUnit> {

    enum CastingType { Action, BonusAction, Reaction, Time }

    CastingTime(CastingType type, int value, TimeUnit unit, String str) { super(type, value, unit, str); }

    public String string() {
        if (!str.isEmpty()) { return str; }
        if (type == CastingType.Time) {
            String unitStr = value == 1 ? unit.name() : unit.pluralName();
            return value + " " + unitStr;
        } else {
            return "1 " + type.name().toLowerCase();
        }
    }


}
