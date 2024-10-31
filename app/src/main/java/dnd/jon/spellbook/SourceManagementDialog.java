package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import dnd.jon.spellbook.databinding.SourceManagementBinding;

public class SourceManagementDialog extends DialogFragment
                                    implements NamedItemEventHandler {

    private static final String updateSourceTag = "updateSource";
    private static final String duplicateTag = "duplicateSource";
    private static final String confirmDeleteTag = "confirmDeleteSource";
    static private final String TAG = "SOURCE_MANAGEMENT_DIALOG";
    private SourceAdapter adapter;
    private FragmentActivity activity;
    private SpellbookViewModel viewModel;
    private ActivityResultLauncher<String> exportLauncher;

    // TODO: There must be a way to not need this?
    private String exportName;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        activity = requireActivity();
        viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory())
                            .get(SpellbookViewModel.class);
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final LayoutInflater inflater = getLayoutInflater();
        final SourceManagementBinding binding = SourceManagementBinding.inflate(inflater);
        builder.setView(binding.getRoot());

        adapter = new SourceAdapter(activity, this);
        final RecyclerView recyclerView = binding.sourceManagementRecyclerView;
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        exportLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
            if (uri == null || uri.getPath() == null) {
                Toast.makeText(activity, getString(R.string.selected_path_null), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                final OutputStream outputStream = activity.getContentResolver().openOutputStream(uri);
                final Source source = viewModel.getCreatedSourceByName(exportName);
                final Collection<Spell> spells = viewModel.getCreatedSpellsForSource(source);
                final String json = JSONUtils.asJSON(source, activity, spells).toString();
                final byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
                outputStream.write(bytes);
            } catch (IOException | JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onUpdateEvent(String name) {
        final Bundle args = new Bundle();
        args.putString(SourceCreationDialog.NAME_KEY, name);
        final SourceCreationDialog dialog = new SourceCreationDialog();
        dialog.setArguments(args);
        dialog.show(activity.getSupportFragmentManager(), updateSourceTag);
    }

    @Override
    public void onDuplicateEvent(String name) {
        // In case the duplicate option somehow is displayed,
        // we may as well do something sensible
        final Bundle args = new Bundle();
        args.putString(SourceCreationDialog.NAME_KEY, name);
        final SourceCreationDialog dialog = new SourceCreationDialog();
        dialog.setArguments(args);
        dialog.show(activity.getSupportFragmentManager(), duplicateTag);
    }

    @Override
    public void onDeleteEvent(String name) {
        final Bundle args = new Bundle();
        args.putString(DeleteSourceDialog.NAME_KEY, name);
        final DeleteSourceDialog dialog = new DeleteSourceDialog();
        dialog.setArguments(args);
        dialog.show(activity.getSupportFragmentManager(), confirmDeleteTag);
    }

    @Override
    public void onExportEvent(String name) {
        exportName = name;
        exportLauncher.launch(name + ".json");
    }

    @Override
    public void onCopyEvent(String name) {
        String message;
        try {
            final Source source = viewModel.getCreatedSourceByName(name);
            final Collection<Spell> spells = viewModel.getCreatedSpellsForSource(source);
            final String json = JSONUtils.asJSON(source, activity, spells).toString();
            final String jsonString = json.toString();
            final String label = name + " JSON";
            AndroidUtils.copyToClipboard(activity, jsonString, label);
            message = getString(R.string.item_json_copied, name);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            message = getString(R.string.json_import_error);
        }
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSelectionEvent(String name) {
        final SourceCreationDialog dialog = new SourceCreationDialog();
        final Bundle args = new Bundle();
        args.putString(SourceCreationDialog.NAME_KEY, name);
        dialog.setArguments(args);
        dialog.show(activity.getSupportFragmentManager(), updateSourceTag);
    }
}

