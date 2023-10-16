package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import dnd.jon.spellbook.databinding.YesNoBinding;

public class ConfirmNextAvailableCastDialog extends DialogFragment {

    static final String SPELL_KEY = "spell";
    static final String LEVEL_KEY = "level";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        final Bundle args = getArguments();
        final int level = args != null ? args.getInt(LEVEL_KEY, 0) : 0;
        final Spell spell = args != null ? args.getParcelable(SPELL_KEY) : null;

        if (spell == null || level == 0) {
            this.dismiss();
        }

        final FragmentActivity activity = requireActivity();
        final SpellbookViewModel viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory()).get(SpellbookViewModel.class);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final YesNoBinding binding = YesNoBinding.inflate(activity.getLayoutInflater());
        binding.yesButton.setOnClickListener((v) -> {
            viewModel.castSpell(spell, level);
            dismiss();
        });
        binding.noButton.setOnClickListener((v) -> dismiss());
        binding.yesNoTitle.setText(activity.getString(R.string.use_higher_level_slot_query));
        binding.yesNoMessage.setText(getString(R.string.want_to_cast_next_available_level, viewModel.getProfile().getName(), spell.getLevel(), level));

        builder.setView(binding.getRoot());
        return builder.create();
    }
}
