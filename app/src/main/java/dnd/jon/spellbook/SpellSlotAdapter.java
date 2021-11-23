package dnd.jon.spellbook;

import android.view.LayoutInflater;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dnd.jon.spellbook.databinding.SpellSlotRowBinding;

public class SpellSlotAdapter extends RecyclerView.Adapter<SpellSlotAdapter.SpellSlotRowHolder> {

    // Member values
    private final Context context;
    private SpellSlotStatus status;

    private static final float SCALE_FACTOR = 1.1F;

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

    void setSpellSlotStatus(SpellSlotStatus status) {
        this.status = status;
        this.refresh();
    }

    void refresh() {
        this.notifyDataSetChanged();
    }

    // The RowHolder class
    protected static class SpellSlotRowHolder extends RecyclerView.ViewHolder {

        private final SpellSlotRowBinding binding;
        private final List<CheckBox> checkboxes;
        private final CompoundButton.OnCheckedChangeListener checkboxListener;

        public SpellSlotRowHolder(SpellSlotRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.checkboxes = new ArrayList<>();

            checkboxListener = (checkbox, checked) -> {
                final int change = checked ? 1 : -1;
                final SpellSlotStatus status = this.binding.getSpellSlotStatus();
                final int level = this.binding.getLevel();
                final int newUsedSlots = status.getUsedSlots(level) + change;
                status.setUsedSlots(level, newUsedSlots);
            };
        }

        void setup(SpellSlotStatus status, int level) {
            if (status == null) { return; }

            binding.setSpellSlotStatus(status);
            binding.setLevel(level);
            binding.executePendingBindings();

            setupCheckboxes();

        }

        private void setupCheckboxes() {
            final int level = binding.getLevel();
            final SpellSlotStatus status = binding.getSpellSlotStatus();
            final int totalSlots = status.getTotalSlots(level);
            final int usedSlots = status.getUsedSlots(level);

            binding.spellSlotRowCheckboxesContainer.removeAllViews();
            checkboxes.clear();

            for (int i = 0; i < totalSlots; i++) {
                final CheckBox checkbox = new CheckBox(itemView.getContext());
                checkbox.setChecked(i < usedSlots);
                checkbox.setScaleX(SCALE_FACTOR);
                checkbox.setScaleY(SCALE_FACTOR);
                checkbox.setOnCheckedChangeListener(checkboxListener);
                binding.spellSlotRowCheckboxesContainer.addView(checkbox);
                checkboxes.add(checkbox);
            }
        }
    }
}
