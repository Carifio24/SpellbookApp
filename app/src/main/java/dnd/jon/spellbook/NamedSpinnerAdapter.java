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

public class NamedSpinnerAdapter<T> extends ArrayAdapter<T> {

    // Static member values
    static final int layoutID = R.layout.spinner_item;
    static final int labelID = R.id.spinner_row_text_view;
    String[] objects = null;

    // Member values
    final Context context;
    private final T[] items;
    final BiFunction<Context,T,String> namingFunction;
    private final int textSize;

    NamedSpinnerAdapter(Context context, T[] items, BiFunction<Context,T,String> namingFunction, int textSize) {
        super(context, layoutID, items);
        this.context = context;
        this.items = items;
        this.textSize = textSize;
        this.namingFunction = namingFunction;
    }

    NamedSpinnerAdapter(Context context, T[] items, BiFunction<Context,T,String> namingFunction) {
        this(context, items, namingFunction, 12);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getSpinnerRow(position, parent);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        notifyDataSetChanged();
        return getSpinnerRow(position, parent);
    }

    private View getSpinnerRow(int position, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(layoutID, parent, false);
        TextView label = row.findViewById(labelID);
        label.setText(namingFunction.apply(context, getItem(position)));
        label.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        if (textSize > 0) { label.setTextSize(textSize); }
        label.setGravity(Gravity.CENTER);
        return row;
    }

    int itemIndex(T item) {
        final String itemName = namingFunction.apply(context, item);
        final int index = Arrays.asList(objects).indexOf(itemName);
        return (index != -1) ? index : 0;
    }

    String[] getNames() {
        if (objects == null) {
            objects = DisplayUtils.getDisplayNames(context, items, namingFunction);
        }
        return (objects == null) ? new String[0] : Arrays.copyOf(objects, objects.length);
    }

    T[] getData() { return items; }
}
