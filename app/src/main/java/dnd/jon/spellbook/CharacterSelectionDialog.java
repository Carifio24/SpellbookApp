package dnd.jon.spellbook;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import dnd.jon.spellbook.databinding.CharacterSelectionBinding;

public class CharacterSelectionDialog extends DialogFragment
                                      implements NamedItemEventHandler {

    static final String CHARACTER_CREATION_TAG = "create_character";
    static final String IMPORT_CHARACTER_TAG = "import_character";

    private static final String renameTag = "changeCharacterName";
    private static final String duplicateTag = "duplicateCharacter";
    private static final String confirmDeleteTag = "confirmDeleteCharacter";

    private FragmentActivity activity;
    private CharacterAdapter adapter;
    private SpellbookViewModel viewModel;
    private ActivityResultLauncher<String> exportLauncher;
    // TODO: There must be a way to not need this?
    private String exportName;

    static private final String TAG = "CHARACTER_SELECTION_DIALOG";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the activity
        activity = requireActivity();

        viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);

        // Create the dialog builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        final CharacterSelectionBinding binding = CharacterSelectionBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // Set the button listeners
        binding.newCharacterButton.setOnClickListener(this::createNewCharacterListener);
        binding.importCharacterButton.setOnClickListener(this::importCharacter);

        // Set the adapter for the character table
        adapter = new CharacterAdapter(activity, this);
        final RecyclerView recyclerView = binding.selectionRecyclerView;
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        exportLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
            if (uri == null || uri.getPath() == null) {
                Toast.makeText(activity, getString(R.string.selected_path_null), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                final OutputStream outputStream = activity.getContentResolver().openOutputStream(uri);
                final CharacterProfile profile = viewModel.getProfileByName(exportName);
                final String json = profile.toJSON().toString(4);
                final byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                outputStream.write(bytes);
            } catch (IOException | JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        });
        
        // Create the dialog and set a few options
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private void createNewCharacterListener(View view) {
        final CreateCharacterDialog dialog = new CreateCharacterDialog();
        dialog.show(activity.getSupportFragmentManager(), CHARACTER_CREATION_TAG);
    }

    private void importCharacter(View view) {
        final ImportCharacterDialog dialog = new ImportCharacterDialog();
        dialog.show(activity.getSupportFragmentManager(), IMPORT_CHARACTER_TAG);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

    CharacterAdapter getAdapter() { return adapter; }

    @Override
    public void onUpdateEvent(String originalName) {
        final Bundle args = new Bundle();
        args.putString(NameChangeDialog.nameKey, originalName);
        final CharacterNameChangeDialog dialog = new CharacterNameChangeDialog();
        dialog.setArguments(args);
        dialog.show(activity.getSupportFragmentManager(), renameTag);
    }

    @Override
    public void onDuplicateEvent(String name) {
        final Bundle args = new Bundle();
        args.putParcelable(CreateCharacterDialog.PROFILE_KEY, viewModel.getProfileByName(name));
        final CreateCharacterDialog dialog = new CreateCharacterDialog();
        dialog.setArguments(args);
        dialog.show(activity.getSupportFragmentManager(), duplicateTag);
    }

    @Override
    public void onDeleteEvent(String name) {
        final Bundle args = new Bundle();
        args.putString(DeleteCharacterDialog.NAME_KEY, name);
        final DeleteCharacterDialog dialog = new DeleteCharacterDialog();
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
        final CharacterProfile profile = viewModel.getProfileByName(name);
        String message;
        try {
            final String json = profile.toJSON().toString();
            final String label = name + " JSON";
            AndroidUtils.copyToClipboard(activity, json, label);
            message = getString(R.string.item_json_copied, name);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            message = getString(R.string.error_copying_profile_json, name);
        }
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSelectionEvent(String name) {
        viewModel.setProfileByName(name);
        // Show a Toast message after selection
        Toast.makeText(activity, activity.getString(R.string.character_selected_toast, name), Toast.LENGTH_SHORT).show();
    }
}
