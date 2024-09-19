package dnd.jon.spellbook;

import java.util.HashMap;
import java.util.Map;

public enum Ruleset {
    RULES_2014("2014"),
    RULES_2024("2024"),
    RULES_CREATED("created");

    private final String internalName;

    String getInternalName() { return internalName; }

    private static final Map<String, Ruleset> _internalNameMap = new HashMap<>();

    Ruleset(String internalName) {
        this.internalName = internalName;
    }

    static Ruleset fromInternalName(String name) {
        return _internalNameMap.get(name);
    }

    static {
        for (Ruleset ruleset : Ruleset.values()) {
            _internalNameMap.put(ruleset.getInternalName(), ruleset);
        }
    }
}
