package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import dnd.jon.spellbook.databinding.SpellSlotManagerBinding;

public class SpellSlotManagerFragment extends Fragment {

    private SpellSlotManagerBinding binding;
    private final SpellSlotStatus status;

    public SpellSlotManagerFragment(SpellSlotStatus status) {
        super(R.layout.spell_slot_manager);
        this.status = status;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = SpellSlotManagerBinding.inflate(inflater);
        binding.setSpellSlotStatus(status);
        setupRecycler();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecycler() {
        final SpellSlotAdapter adapter = new SpellSlotAdapter(binding.getSpellSlotStatus());
        binding.spellSlotsRecycler.setAdapter(adapter);
    }

}
