package dnd.jon.spellbook;

import java.util.HashMap;
import java.util.Map;

enum Sourcebook {
    PLAYERS_HANDBOOK(0), XANATHARS_GTE(1), SWORD_COAST_AG(2);

    int value;
    Sourcebook(int val) {value = val;}

    private static final Map<Integer,Sourcebook> _map = new HashMap<>();
    static {
        for (Sourcebook s : Sourcebook.values()) {
            _map.put(s.value, s);
        }
    }

    static Sourcebook from(int value) {
        return _map.get(value);
    }

    String code() {
        switch (this) {
            case PLAYERS_HANDBOOK:
                return "PHB";
            case XANATHARS_GTE:
                return "XGE";
            case SWORD_COAST_AG:
                return "SCAG";
            default:
                return "PHB"; // Placeholder only, the above options exhaust the enum
        }
    }

}
