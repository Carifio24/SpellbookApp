package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

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
        final Integer[] levels = IntStream.rangeClosed(baseLevel, maxLevel).boxed().toArray(Integer[]::new);
        final ArrayAdapter<Integer> adapter = new ArrayAdapter<>(activity, R.layout.spinner_item, levels);
        binding.higherLevelSlotSpinner.setAdapter(adapter);

        binding.higherLevelSlotCancel.setOnClickListener((v) -> this.dismiss());

        binding.higherLevelSlotCast.setOnClickListener((v) -> {
            final int level = (int) binding.higherLevelSlotSpinner.getSelectedItem();
            viewModel.castSpell(spell, level);
            this.dismiss();
        });

        return builder.create();
    }
}
