package dnd.jon.spellbook;

import com.google.common.truth.Correspondence;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TestUtils {

    static <T> boolean sameElements(Collection<T> collection1, Collection<T> collection2) {
        final Set<T> set1 = new HashSet<>(collection1);
        final Set<T> set2 = new HashSet<>(collection2);
        return set1.equals(set2);
    }

//    static <T> boolean sameElements(T[] array, Collection<T> collection) {
//        return sameElements(Arrays.asList(array), collection);
//    }
//
//    static <T> boolean sameElements(Collection<T> collection, T[] array) {
//        return sameElements(array, collection);
//    }
//
//    static <T> boolean sameElements(T[] array1, T[] array2) {
//        return sameElements(array1, Arrays.asList(array2));
//    }

    static <T> Correspondence<Collection<T>, Collection<T>> haveSameElements() {
        return Correspondence.from(TestUtils::sameElements, "Check if two collections have the same elements");
    }

    static final Correspondence<Collection<Source>, Collection<Source>> haveSameSources = haveSameElements();

}
