package dnd.jon.spellbook;

import java.util.HashMap;
import java.util.Map;

enum TimeUnit implements Unit {
    SECOND(1), ROUND(6), MINUTE(60), HOUR(60*60), DAY(24*60*60), YEAR(365*24*60*60);
    private final int seconds;

    TimeUnit(int seconds) {
        this.seconds = seconds;
    }

    int inSeconds() { return seconds; }
    public int value() { return seconds; }

    private static final Map<TimeUnit, String[]> names = new HashMap<TimeUnit, String[]>() {{
        put(SECOND, new String[]{"second", "seconds", "s", "s.", "secs"});
        put(ROUND, new String[]{"round", "rounds"});
        put(MINUTE, new String[]{"minute", "minutes", "min", "min."});
        put(HOUR, new String[]{"hour", "hours", "hr", "hr."});
        put(DAY, new String[]{"day", "days"});
        put(YEAR, new String[]{"year", "years", "yr", "yr."});
    }};

    public String singularName() { return names.get(this)[0]; }
    public String pluralName() {
        return names.get(this)[0] + "s";
    }

    public String abbreviation() {
        switch (this) {
            case SECOND:
                return "s";
            case MINUTE:
                return "min";
            case HOUR:
                return "hr";
            case DAY:
                return "dy";
            case YEAR:
                return "yr";
            default:
                return ""; // Unreachable
        }
    }

    static TimeUnit fromString(String s) throws Exception {
        s = s.toLowerCase();
        for (HashMap.Entry<TimeUnit, String[]> entry : names.entrySet()) {
            for (String t : entry.getValue()) {
                if (s.equals(t)) { return entry.getKey(); }
            }
        }
        throw new Exception("Not a valid unit string");
    }

}
