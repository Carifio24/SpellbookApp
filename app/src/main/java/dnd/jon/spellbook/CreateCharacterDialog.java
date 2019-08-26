package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class CreateCharacterDialog extends DialogFragment {

    private View view;
    private MainActivity main;
    private View.OnClickListener createListener;
    private View.OnClickListener cancelListener;

    private static final ArrayList<Character> illegalCharacters = new ArrayList<>(Arrays.asList('\\', '/', '.'));

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

            // The number of current characters
            ArrayList<String> characters = main.charactersList();
            int nChars = characters.size();

            // Get the name from the EditText
            EditText et = view.findViewById(R.id.creation_edittext);
            String name = et.getText().toString();

            // Reject an empty name
            if (name.isEmpty()) {
                TextView tv = view.findViewById(R.id.creation_message);
                tv.setTextColor(Color.RED);
                tv.setText(R.string.empty_name);
                return;
            }

            // Reject a name that contains / or \
            // / causes path issues - forbid both, as well as a period, to be safe
            for (Character c : illegalCharacters) {
                    if (name.contains(c.toString())) {
                        TextView tv = view.findViewById(R.id.creation_message);
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                        tv.setTextColor(Color.RED);
                        tv.setText(R.string.illegal_character);
                        return;
                    }
            }

            // Reject a name that already exists
            if (characters.contains(name)) {
                TextView tv = view.findViewById(R.id.creation_message);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                tv.setTextColor(Color.RED);
                tv.setText(R.string.duplicate_name);
                return;
            }

            // Create the new character profile
            CharacterProfile cp = new CharacterProfile(name);
            String charFile = cp.getName() + ".json";
            File profileLocation = new File(main.getProfilesDir(), charFile);
            cp.save(profileLocation);

            //Set it as the current profile if there are no others
            if (nChars == 0) {
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

        // If there are no characters, we make sure that the window cannot be exited
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
        super.onDismiss(d);
        View charSelect = main.getCharacterSelect();
        if (charSelect != null) {
            TableLayout table = charSelect.findViewById(R.id.selection_table);
            CharacterTable ct = new CharacterTable(table);
            ct.updateTable();
        }
    }

}
