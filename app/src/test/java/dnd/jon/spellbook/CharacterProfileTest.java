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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
public class CharacterProfileTest {

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
            final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();
            final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();

            Truth.assertThat(cp.getName()).isEqualTo("A1");
            Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.NAME);
            Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.NAME);
            Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(9);

            final Version version = Spellbook.V_2_10_0;
            final Collection<Source> shouldNotBeVisibleSources = SpellbookUtils.mutableCollectionFromArray(new Source[]{Source.XANATHARS_GTE, Source.SWORD_COAST_AG, Source.TASHAS_COE});
            shouldNotBeVisibleSources.addAll(Spellbook.sourcesAddedAfterVersion(version));
            final Collection<Source> shouldBeVisibleSources = SpellbookUtils.complement(shouldNotBeVisibleSources, Source.values());

            Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(true)).containsExactlyElementsIn(CasterClass.values());
            Truth.assertThat(sortFilterStatus.getVisibleSchools(false)).hasSize(0);
            Truth.assertThat(sortFilterStatus.getVisibleSources(false)).containsExactlyElementsIn(shouldNotBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(false)).hasSize(0);

            Truth.assertThat(sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(24);

            Truth.assertThat(sortFilterStatus.getMinUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(sortFilterStatus.getMinValue(Duration.DurationType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.DAY);
            Truth.assertThat(sortFilterStatus.getMaxValue(Duration.DurationType.class)).isEqualTo(30);

            Truth.assertThat(sortFilterStatus.getMinUnit(Range.RangeType.class)).isEqualTo(LengthUnit.FOOT);
            Truth.assertThat(sortFilterStatus.getMinValue(Range.RangeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Range.RangeType.class)).isEqualTo(LengthUnit.MILE);
            Truth.assertThat(sortFilterStatus.getMaxValue(Range.RangeType.class)).isEqualTo(1);

            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(true)).containsExactlyElementsIn(CastingTime.CastingTimeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(true)).containsExactlyElementsIn(Duration.DurationType.values());
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(true)).containsExactlyElementsIn(Range.RangeType.values());

            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(false)).hasSize(0);
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(false)).hasSize(0);
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(false)).hasSize(0);

            Truth.assertThat(sortFilterStatus.getApplyFiltersToSearch()).isFalse();
            Truth.assertThat(sortFilterStatus.getApplyFiltersToLists()).isFalse();
            Truth.assertThat(sortFilterStatus.getUseTashasExpandedLists()).isFalse();

            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).hasSize(0);
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).hasSize(0);
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).hasSize(0);

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
    public void CorrectParseTest_v2_9_2_n1() {
        final String jsonString = "{\"CharacterName\":\"Test\",\"Spells\":[{\"SpellName\":\"Meld into Stone\",\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellName\":\"Feign Death\",\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellName\":\"Leomund's Tiny Hut\",\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellName\":\"Drawmij's Instant Summons\",\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellName\":\"Commune with Nature\",\"Favorite\":true,\"Prepared\":false,\"Known\":false}],\"SortField1\":\"Range\",\"SortField2\":\"Level\",\"Reverse1\":true,\"Reverse2\":false,\"HiddenSourcebooks\":[\"Xanathar's Guide to Everything\"],\"HiddenDurationTypes\":[\"Special\"],\"HiddenCastingTimeTypes\":[],\"HiddenCasters\":[\"Cleric\"],\"HiddenSchools\":[\"Abjuration\"],\"HiddenRangeTypes\":[\"Finite range\"],\"QuantityRanges\":{\"DurationFilters\":{\"MinUnit\":\"minutes\",\"MaxUnit\":\"years\",\"MinText\":\"10\",\"MaxText\":\"22\"},\"CastingTimeFilters\":{\"MinUnit\":\"seconds\",\"MaxUnit\":\"minutes\",\"MinText\":\"0\",\"MaxText\":\"4\"},\"RangeFilters\":{\"MinUnit\":\"feet\",\"MaxUnit\":\"miles\",\"MinText\":\"0\",\"MaxText\":\"1\"}},\"StatusFilter\":\"Favorites\",\"Ritual\":true,\"NotRitual\":false,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[false,true,true],\"MinSpellLevel\":3,\"MaxSpellLevel\":6,\"VersionCode\":\"2.9.2\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();
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
            final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();

            final Version version = new Version(2,9,2);
            final Collection<Source> shouldBeNotBeVisibleSources = SpellbookUtils.mutableCollectionFromArray(new Source[]{Source.XANATHARS_GTE, Source.SWORD_COAST_AG});
            shouldBeNotBeVisibleSources.addAll(Spellbook.sourcesAddedAfterVersion(version));
            final Collection<Source> shouldBeVisibleSources = SpellbookUtils.complement(shouldBeNotBeVisibleSources, Source.values());

            Truth.assertThat(cp.getName()).isEqualTo("Test2");
            Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.NAME);
            Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.NAME);
            Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(9);

            Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(School.values());
            Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(true)).containsExactlyElementsIn(CasterClass.values());
            Truth.assertThat(sortFilterStatus.getVisibleSchools(false)).hasSize(0);
            Truth.assertThat(sortFilterStatus.getVisibleSources(false)).containsExactlyElementsIn(shouldBeNotBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(false)).hasSize(0);

            Truth.assertThat(sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(24);

            Truth.assertThat(sortFilterStatus.getMinUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(sortFilterStatus.getMinValue(Duration.DurationType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.DAY);
            Truth.assertThat(sortFilterStatus.getMaxValue(Duration.DurationType.class)).isEqualTo(30);

            Truth.assertThat(sortFilterStatus.getMinUnit(Range.RangeType.class)).isEqualTo(LengthUnit.FOOT);
            Truth.assertThat(sortFilterStatus.getMinValue(Range.RangeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(Range.RangeType.class)).isEqualTo(LengthUnit.MILE);
            Truth.assertThat(sortFilterStatus.getMaxValue(Range.RangeType.class)).isEqualTo(1);

            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(true)).containsExactlyElementsIn(CastingTime.CastingTimeType.values());
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(true)).containsExactlyElementsIn(Duration.DurationType.values());
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(true)).containsExactlyElementsIn(Range.RangeType.values());

            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(false)).hasSize(0);
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(false)).hasSize(0);
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(false)).hasSize(0);

            Truth.assertThat(sortFilterStatus.getApplyFiltersToSearch()).isFalse();
            Truth.assertThat(sortFilterStatus.getApplyFiltersToLists()).isFalse();
            Truth.assertThat(sortFilterStatus.getUseTashasExpandedLists()).isFalse();

            // Need to figure out a way to mock this
            // Since it relies on MainActivity.englishSpells
            //Truth.assertThat(sortFilterStatus.favoriteSpellIDs()).hasSize(0);
            //Truth.assertThat(sortFilterStatus.preparedSpellIDs()).hasSize(0);
            //Truth.assertThat(sortFilterStatus.knownSpellIDs()).hasSize(0);

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
}
