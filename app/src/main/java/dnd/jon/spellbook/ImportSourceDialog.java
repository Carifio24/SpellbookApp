package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import org.javatuples.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import dnd.jon.spellbook.databinding.ImportSourceBinding;


public class ImportSourceDialog extends DialogFragment {
    private ImportSourceBinding binding;
    private FragmentActivity activity;
    private SpellbookViewModel viewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        activity = requireActivity();
        viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory())
                            .get(SpellbookViewModel.class);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        binding = ImportSourceBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        binding.sourceImportButton.setOnClickListener((v) -> {
            final String jsonString = binding.sourceImportEditText.getText().toString();
            boolean complete = false;
            String message;
            try {
                final JSONObject json = new JSONObject(jsonString);
                final Pair<Boolean, String> result = viewModel.addSourceFromJSON(json);
                message = result.getValue1();
                complete = result.getValue0();
            } catch (JSONException e) {
                message = getString(R.string.invalid_json_for, getString(R.string.source));
            }

            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            if (complete) {
                this.dismiss();
            }
        });

        binding.sourceImportCancelButton.setOnClickListener((v) -> this.dismiss());

        return builder.create();
    }
}
