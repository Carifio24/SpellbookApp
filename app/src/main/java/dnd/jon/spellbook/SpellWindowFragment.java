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

    static final String SPELL_KEY = "spell";
    static final String TEXT_SIZE_KEY = "textSize";
    static final String INDEX_KEY = "index";
    static final String FAVORITE_KEY = "favorite";
    static final String KNOWN_KEY = "known";
    static final String PREPARED_KEY = "prepared";

    private SpellWindowBinding binding;
    private SpellbookViewModel spellbookViewModel;
    private LiveData<SpellStatus> spellStatus;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SpellWindowBinding.inflate(inflater);
        spellbookViewModel = new ViewModelProvider(this).get(SpellbookViewModel.class);

        // Set up the star buttons
        setUpButtons();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    void setSpell(Spell spell) { binding.setSpell(spell); }


    private void setUpButtons() {

        // The favorite button
        binding.favoriteButton.setOnClickListener( (v) -> spellbookViewModel.toggleFavorite(binding.getSpell()));
        spellStatus.observe(getViewLifecycleOwner(), (newStatus) -> binding.favoriteButton.set(newStatus.favorite));

        // The known button
        binding.knownButton.setOnClickListener( (v) -> spellbookViewModel.toggleKnown(binding.getSpell()));
        spellStatus.observe(getViewLifecycleOwner(), (newStatus) -> binding.knownButton.set(newStatus.known));

        // The known button
        binding.preparedButton.setOnClickListener( (v) -> spellbookViewModel.togglePrepared(binding.getSpell()));
        spellStatus.observe(getViewLifecycleOwner(), (newStatus) -> binding.preparedButton.set(newStatus.prepared));

    }
}
