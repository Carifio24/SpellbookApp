package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CharacterSelectionDialog extends DialogFragment {

    private MainActivity main;
    private View view;
    private TableLayout characterTable;
    private View.OnClickListener textListener;
    private View.OnClickListener deleteListener;
    private View.OnClickListener newCharacterListener;

    private static int textSize = 32;
    private static float nameWidthFrac = 0.7f;
    private static int deleteImageID = android.R.drawable.ic_menu_delete;

    void updateTable() {

        // Clear out all rows, if necessary
        characterTable.removeAllViews();

        Typeface cloisterBlack = ResourcesCompat.getFont(main, R.font.cloister_black);

        // Populate the table
        for (String charName : main.charactersList()) {

            // The character name
            TextView tv = new TextView(main);
            tv.setText(charName);
            tv.setTypeface(cloisterBlack);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
            tv.setOnClickListener(textListener);

            // The delete icon
            ImageButton deleteButton = new ImageButton(main);
            deleteButton.setImageResource(deleteImageID);
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
            characterTable.addView(tr);

        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the main activity
        main = (MainActivity) getActivity();

        // Create the text listener
        textListener = (View view) -> {
            TextView tv = (TextView) view;
            String name = tv.getText().toString();
            main.loadCharacterProfile(name);
            this.dismiss();
        };

        // Create the delete listener
        deleteListener = (View view) -> {
            ImageButton button = (ImageButton) view;
            String name = button.getTag().toString();
            // Fill in the rest once CharacterDeletionDialog is finalized
        };

        // Create the new character listener
        newCharacterListener = (View view) -> {
            CreateCharacterDialog dialog = new CreateCharacterDialog();
            dialog.show(main.getSupportFragmentManager(), "createCharacter");
            //FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            //transaction.add(R.id.character_creation, dialog).commit();
        };

        // Create the dialog builder
        AlertDialog.Builder b = new AlertDialog.Builder(main);

        // Inflate the view and set the builder to use this view
        LayoutInflater inflater = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.character_selection, null);
        b.setView(view);

        // Set the new character listener
        Button newCharacterButton = view.findViewById(R.id.new_character_button);
        newCharacterButton.setOnClickListener(newCharacterListener);

        // Populate the character table
        characterTable = view.findViewById(R.id.selection_table);
        updateTable();

        // Return the dialog
        return b.create();

    }





}
