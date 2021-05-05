package dnd.jon.spellbook;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.google.common.truth.Truth;

import java.lang.reflect.Array;
import java.util.Arrays;

@RunWith(RobolectricTestRunner.class)
public class CharacterProfileTest {

    @Test
    @Config(sdk = 28)
    public void CorrectParseTest_v2_10_n1() {
        final String jsonString = "{\"CharacterName\":\"2B\",\"Spells\":[{\"SpellID\":178,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":434,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":197,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":199,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":254,\"Favorite\":false,\"Prepared\":true,\"Known\":false}],\"SortField1\":\"Duration\",\"SortField2\":\"Casting Time\",\"Reverse1\":false,\"Reverse2\":true,\"HiddenCastingTimeTypes\":[\"time\"],\"HiddenSourcebooks\":[],\"HiddenRangeTypes\":[],\"HiddenSchools\":[\"Abjuration\",\"Divination\"],\"HiddenDurationTypes\":[],\"HiddenCasters\":[\"Artificer\",\"Bard\"],\"QuantityRanges\":{\"CastingTimeFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"hour\",\"MinText\":\"0\",\"MaxText\":\"24\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"0\",\"MaxText\":\"1\"},\"DurationFilters\":{\"MinUnit\":\"minute\",\"MaxUnit\":\"hour\",\"MinText\":\"2\",\"MaxText\":\"3\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,true,true],\"MinSpellLevel\":1,\"MaxSpellLevel\":8,\"ApplyFiltersToSpellLists\":false,\"ApplyFiltersToSearch\":false,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.10.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("2B");
            Truth.assertThat(cp.getStatusFilter()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(cp.getFirstSortField()).isEqualTo(SortField.DURATION);
            Truth.assertThat(cp.getSecondSortField()).isEqualTo(SortField.CASTING_TIME);
            Truth.assertThat(cp.getFirstSortReverse()).isFalse();
            Truth.assertThat(cp.getSecondSortReverse()).isTrue();
            Truth.assertThat(cp.getMinSpellLevel()).isEqualTo(1);
            Truth.assertThat(cp.getMaxSpellLevel()).isEqualTo(8);

            Truth.assertThat(cp.getVisibleValues(School.class)).isEqualTo(new School[]{School.CONJURATION, School.ENCHANTMENT, School.EVOCATION, School.ILLUSION, School.NECROMANCY, School.TRANSMUTATION});
            Truth.assertThat(cp.getVisibleValues(Sourcebook.class)).isEqualTo(new Sourcebook[]{Sourcebook.PLAYERS_HANDBOOK, Sourcebook.XANATHARS_GTE, Sourcebook.SWORD_COAST_AG, Sourcebook.TASHAS_COE});
            Truth.assertThat(cp.getVisibleValues(CasterClass.class)).isEqualTo(Arrays.copyOfRange(CasterClass.values(), 2, CasterClass.values().length));
            Truth.assertThat(cp.getVisibleValues(School.class, false)).isEqualTo(new School[]{School.ABJURATION, School.DIVINATION});
            Truth.assertThat(cp.getVisibleValues(Sourcebook.class, false)).hasLength(4);
            Truth.assertThat(cp.getVisibleValues(CasterClass.class, false)).isEqualTo(new CasterClass[]{CasterClass.ARTIFICER, CasterClass.BARD});

            Truth.assertThat(cp.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(cp.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(0);
            Truth.assertThat(cp.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(cp.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(24);

            Truth.assertThat(cp.getMinUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.MINUTE);
            Truth.assertThat(cp.getMinValue(Duration.DurationType.class)).isEqualTo(2);
            Truth.assertThat(cp.getMaxUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(cp.getMaxValue(Duration.DurationType.class)).isEqualTo(3);

            Truth.assertThat(cp.getMinUnit(Range.RangeType.class)).isEqualTo(LengthUnit.FOOT);
            Truth.assertThat(cp.getMinValue(Range.RangeType.class)).isEqualTo(0);
            Truth.assertThat(cp.getMaxUnit(Range.RangeType.class)).isEqualTo(LengthUnit.MILE);
            Truth.assertThat(cp.getMaxValue(Range.RangeType.class)).isEqualTo(1);

            Truth.assertThat(cp.getVisibleValues(CastingTime.CastingTimeType.class)).isEqualTo(new CastingTime.CastingTimeType[]{CastingTime.CastingTimeType.ACTION, CastingTime.CastingTimeType.BONUS_ACTION, CastingTime.CastingTimeType.REACTION});
            Truth.assertThat(cp.getVisibleValues(Duration.DurationType.class)).isEqualTo(Duration.DurationType.values());
            Truth.assertThat(cp.getVisibleValues(Range.RangeType.class)).isEqualTo(Range.RangeType.values());

            Truth.assertThat(cp.getVisibleValues(CastingTime.CastingTimeType.class, false)).isEqualTo(new CastingTime.CastingTimeType[]{CastingTime.CastingTimeType.TIME});
            Truth.assertThat(cp.getVisibleValues(Duration.DurationType.class, false)).hasLength(0);
            Truth.assertThat(cp.getVisibleValues(Range.RangeType.class, false)).hasLength(0);

            Truth.assertThat(cp.getApplyFiltersToSearch()).isFalse();
            Truth.assertThat(cp.getApplyFiltersToSpellLists()).isFalse();
            Truth.assertThat(cp.getUseTCEExpandedLists()).isFalse();

            Truth.assertThat(cp.favoriteSpellIDs()).isEqualTo(Arrays.asList(178, 434));
            Truth.assertThat(cp.preparedSpellIDs()).isEqualTo(Arrays.asList(178, 434, 197, 199, 254));
            Truth.assertThat(cp.knownSpellIDs()).isEqualTo(Arrays.asList(178, 434));

            Truth.assertThat(cp.getConcentrationFilter(true)).isTrue();
            Truth.assertThat(cp.getConcentrationFilter(false)).isTrue();
            Truth.assertThat(cp.getRitualFilter(true)).isTrue();
            Truth.assertThat(cp.getRitualFilter(false)).isTrue();

            Truth.assertThat(cp.getVerbalComponentFilter(true)).isTrue();
            Truth.assertThat(cp.getSomaticComponentFilter(true)).isTrue();
            Truth.assertThat(cp.getMaterialComponentFilter(true)).isTrue();
            Truth.assertThat(cp.getVerbalComponentFilter(false)).isTrue();
            Truth.assertThat(cp.getSomaticComponentFilter(false)).isTrue();
            Truth.assertThat(cp.getMaterialComponentFilter(false)).isTrue();

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
            Truth.assertThat(cp.getStatusFilter()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(cp.getFirstSortField()).isEqualTo(SortField.NAME);
            Truth.assertThat(cp.getSecondSortField()).isEqualTo(SortField.NAME);
            Truth.assertThat(cp.getFirstSortReverse()).isFalse();
            Truth.assertThat(cp.getSecondSortReverse()).isFalse();
            Truth.assertThat(cp.getMinSpellLevel()).isEqualTo(0);
            Truth.assertThat(cp.getMaxSpellLevel()).isEqualTo(9);

            Truth.assertThat(cp.getVisibleValues(School.class)).isEqualTo(School.values());
            Truth.assertThat(cp.getVisibleValues(Sourcebook.class)).isEqualTo(new Sourcebook[]{Sourcebook.PLAYERS_HANDBOOK});
            Truth.assertThat(cp.getVisibleValues(CasterClass.class)).isEqualTo(CasterClass.values());
            Truth.assertThat(cp.getVisibleValues(School.class, false)).hasLength(0);
            Truth.assertThat(cp.getVisibleValues(Sourcebook.class, false)).hasLength(7);
            Truth.assertThat(cp.getVisibleValues(CasterClass.class, false)).hasLength(0);

            Truth.assertThat(cp.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(cp.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(0);
            Truth.assertThat(cp.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(cp.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(24);

            Truth.assertThat(cp.getMinUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(cp.getMinValue(Duration.DurationType.class)).isEqualTo(0);
            Truth.assertThat(cp.getMaxUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.DAY);
            Truth.assertThat(cp.getMaxValue(Duration.DurationType.class)).isEqualTo(30);

            Truth.assertThat(cp.getMinUnit(Range.RangeType.class)).isEqualTo(LengthUnit.FOOT);
            Truth.assertThat(cp.getMinValue(Range.RangeType.class)).isEqualTo(0);
            Truth.assertThat(cp.getMaxUnit(Range.RangeType.class)).isEqualTo(LengthUnit.MILE);
            Truth.assertThat(cp.getMaxValue(Range.RangeType.class)).isEqualTo(1);

            Truth.assertThat(cp.getVisibleValues(CastingTime.CastingTimeType.class)).isEqualTo(CastingTime.CastingTimeType.values());
            Truth.assertThat(cp.getVisibleValues(Duration.DurationType.class)).isEqualTo(Duration.DurationType.values());
            Truth.assertThat(cp.getVisibleValues(Range.RangeType.class)).isEqualTo(Range.RangeType.values());

            Truth.assertThat(cp.getVisibleValues(CastingTime.CastingTimeType.class, false)).hasLength(0);
            Truth.assertThat(cp.getVisibleValues(Duration.DurationType.class, false)).hasLength(0);
            Truth.assertThat(cp.getVisibleValues(Range.RangeType.class, false)).hasLength(0);

            Truth.assertThat(cp.getApplyFiltersToSearch()).isFalse();
            Truth.assertThat(cp.getApplyFiltersToSpellLists()).isFalse();
            Truth.assertThat(cp.getUseTCEExpandedLists()).isFalse();

            Truth.assertThat(cp.favoriteSpellIDs()).hasSize(0);
            Truth.assertThat(cp.preparedSpellIDs()).hasSize(0);
            Truth.assertThat(cp.knownSpellIDs()).hasSize(0);

            Truth.assertThat(cp.getConcentrationFilter(true)).isTrue();
            Truth.assertThat(cp.getConcentrationFilter(false)).isTrue();
            Truth.assertThat(cp.getRitualFilter(true)).isTrue();
            Truth.assertThat(cp.getRitualFilter(false)).isTrue();

            Truth.assertThat(cp.getVerbalComponentFilter(true)).isTrue();
            Truth.assertThat(cp.getSomaticComponentFilter(true)).isTrue();
            Truth.assertThat(cp.getMaterialComponentFilter(true)).isTrue();
            Truth.assertThat(cp.getVerbalComponentFilter(false)).isTrue();
            Truth.assertThat(cp.getSomaticComponentFilter(false)).isTrue();
            Truth.assertThat(cp.getMaterialComponentFilter(false)).isTrue();

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

            Truth.assertThat(cp.getName()).isEqualTo("Test");
            Truth.assertThat(cp.getStatusFilter()).isEqualTo(StatusFilterField.FAVORITES);
            Truth.assertThat(cp.getFirstSortField()).isEqualTo(SortField.RANGE);
            Truth.assertThat(cp.getSecondSortField()).isEqualTo(SortField.LEVEL);
            Truth.assertThat(cp.getFirstSortReverse()).isTrue();
            Truth.assertThat(cp.getSecondSortReverse()).isFalse();
            Truth.assertThat(cp.getMinSpellLevel()).isEqualTo(3);
            Truth.assertThat(cp.getMaxSpellLevel()).isEqualTo(6);

            School[] visibleSchools = School.values();
            visibleSchools = Arrays.copyOfRange(visibleSchools, 1, visibleSchools.length);
            Truth.assertThat(cp.getVisibleValues(School.class)).isEqualTo(visibleSchools);
            Truth.assertThat(cp.getVisibleValues(Sourcebook.class)).isEqualTo(new Sourcebook[]{Sourcebook.PLAYERS_HANDBOOK, Sourcebook.SWORD_COAST_AG});
            CasterClass[] visibleClasses = new CasterClass[]{CasterClass.BARD, CasterClass.DRUID, CasterClass.PALADIN, CasterClass.RANGER, CasterClass.SORCERER, CasterClass.WARLOCK, CasterClass.WIZARD};
            Truth.assertThat(cp.getVisibleValues(CasterClass.class)).isEqualTo(visibleClasses);
            Truth.assertThat(cp.getVisibleValues(School.class, false)).isEqualTo(new School[]{School.ABJURATION});
            Truth.assertThat(cp.getVisibleValues(Sourcebook.class, false)).hasLength(6);
            Truth.assertThat(cp.getVisibleValues(CasterClass.class, false)).isEqualTo(new CasterClass[]{CasterClass.ARTIFICER, CasterClass.CLERIC});

            Truth.assertThat(cp.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(cp.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(0);
            Truth.assertThat(cp.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.MINUTE);
            Truth.assertThat(cp.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(4);

            Truth.assertThat(cp.getMinUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.MINUTE);
            Truth.assertThat(cp.getMinValue(Duration.DurationType.class)).isEqualTo(10);
            Truth.assertThat(cp.getMaxUnit(Duration.DurationType.class)).isEqualTo(TimeUnit.YEAR);
            Truth.assertThat(cp.getMaxValue(Duration.DurationType.class)).isEqualTo(22);

            Truth.assertThat(cp.getMinUnit(Range.RangeType.class)).isEqualTo(LengthUnit.FOOT);
            Truth.assertThat(cp.getMinValue(Range.RangeType.class)).isEqualTo(0);
            Truth.assertThat(cp.getMaxUnit(Range.RangeType.class)).isEqualTo(LengthUnit.MILE);
            Truth.assertThat(cp.getMaxValue(Range.RangeType.class)).isEqualTo(1);

            Truth.assertThat(cp.getVisibleValues(CastingTime.CastingTimeType.class)).isEqualTo(CastingTime.CastingTimeType.values());
            Truth.assertThat(cp.getVisibleValues(Duration.DurationType.class)).isEqualTo(new Duration.DurationType[]{Duration.DurationType.INSTANTANEOUS, Duration.DurationType.SPANNING, Duration.DurationType.UNTIL_DISPELLED});
            Truth.assertThat(cp.getVisibleValues(Range.RangeType.class)).isEqualTo(new Range.RangeType[]{Range.RangeType.SPECIAL, Range.RangeType.SELF, Range.RangeType.TOUCH, Range.RangeType.SIGHT, Range.RangeType.UNLIMITED});

            Truth.assertThat(cp.getVisibleValues(CastingTime.CastingTimeType.class, false)).hasLength(0);
            Truth.assertThat(cp.getVisibleValues(Duration.DurationType.class, false)).isEqualTo(new Duration.DurationType[]{Duration.DurationType.SPECIAL});
            Truth.assertThat(cp.getVisibleValues(Range.RangeType.class, false)).isEqualTo(new Range.RangeType[]{Range.RangeType.RANGED});

            Truth.assertThat(cp.getApplyFiltersToSearch()).isFalse();
            Truth.assertThat(cp.getApplyFiltersToSpellLists()).isFalse();
            Truth.assertThat(cp.getUseTCEExpandedLists()).isFalse();

            // Need to figure out a way to mock this
            // Since it relies on MainActivity.englishSpells
            //Truth.assertThat(cp.favoriteSpellIDs()).hasSize(2);
            //Truth.assertThat(cp.preparedSpellIDs()).hasSize(2);
            //Truth.assertThat(cp.knownSpellIDs()).hasSize(1);

            Truth.assertThat(cp.getConcentrationFilter(true)).isTrue();
            Truth.assertThat(cp.getConcentrationFilter(false)).isTrue();
            Truth.assertThat(cp.getRitualFilter(true)).isTrue();
            Truth.assertThat(cp.getRitualFilter(false)).isFalse();

            Truth.assertThat(cp.getVerbalComponentFilter(true)).isTrue();
            Truth.assertThat(cp.getSomaticComponentFilter(true)).isTrue();
            Truth.assertThat(cp.getMaterialComponentFilter(true)).isTrue();
            Truth.assertThat(cp.getVerbalComponentFilter(false)).isFalse();
            Truth.assertThat(cp.getSomaticComponentFilter(false)).isTrue();
            Truth.assertThat(cp.getMaterialComponentFilter(false)).isTrue();

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

            Truth.assertThat(cp.getName()).isEqualTo("Test");
            Truth.assertThat(cp.getStatusFilter()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(cp.getFirstSortField()).isEqualTo(SortField.NAME);
            Truth.assertThat(cp.getSecondSortField()).isEqualTo(SortField.NAME);
            Truth.assertThat(cp.getFirstSortReverse()).isFalse();
            Truth.assertThat(cp.getSecondSortReverse()).isFalse();
            Truth.assertThat(cp.getMinSpellLevel()).isEqualTo(0);
            Truth.assertThat(cp.getMaxSpellLevel()).isEqualTo(9);

            Truth.assertThat(cp.getVisibleValues(School.class)).isEqualTo(School.values());
            Truth.assertThat(cp.getVisibleValues(Sourcebook.class)).isEqualTo(new Sourcebook[]{Sourcebook.PLAYERS_HANDBOOK, Sourcebook.SWORD_COAST_AG});
            CasterClass[] visibleClasses = new CasterClass[]{CasterClass.BARD, CasterClass.DRUID, CasterClass.PALADIN, CasterClass.RANGER, CasterClass.SORCERER, CasterClass.WARLOCK, CasterClass.WIZARD};
            Truth.assertThat(cp.getVisibleValues(CasterClass.class)).isEqualTo(visibleClasses);
            Truth.assertThat(cp.getVisibleValues(School.class, false)).isEqualTo(new School[]{School.ABJURATION});
            Truth.assertThat(cp.getVisibleValues(Sourcebook.class, false)).hasLength(6);
            Truth.assertThat(cp.getVisibleValues(CasterClass.class, false)).isEqualTo(new CasterClass[]{CasterClass.ARTIFICER, CasterClass.CLERIC});
        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }

}
