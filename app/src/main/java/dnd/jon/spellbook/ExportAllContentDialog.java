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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import dnd.jon.spellbook.databinding.ExportAllContentBinding;

public class ExportAllContentDialog extends DialogFragment {

    private static final String TAG = "EXPORT_ALL_CONTENT_DIALOG";
    private SpellbookViewModel viewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = requireActivity();
        viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory())
                        .get(SpellbookViewModel.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final ExportAllContentBinding binding = ExportAllContentBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        final ActivityResultLauncher<String> chooser = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
            if (uri == null || uri.getPath() == null) {
                Toast.makeText(activity, R.string.error_exporting_app_content, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                final OutputStream outputStream = activity.getContentResolver().openOutputStream(uri);
                final JSONObject json = viewModel.allCreatedContent();
                final String content = json.toString();
                final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                outputStream.write(bytes);
            } catch (IOException | JSONException e) {
                final String errorMessage = getString(R.string.error_exporting_app_content);
                Log.e(TAG, e.getMessage());
                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        binding.exportContentCancelButton.setOnClickListener((View view) -> this.dismiss());
        binding.exportContentFileButton.setOnClickListener((View view) -> chooser.launch(getString(R.string.default_export_content_filename)));
        binding.exportContentClipboardButton.setOnClickListener((View view) -> {
            String message;
            try {
                final JSONObject json = viewModel.allCreatedContent();
                final String content = json.toString();
                final String label = getString(R.string.default_export_content_cliplabel);
                AndroidUtils.copyToClipboard(activity, content, label);
                message = getString(R.string.app_content_clipboard_success);
            } catch (JSONException e) {
                message = getString(R.string.error_exporting_app_content);
                Log.e(TAG, e.getMessage());
            }
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        });

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }
}
