package dnd.jon.spellbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import dnd.jon.spellbook.databinding.SpellSlotManagerBinding;

public class SpellSlotManagerDialog extends DialogFragment {

    private FragmentActivity activity;
    private SpellSlotManagerBinding binding;
    private SpellSlotAdapter adapter;
    private SpellbookViewModel viewModel;
    private Observable.OnPropertyChangedCallback spellSlotStatusCallback;
    private static final String SPELL_SLOT_ADJUST_TOTALS_TAG = "adjustSpellSlotTotals";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        activity = requireActivity();
        viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        binding = SpellSlotManagerBinding.inflate(inflater);
        binding.setStatus(viewModel.getSpellSlotStatus());
        binding.slotManagerEditButton.setVisibility(View.VISIBLE);
        binding.slotManagerEditButton.setOnClickListener((v) -> openEditDialog());
        binding.slotManagerRegainButton.setVisibility(View.VISIBLE);
        binding.slotManagerRegainButton.setOnClickListener((v) -> viewModel.getSpellSlotStatus().regainAllSlots());
        builder.setView(binding.getRoot());
        setupRecycler();
        setupSpellSlotListeners();

        return builder.create();
    }

    private void openEditDialog() {
        final SpellSlotStatus spellSlotStatus = viewModel.getSpellSlotStatus();
        final Bundle args = new Bundle();
        args.putParcelable(SpellSlotAdjustTotalsDialog.SPELL_SLOT_STATUS_KEY, spellSlotStatus);
        final SpellSlotAdjustTotalsDialog dialog = new SpellSlotAdjustTotalsDialog();
        dialog.setArguments(args);
        dialog.show(activity.getSupportFragmentManager(), SPELL_SLOT_ADJUST_TOTALS_TAG);
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
