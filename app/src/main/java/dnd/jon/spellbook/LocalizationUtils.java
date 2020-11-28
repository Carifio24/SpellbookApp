package dnd.jon.spellbook;

import android.os.Build;
import android.os.LocaleList;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class LocalizationUtils {

    private static final Map<CasterClass,Integer> tableLayoutIDs = new EnumMap<CasterClass,Integer>(CasterClass.class) {{
            put(CasterClass.ARTIFICER, R.layout.artificer_table_layout);
            put(CasterClass.BARD, R.layout.bard_table_layout);
            put(CasterClass.CLERIC, R.layout.cleric_table_layout);
            put(CasterClass.DRUID, R.layout.druid_table_layout);
            put(CasterClass.PALADIN, R.layout.paladin_table_layout);
            put(CasterClass.RANGER, R.layout.ranger_table_layout);
            put(CasterClass.SORCERER, R.layout.sorcerer_table_layout);
            put(CasterClass.WARLOCK, R.layout.warlock_table_layout);
            put(CasterClass.WIZARD, R.layout.wizard_table_layout);
    }};
    private static final Map<CasterClass,Integer> spellcastingInfoIDs = new EnumMap<CasterClass, Integer>(CasterClass.class) {{
        put(CasterClass.ARTIFICER, R.string.artificer_spellcasting_info);
        put(CasterClass.BARD, R.string.bard_spellcasting_info);
        put(CasterClass.CLERIC, R.string.cleric_spellcasting_info);
        put(CasterClass.DRUID, R.string.druid_spellcasting_info);
        put(CasterClass.PALADIN, R.string.paladin_spellcasting_info);
        put(CasterClass.RANGER, R.string.ranger_spellcasting_info);
        put(CasterClass.SORCERER, R.string.sorcerer_spellcasting_info);
        put(CasterClass.WARLOCK, R.string.warlock_spellcasting_info);
        put(CasterClass.WIZARD, R.string.wizard_spellcasting_info);
    }};

    private static CasterClass[] _supportedClasses = null;
    private static Sourcebook[] _supportedSources = null;

    static String getCurrentLanguage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return LocaleList.getDefault().get(0).getLanguage();
        } else{
            return Locale.getDefault().getLanguage();
        }
    }

    static CasterClass[] supportedClasses() {
        if (_supportedClasses != null) { return _supportedClasses; }
        final String language = LocalizationUtils.getCurrentLanguage();
        if (language.contains("pt")) {
            _supportedClasses = new CasterClass[] { CasterClass.BARD, CasterClass.CLERIC, CasterClass.DRUID, CasterClass.PALADIN,
                    CasterClass.RANGER, CasterClass.SORCERER, CasterClass.WARLOCK, CasterClass.WIZARD };
        } else {
            _supportedClasses = CasterClass.values();
        }
        return _supportedClasses;
    }

    static Sourcebook[] supportedSources() {
        if (_supportedSources != null) { return _supportedSources; }
        final String language = LocalizationUtils.getCurrentLanguage();
        if (language.contains("pt")) {
            _supportedSources = new Sourcebook[] { Sourcebook.PLAYERS_HANDBOOK, Sourcebook.XANATHARS_GTE, Sourcebook.SWORD_COAST_AG };
        } else {
            _supportedSources = Sourcebook.values();
        }
        return _supportedSources;
    }

    private static <E extends Enum<E>> int[] supportedIDs(E[] items, Map<E,Integer> map) {
        return Arrays.stream(items).mapToInt(map::get).toArray();
    }
    static int[] supportedTableLayoutIDs() { return supportedIDs(supportedClasses(), tableLayoutIDs); }
    static int[] supportedSpellcastingInfoIDs() { return supportedIDs(supportedClasses(), spellcastingInfoIDs); }

}
