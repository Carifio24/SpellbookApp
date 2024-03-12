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

        if (savedInstanceState != null) {
            final Spell spell = savedInstanceState.getParcelable(SPELL_KEY);
            if (viewModel.currentSpell().getValue() == null && spell != null) {
                viewModel.setCurrentEditingSpell(spell);
            }
        }

        handler = new SpellCreationHandler(activity, binding, TAG);
        handler.setOnSpellCreated(() -> {
            final NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            navHostFragment.getNavController().navigateUp();
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handler.setup();
    }

    @Override
    public void onStop() {
        viewModel.setCurrentEditingSpell(null);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SPELL_KEY, viewModel.currentEditingSpell().getValue());
    }

}
