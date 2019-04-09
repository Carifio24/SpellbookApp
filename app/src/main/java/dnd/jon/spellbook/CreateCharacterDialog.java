package dnd.jon.spellbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

public class CreateCharacterDialog extends DialogFragment {

    EditText nameEntry;
    static final String MESSAGE = "Please enter the name of your character";
    static final String TITLE = "Character creation";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        nameEntry = new EditText(getActivity());

        // The main activity
        MainActivity main = (MainActivity) getActivity();

        int requestCode = RequestCodes.CREATE_CHARACTER_REQUEST;

        AlertDialog.Builder builder = new AlertDialog.Builder(main);
        builder.setTitle(TITLE).setMessage(MESSAGE).setView(nameEntry);
        builder.setNegativeButton(android.R.string.no, (DialogInterface dialog, int which) ->
        {
            main.onActivityResult(requestCode, Activity.RESULT_CANCELED, new Intent());
        });
        builder.setPositiveButton(android.R.string.yes, (DialogInterface dialog, int which) ->
        {
            // We'll assign the real function later
            // Just want to make sure that the callback function doesn't get instantiated now

        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((View v) -> {

            // Get what's in the text field
            String characterName = nameEntry.getText().toString();

            // Create a new character profile and assign this as the current one
            CharacterProfile cp = new CharacterProfile(characterName);
            main.setCharacterProfile(cp);
        });
        return alertDialog;

    }

}
