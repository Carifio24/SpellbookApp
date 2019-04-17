package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

public class DeleteCharacterDialog extends DialogFragment {

    private View view;
    private MainActivity main;
    private String name;
    private View.OnClickListener yesListener;
    private View.OnClickListener noListener;

    static final String nameKey = "Name";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        // The character name
        name = getArguments().getString(nameKey);

        // The main activity
        main = (MainActivity) getActivity();

        // Create the dialog builder
        AlertDialog.Builder b = new AlertDialog.Builder(main);

        // Inflate the view and set the builder to use this view
        LayoutInflater inflater = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.yes_no, null);
        b.setView(view);

        // Set the title
        TextView title = view.findViewById(R.id.yes_no_title);
        String titleText = "Confirm";
        title.setText(titleText);

        // Set the message
        TextView message = view.findViewById(R.id.yes_no_message);
        String messageText = "Are you sure you want to delete " + name + "?";
        message.setText(messageText);

        // The listener to delete; for the yes button
        yesListener = (View view) -> {
            main.deleteCharacterProfile(name);
            this.dismiss();
        };

        // The listener to cancel; for the no button
        noListener = (View view) -> {
            this.dismiss();
        };

        // Set the button listeners
        Button yesButton = view.findViewById(R.id.yes_button);
        Button noButton = view.findViewById(R.id.no_button);
        yesButton.setOnClickListener(yesListener);
        noButton.setOnClickListener(noListener);

        // Return the dialog
        return b.create();

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
