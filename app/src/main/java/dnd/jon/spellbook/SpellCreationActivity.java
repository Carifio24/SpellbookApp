package dnd.jon.spellbook;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.content.Intent;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.javatuples.Quartet;

import dnd.jon.spellbook.databinding.QuantityTypeCreationBinding;
import dnd.jon.spellbook.databinding.SpellCreationBinding;

public final class SpellCreationActivity extends AppCompatActivity {

    private static final String SPELL_KEY = "spell";

    private final SpellBuilder spellBuilder = new SpellBuilder();
    private SpellCreationBinding binding;

    private final Intent returnIntent = new Intent(SpellCreationActivity.this, MainActivity.class);

    private static final Map<Class<? extends QuantityType>, Quartet<Class<? extends Quantity>, Class<? extends Unit>, Function<SpellCreationBinding,QuantityTypeCreationBinding>, Integer>> quantityTypeInfo = new HashMap<Class<? extends QuantityType>, Quartet<Class<? extends Quantity>, Class<? extends Unit>, Function<SpellCreationBinding,QuantityTypeCreationBinding>, Integer>>() {{
        put(CastingTime.CastingTimeType.class, new Quartet<>(CastingTime.class, TimeUnit.class, (b) -> b.castingTimeSelection, R.string.casting_time));
        put(Duration.DurationType.class, new Quartet<>(Duration.class, TimeUnit.class, (b) -> b.durationSelection, R.string.duration));
        put(Range.RangeType.class, new Quartet<>(Range.class, LengthUnit.class, (b) -> b.rangeSelection, R.string.range));
    }};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the binding and set the content view as its root view
        binding = SpellCreationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set the toolbar as the app bar for the activity
        final Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.spell_creation);

        // Set up the back arrow on the navigation bar
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener((v) -> this.finish());

        // Populate the school spinner
        final NameDisplayableSpinnerAdapter<School> schoolAdapter = new NameDisplayableSpinnerAdapter<>(this, School.class);
        binding.schoolSelector.setAdapter(schoolAdapter);

        // Populate the checkbox grid for caster classes
        populateCheckboxGrid(CasterClass.class, binding.classesSelectionGrid);

        // Populate the options for the quantity types
        populateRangeSelectionWindow(CastingTime.CastingTimeType.class, TimeUnit.class, binding.castingTimeSelection);
        populateRangeSelectionWindow(Duration.DurationType.class, TimeUnit.class, binding.durationSelection);
        populateRangeSelectionWindow(Range.RangeType.class, LengthUnit.class, binding.rangeSelection);

        // Set up the materials entry to show when the material checkbox is selected, and hide when it isn't
        binding.materialCheckbox.setOnCheckedChangeListener((cb, checked) -> {
            final int visibility = checked ? View.VISIBLE : View.GONE;
            binding.materialsEntry.setVisibility(visibility);
        });

        // Set up the create spell button
        binding.createSpellButton.setOnClickListener( (v) -> createSpell() );

    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED, returnIntent);
        this.finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.identity, android.R.anim.slide_out_right);
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
            button.setTag(e);
            radioGrid.addView(button);
        }
    }

    private <E extends Enum<E> & QuantityType, U extends Enum<U> & Unit> void populateRangeSelectionWindow(Class<E> enumType, Class<U> unitType, QuantityTypeCreationBinding qtcBinding) {

        // Set the choices for the first spinner
        final Spinner optionsSpinner = qtcBinding.quantityTypeSpinner;
        final NameDisplayableSpinnerAdapter optionsAdapter = new NameDisplayableSpinnerAdapter(this, enumType, 12);
        optionsSpinner.setAdapter(optionsAdapter);

        // If the spanning type is selected, we want to display the spanning option choices
        optionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Show or hide the spanning stuff as needed
                final E type = enumType.cast(parent.getItemAtPosition(position));
                final int spanningVisibility = type.isSpanningType() ? View.VISIBLE : View.GONE;
                qtcBinding.spanningTypeContent.setVisibility(spanningVisibility);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        optionsSpinner.setSelection(0);

        // Populate the spanning type elements
        // Note that they're hidden to start
        final Spinner unitSpinner = qtcBinding.spanningUnitSelector;
        final UnitTypeSpinnerAdapter unitAdapter = new UnitTypeSpinnerAdapter(this, unitType, 12);
        unitSpinner.setAdapter(unitAdapter);

    }

    private List<CasterClass> selectedClasses() {
        final List<CasterClass> classes = new ArrayList<>();
        final GridLayout grid = binding.classesSelectionGrid;
        for (int i = 0; i < grid.getChildCount(); ++i) {
            final CheckBox cb = (CheckBox) grid.getChildAt(i);
            if (cb.isChecked()) {
                classes.add((CasterClass) cb.getTag());
            }
        }
        return classes;
    }

    private void showErrorMessage(String text) {
        binding.errorText.setText(text);
        binding.spellCreationScroll.fullScroll(ScrollView.FOCUS_UP);
    }

    private void createSpell() {

        // Check the spell name
        final String name = binding.nameEntry.getText().toString();
        final String spellNameString = "spell name";
        if (name.isEmpty()) { showErrorMessage("The spell name is empty"); return; }
        for (Character c : SpellbookUtils.illegalCharacters) {
            final String cStr = c.toString();
            if (name.contains(cStr)) {
                showErrorMessage(getString(R.string.illegal_character, spellNameString, cStr));
                return;
            }
        }

        // Check the spell level
        int level;
        try {
            level = Integer.parseInt(binding.levelEntry.getText().toString());
        } catch (NumberFormatException e) {
            showErrorMessage(String.format(Locale.US, "The spell level must be an integer between %d and %d", Spellbook.MIN_SPELL_LEVEL, Spellbook.MAX_SPELL_LEVEL));
            return;
        }

        // Check the components
        final boolean[] components = new boolean[]{ binding.verbalCheckbox.isChecked(), binding.somaticCheckbox.isChecked(), binding.materialCheckbox.isChecked() };
        final boolean oneChecked = components[0] || components[1] || components[2];
        if (!oneChecked) {
            showErrorMessage("The spell has no components selected."); return;
        }

        // If material is selected, check that the materials description isn't empty
        final boolean materialChecked = components[2];
        final String materialsString = materialChecked ? binding.materialsEntry.getText().toString() : "";
        if (materialChecked && materialsString.isEmpty()) {
            showErrorMessage("The description of the material components is empty.");
            return;
        }

        // Get the selected classes
        // At least one class must be selected
        final List<CasterClass> classes = selectedClasses();
        System.out.println("There are " + classes.size() + " classes selected");
        if (classes.size() == 0) {
            showErrorMessage("No caster classes are selected.");
            return;
        }

        // If one of the spanning types is selected, the text field needs to be filled out
        final Map<Class<? extends QuantityType>, Quantity> quantityValues = new HashMap<>();
        for (Map.Entry<Class<? extends QuantityType>, Quartet<Class<? extends Quantity>, Class<? extends Unit>, Function<SpellCreationBinding,QuantityTypeCreationBinding>, Integer>> entry : quantityTypeInfo.entrySet()) {

            final Class<? extends QuantityType> quantityType = entry.getKey();
            final Quartet<Class<? extends Quantity>, Class<? extends Unit>, Function<SpellCreationBinding,QuantityTypeCreationBinding>, Integer> data = entry.getValue();
            final Class<? extends Quantity> quantityClass = data.getValue0();
            final QuantityTypeCreationBinding qtcBinding = data.getValue2().apply(binding);

            final Spinner quantityTypeSpinner = qtcBinding.quantityTypeSpinner;
            final QuantityType type = quantityType.cast(quantityTypeSpinner.getSelectedItem());

            // Get the quantity
            Quantity quantity = null;
            try {
                if (type.isSpanningType()) {
                    final String spanningText = qtcBinding.spanningValueEntry.getText().toString();
                    final boolean spanningTextMissing = spanningText.isEmpty();
                    if (spanningTextMissing) {
                        final int quantityTypeNameID = data.getValue3();
                        final String quantityTypeName = getResources().getString(quantityTypeNameID);
                        showErrorMessage("The entry field for " + quantityTypeName + " is empty.");
                        return;
                    }
                    final Class<? extends Unit> unitType = data.getValue1();
                    final Unit unit = unitType.cast(qtcBinding.spanningUnitSelector.getSelectedItem());
                    final int value = Integer.parseInt(qtcBinding.spanningValueEntry.toString());
                    final Constructor constructor = quantityClass.getDeclaredConstructor(quantityType, int.class, unitType);
                    quantity = quantityClass.cast(constructor.newInstance(type, value, unit));
                } else {
                    final Constructor constructor = quantityClass.getDeclaredConstructor(quantityType);
                    quantity = quantityClass.cast(constructor.newInstance(type));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            quantityValues.put(quantityType, quantity);

        }

        // Check if the description is empty
        final String description = binding.descriptionEntry.getText().toString();
        if (description.isEmpty()) {
            showErrorMessage("The spell description is empty.");
            return;
        }

        // Once we've passed all of the checks, create the spell
        final Spell spell = spellBuilder
                .setName(name)
                .setSchool(School.fromDisplayName((String) binding.schoolSelector.getSelectedItem()))
                .setLevel(level)
                .setRitual(binding.ritualSelector.isChecked())
                .setConcentration(binding.concentrationSelector.isChecked())
                .setCastingTime((CastingTime) quantityValues.get(CastingTime.CastingTimeType.class))
                .setRange((Range) quantityValues.get(Range.RangeType.class))
                .setComponents(components)
                .setDuration((Duration) quantityValues.get(Duration.DurationType.class))
                .setClasses(classes)
                .setDescription(description)
                .setHigherLevelDesc(binding.higherLevelEntry.getText().toString())
                .build();

        // Add the spell to the return intent and finish the activity
        returnIntent.putExtra(SPELL_KEY, spell);
        setResult(Activity.RESULT_OK, returnIntent);


    }




}
