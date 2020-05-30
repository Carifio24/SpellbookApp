package dnd.jon.spellbook;

import android.content.Context;
import android.widget.TextView;

import java.util.Locale;

class AndroidUtils {

    static String stringFromID(Context context, int stringID) { return context.getResources().getString(stringID); }
    static float dimensionFromID(Context context, int dimensionID) { return context.getResources().getDimension(dimensionID); }

    static void setNumberText(TextView tv, int number) {
        tv.setText(String.format(Locale.US, "%d", number));
    }




}
