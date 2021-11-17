package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import dnd.jon.spellbook.databinding.SaveSortFilterStatusBinding;

public class SaveSortFilterStatusDialog extends DialogFragment {

    private FragmentActivity activity;
    private SpellbookViewModel viewModel;
    private SaveSortFilterStatusBinding binding;
    private SortFilterStatus status;

    static final String STATUS_KEY = "status";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = requireActivity();
        viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory())
                .get(SpellbookViewModel.class);

        // Get arguments
        final Bundle args = getArguments();
        if (args != null && args.containsKey(STATUS_KEY)) {
            status = args.getParcelable(STATUS_KEY);
        }

        // Create the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        binding = SaveSortFilterStatusBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // Create the status saving listener
        binding.saveStatusButton.setOnClickListener((button) -> {

            // Get the name from the EditText
            final EditText et = binding.saveSortFilterStatusEditText;
            final String name = et.getText().toString();

            // Check that the name is valid
            final String error = viewModel.statusNameValidator(name);
            if (!error.isEmpty()) {
                setErrorMessage(error);
                return;
            }

            // Save the status
            viewModel.saveSortFilterStatus(status, name);
        });

        return builder.create();
    }

    private void setErrorMessage(String error) {
        final TextView tv = binding.saveSortFilterStatusMessage;
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        tv.setTextColor(Color.RED);
        tv.setText(error);
    }

}
