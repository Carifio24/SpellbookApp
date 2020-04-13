package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;

class SpellbookUtils {

    static final ArrayList<Character> illegalCharacters = new ArrayList<>(Arrays.asList('\\', '/', '.'));

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

//    static String firstLetterCapitalized(String s) {
//        return s.substring(0,1).toUpperCase() + s.substring(1);
//    }

    static <T extends Enum<T>> void setNamedSpinnerByItem(Spinner spinner, T item) {
        try {
            final NamedSpinnerAdapter<T> adapter = (NamedSpinnerAdapter<T>) spinner.getAdapter();
            final int index = adapter.itemIndex(item);
            spinner.setSelection(index);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }
}
