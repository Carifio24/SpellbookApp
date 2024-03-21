package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import dnd.jon.spellbook.databinding.MessageDialogBinding;

class SpellbookUtils {

    static <T> T coalesce(@Nullable T one, @NonNull T two) {
        return one != null ? one : two;
    }

    static final int defaultColor = Color.argb(138, 0, 0, 0);

    @FunctionalInterface
    public interface ThrowsExceptionFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    static Integer intParse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
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
        return yn ? "yes" : "no";
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

    static <T> T[] jsonToArray(JSONArray jarr, Class<T> elementType, BiFunction<JSONArray, Integer, T> itemGetter) {
        final T[] arr = (T[]) Array.newInstance(elementType, jarr.length());
        for (int i = 0; i < jarr.length(); ++i) {
            arr[i] = itemGetter.apply(jarr, i);
        }
        return arr;
    }

//    static String firstLetterCapitalized(String s) {
//        return s.substring(0,1).toUpperCase() + s.substring(1);
//    }

    static <T extends Enum<T>> void setNamedSpinnerByItem(Spinner spinner, T item) {
        try {
            final NamedEnumSpinnerAdapter<T> adapter = (NamedEnumSpinnerAdapter<T>) spinner.getAdapter();
            spinner.setSelection(adapter.itemIndex(item));
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    static void clickButtons(Collection<ToggleButton> buttons, Function<ToggleButton, Boolean> filter) {
        if (buttons == null) {
            return;
        }
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
            case 3:
                return "3rd";
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

    static void showMessageDialog(Context context, int titleID, int messageID, boolean mustPressOK, Runnable onDismissAction) {
        // Create the dialog builder
        final AlertDialog.Builder b = new AlertDialog.Builder(context);

        // Inflate the view and set the builder to use this view
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final MessageDialogBinding binding = MessageDialogBinding.inflate(inflater);
        b.setView(binding.getRoot());

        // Set the title and message
        binding.messageDialogTitle.setText(titleID);
        binding.messageDialogMessage.setText(messageID);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
//            binding.messageDialogMessage.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));
//        } else{
//            binding.messageDialogMessage.setText(HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY));
//        }

        // The dialog itself
        final Dialog dialog = b.create();

        // When we press ok, the dialog should finish
        binding.okButton.setOnClickListener((v) -> {
            dialog.dismiss();
        });

        // Set whether or not we must press the OK button
        dialog.setCancelable(!mustPressOK);
        dialog.setCanceledOnTouchOutside(!mustPressOK);
        dialog.setOnDismissListener((di) -> {
            if (onDismissAction != null) {
                onDismissAction.run();
            }
        });

        dialog.show();
    }

    static <K, V> Map<K, V> copyOfMap(Map<K, V> map, Class<K> keyType) {
        if (keyType.isEnum()) {
            return new EnumMap(map);
        } else {
            return new HashMap<>(map);
        }
    }

    static <T> T[] concatenateAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    static <T> T[] concatenateAll(List<T[]> arrays) {
        if (arrays.size() <= 0) {
            return null;
        }
        T[] first = arrays.get(0);
        int totalLength = arrays.stream().map(array -> array.length).reduce(0, Integer::sum);
        T[] result = Arrays.copyOf(first, totalLength);

        int offset = first.length;
        for (int i = 1; i < arrays.size(); i++) {
            T[] array = arrays.get(i);
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    static <T> T[] arrayDifference(Class<T> type, T[] array, T[] remove) {
        final Set<T> arrayAsSet = new HashSet<>(Arrays.asList(array));
        final Set<T> removeSet = new HashSet<>(Arrays.asList(remove));
        arrayAsSet.removeAll(removeSet);
        return arrayAsSet.toArray((T[]) Array.newInstance(type, arrayAsSet.size()));
    }

    static <T> Collection<T> complement(Collection<T> items, Collection<T> allItems) {
        return allItems.stream().filter(t -> !items.contains(t)).collect(Collectors.toList());
    }

    static <T> Collection<T> complement(Collection<T> items, T[] allItems) {
        return complement(items, Arrays.asList(allItems));
    }

    static <T extends Enum<T>> Collection<T> complement(Collection<T> items, Class<T> type) {
        return complement(items, type.getEnumConstants());
    }

    static <T> Collection<T> mutableCollectionFromArray(T[] items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    static boolean filenameEndsWith(File file, String extension) {
        return file.getName().endsWith(extension);
    }

    static Predicate<File> extensionFilter(String extension) {
        return (file) -> file.getName().endsWith(extension);
    }

    static <U extends Unit> Unit defaultUnit(Class<U> unitType) {
        if (unitType == TimeUnit.class) {
            return TimeUnit.SECOND;
        } else if (unitType == LengthUnit.class) {
            return LengthUnit.FOOT;
        }
        return null;

    }

    public static <T> T[] removeElement(Class<T> type, T[] items, T element) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == element) {
                final T[] copy = (T[]) Array.newInstance(type, items.length - 1);
                System.arraycopy(items, 0, copy, 0, i);
                System.arraycopy(items, i + 1, copy, i, items.length - i - 1);
                return copy;
            }
        }
        return items;
    }

}

