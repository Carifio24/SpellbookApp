package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import dnd.jon.spellbook.databinding.CharacterImportBinding;

public class ImportCharacterDialog extends DialogFragment {

    private static final String TAG = "IMPORT_CHARACTER_DIALOG";

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

        binding.characterImportFileButton.setOnClickListener(this::importProfileFromFile);
        binding.characterImportButton.setOnClickListener(this::importProfileFromText);
        binding.characterImportCancelButton.setOnClickListener((v) -> this.dismiss());

        return builder.create();
    }

    private void importProfileFromText(View view) {
        final String jsonString = binding.characterImportEditText.getText().toString();
        String toastMessage;
        boolean complete = false;
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile profile = CharacterProfile.fromJSON(json);
            final CharacterProfile sameName = viewModel.getProfileByName(profile.getName());
            if (sameName != null) {
                toastMessage = getString(R.string.duplicate_name, getString(R.string.character_lowercase));
            } else {
                toastMessage = getString(R.string.imported_toast, profile.getName());
                viewModel.saveProfile(profile);
                complete = true;
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            toastMessage = getString(R.string.json_import_error);
        }

        Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show();
        if (complete) {
            this.dismiss();
        }
    }

    private void importProfileFromFile(View view) {
        final ActivityResultLauncher<String[]> importCharacterFileChooser = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            final FragmentActivity activity = requireActivity();
            String toastMessage;
            boolean complete = false;
            if (uri == null || uri.getPath() == null) {
                Toast.makeText(activity, getString(R.string.selected_path_null), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                final InputStream inputStream = activity.getContentResolver().openInputStream(uri);
                final String text = new BufferedReader(new InputStreamReader(inputStream))
                        .lines().collect(Collectors.joining());
                final JSONObject json = new JSONObject(text);
                final CharacterProfile profile = CharacterProfile.fromJSON(json);
                viewModel.saveProfile(profile);
                toastMessage = getString(R.string.imported_toast, profile.getName());
                complete = true;
            } catch (FileNotFoundException | JSONException e) {
                Log.e(TAG, e.getMessage());
                toastMessage = getString(R.string.json_import_error);
            }

            Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show();
            if (complete) {
                this.dismiss();
            }
        });
        importCharacterFileChooser.launch(null);
    }

}
