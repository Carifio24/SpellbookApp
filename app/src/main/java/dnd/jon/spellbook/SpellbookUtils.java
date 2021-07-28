package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.LocaleList;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.Spinner;

import org.json.JSONArray;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

import dnd.jon.spellbook.databinding.MessageDialogBinding;

class SpellbookUtils {

    static <T> T coalesce(@Nullable T one, @NonNull T two) {
        return one != null ? one : two;
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

    static String bool_to_yn(boolean yn) { return yn ? "yes" : "no"; }

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

    static <T extends Enum<T>> void setNamedSpinnerByItem(Spinner spinner, T item) {
        try {
            final NamedSpinnerAdapter<T> adapter = (NamedSpinnerAdapter<T>) spinner.getAdapter();
            spinner.setSelection(adapter.itemIndex(item));
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

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
        dialog.setCancelable(!mustPressOK) ;
        dialog.setCanceledOnTouchOutside(!mustPressOK);
        dialog.setOnDismissListener((di) -> {
            if (onDismissAction != null) {
                onDismissAction.run();
            }
        });

        dialog.show();
    }

}
