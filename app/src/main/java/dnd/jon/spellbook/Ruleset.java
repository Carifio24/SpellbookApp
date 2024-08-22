package dnd.jon.spellbook;


import android.util.SparseArray;

enum Ruleset {

    DND_2014(0, "5e_2014", R.string.rules_2014),
    DND_2024(1, "5e_2024", R.string.rules_2024);

    final int value;
    final String key;
    final int nameResourceID;

    int getValue() { return value; }

    Ruleset(int value, String key, int nameResourceID) {
        this.value = value;
        this.key = key;
        this.nameResourceID = nameResourceID;
    }

    private static final SparseArray<Ruleset> _map = new SparseArray<>();
    static {
        for (Ruleset ruleset : Ruleset.values()) {
            _map.put(ruleset.value, ruleset);
        }
    }

    static Ruleset fromValue(int value) { return _map.get(value); }
}