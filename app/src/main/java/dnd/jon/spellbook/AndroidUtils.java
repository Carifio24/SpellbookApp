package dnd.jon.spellbook;

import android.app.Activity;
import android.util.DisplayMetrics;

import org.javatuples.Pair;

class AndroidUtils {
    static Pair<Integer,Integer> screenDimensions(Activity activity) {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new Pair<>(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }
}
