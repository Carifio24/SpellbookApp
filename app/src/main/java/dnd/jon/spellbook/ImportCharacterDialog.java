package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONException;
import org.json.JSONObject;

import dnd.jon.spellbook.databinding.CharacterImportBinding;

public class ImportCharacterDialog extends DialogFragment {

    private CharacterImportBinding binding;
    private FragmentActivity activity;
    private SpellbookViewModel viewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        activity = requireActivity();
        viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory())
                        .get(SpellbookViewModel.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        binding = CharacterImportBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // Create the character import listener
        binding.importButton.setOnClickListener((v) -> {
            final String jsonString = binding.importEditText.getText().toString();
            String toastMessage;
            boolean complete = false;
            try {
                final JSONObject json = new JSONObject(jsonString);
                final CharacterProfile profile = CharacterProfile.fromJSON(json);
                final CharacterProfile sameName = viewModel.getProfileByName(profile.getName());
                if (sameName != null) {
                    toastMessage = getString(R.string.duplicate_name, getString(R.string.character));
                } else {
                    toastMessage = getString(R.string.character_imported_toast, profile.getName());
                    viewModel.saveProfile(profile);
                    complete = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                toastMessage = getString(R.string.character_import_error);
            }

            Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show();
            if (complete) {
                this.dismiss();
            }
        });

        binding.importCancelButton.setOnClickListener((v) -> this.dismiss());

        return builder.create();
    }

}
