package dnd.jon.spellbook;

import android.content.Context;

public class NameDisplayableSpinnerAdapter<T extends NameDisplayable> extends NamedSpinnerAdapter<T> {
    NameDisplayableSpinnerAdapter(Context context, T[] items, int textSize) { super(context, items, DisplayUtils::getDisplayName, textSize); }
    NameDisplayableSpinnerAdapter(Context context, T[] items) { super(context, items, DisplayUtils::getDisplayName); }
}
