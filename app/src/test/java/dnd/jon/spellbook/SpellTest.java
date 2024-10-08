package dnd.jon.spellbook;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.common.truth.Truth;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Locale;


@RunWith(RobolectricTestRunner.class)
public class SpellTest {

    @Test
    @Config(sdk = 34)
    public void SpellParseTest() {
        final String jsonString = "{ \"id\": 100000, \"name\": \"Test Spell\", \"desc\": \"abcde\", \"higher_level\": \"fghij\", \"range\": \"2 foot\", \"material\": \"\", \"royalty\": \"\", \"ritual\": true, \"duration\": \"3 minute\", \"concentration\": false, \"casting_time\": \"action\", \"level\": 2, \"school\": \"Evocation\", \"locations\": [], \"components\": [ \"V\", \"S\" ], \"classes\": [ \"Artificer\", \"Paladin\" ], \"subclasses\": [], \"tce_expanded_classes\": [] }";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final Context context = InstrumentationRegistry.getInstrumentation().getContext();
            final SpellCodec codec = new SpellCodec(context);
            final SpellBuilder builder = new SpellBuilder(context);
            final Spell spell = codec.parseSpell(json, builder, true);
            Truth.assertThat(spell.getID()).isEqualTo(100000);
            Truth.assertThat(spell.getName()).isEqualTo("Test Spell");
            Truth.assertThat(spell.getDescription()).isEqualTo("abcde");
            Truth.assertThat(spell.getHigherLevel()).isEqualTo("fghij");

            Truth.assertThat(spell.getSchool()).isEqualTo(School.EVOCATION);
            Truth.assertThat(spell.getLevel()).isEqualTo(2);
            final boolean[] expectedComponents = new boolean[]{true, true, false, false};
            Assert.assertArrayEquals(spell.getComponents(), expectedComponents);
            final CastingTime expectedCT = new CastingTime(CastingTime.CastingTimeType.ACTION);
            Truth.assertThat(expectedCT.compareTo(spell.getCastingTime())).isEqualTo(0);
            final Duration expectedDuration = new Duration(Duration.DurationType.SPANNING, 3, TimeUnit.MINUTE);
            Truth.assertThat(expectedDuration.compareTo(spell.getDuration())).isEqualTo(0);
            final Range expectedRange = new Range(Range.RangeType.RANGED, 2, LengthUnit.FOOT);
            Truth.assertThat(expectedRange.compareTo(spell.getRange())).isEqualTo(0);

            Truth.assertThat(spell.getConcentration()).isFalse();
            Truth.assertThat(spell.getRitual()).isTrue();

            final CasterClass[] expectedClasses = new CasterClass[]{CasterClass.ARTIFICER, CasterClass.PALADIN};
            Assert.assertArrayEquals(expectedClasses, spell.getClasses().toArray());
            Assert.assertArrayEquals(new Subclass[0], spell.getSubclasses().toArray());
            Assert.assertArrayEquals(new CasterClass[0], spell.getTashasExpandedClasses().toArray());
            Truth.assertThat(spell.getLocations().size()).isEqualTo(0);

            Truth.assertThat(spell.getMaterial()).isEmpty();
            Truth.assertThat(spell.getRoyalty()).isEmpty();

        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    @Config(sdk = 34)
    public void spellParsePtTest() {
       final String jsonString = "{ \"id\": 100000, \"name\": \"Spell 1\", \"desc\": \"Stuff\", \"higher_level\": \"More stuff\", \"range\": \"Toque\", \"material\": \"\", \"royalty\": \"\", \"ritual\": true, \"duration\": \"3 horas\", \"concentration\": false, \"casting_time\": \"1 ação\", \"level\": 2, \"school\": \"Adivinhação\", \"locations\": [ { \"sourcebook\": \"SB\", \"page\": -1 } ], \"components\": [ \"V\", \"S\" ], \"classes\": [ \"Artífice\", \"Bardo\", \"Clérigo\" ], \"subclasses\": [], \"tce_expanded_classes\": [] }";
       try {
           final JSONObject json = new JSONObject(jsonString);
           final Context instrumentationContext = InstrumentationRegistry.getInstrumentation().getContext();
           final Context context = LocalizationUtils.getLocalizedContext(instrumentationContext, new Locale("pt"));
           final SpellCodec codec = new SpellCodec(context);
           final SpellBuilder builder = new SpellBuilder(context);
           final Spell spell = codec.parseSpell(json, builder, false);
           Truth.assertThat(spell.getID()).isEqualTo(100000);
           Truth.assertThat(spell.getName()).isEqualTo("Spell 1");
           Truth.assertThat(spell.getDescription()).isEqualTo("Stuff");
           Truth.assertThat(spell.getHigherLevel()).isEqualTo("More stuff");

           Truth.assertThat(spell.getSchool()).isEqualTo(School.DIVINATION);
           Truth.assertThat(spell.getLevel()).isEqualTo(2);
           final boolean[] expectedComponents = new boolean[]{true,true,false,false};
           Assert.assertArrayEquals(spell.getComponents(), expectedComponents);
           final CastingTime expectedCT = new CastingTime(CastingTime.CastingTimeType.ACTION);
           Truth.assertThat(expectedCT.compareTo(spell.getCastingTime())).isEqualTo(0);
           final Duration expectedDuration = new Duration(Duration.DurationType.SPANNING, 3, TimeUnit.HOUR);
           Truth.assertThat(expectedDuration.compareTo(spell.getDuration())).isEqualTo(0);
           final Range expectedRange = new Range(Range.RangeType.TOUCH);
           Truth.assertThat(expectedRange.compareTo(spell.getRange())).isEqualTo(0);

           Truth.assertThat(spell.getConcentration()).isFalse();
           Truth.assertThat(spell.getRitual()).isTrue();

           final CasterClass[] expectedClasses = new CasterClass[]{CasterClass.ARTIFICER, CasterClass.BARD, CasterClass.CLERIC};
           Assert.assertArrayEquals(expectedClasses, spell.getClasses().toArray());
           Assert.assertArrayEquals(new Subclass[0], spell.getSubclasses().toArray());
           Assert.assertArrayEquals(new CasterClass[0], spell.getTashasExpandedClasses().toArray());
           Truth.assertThat(spell.getLocations().size()).isEqualTo(0);

           Truth.assertThat(spell.getMaterial()).isEmpty();
           Truth.assertThat(spell.getRoyalty()).isEmpty();

       } catch (JSONException e) {
           e.printStackTrace();
           Assert.fail();
       }
    }
}
