package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

class CharacterTable {

    private MainActivity main;
    private TableLayout table;
    private View.OnClickListener textListener;
    private View.OnClickListener deleteListener;

    TableLayout getTable() { return table; }

    void updateTable() {

        System.out.println("Updating table...");

        // Clear out all rows, if necessary
        table.removeAllViews();

        // Typeface cloisterBlack = ResourcesCompat.getFont(main, R.font.cloister_black);

        // Populate the table
        for (String charName : main.charactersList()) {

            // Inflate the table row from the XML layout
            TableRow tr = (TableRow) View.inflate(main, R.layout.character_table_row, null);

            // The character name
            TextView tv = tr.findViewById(R.id.character_row_text);
            tv.setText(charName);
            tv.setOnClickListener(textListener);

            // The delete icon
            ImageButton deleteButton = tr.findViewById(R.id.character_row_button);
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
            TextView tv = (TextView) view;
            String name = tv.getText().toString();
            main.loadCharacterProfile(name);
            CharacterSelectionDialog mainSelectionDialog = main.getSelectionDialog();
            if (mainSelectionDialog != null) {
                mainSelectionDialog.dismiss();
            }
            // Show a Toast message after selection
            Toast.makeText(main, "Character selected: " + name, Toast.LENGTH_SHORT).show();
        };

        // Create the delete listener
        deleteListener = (View view) -> {
            ImageButton button = (ImageButton) view;
            String name = button.getTag().toString();
            Bundle args = new Bundle();
            args.putString(DeleteCharacterDialog.nameKey, name);
            DeleteCharacterDialog dialog = new DeleteCharacterDialog();
            dialog.setArguments(args);
            dialog.show(main.getSupportFragmentManager(), "confirmDeleteCharacter");
        };

        updateTable();
    }

}
