package dnd.jon.spellbook;

import java.util.Collection;

import static org.junit.Assert.*;
import android.view.View;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class AndroidTestUtils {

    static <T> void assertCollectionsSameUnordered(Collection<T> collection1, Collection<T> collection2) {
        assertEquals(collection1.size(), collection2.size());
        assertTrue(collection1.containsAll(collection2));
        assertTrue(collection2.containsAll(collection1));
    }

    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }
}
