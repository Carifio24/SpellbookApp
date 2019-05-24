package dnd.jon.spellbook;

public class Duration extends Quantity<Duration.DurationType, TimeUnit>{

    enum DurationType { Instantaneous, Spanning };

    Duration(DurationType type, int value, TimeUnit unit, String str) {
        super(type, value, unit, str);
    }

    int timeInSeconds() { return baseValue(); }

    public String string() {
        if (!str.isEmpty()) { return str; }
        if (type == DurationType.Instantaneous) {
            return type.name();
        } else {
            String unitStr = value == 1 ? unit.name() : unit.pluralName();
            return value + " " + unitStr;
        }
    }

    static Duration fromString(String s) {
        if (s.startsWith(DurationType.Instantaneous.name())) {
            return new Duration(DurationType.Instantaneous, 0, TimeUnit.second, s);
        } else {
            if (s.startsWith("Up to ")) {
                s = s.substring(6);
                String[] sSplit = s.split(" ", 2);
                int value = Integer.parseInt(sSplit[0]);
                TimeUnit unit = Time
            }
        }
    }

}
