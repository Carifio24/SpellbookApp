package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import dnd.jon.spellbook.databinding.CharacterCreationBinding;

public class CreateCharacterDialog extends DialogFragment {

    private CharacterProfile baseProfile;
    private FragmentActivity activity;
    private CharacterCreationBinding binding;
    private SpellbookViewModel viewModel;

    static final String PROFILE_KEY = "profile";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // The main activity
        activity = requireActivity();

        viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory())
                .get(SpellbookViewModel.class);

        // Get arguments
        // Used for if we're duplicating a character
        final Bundle args = getArguments();
        if (args != null && args.containsKey(PROFILE_KEY)) {
            baseProfile = args.getParcelable(PROFILE_KEY);
        } else {
            baseProfile = null;
        }

        // Create the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        binding = CharacterCreationBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // Create the character creation listener
        View.OnClickListener createListener = (View v) -> {

            // The number of current characters
            List<String> characters = viewModel.currentCharacterNames().getValue();
            int nChars = characters != null ? characters.size() : 0;

            // Get the name from the EditText
            EditText et = binding.creationEditText;
            String name = et.getText().toString();

            // Check that the character name is valid
            final String error = viewModel.characterNameValidator(name);
            if (!error.isEmpty()) {
                setErrorMessage(error);
                return;
            }

            // Create the new character profile
            // using the given base profile, if we have one
            final boolean duplicating = (baseProfile != null);
            CharacterProfile profile;
            if (duplicating) {
                profile = baseProfile.duplicate();
                profile.setName(name);
            } else {
                profile = new CharacterProfile(name);
            }
            viewModel.saveProfile(profile);

            // Display a Toast message indicating character creation
            final String toastString = duplicating ?
                    activity.getString(R.string.character_duplicated_toast, name, baseProfile.getName()) :
                    activity.getString(R.string.character_created_toast, name);
            Toast.makeText(activity, toastString, Toast.LENGTH_SHORT).show();

            //Set it as the current profile if there are no others
            if (nChars == 0) {
                viewModel.setProfile(profile);
            }
            this.dismiss();
        };

        // Create the cancel listener
        View.OnClickListener cancelListener = (View view) -> this.dismiss();

        // Set the button listeners
        binding.createButton.setOnClickListener(createListener);
        binding.cancelButton.setOnClickListener(cancelListener);

        // The dialog
        AlertDialog alert = builder.create();

        // If there are no characters, we make sure that the window cannot be exited
        final List<String> updatedNames = viewModel.currentCharacterNames().getValue();
        if (updatedNames == null || updatedNames.size() == 0) {
            binding.cancelButton.setVisibility(View.GONE);
            setCancelable(false);
            alert.setCanceledOnTouchOutside(false);
        }

        // Return the dialog
        return alert;

    }

    private void setErrorMessage(String error) {
        final TextView tv = binding.creationMessage;
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        tv.setTextColor(Color.RED);
        tv.setText(error);
    }

}
