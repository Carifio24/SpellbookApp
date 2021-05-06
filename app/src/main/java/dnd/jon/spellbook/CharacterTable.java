package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

class CharacterTable {

    private final MainActivity main;
    private final TableLayout table;
    private final View.OnClickListener textListener;
    private final View.OnClickListener deleteListener;
    private final View.OnClickListener editListener;

    TableLayout getTable() { return table; }

    void updateTable() {

        // Clear out all rows, if necessary
        table.removeAllViews();

        // Typeface cloisterBlack = ResourcesCompat.getFont(main, R.font.cloister_black);

        // Populate the table
        for (String charName : main.charactersList()) {

            // Inflate the table row from the XML layout
            final TableRow tr = (TableRow) View.inflate(main, R.layout.character_table_row, null);

            // The character name
            final TextView tv = tr.findViewById(R.id.character_row_text);
            tv.setText(charName);
            tv.setOnClickListener(textListener);

            // The edit icon
            final ImageButton editButton = tr.findViewById(R.id.character_row_edit);
            editButton.setTag(charName);
            editButton.setOnClickListener(editListener);

            // The delete icon
            final ImageButton deleteButton = tr.findViewById(R.id.character_row_delete);
            deleteButton.setTag(charName);
            deleteButton.setOnClickListener(deleteListener);

            // Add the row to the table
            table.addView(tr);

        }

    }

    CharacterTable(TableLayout t) {
        table = t;
        main = (MainActivity) table.getContext();

        // Create the text listener
        textListener = (View view) -> {
            final TextView tv = (TextView) view;
            final String name = tv.getText().toString();
            main.loadCharacterProfile(name);
            CharacterSelectionDialog mainSelectionDialog = main.getSelectionDialog();
            if (mainSelectionDialog != null) {
                mainSelectionDialog.dismiss();
            }
            // Show a Toast message after selection
            Toast.makeText(main, main.getString(R.string.character_selected_toast, name), Toast.LENGTH_SHORT).show();
        };

        // Create the edit listener
        editListener = (View view) -> {
            final ImageButton button = (ImageButton) view;
            final String name = button.getTag().toString();
            final Bundle args = new Bundle();
            args.putString(NameChangeDialog.nameKey, name);
            final NameChangeDialog dialog = new NameChangeDialog();
            dialog.setArguments(args);
            dialog.show(main.getSupportFragmentManager(), "changeCharacterName");
            // TODO - Finish implementation after creating NameChangeDialog
        };

        // Create the delete listener
        deleteListener = (View view) -> {
            final ImageButton button = (ImageButton) view;
            final String name = button.getTag().toString();
            final Bundle args = new Bundle();
            args.putString(DeleteCharacterDialog.nameKey, name);
            final DeleteCharacterDialog dialog = new DeleteCharacterDialog();
            dialog.setArguments(args);
            dialog.show(main.getSupportFragmentManager(), "confirmDeleteCharacter");
        };

        updateTable();
    }

}
