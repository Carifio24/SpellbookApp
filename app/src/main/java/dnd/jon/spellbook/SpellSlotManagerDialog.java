package dnd.jon.spellbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class SpellSlotManagerDialog extends DialogFragment {

    private Activity activity;
    private SpellSlotStatus status;

    static final String statusKey = "name";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            status = args.getParcelable(statusKey);
        }

        activity = requireActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.spell_slot_manager, null);
        builder.setView(view);

        return builder.create();
    }

    LinearLayout setupRow(int level) {
        final LinearLayout linearLayout = new LinearLayout(activity);
        final int total = status.getTotalSlots(level);
        final int available = status.getAvailableSlots(level);
        final int usedSlots = total - available;
        for (int i = 0; i < total; i++) {
            final CheckBox checkBox = new CheckBox(activity);
            checkBox.setChecked(i < usedSlots);
            checkBox.setTag(level);
            linearLayout.addView(checkBox);
        }
        return linearLayout;
    }

}
