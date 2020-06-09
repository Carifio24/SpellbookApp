package dnd.jon.spellbook;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;

class AndroidUtils {

    static String stringFromID(Context context, int stringID) { return context.getResources().getString(stringID); }
    static int integerFromID(Context context, int integerID) { return context.getResources().getInteger(integerID); }
    static float dimensionFromID(Context context, int dimensionID) { return context.getResources().getDimension(dimensionID); }

    static void setNumberText(TextView tv, int number) {
        tv.setText(String.format(Locale.US, "%d", number));
    }

    static <T> void setSpinnerByItem(Spinner spinner, T item) {
        try {
            final ArrayAdapter<T> adapter = (ArrayAdapter<T>) spinner.getAdapter();
            spinner.setSelection(adapter.getPosition(item));
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }



}
