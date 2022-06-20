package dnd.jon.spellbook;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.view.View;

import dnd.jon.spellbook.databinding.CharacterSelectionBinding;

public class CharacterSelectionDialog extends DialogFragment {

    static final String CHARACTER_CREATION_TAG = "create_character";
    static final String IMPORT_CHARACTER_TAG = "import_character";

    private FragmentActivity activity;
    private CharacterAdapter adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the activity
        activity = requireActivity();

        // Create the new character listener
        final View.OnClickListener newCharacterListener = (View view) -> {
            final CreateCharacterDialog dialog = new CreateCharacterDialog();
            dialog.show(activity.getSupportFragmentManager(), CHARACTER_CREATION_TAG);
        };

        // Create the import character listener
        final View.OnClickListener importListener = (View view) -> {
            final ImportCharacterDialog dialog = new ImportCharacterDialog();
            dialog.show(activity.getSupportFragmentManager(), IMPORT_CHARACTER_TAG);
        };

        // Create the dialog builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        final CharacterSelectionBinding binding = CharacterSelectionBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // Set the button listeners
        binding.newCharacterButton.setOnClickListener(newCharacterListener);
        binding.importCharacterButton.setOnClickListener(importListener);

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

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

    CharacterAdapter getAdapter() { return adapter; }

}
