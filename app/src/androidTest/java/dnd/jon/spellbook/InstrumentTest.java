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
                    assertEquals(source.getCode(), "TST");
                    assertEquals(source.getDisplayName(), "Test Source");

                    final List<Spell> createdSpells = viewModel.getCreatedSpells();
                    assertEquals(1, createdSpells.size());
                    final Spell spell = createdSpells.get(0);
                    assertEquals(spell.getName(), "Test Spell");
                    assertEquals(spell.getDescription(), "abc");
                    assertEquals(spell.getHigherLevel(), "def");
                    assertEquals(spell.getID(), 100000);
                    assertTrue(spell.getMaterial().isEmpty());
                    assertTrue(spell.getRoyalty().isEmpty());
                    assertFalse(spell.getRitual());
                    assertTrue(spell.getConcentration());
                    assertEquals(spell.getLevel(), 2);
                    assertEquals(spell.getSchool(), School.ABJURATION);
                    final Map<Source, Integer> locations = spell.getLocations();
                    assertEquals(locations.size(), 1);
                    final Integer page = locations.get(source);
                    assertNotNull(page);
                    assertEquals(page.intValue(), -1);

                    final Range range = spell.getRange();
                    assertEquals(range.type, Range.RangeType.RANGED);
                    assertEquals(range.value, 3, 0);
                    assertEquals(range.unit, LengthUnit.FOOT);

                    final CastingTime castingTime = spell.getCastingTime();
                    assertEquals(castingTime.type, CastingTime.CastingTimeType.ACTION);
                    assertEquals(castingTime.value, 1, 0);
                    assertEquals(castingTime.unit, TimeUnit.SECOND);

                    final Duration duration = spell.getDuration();
                    assertEquals(duration.type, Duration.DurationType.SPANNING);
                    assertEquals(duration.value, 2, 0);
                    assertEquals(duration.unit, TimeUnit.SECOND);

                    assertArrayEquals(spell.getComponents(), new boolean[]{true, true, false, false});
                    assertArrayEquals(spell.getClasses().toArray(), new CasterClass[]{CasterClass.ARTIFICER, CasterClass.PALADIN});
                    assertEquals(spell.getSubclasses().size(), 0);
                    assertEquals(spell.getTashasExpandedClasses().size(), 0);
                    assertEquals(spell.getRuleset(), Ruleset.RULES_CREATED);

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
                    assertEquals(source.getCode(), "TST");
                    assertEquals(source.getDisplayName(), "Test Source");

                    final List<Spell> createdSpells = viewModel.getCreatedSpells();
                    assertEquals(1, createdSpells.size());
                    final Spell spell = createdSpells.get(0);

                    final Set<Spell> tstSpells = viewModel.getCreatedSpellsForSource(source);
                    System.out.println(createdSpells);
                    System.out.println(tstSpells);
                    AndroidTestUtils.assertCollectionsSameUnordered(createdSpells, tstSpells);

                    assertEquals(spell.getName(), "Test Spell");
                    assertEquals(spell.getDescription(), "abc");
                    assertEquals(spell.getHigherLevel(), "def");
                    assertEquals(spell.getID(), 100000);
                    assertTrue(spell.getMaterial().isEmpty());
                    assertTrue(spell.getRoyalty().isEmpty());
                    assertFalse(spell.getRitual());
                    assertTrue(spell.getConcentration());
                    assertEquals(spell.getLevel(), 2);
                    assertEquals(spell.getSchool(), School.ABJURATION);
                    final Map<Source, Integer> locations = spell.getLocations();
                    assertEquals(locations.size(), 1);
                    final Integer page = locations.get(source);
                    assertNotNull(page);
                    assertEquals(page.intValue(), -1);

                    final Range range = spell.getRange();
                    assertEquals(range.type, Range.RangeType.RANGED);
                    assertEquals(range.value, 3, 0);
                    assertEquals(range.unit, LengthUnit.FOOT);

                    final CastingTime castingTime = spell.getCastingTime();
                    assertEquals(castingTime.type, CastingTime.CastingTimeType.ACTION);
                    assertEquals(castingTime.value, 1, 0);
                    assertEquals(castingTime.unit, TimeUnit.SECOND);

                    final Duration duration = spell.getDuration();
                    assertEquals(duration.type, Duration.DurationType.SPANNING);
                    assertEquals(duration.value, 2, 0);
                    assertEquals(duration.unit, TimeUnit.SECOND);

                    assertArrayEquals(spell.getComponents(), new boolean[]{true, true, false, false});
                    assertArrayEquals(spell.getClasses().toArray(), new CasterClass[]{CasterClass.ARTIFICER, CasterClass.PALADIN});
                    assertEquals(spell.getSubclasses().size(), 0);
                    assertEquals(spell.getTashasExpandedClasses().size(), 0);
                    assertEquals(spell.getRuleset(), Ruleset.RULES_CREATED);

                    assertEquals(1, viewModel.getCreatedSpellsForSource(source).size());

                    final List<String> characterNames = viewModel.getCharacterNames();
                    assertEquals(characterNames.size(), 1);

                    final CharacterProfile profile = viewModel.getProfileByName(characterNames.get(0));
                    assertEquals(profile.getName(), "Test");

                    final SortFilterStatus sortFilterStatus = profile.getSortFilterStatus();
                    final SpellFilterStatus spellFilterStatus = profile.getSpellFilterStatus();
                    final SpellSlotStatus spellSlotStatus = profile.getSpellSlotStatus();
                    assertEquals(sortFilterStatus.getStatusFilterField(), StatusFilterField.ALL);
                    assertEquals(sortFilterStatus.getFirstSortField(), SortField.DURATION);
                    assertEquals(sortFilterStatus.getSecondSortField(), SortField.NAME);
                    assertFalse(sortFilterStatus.getFirstSortReverse());
                    assertTrue(sortFilterStatus.getSecondSortReverse());
                    assertEquals(sortFilterStatus.getMinSpellLevel(), 0);
                    assertEquals(sortFilterStatus.getMaxSpellLevel(), 9);

                    // 'Source' is a used-created source, so it should be null and removed
                    final Collection<Source> shouldBeVisibleSources = new ArrayList<>(Arrays.asList(Source.PLAYERS_HANDBOOK, Source.XANATHARS_GTE, Source.TASHAS_COE));
                    final Collection<Source> shouldBeHiddenSources = SpellbookUtils.complement(shouldBeVisibleSources, Source.values());
                    final Collection<Source> visibleSources = sortFilterStatus.getVisibleSources(true);
                    final Collection<Source> hiddenSources = sortFilterStatus.getVisibleSources(false);
                    AndroidTestUtils.assertCollectionsSameUnordered(shouldBeVisibleSources, visibleSources);
                    AndroidTestUtils.assertCollectionsSameUnordered(shouldBeHiddenSources, hiddenSources);

                    final Collection<School> shouldBeVisibleSchools = Arrays.asList(School.values());
                    AndroidTestUtils.assertCollectionsSameUnordered(shouldBeVisibleSchools, sortFilterStatus.getVisibleSchools(true));
                    assertEquals(sortFilterStatus.getVisibleSchools(false).size(), 0);

                    final Collection<CasterClass> shouldBeVisibleClasses = Arrays.asList(CasterClass.values());
                    AndroidTestUtils.assertCollectionsSameUnordered(shouldBeVisibleClasses, sortFilterStatus.getVisibleClasses(true));
                    assertEquals(sortFilterStatus.getVisibleClasses(false).size(), 0);

                    assertEquals(sortFilterStatus.getMinUnit(CastingTime.CastingTimeType.class), TimeUnit.SECOND);
                    assertEquals(sortFilterStatus.getMinValue(CastingTime.CastingTimeType.class), 0);
                    assertEquals(sortFilterStatus.getMaxUnit(CastingTime.CastingTimeType.class), TimeUnit.HOUR);
                    assertEquals(sortFilterStatus.getMaxValue(CastingTime.CastingTimeType.class), 24);

                    assertEquals(sortFilterStatus.getMinUnit(Range.RangeType.class), LengthUnit.FOOT);
                    assertEquals(sortFilterStatus.getMinValue(Range.RangeType.class), 0);
                    assertEquals(sortFilterStatus.getMaxUnit(Range.RangeType.class), LengthUnit.MILE);
                    assertEquals(sortFilterStatus.getMaxValue(Range.RangeType.class), 1);

                    assertEquals(sortFilterStatus.getMinUnit(Duration.DurationType.class), TimeUnit.SECOND);
                    assertEquals(sortFilterStatus.getMinValue(Duration.DurationType.class), 0);
                    assertEquals(sortFilterStatus.getMaxUnit(Duration.DurationType.class), TimeUnit.DAY);
                    assertEquals(sortFilterStatus.getMaxValue(Duration.DurationType.class), 30);

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

                    AndroidTestUtils.assertCollectionsSameUnordered(spellFilterStatus.favoriteSpellIDs(), Arrays.asList(10, 19, 106));
                    AndroidTestUtils.assertCollectionsSameUnordered(spellFilterStatus.preparedSpellIDs(), Arrays.asList(19, 106));
                    AndroidTestUtils.assertCollectionsSameUnordered(spellFilterStatus.knownSpellIDs(), Arrays.asList(19, 106, 198));

                    assertEquals(spellSlotStatus.getTotalSlots(1), 4);
                    assertEquals(spellSlotStatus.getTotalSlots(2), 3);
                    assertEquals(spellSlotStatus.getTotalSlots(3), 2);
                    assertEquals(spellSlotStatus.getTotalSlots(4), 2);
                    assertEquals(spellSlotStatus.getTotalSlots(5), 1);
                    for (int level = 6; level <= Spellbook.MAX_SPELL_LEVEL; level++) {
                        assertEquals(spellSlotStatus.getTotalSlots(level), 0);
                    }

                    assertEquals(spellSlotStatus.getUsedSlots(1), 3);
                    assertEquals(spellSlotStatus.getUsedSlots(2), 1);
                    assertEquals(spellSlotStatus.getUsedSlots(3), 0);
                    assertEquals(spellSlotStatus.getUsedSlots(4), 1);
                    for (int level = 5; level <= Spellbook.MAX_SPELL_LEVEL; level++) {
                        assertEquals(spellSlotStatus.getUsedSlots(level), 0);
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
               assertEquals(viewModel.getAllSpells().size(), 941);
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
                assertEquals(viewModel.getAllSpells().size(), 941);
            });
        }
    }
}
