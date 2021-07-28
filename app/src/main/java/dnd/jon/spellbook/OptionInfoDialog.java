package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import dnd.jon.spellbook.databinding.OptionInfoDialogBinding;

public class OptionInfoDialog extends DialogFragment {

    static final String TITLE_KEY = "title";
    static final String DESCRIPTION_KEY = "description";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the activity
        final Context context = requireContext();

        // Create the dialog builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Get the title and message
        final Bundle args = getArguments();
        final String title = args.getString(TITLE_KEY);
        final String description = args.getString(DESCRIPTION_KEY);

        // Inflate the view and set the builder to use this view
        final OptionInfoDialogBinding binding = OptionInfoDialogBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // Set the binding arguments
        binding.setTitle(title);
        binding.setDescription(description);
        binding.executePendingBindings();

        // Return the created dialog
        return builder.create();
    }
}
