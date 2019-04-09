package dnd.jon.spellbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;

public class CharacterSelectionDialog extends DialogFragment {

    TableLayout charactersTable;

    static final String NAME_KEY = "name";
    static final String TITLE = "Characters";
    static final String MESSAGE = "Please select a character.";

    MainActivity main;
    View.OnClickListener textListener;
    View.OnClickListener deleteListener;

    static final int requestCode = RequestCodes.CHARACTER_SELECTION_REQUEST;

    void updateTable() {

        // Clear out all rows, if necessary
        charactersTable.removeAllViews();

        // Set up the table of characters
        for (String charName : main.charactersList()) {

            // The character name
            TextView tv = new TextView(getActivity());
            tv.setText(charName);
            tv.setOnClickListener(textListener);

            // The delete icon
            ImageButton deleteButton = new ImageButton(getActivity());
            deleteButton.setImageResource(android.R.drawable.ic_menu_delete);
            deleteButton.setTag(charName);
            deleteButton.setOnClickListener(deleteListener);
            TableRow.LayoutParams dblp = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.FILL_PARENT);
            dblp.gravity = Gravity.RIGHT;
            deleteButton.setLayoutParams(dblp);

            // Put into one TableRow
            TableRow tr = new TableRow(getActivity());
            tr.addView(tv);
            tr.addView(deleteButton);

            // Add the row to the table
            charactersTable.addView(tr);
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // The main activity
        main = (MainActivity) getActivity();

        // The TableLayout
        charactersTable = new TableLayout(getActivity());

        // The TextView listener
        textListener = (View view) -> {
            TextView tv = (TextView) view;
            String name = tv.getText().toString();
            main.loadCharacterProfile(name);
            this.dismiss();
        };

        // The delete listener
        deleteListener = (View view) -> {
            ImageButton button = (ImageButton) view;
            String name = button.getTag().toString();
            String title = "Confirm";
            String message = "Are you sure you want to delete " + name + "?";
            YesNoDialog dialog = new YesNoDialog();
            Bundle args = new Bundle();
            args.putInt(YesNoDialog.REQUEST_KEY, RequestCodes.DELETE_CHARACTER_REQUEST);
            args.putString(YesNoDialog.MESSAGE_KEY, message);
            args.putString(YesNoDialog.TITLE_KEY, title);
            dialog.setArguments(args);
            dialog.show(main.getSupportFragmentManager(), "deleteCharacter");
            updateTable();
        };

        updateTable();

        AlertDialog.Builder builder = new AlertDialog.Builder(main);
        builder.setTitle(TITLE).setMessage(MESSAGE).setView(charactersTable);
        builder.setNegativeButton(android.R.string.no, (
                DialogInterface dialog, int which) ->
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
        alertDialog.show();
        return alertDialog;
    }

}
