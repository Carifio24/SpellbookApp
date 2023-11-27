package dnd.jon.spellbook;

import android.util.SparseArray;

import androidx.annotation.Keep;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Source implements NameDisplayable {
    private static Source[] _values = new Source[]{};
    private static final SparseArray<Source> _valueMap = new SparseArray<>();
    private static final Map<String, Source> _nameMap = new HashMap<>();
    private static final Map<String, Source> _codeMap = new HashMap<>();

    static final Source PLAYERS_HANDBOOK = new Source(R.string.phb_name, R.string.phb_code, "Player's Handbook", "PHB", true);
    static final Source XANATHARS_GTE = new Source(R.string.xge_name, R.string.xge_code, "Xanathar's Guide to Everything", "XGE", true);
    static final Source SWORD_COAST_AG = new Source(R.string.scag_name,R.string.scag_code, "Sword Coast Adv. Guide", "SCAG", false);
    static final Source TASHAS_COE = new Source(R.string.tce_name, R.string.tce_code, "Tasha's Cauldron of Everything", "TCE", true);
    static final Source ACQUISITIONS_INC = new Source(R.string.ai_name, R.string.ai_code, "Acquisitions Incorporated", "AI", false);
    static final Source LOST_LAB_KWALISH = new Source(R.string.llk_name, R.string.llk_code, "Lost Laboratory of Kwalish", "LLK", false);
    static final Source RIME_FROSTMAIDEN = new Source(R.string.rf_name, R.string.rf_code, "Rime of the Frostmaiden", "RF", false);
    static final Source EXPLORERS_GTW = new Source(R.string.egw_name, R.string.egw_code, "Explorer's Guide to Wildemount", "EGW", false);
    static final Source FIZBANS_TOD = new Source(R.string.ftd_name, R.string.ftd_code, "Fizban's Treasury of Dragons", "FTD", false);
    static final Source STRIXHAVEN_COC = new Source(R.string.scc_name, R.string.scc_code, "Strixhaven: A Curriculum of Chaos", "SCC", false);
    static final Source ASTRAL_AG = new Source(R.string.aag_name, R.string.aag_code, "Astral Adventurer's Guide", "AAG", false);
    static final Source GUILDMASTERS_GTR = new Source(R.string.ggr_name, R.string.ggr_code, "Guildmaster's Guide to Ravnica", "GGR", false);
    static final Source TALDOREI_CSR = new Source(R.string.tdcsr_name, R.string.tdcsr_code, "Tal'Dorei Campaign Setting Reborn", "TDCSR", false);

    // Constructor
    private Source(int value, int displayNameID, int codeID, String internalName, String internalCode, boolean core, boolean created) {
        this.value = value;
        this.displayNameID = displayNameID;
        this.codeID = codeID;
        this.internalName = internalName;
        this.internalCode = internalCode;
        this.core = core;
        this.created = created;

        addToStructures(this);
    }

    Source(int displayNameID, int codeID, String internalName, String internalCode, boolean core, boolean created) {
        this(_values.length, displayNameID, codeID, internalName, internalCode, core, created);
    }

    public Source(int displayNameID, int codeID, String internalName, String internalCode, boolean core) {
        this(_values.length, displayNameID, codeID, internalName, internalCode, core, false);
    }

    final private int value;
    final private int displayNameID;
    final private int codeID;
    final private String internalName;
    final private String internalCode;
    final private boolean core;
    final private boolean created;

    int getValue() { return value; }
    public int getDisplayNameID() { return displayNameID; }
    public int getCodeID() { return codeID; }
    public String getInternalName() { return internalName; }
    public String getInternalCode() { return internalCode; }

    static Source[] values() { return _values; }
    static Collection<Source> collection() { return Arrays.asList(_values.clone()); }

    private static void addToStructures(Source source) {
        _values = Arrays.copyOf(_values, _values.length + 1);
        _values[_values.length - 1] = source;
        _valueMap.put(source.value, source);
        _nameMap.put(source.internalName, source);
        _codeMap.put(source.internalCode, source);
    }

    static Source fromValue(int value) {
        return _valueMap.get(value);
    }

    @Keep
    static Source fromInternalName(String name) {
        final Source sb = _codeMap.get(name);
        if (sb != null) {
            return sb;
        }
        return _nameMap.get(name);
    }

    private static Source[] filteredSourcebooks(Predicate<Source> filter) {
        return Arrays.stream(Source.values()).filter(filter).toArray(Source[]::new);
    }

    static Source[] coreSourcebooks() {
        return filteredSourcebooks(sb -> sb.core && !sb.created);
    }
    static Source[] nonCoreSourcebooks() {
        return filteredSourcebooks(sb -> !(sb.core || sb.created));
    }

    static Source[] sourcebooks() {
        return filteredSourcebooks(sb -> !sb.created);
    }

    static Source[] createdSources() {
        return filteredSourcebooks(sb -> sb.created);
    }

    public boolean equals(Source other) {
        return this.internalName.equals(other.internalName) && this.internalCode.equals(other.internalCode);
    }


}
