package dnd.jon.spellbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import dnd.jon.spellbook.databinding.CreationChooserBinding;

public class CreationChooserDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the activity and create the dialog builder
        final Activity activity = requireActivity();
        final AlertDialog.Builder b = new AlertDialog.Builder(activity);

        // Get the binding and set the root view
        final CreationChooserBinding binding = CreationChooserBinding.inflate(activity.getLayoutInflater());
        b.setView(binding.getRoot());

        // Set up the button listeners
        binding.newSourceButton.setOnClickListener((v) -> openSourceActivity());
        binding.newSpellButton.setOnClickListener((v) -> openSpellActivity());

        // Create and return the dialog
        return b.create();

    }

    private <A extends Activity> void openActivity(Class<A> typeToOpen, int requestCode) {
        final Activity activity = requireActivity();
        final Intent intent = new Intent(activity, typeToOpen);
        activity.startActivityForResult(intent, requestCode);
    }

    private void openSpellActivity() { openActivity(SpellCreationActivity.class, RequestCodes.SPELL_CREATION_REQUEST); }
    private void openSourceActivity() { openActivity(SourceCreationActivity.class, RequestCodes.SOURCE_CREATION_REQUEST); }

}
