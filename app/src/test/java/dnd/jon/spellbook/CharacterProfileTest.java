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
import java.util.UUID;

@RunWith(RobolectricTestRunner.class)
public class CharacterProfileTest {

    private static void checkIsDefaultFor(SortFilterStatus sortFilterStatus, Version version) {
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

        final Collection<School> shouldBeVisibleSchools = Arrays.asList(School.values());
        Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(shouldBeVisibleSchools);
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
        Truth.assertThat(sortFilterStatus.getRoyaltyFilter(true)).isTrue();
        Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
        Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
        Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();
        Truth.assertThat(sortFilterStatus.getRoyaltyFilter(false)).isTrue();

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
    }

    private static void checkIsDefaultFor(SpellFilterStatus spellFilterStatus, Version version) {
        Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).isEmpty();
        Truth.assertThat(spellFilterStatus.preparedSpellIDs()).isEmpty();
        Truth.assertThat(spellFilterStatus.knownSpellIDs()).isEmpty();
    }

    private static void checkIsDefaultProfileFor(CharacterProfile cp, Version version) {
        final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();
        checkIsDefaultFor(sortFilterStatus, version);

        final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();
        checkIsDefaultFor(spellFilterStatus, version);
    }

    @Test
    @Config(sdk = 34)
    public void CorrectParseTest_pre_v3_7_n1() {
        final String jsonString = "{\"CharacterName\":\"Test\",\"SpellFilterStatus\":{\"Spells\":[{\"SpellID\":4,\"Favorite\":true,\"Prepared\":true,\"Known\":false},{\"SpellID\":5,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":6,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":7,\"Favorite\":false,\"Prepared\":false,\"Known\":true}]},\"SortFilterStatus\":{\"SortField1\":\"School\",\"SortField2\":\"Name\",\"Reverse1\":false,\"Reverse2\":true,\"MinSpellLevel\":1,\"MaxSpellLevel\":4,\"ApplyFiltersToSearch\":false,\"ApplyFiltersToSpellLists\":false,\"UseTCEExpandedLists\":false,\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true,true],\"NotComponentsFilters\":[true,true,true,true],\"Sourcebooks\":[\"Source\",\"Tasha's Cauldron of Everything\",\"Player's Handbook\",\"Xanathar's Guide to Everything\"],\"Classes\":[\"Artificer\",\"Bard\",\"Cleric\",\"Druid\",\"Paladin\",\"Ranger\",\"Sorcerer\",\"Warlock\",\"Wizard\"],\"Schools\":[\"Abjuration\",\"Conjuration\",\"Divination\",\"Enchantment\",\"Evocation\",\"Illusion\",\"Necromancy\",\"Transmutation\"],\"CastingTimeTypes\":[\"action\",\"reaction\",\"time\"],\"DurationTypes\":[\"Special\",\"Instantaneous\",\"Finite duration\",\"Until dispelled\"],\"RangeTypes\":[\"Special\",\"Self\",\"Sight\",\"Finite range\"],\"CastingTimeBounds\":{\"MinValue\":0,\"MaxValue\":12,\"MinUnit\":\"second\",\"MaxUnit\":\"hour\"},\"DurationBounds\":{\"MinValue\":0,\"MaxValue\":30,\"MinUnit\":\"second\",\"MaxUnit\":\"day\"},\"RangeBounds\":{\"MinValue\":0,\"MaxValue\":1,\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\"}},\"SpellSlotStatus\":{\"totalSlots\":[3,2,1,1,0,0,0,0,0],\"usedSlots\":[1,1,1,0,0,0,0,0,0]},\"VersionCode\":\"3.6.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("Test");

            final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();
            final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();
            final SpellSlotStatus spellSlotStatus = cp.getSpellSlotStatus();
            Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.SCHOOL);
            Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.NAME);
            Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isTrue();
            Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(1);
            Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(4);

            // 'Source' is a used-created source, so it should be null and removed
            final Collection<Source> shouldBeVisibleSources = new ArrayList<>(Arrays.asList(Source.PLAYERS_HANDBOOK, Source.XANATHARS_GTE, Source.TASHAS_COE));
            final Collection<Source> shouldBeHiddenSources = SpellbookUtils.complement(shouldBeVisibleSources, Source.values());
            Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleSources(false)).containsExactlyElementsIn(shouldBeHiddenSources);


            final Collection<School> shouldBeVisibleSchools = Arrays.asList(School.values());
            Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(shouldBeVisibleSchools);
            Truth.assertThat(sortFilterStatus.getVisibleSchools(false)).isEmpty();

            final Collection<CasterClass> shouldBeVisibleClasses = Arrays.asList(CasterClass.values());
            Truth.assertThat(sortFilterStatus.getVisibleClasses(true)).containsExactlyElementsIn(shouldBeVisibleClasses);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(false)).isEmpty();


            Truth.assertThat(sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(12);

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
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(false)).isTrue();

            final Collection<CastingTime.CastingTimeType> shouldBeVisibleCastingTimeTypes = new ArrayList<>(Arrays.asList(CastingTime.CastingTimeType.ACTION, CastingTime.CastingTimeType.REACTION, CastingTime.CastingTimeType.TIME));
            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(true)).containsExactlyElementsIn(shouldBeVisibleCastingTimeTypes);
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(true)).containsExactlyElementsIn(Duration.DurationType.values());
            final Collection<Range.RangeType> shouldBeVisibleRangeTypes = new ArrayList<>(Arrays.asList(Range.RangeType.RANGED, Range.RangeType.SELF, Range.RangeType.SIGHT, Range.RangeType.SPECIAL));
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(true)).containsExactlyElementsIn(shouldBeVisibleRangeTypes);

            Truth.assertThat(sortFilterStatus.getApplyFiltersToSearch()).isFalse();
            Truth.assertThat(sortFilterStatus.getApplyFiltersToLists()).isFalse();
            Truth.assertThat(sortFilterStatus.getUseTashasExpandedLists()).isFalse();

            Truth.assertThat(sortFilterStatus.getConcentrationFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getConcentrationFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(false)).isTrue();

            final UUID[] favoriteIDs = new UUID[]{
                UUID.fromString("9263025c-edfa-4a56-b1c0-b7e66fa959c6"),
                UUID.fromString("b4a05889-eddb-411e-98fa-bb63ffca99e4"),
                UUID.fromString("8e7bb7e8-d3c8-4cdc-8f53-a22b7eb49bb0")
            };
            final UUID[] preparedIDs = favoriteIDs;
            final UUID[] knownIDs = new UUID[]{
                UUID.fromString("b4a05889-eddb-411e-98fa-bb63ffca99e4"),
                UUID.fromString("8e7bb7e8-d3c8-4cdc-8f53-a22b7eb49bb0"),
                UUID.fromString("9fdc0216-34f1-444a-9262-772211155d71")
            };
            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(favoriteIDs);
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(preparedIDs);
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(knownIDs);

            Truth.assertThat(spellSlotStatus.getTotalSlots(1)).isEqualTo(3);
            Truth.assertThat(spellSlotStatus.getTotalSlots(2)).isEqualTo(2);
            Truth.assertThat(spellSlotStatus.getTotalSlots(3)).isEqualTo(1);
            Truth.assertThat(spellSlotStatus.getTotalSlots(4)).isEqualTo(1);

            Truth.assertThat(spellSlotStatus.getUsedSlots(1)).isEqualTo(1);
            Truth.assertThat(spellSlotStatus.getAvailableSlots(1)).isEqualTo(2);
            Truth.assertThat(spellSlotStatus.getUsedSlots(2)).isEqualTo(1);
            Truth.assertThat(spellSlotStatus.getAvailableSlots(2)).isEqualTo(1);
            Truth.assertThat(spellSlotStatus.getUsedSlots(3)).isEqualTo(1);
            Truth.assertThat(spellSlotStatus.getAvailableSlots(3)).isEqualTo(0);
            Truth.assertThat(spellSlotStatus.getUsedSlots(4)).isEqualTo(0);
            Truth.assertThat(spellSlotStatus.getAvailableSlots(4)).isEqualTo(1);
            for (int level = 5; level < Spellbook.MAX_SPELL_LEVEL; level++) {
                Truth.assertThat(spellSlotStatus.getTotalSlots(level)).isEqualTo(0);
                Truth.assertThat(spellSlotStatus.getUsedSlots(level)).isEqualTo(0);
                Truth.assertThat(spellSlotStatus.getAvailableSlots(level)).isEqualTo(0);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 34)
    public void CorrectParseTest_pre_v3_7_n2() {
        final String jsonString = "{\"CharacterName\":\"Test\",\"SpellFilterStatus\":{\"Spells\":[{\"SpellID\":4,\"Favorite\":true,\"Prepared\":true,\"Known\":false},{\"SpellID\":5,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":6,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":7,\"Favorite\":false,\"Prepared\":false,\"Known\":true}]},\"SortFilterStatus\":{\"SortField1\":\"School\",\"SortField2\":\"Name\",\"Reverse1\":false,\"Reverse2\":true,\"MinSpellLevel\":1,\"MaxSpellLevel\":4,\"ApplyFiltersToSearch\":false,\"ApplyFiltersToSpellLists\":false,\"UseTCEExpandedLists\":false,\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true,true],\"NotComponentsFilters\":[true,true,true,true],\"Sourcebooks\":[\"Source\",\"Tasha's Cauldron of Everything\",\"Player's Handbook\",\"Xanathar's Guide to Everything\"],\"Classes\":[\"Artificer\",\"Bard\",\"Cleric\",\"Druid\",\"Paladin\",\"Ranger\",\"Sorcerer\",\"Warlock\",\"Wizard\"],\"Schools\":[\"Abjuration\",\"Conjuration\",\"Divination\",\"Enchantment\",\"Evocation\",\"Illusion\",\"Necromancy\",\"Transmutation\"],\"CastingTimeTypes\":[\"action\",\"reaction\",\"time\"],\"DurationTypes\":[\"Special\",\"Instantaneous\",\"Finite duration\",\"Until dispelled\"],\"RangeTypes\":[\"Special\",\"Self\",\"Sight\",\"Finite range\"],\"CastingTimeBounds\":{\"MinValue\":0,\"MaxValue\":12,\"MinUnit\":\"second\",\"MaxUnit\":\"hour\"},\"DurationBounds\":{\"MinValue\":0,\"MaxValue\":30,\"MinUnit\":\"second\",\"MaxUnit\":\"day\"},\"RangeBounds\":{\"MinValue\":0,\"MaxValue\":1,\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\"}},\"SpellSlotStatus\":{\"totalSlots\":[3,2,1,1,0,0,0,0,0],\"usedSlots\":[1,1,1,0,0,0,0,0,0]},\"VersionCode\":\"3.6.0\"}";
        // Create the source that's referenced in the profile
        final Source source = Source.create("Source", "SRC");
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("Test");

            final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();
            final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();
            final SpellSlotStatus spellSlotStatus = cp.getSpellSlotStatus();
            Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.SCHOOL);
            Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.NAME);
            Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isTrue();
            Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(1);
            Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(4);

            final Collection<Source> shouldBeVisibleSources = new ArrayList<>(Arrays.asList(Source.PLAYERS_HANDBOOK, Source.XANATHARS_GTE, Source.TASHAS_COE, source));
            final Collection<Source> shouldBeHiddenSources = SpellbookUtils.complement(shouldBeVisibleSources, Source.values());
            Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleSources(false)).containsExactlyElementsIn(shouldBeHiddenSources);


            final Collection<School> shouldBeVisibleSchools = Arrays.asList(School.values());
            Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(shouldBeVisibleSchools);
            Truth.assertThat(sortFilterStatus.getVisibleSchools(false)).isEmpty();

            final Collection<CasterClass> shouldBeVisibleClasses = Arrays.asList(CasterClass.values());
            Truth.assertThat(sortFilterStatus.getVisibleClasses(true)).containsExactlyElementsIn(shouldBeVisibleClasses);
            Truth.assertThat(sortFilterStatus.getVisibleClasses(false)).isEmpty();


            Truth.assertThat(sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.SECOND);
            Truth.assertThat(sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class)).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class)).isEqualTo(TimeUnit.HOUR);
            Truth.assertThat(sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class)).isEqualTo(12);

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
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(false)).isTrue();

            final Collection<CastingTime.CastingTimeType> shouldBeVisibleCastingTimeTypes = new ArrayList<>(Arrays.asList(CastingTime.CastingTimeType.ACTION, CastingTime.CastingTimeType.REACTION, CastingTime.CastingTimeType.TIME));
            Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(true)).containsExactlyElementsIn(shouldBeVisibleCastingTimeTypes);
            Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(true)).containsExactlyElementsIn(Duration.DurationType.values());
            final Collection<Range.RangeType> shouldBeVisibleRangeTypes = new ArrayList<>(Arrays.asList(Range.RangeType.RANGED, Range.RangeType.SELF, Range.RangeType.SIGHT, Range.RangeType.SPECIAL));
            Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(true)).containsExactlyElementsIn(shouldBeVisibleRangeTypes);

            Truth.assertThat(sortFilterStatus.getApplyFiltersToSearch()).isFalse();
            Truth.assertThat(sortFilterStatus.getApplyFiltersToLists()).isFalse();
            Truth.assertThat(sortFilterStatus.getUseTashasExpandedLists()).isFalse();

            Truth.assertThat(sortFilterStatus.getConcentrationFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getConcentrationFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(false)).isTrue();

            final UUID[] favoriteIDs = new UUID[]{
                    UUID.fromString("9263025c-edfa-4a56-b1c0-b7e66fa959c6"),
                    UUID.fromString("b4a05889-eddb-411e-98fa-bb63ffca99e4"),
                    UUID.fromString("8e7bb7e8-d3c8-4cdc-8f53-a22b7eb49bb0")
            };
            final UUID[] preparedIDs = favoriteIDs;
            final UUID[] knownIDs = new UUID[]{
                    UUID.fromString("b4a05889-eddb-411e-98fa-bb63ffca99e4"),
                    UUID.fromString("8e7bb7e8-d3c8-4cdc-8f53-a22b7eb49bb0"),
                    UUID.fromString("9fdc0216-34f1-444a-9262-772211155d71")
            };
            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(favoriteIDs);
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(preparedIDs);
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(knownIDs);

            Truth.assertThat(spellSlotStatus.getTotalSlots(1)).isEqualTo(3);
            Truth.assertThat(spellSlotStatus.getTotalSlots(2)).isEqualTo(2);
            Truth.assertThat(spellSlotStatus.getTotalSlots(3)).isEqualTo(1);
            Truth.assertThat(spellSlotStatus.getTotalSlots(4)).isEqualTo(1);

            Truth.assertThat(spellSlotStatus.getUsedSlots(1)).isEqualTo(1);
            Truth.assertThat(spellSlotStatus.getAvailableSlots(1)).isEqualTo(2);
            Truth.assertThat(spellSlotStatus.getUsedSlots(2)).isEqualTo(1);
            Truth.assertThat(spellSlotStatus.getAvailableSlots(2)).isEqualTo(1);
            Truth.assertThat(spellSlotStatus.getUsedSlots(3)).isEqualTo(1);
            Truth.assertThat(spellSlotStatus.getAvailableSlots(3)).isEqualTo(0);
            Truth.assertThat(spellSlotStatus.getUsedSlots(4)).isEqualTo(0);
            Truth.assertThat(spellSlotStatus.getAvailableSlots(4)).isEqualTo(1);
            for (int level = 5; level < Spellbook.MAX_SPELL_LEVEL; level++) {
                Truth.assertThat(spellSlotStatus.getTotalSlots(level)).isEqualTo(0);
                Truth.assertThat(spellSlotStatus.getUsedSlots(level)).isEqualTo(0);
                Truth.assertThat(spellSlotStatus.getAvailableSlots(level)).isEqualTo(0);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            source.delete();
        }
    }

    @Test
    @Config(sdk = 34)
    public void CorrectParseTest_v3_0_0_n1() {
        final String jsonString = "{\"CharacterName\":\"t3\",\"SpellFilterStatus\":{\"Spells\":[{\"SpellID\":1,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":2,\"Favorite\":false,\"Prepared\":false,\"Known\":true},{\"SpellID\":3,\"Favorite\":false,\"Prepared\":true,\"Known\":true},{\"SpellID\":362,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":363,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":364,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":492,\"Favorite\":true,\"Prepared\":false,\"Known\":false}]},\"SortFilterStatus\":{\"SortField1\":\"Name\",\"SortField2\":\"Name\",\"Reverse1\":false,\"Reverse2\":false,\"MinSpellLevel\":0,\"MaxSpellLevel\":9,\"ApplyFiltersToSearch\":false,\"ApplyFiltersToSpellLists\":false,\"UseTCEExpandedLists\":false,\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,true,true],\"Sourcebooks\":[\"Player's Handbook\",\"Rime of the Frostmaiden\"],\"Classes\":[\"Artificer\",\"Bard\",\"Cleric\",\"Druid\",\"Paladin\",\"Ranger\",\"Sorcerer\",\"Warlock\",\"Wizard\"],\"Schools\":[\"Abjuration\",\"Conjuration\",\"Divination\",\"Enchantment\",\"Evocation\",\"Illusion\",\"Necromancy\",\"Transmutation\"],\"CastingTimeTypes\":[\"action\",\"bonus action\",\"reaction\",\"time\"],\"DurationTypes\":[\"Special\",\"Instantaneous\",\"Finite duration\",\"Until dispelled\"],\"RangeTypes\":[\"Special\",\"Self\",\"Touch\",\"Sight\",\"Finite range\",\"Unlimited\"],\"CastingTimeBounds\":{\"MinValue\":0,\"MaxValue\":24,\"MinUnit\":\"second\",\"MaxUnit\":\"hour\"},\"DurationBounds\":{\"MinValue\":0,\"MaxValue\":30,\"MinUnit\":\"second\",\"MaxUnit\":\"day\"},\"RangeBounds\":{\"MinValue\":0,\"MaxValue\":1,\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\"}},\"SpellSlotStatus\":{\"totalSlots\":[0,0,0,0,0,0,0,0,0],\"usedSlots\":[0,0,0,0,0,0,0,0,0]},\"VersionCode\":\"3.0.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("t3");

            final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();
            final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();
            Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.NAME);
            Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.NAME);
            Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isFalse();
            Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(0);
            Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(9);

            final Version version = new Version(3, 0, 0);
            final Collection<Source> shouldBeVisibleSources = new ArrayList<>(Arrays.asList(Source.PLAYERS_HANDBOOK, Source.RIME_FROSTMAIDEN));
            final Collection<Source> shouldBeHiddenSources = SpellbookUtils.complement(shouldBeVisibleSources, Source.values());
            Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
            Truth.assertThat(sortFilterStatus.getVisibleSources(false)).containsExactlyElementsIn(shouldBeHiddenSources);

            final Collection<School> shouldBeVisibleSchools = Arrays.asList(School.values());
            Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(shouldBeVisibleSchools);
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
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(false)).isTrue();

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

            final UUID[] favoriteIDs = new UUID[]{
                UUID.fromString("82562a70-ba7c-48ed-acfb-dfcacd105cd8"),
                UUID.fromString("298ee924-658c-4b77-9404-d3ac064de9d2"),
                UUID.fromString("b3a35618-503c-4ef9-ac0c-ecb181665aee")
            };
            final UUID[] preparedIDs = new UUID[]{
                UUID.fromString("b00aed01-c695-4d17-981d-a37684a8628e"),
                UUID.fromString("85ae9373-8da6-4c69-8eea-b2d24dc20790"),
                UUID.fromString("21e630a1-67b7-4719-89c5-599b1b5a1888")
            };
            final UUID[] knownIDs = new UUID[]{
                UUID.fromString("b38d70d1-484f-46a5-b2c4-1d379c8fc096"),
                UUID.fromString("85ae9373-8da6-4c69-8eea-b2d24dc20790")
            };
            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(favoriteIDs);
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(preparedIDs);
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(knownIDs);

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 34)
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
    @Config(sdk = 34)
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
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isFalse();
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(false)).isTrue();

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

            final UUID[] favoriteIDs = new UUID[]{
                UUID.fromString("21e630a1-67b7-4719-89c5-599b1b5a1888"),
                UUID.fromString("bb6e3746-6381-4d8c-9274-a811842b364b"),
                UUID.fromString("bc675def-9a34-4205-866b-5a9f12c3d2b8"),
                UUID.fromString("0c2b0931-f8be-462a-85e0-27df6a420d86"),
                UUID.fromString("a8a013e2-4013-451b-a76c-618e1f52437b"),
                UUID.fromString("fa55bba4-e35f-4081-85eb-5fbc96bf198e")
            };
            final UUID[] preparedIDs = new UUID[]{
                UUID.fromString("bb6e3746-6381-4d8c-9274-a811842b364b"),
                UUID.fromString("a4e5f13f-328e-4c6f-9291-86d05eb5a034"),
                UUID.fromString("a95e4f86-35fe-4977-a627-b5e4e4abee2b"),
                UUID.fromString("a8a013e2-4013-451b-a76c-618e1f52437b"),
                UUID.fromString("fa55bba4-e35f-4081-85eb-5fbc96bf198e"),
                UUID.fromString("e08845e7-7cc3-4ea3-b854-24eb87f45fd6"),
                UUID.fromString("4d8cea8f-8cf0-4058-8a02-5c1a59060d51")
            };
            final UUID[] knownIDs = new UUID[]{
                UUID.fromString("22fa37f1-765c-4ef0-a83d-34de9b343dca"),
                UUID.fromString("395674b2-321a-4919-a6a0-2ee5a09e28c0"),
                UUID.fromString("d0e09382-09dc-40bb-ad27-66664804eaca"),
                UUID.fromString("a95e4f86-35fe-4977-a627-b5e4e4abee2b"),
                UUID.fromString("0c2b0931-f8be-462a-85e0-27df6a420d86"),
                UUID.fromString("a8a013e2-4013-451b-a76c-618e1f52437b")
            };
            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(favoriteIDs);
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(preparedIDs);
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(knownIDs);

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 34)
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
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isFalse();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(false)).isTrue();

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

            final UUID[] favoriteIDs = new UUID[]{
                UUID.fromString("2d42af75-1274-4218-be2c-e626ada9073a"),
                UUID.fromString("fffa022f-fe4b-4c59-ac9c-abf09bb984d3"),
                UUID.fromString("092518a9-77bd-4d97-a19e-d5866f5f2ba6"),
                UUID.fromString("1f75ece0-0f79-483c-93c8-78314468a60b"),
                UUID.fromString("a4e5f13f-328e-4c6f-9291-86d05eb5a034"),
                UUID.fromString("94209708-eca2-46fe-9054-e51e4e17b4e1")
            };
            final UUID[] preparedIDs = new UUID[]{
                UUID.fromString("2d42af75-1274-4218-be2c-e626ada9073a"),
                UUID.fromString("fffa022f-fe4b-4c59-ac9c-abf09bb984d3"),
                UUID.fromString("de28e922-ccb2-4549-a135-e2cef670a0ef"),
                UUID.fromString("5b1577ce-f22d-498a-9522-7c895e8fac9e"),
                UUID.fromString("e7e8b4f8-e28a-4b97-95fc-a5370487e56a"),
                UUID.fromString("a4e5f13f-328e-4c6f-9291-86d05eb5a034")
            };
            final UUID[] knownIDs = new UUID[]{
                UUID.fromString("2d42af75-1274-4218-be2c-e626ada9073a"),
                UUID.fromString("fffa022f-fe4b-4c59-ac9c-abf09bb984d3"),
                UUID.fromString("aff9c8c9-bb8b-4717-a5be-e905dd498678"),
                UUID.fromString("8d397549-34cc-4e51-9d04-60f8d8484c74"),
                UUID.fromString("e7e8b4f8-e28a-4b97-95fc-a5370487e56a"),
                UUID.fromString("94209708-eca2-46fe-9054-e51e4e17b4e1")
            };
            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(favoriteIDs);
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(preparedIDs);
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(knownIDs);

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 34)
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
    @Config(sdk = 34)
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
    @Config(sdk = 34)
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
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(false)).isTrue();

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

            final UUID[] favoriteIDs = new UUID[]{
                UUID.fromString("0f69432e-df07-40a2-a488-7041aba44d0c"),
                UUID.fromString("2519dcff-3cb1-442d-8a2a-89d9d0e03a89"),
                UUID.fromString("ff424832-c47a-48d5-9c37-71737da6fc08"),
                UUID.fromString("9c66cf21-6354-464c-a485-86a06304eadf")
            };
            final UUID[] preparedIDs = new UUID[]{
                UUID.fromString("0f69432e-df07-40a2-a488-7041aba44d0c"),
                UUID.fromString("ea2fb7a3-b62f-46db-846b-ee6d164ce71c"),
                UUID.fromString("a3664bab-72f0-412f-8b4b-a3ae6322065f"),
                UUID.fromString("9c66cf21-6354-464c-a485-86a06304eadf")
            };
            final UUID[] knownIDs = new UUID[]{
                UUID.fromString("0f69432e-df07-40a2-a488-7041aba44d0c"),
                UUID.fromString("845bec18-49dc-4d4c-8b79-c4548a8349ff"),
                UUID.fromString("9c66cf21-6354-464c-a485-86a06304eadf")
            };
            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(favoriteIDs);
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(preparedIDs);
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(knownIDs);

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 34)
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
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(false)).isTrue();

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
    @Config(sdk = 34)
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
    @Config(sdk = 34)
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
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isFalse();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(false)).isTrue();

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
    @Config(sdk = 34)
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
    @Config(sdk = 34)
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
    @Config(sdk = 34)
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
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(false)).isTrue();

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

            final UUID[] favoriteIDs = new UUID[]{
                    UUID.fromString("b00aed01-c695-4d17-981d-a37684a8628e"),
                    UUID.fromString("b38d70d1-484f-46a5-b2c4-1d379c8fc096"),
                    UUID.fromString("85ae9373-8da6-4c69-8eea-b2d24dc20790"),
                    UUID.fromString("1339fcd0-6179-42e7-9f87-c17fb233c19f"),
                    UUID.fromString("b8532b9f-168b-40da-be01-96e4db546393")
            };

            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(new Integer[]{1, 2, 3, 103, 197, 221, 418});
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(new Integer[]{8, 9, 10, 93, 221, 260, 419});
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(new Integer[]{87, 221, 318, 451});

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 34)
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

            final UUID[] favoriteIDs = new UUID[]{
                UUID.fromString("bd86a97e-f13a-4102-b78d-f0de294a9915"),
                UUID.fromString("f425ac68-fa21-40b2-ae0c-f6077ed1763b")
            };
            final UUID[] preparedIDs = new UUID[]{
                UUID.fromString("bd86a97e-f13a-4102-b78d-f0de294a9915"),
                UUID.fromString("f425ac68-fa21-40b2-ae0c-f6077ed1763b"),
                UUID.fromString("b8532b9f-168b-40da-be01-96e4db546393"),
                UUID.fromString("c2f36f98-4056-4fa0-b7ac-77766d668bf2"),
                UUID.fromString("6ee69c65-5204-452c-9403-7fc0d089b2e3")
            };
            final UUID[] knownIDs = new UUID[]{
                UUID.fromString("bd86a97e-f13a-4102-b78d-f0de294a9915"),
                UUID.fromString("f425ac68-fa21-40b2-ae0c-f6077ed1763b")
            };

            Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(favoriteIDs);
            Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(preparedIDs);
            Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(knownIDs);

            Truth.assertThat(sortFilterStatus.getConcentrationFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getConcentrationFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getRitualFilter(false)).isTrue();

            Truth.assertThat(sortFilterStatus.getVerbalFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(false)).isTrue();


        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    @Config(sdk = 34)
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
    @Config(sdk = 34)
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
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(true)).isTrue();
            Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isFalse();
            Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isTrue();
            Truth.assertThat(sortFilterStatus.getRoyaltyFilter(false)).isTrue();

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 34)
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
