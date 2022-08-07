package dnd.jon.spellbook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class Spellbook {

    static final int MIN_SPELL_LEVEL = 0;
    static final int MAX_SPELL_LEVEL = 9;

    static final Version V_2_0_0 = new Version(2, 0, 0);
    static final Version V_2_10_0 = new Version(2, 10 ,0);
    static final Version V_2_11_0 = new Version(2, 11 ,0);
    static final Version V_2_12_0 = new Version(2, 12 ,0);
    static final Version V_2_13_0 = new Version(2, 13 ,0);
    static final Version V_3_0_0 = new Version(3, 0, 0);

    static final Version[] VERSIONS = { V_2_0_0, V_2_10_0, V_2_11_0, V_2_11_0, V_2_12_0, V_2_13_0 };
    static private final Map<Version, Collection<Source>> SOURCES_NEW_IN_VERSION = new HashMap<Version, Collection<Source>>() {{
       put(V_2_0_0, Arrays.asList(Source.PLAYERS_HANDBOOK, Source.XANATHARS_GTE, Source.SWORD_COAST_AG));
       put(V_2_10_0, Arrays.asList(Source.TASHAS_COE));
       put(V_2_11_0, Arrays.asList(Source.ACQUISITIONS_INC, Source.EXPLORERS_GTW, Source.LOST_LAB_KWALISH, Source.RIME_FROSTMAIDEN));
       put(V_2_12_0, Arrays.asList(Source.FIZBANS_TOD));
       put(V_2_13_0, Arrays.asList(Source.STRIXHAVEN_COC));
    }};

    static Collection<Source> newSourcesForVersion(Version version) {
        return SOURCES_NEW_IN_VERSION.get(version);
    }

    static private Collection<Source> sourcesByCondition(Function<Version,Boolean> versionCondition) {
        return SOURCES_NEW_IN_VERSION.entrySet().stream().filter(entry -> versionCondition.apply(entry.getKey())).map(entry -> entry.getValue()).flatMap(Collection::stream).collect(Collectors.toList());
    }

    static Collection<Source> sourcesPriorToVersion(Version version) {
        return sourcesByCondition(v -> v.compareTo(version) < 0);
    }

    static Collection<Source> sourcesAddedAfterVersion(Version version) {
        return sourcesByCondition(v -> v.compareTo(version) > 0);
    }

    static Collection<CasterClass> classesAddedAfterVersion(Version version) {
        // We need the list to be mutable, so we wrap in the new ArrayList<>() call
        final List<CasterClass> classes = new ArrayList<>(Arrays.asList(CasterClass.values().clone()));
        if (version.compareTo(V_2_10_0) < 0) {
            classes.remove(CasterClass.ARTIFICER);
        }
        return classes;
    }


}
