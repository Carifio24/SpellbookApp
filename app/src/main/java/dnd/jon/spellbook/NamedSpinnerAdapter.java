package dnd.jon.spellbook;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.function.BiFunction;

class NamedSpinnerAdapter<T extends Enum<T>> extends DefaultSpinnerAdapter<T> {

    NamedSpinnerAdapter(Context context, Class<T> type, BiFunction<Context,T,String> namingFunction, int textSize) {
        super(context, type.getEnumConstants(), namingFunction, textSize);
   }

    NamedSpinnerAdapter(Context context, Class<T> type, BiFunction<Context,T,String> namingFunction) { this(context, type, namingFunction,12); }

}
