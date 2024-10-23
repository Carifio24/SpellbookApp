package dnd.jon.spellbook;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import dnd.jon.spellbook.databinding.CharacterSelectionBinding;

public class CharacterSelectionDialog extends DialogFragment {

    static final String CHARACTER_CREATION_TAG = "create_character";
    static final String IMPORT_CHARACTER_TAG = "import_character";

    private FragmentActivity activity;
    private CharacterAdapter adapter;

    static private final String TAG = "CHARACTER_SELECTION_DIALOG";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the activity
        activity = requireActivity();

        // Create the dialog builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        final CharacterSelectionBinding binding = CharacterSelectionBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // Set the button listeners
        binding.newCharacterButton.setOnClickListener(this::createNewCharacterListener);
        binding.importCharacterButton.setOnClickListener(this::importCharacter);

        // Set the adapter for the character table
        adapter = new CharacterAdapter(activity);
        final RecyclerView recyclerView = binding.selectionRecyclerView;
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        
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

}
