package dnd.jon.spellbook;

import android.util.SparseArray;

import androidx.annotation.Keep;

import java.util.HashMap;
import java.util.Map;

public enum Sourcebook implements NameDisplayable {
    PLAYERS_HANDBOOK(0, R.string.phb_name, R.string.phb_code, "PHB"),
    XANATHARS_GTE(1, R.string.xge_name,R.string.xge_code, "XGE"),
    SWORD_COAST_AG(2, R.string.scag_name,R.string.scag_code, "SCAG"),
    TASHAS_COE(3, R.string.tce_name, R.string.tce_code, "TCE");

    // Constructor
    Sourcebook(int value, int displayNameID, int codeID, String internalName) {
        this.value = value;
        this.displayNameID = displayNameID;
        this.codeID = codeID;
        this.internalName = internalName;
    }

    final private int value;
    final private int displayNameID;
    final private int codeID;
    final private String internalName;

    int getValue() { return value; }
    public int getDisplayNameID() { return displayNameID; }
    public int getCodeID() { return codeID; }
    public String getInternalName() { return internalName; }

    private static final SparseArray<Sourcebook> _map = new SparseArray<>();
    static {
        for (Sourcebook s : Sourcebook.values()) {
            _map.put(s.value, s);
        }
    }

    private static final Map<String,Sourcebook> _nameMap = new HashMap<>();
    static {
        for (Sourcebook s : Sourcebook.values()) {
            _nameMap.put(s.internalName, s);
        }
    }


    static Sourcebook fromValue(int value) {
        return _map.get(value);
    }

    @Keep
    static Sourcebook fromInternalName(String name) { return _nameMap.get(name); }

    static Sourcebook[] supported() {
        final String language = LocalizationUtils.getCurrentLanguage();
        if (language.contains("pt")) {
            return new Sourcebook[] { PLAYERS_HANDBOOK, XANATHARS_GTE, SWORD_COAST_AG };
        } else {
            return Sourcebook.values();
        }
    }

}
