package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import dnd.jon.spellbook.databinding.SpellSlotManagerBinding;

public class SpellSlotManagerFragment extends Fragment {

    private SpellSlotManagerBinding binding;
    private SpellbookViewModel viewModel;
    private SpellSlotAdapter adapter;
    private Observable.OnPropertyChangedCallback spellSlotStatusCallback;

    public SpellSlotManagerFragment() {
        super(R.layout.spell_slot_manager);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final FragmentActivity activity = requireActivity();
        this.viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);
        this.binding = SpellSlotManagerBinding.inflate(inflater);
        viewModel.currentSpellSlotStatus().observe(getViewLifecycleOwner(), this::updateSpellSlotStatus);

        setupRecycler();
        setupSpellSlotListeners();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecycler() {
        adapter = new SpellSlotAdapter(requireContext(), viewModel.getSpellSlotStatus());
        binding.spellSlotsRecycler.setAdapter(adapter);
        binding.spellSlotsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupSpellSlotListeners() {
        final SpellSlotStatus status = viewModel.getSpellSlotStatus();
        status.removeOnPropertyChangedCallback(spellSlotStatusCallback);

        this.spellSlotStatusCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (sender != status) { return; }
                if (propertyId == BR.totalSlotsFlag || propertyId == BR.usedSlotsFlag) {
                    adapter.refresh();
                }
            }
        };
        status.addOnPropertyChangedCallback(spellSlotStatusCallback);
    }

    private void updateSpellSlotStatus(SpellSlotStatus status) {
        adapter.setSpellSlotStatus(status);
        adapter.refresh();
        setupSpellSlotListeners();
    }


}
