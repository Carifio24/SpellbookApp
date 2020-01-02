package dnd.jon.spellbook;

import java.util.HashMap;
import java.util.Map;

enum Sourcebook {
    PLAYERS_HANDBOOK(0, "Player's Handbook", "PHB"), XANATHARS_GTE(1, "Xanathar's Guide to Everything", "XGE"), SWORD_COAST_AG(2, "Sword Coast Adv. Guide", "SCAG");

    // Constructor
    Sourcebook(int value, String displayName, String code) {
        this.value = value;
        this.dispName = displayName;
        this.sbCode = code;
    }

    final int value;
    final private String dispName;
    final private String sbCode;

    String displayName() { return dispName; }
    String code() { return sbCode; }

    private static final Map<Integer,Sourcebook> _map = new HashMap<>();
    static {
        for (Sourcebook s : Sourcebook.values()) {
            _map.put(s.value, s);
        }
    }

    static Sourcebook fromValue(int value) {
        return _map.get(value);
    }

}
