package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import dnd.jon.spellbook.databinding.FeedbackDialogBinding;

public class FeedbackDialog extends DialogFragment {

    private FragmentActivity activity;
    private FeedbackDialogBinding binding;
    private static final String devEmail = "dndspellbookapp@gmail.com";
    private static final String emailMessage = "[Android] Feedback";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the main activity
        activity = requireActivity();

        // Create the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        binding = FeedbackDialogBinding.inflate(getLayoutInflater());

        // Create the listener for the send button
        Button sendButton = binding.feedbackSendButton;
        sendButton.setOnClickListener((v) -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{devEmail});
            i.putExtra(Intent.EXTRA_SUBJECT, emailMessage);
            i.putExtra(Intent.EXTRA_TEXT, feedbackMessage());
            try {
                startActivity(Intent.createChooser(i, getString(R.string.send_email)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(activity, activity.getString(R.string.no_email_clients), Toast.LENGTH_SHORT).show();
            }
        });

        // Create the listener for the cancel button
        Button cancelButton = binding.feedbackCancelButton;
        cancelButton.setOnClickListener( (v) -> this.dismiss() );

        // Create the dialog and return
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener((DialogInterface di) -> this.dismiss() );
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private String feedbackMessage() {

        // Return the current text in the EditText
        EditText textBox = binding.feedbackBox;
        return textBox.getText().toString();
    }

}
