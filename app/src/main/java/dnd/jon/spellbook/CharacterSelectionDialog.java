package dnd.jon.spellbook;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

public class CharacterSelectionDialog extends DialogFragment {

    private MainActivity main;
    private View view;
    private CharacterTable characterTable;
    private View.OnClickListener newCharacterListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the main activity
        main = (MainActivity) getActivity();

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
        TableLayout t = view.findViewById(R.id.selection_table);
        characterTable = new CharacterTable(t);

        // Attach the dialog to main and return
        AlertDialog d = b.create();
        d.setOnCancelListener((DialogInterface di) -> { this.dismiss(); });
        d.setCanceledOnTouchOutside(true);
        main.setCharacterSelect(view);
        main.setSelectionDialog(this);
        return d;

    }


    @Override
    public void onDismiss(DialogInterface d) {
        super.onDismiss(d);
        System.out.println("Dismissing dialog...");
        main.setCharacterSelect(null);
    }

}
