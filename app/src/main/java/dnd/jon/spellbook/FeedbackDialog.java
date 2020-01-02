package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FeedbackDialog extends DialogFragment {

    private MainActivity main;
    private View view;
    private static final String devEmail = "dndspellbookapp@gmail.com";
    private static final String emailMessage = "[Android] Feedback";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the main activity
        main = (MainActivity) getActivity();

        // Create the dialog builder
        AlertDialog.Builder b = new AlertDialog.Builder(main);

        // Inflate the view and set the builder to use this view
        LayoutInflater inflater = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.feedback_dialog, null);
        b.setView(view);

        // Create the listener for the send button
        Button sendButton = view.findViewById(R.id.feedback_send_button);
        sendButton.setOnClickListener((v) -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{devEmail});
            i.putExtra(Intent.EXTRA_SUBJECT, emailMessage);
            i.putExtra(Intent.EXTRA_TEXT, feedbackMessage());
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(main, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        });

        // Create the listener for the cancel button
        Button cancelButton = view.findViewById(R.id.feedback_cancel_button);
        cancelButton.setOnClickListener( (v) -> this.dismiss() );

        // Create the dialog and return
        AlertDialog d = b.create();
        d.setOnCancelListener((DialogInterface di) -> this.dismiss() );
        d.setCanceledOnTouchOutside(true);
        return d;
    }

    private String feedbackMessage() {

        // Should never happen, but just in case
        if (view == null) { return ""; }

        // Return the current text in the EditText
        EditText textBox = view.findViewById(R.id.feedback_box);
        return textBox.getText().toString();
    }

}
