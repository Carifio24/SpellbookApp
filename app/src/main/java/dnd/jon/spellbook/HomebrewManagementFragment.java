package dnd.jon.spellbook;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import dnd.jon.spellbook.databinding.HomebrewManagementBinding;

public class HomebrewManagementFragment extends SpellbookFragment<HomebrewManagementBinding> {

    private HomebrewItemsAdapter adapter;

    private static final String SOURCE_CREATION_TAG = "SOURCE_CREATION";
    private static final String SPELL_CREATION_TAG = "SPELL_CREATION";

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

        // requireActivity().registerForContextMenu(binding.createdItemsEl);

        // Set up the adapter to open the spell editing window when a child is clicked
        binding.createdItemsEl.setOnChildClickListener((elView, vw, gp, cp, id) -> {
            final Spell spell = (Spell) adapter.getChild(gp, cp);
            viewModel.setCurrentEditingSpell(spell);
            navController().navigate(R.id.action_homebrewManagementFragment_to_spellCreationFragment);
            return true;
        });

        // Update the list of spells whenever a spell is added/deleted
        viewModel.currentCreatedSpells().observe(getViewLifecycleOwner(), adapter::updateSpells);

        // Set up the FAB
        final SpeedDialView speedDialView = binding.speeddialHomebrewFab;
        final Resources.Theme theme = context.getTheme();
        final int darkBrown = getResources().getColor(R.color.darkBrown, theme);
        final int transparent = getResources().getColor(android.R.color.transparent, theme);
        final int white = getResources().getColor(android.R.color.white, theme);
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.homebrew_fab_add_source, R.drawable.book_filled)
                        .setLabel(R.string.homebrew_add_source)
                        .setLabelBackgroundColor(transparent)
                        .setLabelColor(white)
                        .setFabImageTintColor(white)
                        .setFabBackgroundColor(darkBrown)
                        .create()
        );
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.homebrew_fab_add_spell, R.drawable.wand_filled)
                        .setLabel(R.string.homebrew_add_spell)
                        .setLabelBackgroundColor(transparent)
                        .setLabelColor(white)
                        .setFabImageTintColor(white)
                        .setFabBackgroundColor(darkBrown)
                        .create()
        );

        speedDialView.setOnActionSelectedListener(actionItem -> {
            boolean handled = false;
           if (actionItem.getId() == R.id.homebrew_fab_add_source) {
               final SourceCreationDialog dialog = new SourceCreationDialog();
               dialog.show(requireActivity().getSupportFragmentManager(), SOURCE_CREATION_TAG);
               handled = true;
           }
           if (actionItem.getId() == R.id.homebrew_fab_add_spell) {
               navController().navigate(R.id.action_homebrewManagementFragment_to_spellCreationFragment);
               handled = true;
           }
           if (handled) {
               speedDialView.close();
           }
           return handled;
        });

    }

    private NavController navController() {
        final NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        return navHostFragment.getNavController();
    }

}