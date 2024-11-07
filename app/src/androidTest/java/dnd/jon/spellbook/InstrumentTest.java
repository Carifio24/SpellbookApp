package dnd.jon.spellbook;

import android.content.Context;

import androidx.lifecycle.ViewModelProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;


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
        final String json = "{\"name\":\"Test Source\",\"code\":\"TST\",\"spells\":[{\"id\":100000,\"name\":\"Test Spell\",\"desc\":\"abc\",\"higher_level\":\"def\",\"range\":\"3 feet\",\"material\":\"\",\"royalty\":\"\",\"ritual\":false,\"duration\":\"2 seconds\",\"concentration\":true,\"casting_time\":\"1 action\",\"level\":2,\"school\":\"Abjuration\",\"locations\":[{\"sourcebook\":\"TST\",\"page\":-1}],\"components\":[\"V\",\"S\"],\"classes\":[\"Artificer\",\"Paladin\"],\"subclasses\":[],\"tce_expanded_classes\":[],\"ruleset\":\"created\"}]}";
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                final SpellbookViewModel viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);
                viewModel.addSourceFromText(json);

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
                final Map<Source,Integer> locations = spell.getLocations();
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
            });
        }
    }
}
