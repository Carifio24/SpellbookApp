package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.File;

public class CreateCharacterDialog extends DialogFragment {

    private View view;
    private MainActivity main;
    private View.OnClickListener createListener;
    private View.OnClickListener cancelListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // The main activity
        main = (MainActivity) getActivity();

        // Create the dialog builder
        AlertDialog.Builder b = new AlertDialog.Builder(main);

        // Inflate the view and set the builder to use this view
        LayoutInflater inflater = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.character_creation, null);
        b.setView(view);

        // Create the character creation listener
        createListener = (View v) -> {

            // Get the name from the EditText
            EditText et = view.findViewById(R.id.creation_edittext);
            String name = et.getText().toString();

            // Reject an empty name
            if (name.isEmpty()) {
                TextView tv = view.findViewById(R.id.creation_message);
                tv.setText(R.string.empty_name);
                return;
            }

            // Create the new character profile
            CharacterProfile cp = new CharacterProfile(name);
            String charFile = cp.getName() + ".json";
            File profileLocation = new File(main.profilesDir, charFile);
            cp.save(profileLocation);

            //Set it as the current profile if there are no others
            if (main.charactersList().size() == 0) {
                main.setCharacterProfile(cp);
            }
            this.dismiss();
        };

        // Create the cancel listener
        cancelListener = (View view) -> {
            this.dismiss();
        };

        // Set the button listeners
        view.findViewById(R.id.create_button).setOnClickListener(createListener);
        view.findViewById(R.id.cancel_button).setOnClickListener(cancelListener);

        // The dialog
        AlertDialog alert = b.create();

        if (main.charactersList().size() == 0) {
            view.findViewById(R.id.cancel_button).setVisibility(View.GONE);
            setCancelable(false);
            alert.setCanceledOnTouchOutside(false);
        }

        // Return the dialog
        return alert;

    }

    @Override
    public void onDismiss(DialogInterface d) {
        if (main.characterSelect != null) {
            View v = main.characterSelect;
            TableLayout table = v.findViewById(R.id.selection_table);
            CharacterTable ct = new CharacterTable(table);
            ct.updateTable();
        }
    }

}
