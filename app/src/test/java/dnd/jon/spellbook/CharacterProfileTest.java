package dnd.jon.spellbook;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ResourcesMode;

import com.google.common.truth.Truth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
                UUID.fromString("e10da93f-b173-44b6-a7f7-b73a82d06745"),
                UUID.fromString("d400f535-1c14-4358-bc17-714b2bc5d336"),
                UUID.fromString("ab24f0db-4e0b-4c89-95e5-c56c96d97d3a")
            };
            final UUID[] preparedIDs = favoriteIDs;
            final UUID[] knownIDs = new UUID[]{
                UUID.fromString("d400f535-1c14-4358-bc17-714b2bc5d336"),
                UUID.fromString("ab24f0db-4e0b-4c89-95e5-c56c96d97d3a"),
                UUID.fromString("3368ff16-01d5-4bba-8d27-68a3123b5fc5")
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
                    UUID.fromString("e10da93f-b173-44b6-a7f7-b73a82d06745"),
                    UUID.fromString("d400f535-1c14-4358-bc17-714b2bc5d336"),
                    UUID.fromString("ab24f0db-4e0b-4c89-95e5-c56c96d97d3a")
            };
            final UUID[] preparedIDs = favoriteIDs;
            final UUID[] knownIDs = new UUID[]{
                    UUID.fromString("d400f535-1c14-4358-bc17-714b2bc5d336"),
                    UUID.fromString("ab24f0db-4e0b-4c89-95e5-c56c96d97d3a"),
                    UUID.fromString("3368ff16-01d5-4bba-8d27-68a3123b5fc5")
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
                UUID.fromString("f7cc5226-40b8-48d8-a7bd-501740a6b34d"),
                UUID.fromString("ad56aa5e-e76d-4029-bab8-cb5061330a79"),
                UUID.fromString("3e41be69-71f5-4bf2-b328-bbd465fbd617")
            };
            final UUID[] preparedIDs = new UUID[]{
                UUID.fromString("ce15d91e-938c-4c9d-ad9a-ab57a9f7bb10"),
                UUID.fromString("a3b949bb-afc7-4fc9-9308-a38c1c5e0c8c"),
                UUID.fromString("293c964a-ff6c-4a60-8afe-814aaf8a413a")
            };
            final UUID[] knownIDs = new UUID[]{
                UUID.fromString("ca1e9ae1-3a66-4953-95ee-22f2f688af20"),
                UUID.fromString("a3b949bb-afc7-4fc9-9308-a38c1c5e0c8c")
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
                UUID.fromString("293c964a-ff6c-4a60-8afe-814aaf8a413a"),
                UUID.fromString("cfba48df-a52a-452c-8d73-d4966add826b"),
                UUID.fromString("5eca8038-77bb-45f7-852a-b595e9bcc73d"),
                UUID.fromString("4d35f438-3f45-4075-aa5c-cdad77eadb87"),
                UUID.fromString("3723d331-0305-4f2a-b4a4-b041d48f16c8"),
                UUID.fromString("4680fadb-422b-4e17-9c23-9da964197c0f")
            };
            final UUID[] preparedIDs = new UUID[]{
                UUID.fromString("cfba48df-a52a-452c-8d73-d4966add826b"),
                UUID.fromString("1cb45110-e1bc-4dbe-b12f-b6e745c2d1c9"),
                UUID.fromString("c2691c2b-04cb-4000-9661-215ee5b52794"),
                UUID.fromString("3723d331-0305-4f2a-b4a4-b041d48f16c8"),
                UUID.fromString("4680fadb-422b-4e17-9c23-9da964197c0f"),
                UUID.fromString("21b62a42-4ad1-4371-b9a3-7bd51961a392"),
                UUID.fromString("83d582e0-9de7-4ecd-bb9a-be6d722093d7")
            };
            final UUID[] knownIDs = new UUID[]{
                UUID.fromString("6e7b22c7-82c5-4f82-85b0-08217d7ac691"),
                UUID.fromString("07602123-5a79-413d-923f-4574a06cd765"),
                UUID.fromString("8a55e15a-85f0-4f70-8f7d-4ec3d638e430"),
                UUID.fromString("c2691c2b-04cb-4000-9661-215ee5b52794"),
                UUID.fromString("4d35f438-3f45-4075-aa5c-cdad77eadb87"),
                UUID.fromString("3723d331-0305-4f2a-b4a4-b041d48f16c8")
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
                UUID.fromString("8a6edaa7-7531-4941-9a65-ccfdc987fdfc"),
                UUID.fromString("0021e3ce-0459-4f36-8022-6eb78ce41116"),
                UUID.fromString("a2425b99-12d6-41ef-bc85-384ca8e0e421"),
                UUID.fromString("381f0937-a08b-4407-88a0-270969483742"),
                UUID.fromString("1cb45110-e1bc-4dbe-b12f-b6e745c2d1c9"),
                UUID.fromString("6c8e8568-3c32-4774-8b75-0e04b057fb0d")
            };
            final UUID[] preparedIDs = new UUID[]{
                UUID.fromString("8a6edaa7-7531-4941-9a65-ccfdc987fdfc"),
                UUID.fromString("0021e3ce-0459-4f36-8022-6eb78ce41116"),
                UUID.fromString("33836586-97b1-49eb-a912-90d45ee8bbfa"),
                UUID.fromString("2bfc62ae-3237-4be3-8e95-a6097b0eac2c"),
                UUID.fromString("c7a309b5-ed0f-4f36-bad6-96312edbc300"),
                UUID.fromString("1cb45110-e1bc-4dbe-b12f-b6e745c2d1c9")
            };
            final UUID[] knownIDs = new UUID[]{
                UUID.fromString("8a6edaa7-7531-4941-9a65-ccfdc987fdfc"),
                UUID.fromString("0021e3ce-0459-4f36-8022-6eb78ce41116"),
                UUID.fromString("477dfca0-23b4-4703-9def-01d1358a34c8"),
                UUID.fromString("a423c918-f300-4096-b5fe-c38deecaa280"),
                UUID.fromString("c7a309b5-ed0f-4f36-bad6-96312edbc300"),
                UUID.fromString("6c8e8568-3c32-4774-8b75-0e04b057fb0d")
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
                UUID.fromString("1bc235cc-66aa-4fdd-bf60-a57ece0a7527"),
                UUID.fromString("73b2e8b3-de2a-4696-9569-ad442e8a90e8"),
                UUID.fromString("d1ef9a13-9429-42fd-9572-54f7bfebcb8f"),
                UUID.fromString("a9d2bb86-0d2f-4bd9-ac3c-1e5ad24c50de")
            };
            final UUID[] preparedIDs = new UUID[]{
                UUID.fromString("1bc235cc-66aa-4fdd-bf60-a57ece0a7527"),
                UUID.fromString("d28a4cd8-b317-401a-bbae-32437a2d672b"),
                UUID.fromString("b360df08-a109-4bd3-8388-e02b225e210c"),
                UUID.fromString("a9d2bb86-0d2f-4bd9-ac3c-1e5ad24c50de")
            };
            final UUID[] knownIDs = new UUID[]{
                UUID.fromString("1bc235cc-66aa-4fdd-bf60-a57ece0a7527"),
                UUID.fromString("0e71811f-cd32-4b71-a950-2191d2567445"),
                UUID.fromString("a9d2bb86-0d2f-4bd9-ac3c-1e5ad24c50de")
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

            final UUID[] favoriteIDs = new UUID[]{
                    UUID.fromString("ce15d91e-938c-4c9d-ad9a-ab57a9f7bb10"),
                    UUID.fromString("ca1e9ae1-3a66-4953-95ee-22f2f688af20"),
                    UUID.fromString("3368ff16-01d5-4bba-8d27-68a3123b5fc5"),
                    UUID.fromString("5c2e3b16-8d5a-456c-b4eb-48e22d2091f7"),
                    UUID.fromString("56a3d647-133f-43ae-8bfc-faa77141a062"),
                    UUID.fromString("b09a044d-69ec-4d79-8630-5f8c42a0f750"),
            };
            final UUID[] preparedIDs = new UUID[]{
                    UUID.fromString("a3b949bb-afc7-4fc9-9308-a38c1c5e0c8c"),
                    UUID.fromString("e10da93f-b173-44b6-a7f7-b73a82d06745"),
                    UUID.fromString("3368ff16-01d5-4bba-8d27-68a3123b5fc5"),
                    UUID.fromString("b09a044d-69ec-4d79-8630-5f8c42a0f750"),
                    UUID.fromString("56a3d647-133f-43ae-8bfc-faa77141a062"),
                    UUID.fromString("940cbf0f-be98-4950-86c4-2ed10039bf78")
            };
            final UUID[] knownIDs = new UUID[]{
                    UUID.fromString("d400f535-1c14-4358-bc17-714b2bc5d336"),
                    UUID.fromString("ab24f0db-4e0b-4c89-95e5-c56c96d97d3a"),
                    UUID.fromString("3368ff16-01d5-4bba-8d27-68a3123b5fc5"),
                    UUID.fromString("b09a044d-69ec-4d79-8630-5f8c42a0f750"),
                    UUID.fromString("940cbf0f-be98-4950-86c4-2ed10039bf78"),
                    UUID.fromString("5c2e3b16-8d5a-456c-b4eb-48e22d2091f7")
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

            final UUID[] favoriteIDs = new UUID[]{
                    UUID.fromString("fe24ca75-de28-437a-82f8-8c36aa25120a"),
                    UUID.fromString("e0ffd650-b1ad-4cb3-9abd-f91e09578761"),
                    UUID.fromString("9e51ec14-73b6-4198-8484-fc4e1c25308c"),
                    UUID.fromString("ee637d2d-b8d0-4065-a5e1-480157c8ab4e"),
                    UUID.fromString("adf1f929-4767-480f-84f0-cf108960f75f"),
            };
            final UUID[] preparedIDs = new UUID[]{
                    UUID.fromString("b08676a6-46b3-480e-971c-658eb7e5632d"),
                    UUID.fromString("e0ffd650-b1ad-4cb3-9abd-f91e09578761"),
                    UUID.fromString("9e51ec14-73b6-4198-8484-fc4e1c25308c"),
                    UUID.fromString("ee637d2d-b8d0-4065-a5e1-480157c8ab4e"),
                    UUID.fromString("f3d85808-b976-430e-80a8-cc6f4b13c470"),
            };

            final UUID[] knownIDs = new UUID[]{
                    UUID.fromString("e0ffd650-b1ad-4cb3-9abd-f91e09578761"),
                    UUID.fromString("cd7449cc-084c-48fc-8a5d-4d7d85bb4899"),
                    UUID.fromString("66ceac26-4619-459a-8f6d-bfb8cf7684e7"),
                    UUID.fromString("adf1f929-4767-480f-84f0-cf108960f75f"),
                    UUID.fromString("f3d85808-b976-430e-80a8-cc6f4b13c470")
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
                    UUID.fromString("ce15d91e-938c-4c9d-ad9a-ab57a9f7bb10"),
                    UUID.fromString("ca1e9ae1-3a66-4953-95ee-22f2f688af20"),
                    UUID.fromString("a3b949bb-afc7-4fc9-9308-a38c1c5e0c8c"),
                    UUID.fromString("070f925f-e249-4591-9f39-3b723ee4fb70"),
                    UUID.fromString("1d53e730-ca55-468b-af82-07d416d212fc"),
                    UUID.fromString("d1ef9a13-9429-42fd-9572-54f7bfebcb8f"),
                    UUID.fromString("3004b3e6-e9b3-4094-9590-5d544c2010db"),
            };
            final UUID[] preparedIDs = new UUID[]{
                   UUID.fromString("b360df08-a109-4bd3-8388-e02b225e210c"),
                   UUID.fromString("b01c3680-7195-4e64-b21f-2a1553e6a40b"),
                   UUID.fromString("b09a044d-69ec-4d79-8630-5f8c42a0f750"),
                    UUID.fromString("56a3d647-133f-43ae-8bfc-faa77141a062"),
                    UUID.fromString("940cbf0f-be98-4950-86c4-2ed10039bf78"),
                    UUID.fromString("b6676a8c-2496-4b49-9d66-2f6c02583014"),
                    UUID.fromString("d1ef9a13-9429-42fd-9572-54f7bfebcb8f")
            };
            final UUID[] knownIDs = new UUID[]{
                    UUID.fromString("84b766ea-eef9-472d-aaaf-c72d090fcf60"),
                    UUID.fromString("17403baa-8532-412e-91cb-db4767546814"),
                    UUID.fromString("d1ef9a13-9429-42fd-9572-54f7bfebcb8f"),
                    UUID.fromString("fc1a98b3-801a-4515-b358-a50663c22557")
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
                UUID.fromString("91a28d18-ddc1-40f1-98e2-759b01df8184"),
                UUID.fromString("7cbdba38-3c74-40fc-badb-793ecdf75df5")
            };
            final UUID[] preparedIDs = new UUID[]{
                UUID.fromString("91a28d18-ddc1-40f1-98e2-759b01df8184"),
                UUID.fromString("7cbdba38-3c74-40fc-badb-793ecdf75df5"),
                UUID.fromString("1d53e730-ca55-468b-af82-07d416d212fc"),
                UUID.fromString("b08676a6-46b3-480e-971c-658eb7e5632d"),
                UUID.fromString("d44708ef-68d8-426b-9e25-46d8a52e7780")
            };
            final UUID[] knownIDs = new UUID[]{
                UUID.fromString("91a28d18-ddc1-40f1-98e2-759b01df8184"),
                UUID.fromString("7cbdba38-3c74-40fc-badb-793ecdf75df5")
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

    @Test
    @Config(sdk = 34)
    public void CorrectParseTest_v4_4_8() {
       final String jsonString = "{ \"CharacterName\": \"TestingCharacter\", \"SpellFilterStatus\": { \"Spells\": [ { \"SpellID\": \"ce15d91e-938c-4c9d-ad9a-ab57a9f7bb10\", \"Favorite\": true, \"Prepared\": false, \"Known\": false }, { \"SpellID\": \"293c964a-ff6c-4a60-8afe-814aaf8a413a\", \"Favorite\": true, \"Prepared\": false, \"Known\": true }, { \"SpellID\": \"ad56aa5e-e76d-4029-bab8-cb5061330a79\", \"Favorite\": true, \"Prepared\": false, \"Known\": false }, { \"SpellID\": \"a3b949bb-afc7-4fc9-9308-a38c1c5e0c8c\", \"Favorite\": false, \"Prepared\": true, \"Known\": true }, { \"SpellID\": \"e10da93f-b173-44b6-a7f7-b73a82d06745\", \"Favorite\": false, \"Prepared\": false, \"Known\": true }, { \"SpellID\": \"d400f535-1c14-4358-bc17-714b2bc5d336\", \"Favorite\": false, \"Prepared\": false, \"Known\": true }, { \"SpellID\": \"ab24f0db-4e0b-4c89-95e5-c56c96d97d3a\", \"Favorite\": true, \"Prepared\": false, \"Known\": false }, { \"SpellID\": \"ca1e9ae1-3a66-4953-95ee-22f2f688af20\", \"Favorite\": false, \"Prepared\": true, \"Known\": false } ] }, \"SortFilterStatus\": { \"StatusFilter\":\"Favorites\", \"SortField1\": \"Level\", \"SortField2\": \"Range\", \"Reverse1\": true, \"Reverse2\": false, \"MinSpellLevel\": 0, \"MaxSpellLevel\": 9, \"ApplyFiltersToSearch\": false, \"ApplyFiltersToSpellLists\": true, \"UseTCEExpandedLists\": false, \"HideDuplicateSpells\": true, \"Prefer2024Spells\": true, \"Ritual\": true, \"NotRitual\": true, \"Concentration\": true, \"NotConcentration\": true, \"ComponentsFilters\": [ false, true, true, false ], \"NotComponentsFilters\": [ true, true, false, true ], \"Sourcebooks\": [ \"Tasha's Cauldron of Everything\", \"Player's Handbook\", \"Xanathar's Guide to Everything\" ], \"Classes\": [ \"Artificer\", \"Bard\", \"Cleric\", \"Druid\", \"Paladin\", \"Ranger\", \"Sorcerer\", \"Warlock\", \"Wizard\" ], \"Schools\": [ \"Abjuration\", \"Conjuration\", \"Divination\", \"Enchantment\", \"Evocation\", \"Illusion\", \"Necromancy\", \"Transmutation\" ], \"CastingTimeTypes\": [ \"bonus action\", \"reaction\", \"time\" ], \"DurationTypes\": [ \"Special\", \"Instantaneous\", \"Finite duration\" ], \"RangeTypes\": [ \"Special\", \"Sight\", \"Finite range\", \"Unlimited\" ], \"CastingTimeBounds\": { \"MinValue\": 0, \"MaxValue\": 24, \"MinUnit\": \"second\", \"MaxUnit\": \"hour\" }, \"DurationBounds\": { \"MinValue\": 0, \"MaxValue\": 30, \"MinUnit\": \"second\", \"MaxUnit\": \"day\" }, \"RangeBounds\": { \"MinValue\": 0, \"MaxValue\": 1, \"MinUnit\": \"foot\", \"MaxUnit\": \"mile\" } }, \"SpellSlotStatus\": { \"totalSlots\": [ 6, 4, 3, 0, 0, 0, 0, 0, 0 ], \"usedSlots\": [ 1, 1, 1, 0, 0, 0, 0, 0, 0 ] }, \"VersionCode\": \"4.4.8\" }";
       try {
           final JSONObject json = new JSONObject(jsonString);
           final CharacterProfile cp = CharacterProfile.fromJSON(json);
           final SortFilterStatus sortFilterStatus = cp.getSortFilterStatus();

           Truth.assertThat(cp.getName()).isEqualTo("TestingCharacter");
           Truth.assertThat(sortFilterStatus.getStatusFilterField()).isEqualTo(StatusFilterField.FAVORITES);
           Truth.assertThat(sortFilterStatus.getFirstSortField()).isEqualTo(SortField.LEVEL);
           Truth.assertThat(sortFilterStatus.getSecondSortField()).isEqualTo(SortField.RANGE);
           Truth.assertThat(sortFilterStatus.getFirstSortReverse()).isTrue();
           Truth.assertThat(sortFilterStatus.getSecondSortReverse()).isFalse();
           Truth.assertThat(sortFilterStatus.getMinSpellLevel()).isEqualTo(0);
           Truth.assertThat(sortFilterStatus.getMaxSpellLevel()).isEqualTo(9);

           final Collection<Source> shouldBeVisibleSources = Arrays.asList(Source.PLAYERS_HANDBOOK, Source.XANATHARS_GTE, Source.TASHAS_COE);
           final Collection<Source> shouldBeHiddenSources = SpellbookUtils.complement(shouldBeVisibleSources, Source.values());
           Truth.assertThat(sortFilterStatus.getVisibleSources(true)).containsExactlyElementsIn(shouldBeVisibleSources);
           Truth.assertThat(sortFilterStatus.getVisibleSources(false)).containsExactlyElementsIn(shouldBeHiddenSources);
           Truth.assertThat(sortFilterStatus.getVisibleSchools(true)).containsExactlyElementsIn(School.values());
           Truth.assertThat(sortFilterStatus.getVisibleSchools(false)).isEmpty();
           Truth.assertThat(sortFilterStatus.getVisibleClasses(true)).containsExactlyElementsIn(CasterClass.values());
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

           Truth.assertThat(sortFilterStatus.getVerbalFilter(true)).isFalse();
           Truth.assertThat(sortFilterStatus.getSomaticFilter(true)).isTrue();
           Truth.assertThat(sortFilterStatus.getMaterialFilter(true)).isTrue();
           Truth.assertThat(sortFilterStatus.getRoyaltyFilter(true)).isFalse();
           Truth.assertThat(sortFilterStatus.getVerbalFilter(false)).isTrue();
           Truth.assertThat(sortFilterStatus.getSomaticFilter(false)).isTrue();
           Truth.assertThat(sortFilterStatus.getMaterialFilter(false)).isFalse();
           Truth.assertThat(sortFilterStatus.getRoyaltyFilter(false)).isTrue();

           final Collection<CastingTime.CastingTimeType> shouldBeVisibleCTTs = Arrays.asList(CastingTime.CastingTimeType.BONUS_ACTION, CastingTime.CastingTimeType.REACTION, CastingTime.CastingTimeType.TIME);
           final Collection<CastingTime.CastingTimeType> shouldBeHiddenCTTs = SpellbookUtils.complement(shouldBeVisibleCTTs, CastingTime.CastingTimeType.values());
           Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(true)).containsExactlyElementsIn(shouldBeVisibleCTTs);
           Truth.assertThat(sortFilterStatus.getVisibleCastingTimeTypes(false)).containsExactlyElementsIn(shouldBeHiddenCTTs);

           final Collection<Duration.DurationType> shouldBeVisibleDTs = Arrays.asList(Duration.DurationType.SPECIAL, Duration.DurationType.INSTANTANEOUS, Duration.DurationType.SPANNING);
           final Collection<Duration.DurationType> shouldBeHiddenDTs = SpellbookUtils.complement(shouldBeVisibleDTs, Duration.DurationType.values());
           Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(true)).containsExactlyElementsIn(shouldBeVisibleDTs);
           Truth.assertThat(sortFilterStatus.getVisibleDurationTypes(false)).containsExactlyElementsIn(shouldBeHiddenDTs);

           final Collection<Range.RangeType> shouldBeVisibleRTs = Arrays.asList(Range.RangeType.SPECIAL, Range.RangeType.SIGHT, Range.RangeType.RANGED, Range.RangeType.UNLIMITED);
           final Collection<Range.RangeType> shouldBeHiddenRTs = SpellbookUtils.complement(shouldBeVisibleRTs, Range.RangeType.values());
           Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(true)).containsExactlyElementsIn(shouldBeVisibleRTs);
           Truth.assertThat(sortFilterStatus.getVisibleRangeTypes(false)).containsExactlyElementsIn(shouldBeHiddenRTs);

           Truth.assertThat(sortFilterStatus.getApplyFiltersToSearch()).isFalse();
           Truth.assertThat(sortFilterStatus.getApplyFiltersToLists()).isTrue();
           Truth.assertThat(sortFilterStatus.getUseTashasExpandedLists()).isFalse();

           final UUID[] favoriteIDs = new UUID[]{
                   UUID.fromString("ce15d91e-938c-4c9d-ad9a-ab57a9f7bb10"),
                   UUID.fromString("293c964a-ff6c-4a60-8afe-814aaf8a413a"),
                   UUID.fromString("ad56aa5e-e76d-4029-bab8-cb5061330a79"),
                   UUID.fromString("ab24f0db-4e0b-4c89-95e5-c56c96d97d3a"),
           };
           final UUID[] preparedIDs = new UUID[]{
                UUID.fromString("a3b949bb-afc7-4fc9-9308-a38c1c5e0c8c"),
                   UUID.fromString("ca1e9ae1-3a66-4953-95ee-22f2f688af20"),
           };
           final UUID[] knownIDs = new UUID[]{
                UUID.fromString("293c964a-ff6c-4a60-8afe-814aaf8a413a"),
                  UUID.fromString("a3b949bb-afc7-4fc9-9308-a38c1c5e0c8c"),
                   UUID.fromString("e10da93f-b173-44b6-a7f7-b73a82d06745"),
                   UUID.fromString("d400f535-1c14-4358-bc17-714b2bc5d336")
           };

           final SpellFilterStatus spellFilterStatus = cp.getSpellFilterStatus();
           Truth.assertThat(spellFilterStatus.favoriteSpellIDs()).containsExactlyElementsIn(favoriteIDs);
           Truth.assertThat(spellFilterStatus.preparedSpellIDs()).containsExactlyElementsIn(preparedIDs);
           Truth.assertThat(spellFilterStatus.knownSpellIDs()).containsExactlyElementsIn(knownIDs);

           final SpellSlotStatus spellSlotStatus = cp.getSpellSlotStatus();
           Truth.assertThat(spellSlotStatus.getTotalSlots(1)).isEqualTo(6);
           Truth.assertThat(spellSlotStatus.getTotalSlots(2)).isEqualTo(4);
           Truth.assertThat(spellSlotStatus.getTotalSlots(3)).isEqualTo(3);
           Truth.assertThat(spellSlotStatus.getUsedSlots(1)).isEqualTo(1);
           Truth.assertThat(spellSlotStatus.getUsedSlots(2)).isEqualTo(1);
           Truth.assertThat(spellSlotStatus.getUsedSlots(3)).isEqualTo(1);
           for (int level = 4; level <= 9; level++) {
               Truth.assertThat(spellSlotStatus.getTotalSlots(level)).isEqualTo(0);
               Truth.assertThat(spellSlotStatus.getUsedSlots(level)).isEqualTo(0);
           }

       }catch (JSONException e) {
           e.printStackTrace();
           Assert.fail();
       }
    }
}
