package dnd.jon.spellbook;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import dnd.jon.spellbook.databinding.CreationManagementBinding;

public class CreationManagementFragment extends Fragment {

    private CreationManagementBinding binding;
    private SpellbookViewModel spellbookViewModel;
    private CreatedItemsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = CreationManagementBinding.inflate(inflater);
        spellbookViewModel = new ViewModelProvider(requireActivity(),new SpellbookViewModelFactory(requireActivity().getApplication())).get(SpellbookViewModel.class);
        adapter = new CreatedItemsAdapter(requireContext());
        binding.createdItemsEl.setAdapter(adapter);

        // Set up the adapter to open the spell editing window when a child is clicked
        binding.createdItemsEl.setOnChildClickListener((elView, view, gp, cp, id) -> {
            final CreatedItemsAdapter adapter = (CreatedItemsAdapter) elView.getAdapter();
            final Spell spell = (Spell) adapter.getChild(gp, cp);
            openSpellEditor(spell);
            return true;
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.SPELL_MODIFICATION_REQUEST && resultCode == Activity.RESULT_OK) {
            final Spell spell = data.getParcelableExtra(SpellCreationActivity.SPELL_KEY);
            spellbookViewModel.updateSpell(spell);
        }
    }

    private void openSpellEditor(Spell spell) {
        final Activity activity = requireActivity();
        final Intent intent = new Intent(activity, SpellCreationActivity.class);
        Bundle options = ActivityOptions.makeCustomAnimation(activity, R.anim.identity, android.R.anim.slide_in_left).toBundle();
        options.putParcelable(SpellCreationActivity.SPELL_KEY, spell);
        startActivityForResult(intent, RequestCodes.SPELL_MODIFICATION_REQUEST, options);
    }








}
