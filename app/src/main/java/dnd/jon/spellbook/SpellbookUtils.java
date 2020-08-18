package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.json.JSONArray;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

class SpellbookUtils {

    static final List<Character> illegalCharacters = new ArrayList<>(Arrays.asList('\\', '/', '.'));

    static <T> T coalesce(@Nullable T one, @NonNull T two) {
        return one != null ? one : two;
    }


    static boolean yn_to_bool(String yn) throws Exception {
        if (yn.equals("no")) {
            return false;
        } else if (yn.equals("yes")) {
            return true;
        } else {
            throw new Exception("String must be yes or no");
        }
    }

    static String bool_to_yn(boolean yn) {
        if (yn) {
            return "yes";
        } else {
            return "no";
        }
    }

    static int parseFromString(final String s, final int defaultValue) {
        int x;
        try {
            x = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            x = defaultValue;
        }
        return x;
    }

    static <T> T[] jsonToArray(JSONArray jarr, Class<T> elementType, BiFunction<JSONArray,Integer,T> itemGetter) {
        final T[] arr = (T[]) Array.newInstance(elementType, jarr.length());
        for (int i = 0; i < jarr.length(); ++i) {
            arr[i] = itemGetter.apply(jarr, i);
        }
        return arr;
    }

    //    static String firstLetterCapitalized(String s) {
    //        return s.substring(0,1).toUpperCase() + s.substring(1);
    //    }

    static void clickButtons(Collection<ToggleButton> buttons, Function<ToggleButton,Boolean> filter) {
        if (buttons == null) { return; }
        for (ToggleButton tb : buttons) {
            if (filter.apply(tb)) {
                tb.callOnClick();
            }
        }
    }

    static String ordinal(int value) {
        switch (value) {
            case 1:
                return "1st";
            case 2:
                return "2nd";
            default:
                return value + "th";
        }
    }

    static String stackTrace(Exception e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    static <T> IntFunction<T[]> arrayGenerator(Class<T> type) {
        return (int n) -> (T[]) Array.newInstance(type, n);
    }

    private static <E extends Enum<E>> EnumSet<E> makeEnumSet(Class<E> type, Predicate<E> filter) {
        final E[] values = type.getEnumConstants();
        if (values == null) { return EnumSet.noneOf(type); }
        if (filter == null) { return EnumSet.allOf(type); }
        final EnumSet<E> set = EnumSet.noneOf(type);
        for (E e : values) {
            if (filter.test(e)) {
                set.add(e);
            }
        }
        return set;
    }

    private static <E extends Enum<E>> EnumSet<E> makeEnumSet(Class<E> type) { return makeEnumSet(type, null); }

    static Collection<Character> illegalCharactersCheck(String s) {
        final Collection<Character> illegalCharsFound = new TreeSet<>();
        for (int i = 0; i < s.length(); ++i) {
            final Character c = s.charAt(i);
            if (illegalCharacters.contains(c)) {
                illegalCharsFound.add(c);
            }
        }
        return illegalCharsFound;
    }

}
