package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;
import java.util.stream.IntStream;

import dnd.jon.spellbook.databinding.HigherLevelSlotSelectionBinding;

public class HigherLevelSlotDialog extends DialogFragment {

    private HigherLevelSlotSelectionBinding binding;
    private SpellbookViewModel viewModel;
    final static String SPELL_KEY = "spell";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = requireActivity();
        viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory()).get(SpellbookViewModel.class);

        final Bundle args = getArguments();
        final Spell spell = args != null ? args.getParcelable(SPELL_KEY) : null;
        final int baseLevel = spell != null ? spell.getLevel() : 1;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        binding = HigherLevelSlotSelectionBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());

        final int maxLevel = viewModel.getSpellSlotStatus().maxLevelWithSlots();
        final String[] options = Arrays.copyOfRange(activity.getResources().getStringArray(R.array.ordinal_numbers), Math.max(baseLevel - 1, 0), maxLevel);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.spinner_item, options);
        binding.higherLevelSlotSpinner.setAdapter(adapter);

        binding.higherLevelSlotCancel.setOnClickListener((v) -> this.dismiss());

        binding.higherLevelSlotCast.setOnClickListener((v) -> {
            final int level = binding.higherLevelSlotSpinner.getSelectedItemPosition() + 1;
            viewModel.castSpell(spell, level);
            this.dismiss();
        });

        return builder.create();
    }
}
