package dnd.jon.spellbook;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.google.common.truth.Truth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@RunWith(RobolectricTestRunner.class)
public class CharacterProfileTest {

    private static void checkIsDefaultProfileFor(CharacterProfile cp, Version version) {
        final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();
        final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();
        Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.ALL);
        Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.NAME);
        Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.NAME);
        Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isFalse();
        Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isFalse();
        Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(Spellbook.MIN_SPELL_LEVEL);
        Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(Spellbook.MAX_SPELL_LEVEL);

        final Collection<Source> shouldBeVisibleSources = Collections.singletonList(Source.PLAYERS_HANDBOOK);
        final Collection<Source> shouldBeHiddenSources = SpellbookUtils.complement(shouldBeVisibleSources, Source.values());
        Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
        Truth.assertThat(sortFilterStatus.getVisibleSources(false)).containsExactlyElementsIn(shouldBeHiddenSources);
        Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(School.values());
        Truth.assertThat(sortFilterStatus.getVisibleSchools(false)).isEmpty();

        final Collection<CasterClass> shouldBeVisibleClasses = Arrays.asList(CasterClass.values());
        Truth.assertThat(sortFilterStatus.getVisibleClasses(true)).containsExactlyElementsIn(shouldBeVisibleClasses);
        Truth.assertThat(sortFilterStatus.getVisibleClasses(false)).isEmpty();

        Truth.assertThat(sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.SECOND);
        Truth.assertThat(sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(0);
        Truth.assertThat(sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
        Truth.assertThat(sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(24);

        Truth.assertThat(sortFilterStatus.getMinUnit(Range.RangeType.class)).isEqualTo(LengthUnit.FOOT);
        Truth.assertThat(sortFilterStatus.getMinValue(Range.RangeType.class)).isEqualTo(0);
        Truth.assertThat(sortFilterStatus.getMaxUnit(Range.RangeType.class)).isEqualTo(LengthUnit.MILE);
        Truth.assertThat(sortFilterStatus.getMaxValue(Range.RangeType.class)).isEqualTo(1);

        Truth.assertThat(sortFilterStatus.getMinUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.SECOND);
        Truth.assertThat(sortFilterStatus.getMinValue(Duration.DurationType.class)).isEqualTo(0);
        Truth.assertThat(sortFilterStatus.getMaxUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.DAY);
        Truth.assertThat(sortFilterStatus.getMaxValue(Duration.DurationType.class)).isEqualTo(30);

        Truth.assertThat(sortFilterStatus.getVerbalFilter(true)).isTrue();
        Truth.assertThat(sortFilterStatus.getSomaticFilter(true)).isTrue();
        Truth.assertThat(sortFilterStatus.getMaterialFilter(true)).isTrue();
        Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
        Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
        Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();

        Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(true)).containsExactlyElementsIn(CastingTime.CastingTimeType.values());
        Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(true)).containsExactlyElementsIn(Duration.DurationType.values());
        Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(true)).containsExactlyElementsIn(Range.RangeType.values());

        Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(false)).isEmpty();
        Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(false)).isEmpty();
        Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(false)).isEmpty();

        Truth.assertThat(sortFilterStatus.getApplyFiltersToSearch()).isFalse();
        Truth.assertThat(sortFilterStatus.getApplyFiltersToLists()).isFalse();
        Truth.assertThat(sortFilterStatus.getUseTashasExpandedLists()).isFalse();

        Truth.assertThat(sortFilterStatus.getConcentrationFilter(true)).isTrue();
        Truth.assertThat(sortFilterStatus.getConcentrationFilter(false)).isTrue();
        Truth.assertThat(sortFilterStatus.getRitualFilter(true)).isTrue();
        Truth.assertThat(sortFilterStatus.getRitualFilter(false)).isTrue();

        Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).isEmpty();
        Truth.assertThat(spellFilterStatus.preparedSpellIDs()).isEmpty();
        Truth.assertThat(spellFilterStatus.knownSpellIDs()).isEmpty();
    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_13_3_n1() {
        final String jsonString = "{\"CharacterName\":\"t1\",\"Spells\":[],\"SortField1\":\"Name\",\"SortField2\":\"Name\",\"Reverse1\":false,\"Reverse2\":false,\"HiddenSchools\":[],\"HiddenDurationTypes\":[],\"HiddenRangeTypes\":[],\"HiddenCasters\":[],\"HiddenSourcebooks\":[\"XGE\",\"SCAG\",\"TCE\",\"AI\",\"LLK\",\"RF\",\"EGW\",\"FTD\",\"SCC\"],\"HiddenCastingTimeTypes\":[],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"day\",\"MinText\":\"0\",\"MaxText\":\"30\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"0\",\"MaxText\":\"1\"},\"CastingTimeFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"hour\",\"MinText\":\"0\",\"MaxText\":\"24\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,true,true],\"MinSpellLevel\":0,\"MaxSpellLevel\":9,\"ApplyFiltersToSpellLists\":false,\"ApplyFiltersToSearch\":false,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.13.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("t1");
            checkIsDefaultProfileFor(cp, new Version(2, 13, 3));
        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_13_3_n2() {
        final String jsonString = "{\"CharacterName\":\"t2\",\"Spells\":[{\"SpellID\":449,\"Favorite\":false,\"Prepared\":true,\"Known\":true},{\"SpellID\":450,\"Favorite\":true,\"Prepared\":false,\"Known\":true},{\"SpellID\":388,\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellID\":485,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":454,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":486,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":364,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":365,\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellID\":432,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":368,\"Favorite\":true,\"Prepared\":true,\"Known\":false},{\"SpellID\":412,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":477,\"Favorite\":true,\"Prepared\":true,\"Known\":false},{\"SpellID\":447,\"Favorite\":false,\"Prepared\":false,\"Known\":true}],\"SortField1\":\"Level\",\"SortField2\":\"School\",\"Reverse1\":false,\"Reverse2\":true,\"HiddenSchools\":[\"Abjuration\",\"Conjuration\"],\"HiddenDurationTypes\":[\"Until dispelled\"],\"HiddenRangeTypes\":[\"Special\",\"Self\"],\"HiddenCasters\":[\"Artificer\",\"Bard\"],\"HiddenSourcebooks\":[\"PHB\",\"SCAG\",\"LLK\",\"RF\",\"EGW\",\"FTD\",\"SCC\"],\"HiddenCastingTimeTypes\":[\"bonus action\"],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"round\",\"MaxUnit\":\"day\",\"MinText\":\"2\",\"MaxText\":\"3\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"0\",\"MaxText\":\"3\"},\"CastingTimeFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"hour\",\"MinText\":\"0\",\"MaxText\":\"24\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,true,false],\"MinSpellLevel\":0,\"MaxSpellLevel\":6,\"ApplyFiltersToSpellLists\":false,\"ApplyFiltersToSearch\":true,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.13.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("t2");

            final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();
            final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();
            Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.LEVEL);
            Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.SCHOOL);
            Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isTrue();
            Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(6);

            final Version version = new Version(2, 13, 2);
            final Collection<Source> shouldBeHiddenSources = new ArrayList<>(Arrays.asList(Source.PLAYERS_HANDBOOK, Source.SWORD_COAST_AG, Source.LOST_LAB_KWALISH, Source.RIME_FROSTMAIDEN, Source.EXPLORERS_GTW, Source.FIZBANS_TOD, Source.STRIXHAVEN_COC));
            shouldBeHiddenSources.addAll(Spellbook.sourcesAddedAfterVersion(version));
            final Collection<Source> shouldBeVisibleSources = SpellbookUtils.complement(shouldBeHiddenSources, Source.values());
            Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleSources(false)).containsExactlyElementsIn(shouldBeHiddenSources);

            final Collection<School> shouldBeHiddenSchools = Arrays.asList(School.ABJURATION, School.CONJURATION);
            final Collection<School> shouldBeVisibleSchools = SpellbookUtils.complement(shouldBeHiddenSchools, School.values());
            Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(shouldBeVisibleSchools);
            Truth.assertThat(sortFilterStatus.getVisibleSchools(false)).containsExactlyElementsIn(shouldBeHiddenSchools);

            final Collection<CasterClass> shouldBeHiddenClasses = Arrays.asList(CasterClass.ARTIFICER, CasterClass.BARD);
            final Collection<CasterClass> shouldBeVisibleClasses = SpellbookUtils.complement(shouldBeHiddenClasses, CasterClass.values());
            Truth.assertThat(sortFilterStatus.getVisibleClasses(true)).containsExactlyElementsIn(shouldBeVisibleClasses);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(false)).containsExactlyElementsIn(shouldBeHiddenClasses);

            Truth.assertThat(sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(24);

            Truth.assertThat(sortFilterStatus.getMinUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.ROUND);
            Truth.assertThat(sortFilterStatus.getMinValue(Duration.DurationType.class)).isEqualTo(2);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.DAY);
            Truth.assertThat(sortFilterStatus.getMaxValue(Duration.DurationType.class)).isEqualTo(3);

            Truth.assertThat(sortFilterStatus.getMinUnit(Range.RangeType.class)).isEqualTo(LengthUnit.FOOT);
            Truth.assertThat(sortFilterStatus.getMinValue(Range.RangeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Range.RangeType.class)).isEqualTo(LengthUnit.MILE);
            Truth.assertThat(sortFilterStatus.getMaxValue(Range.RangeType.class)).isEqualTo(3);

            Truth.assertThat(sortFilterStatus.getVerbalFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isFalse();

            final Collection<CastingTime.CastingTimeType> shouldBeHiddenCTTs = Collections.singletonList(CastingTime.CastingTimeType.BONUS_ACTION);
            final Collection<CastingTime.CastingTimeType> shouldBeVisibleCTTs = SpellbookUtils.complement(shouldBeHiddenCTTs, CastingTime.CastingTimeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(true)).containsExactlyElementsIn(shouldBeVisibleCTTs);
            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(false)).containsExactlyElementsIn(shouldBeHiddenCTTs);

            final Collection<Duration.DurationType> shouldBeHiddenDTs = Collections.singletonList(Duration.DurationType.UNTIL_DISPELLED);
            final Collection<Duration.DurationType> shouldBeVisibleDTs = SpellbookUtils.complement(shouldBeHiddenDTs, Duration.DurationType.values());
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(true)).containsExactlyElementsIn(shouldBeVisibleDTs);
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(false)).containsExactlyElementsIn(shouldBeHiddenDTs);

            final Collection<Range.RangeType> shouldBeHiddenRTs = Arrays.asList(Range.RangeType.SPECIAL, Range.RangeType.SELF);
            final Collection<Range.RangeType> shouldBeVisibleRTs = SpellbookUtils.complement(shouldBeHiddenRTs, Range.RangeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(true)).containsExactlyElementsIn(shouldBeVisibleRTs);
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(false)).containsExactlyElementsIn(shouldBeHiddenRTs);

            Truth.assertThat(sortFilterStatus.getApplyFiltersToSearch()).isTrue();
            Truth.assertThat(sortFilterStatus.getApplyFiltersToLists()).isFalse();
            Truth.assertThat(sortFilterStatus.getUseTashasExpandedLists()).isFalse();

            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(new Integer[]{364, 368, 432, 450, 454, 477});
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(new Integer[]{368, 412, 449, 454, 477, 485, 486});
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(new Integer[]{365, 388, 447, 449, 450, 454});

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_13_2_n1() {
        final String jsonString = "{\"CharacterName\":\"testA\",\"Spells\":[{\"SpellID\":177,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":116,\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellID\":117,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":88,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":216,\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellID\":202,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":27,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":283,\"Favorite\":false,\"Prepared\":true,\"Known\":true},{\"SpellID\":380,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":412,\"Favorite\":true,\"Prepared\":true,\"Known\":false},{\"SpellID\":430,\"Favorite\":true,\"Prepared\":false,\"Known\":true}],\"SortField1\":\"Level\",\"SortField2\":\"School\",\"Reverse1\":false,\"Reverse2\":true,\"HiddenSchools\":[\"Conjuration\",\"Enchantment\"],\"HiddenDurationTypes\":[\"Instantaneous\"],\"HiddenRangeTypes\":[\"Special\",\"Self\"],\"HiddenCasters\":[\"Cleric\",\"Ranger\"],\"HiddenSourcebooks\":[\"SCAG\",\"AI\",\"LLK\",\"RF\",\"EGW\",\"FTD\",\"SCC\"],\"HiddenCastingTimeTypes\":[\"time\"],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"day\",\"MinText\":\"2\",\"MaxText\":\"4\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"5\",\"MaxText\":\"1\"},\"CastingTimeFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"hour\",\"MinText\":\"0\",\"MaxText\":\"24\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,false,true],\"MinSpellLevel\":2,\"MaxSpellLevel\":7,\"ApplyFiltersToSpellLists\":true,\"ApplyFiltersToSearch\":false,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.13.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("testA");

            final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();
            final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();
            Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.LEVEL);
            Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.SCHOOL);
            Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isTrue();
            Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(2);
            Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(7);

            final Version version = new Version(2, 13, 2);
            final Collection<Source> shouldBeHiddenSources = new ArrayList<>(Arrays.asList(Source.SWORD_COAST_AG, Source.ACQUISITIONS_INC, Source.LOST_LAB_KWALISH, Source.RIME_FROSTMAIDEN, Source.EXPLORERS_GTW, Source.FIZBANS_TOD, Source.STRIXHAVEN_COC));
            shouldBeHiddenSources.addAll(Spellbook.sourcesAddedAfterVersion(version));
            final Collection<Source> shouldBeVisibleSources = SpellbookUtils.complement(shouldBeHiddenSources, Source.values());
            Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleSources(false)).containsExactlyElementsIn(shouldBeHiddenSources);

            final Collection<School> shouldBeHiddenSchools = Arrays.asList(School.CONJURATION, School.ENCHANTMENT);
            final Collection<School> shouldBeVisibleSchools = SpellbookUtils.complement(shouldBeHiddenSchools, School.values());
            Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(shouldBeVisibleSchools);
            Truth.assertThat(sortFilterStatus.getVisibleSchools(false)).containsExactlyElementsIn(shouldBeHiddenSchools);

            final Collection<CasterClass> shouldBeHiddenClasses = Arrays.asList(CasterClass.CLERIC, CasterClass.RANGER);
            final Collection<CasterClass> shouldBeVisibleClasses = SpellbookUtils.complement(shouldBeHiddenClasses, CasterClass.values());
            Truth.assertThat(sortFilterStatus.getVisibleClasses(true)).containsExactlyElementsIn(shouldBeVisibleClasses);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(false)).containsExactlyElementsIn(shouldBeHiddenClasses);

            Truth.assertThat(sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(24);

            Truth.assertThat(sortFilterStatus.getMinUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(sortFilterStatus.getMinValue(Duration.DurationType.class)).isEqualTo(2);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.DAY);
            Truth.assertThat(sortFilterStatus.getMaxValue(Duration.DurationType.class)).isEqualTo(4);

            Truth.assertThat(sortFilterStatus.getMinUnit(Range.RangeType.class)).isEqualTo(LengthUnit.FOOT);
            Truth.assertThat(sortFilterStatus.getMinValue(Range.RangeType.class)).isEqualTo(5);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Range.RangeType.class)).isEqualTo(LengthUnit.MILE);
            Truth.assertThat(sortFilterStatus.getMaxValue(Range.RangeType.class)).isEqualTo(1);

            Truth.assertThat(sortFilterStatus.getVerbalFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isFalse();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();

            final Collection<CastingTime.CastingTimeType> shouldBeHiddenCTTs = Collections.singletonList(CastingTime.CastingTimeType.TIME);
            final Collection<CastingTime.CastingTimeType> shouldBeVisibleCTTs = SpellbookUtils.complement(shouldBeHiddenCTTs, CastingTime.CastingTimeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(true)).containsExactlyElementsIn(shouldBeVisibleCTTs);
            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(false)).containsExactlyElementsIn(shouldBeHiddenCTTs);

            final Collection<Duration.DurationType> shouldBeHiddenDTs = Collections.singletonList(Duration.DurationType.INSTANTANEOUS);
            final Collection<Duration.DurationType> shouldBeVisibleDTs = SpellbookUtils.complement(shouldBeHiddenDTs, Duration.DurationType.values());
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(true)).containsExactlyElementsIn(shouldBeVisibleDTs);
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(false)).containsExactlyElementsIn(shouldBeHiddenDTs);

            final Collection<Range.RangeType> shouldBeHiddenRTs = Arrays.asList(Range.RangeType.SPECIAL, Range.RangeType.SELF);
            final Collection<Range.RangeType> shouldBeVisibleRTs = SpellbookUtils.complement(shouldBeHiddenRTs, Range.RangeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(true)).containsExactlyElementsIn(shouldBeVisibleRTs);
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(false)).containsExactlyElementsIn(shouldBeHiddenRTs);

            Truth.assertThat(sortFilterStatus.getApplyFiltersToSearch()).isFalse();
            Truth.assertThat(sortFilterStatus.getApplyFiltersToLists()).isTrue();
            Truth.assertThat(sortFilterStatus.getUseTashasExpandedLists()).isFalse();

            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(new Integer[]{27, 88, 202, 380, 412, 430});
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(new Integer[]{27, 88, 117, 177, 283, 412});
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(new Integer[]{27, 88, 116, 216, 283, 430});

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_13_2_n2() {
        final String jsonString = "{\"CharacterName\":\"testB\",\"Spells\":[],\"SortField1\":\"Name\",\"SortField2\":\"Name\",\"Reverse1\":false,\"Reverse2\":false,\"HiddenSchools\":[],\"HiddenDurationTypes\":[],\"HiddenRangeTypes\":[],\"HiddenCasters\":[],\"HiddenSourcebooks\":[\"XGE\",\"SCAG\",\"TCE\",\"AI\",\"LLK\",\"RF\",\"EGW\",\"FTD\",\"SCC\"],\"HiddenCastingTimeTypes\":[],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"day\",\"MinText\":\"0\",\"MaxText\":\"30\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"0\",\"MaxText\":\"1\"},\"CastingTimeFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"hour\",\"MinText\":\"0\",\"MaxText\":\"24\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,true,true],\"MinSpellLevel\":0,\"MaxSpellLevel\":9,\"ApplyFiltersToSpellLists\":false,\"ApplyFiltersToSearch\":false,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.13.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("testB");
            checkIsDefaultProfileFor(cp, new Version(2, 13, 2));
        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_13_1_n1() {
        final String jsonString = "{\"CharacterName\":\"empty\",\"Spells\":[],\"SortField1\":\"Name\",\"SortField2\":\"Name\",\"Reverse1\":false,\"Reverse2\":false,\"HiddenSchools\":[],\"HiddenDurationTypes\":[],\"HiddenRangeTypes\":[],\"HiddenCasters\":[],\"HiddenSourcebooks\":[\"XGE\",\"SCAG\",\"TCE\",\"AI\",\"LLK\",\"RF\",\"EGW\",\"FTD\",\"SCC\"],\"HiddenCastingTimeTypes\":[],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"day\",\"MinText\":\"0\",\"MaxText\":\"30\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"0\",\"MaxText\":\"1\"},\"CastingTimeFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"hour\",\"MinText\":\"0\",\"MaxText\":\"24\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,true,true],\"MinSpellLevel\":0,\"MaxSpellLevel\":9,\"ApplyFiltersToSpellLists\":false,\"ApplyFiltersToSearch\":false,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.13.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("empty");
            checkIsDefaultProfileFor(cp, new Version(2, 13, 1));
        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_13_1_n2() {
        final String jsonString = "{\"CharacterName\":\"TESTING\",\"Spells\":[{\"SpellID\":512,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":98,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":419,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":259,\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellID\":165,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":106,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":221,\"Favorite\":true,\"Prepared\":false,\"Known\":false}],\"SortField1\":\"Range\",\"SortField2\":\"Casting Time\",\"Reverse1\":true,\"Reverse2\":true,\"HiddenSchools\":[\"Necromancy\",\"Transmutation\"],\"HiddenDurationTypes\":[\"Special\",\"Until dispelled\"],\"HiddenRangeTypes\":[],\"HiddenCasters\":[\"Warlock\",\"Wizard\"],\"HiddenSourcebooks\":[],\"HiddenCastingTimeTypes\":[],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"round\",\"MaxUnit\":\"day\",\"MinText\":\"0\",\"MaxText\":\"5\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"2\",\"MaxText\":\"2\"},\"CastingTimeFilters\":{\"MinUnit\":\"hour\",\"MaxUnit\":\"hour\",\"MinText\":\"3\",\"MaxText\":\"7\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":false,\"NotConcentration\":true,\"ComponentsFilters\":[true,false,true],\"NotComponentsFilters\":[true,true,true],\"MinSpellLevel\":4,\"MaxSpellLevel\":8,\"ApplyFiltersToSpellLists\":true,\"ApplyFiltersToSearch\":false,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.13.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("TESTING");

            final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();
            final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();
            Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.RANGE);
            Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.CASTING_TIME);
            Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isTrue();
            Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isTrue();
            Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(4);
            Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(8);

            final Version version = new Version(2, 13, 1);
            final Collection<Source> shouldBeHiddenSources = Spellbook.sourcesAddedAfterVersion(version);
            final Collection<Source> shouldBeVisibleSources = SpellbookUtils.complement(shouldBeHiddenSources, Source.values());
            Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleSources(false)).containsExactlyElementsIn(shouldBeHiddenSources);

            final Collection<School> shouldBeHiddenSchools = Arrays.asList(School.NECROMANCY, School.TRANSMUTATION);
            final Collection<School> shouldBeVisibleSchools = SpellbookUtils.complement(shouldBeHiddenSchools, School.values());
            Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(shouldBeVisibleSchools);
            Truth.assertThat(sortFilterStatus.getVisibleSchools(false)).containsExactlyElementsIn(shouldBeHiddenSchools);

            final Collection<CasterClass> shouldBeHiddenClasses = Arrays.asList(CasterClass.WARLOCK, CasterClass.WIZARD);
            final Collection<CasterClass> shouldBeVisibleClasses = SpellbookUtils.complement(shouldBeHiddenClasses, CasterClass.values());
            Truth.assertThat(sortFilterStatus.getVisibleClasses(true)).containsExactlyElementsIn(shouldBeVisibleClasses);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(false)).containsExactlyElementsIn(shouldBeHiddenClasses);

            Truth.assertThat(sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(3);
            Truth.assertThat(sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(7);

            Truth.assertThat(sortFilterStatus.getMinUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.ROUND);
            Truth.assertThat(sortFilterStatus.getMinValue(Duration.DurationType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.DAY);
            Truth.assertThat(sortFilterStatus.getMaxValue(Duration.DurationType.class)).isEqualTo(5);

            Truth.assertThat(sortFilterStatus.getMinUnit(Range.RangeType.class)).isEqualTo(LengthUnit.FOOT);
            Truth.assertThat(sortFilterStatus.getMinValue(Range.RangeType.class)).isEqualTo(2);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Range.RangeType.class)).isEqualTo(LengthUnit.MILE);
            Truth.assertThat(sortFilterStatus.getMaxValue(Range.RangeType.class)).isEqualTo(2);

            Truth.assertThat(sortFilterStatus.getVerbalFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(true)).isFalse();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();

            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(true)).containsExactlyElementsIn(CastingTime.CastingTimeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(false)).isEmpty();

            final Collection<Duration.DurationType> shouldBeHiddenDTs = Arrays.asList(Duration.DurationType.SPECIAL, Duration.DurationType.UNTIL_DISPELLED);
            final Collection<Duration.DurationType> shouldBeVisibleDTs = SpellbookUtils.complement(shouldBeHiddenDTs, Duration.DurationType.values());
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(true)).containsExactlyElementsIn(shouldBeVisibleDTs);
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(false)).containsExactlyElementsIn(shouldBeHiddenDTs);

            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(true)).containsExactlyElementsIn(Range.RangeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(false)).isEmpty();

            Truth.assertThat(sortFilterStatus.getApplyFiltersToSearch()).isFalse();
            Truth.assertThat(sortFilterStatus.getApplyFiltersToLists()).isTrue();
            Truth.assertThat(sortFilterStatus.getUseTashasExpandedLists()).isFalse();

            Truth.assertThat(sortFilterStatus.getConcentrationFilter(true)).isFalse();
            Truth.assertThat(sortFilterStatus.getConcentrationFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(false)).isTrue();

            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(new Integer[]{98, 106, 221, 512});
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(new Integer[]{98, 165, 419, 512});
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(new Integer[]{98, 259, 512});

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_13_n1() {
        final String jsonString = "{\"CharacterName\":\"abcd\",\"Spells\":[{\"SpellID\":1,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":2,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":3,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":4,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":5,\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellID\":6,\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellID\":7,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":8,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":9,\"Favorite\":true,\"Prepared\":true,\"Known\":false},{\"SpellID\":10,\"Favorite\":false,\"Prepared\":true,\"Known\":true},{\"SpellID\":11,\"Favorite\":true,\"Prepared\":false,\"Known\":true}],\"SortField1\":\"Duration\",\"SortField2\":\"Casting Time\",\"Reverse1\":false,\"Reverse2\":true,\"HiddenSchools\":[\"Divination\",\"Enchantment\"],\"HiddenDurationTypes\":[\"Finite duration\"],\"HiddenRangeTypes\":[\"Special\",\"Touch\"],\"HiddenCasters\":[\"Paladin\",\"Ranger\"],\"HiddenSourcebooks\":[\"SCAG\",\"AI\",\"LLK\",\"RF\",\"EGW\",\"FTD\",\"SCC\"],\"HiddenCastingTimeTypes\":[\"bonus action\"],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"day\",\"MinText\":\"0\",\"MaxText\":\"30\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"0\",\"MaxText\":\"1\"},\"CastingTimeFilters\":{\"MinUnit\":\"minute\",\"MaxUnit\":\"hour\",\"MinText\":\"2\",\"MaxText\":\"6\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,true,true],\"MinSpellLevel\":2,\"MaxSpellLevel\":4,\"ApplyFiltersToSpellLists\":false,\"ApplyFiltersToSearch\":true,\"UseTCEExpandedLists\":true,\"VersionCode\":\"2.13.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("abcd");

            final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();
            final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();
            Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.DURATION);
            Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.CASTING_TIME);
            Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isTrue();
            Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(2);
            Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(4);

            final Collection<Source> shouldBeHiddenSources = new ArrayList<>(Arrays.asList(Source.SWORD_COAST_AG, Source.ACQUISITIONS_INC, Source.LOST_LAB_KWALISH, Source.RIME_FROSTMAIDEN, Source.EXPLORERS_GTW, Source.FIZBANS_TOD, Source.STRIXHAVEN_COC));
            shouldBeHiddenSources.addAll(Spellbook.sourcesAddedAfterVersion(Spellbook.V_2_13_0));
            final Collection<Source> shouldBeVisibleSources = SpellbookUtils.complement(shouldBeHiddenSources, Source.values());
            Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleSources(false)).containsExactlyElementsIn(shouldBeHiddenSources);

            final Collection<School> shouldBeHiddenSchools = Arrays.asList(School.DIVINATION, School.ENCHANTMENT);
            final Collection<School> shouldBeVisibleSchools = SpellbookUtils.complement(shouldBeHiddenSchools, School.values());
            Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(shouldBeVisibleSchools);
            Truth.assertThat(sortFilterStatus.getVisibleSchools(false)).containsExactlyElementsIn(shouldBeHiddenSchools);

            final Collection<CasterClass> shouldBeHiddenClasses = Arrays.asList(CasterClass.PALADIN, CasterClass.RANGER);
            final Collection<CasterClass> shouldBeVisibleClasses = SpellbookUtils.complement(shouldBeHiddenClasses, CasterClass.values());
            Truth.assertThat(sortFilterStatus.getVisibleClasses(true)).containsExactlyElementsIn(shouldBeVisibleClasses);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(false)).containsExactlyElementsIn(shouldBeHiddenClasses);

            Truth.assertThat(sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.MINUTE);
            Truth.assertThat(sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(2);
            Truth.assertThat(sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(6);

            Truth.assertThat(sortFilterStatus.getMinUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(sortFilterStatus.getMinValue(Duration.DurationType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.DAY);
            Truth.assertThat(sortFilterStatus.getMaxValue(Duration.DurationType.class)).isEqualTo(30);

            Truth.assertThat(sortFilterStatus.getMinUnit(Range.RangeType.class)).isEqualTo(LengthUnit.FOOT);
            Truth.assertThat(sortFilterStatus.getMinValue(Range.RangeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Range.RangeType.class)).isEqualTo(LengthUnit.MILE);
            Truth.assertThat(sortFilterStatus.getMaxValue(Range.RangeType.class)).isEqualTo(1);

            Truth.assertThat(sortFilterStatus.getVerbalFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();

            final Collection<CastingTime.CastingTimeType> shouldBeHiddenCTTs = Collections.singletonList(CastingTime.CastingTimeType.BONUS_ACTION);
            final Collection<CastingTime.CastingTimeType> shouldBeVisibleCTTs = SpellbookUtils.complement(shouldBeHiddenCTTs, CastingTime.CastingTimeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(true)).containsExactlyElementsIn(shouldBeVisibleCTTs);
            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(false)).containsExactlyElementsIn(shouldBeHiddenCTTs);

            final Collection<Duration.DurationType> shouldBeHiddenDTs = Collections.singletonList(Duration.DurationType.SPANNING);
            final Collection<Duration.DurationType> shouldBeVisibleDTs = SpellbookUtils.complement(shouldBeHiddenDTs, Duration.DurationType.values());
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(true)).containsExactlyElementsIn(shouldBeVisibleDTs);
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(false)).containsExactlyElementsIn(shouldBeHiddenDTs);

            final Collection<Range.RangeType> shouldBeHiddenRTs = Arrays.asList(Range.RangeType.SPECIAL, Range.RangeType.TOUCH);
            final Collection<Range.RangeType> shouldBeVisibleRTs = SpellbookUtils.complement(shouldBeHiddenRTs, Range.RangeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(true)).containsExactlyElementsIn(shouldBeVisibleRTs);
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(false)).containsExactlyElementsIn(shouldBeHiddenRTs);

            Truth.assertThat(sortFilterStatus.getApplyFiltersToSearch()).isTrue();
            Truth.assertThat(sortFilterStatus.getApplyFiltersToLists()).isFalse();
            Truth.assertThat(sortFilterStatus.getUseTashasExpandedLists()).isTrue();

            Truth.assertThat(sortFilterStatus.getConcentrationFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getConcentrationFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(false)).isTrue();

            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(new Integer[]{1, 2, 7, 8, 9, 11});
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(new Integer[]{3, 4, 7, 8, 9, 10});
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(new Integer[]{5, 6, 7, 8, 10, 11});

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_13_n2() {
        final String jsonString = "{\"CharacterName\":\"efgh\",\"Spells\":[],\"SortField1\":\"Name\",\"SortField2\":\"Name\",\"Reverse1\":false,\"Reverse2\":false,\"HiddenSchools\":[],\"HiddenDurationTypes\":[],\"HiddenRangeTypes\":[],\"HiddenCasters\":[],\"HiddenSourcebooks\":[\"XGE\",\"SCAG\",\"TCE\",\"AI\",\"LLK\",\"RF\",\"EGW\",\"FTD\",\"SCC\"],\"HiddenCastingTimeTypes\":[],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"day\",\"MinText\":\"0\",\"MaxText\":\"30\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"0\",\"MaxText\":\"1\"},\"CastingTimeFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"hour\",\"MinText\":\"0\",\"MaxText\":\"24\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,true,true],\"MinSpellLevel\":0,\"MaxSpellLevel\":9,\"ApplyFiltersToSpellLists\":false,\"ApplyFiltersToSearch\":false,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.13.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("efgh");
            checkIsDefaultProfileFor(cp, Spellbook.V_2_13_0);
        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_12_n1() {
        final String jsonString = "{\"CharacterName\":\"M34\",\"Spells\":[{\"SpellID\":291,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":51,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":325,\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellID\":277,\"Favorite\":true,\"Prepared\":true,\"Known\":false},{\"SpellID\":230,\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellID\":199,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":376,\"Favorite\":true,\"Prepared\":true,\"Known\":false},{\"SpellID\":253,\"Favorite\":true,\"Prepared\":false,\"Known\":true},{\"SpellID\":271,\"Favorite\":false,\"Prepared\":true,\"Known\":true}],\"SortField1\":\"Range\",\"SortField2\":\"Duration\",\"Reverse1\":true,\"Reverse2\":false,\"HiddenSchools\":[\"Abjuration\"],\"HiddenDurationTypes\":[\"Special\",\"Until dispelled\"],\"HiddenRangeTypes\":[\"Finite range\"],\"HiddenCasters\":[\"Artificer\",\"Bard\"],\"HiddenSourcebooks\":[\"SCAG\",\"AI\",\"LLK\",\"RF\",\"EGW\",\"FTD\"],\"HiddenCastingTimeTypes\":[],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"day\",\"MinText\":\"5\",\"MaxText\":\"30\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"0\",\"MaxText\":\"1\"},\"CastingTimeFilters\":{\"MinUnit\":\"minute\",\"MaxUnit\":\"hour\",\"MinText\":\"10\",\"MaxText\":\"4\"}},\"StatusFilter\":\"All\",\"Ritual\":false,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[false,true,true],\"MinSpellLevel\":2,\"MaxSpellLevel\":8,\"ApplyFiltersToSpellLists\":true,\"ApplyFiltersToSearch\":false,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.12.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("M34");

            final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();
            final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();
            Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.RANGE);
            Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.DURATION);
            Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isTrue();
            Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(2);
            Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(8);

            final Collection<Source> shouldBeHiddenSources = new ArrayList<>(Arrays.asList(Source.SWORD_COAST_AG, Source.ACQUISITIONS_INC, Source.LOST_LAB_KWALISH, Source.RIME_FROSTMAIDEN, Source.EXPLORERS_GTW, Source.FIZBANS_TOD));
            shouldBeHiddenSources.addAll(Spellbook.sourcesAddedAfterVersion(Spellbook.V_2_12_0));
            final Collection<Source> shouldBeVisibleSources = SpellbookUtils.complement(shouldBeHiddenSources, Source.values());
            Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleSources(false)).containsExactlyElementsIn(shouldBeHiddenSources);

            final Collection<School> shouldBeHiddenSchools = Collections.singletonList(School.ABJURATION);
            final Collection<School> shouldBeVisibleSchools = SpellbookUtils.complement(shouldBeHiddenSchools, School.values());
            Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(shouldBeVisibleSchools);
            Truth.assertThat(sortFilterStatus.getVisibleSchools(false)).containsExactlyElementsIn(shouldBeHiddenSchools);

            final Collection<CasterClass> shouldBeBeHiddenClasses = Arrays.asList(CasterClass.ARTIFICER, CasterClass.BARD);
            final Collection<CasterClass> shouldBeVisibleClasses = SpellbookUtils.complement(shouldBeBeHiddenClasses, CasterClass.values());
            Truth.assertThat(sortFilterStatus.getVisibleClasses(true)).containsExactlyElementsIn(shouldBeVisibleClasses);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(false)).containsExactlyElementsIn(shouldBeBeHiddenClasses);

            Truth.assertThat(sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.MINUTE);
            Truth.assertThat(sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(10);
            Truth.assertThat(sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(4);

            Truth.assertThat(sortFilterStatus.getMinUnit(Range.RangeType.class)).isEqualTo(LengthUnit.FOOT);
            Truth.assertThat(sortFilterStatus.getMinValue(Range.RangeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Range.RangeType.class)).isEqualTo(LengthUnit.MILE);
            Truth.assertThat(sortFilterStatus.getMaxValue(Range.RangeType.class)).isEqualTo(1);

            Truth.assertThat(sortFilterStatus.getMinUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(sortFilterStatus.getMinValue(Duration.DurationType.class)).isEqualTo(5);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.DAY);
            Truth.assertThat(sortFilterStatus.getMaxValue(Duration.DurationType.class)).isEqualTo(30);

            Truth.assertThat(sortFilterStatus.getVerbalFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isFalse();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();

            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(true)).containsExactlyElementsIn(CastingTime.CastingTimeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(false)).isEmpty();

            final Collection<Duration.DurationType> shouldBeHiddenDTs = Arrays.asList(Duration.DurationType.SPECIAL, Duration.DurationType.UNTIL_DISPELLED);
            final Collection<Duration.DurationType> shouldBeVisibleDTs = SpellbookUtils.complement(shouldBeHiddenDTs, Duration.DurationType.values());
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(true)).containsExactlyElementsIn(shouldBeVisibleDTs);
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(false)).containsExactlyElementsIn(shouldBeHiddenDTs);

            final Collection<Range.RangeType> shouldBeHiddenRTs = Collections.singletonList(Range.RangeType.RANGED);
            final Collection<Range.RangeType> shouldBeVisibleRTs = SpellbookUtils.complement(shouldBeHiddenRTs, Range.RangeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(true)).containsExactlyElementsIn(shouldBeVisibleRTs);
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(false)).containsExactlyElementsIn(shouldBeHiddenRTs);

            Truth.assertThat(sortFilterStatus.getApplyFiltersToSearch()).isFalse();
            Truth.assertThat(sortFilterStatus.getApplyFiltersToLists()).isTrue();
            Truth.assertThat(sortFilterStatus.getUseTashasExpandedLists()).isFalse();

            Truth.assertThat(sortFilterStatus.getConcentrationFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getConcentrationFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(true)).isFalse();
            Truth.assertThat(sortFilterStatus.getRitualFilter(false)).isTrue();

            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(new Integer[]{51, 253, 277, 291, 376});
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(new Integer[]{199, 271, 277, 291, 376});
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(new Integer[]{230, 253, 271, 291, 325});

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_12_n2() {
        final String jsonString = "{\"CharacterName\":\"qwerty\",\"Spells\":[],\"SortField1\":\"Name\",\"SortField2\":\"Name\",\"Reverse1\":false,\"Reverse2\":false,\"HiddenSchools\":[],\"HiddenDurationTypes\":[],\"HiddenRangeTypes\":[],\"HiddenCasters\":[],\"HiddenSourcebooks\":[\"XGE\",\"SCAG\",\"TCE\",\"AI\",\"LLK\",\"RF\",\"EGW\",\"FTD\"],\"HiddenCastingTimeTypes\":[],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"day\",\"MinText\":\"0\",\"MaxText\":\"30\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"0\",\"MaxText\":\"1\"},\"CastingTimeFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"hour\",\"MinText\":\"0\",\"MaxText\":\"24\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,true,true],\"MinSpellLevel\":0,\"MaxSpellLevel\":9,\"ApplyFiltersToSpellLists\":false,\"ApplyFiltersToSearch\":false,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.12.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("qwerty");
            checkIsDefaultProfileFor(cp, Spellbook.V_2_12_0);
        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_11_n1() {
        final String jsonString = "{\"CharacterName\":\"test\",\"Spells\":[],\"SortField1\":\"Name\",\"SortField2\":\"Name\",\"Reverse1\":false,\"Reverse2\":false,\"HiddenSchools\":[],\"HiddenDurationTypes\":[],\"HiddenRangeTypes\":[],\"HiddenCasters\":[],\"HiddenSourcebooks\":[\"XGE\",\"SCAG\",\"TCE\",\"AI\",\"LLK\",\"RF\",\"EGW\"],\"HiddenCastingTimeTypes\":[],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"day\",\"MinText\":\"0\",\"MaxText\":\"30\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"0\",\"MaxText\":\"1\"},\"CastingTimeFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"hour\",\"MinText\":\"0\",\"MaxText\":\"24\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,true,true],\"MinSpellLevel\":0,\"MaxSpellLevel\":9,\"ApplyFiltersToSpellLists\":false,\"ApplyFiltersToSearch\":false,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.11.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("test");
            checkIsDefaultProfileFor(cp, Spellbook.V_2_11_0);
        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_11_n2() {
        final String jsonString = "{\"CharacterName\":\"wwét\",\"Spells\":[{\"SpellID\":1,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":2,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":418,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":3,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":419,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":451,\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellID\":260,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":197,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":103,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":8,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":9,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":10,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":87,\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellID\":93,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":221,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":318,\"Favorite\":false,\"Prepared\":false,\"Known\":true}],\"SortField1\":\"Duration\",\"SortField2\":\"Range\",\"Reverse1\":false,\"Reverse2\":true,\"HiddenSchools\":[\"Abjuration\",\"Conjuration\"],\"HiddenDurationTypes\":[],\"HiddenRangeTypes\":[\"Special\",\"Touch\",\"Unlimited\"],\"HiddenCasters\":[\"Artificer\"],\"HiddenSourcebooks\":[\"AI\",\"EGW\"],\"HiddenCastingTimeTypes\":[\"bonus action\"],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"minute\",\"MaxUnit\":\"day\",\"MinText\":\"10\",\"MaxText\":\"6\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"0\",\"MaxText\":\"1\"},\"CastingTimeFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"hour\",\"MinText\":\"0\",\"MaxText\":\"24\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,false,true],\"NotComponentsFilters\":[true,true,true],\"MinSpellLevel\":1,\"MaxSpellLevel\":7,\"ApplyFiltersToSpellLists\":true,\"ApplyFiltersToSearch\":true,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.11.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("wwét");

            final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();
            final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();
            Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.DURATION);
            Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.RANGE);
            Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isTrue();
            Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(1);
            Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(7);

            final Collection<Source> shouldBeHiddenSources = new ArrayList<>(Arrays.asList(Source.ACQUISITIONS_INC, Source.EXPLORERS_GTW));
            shouldBeHiddenSources.addAll(Spellbook.sourcesAddedAfterVersion(Spellbook.V_2_11_0));
            final Collection<Source> shouldBeVisibleSources = SpellbookUtils.complement(shouldBeHiddenSources, Source.values());
            Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleSources(false)).containsExactlyElementsIn(shouldBeHiddenSources);

            final Collection<School> shouldBeHiddenSchools = Arrays.asList(School.ABJURATION, School.CONJURATION);
            final Collection<School> shouldBeVisibleSchools = SpellbookUtils.complement(shouldBeHiddenSchools, School.values());
            Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(shouldBeVisibleSchools);
            Truth.assertThat(sortFilterStatus.getVisibleSchools(false)).containsExactlyElementsIn(shouldBeHiddenSchools);

            final Collection<CasterClass> shouldBeHiddenClasses = Collections.singletonList(CasterClass.ARTIFICER);
            final Collection<CasterClass> shouldBeVisibleClasses = SpellbookUtils.complement(shouldBeHiddenClasses, CasterClass.values());
            Truth.assertThat(sortFilterStatus.getVisibleClasses(true)).containsExactlyElementsIn(shouldBeVisibleClasses);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(false)).containsExactlyElementsIn(shouldBeHiddenClasses);

            Truth.assertThat(sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(24);

            Truth.assertThat(sortFilterStatus.getMinUnit(Range.RangeType.class)).isEqualTo(LengthUnit.FOOT);
            Truth.assertThat(sortFilterStatus.getMinValue(Range.RangeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Range.RangeType.class)).isEqualTo(LengthUnit.MILE);
            Truth.assertThat(sortFilterStatus.getMaxValue(Range.RangeType.class)).isEqualTo(1);

            Truth.assertThat(sortFilterStatus.getMinUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.MINUTE);
            Truth.assertThat(sortFilterStatus.getMinValue(Duration.DurationType.class)).isEqualTo(10);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.DAY);
            Truth.assertThat(sortFilterStatus.getMaxValue(Duration.DurationType.class)).isEqualTo(6);

            Truth.assertThat(sortFilterStatus.getVerbalFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(true)).isFalse();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();

            final Collection<CastingTime.CastingTimeType> shouldBeHiddenCTTs = Collections.singletonList(CastingTime.CastingTimeType.BONUS_ACTION);
            final Collection<CastingTime.CastingTimeType> shouldBeVisibleCTTs = SpellbookUtils.complement(shouldBeHiddenCTTs, CastingTime.CastingTimeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(true)).containsExactlyElementsIn(shouldBeVisibleCTTs);
            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(false)).containsExactlyElementsIn(shouldBeHiddenCTTs);

            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(true)).containsExactlyElementsIn(Duration.DurationType.values());
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(false)).isEmpty();

            final Collection<Range.RangeType> shouldBeHiddenRTs = Arrays.asList(Range.RangeType.SPECIAL, Range.RangeType.TOUCH, Range.RangeType.UNLIMITED);
            final Collection<Range.RangeType> shouldBeVisibleRTs = SpellbookUtils.complement(shouldBeHiddenRTs, Range.RangeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(true)).containsExactlyElementsIn(shouldBeVisibleRTs);
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(false)).containsExactlyElementsIn(shouldBeHiddenRTs);

            Truth.assertThat(sortFilterStatus.getApplyFiltersToSearch()).isTrue();
            Truth.assertThat(sortFilterStatus.getApplyFiltersToLists()).isTrue();
            Truth.assertThat(sortFilterStatus.getUseTashasExpandedLists()).isFalse();

            Truth.assertThat(sortFilterStatus.getConcentrationFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getConcentrationFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(false)).isTrue();

            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(new Integer[]{1, 2, 3, 103, 197, 221, 418});
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(new Integer[]{8, 9, 10, 93, 221, 260, 419});
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(new Integer[]{87, 221, 318, 451});

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_10_n1() {
        final String jsonString = "{\"CharacterName\":\"2B\",\"Spells\":[{\"SpellID\":178,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":434,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":197,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":199,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":254,\"Favorite\":false,\"Prepared\":true,\"Known\":false}],\"SortField1\":\"Duration\",\"SortField2\":\"Casting Time\",\"Reverse1\":false,\"Reverse2\":true,\"HiddenCastingTimeTypes\":[\"time\"],\"HiddenSourcebooks\":[],\"HiddenRangeTypes\":[],\"HiddenSchools\":[\"Abjuration\",\"Divination\"],\"HiddenDurationTypes\":[],\"HiddenCasters\":[\"Artificer\",\"Bard\"],\"QuantityRanges\":{\"CastingTimeFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"hour\",\"MinText\":\"0\",\"MaxText\":\"24\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"0\",\"MaxText\":\"1\"},\"DurationFilters\":{\"MinUnit\":\"minute\",\"MaxUnit\":\"hour\",\"MinText\":\"2\",\"MaxText\":\"3\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,true,true],\"MinSpellLevel\":1,\"MaxSpellLevel\":8,\"ApplyFiltersToSpellLists\":false,\"ApplyFiltersToSearch\":false,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.10.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();
            final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();
            Truth.assertThat(cp.getName()).isEqualTo("2B");
            Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.DURATION);
            Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.CASTING_TIME);
            Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isTrue();
            Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(1);
            Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(8);

            final Collection<Source> shouldBeHiddenSources = Spellbook.sourcesAddedAfterVersion(Spellbook.V_2_10_0);
            final Collection<Source> shouldBeVisibleSources = SpellbookUtils.complement(shouldBeHiddenSources, Source.values());
            Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(new School[]{School.CONJURATION, School.ENCHANTMENT, School.EVOCATION, School.ILLUSION, School.NECROMANCY, School.TRANSMUTATION});
            Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(true)).containsExactlyElementsIn(Arrays.copyOfRange(CasterClass.values(), 2, CasterClass.values().length));
            Truth.assertThat(sortFilterStatus.getVisibleSchools( false)).containsExactlyElementsIn(new School[]{School.ABJURATION, School.DIVINATION});
            Truth.assertThat(sortFilterStatus.getVisibleSources( false)).containsExactlyElementsIn(shouldBeHiddenSources);
            Truth.assertThat(sortFilterStatus.getVisibleClasses( false)).containsExactlyElementsIn(new CasterClass[]{CasterClass.ARTIFICER, CasterClass.BARD});

            Truth.assertThat(sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(24);

            Truth.assertThat(sortFilterStatus.getMinUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.MINUTE);
            Truth.assertThat(sortFilterStatus.getMinValue(Duration.DurationType.class)).isEqualTo(2);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(sortFilterStatus.getMaxValue(Duration.DurationType.class)).isEqualTo(3);

            Truth.assertThat(sortFilterStatus.getMinUnit(Range.RangeType.class)).isEqualTo(LengthUnit.FOOT);
            Truth.assertThat(sortFilterStatus.getMinValue(Range.RangeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Range.RangeType.class)).isEqualTo(LengthUnit.MILE);
            Truth.assertThat(sortFilterStatus.getMaxValue(Range.RangeType.class)).isEqualTo(1);

            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(true)).containsExactlyElementsIn(new CastingTime.CastingTimeType[]{CastingTime.CastingTimeType.ACTION, CastingTime.CastingTimeType.BONUS_ACTION, CastingTime.CastingTimeType.REACTION});
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(true)).containsExactlyElementsIn(Duration.DurationType.values());
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(true)).containsExactlyElementsIn(Range.RangeType.values());

            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(false)).containsExactlyElementsIn(new CastingTime.CastingTimeType[]{CastingTime.CastingTimeType.TIME});
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes( false)).hasSize(0);
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(false)).hasSize(0);

            Truth.assertThat(sortFilterStatus.getApplyFiltersToSearch()).isFalse();
            Truth.assertThat(sortFilterStatus.getApplyFiltersToLists()).isFalse();
            Truth.assertThat(sortFilterStatus.getUseTashasExpandedLists()).isFalse();

            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).isEqualTo(Arrays.asList(178, 434));
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).isEqualTo(Arrays.asList(178, 434, 197, 199, 254));
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).isEqualTo(Arrays.asList(178, 434));

            Truth.assertThat(sortFilterStatus.getConcentrationFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getConcentrationFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(false)).isTrue();

            Truth.assertThat(sortFilterStatus.getVerbalFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_10_n2() {
        final String jsonString = "{\"CharacterName\":\"A1\",\"Spells\":[],\"SortField1\":\"Name\",\"SortField2\":\"Name\",\"Reverse1\":false,\"Reverse2\":false,\"HiddenCastingTimeTypes\":[],\"HiddenSourcebooks\":[\"XGE\",\"SCAG\",\"TCE\"],\"HiddenRangeTypes\":[],\"HiddenSchools\":[],\"HiddenDurationTypes\":[],\"HiddenCasters\":[],\"QuantityRanges\":{\"CastingTimeFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"hour\",\"MinText\":\"0\",\"MaxText\":\"24\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"0\",\"MaxText\":\"1\"},\"DurationFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"day\",\"MinText\":\"0\",\"MaxText\":\"30\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,true,true],\"MinSpellLevel\":0,\"MaxSpellLevel\":9,\"ApplyFiltersToSpellLists\":false,\"ApplyFiltersToSearch\":false,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.10.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("A1");
            checkIsDefaultProfileFor(cp, Spellbook.V_2_10_0);
        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_9_2_n1() {
        final String jsonString = "{\"CharacterName\":\"Test\",\"Spells\":[{\"SpellName\":\"Meld into Stone\",\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellName\":\"Feign Death\",\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellName\":\"Leomund's Tiny Hut\",\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellName\":\"Drawmij's Instant Summons\",\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellName\":\"Commune with Nature\",\"Favorite\":true,\"Prepared\":false,\"Known\":false}],\"SortField1\":\"Range\",\"SortField2\":\"Level\",\"Reverse1\":true,\"Reverse2\":false,\"HiddenSourcebooks\":[\"Xanathar's Guide to Everything\"],\"HiddenDurationTypes\":[\"Special\"],\"HiddenCastingTimeTypes\":[],\"HiddenCasters\":[\"Cleric\"],\"HiddenSchools\":[\"Abjuration\"],\"HiddenRangeTypes\":[\"Finite range\"],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"minutes\",\"MaxUnit\":\"years\",\"MinText\":\"10\",\"MaxText\":\"22\"},\"CastingTimeFilters\":{\"MinUnit\":\"seconds\",\"MaxUnit\":\"minutes\",\"MinText\":\"0\",\"MaxText\":\"4\"},\"RangeFilters\":{\"MinUnit\":\"feet\",\"MaxUnit\":\"miles\",\"MinText\":\"0\",\"MaxText\":\"1\"}},\"StatusFilter\":\"Favorites\",\"Ritual\":true,\"NotRitual\":false,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[false,true,true],\"MinSpellLevel\":3,\"MaxSpellLevel\":6,\"VersionCode\":\"2.9.2\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();

            Truth.assertThat(cp.getName()).isEqualTo("Test");
            Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.FAVORITES);
            Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.RANGE);
            Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.LEVEL);
            Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isTrue();
            Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(3);
            Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(6);

            final Version version = new Version(2, 9, 2);
            Collection<School> visibleSchools = SpellbookUtils.mutableCollectionFromArray(School.values());
            visibleSchools.remove(School.ABJURATION);
            Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(visibleSchools);
            final Collection<Source> shouldNotBeVisibleSources = SpellbookUtils.mutableCollectionFromArray(new Source[]{Source.XANATHARS_GTE});
            final Collection<Source> sourcesAfter = Spellbook.sourcesAddedAfterVersion(version);
            shouldNotBeVisibleSources.addAll(sourcesAfter);
            final Collection<Source> shouldBeVisibleSources = SpellbookUtils.mutableCollectionFromArray(Source.values());
            shouldBeVisibleSources.removeAll(shouldNotBeVisibleSources);
            final Collection<CasterClass> shouldNotBeVisibleClasses = Arrays.asList(CasterClass.ARTIFICER, CasterClass.CLERIC);
            final Collection<CasterClass> shouldBeVisibleClasses = SpellbookUtils.mutableCollectionFromArray(CasterClass.values());
            shouldBeVisibleClasses.removeAll(shouldNotBeVisibleClasses);
            final Collection<School> shouldNotBeVisibleSchools = Arrays.asList(School.ABJURATION);
            final Collection<School> shouldBeVisibleSchools = SpellbookUtils.mutableCollectionFromArray(School.values());
            shouldBeVisibleSchools.removeAll(shouldNotBeVisibleSchools);

            Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(shouldBeVisibleSchools);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(true)).containsExactlyElementsIn(shouldBeVisibleClasses);
            Truth.assertThat(sortFilterStatus.getVisibleSchools(false)).containsExactlyElementsIn(shouldNotBeVisibleSchools);
            Truth.assertThat(sortFilterStatus.getVisibleSources(false)).containsExactlyElementsIn(shouldNotBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(false)).containsExactlyElementsIn(shouldNotBeVisibleClasses);

            Truth.assertThat(sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.MINUTE);
            Truth.assertThat(sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(4);

            Truth.assertThat(sortFilterStatus.getMinUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.MINUTE);
            Truth.assertThat(sortFilterStatus.getMinValue(Duration.DurationType.class)).isEqualTo(10);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.YEAR);
            Truth.assertThat(sortFilterStatus.getMaxValue(Duration.DurationType.class)).isEqualTo(22);

            Truth.assertThat(sortFilterStatus.getMinUnit(Range.RangeType.class)).isEqualTo(LengthUnit.FOOT);
            Truth.assertThat(sortFilterStatus.getMinValue(Range.RangeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Range.RangeType.class)).isEqualTo(LengthUnit.MILE);
            Truth.assertThat(sortFilterStatus.getMaxValue(Range.RangeType.class)).isEqualTo(1);

            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(true)).containsExactlyElementsIn(CastingTime.CastingTimeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(true)).containsExactlyElementsIn(new Duration.DurationType[]{Duration.DurationType.INSTANTANEOUS, Duration.DurationType.SPANNING, Duration.DurationType.UNTIL_DISPELLED});
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(true)).containsExactlyElementsIn(new Range.RangeType[]{Range.RangeType.SPECIAL, Range.RangeType.SELF, Range.RangeType.TOUCH, Range.RangeType.SIGHT, Range.RangeType.UNLIMITED});

            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(false)).hasSize(0);
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(false)).containsExactlyElementsIn(new Duration.DurationType[]{Duration.DurationType.SPECIAL});
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(false)).containsExactlyElementsIn(new Range.RangeType[]{Range.RangeType.RANGED});

            Truth.assertThat(sortFilterStatus.getApplyFiltersToSearch()).isFalse();
            Truth.assertThat(sortFilterStatus.getApplyFiltersToLists()).isFalse();
            Truth.assertThat(sortFilterStatus.getUseTashasExpandedLists()).isFalse();

            // Need to figure out a way to mock this
            // Since it relies on MainActivity.englishSpells
            //Truth.assertThat(sortFilterStatus.favoriteSpellIDs()).hasSize(2);
            //Truth.assertThat(sortFilterStatus.preparedSpellIDs()).hasSize(2);
            //Truth.assertThat(sortFilterStatus.knownSpellIDs()).hasSize(1);

            Truth.assertThat(sortFilterStatus.getConcentrationFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getConcentrationFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(false)).isFalse();

            Truth.assertThat(sortFilterStatus.getVerbalFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isFalse();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_9_2_n2() {
        final String jsonString = "{\"CharacterName\":\"Test2\",\"Spells\":[{\"SpellName\":\"Alter Self\",\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellName\":\"Alarm\",\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellName\":\"Animal Friendship\",\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellName\":\"Animal Messenger\",\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellName\":\"Animal Shapes\",\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellName\":\"Aid\",\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellName\":\"Acid Splash\",\"Favorite\":true,\"Prepared\":true,\"Known\":true}],\"SortField1\":\"Name\",\"SortField2\":\"Name\",\"Reverse1\":false,\"Reverse2\":false,\"HiddenDurationTypes\":[],\"HiddenSourcebooks\":[\"Xanathar's Guide to Everything\",\"Sword Coast Adv. Guide\"],\"HiddenRangeTypes\":[],\"HiddenCasters\":[],\"HiddenSchools\":[],\"HiddenCastingTimeTypes\":[],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"seconds\",\"MaxUnit\":\"days\",\"MinText\":\"0\",\"MaxText\":\"30\"},\"RangeFilters\":{\"MinUnit\":\"feet\",\"MaxUnit\":\"miles\",\"MinText\":\"0\",\"MaxText\":\"1\"},\"CastingTimeFilters\":{\"MinUnit\":\"seconds\",\"MaxUnit\":\"hours\",\"MinText\":\"0\",\"MaxText\":\"24\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,true,true],\"MinSpellLevel\":0,\"MaxSpellLevel\":9,\"VersionCode\":\"2.9.2\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("Test2");
            final Version version = new Version(2,9,2);
            checkIsDefaultProfileFor(cp, version);
        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
