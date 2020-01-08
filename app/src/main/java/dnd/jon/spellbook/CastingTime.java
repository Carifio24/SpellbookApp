package dnd.jon.spellbook;

public class CastingTime extends Quantity<CastingTime.CastingType, TimeUnit> {

    enum CastingType {
        ACTION("action"), BONUS_ACTION("bonus action"), REACTION("reaction"), TIME("time");

        private final String displayName;
        String getDisplayName() { return displayName; }

        CastingType(String displayName) { this.displayName = displayName; }

        static final CastingType[] actionTypes = { ACTION, BONUS_ACTION, REACTION };

    }

    private static int timePerRound = 6;

    CastingTime(CastingType type, int value, TimeUnit unit, String str) { super(type, value, unit, str); }

    CastingTime() { this(CastingType.ACTION, timePerRound, TimeUnit.SECOND, ""); }

    public String string() {
        if (!str.isEmpty()) { return str; }
        if (type == CastingType.TIME) {
            String unitStr = (value == 1) ? unit.singularName() : unit.pluralName();
            return value + " " + unitStr;
        } else {
            String typeStr = " " + type.getDisplayName();
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
            for (CastingType ct : CastingType.actionTypes) {
                if (typeStr.startsWith(ct.getDisplayName())) {
                    type = ct;
                    break;
                }
            }
            if (type != null) {
                int inRounds = value * timePerRound;
                return new CastingTime(type, inRounds, TimeUnit.SECOND, s);
            }

            // Otherwise, get the time unit
            TimeUnit unit = TimeUnit.fromString(sSplit[1]);
            return new CastingTime(CastingType.TIME, value, unit, s);

        } catch (Exception e) {
            e.printStackTrace();
            return new CastingTime();
        }

    }


}
