package dnd.jon.spellbook;

import java.util.Collection;

import static org.junit.Assert.*;

public class AndroidTestUtils {

    static <T> void assertCollectionsSameUnordered(Collection<T> collection1, Collection<T> collection2) {
        assertEquals(collection1.size(), collection2.size());
        assertTrue(collection1.containsAll(collection2));
        assertTrue(collection2.containsAll(collection1));
    }
}
