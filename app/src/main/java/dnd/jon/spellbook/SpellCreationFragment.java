package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import dnd.jon.spellbook.databinding.SpellCreationBinding;

public final class SpellCreationFragment extends SpellbookFragment<SpellCreationBinding> {
    private static final String TAG = "SpellCreationFragment";
    private static final String SPELL_KEY = "spell";
    private SpellCreationHandler handler;

    public SpellCreationFragment() {
        super(R.layout.spell_creation);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = SpellCreationBinding.inflate(inflater);

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

        handler = new SpellCreationHandler(activity, binding, TAG, spell);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handler.setup();
        handler.setOnSpellCreated(() -> {
            final NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            navHostFragment.getNavController().navigateUp();
        });
        viewModel.currentEditingSpell().observe(requireActivity(), (newSpell) -> {
            if (newSpell != null) {
                handler.setSpellInfo(newSpell);
            }
        });
    }

    @Override
    public void onStop() {
        viewModel.setCurrentEditingSpell(null);
        super.onStop();
    }

    // Note that for API > 28, `onStop` is called before `onSaveInstanceState`
    // This poses a bit of a problem for us - we want to set the editing spell
    // to null in `onStop`, but we want to keep track of what it was in `onSaveInstanceState`
    // So we need to pull out the spell from the handler
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SPELL_KEY, handler.getSpell());
    }

}
