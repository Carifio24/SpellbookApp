package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import dnd.jon.spellbook.databinding.SpellWindowBinding;

public class SpellWindowFragment extends Fragment {

    private SpellWindowBinding binding;
    private SpellbookViewModel spellbookViewModel;
    private LiveData<SpellStatus> spellStatus;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SpellWindowBinding.inflate(inflater);
        spellbookViewModel = new ViewModelProvider(this).get(SpellbookViewModel.class);

        // Set up the star buttons
        setUpButtons();

        // Set the current spell when it changes
        setSpell(spellbookViewModel.getCurrentSpell().getValue());
        spellbookViewModel.getCurrentSpell().observe(getViewLifecycleOwner(), this::setSpell);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setSpell(Spell spell) {
        binding.setSpell(spell);
        binding.executePendingBindings();
        spellStatus = spellbookViewModel.statusForSpell(spell);
    }

    private void setUpButtons() {

        // The favorite button
        binding.favoriteButton.setOnClickListener( (v) -> spellbookViewModel.toggleFavorite(binding.getSpell()));
        spellStatus.observe(getViewLifecycleOwner(), (newStatus) -> binding.favoriteButton.set(newStatus.favorite));

        // The known button
        binding.knownButton.setOnClickListener( (v) -> spellbookViewModel.toggleKnown(binding.getSpell()));
        spellStatus.observe(getViewLifecycleOwner(), (newStatus) -> binding.knownButton.set(newStatus.known));

        // The prepared button
        binding.preparedButton.setOnClickListener( (v) -> spellbookViewModel.togglePrepared(binding.getSpell()));
        spellStatus.observe(getViewLifecycleOwner(), (newStatus) -> binding.preparedButton.set(newStatus.prepared));

    }
}
