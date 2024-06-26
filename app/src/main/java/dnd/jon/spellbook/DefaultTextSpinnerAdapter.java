package dnd.jon.spellbook;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

class DefaultTextSpinnerAdapter extends ArrayAdapter<String> {

    // Members values
    private final Context context;
    private final int layoutID;
    private final int labelID;
    private final String[] objects;
    private final String defaultText;
    private final String firstElement;
    private boolean defaultSet;

    public DefaultTextSpinnerAdapter(Context context, String[] objects, int layoutID, int labelID, String defaultText, boolean defaultSet) {
        super(context, layoutID, objects);
        this.context = context;
        this.objects = objects;
        this.firstElement = objects[0];
        this.layoutID = layoutID;
        this.labelID = labelID;
        this.defaultSet = defaultSet;
        this.defaultText = defaultText;

        //this.setDropDownViewResource(layoutID);

        // Set the default text, if necessary
        setDefault(defaultSet);

    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        if (defaultSet) {
            objects[0] = firstElement;
            defaultSet = false;
        }
        return getSpinnerRow(position, parent);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        notifyDataSetChanged();
        return getSpinnerRow(position, parent);
    }

    void setDefault(boolean b) {
        objects[0] = b ? defaultText : firstElement;
        defaultSet = b;
        notifyDataSetChanged();
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
