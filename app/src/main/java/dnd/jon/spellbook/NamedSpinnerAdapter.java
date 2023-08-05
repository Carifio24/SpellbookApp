package dnd.jon.spellbook;

import android.content.Context;

import java.util.function.BiFunction;

class NamedSpinnerAdapter<T extends Enum<T>> extends DefaultSpinnerAdapter<T> {

    private static String[] objects = null;

    // Member values
    private final Class<T> type;

    NamedSpinnerAdapter(Context context, Class<T> type, BiFunction<Context,T,String> namingFunction, int textSize) {
        super(context, type.getEnumConstants(), namingFunction, textSize);
        this.type = type;
    }

    NamedSpinnerAdapter(Context context, Class<T> type, BiFunction<Context,T,String> namingFunction) { this(context, type, namingFunction,12); }

}