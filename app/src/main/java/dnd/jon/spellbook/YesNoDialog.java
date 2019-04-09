package dnd.jon.spellbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class YesNoDialog extends DialogFragment {

    static final String ARG_TITLE_KEY = "title";
    static final String ARG_MESSAGE_KEY = "message";
    static final String ARG_REQUEST_KEY = "request";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString(ARG_TITLE_KEY);
        String message = args.getString(ARG_MESSAGE_KEY);
        final int requestCode = args.getInt(ARG_REQUEST_KEY);

        // The main activity
        MainActivity main = (MainActivity) getActivity();

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, (DialogInterface dialog, int which) ->
                {
                    main.onActivityResult(requestCode, Activity.RESULT_OK, new Intent());

                })
                .setNegativeButton(android.R.string.no, (DialogInterface dialog, int which) ->
                {
                    main.onActivityResult(requestCode, Activity.RESULT_CANCELED, new Intent());
                })
                .create();
    }

}