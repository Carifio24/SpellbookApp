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

import java.io.File;

public class CreateCharacterDialog extends DialogFragment {

    EditText nameEntry;
    static final String MESSAGE = "Please enter the name of your character";
    static final String TITLE = "Character creation";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // The main activity
        MainActivity main = (MainActivity) getActivity();

        // The EditText
        nameEntry = new EditText(getActivity());

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

        // Create the AlertDialog
        final AlertDialog alertDialog = builder.create();

        // Adjust the width of the EditText
        int width = alertDialog.getWindow().getDecorView().getWidth();
        float frac = 0.75f;
        int entryWidth = Math.round(frac * width);
        nameEntry.setWidth(entryWidth);
        nameEntry.setX((width - entryWidth) / 2);

        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((View v) -> {

            // Get what's in the text field
            String characterName = nameEntry.getText().toString();

            // The name cannot be empty
            if (characterName.isEmpty()) {
                alertDialog.setMessage("The character name cannot be empty");
                return;
            }

            // Check whether or not this name already exists
            File profilesDir = main.profilesDir;
            File charFile = new File(profilesDir, characterName + ".json");
            if (charFile.exists()) {
                alertDialog.setMessage("A character with this name already exists");
                return;
            }

            // Create a new character profile and assign this as the current one
            CharacterProfile cp = new CharacterProfile(characterName);
            main.setCharacterProfile(cp);
            alertDialog.dismiss();
        });
        return alertDialog;

    }

}
