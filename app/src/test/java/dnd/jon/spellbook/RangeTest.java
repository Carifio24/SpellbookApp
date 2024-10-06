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
public class RangeTest {

    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getContext();
    }

    @Test
    @Config(sdk = 34)
    public void RangeSpecialParseTest() {
        final String rangeString = "Special";
        final Range range = DisplayUtils.rangeFromString(context, rangeString);
        final Range expected = new Range(Range.RangeType.SPECIAL);
        Truth.assertThat(range).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void RangeSelfParseTest() {
        final String rangeString = "Self";
        final Range range = DisplayUtils.rangeFromString(context, rangeString);
        final Range expected = new Range(Range.RangeType.SELF);
        Truth.assertThat(range).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void RangeSelfParseTest2() {
        final String rangeString = "Self (15 foot radius)";
        final Range range = DisplayUtils.rangeFromString(context, rangeString);
        final Range expected = new Range(Range.RangeType.SELF, 15, LengthUnit.FOOT);
        Truth.assertThat(range).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void RangeSelfParseTest3() {
        final String rangeString = "Self (15-foot cone)";
        final Range range = DisplayUtils.rangeFromString(context, rangeString);
        final Range expected = new Range(Range.RangeType.SELF, 15, LengthUnit.FOOT);
        Truth.assertThat(range).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void RangeTouchParseTest() {
        final String rangeString = "Touch";
        final Range range = DisplayUtils.rangeFromString(context, rangeString);
        final Range expected = new Range(Range.RangeType.TOUCH);
        Truth.assertThat(range).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void RangeSightParseTest() {
        final String rangeString = "Sight";
        final Range range = DisplayUtils.rangeFromString(context, rangeString);
        final Range expected = new Range(Range.RangeType.SIGHT);
        Truth.assertThat(range).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void RangeUnlimitedParseTest() {
        final String rangeString = "Unlimited";
        final Range range = DisplayUtils.rangeFromString(context, rangeString);
        final Range expected = new Range(Range.RangeType.UNLIMITED);
        Truth.assertThat(range).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void RangeFiniteParseTest() {
        final String rangeString = "1 foot";
        final Range range = DisplayUtils.rangeFromString(context, rangeString);
        final Range expected = new Range(Range.RangeType.RANGED, 1, LengthUnit.FOOT);
        Truth.assertThat(range).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void RangeFiniteParseTest2() {
        final String rangeString = "2 feet";
        final Range range = DisplayUtils.rangeFromString(context, rangeString);
        final Range expected = new Range(Range.RangeType.RANGED, 2, LengthUnit.FOOT);
        Truth.assertThat(range).isEqualTo(expected);
    }


    @Test
    @Config(sdk = 34)
    public void RangeFiniteParseTest3() {
        final String rangeString = "1106 meters";
        final Range range = DisplayUtils.rangeFromString(context, rangeString);
        final Range expected = new Range(Range.RangeType.RANGED, 1106, LengthUnit.METER);
        Truth.assertThat(range).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void RangeFiniteParseTest4() {
        final String rangeString = "3 kilometers";
        final Range range = DisplayUtils.rangeFromString(context, rangeString);
        final Range expected = new Range(Range.RangeType.RANGED, 3, LengthUnit.KILOMETER);
        Truth.assertThat(range).isEqualTo(expected);
    }

    @Test
    @Config(sdk = 34)
    public void RangeFiniteParseTest5() {
        final String rangeString = "6 miles";
        final Range range = DisplayUtils.rangeFromString(context, rangeString);
        final Range expected = new Range(Range.RangeType.RANGED, 6, LengthUnit.MILE);
        Truth.assertThat(range).isEqualTo(expected);
    }

}
