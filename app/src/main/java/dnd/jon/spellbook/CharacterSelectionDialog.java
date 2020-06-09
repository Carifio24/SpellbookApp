package dnd.jon.spellbook;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import dnd.jon.spellbook.databinding.CharacterSelectionBinding;
import dnd.jon.spellbook.databinding.CharacterTableRowBinding;

public class CharacterSelectionDialog extends DialogFragment {

    private CharacterSelectionBinding binding;
    private SpellbookViewModel spellbookViewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the main activity
        final FragmentActivity activity = requireActivity();

        // Get the ViewModel
        spellbookViewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);

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
        binding = CharacterSelectionBinding.inflate(getLayoutInflater());
        b.setView(binding.getRoot());

        // Set the new character listener
        final Button newCharacterButton = binding.newCharacterButton;
        newCharacterButton.setOnClickListener(newCharacterListener);

        // Populate the character table
        populateTable(spellbookViewModel.getAllCharacterNames().getValue());

        // Attach the dialog to main and return
        final AlertDialog d = b.create();
        d.setOnCancelListener( (DialogInterface di) -> this.dismiss() );
        d.setCanceledOnTouchOutside(true);
        return d;

    }

    private void populateTable(List<String> names) {
        final TableLayout tableLayout = binding.selectionTable;
        for (String name : names) {
            final CharacterTableRowBinding rowBinding = CharacterTableRowBinding.inflate(getLayoutInflater());
            final TableRow tableRow = rowBinding.characterRow;
            rowBinding.characterRowText.setText(name);
            rowBinding.characterRowButton.setTag(name);

            // Create the text listener and set it to the label
            final TextView.OnClickListener textListener = (View view) -> {
                final TextView tv = (TextView) view;
                final String charName = tv.getText().toString();
                spellbookViewModel.setCharacter(charName);

                // Show a Toast message after selection
                Toast.makeText(requireActivity(), "Character selected: " + name, Toast.LENGTH_SHORT).show();
            };
            rowBinding.characterRowText.setOnClickListener(textListener);

            // Create the delete button listener and set it to the button
            final ImageButton.OnClickListener deleteListener = (View view) -> {
                final String charName = (String) view.getTag();
                final Bundle args = new Bundle();
                args.putString(DeleteCharacterDialog.nameKey, charName);
                final DeleteCharacterDialog dialog = new DeleteCharacterDialog();
                dialog.setArguments(args);
                dialog.show(requireActivity().getSupportFragmentManager(), "confirmDeleteCharacter");
            };
            rowBinding.characterRowButton.setOnClickListener(deleteListener);
            tableLayout.addView(tableRow);
        }
    }

}
