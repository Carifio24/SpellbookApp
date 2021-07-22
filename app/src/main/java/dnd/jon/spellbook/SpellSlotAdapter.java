package dnd.jon.spellbook;

import android.view.LayoutInflater;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dnd.jon.spellbook.databinding.SpellSlotRowBinding;

public class SpellSlotAdapter extends RecyclerView.Adapter<SpellSlotAdapter.SpellSlotRowHolder> {

    // Member values
    private final SpellSlotStatus status;

    // Constructor
    SpellSlotAdapter(SpellSlotStatus status) {
        this.status = status;
    }

    // ViewHolder methods
    @NonNull
    public SpellSlotRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final SpellSlotRowBinding binding = SpellSlotRowBinding.inflate(inflater, parent, false);
        return new SpellSlotRowHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SpellSlotAdapter.SpellSlotRowHolder holder, int position) {
        if ( (position >= Spellbook.MAX_SPELL_LEVEL) || position < 0) {
            return;
        }

        holder.setup(status, position + 1);
    }

    @Override
    public int getItemCount() {
        return status.maxLevelWithSlots();
    }


    // The RowHolder class
    protected static class SpellSlotRowHolder extends RecyclerView.ViewHolder {

        private final SpellSlotRowBinding binding;

        public SpellSlotRowHolder(SpellSlotRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setup(SpellSlotStatus status, int level) {
            final int totalSlots = status.getTotalSlots(level);
            final int usedSlots = status.getUsedSlots(level);
            if (usedSlots < 0) { return; }
            final LinearLayout linearLayout = binding.spellSlotRowLayout;
            final Context context = itemView.getContext();
            for (int i = 0; i < totalSlots; i++) {
                final RadioButton radioButton = new RadioButton(context);
                radioButton.setChecked(i < usedSlots);
                radioButton.setOnCheckedChangeListener((button, checked) -> {
                    if (checked) {
                        status.useSlot(level);
                    } else {
                        status.gainSlot(level);
                    }
                });
                linearLayout.addView(radioButton);
            }
        }
    }
}
