package dnd.jon.spellbook;

import java.util.TreeSet;

public class Duration extends Quantity<Duration.DurationType, TimeUnit>{

    enum DurationType {
        Special("Special"), Instantaneous("Instantaneous"), Spanning("Spanning"), UntilDispelled("Until dispelled");

        final private String displayName;

        DurationType(String name) { this.displayName = name; }

        static private final DurationType[] nonSpanning = { Special, Instantaneous, UntilDispelled };

    }

    Duration(DurationType type, int value, TimeUnit unit, String str) {
        super(type, value, unit, str);
    }

    Duration() { this(DurationType.Instantaneous, 0, TimeUnit.second, ""); }

    int timeInSeconds() { return baseValue(); }

    public String string() {
        if (!str.isEmpty()) { return str; }
        switch (type) {
            case Instantaneous:
            case Special:
            case UntilDispelled:
                return type.displayName;
            case Spanning:
                String unitStr = (value == 1) ? unit.name() : unit.pluralName();
                return value + " " + unitStr;
            default:
                return ""; // Unreachable, the above switch exhausts the enum
        }
    }



    static Duration fromString(String s) {
        try {

            // For non-spanning duration types
            for (DurationType durationType : DurationType.nonSpanning) {
                if (s.startsWith(durationType.displayName)) {
                    return new Duration(durationType, 0, TimeUnit.second, s);
                }
            }

            // If we have a real distance
            String concentrationPrefix = "Up to ";
            String t = s;
            if (s.startsWith(concentrationPrefix)) {
                t = s.substring(concentrationPrefix.length());
            }
            String[] sSplit = t.split(" ", 2);
            int value = Integer.parseInt(sSplit[0]);
            TimeUnit unit = TimeUnit.fromString(sSplit[1]);
            return new Duration(DurationType.Spanning, value, unit, s);

        } catch (Exception e) {
            e.printStackTrace();
            return new Duration();
        }

    }

}
