package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.stream.IntStream;

import dnd.jon.spellbook.databinding.QuantityTypeCreationBinding;
import dnd.jon.spellbook.databinding.SpellCreationBinding;

public final class SpellCreationActivity extends AppCompatActivity {

    private final SpellBuilder spellBuilder = new SpellBuilder();
    private SpellCreationBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the binding and set the content view as its root view
        binding = SpellCreationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Populate the school spinner
        final String[] schoolNames = Spellbook.schoolNames;
        final ArrayAdapter<String> schoolAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, schoolNames);
        binding.schoolSelector.setAdapter(schoolAdapter);

        // Populate the spell level spinner
        final Integer[] spellLevels = IntStream.rangeClosed(0, 9).boxed().toArray(Integer[]::new);
        final ArrayAdapter<Integer> levelAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, spellLevels);
        binding.levelSelector.setAdapter(levelAdapter);

        // Populate the checkbox grid for caster classes
        populateCheckboxGrid(CasterClass.class, binding.classesSelectionGrid);

        // Populate the options for the quantity types
        populateRangeSelectionWindow(CastingTime.CastingTimeType.class, TimeUnit.class, binding.castingTimeSelection);
        populateRangeSelectionWindow(Duration.DurationType.class, TimeUnit.class, binding.durationSelection);
        populateRangeSelectionWindow(Range.RangeType.class, LengthUnit.class, binding.rangeSelection);

    }

    @Override
    public void onBackPressed() {
        this.finish();
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

    private <E extends Enum<E> & QuantityType, U extends Enum<U> & Unit> void populateRangeSelectionWindow(Class<E> enumType, Class<U> unitType, QuantityTypeCreationBinding qtcBinding) {

        // Set the choices for the first spinner
        final String[] enumNames = EnumUtils.displayNames(enumType);
        final Spinner optionsSpinner = qtcBinding.quantityTypeSpinner;
        final SortFilterSpinnerAdapter adapter = new SortFilterSpinnerAdapter(this, enumNames, 12);
        optionsSpinner.setAdapter(adapter);

        // Get the index of the spanning type
        final E[] enums = enumType.getEnumConstants();
        if (enums == null) { return; }
        int spanningIndex = 0;
        final String spanningName = enums[0].getSpanningType().getDisplayName();
        for (int i = 0; i < optionsSpinner.getCount(); ++i) {
            if ( (optionsSpinner.getItemAtPosition(i)).equals(spanningName)) {
                spanningIndex = i;
            }
        }
        optionsSpinner.setTag(spanningIndex);


        // If the spanning type is selected, we want to display the spanning option choices
        optionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Show or hide the spanning stuff as needed
                final int spanningIndex = (int) parent.getTag();
                final int spanningVisibility = (spanningIndex == position) ? View.VISIBLE : View.GONE;
                qtcBinding.spanningTypeContent.setVisibility(spanningVisibility);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        optionsSpinner.setSelection(0);

        // Populate the spanning type elements
        // Note that they're hidden to start
        final String[] unitNames = EnumUtils.unitPluralNames(unitType);
        final Spinner unitSpinner = qtcBinding.spanningUnitSelector;
        final SortFilterSpinnerAdapter unitAdapter = new SortFilterSpinnerAdapter(this, unitNames, 12);
        unitSpinner.setAdapter(unitAdapter);


    }



}
