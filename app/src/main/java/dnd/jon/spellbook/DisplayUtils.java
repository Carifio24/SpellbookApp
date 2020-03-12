package dnd.jon.spellbook;

import android.content.Context;
import android.util.TypedValue;

final class DisplayUtils {

    static float dpToPx(Context context, float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

}
