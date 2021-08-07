package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import dnd.jon.spellbook.databinding.NameChangeBinding;

public class NameChangeDialog extends DialogFragment {

    private FragmentActivity activity;
    private String originalName;
    private EditText editText;
    private NameChangeBinding binding;
    private CharacterProfileViewModel viewModel;

    static final String nameKey = "name";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        // The character name
        final Bundle args = getArguments();
        if (args != null) {
            originalName = args.getString(nameKey);
        } else {
            // TODO: Add a Toast error message
            this.dismiss();
        }

        // The activity and the view model
        activity = requireActivity();
        viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory())
                .get(CharacterProfileViewModel.class);

        // Create the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        binding = NameChangeBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // We want to start with the original name in the text field
        // When it gets focus, everything is selected
        editText = binding.nameChangeEditText;
        editText.setText(originalName);
        editText.setSelectAllOnFocus(true);

        // Create the name change listener
        View.OnClickListener changeListener = (View v) -> {

            // Get the newly-entered name
            final String newName = editText.getText().toString();

            // Is this a valid name?
            final String error = viewModel.characterNameValidator(newName);
            if (!error.isEmpty()) {
                setErrorMessage(error);
                return;
            }

            // If it's the same as the current name
            if (newName.equals(originalName)) {
                setErrorMessage(activity.getString(R.string.same_name));
                return;
            }

            // Otherwise, change the character profile
            // Save the new one, and delete the old
            final CharacterProfile profile = viewModel.getProfileByName(originalName);
            if (profile != null) {
                profile.setName(newName);
                final boolean saved = viewModel.saveProfile(profile);
                final boolean deleted = saved && viewModel.deleteProfileByName(originalName);
                if (!(saved && deleted)) {
                    Toast.makeText(activity, activity.getString(R.string.name_change_error), Toast.LENGTH_SHORT).show();
                }
            } else {
                // TODO: Toast error message
            }
            this.dismiss();

        };

        // Create the cancel listener
        View.OnClickListener cancelListener = (View view) -> this.dismiss();

        // Set the button listeners
        binding.nameChangeApproveButton.setOnClickListener(changeListener);
        binding.nameChangeCancelButton.setOnClickListener(cancelListener);

        // Return the dialog
        return builder.create();

    }

    private void setErrorMessage(String error) {
        final TextView tv = binding.nameChangeMessage;
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        tv.setTextColor(Color.RED);
        tv.setText(error);
    }

}
