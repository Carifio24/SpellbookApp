package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import dnd.jon.spellbook.databinding.SpellCreationBinding;

public class SpellCreationDialog extends DialogFragment {
    private static final String TAG = "SpellCreationDialog";
    private static final String DELETE_SPELL_DIALOG_TAG = "DeleteSpellDialog";
    private static final String SPELL_KEY = "spell";
    private SpellbookViewModel viewModel;
    private SpellCreationHandler handler;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        final FragmentActivity activity = requireActivity();
        viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);

        Spell editingSpell = viewModel.currentEditingSpell().getValue();
        Spell spell = editingSpell;
        if (savedInstanceState != null) {
            spell = savedInstanceState.getParcelable(SPELL_KEY);
            if (editingSpell == null && spell != null) {
                viewModel.setCurrentEditingSpell(spell);
            }
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = getLayoutInflater();
        final SpellCreationBinding binding = SpellCreationBinding.inflate(inflater);
        handler = new SpellCreationHandler(activity, binding, TAG, spell);
        handler.setOnSpellCreated(this::dismiss);
        handler.setup();
        builder.setView(binding.getRoot());
        if (spell != null && spell.equals(editingSpell)) {
            binding.deleteSpellButton.setVisibility(View.VISIBLE);
            final Spell spellToDelete = spell;
            binding.deleteSpellButton.setOnClickListener(v -> {
                final DeleteSpellDialog dialog = new DeleteSpellDialog();
                dialog.setOnConfirm(this::dismiss);
                final Bundle args = new Bundle();
                args.putString(DeleteSpellDialog.NAME_KEY, spellToDelete.getName());
                dialog.setArguments(args);
                dialog.show(activity.getSupportFragmentManager(), DELETE_SPELL_DIALOG_TAG);
            });
        }
        viewModel.currentEditingSpell().observe(requireActivity(), (newSpell) -> {
            if (newSpell != null) {
                handler.setSpellInfo(newSpell);
            }
        });
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SPELL_KEY, viewModel.currentEditingSpell().getValue());
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        viewModel.setCurrentEditingSpell(null);
        super.onDismiss(dialog);
    }
}
