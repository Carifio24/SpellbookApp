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

import dnd.jon.spellbook.databinding.ImportContentBinding;

public class ImportContentDialog extends DialogFragment {

    private static final String TAG = "IMPORT_CONTENT_DIALOG";
    private ImportContentBinding binding;
    private FragmentActivity activity;
    private SpellbookViewModel viewModel;
    private ActivityResultLauncher<String[]> importContentFileChooser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        importContentFileChooser = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            final FragmentActivity activity = requireActivity();
            boolean complete = false;
            int messageID;
            if (uri == null || uri.getPath() == null) {
                Toast.makeText(activity, getString(R.string.selected_path_null), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                final InputStream inputStream = activity.getContentResolver().openInputStream(uri);
                final String text = new BufferedReader(new InputStreamReader(inputStream))
                        .lines().collect(Collectors.joining());
                final JSONObject json = new JSONObject(text);
                final boolean success = viewModel.loadCreatedContent(json);
                messageID = success ? R.string.content_loaded_successfully : R.string.issues_loading_some_content;
                complete = success;
            } catch (FileNotFoundException | JSONException e) {
                Log.e(TAG, e.getMessage());
                messageID = R.string.json_import_error;
            }

            Toast.makeText(activity, messageID, Toast.LENGTH_SHORT).show();
            if (complete) {
                this.dismiss();
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        activity = requireActivity();
        viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory())
                .get(SpellbookViewModel.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        binding = ImportContentBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        binding.contentImportButton.setOnClickListener(this::importContentFromText);
        binding.contentImportFileButton.setOnClickListener(this::importContentFromFile);
        binding.contentImportCancelButton.setOnClickListener((v) -> this.dismiss());

        return builder.create();
    }

    private void importContentFromText(View view) {
        final String jsonString = binding.contentImportEditText.getText().toString();
        int messageID;
        boolean complete = true;
        try {
            final JSONObject json = new JSONObject(jsonString);
            final boolean success = viewModel.loadCreatedContent(json);
            messageID = success ? R.string.content_loaded_successfully : R.string.issues_loading_some_content;
            complete = success;
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            messageID = R.string.json_import_error;
        }

        Toast.makeText(activity, messageID, Toast.LENGTH_SHORT).show();
        if (complete) {
            this.dismiss();
        }
    }

    private void importContentFromFile(View view) {
        importContentFileChooser.launch(new String[]{"application/json"});
    }
}
