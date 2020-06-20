package dnd.jon.spellbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import dnd.jon.spellbook.databinding.YesNoBinding;
import dnd.jon.spellbook.databinding.YesNoFilterViewBinding;

public class DeleteCharacterDialog extends DialogFragment {

    private String name;
    private SpellbookViewModel spellbookViewModel;

    static final String nameKey = "Name";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        // The character name
        name = getArguments().getString(nameKey);

        // The activity and the ViewModel
        final Activity activity = requireActivity();
        spellbookViewModel = new ViewModelProvider(requireActivity(), new SpellbookViewModelFactory(activity.getApplication())).get(SpellbookViewModel.class);

        // Create the dialog builder
        AlertDialog.Builder b = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        final YesNoBinding binding = YesNoBinding.inflate(activity.getLayoutInflater());
        b.setView(binding.getRoot());

        // Set the title
        final TextView title = binding.yesNoTitle;
        final String titleText = "Confirm";
        title.setText(titleText);

        // Set the message
        final TextView message = binding.yesNoMessage;
        final String messageText = "Are you sure you want to delete " + name + "?";
        message.setText(messageText);

        // The listener to delete; for the yes button
        final View.OnClickListener yesListener = (v) -> {
            spellbookViewModel.deleteCharacter(name);
            Toast.makeText(requireActivity(), "Character deleted: " + name, Toast.LENGTH_SHORT).show();
            this.dismiss();
        };

        // The listener to cancel; for the no button
        final View.OnClickListener noListener = (v) -> this.dismiss();

        // Set the button listeners
        final Button yesButton = binding.yesButton;
        final Button noButton = binding.noButton;
        yesButton.setOnClickListener(yesListener);
        noButton.setOnClickListener(noListener);

        // Return the dialog
        return b.create();

    }

}
