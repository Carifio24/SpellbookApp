package dnd.jon.spellbook;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class MessageDialog extends DialogFragment {

    static final String TITLE_KEY = "title";
    static final String NAME_KEY = "name";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        final Bundle args = getArguments();
        final String title = args.getString(TITLE_KEY);
        final int

    }

}
