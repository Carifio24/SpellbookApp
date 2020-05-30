package dnd.jon.spellbook;

import android.content.Context;

class UnitTypeSpinnerAdapter<T extends Enum<T> & Unit> extends DisplayNameSpinnerAdapter<T> {
    UnitTypeSpinnerAdapter(Context context, Class<T> type, int textSize) { super(context, type, T::pluralName, textSize); }
    UnitTypeSpinnerAdapter(Context context, Class<T> type) { super(context, type, T::pluralName); }
}
