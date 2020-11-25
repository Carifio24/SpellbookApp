package dnd.jon.spellbook;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

public class CharacterSelectionDialog extends DialogFragment {

    private MainActivity main;
    private CharacterAdapter adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the main activity
        main = (MainActivity) getActivity();

        // Create the new character listener
        final View.OnClickListener newCharacterListener = (View view) -> {
            CreateCharacterDialog dialog = new CreateCharacterDialog();
            dialog.show(main.getSupportFragmentManager(), "createCharacter");
            //FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            //transaction.add(R.id.character_creation, dialog).commit();
        };

        // Create the dialog builder
        final AlertDialog.Builder b = new AlertDialog.Builder(main);

        // Inflate the view and set the builder to use this view
        final LayoutInflater inflater = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.character_selection, null);
        b.setView(view);

        // Set the new character listener
        final Button newCharacterButton = view.findViewById(R.id.new_character_button);
        newCharacterButton.setOnClickListener(newCharacterListener);

        // Set the adapter for the character table, and get the initial set of names
        adapter = new CharacterAdapter(main);
        final RecyclerView recyclerView = view.findViewById(R.id.selection_recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(main));
        //adapter.setCharacterNames(spellbookViewModel.getAllCharacterNamesStatic());

        // Attach the dialog to main and return
        final AlertDialog d = b.create();
        d.setOnCancelListener( (DialogInterface di) -> this.dismiss() );
        d.setCanceledOnTouchOutside(true);
        main.setCharacterSelect(view);
        main.setSelectionDialog(this);
        return d;

    }


    @Override
    public void onDismiss(@NonNull DialogInterface d) {
        super.onDismiss(d);
        System.out.println("Dismissing dialog...");
        main.setCharacterSelect(null);
    }

    CharacterAdapter getAdapter() { return adapter; }

}
