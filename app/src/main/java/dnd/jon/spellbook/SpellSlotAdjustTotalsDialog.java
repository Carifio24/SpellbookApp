package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Locale;

import dnd.jon.spellbook.databinding.SpellSlotAdjustTotalItemBinding;
import dnd.jon.spellbook.databinding.SpellSlotAdjustTotalsBinding;

public class SpellSlotAdjustTotalsDialog extends DialogFragment {

    private SpellSlotStatus status;
    static final String SPELL_SLOT_STATUS_KEY = "SpellSlotStatus";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the arguments
        final Bundle args = getArguments();

        // Unpack the spell slot status
        status = args.getParcelable(SPELL_SLOT_STATUS_KEY);

        // Get the main activity
        final FragmentActivity activity = requireActivity();

        // Create the dialog builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        final SpellSlotAdjustTotalsBinding binding = SpellSlotAdjustTotalsBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        if (status == null) {
            return builder.create();
        }
        binding.setStatus(status);

        // Individual level adjustment items
        final SpellSlotAdjustTotalItemBinding[] itemBindings = new dnd.jon.spellbook.databinding.SpellSlotAdjustTotalItemBinding[]{
            binding.adjustSlotsLevel1,
            binding.adjustSlotsLevel2,
            binding.adjustSlotsLevel3,
            binding.adjustSlotsLevel4,
            binding.adjustSlotsLevel5,
            binding.adjustSlotsLevel6,
            binding.adjustSlotsLevel7,
            binding.adjustSlotsLevel8,
            binding.adjustSlotsLevel9
        };

        for (int i = 0; i < itemBindings.length; i++) {
            final SpellSlotAdjustTotalItemBinding item = itemBindings[i];
            final EditText editText = item.spellSlotAdjustTotalEditText;
            final int level = i + 1;
            editText.setOnFocusChangeListener( (v, hasFocus) -> {
                if (hasFocus ||
                    item.getStatus() == null ||
                    !(v instanceof EditText)) { return; }
                final EditText et = (EditText) v;
                int value;
                try {
                    value = Integer.parseInt(et.getText().toString());
                } catch (NumberFormatException nfe) {
                    value = 0;
                    et.setText(String.format(Locale.getDefault(), "%d", value));
                }
                status.setTotalSlots(level, value);
            });
        }

        // Create the dialog and return
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }
}
