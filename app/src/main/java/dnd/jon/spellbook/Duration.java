package dnd.jon.spellbook;

public class Duration extends Quantity<Duration.DurationType, TimeUnit>{

    enum DurationType { Special, Instantaneous, Spanning, UntilDispelled }

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
                return type.name();
            case UntilDispelled:
                return "Until dispelled";
            case Spanning:
                String unitStr = (value == 1) ? unit.name() : unit.pluralName();
                return value + " " + unitStr;
            default:
                return ""; // Unreachable, the above switch exhausts the enum
        }
    }

    static Duration fromString(String s) {
        try {
            if (s.startsWith(DurationType.Special.name())) {
                return new Duration(DurationType.Special, 0, TimeUnit.second, s);
            } else if (s.startsWith(DurationType.Instantaneous.name())) {
                return new Duration(DurationType.Instantaneous, 0, TimeUnit.second, s);
            } else if (s.startsWith("Until dispelled")) {
                return new Duration(DurationType.UntilDispelled, 0, TimeUnit.second, s);
            } else {
                String concentrationPrefix = "Up to ";
                String t = s;
                if (s.startsWith(concentrationPrefix)) {
                    t = s.substring(concentrationPrefix.length());
                }
                String[] sSplit = t.split(" ", 2);
                int value = Integer.parseInt(sSplit[0]);
                TimeUnit unit = TimeUnit.fromString(sSplit[1]);
                return new Duration(DurationType.Spanning, value, unit, s);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Duration();
        }

    }

}
