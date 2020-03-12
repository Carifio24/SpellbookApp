package dnd.jon.spellbook;

import org.javatuples.Tuple;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.reflect.Method;

class SpellbookUtils {

    static <T> T coalesce(@Nullable T one, T two) {
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

//    static <T extends Tuple> T tupleCopy(T t) {
//        try {
//            final List<Object> items = new ArrayList<>();
//            for (int i = 0; i < t.getSize(); ++i) {
//                items.add(t.getValue(i));
//            }
//            final Class<? extends Tuple> clazz = t.getClass();
//            final Method constructor = clazz.getMethod("fromCollection", Collection.class);
//            return (T) constructor.invoke(items);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//
//    }

//    static String firstLetterCapitalized(String s) {
//        return s.substring(0,1).toUpperCase() + s.substring(1);
//    }

}
