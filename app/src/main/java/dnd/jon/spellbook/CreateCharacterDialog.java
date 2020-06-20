package dnd.jon.spellbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateCharacterDialog extends DialogFragment {

    private View view;
    private SpellbookViewModel spellbookViewModel;
    static final String MUST_COMPLETE_KEY = "must_complete";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Activity activity = requireActivity();

        // Get the view model
        spellbookViewModel = new ViewModelProvider(requireActivity(), new SpellbookViewModelFactory(activity.getApplication())).get(SpellbookViewModel.class);

        // Create the dialog builder
        AlertDialog.Builder b = new AlertDialog.Builder(activity);

        // Whether or not completion is mandatory
        final Bundle args = getArguments();
        final boolean mustComplete = (args != null) && args.getBoolean(MUST_COMPLETE_KEY, false);

        // Inflate the view and set the builder to use this view
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.character_creation, null);
        b.setView(view);

        // Create the character creation listener
        View.OnClickListener createListener = (View v) -> {

            // The current list of characters
            List<String> characterNames = spellbookViewModel.getAllCharacterNames().getValue();

            // Get the name from the EditText
            EditText et = view.findViewById(R.id.creation_edit_text);
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
            final String nameString = "name";
            for (Character c : SpellbookUtils.illegalCharacters) {
                final String cStr = c.toString();
                    if (name.contains(cStr)) {
                        TextView tv = view.findViewById(R.id.creation_message);
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                        tv.setTextColor(Color.RED);
                        tv.setText(getString(R.string.illegal_character, nameString, cStr));
                        return;
                    }
            }

            // Reject a name that already exists
            if (characterNames != null && characterNames.contains(name)) {
                TextView tv = view.findViewById(R.id.creation_message);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                tv.setTextColor(Color.RED);
                tv.setText(R.string.duplicate_name);
                return;
            }

            // Create the new character profile
            CharacterProfile cp = new CharacterProfile(name);
            spellbookViewModel.addCharacter(cp);

            // Display a Toast message
            Toast.makeText(activity, "Character created: " + name, Toast.LENGTH_SHORT).show();

            //Set it as the current profile if there are no others
            if (spellbookViewModel.getCharactersCount() == 0) {
                spellbookViewModel.setCharacter(name);
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

        // If specified, we make sure that the window cannot be exited
        if (mustComplete) {
            view.findViewById(R.id.cancel_button).setVisibility(View.GONE);
            setCancelable(false);
            alert.setCanceledOnTouchOutside(false);
        }

        // Return the dialog
        return alert;

    }

}
