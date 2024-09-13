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
public class CastingTimeTest {

    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getContext();
    }

    @Test
    @Config(sdk = 28)
    public void CastingTimeActionParseTest() {
        final String castingTimeString = "1 action";
        final CastingTime castingTime = DisplayUtils.castingTimeFromString(context, castingTimeString);
        final CastingTime expected = new CastingTime(CastingTime.CastingTimeType.ACTION);
        Truth.assertThat(castingTime).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 28)
    public void CastingTimeBonusActionParseTest() {
        final String castingTimeString = "1 bonus action";
        final CastingTime castingTime = DisplayUtils.castingTimeFromString(context, castingTimeString);
        final CastingTime expected = new CastingTime(CastingTime.CastingTimeType.BONUS_ACTION);
        Truth.assertThat(castingTime).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 28)
    public void CastingTimeReactionParseTest() {
        final String castingTimeString = "1 reaction";
        final CastingTime castingTime = DisplayUtils.castingTimeFromString(context, castingTimeString);
        final CastingTime expected = new CastingTime(CastingTime.CastingTimeType.REACTION);
        Truth.assertThat(castingTime).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 28)
    public void CastingTimeSpanningParseTest() {
        final String castingTimeString = "10 seconds";
        final CastingTime castingTime = DisplayUtils.castingTimeFromString(context, castingTimeString);
        final CastingTime expected = new CastingTime(CastingTime.CastingTimeType.TIME, 10, TimeUnit.SECOND);
        Truth.assertThat(castingTime).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 28)
    public void CastingTimeSpanningParseTest2() {
        final String castingTimeString = "25 minutes";
        final CastingTime castingTime = DisplayUtils.castingTimeFromString(context, castingTimeString);
        final CastingTime expected = new CastingTime(CastingTime.CastingTimeType.TIME, 25, TimeUnit.MINUTE);
        Truth.assertThat(castingTime).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 28)
    public void CastingTimeSpanningParseTest3() {
        final String castingTimeString = "1 round";
        final CastingTime castingTime = DisplayUtils.castingTimeFromString(context, castingTimeString);
        final CastingTime expected = new CastingTime(CastingTime.CastingTimeType.TIME, 1, TimeUnit.ROUND);
        Truth.assertThat(castingTime).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 28)
    public void CastingTimeSpanningParseTest4() {
        final String castingTimeString = "2 years";
        final CastingTime castingTime = DisplayUtils.castingTimeFromString(context, castingTimeString);
        final CastingTime expected = new CastingTime(CastingTime.CastingTimeType.TIME, 2, TimeUnit.YEAR);
        Truth.assertThat(castingTime).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 28)
    public void CastingTimeSpanningParseTest5() {
        final String castingTimeString = "1 day";
        final CastingTime castingTime = DisplayUtils.castingTimeFromString(context, castingTimeString);
        final CastingTime expected = new CastingTime(CastingTime.CastingTimeType.TIME, 1, TimeUnit.DAY);
        Truth.assertThat(castingTime).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 28)
    public void CastingTimeSpanningParseTest6() {
        final String castingTimeString = "4 hours";
        final CastingTime castingTime = DisplayUtils.castingTimeFromString(context, castingTimeString);
        final CastingTime expected = new CastingTime(CastingTime.CastingTimeType.TIME, 4, TimeUnit.HOUR);
        Truth.assertThat(castingTime).isEqualTo(expected);
    }


    @Test
    @Config(sdk = 28)
    public void CastingTimeActionParse2024Test() {
        final String castingTimeString = "Action";
        final CastingTime castingTime = DisplayUtils.castingTimeFromString(context, castingTimeString);
        final CastingTime expected = new CastingTime(CastingTime.CastingTimeType.ACTION);
        Truth.assertThat(castingTime).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 28)
    public void CastingTimeBonusActionParse2024Test() {
        final String castingTimeString = "Bonus action";
        final CastingTime castingTime = DisplayUtils.castingTimeFromString(context, castingTimeString);
        final CastingTime expected = new CastingTime(CastingTime.CastingTimeType.BONUS_ACTION);
        Truth.assertThat(castingTime).isEqualTo(expected);
    }

}
