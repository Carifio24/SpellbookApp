package dnd.jon.spellbook;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocalizationUtils {

    private static final List<String> supportedLanguages = new ArrayList<>();
    static {
        supportedLanguages.add("en");
        supportedLanguages.add("pt");
    }

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

    static Locale getLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return LocaleList.getDefault().get(0);
        } else{
            return Locale.getDefault();
        }
    }

    static Locale defaultSpellLocale() {
        final Locale locale = getLocale();
        final String language = locale.getLanguage();
        if (isLanguageSupported(language)) {
            return locale;
        } else {
            return Locale.US;
        }
    }

    static boolean isLanguageSupported(String languageCode) {
        return supportedLanguages.contains(languageCode);
    }

    static String getCurrentLanguage() {
        return getLocale().getLanguage();
    }

    static @NonNull Context getLocalizedContext(Context context, Locale desiredLocale) {
        final Configuration conf = new Configuration(context.getResources().getConfiguration());
        conf.setLocale(desiredLocale);
        return context.createConfigurationContext(conf);
    }

    static @NonNull Resources getLocalizedResources(Context context, Locale desiredLocale) {
        return getLocalizedContext(context, desiredLocale).getResources();
    }

    static @NonNull Locale getInternalLocale() {
        return Locale.US;
    }

    static @NonNull Context getInternalContext(Context context) {
        return getLocalizedContext(context, getInternalLocale());
    }

    static CasterClass[] supportedClasses() { return CasterClass.values(); }
    static Source[] supportedSources() { return Source.values(); }
    static Source[] supportedCoreSourcebooks() { return Source.coreSourcebooks(); }
    static Source[] supportedNonCoreSourcebooks() { return Source.nonCoreSourcebooks(); }

    private static <E extends Enum<E>> int[] supportedIDs(E[] items, Map<E,Integer> map) {
        return Arrays.stream(items).mapToInt(map::get).toArray();
    }
    static int[] supportedTableLayoutIDs() { return supportedIDs(supportedClasses(), tableLayoutIDs); }
    static int[] supportedSpellcastingInfoIDs() { return supportedIDs(supportedClasses(), spellcastingInfoIDs); }

    // BCP language codes are in BCP 47 format
    // https://appmakers.dev/bcp-47-language-codes-list/
    static boolean hasOneInstalled(String[] bcpLanguageCodes, String languageCode) {
        final LocaleList localeList = LocaleList.getDefault();
        final Locale locale = localeList.getFirstMatch(bcpLanguageCodes);
        return locale.getLanguage().equals(languageCode);
    }
    static boolean hasEnglishInstalled() {
        return hasOneInstalled(new String[]{"en-AU", "en-GB", "en-IE", "en-US", "en-ZA"}, "en");
    }

    static boolean hasPortugueseInstalled() {
        return hasOneInstalled(new String[]{"pt-BR", "pt-PT"}, "pt");
    }

}
