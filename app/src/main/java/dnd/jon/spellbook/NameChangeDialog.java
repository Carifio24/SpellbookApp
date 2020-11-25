package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class NameChangeDialog extends DialogFragment {

    private MainActivity main;
    private View view;
    private String originalName;
    private EditText editText;

    static final String nameKey = "name";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        // The character name
        originalName = getArguments().getString(nameKey);

        // The main activity
        main = (MainActivity) getActivity();

        // Create the dialog builder
        AlertDialog.Builder b = new AlertDialog.Builder(main);

        // Inflate the view and set the builder to use this view
        final LayoutInflater inflater = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.name_change, null);
        b.setView(view);

        // We want to start with the original name in the text field
        // When it gets focus, everything is selected
        editText = view.findViewById(R.id.name_change_edit_text);
        editText.setText(originalName);
        editText.setSelectAllOnFocus(true);

        // Create the name change listener
        View.OnClickListener changeListener = (View v) -> {

            // Get the newly-entered name
            final String newName = editText.getText().toString();

            // Is this a valid name?
            final String error = SpellbookUtils.characterNameValidator(main, newName, main.charactersList());
            if (!error.isEmpty()) {
                setErrorMessage(error);
                return;
            }

            // If it's the same as the current name
            if (newName.equals(originalName)) {
                setErrorMessage("This name is the same as the character's current name");
                return;
            }

            // Otherwise, change the character profile
            // Save the new one, and delete the old
            final CharacterProfile profile = main.getProfileByName(originalName);
            profile.setName(newName);
            final boolean saved = main.saveCharacterProfile(profile);
            final boolean deleted = saved && main.deleteCharacterProfile(originalName);
            if (!(saved && deleted)) {
                Toast.makeText(main, "Error changing name.", Toast.LENGTH_SHORT).show();
            }
            final CharacterSelectionDialog charSelect = main.getSelectionDialog();
            if (charSelect != null) {
                charSelect.getAdapter().updateCharactersList();
            }
            this.dismiss();

        };

        // Create the cancel listener
        View.OnClickListener cancelListener = (View view) -> this.dismiss();

        // Set the button listeners
        view.findViewById(R.id.name_change_approve_button).setOnClickListener(changeListener);
        view.findViewById(R.id.name_change_cancel_button).setOnClickListener(cancelListener);

        // Return the dialog
        return b.create();

    }

    private void setErrorMessage(String error) {
        final TextView tv = view.findViewById(R.id.name_change_message);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        tv.setTextColor(Color.RED);
        tv.setText(error);
    }

}
