package dnd.jon.spellbook;

import android.app.Dialog;
import android.content.Context;
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
            CreateCharacterDialog dialog = new CreateCharacterDialog();
            dialog.show(activity.getSupportFragmentManager(), CHARACTER_CREATION_TAG);
        };

        // Create the dialog builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        final CharacterSelectionBinding binding = CharacterSelectionBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        // Set the new character listener
        binding.newCharacterButton.setOnClickListener(newCharacterListener);

        // Set the adapter for the character table, and get the initial set of names
        adapter = new CharacterAdapter(activity);
        final RecyclerView recyclerView = binding.selectionRecyclerView;
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        
        // Create the dialog and set a few options
        final AlertDialog dialog = builder.create();
        dialog.setOnCancelListener( (DialogInterface di) -> this.dismiss() );
        dialog.setCanceledOnTouchOutside(true);
        return dialog;

    }

    CharacterAdapter getAdapter() { return adapter; }

}
