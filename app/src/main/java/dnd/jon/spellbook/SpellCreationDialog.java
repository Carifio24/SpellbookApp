package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import dnd.jon.spellbook.databinding.SpellCreationBinding;

public class SpellCreationDialog extends DialogFragment {
    private static final String TAG = "SpellCreationDialog";
    private FragmentActivity activity;
    private SpellbookViewModel viewModel;
    private SpellCreationBinding binding;
    private SpellCreationHandler handler;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        activity = requireActivity();
        viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final LayoutInflater inflater = getLayoutInflater();
        binding = SpellCreationBinding.inflate(inflater);
        handler = new SpellCreationHandler(activity, binding, TAG);
        handler.setup();
        builder.setView(binding.getRoot());

        return builder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        viewModel.setCurrentEditingSpell(null);
        super.onDismiss(dialog);
    }
}
