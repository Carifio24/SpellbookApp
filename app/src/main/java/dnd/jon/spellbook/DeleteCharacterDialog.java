package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DeleteCharacterDialog extends DialogFragment {

    private MainActivity main;
    private String name;

    static final String nameKey = "Name";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        // The character name
        name = getArguments() != null ? getArguments().getString(nameKey) : "";

        // The main activity
        main = (MainActivity) getActivity();

        // Create the dialog builder
        AlertDialog.Builder b = new AlertDialog.Builder(main);

        // Inflate the view and set the builder to use this view
        final LayoutInflater inflater = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.yes_no, null);
        b.setView(view);

        // Set the title
        final TextView title = view.findViewById(R.id.yes_no_title);
        final String titleText = main.getString(R.string.confirm);
        title.setText(titleText);

        // Set the message
        final TextView message = view.findViewById(R.id.yes_no_message);
        final String messageText = main.getString(R.string.delete_character_confirm, name);
        message.setText(messageText);

        // The listener to delete; for the yes button
        final View.OnClickListener yesListener = (v) -> {
            final boolean deleted = main.deleteCharacterProfile(name);
            final int toastMessageID = deleted ? R.string.character_deleted : R.string.error_deleting_character;
            final String toastMessage =  main.getString(toastMessageID, name);
            Toast.makeText(main, toastMessage, Toast.LENGTH_SHORT).show();
            this.dismiss();
        };

        // The listener to cancel; for the no button
        final View.OnClickListener noListener = (v) -> this.dismiss();

        // Set the button listeners
        final Button yesButton = view.findViewById(R.id.yes_button);
        final Button noButton = view.findViewById(R.id.no_button);
        yesButton.setOnClickListener(yesListener);
        noButton.setOnClickListener(noListener);

        // Return the dialog
        return b.create();

    }

    @Override
    public void onDismiss(@NonNull DialogInterface d) {
        super.onDismiss(d);
        final CharacterSelectionDialog charSelect = main.getSelectionDialog();
        if (charSelect != null) {
            charSelect.getAdapter().updateCharactersList();
        }
    }

}
