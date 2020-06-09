package dnd.jon.spellbook;

import android.content.Context;

class NamedEnumSpinnerAdapter<T extends Enum<T> & Named> extends DisplayNameSpinnerAdapter<T> {
    NamedEnumSpinnerAdapter(Context context, Class<T> type, int textSize) { super(context, type.getEnumConstants(), T::getDisplayName, textSize); }
    NamedEnumSpinnerAdapter(Context context, Class<T> type) { super(context, type.getEnumConstants(), T::getDisplayName); }
}
