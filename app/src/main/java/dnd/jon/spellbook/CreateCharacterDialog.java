package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class CreateCharacterDialog extends DialogFragment {

    private View view;
    private MainActivity main;

    @NonNull
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
        View.OnClickListener createListener = (View v) -> {

            // The number of current characters
            List<String> characters = main.charactersList();
            int nChars = characters.size();

            // Get the name from the EditText
            EditText et = view.findViewById(R.id.creation_edit_text);
            String name = et.getText().toString();

            // Check that the character name is valid
            final String error = SpellbookUtils.characterNameValidator(main, name, characters);
            if (!error.isEmpty()) {
                setErrorMessage(error);
                return;
            }

            // Create the new character profile
            CharacterProfile cp = new CharacterProfile(name);
            String charFile = cp.getName() + ".json";
            File profileLocation = new File(main.getProfilesDir(), charFile);
            cp.save(profileLocation);

            // Display a Toast message
            Toast.makeText(main, "Character created: " + name, Toast.LENGTH_SHORT).show();

            //Set it as the current profile if there are no others
            if (nChars == 0) {
                main.setCharacterProfile(cp);
            }
            this.dismiss();
        };

        // Create the cancel listener
        View.OnClickListener cancelListener = (View view) -> this.dismiss();

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

    private void setErrorMessage(String error) {
        final TextView tv = view.findViewById(R.id.creation_message);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        tv.setTextColor(Color.RED);
        tv.setText(error);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface d) {
        super.onDismiss(d);
        CharacterSelectionDialog charSelect = main.getSelectionDialog();
        if (charSelect != null) {
            charSelect.getAdapter().updateCharactersList();
        }
    }

}
