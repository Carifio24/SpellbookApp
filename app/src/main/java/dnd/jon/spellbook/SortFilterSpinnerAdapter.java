package dnd.jon.spellbook;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

class SortFilterSpinnerAdapter extends ArrayAdapter<String> {

    // Static member values
    private static final int layoutID =  R.layout.spinner_item;
    private static final int labelID = R.id.spinner_row_text_view;

    // Member values
    private Context context;
    private String[] objects;

    SortFilterSpinnerAdapter(Context context, String[] objects) {
        super(context, layoutID, objects);
        this.context = context;
        this.objects = objects;
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
        label.setText(objects[position]);
        label.setGravity(Gravity.CENTER);
        return row;
    }

}
