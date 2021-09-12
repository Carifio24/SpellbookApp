package dnd.jon.spellbook;

import android.view.LayoutInflater;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import dnd.jon.spellbook.databinding.SpellSlotRowBinding;

public class SpellSlotAdapter extends RecyclerView.Adapter<SpellSlotAdapter.SpellSlotRowHolder> {

    // Member values
    private final Context context;
    private final SpellSlotStatus status;

    // Constructor
    SpellSlotAdapter(Context context, SpellSlotStatus status) {
        this.context = context;
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
        //return status.maxLevelWithSlots();
        return Spellbook.MAX_SPELL_LEVEL;
    }


    // The RowHolder class
    protected class SpellSlotRowHolder extends RecyclerView.ViewHolder {

        private final SpellSlotRowBinding binding;

        public SpellSlotRowHolder(SpellSlotRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setup(SpellSlotStatus status, int level) {
            if (status == null) { return; }

            binding.setSpellSlotStatus(status);
            binding.setLevel(level);
            binding.executePendingBindings();

            final Locale locale = context.getResources().getConfiguration().getLocales().get(0);
            final int totalSlots = status.getTotalSlots(level);
            final int availableSlots = status.getAvailableSlots(level);
            final NumberSelector numberSelector = binding.spellSlotRowSelector;
            numberSelector.setValue(availableSlots);
            binding.spellRowTotalSlots.setText(String.format(locale, "%d", totalSlots));
        }
    }
}
