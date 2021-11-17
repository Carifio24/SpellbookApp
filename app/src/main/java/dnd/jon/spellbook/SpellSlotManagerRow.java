package dnd.jon.spellbook;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import java.util.List;

import dnd.jon.spellbook.databinding.SpellSlotRowBinding;

public class SpellSlotManagerRow extends LinearLayout {

    private final SpellSlotRowBinding binding;
    private List<CheckBox> checkboxes;
    private final CompoundButton.OnCheckedChangeListener checkboxListener;

    // Constructors
    // This constructor is public so that it can be used via XML
    public SpellSlotManagerRow(Context context, AttributeSet attrs) {
        super(context, attrs);
        binding = SpellSlotRowBinding.inflate((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        addView(binding.getRoot());

        checkboxListener = (checkbox, checked) -> {
            final int change = checked ? 1 : -1;
            final SpellSlotStatus status = binding.getSpellSlotStatus();
            final int level = binding.getLevel();
            final int newUsedSlots = status.getUsedSlots(level) + change;
            status.setUsedSlots(level, newUsedSlots);
        };

        setup();
    }

    private void setup() {
        setupCheckboxes();
    }

    private void setupCheckboxes() {
        final int level = binding.getLevel();
        final SpellSlotStatus status = binding.getSpellSlotStatus();
        final int totalSlots = status.getTotalSlots(level);
        final int usedSlots = status.getUsedSlots(level);

        binding.spellSlotRowLayout.removeAllViews();
        checkboxes.clear();

        for (int i = 0; i < totalSlots; i++) {
            final CheckBox checkbox = new CheckBox(getContext());
            checkbox.setChecked(i < usedSlots);
            checkbox.setOnCheckedChangeListener(checkboxListener);
            checkboxes.add(checkbox);
        }
    }

    void update() { setupCheckboxes(); }

}
