package dnd.jon.spellbook;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Keep;

enum TimeUnit implements Unit {
    SECOND(1, R.string.second_unit, R.string.seconds, R.string.sec, "second", "seconds"),
    ROUND(6, R.string.round, R.string.rounds, R.string.rd, "round", "rounds"),
    MINUTE(60, R.string.minute, R.string.minutes, R.string.min, "minute", "days"),
    HOUR(60*60, R.string.hour, R.string.hours, R.string.hr, "hour", "hours"),
    DAY(24*60*60, R.string.day, R.string.days, R.string.dy, "day", "days"),
    YEAR(365*24*60*60, R.string.year, R.string.years, R.string.yr, "year", "years");

    private final int seconds;
    private final int singularNameID;
    private final int pluralNameID;
    private final int abbreviationID;
    private final String internalName;
    private final String internalPlural;

    TimeUnit(int seconds, int singularNameID, int pluralNameID, int abbreviationID, String internalName, String internalPlural) {
        this.seconds = seconds;
        this.singularNameID = singularNameID;
        this.pluralNameID = pluralNameID;
        this.abbreviationID = abbreviationID;
        this.internalName = internalName;
        this.internalPlural = internalPlural;
    }

    int inSeconds() { return seconds; }
    public int value() { return seconds; }

    public int getSingularNameID() { return singularNameID; }
    public int getPluralNameID() { return pluralNameID; }
    public int getAbbreviationID() { return abbreviationID; }
    public String getInternalName() { return internalName; }

    // Used for lookup by name
    private static final HashMap<String, TimeUnit> _nameMap = new HashMap<>();
    private static final HashMap<String, TimeUnit> _pluralNameMap = new HashMap<>();
    static {
        for (TimeUnit timeUnit : TimeUnit.values()) {
            _nameMap.put(timeUnit.internalName, timeUnit);
            _pluralNameMap.put(timeUnit.internalPlural, timeUnit);
        }
    }

    @Keep
    public static TimeUnit fromInternalName(String name) {
        TimeUnit unit = _nameMap.get(name);
        if (unit != null) {
            return unit;
        }
        return _pluralNameMap.get(name);
    }


}
