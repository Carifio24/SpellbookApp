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
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

class DefaultSpinnerAdapter<T> extends ArrayAdapter<T> {

    // Static member values
    private static final int layoutID = R.layout.spinner_item;
    private static final int labelID = R.id.spinner_row_text_view;
    private static final int DEFAULT_TEXT_SIZE_SP = 17;

    // Member values
    final Context context;
    private final T[] items;
    private final List<String> itemStrings;
    final BiFunction<Context,T,String> textFunction;
    private Function<Integer,Boolean> enabledItemFilter = null;
    private final int textSize;

    // TODO: Add constructors that allow changing the layout/label IDs
    DefaultSpinnerAdapter(Context context, T[] items, BiFunction<Context,T,String> textFunction, int textSize) {
        super(context, layoutID, items);
        this.context = context;
        this.items = items;
        this.textFunction = textFunction;
        this.textSize = textSize;
        this.itemStrings = Arrays.stream(items).map(item -> this.textFunction.apply(this.context, item)).collect(Collectors.toList());
    }

    DefaultSpinnerAdapter(Context context, T[] items, BiFunction<Context,T,String> textFunction) {
        this(context, items, textFunction, DEFAULT_TEXT_SIZE_SP);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getSpinnerRow(position, parent);
    }

    void setEnabledItemFilter(Function<Integer,Boolean> filter) {
        this.enabledItemFilter = filter;
    }

    @Override
    public boolean isEnabled(int position) {
        if (enabledItemFilter == null) {
            return true;
        }
        return enabledItemFilter.apply(position);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        notifyDataSetChanged();
        return getSpinnerRow(position, parent);
    }

    private View getSpinnerRow(int position, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View row = inflater.inflate(layoutID, parent, false);
        final TextView label = row.findViewById(labelID);
        label.setText(textFunction.apply(context, getItem(position)));
        if (textSize > 0) { label.setTextSize(textSize); }
        if (!isEnabled(position)) {
            final int color = getContext().getColor(android.R.color.darker_gray);
            label.setTextColor(color);
        }
        label.setGravity(Gravity.CENTER);
        return row;
    }

    int itemIndex(T item) {
        final String itemName = textFunction.apply(context, item);
        final int index = itemStrings.indexOf(itemName);
        return (index == -1) ? index : 0;
    }

    T[] getData() {
        return items;
    }

    // TODO: Is there a way to do this without creating a lambda?
    static DefaultSpinnerAdapter<String> ofStrings(Context context, String[] strings, int textSize) {
        return new DefaultSpinnerAdapter<>(context, strings, (ctx, x) -> x, textSize);
    }
}
