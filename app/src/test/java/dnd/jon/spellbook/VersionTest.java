package dnd.jon.spellbook;

import org.junit.Test;
import com.google.common.truth.Truth;

public class VersionTest {
    @Test
    public void fromString_CorrectParse_ReturnsVersion() {
        final Version v = Version.fromString("2.11.0");
        Truth.assertThat(v).isNotNull();
        Truth.assertThat(v).isEqualTo(new Version(2,11,0));
    }

    @Test
    public void fromString_CorrectParse_ReturnsNull() {
        final Version v = Version.fromString("2.11.0.5");
        Truth.assertThat(v).isNull();
    }

    @Test
    public void fromString_CorrectParse_ReturnsNull2() {
        final Version v = Version.fromString("Garbage");
        Truth.assertThat(v).isNull();
    }

    @Test
    public void compareMajor() {
        final Version v1 = new Version(2,11,0);
        final Version v2 = new Version(3,2,1);
        Truth.assertThat(v1).isLessThan(v2);
    }

    @Test
    public void compareMinor() {
        final Version v1 = new Version(1,2,3);
        final Version v2 = new Version(1,1,7);
        Truth.assertThat(v2).isLessThan(v1);
    }

    @Test public void comparePatch() {
        final Version v1 = new Version(3,4,5);
        final Version v2 = new Version(3,4,3);
        Truth.assertThat(v1).isGreaterThan(v2);
    }
}
