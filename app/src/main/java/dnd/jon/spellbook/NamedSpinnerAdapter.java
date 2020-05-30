package dnd.jon.spellbook;

import android.content.Context;

class NamedSpinnerAdapter<T extends Enum<T> & Named> extends DisplayNameSpinnerAdapter<T> {
    NamedSpinnerAdapter(Context context, Class<T> type, int textSize) { super(context, type, T::getDisplayName, textSize); }
    NamedSpinnerAdapter(Context context, Class<T> type) { super(context, type, T::getDisplayName); }
}
