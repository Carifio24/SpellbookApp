package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import dnd.jon.spellbook.databinding.YesNoBinding;

public class DeleteCharacterDialog extends DialogFragment {

    private String name;
    private FragmentActivity activity;
    private CharacterProfileViewModel viewModel;

    static final String nameKey = "Name";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // The character name
        name = getArguments() != null ? getArguments().getString(nameKey) : "";

        // The activity and view model
        activity = requireActivity();
        viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory())
                .get(CharacterProfileViewModel.class);

        // Create the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        final YesNoBinding binding = YesNoBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // Set the title
        final TextView title = binding.yesNoTitle;
        final String titleText = activity.getString(R.string.confirm);
        title.setText(titleText);

        // Set the message
        final TextView message = binding.yesNoMessage;;
        final String messageText = activity.getString(R.string.delete_character_confirm, name);
        message.setText(messageText);

        // The listener to delete; for the yes button
        final View.OnClickListener yesListener = (v) -> {
            final boolean deleted = viewModel.deleteProfileByName(name);
            final int toastMessageID = deleted ? R.string.character_deleted : R.string.error_deleting_character;
            final String toastMessage =  activity.getString(toastMessageID, name);
            Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show();
            this.dismiss();
        };

        // The listener to cancel; for the no button
        final View.OnClickListener noListener = (v) -> this.dismiss();

        // Set the button listeners
        binding.yesButton.setOnClickListener(yesListener);
        binding.noButton.setOnClickListener(noListener);

        // Return the dialog
        return builder.create();
    }

}
