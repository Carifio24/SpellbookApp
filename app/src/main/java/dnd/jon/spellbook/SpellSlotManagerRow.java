package dnd.jon.spellbook;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import dnd.jon.spellbook.databinding.SpellSlotRowBinding;

public class SpellSlotManagerRow extends LinearLayout {

    private final SpellSlotRowBinding binding;
    private final int level;
    private int totalSlots;
    private int availableSlots;

    // Constructors
    // This constructor is public so that it can be used via XML
    public SpellSlotManagerRow(Context context, AttributeSet attrs) {
        super(context, attrs);
        binding = SpellSlotRowBinding.inflate((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        addView(binding.getRoot());

        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SpellSlotManagerRow, 0, 0);
        level = typedArray.getInt(R.styleable.SpellSlotManagerRow_spellLevel, 1);
        totalSlots = Math.max(typedArray.getInt(R.styleable.SpellSlotManagerRow_totalSlots, 0), 0);
        availableSlots = Math.max(typedArray.getInt(R.styleable.SpellSlotManagerRow_availableSlots, 0), totalSlots);
        typedArray.recycle();

        setup();
    }

    private void setup() {
        if (totalSlots <= 0) { return; }

        final NumberSelector numberSelector = binding.spellSlotRowSelector;
        numberSelector.setValue(availableSlots);
    }

    // Getters
    int getTotalSlots() { return totalSlots; }
    int getUsedSlots() { return totalSlots - availableSlots; }
    int getAvailableSlots() { return availableSlots; }
    int getLevel() { return level; }

    // Setters
    void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }
    void setAvailableSlots(int availableSlots) { this.availableSlots = Math.max(0, Math.min(availableSlots, this.totalSlots)); }

}
