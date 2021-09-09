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
    private int usedSlots;

    // Constructors
    // This constructor is public so that it can be used via XML
    public SpellSlotManagerRow(Context context, AttributeSet attrs) {
        super(context, attrs);
        binding = SpellSlotRowBinding.inflate((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        addView(binding.getRoot());

        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SpellSlotManagerRow, 0, 0);
        level = typedArray.getInt(R.styleable.SpellSlotManagerRow_spellLevel, 1);
        totalSlots = Math.max(typedArray.getInt(R.styleable.SpellSlotManagerRow_totalSlots, 0), 0);
        usedSlots = Math.max(typedArray.getInt(R.styleable.SpellSlotManagerRow_usedSlots, 0), totalSlots);
        typedArray.recycle();

        setup();
    }

    private void setup() {
        if (totalSlots <= 0) { return; }

        final LinearLayout layout = binding.spellSlotRowLayout;
        final Context context = getContext();
        for (int i = 0; i < totalSlots; i++) {
            final CheckBox checkBox = new CheckBox(context);
            checkBox.setChecked(usedSlots > i);
            layout.addView(checkBox);
        }
    }

    // Getters
    int getTotalSlots() { return totalSlots; }
    int getUsedSlots() { return usedSlots; }
    int getAvailableSlots() { return totalSlots - usedSlots; }
    int getLevel() { return level; }

    // Setters
    void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }
    void setUsedSlots(int usedSlots) { this.usedSlots = Math.max(usedSlots, this.totalSlots); }

}
