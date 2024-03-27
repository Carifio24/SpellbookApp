package dnd.jon.spellbook;

import android.content.Context;

class NameDisplayableEnumSpinnerAdapter<T extends Enum<T> & NameDisplayable> extends NamedEnumSpinnerAdapter<T> {
    NameDisplayableEnumSpinnerAdapter(Context context, Class<T> type, int textSize) { super(context, type, DisplayUtils::getDisplayName, textSize); }
    NameDisplayableEnumSpinnerAdapter(Context context, Class<T> type) { super(context, type, DisplayUtils::getDisplayName); }
}