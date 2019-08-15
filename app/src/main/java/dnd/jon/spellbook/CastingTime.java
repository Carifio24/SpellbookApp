package dnd.jon.spellbook;

public class CastingTime extends Quantity<CastingTime.CastingType, TimeUnit> {

    enum CastingType { Action, BonusAction, Reaction, Time }

    private static int timePerRound = 6;

    CastingTime(CastingType type, int value, TimeUnit unit, String str) { super(type, value, unit, str); }

    CastingTime() { this(CastingType.Action, timePerRound, TimeUnit.second, ""); }

    public String string() {
        if (!str.isEmpty()) { return str; }
        if (type == CastingType.Time) {
            String unitStr = (value == 1) ? unit.name() : unit.pluralName();
            return value + " " + unitStr;
        } else {
            String typeStr = " " + type.name().toLowerCase();
            if (value != 1) {
                typeStr += "s";
            }
            return value + typeStr;
        }
    }

    static CastingTime fromString(String s) {
        try {
            String[] sSplit = s.split(" ", 2);
            int value = Integer.parseInt(sSplit[0]);
            String typeStr = sSplit[1];

            // If the type is one of the action types
            CastingType type = null;
            if (typeStr.startsWith("action")) {
                type = CastingType.Action;
            } else if (typeStr.startsWith("bonus action")) {
                type = CastingType.BonusAction;
            } else if(typeStr.startsWith("reaction")) {
                type = CastingType.Reaction;
            }
            if (type != null) {
                int inRounds = value * timePerRound;
                return new CastingTime(type, inRounds, TimeUnit.second, s);
            }

            // Otherwise, get the time unit
            TimeUnit unit = TimeUnit.fromString(sSplit[1]);
            return new CastingTime(CastingType.Time, value, unit, s);

        } catch (Exception e) {
            e.printStackTrace();
            return new CastingTime();
        }

    }


}
