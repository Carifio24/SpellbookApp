package dnd.jon.spellbook;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class CharacterSelectionDialog extends DialogFragment {

    private CharacterAdapter adapter;
    private SpellbookViewModel spellbookViewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the main activity
        final FragmentActivity activity = requireActivity();

        // Get the ViewModel
        spellbookViewModel = new ViewModelProvider(requireActivity(), new SpellbookViewModelFactory(activity.getApplication())).get(SpellbookViewModel.class);

        // Create the new character listener
        final View.OnClickListener newCharacterListener = (View view) -> {
            final CreateCharacterDialog dialog = new CreateCharacterDialog();
            dialog.show(requireActivity().getSupportFragmentManager(), "createCharacter");
            //FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            //transaction.add(R.id.character_creation, dialog).commit();
        };

        // Create the dialog builder
        final AlertDialog.Builder b = new AlertDialog.Builder(activity);

        // Inflate the view and set the builder to use this view
        // Inflate the view and set the builder to use this view
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.character_selection, null);
        b.setView(view);

        // Set the new character listener
        final Button newCharacterButton = view.findViewById(R.id.new_character_button);
        newCharacterButton.setOnClickListener(newCharacterListener);

        // Set the adapter for the character table
        adapter = new CharacterAdapter(activity, spellbookViewModel);
        final RecyclerView recyclerView = view.findViewById(R.id.selection_recycler_view);
        recyclerView.setAdapter(adapter);

        // If the character list changes, we want to repopulate the table
        spellbookViewModel.getAllCharacterNames().observe(this, (names) -> adapter.setCharacterNames(names));

        // Attach the dialog to main and return
        final AlertDialog d = b.create();
        d.setOnCancelListener( (DialogInterface di) -> this.dismiss() );
        d.setCanceledOnTouchOutside(true);
        return d;

    }

}
