package dnd.jon.spellbook;

import android.util.SparseArray;

import androidx.annotation.Keep;

import java.util.HashMap;
import java.util.Map;

public enum Sourcebook implements NameDisplayable {
    PLAYERS_HANDBOOK(0, R.string.phb_name, R.string.phb_code, "Player's Handbook", "PHB"),
    XANATHARS_GTE(1, R.string.xge_name,R.string.xge_code, "Xanathar's Guide to Everything", "XGE"),
    SWORD_COAST_AG(2, R.string.scag_name,R.string.scag_code, "Sword Coast Adv. Guide", "SCAG"),
    TASHAS_COE(3, R.string.tce_name, R.string.tce_code, "Tasha's Cauldron of Everything", "TCE"),
    ACQUISITIONS_INC(4, R.string.ai_name, R.string.ai_code, "Acquisitions Incorporated", "AI"),
    LOST_LAB_KWALISH(5, R.string.llk_name, R.string.llk_code, "Lost Laboratory of Kwalish", "LLK"),
    RIME_FROSTMAIDEN(6, R.string.rf_name, R.string.rf_code, "Rime of the Frostmaiden", "RF");

    // Constructor
    Sourcebook(int value, int displayNameID, int codeID, String internalName, String internalCode) {
        this.value = value;
        this.displayNameID = displayNameID;
        this.codeID = codeID;
        this.internalName = internalName;
        this.internalCode = internalCode;
    }

    final private int value;
    final private int displayNameID;
    final private int codeID;
    final private String internalName;
    final private String internalCode;

    int getValue() { return value; }
    public int getDisplayNameID() { return displayNameID; }
    public int getCodeID() { return codeID; }
    public String getInternalName() { return internalName; }
    public String getInternalCode() { return internalCode; }

    private static final SparseArray<Sourcebook> _valueMap = new SparseArray<>();
    private static final Map<String,Sourcebook> _nameMap = new HashMap<>();
    private static final Map<String,Sourcebook> _codeMap = new HashMap<>();
    static {
        for (Sourcebook s : Sourcebook.values()) {
            _valueMap.put(s.value, s);
            _nameMap.put(s.internalName, s);
            _codeMap.put(s.internalCode, s);
        }
    }

    static Sourcebook fromValue(int value) {
        return _valueMap.get(value);
    }

    @Keep
    static Sourcebook fromInternalName(String name) {
        final Sourcebook sb = _codeMap.get(name);
        if (sb != null) {
            return sb;
        }
        return _nameMap.get(name);
    }

}
