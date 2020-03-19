package dnd.jon.spellbook;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

import dnd.jon.spellbook.databinding.SpellCreationBinding;

class SpellCreationActivity extends AppCompatActivity {

    private final SpellBuilder spellBuilder = new SpellBuilder();
    private SpellCreationBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the binding and set the content view as its root view
        binding = SpellCreationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Populate the checkbox grid for caster classes
        populateCheckboxGrid(CasterClass.class, binding.classesSelectionGrid);

    }

    private <E extends Enum<E> & NameDisplayable> void populateCheckboxGrid(Class<E> enumType, GridLayout grid) {

        // Get the enum constants
        final E[] enums = enumType.getEnumConstants();

        // If E somehow isn't an enum type, we return
        // Note that the generic bounds guarantee that this won't happen
        if (enums == null) { return; }

        // For each enum instance, do the following:
        // Create a checkbox with the enum's name as its text
        // Add it to the grid layout
        for (E e : enums) {
            final CheckBox checkBox = new CheckBox(this);
            checkBox.setText(e.getDisplayName());
            checkBox.setTag(e);
            grid.addView(checkBox);
        }
    }

    private <E extends Enum<E> & QuantityType> void populateRadioGrid(Class<E> enumType, RadioGridGroup radioGrid) {

        // Get the enum constants
        final E[] enums = enumType.getEnumConstants();

        // If E somehow isn't an enum type, we return
        // Note that the generic bounds guarantee that this won't happen
        if (enums == null) { return; }

        // For each enum instance, do the following:
        // Create a radio with the enum's name as its text
        // Add it to the radio group
        for (E e : enums) {
            final RadioButton button = new RadioButton(this);
            button.setText(e.getDisplayName());
            button.setTag(this);
            radioGrid.addView(button);
        }


    }




}
