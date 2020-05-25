package dnd.jon.spellbook;

import android.content.Context;

class NameDisplayableSpinnerAdapter<T extends Enum<T> & Named> extends NamedSpinnerAdapter<T> {
    NameDisplayableSpinnerAdapter(Context context, Class<T> type, int textSize) { super(context, type, T::getDisplayName, textSize); }
    NameDisplayableSpinnerAdapter(Context context, Class<T> type) { super(context, type, T::getDisplayName); }
}
