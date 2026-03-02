package dnd.jon.spellbook;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ViewModelProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentTest {
    @Before
    public void setup() {
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final File filesDir = context.getFilesDir();
        SpellbookUtils.deleteDirectory(filesDir);
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("dnd.jon.spellbook", appContext.getPackageName());
    }

    @Test
    public void testViewModelLoadSource() {
        final String jsonString = "{\"name\":\"Test Source\",\"code\":\"TST\",\"spells\":[{\"id\":100000,\"name\":\"Test Spell\",\"desc\":\"abc\",\"higher_level\":\"def\",\"range\":\"3 feet\",\"material\":\"\",\"royalty\":\"\",\"ritual\":false,\"duration\":\"2 seconds\",\"concentration\":true,\"casting_time\":\"1 action\",\"level\":2,\"school\":\"Abjuration\",\"locations\":[{\"sourcebook\":\"TST\",\"page\":-1}],\"components\":[\"V\",\"S\"],\"classes\":[\"Artificer\",\"Paladin\"],\"subclasses\":[],\"tce_expanded_classes\":[],\"ruleset\":\"created\"}]}";
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                try {
                    final SpellbookViewModel viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);
                    final JSONObject json = new JSONObject(jsonString);
                    viewModel.addSourceFromJSON(json);

                    final Source[] createdSources = Source.createdSources();
                    assertEquals(1, createdSources.length);
                    final Source source = createdSources[0];
                    assertEquals("TST", source.getCode());
                    assertEquals("Test Source", source.getDisplayName());

                    final List<Spell> createdSpells = viewModel.getCreatedSpells();
                    assertEquals(1, createdSpells.size());
                    final Spell spell = createdSpells.get(0);
                    assertEquals("Test Spell", spell.getName());
                    assertEquals("abc", spell.getDescription());
                    assertEquals("def", spell.getHigherLevel());
                    final UUID spellID = spell.getID();
                    assertEquals(spellID, Spellbook.uuidForID(100000));
                    assertTrue(spell.getMaterial().isEmpty());
                    assertTrue(spell.getRoyalty().isEmpty());
                    assertFalse(spell.getRitual());
                    assertTrue(spell.getConcentration());
                    assertEquals(2, spell.getLevel());
                    assertEquals(School.ABJURATION, spell.getSchool());
                    final Map<Source, Integer> locations = spell.getLocations();
                    assertEquals(1, locations.size());
                    final Integer page = locations.get(source);
                    assertNotNull(page);
                    assertEquals(page.intValue(), -1);

                    final Range range = spell.getRange();
                    assertEquals(Range.RangeType.RANGED, range.type);
                    assertEquals(3, range.value, 0);
                    assertEquals(LengthUnit.FOOT, range.unit);

                    final CastingTime castingTime = spell.getCastingTime();
                    assertEquals(CastingTime.CastingTimeType.ACTION, castingTime.type);
                    assertEquals(1, castingTime.value, 0);
                    assertEquals(TimeUnit.SECOND, castingTime.unit);

                    final Duration duration = spell.getDuration();
                    assertEquals(Duration.DurationType.SPANNING, duration.type);
                    assertEquals(2, duration.value, 0);
                    assertEquals(TimeUnit.SECOND, duration.unit);

                    assertArrayEquals(new boolean[]{true, true, false, false}, spell.getComponents());
                    assertArrayEquals(new CasterClass[]{CasterClass.ARTIFICER, CasterClass.PALADIN}, spell.getClasses().toArray());
                    assertTrue(spell.getSubclasses().isEmpty());
                    assertTrue(spell.getTashasExpandedClasses().isEmpty());
                    assertEquals(Ruleset.RULES_CREATED, spell.getRuleset());

                    assertEquals(1, viewModel.getCreatedSpellsForSource(source).size());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Assert.fail();
                }
            });
        }
    }

    @Test
    public void testViewModelLoadContent() {
        final String jsonString = "{\"profiles\":[{\"CharacterName\":\"Test\",\"SpellFilterStatus\":{\"Spells\":[{\"SpellID\":10,\"Favorite\":true,\"Prepared\":false,\"Known\":false},{\"SpellID\":19,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":106,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":198,\"Favorite\":false,\"Prepared\":false,\"Known\":true}]},\"SortFilterStatus\":{\"SortField1\":\"Duration\",\"SortField2\":\"Name\",\"Reverse1\":false,\"Reverse2\":true,\"MinSpellLevel\":0,\"MaxSpellLevel\":9,\"ApplyFiltersToSearch\":false,\"ApplyFiltersToSpellLists\":false,\"UseTCEExpandedLists\":false,\"HideDuplicateSpells\":true,\"Prefer2024Spells\":true,\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":false,\"ComponentsFilters\":[true,false,true,true],\"NotComponentsFilters\":[true,true,true,true],\"Sourcebooks\":[\"Tasha's Cauldron of Everything\",\"Xanathar's Guide to Everything\",\"Player's Handbook\"],\"Classes\":[\"Artificer\",\"Bard\",\"Cleric\",\"Druid\",\"Paladin\",\"Ranger\",\"Sorcerer\",\"Warlock\",\"Wizard\"],\"Schools\":[\"Abjuration\",\"Conjuration\",\"Divination\",\"Enchantment\",\"Evocation\",\"Illusion\",\"Necromancy\",\"Transmutation\"],\"CastingTimeTypes\":[\"bonus action\",\"reaction\",\"time\"],\"DurationTypes\":[\"Until dispelled\"],\"RangeTypes\":[\"Special\",\"Self\",\"Sight\",\"Finite range\"],\"CastingTimeBounds\":{\"MinValue\":0,\"MaxValue\":24,\"MinUnit\":\"second\",\"MaxUnit\":\"hour\"},\"DurationBounds\":{\"MinValue\":0,\"MaxValue\":30,\"MinUnit\":\"second\",\"MaxUnit\":\"day\"},\"RangeBounds\":{\"MinValue\":0,\"MaxValue\":1,\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\"}},\"SpellSlotStatus\":{\"totalSlots\":[4,3,2,2,1,0,0,0,0],\"usedSlots\":[3,1,0,1,0,0,0,0,0]},\"VersionCode\":\"4.2.0\"}],\"sources\":[{\"name\":\"Test Source\",\"code\":\"TST\"}],\"spells\":[{\"id\":100000,\"name\":\"Test Spell\",\"desc\":\"abc\",\"higher_level\":\"def\",\"range\":\"3 feet\",\"material\":\"\",\"royalty\":\"\",\"ritual\":false,\"duration\":\"2 seconds\",\"concentration\":true,\"casting_time\":\"1 action\",\"level\":2,\"school\":\"Abjuration\",\"locations\":[{\"sourcebook\":\"TST\",\"page\":-1}],\"components\":[\"V\",\"S\"],\"classes\":[\"Artificer\",\"Paladin\"],\"subclasses\":[],\"tce_expanded_classes\":[],\"ruleset\":\"created\"}]}}";
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                try {
                    final SpellbookViewModel viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);
                    final JSONObject json = new JSONObject(jsonString);
                    viewModel.loadCreatedContent(json);

                    final Source[] createdSources = Source.createdSources();
                    assertEquals(1, createdSources.length);
                    final Source source = createdSources[0];
                    assertEquals("TST", source.getCode());
                    assertEquals("Test Source", source.getDisplayName());

                    final List<Spell> createdSpells = viewModel.getCreatedSpells();
                    assertEquals(1, createdSpells.size());
                    final Spell spell = createdSpells.get(0);

                    final Set<Spell> tstSpells = viewModel.getCreatedSpellsForSource(source);
                    AndroidTestUtils.assertCollectionsSameUnordered(createdSpells, tstSpells);

                    assertEquals("Test Spell", spell.getName());
                    assertEquals("abc", spell.getDescription());
                    assertEquals("def", spell.getHigherLevel());
                    final UUID spellID = spell.getID();
                    assertEquals(spellID, Spellbook.uuidForID(100000));
                    assertTrue(spell.getMaterial().isEmpty());
                    assertTrue(spell.getRoyalty().isEmpty());
                    assertFalse(spell.getRitual());
                    assertTrue(spell.getConcentration());
                    assertEquals(2, spell.getLevel());
                    assertEquals(School.ABJURATION, spell.getSchool());
                    final Map<Source, Integer> locations = spell.getLocations();
                    assertEquals(1, locations.size());
                    final Integer page = locations.get(source);
                    assertNotNull(page);
                    assertEquals(-1, page.intValue());

                    final Range range = spell.getRange();
                    assertEquals(Range.RangeType.RANGED, range.type);
                    assertEquals(3, range.value, 0);
                    assertEquals(LengthUnit.FOOT, range.unit);

                    final CastingTime castingTime = spell.getCastingTime();
                    assertEquals(CastingTime.CastingTimeType.ACTION, castingTime.type);
                    assertEquals(1, castingTime.value, 0);
                    assertEquals(TimeUnit.SECOND, castingTime.unit);

                    final Duration duration = spell.getDuration();
                    assertEquals(Duration.DurationType.SPANNING, duration.type);
                    assertEquals(2, duration.value, 0);
                    assertEquals(TimeUnit.SECOND, duration.unit);

                    assertArrayEquals(new boolean[]{true, true, false, false}, spell.getComponents());
                    assertArrayEquals(new CasterClass[]{CasterClass.ARTIFICER, CasterClass.PALADIN}, spell.getClasses().toArray());
                    assertEquals(0, spell.getSubclasses().size());
                    assertEquals(0, spell.getTashasExpandedClasses().size());
                    assertEquals(Ruleset.RULES_CREATED, spell.getRuleset());

                    assertEquals(1, viewModel.getCreatedSpellsForSource(source).size());

                    final List<String> characterNames = viewModel.getCharacterNames();
                    assertEquals(1, characterNames.size());

                    final CharacterProfile profile = viewModel.getProfileByName(characterNames.get(0));
                    assertEquals("Test", profile.getName());

                    final SortFilterStatus sortFilterStatus = profile.getSortFilterStatus();
                    final SpellFilterStatus spellFilterStatus = profile.getSpellFilterStatus();
                    final SpellSlotStatus spellSlotStatus = profile.getSpellSlotStatus();
                    assertEquals(StatusFilterField.ALL, sortFilterStatus.getStatusFilterField());
                    assertEquals(SortField.DURATION, sortFilterStatus.getFirstSortField());
                    assertEquals(SortField.NAME, sortFilterStatus.getSecondSortField());
                    assertFalse(sortFilterStatus.getFirstSortReverse());
                    assertTrue(sortFilterStatus.getSecondSortReverse());
                    assertEquals(0, sortFilterStatus.getMinSpellLevel());
                    assertEquals(9, sortFilterStatus.getMaxSpellLevel());

                    // 'Source' is a used-created source, so it should be null and removed
                    final Collection<Source> shouldBeVisibleSources = new ArrayList<>(Arrays.asList(Source.PLAYERS_HANDBOOK, Source.XANATHARS_GTE, Source.TASHAS_COE));
                    final Collection<Source> shouldBeHiddenSources = SpellbookUtils.complement(shouldBeVisibleSources, Source.values());
                    final Collection<Source> visibleSources = sortFilterStatus.getVisibleSources(true);
                    final Collection<Source> hiddenSources = sortFilterStatus.getVisibleSources(false);
                    AndroidTestUtils.assertCollectionsSameUnordered(shouldBeVisibleSources, visibleSources);
                    AndroidTestUtils.assertCollectionsSameUnordered(shouldBeHiddenSources, hiddenSources);

                    final Collection<School> shouldBeVisibleSchools = Arrays.asList(School.values());
                    AndroidTestUtils.assertCollectionsSameUnordered(shouldBeVisibleSchools, sortFilterStatus.getVisibleSchools(true));
                    assertEquals(0, sortFilterStatus.getVisibleSchools(false).size());

                    final Collection<CasterClass> shouldBeVisibleClasses = Arrays.asList(CasterClass.values());
                    AndroidTestUtils.assertCollectionsSameUnordered(shouldBeVisibleClasses, sortFilterStatus.getVisibleClasses(true));
                    assertTrue(sortFilterStatus.getVisibleClasses(false).isEmpty());

                    assertEquals(TimeUnit.SECOND, sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class));
                    assertEquals(0, sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class));
                    assertEquals(TimeUnit.HOUR, sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class));
                    assertEquals(24, sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class));

                    assertEquals(LengthUnit.FOOT, sortFilterStatus.getMinUnit(Range.RangeType.class));
                    assertEquals(0, sortFilterStatus.getMinValue(Range.RangeType.class));
                    assertEquals(LengthUnit.MILE, sortFilterStatus.getMaxUnit(Range.RangeType.class));
                    assertEquals(1, sortFilterStatus.getMaxValue(Range.RangeType.class));

                    assertEquals(TimeUnit.SECOND, sortFilterStatus.getMinUnit(Duration.DurationType.class));
                    assertEquals(0, sortFilterStatus.getMinValue(Duration.DurationType.class));
                    assertEquals(TimeUnit.DAY, sortFilterStatus.getMaxUnit(Duration.DurationType.class));
                    assertEquals(30, sortFilterStatus.getMaxValue(Duration.DurationType.class));

                    assertTrue(sortFilterStatus.getVerbalFilter(true));
                    assertFalse(sortFilterStatus.getSomaticFilter(true));
                    assertTrue(sortFilterStatus.getMaterialFilter(true));
                    assertTrue(sortFilterStatus.getRoyaltyFilter(true));
                    assertTrue(sortFilterStatus.getVerbalFilter(false));
                    assertTrue(sortFilterStatus.getSomaticFilter(false));
                    assertTrue(sortFilterStatus.getMaterialFilter(false));
                    assertTrue(sortFilterStatus.getRoyaltyFilter(false));

                    final Collection<CastingTime.CastingTimeType> shouldBeVisibleCastingTimeTypes = new ArrayList<>(Arrays.asList(CastingTime.CastingTimeType.BONUS_ACTION, CastingTime.CastingTimeType.REACTION, CastingTime.CastingTimeType.TIME));
                    AndroidTestUtils.assertCollectionsSameUnordered(sortFilterStatus.getVisibleCastingTimeTypes(true), shouldBeVisibleCastingTimeTypes);
                    final Collection<CastingTime.CastingTimeType> shouldBeHiddenCastingTimeTypes = new ArrayList<>(Arrays.asList(CastingTime.CastingTimeType.ACTION));
                    AndroidTestUtils.assertCollectionsSameUnordered(sortFilterStatus.getVisibleCastingTimeTypes(false), shouldBeHiddenCastingTimeTypes);
                    final Collection<Duration.DurationType> shouldBeVisibleDurationTypes = new ArrayList<>(Arrays.asList(Duration.DurationType.UNTIL_DISPELLED));
                    AndroidTestUtils.assertCollectionsSameUnordered(sortFilterStatus.getVisibleDurationTypes(true), shouldBeVisibleDurationTypes);
                    final Collection<Duration.DurationType> shouldBeHiddenDurationTypes = new ArrayList<>(Arrays.asList(Duration.DurationType.SPECIAL, Duration.DurationType.INSTANTANEOUS, Duration.DurationType.SPANNING));
                    AndroidTestUtils.assertCollectionsSameUnordered(sortFilterStatus.getVisibleDurationTypes(false), shouldBeHiddenDurationTypes);
                    final Collection<Range.RangeType> shouldBeVisibleRangeTypes = new ArrayList<>(Arrays.asList(Range.RangeType.SPECIAL, Range.RangeType.SELF, Range.RangeType.SIGHT, Range.RangeType.RANGED));
                    AndroidTestUtils.assertCollectionsSameUnordered(sortFilterStatus.getVisibleRangeTypes(true), shouldBeVisibleRangeTypes);
                    final Collection<Range.RangeType> shouldBeHiddenRangeTypes = new ArrayList<>(Arrays.asList(Range.RangeType.TOUCH, Range.RangeType.UNLIMITED));
                    AndroidTestUtils.assertCollectionsSameUnordered(sortFilterStatus.getVisibleRangeTypes(false), shouldBeHiddenRangeTypes);

                    assertFalse(sortFilterStatus.getApplyFiltersToSearch());
                    assertFalse(sortFilterStatus.getApplyFiltersToLists());
                    assertFalse(sortFilterStatus.getUseTashasExpandedLists());

                    assertTrue(sortFilterStatus.getConcentrationFilter(true));
                    assertFalse(sortFilterStatus.getConcentrationFilter(false));
                    assertTrue(sortFilterStatus.getRitualFilter(false));
                    assertTrue(sortFilterStatus.getRitualFilter(false));

                    final List<UUID> expectedFavoriteIDs = new ArrayList<>() {{
                            add(UUID.fromString("940cbf0f-be98-4950-86c4-2ed10039bf78"));
                            add(UUID.fromString("6c64e999-16b5-43aa-a373-3e882918d847"));
                            add(UUID.fromString("73b2e8b3-de2a-4696-9569-ad442e8a90e8"));
                    }};
                    AndroidTestUtils.assertCollectionsSameUnordered(spellFilterStatus.favoriteSpellIDs(), expectedFavoriteIDs);
                    final List<UUID> expectedPreparedIDs = new ArrayList<>() {{
                        add(UUID.fromString("6c64e999-16b5-43aa-a373-3e882918d847"));
                        put(UUID.fromString("6c64e999-16b5-43aa-a373-3e882918d847"));
                    }};
                    AndroidTestUtils.assertCollectionsSameUnordered(spellFilterStatus.preparedSpellIDs(), expectedPreparedIDs);
                    final List<UUID> expectedKnownIDs = new ArrayList<>() {{
                        add(UUID.fromString("6c64e999-16b5-43aa-a373-3e882918d847"));
                        add(UUID.fromString("6c64e999-16b5-43aa-a373-3e882918d847"));
                        add(UUID.fromString("32da4000-8026-44d1-a130-8ded63de056e"));
                    }};
                    AndroidTestUtils.assertCollectionsSameUnordered(spellFilterStatus.knownSpellIDs(), expectedKnownIDs);

                    assertEquals(4, spellSlotStatus.getTotalSlots(1));
                    assertEquals(3, spellSlotStatus.getTotalSlots(2));
                    assertEquals(2, spellSlotStatus.getTotalSlots(3));
                    assertEquals(2, spellSlotStatus.getTotalSlots(4));
                    assertEquals(1, spellSlotStatus.getTotalSlots(5));
                    for (int level = 6; level <= Spellbook.MAX_SPELL_LEVEL; level++) {
                        assertEquals(0, spellSlotStatus.getTotalSlots(level));
                    }

                    assertEquals(3, spellSlotStatus.getUsedSlots(1));
                    assertEquals(1, spellSlotStatus.getUsedSlots(2));
                    assertEquals(0, spellSlotStatus.getUsedSlots(3));
                    assertEquals(1, spellSlotStatus.getUsedSlots(4));
                    for (int level = 5; level <= Spellbook.MAX_SPELL_LEVEL; level++) {
                        assertEquals(0, spellSlotStatus.getUsedSlots(level));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Assert.fail();
                }
            });
        }
    }

   @Test
   public void testParseSpellList() {
       try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
           scenario.onActivity(activity -> {
               final SpellbookViewModel viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);
               assertEquals(941, viewModel.getAllSpells().size());
           });
       }
   }


    @Test
    public void testParseSpellListPt() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                final Locale ptLocale = new Locale("pt");
                final Application application = activity.getApplication();
                final SpellbookViewModel viewModel = new SpellbookViewModel(application);
                viewModel.updateSpellsForLocale(ptLocale);
                assertEquals(941, viewModel.getAllSpells().size());
            });
        }
    }
}
