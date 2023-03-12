package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import dnd.jon.spellbook.databinding.HomebrewManagementBinding;

public class HomebrewManagementFragment extends SpellbookFragment<HomebrewManagementBinding> {

    private HomebrewItemsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = HomebrewManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new HomebrewItemsAdapter(context, viewModel.currentCreatedSpells().getValue());
        binding.createdItemsEl.setAdapter(adapter);

        // Set up the adapter to open the spell editing window when a child is clicked
        binding.createdItemsEl.setOnChildClickListener((elView, vw, gp, cp, id) -> {
            final Spell spell = (Spell) adapter.getChild(gp, cp);
            viewModel.setCurrentEditingSpell(spell);
            return true;
        });

        // Update the list of spells whenever a spell is added/deleted
        viewModel.currentCreatedSpells().observe(getViewLifecycleOwner(), adapter::updateSpells);
    }

}
