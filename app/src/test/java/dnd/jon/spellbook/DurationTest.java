package dnd.jon.spellbook;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class DurationTest {

    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getContext();
    }

    @Test
    @Config(sdk = 34)
    public void DurationInSecondsTest() {
        Duration duration = new Duration(Duration.DurationType.SPANNING, 3, TimeUnit.ROUND);
        Truth.assertThat(duration.timeInSeconds()).isEqualTo(18);

        duration = new Duration(Duration.DurationType.SPANNING, 8, TimeUnit.SECOND);
        Truth.assertThat(duration.timeInSeconds()).isEqualTo(8);

        duration = new Duration(Duration.DurationType.SPANNING, 30, TimeUnit.MINUTE);
        Truth.assertThat(duration.timeInSeconds()).isEqualTo(1800);

        duration = new Duration(Duration.DurationType.SPANNING, 3, TimeUnit.HOUR);
        Truth.assertThat(duration.timeInSeconds()).isEqualTo(10800);

        duration = new Duration(Duration.DurationType.SPANNING, 2, TimeUnit.DAY);
        Truth.assertThat(duration.timeInSeconds()).isEqualTo(172800);

        duration = new Duration(Duration.DurationType.SPANNING, 7, TimeUnit.YEAR);
        Truth.assertThat(duration.timeInSeconds()).isEqualTo(220752000);

        duration = new Duration(Duration.DurationType.SPECIAL);
        Truth.assertThat(duration.timeInSeconds()).isEqualTo(0);

        duration = new Duration(Duration.DurationType.INSTANTANEOUS);
        Truth.assertThat(duration.timeInSeconds()).isEqualTo(0);

        duration = new Duration(Duration.DurationType.UNTIL_DISPELLED);
        Truth.assertThat(duration.timeInSeconds()).isEqualTo(0);
    }
    @Test
    @Config(sdk = 34)
    public void DurationSpecialParseTest() {
        final String durationString = "Special";
        final Duration duration = DisplayUtils.durationFromString(context, durationString);
        final Duration expected = new Duration(Duration.DurationType.SPECIAL);
        Truth.assertThat(duration).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void DurationInstantaneousParseTest() {
        final String durationString = "Instantaneous";
        final Duration duration = DisplayUtils.durationFromString(context, durationString);
        final Duration expected = new Duration(Duration.DurationType.INSTANTANEOUS);
        Truth.assertThat(duration).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void DurationUntilDispelledParseTest() {
        final String durationString = "Until dispelled";
        final Duration duration = DisplayUtils.durationFromString(context, durationString);
        final Duration expected = new Duration(Duration.DurationType.UNTIL_DISPELLED);
        Truth.assertThat(duration).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void DurationSpanningParseTest() {
        final String durationString = "3 seconds";
        final Duration duration = DisplayUtils.durationFromString(context, durationString);
        final Duration expected = new Duration(Duration.DurationType.SPANNING, 3, TimeUnit.SECOND);
        Truth.assertThat(duration).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void DurationSpanningParseTest2() {
        final String durationString = "5 rounds";
        final Duration duration = DisplayUtils.durationFromString(context, durationString);
        final Duration expected = new Duration(Duration.DurationType.SPANNING, 5, TimeUnit.ROUND);
        Truth.assertThat(duration).isEqualTo(expected);
    }
    @Test
    @Config(sdk = 34)
    public void DurationSpanningParseTest3() {
        final String durationString = "2 hours";
        final Duration duration = DisplayUtils.durationFromString(context, durationString);
        final Duration expected = new Duration(Duration.DurationType.SPANNING, 2, TimeUnit.HOUR);
        Truth.assertThat(duration).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void DurationSpanningParseTest4() {
        final String durationString = "1 year";
        final Duration duration = DisplayUtils.durationFromString(context, durationString);
        final Duration expected = new Duration(Duration.DurationType.SPANNING, 1, TimeUnit.YEAR);
        Truth.assertThat(duration).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void DurationSpanningParseTest5() {
        final String durationString = "18 minutes";
        final Duration duration = DisplayUtils.durationFromString(context, durationString);
        final Duration expected = new Duration(Duration.DurationType.SPANNING, 18, TimeUnit.MINUTE);
        Truth.assertThat(duration).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void DurationSpanningParseTest6() {
        final String durationString = "100 hours";
        final Duration duration = DisplayUtils.durationFromString(context, durationString);
        final Duration expected = new Duration(Duration.DurationType.SPANNING, 100, TimeUnit.HOUR);
        Truth.assertThat(duration).isEqualTo(expected);
    }

}
