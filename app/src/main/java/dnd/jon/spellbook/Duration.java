package dnd.jon.spellbook;

public class Duration {

    private enum DurationType { Instantaneous, Spanning };

    private DurationType type;
    private int value;
    private TimeUnit unit;
    private String str;

    Duration(DurationType type, int value, TimeUnit unit, String str) {
        this.type = type;
        this.value = value;
        this.unit = unit;
        this.str = str;
    }

    int timeInSeconds() { return value * unit.inSeconds(); }

}
