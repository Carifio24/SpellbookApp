package dnd.jon.spellbook;

import java.util.HashMap;
import java.util.Map;

enum TimeUnit implements Valued {
    second(1), round(6), minute(60), hour(60*60), day(24*60*60), year(365*24*60*60);
    private int seconds;

    TimeUnit(int seconds) { this.seconds = seconds; }

    int inSeconds() { return seconds; }
    public int value() { return seconds; }

    private static final Map<TimeUnit, String[]> names = new HashMap<TimeUnit, String[]>() {{
        put(second, new String[]{"second", "seconds", "s", "s.", "secs"});
        put(round, new String[]{"round", "rounds"});
        put(minute, new String[]{"minute", "minutes", "min", "min."});
        put(hour, new String[]{"hour", "hours", "hr", "hr."});
        put(day, new String[]{"day", "days"});
        put(year, new String[]{"year", "years", "yr", "yr."});
    }};

    String pluralName() {
        return this.name() + "s";
    }

    String abbreviation() {
        switch (this) {
            case second:
                return "s";
            case minute:
                return "min";
            case hour:
                return "hr";
            case day:
                return "day";
            case year:
                return "year";
            default:
                return ""; // Unreachable
        }
    }


}
