package dnd.jon.spellbook;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.content.Intent;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

import org.javatuples.Pair;
import org.javatuples.Quartet;

import dnd.jon.spellbook.databinding.QuantityTypeCreationBinding;
import dnd.jon.spellbook.databinding.SpellCreationBinding;

public final class SpellCreationActivity extends AppCompatActivity {

    static final String SPELL_KEY = "spell";
    static final String CLASS_IDS_KEY = "class_ids";

    private final SpellBuilder spellBuilder = new SpellBuilder();
    private SpellCreationBinding binding;

    private Intent returnIntent;
    private SpellbookRepository repository;

    private static final String TAG = "SpellCreationActivity"; // For logging

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
        toolbar.setNavigationOnClickListener((v) -> this.exit());

        // Populate the source adapter
        repository = new SpellbookRepository(this.getApplication());
        final DisplayNameSpinnerAdapter<Source> sourceAdapter = new DisplayNameSpinnerAdapter<>(this, repository.getCreatedSources().toArray(new Source[0]), Source::getDisplayName);
        binding.sourceSelector.setAdapter(sourceAdapter);

        // Populate the school spinner
        final DisplayNameSpinnerAdapter<School> schoolAdapter = new DisplayNameSpinnerAdapter<>(this, repository.getAllSchools().toArray(new School[0]), School::getDisplayName);
        binding.schoolSelector.setAdapter(schoolAdapter);

        // Populate the checkbox grid for caster classes
        populateCheckboxGrid(repository.getAllClasses().toArray(new CasterClass[0]), binding.classesSelectionGrid);

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

        // Determine whether we're creating a new spell, or modifying an existing created spell
        final Intent intent = getIntent();
        final boolean newSpell = intent.hasExtra(SPELL_KEY);
        //final boolean newSpell = intent.getBooleanExtra("New", false);
        if (newSpell) {
            final Spell spell = intent.getParcelableExtra(SPELL_KEY);
            if (spell != null) {
                setSpellInfo(spell);
            }
        }

        // Create the return intent
        returnIntent = new Intent(SpellCreationActivity.this, CreationManagementActivity.class);

    }

    private void exit() {
        setResult(Activity.RESULT_CANCELED, returnIntent);
        this.finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.identity, android.R.anim.slide_out_right);
    }

    private <T extends Named, G extends GridLayout, B extends CompoundButton> void populateGrid(T[] items, G grid, Function<Context,B> buttonMaker) {

        // If items is null, we return
        if (items == null) { return; }

        // For eachitem, do the following:
        // Create a checkbox with the enum's name as its text
        // Add it to the grid layout
        for (T t : items) {
            final B button = buttonMaker.apply(this);
            button.setText(t.getDisplayName());
            button.setTag(t);
            grid.addView(button);
        }

    }

    private <E extends Enum<E> & Named, G extends GridLayout, B extends CompoundButton> void populateGrid(Class<E> enumType, G grid, Function<Context,B> buttonMaker) {
        populateGrid(enumType.getEnumConstants(), grid, buttonMaker);
    }
    private <T extends Named> void populateCheckboxGrid(T[] items, GridLayout grid) { populateGrid(items, grid, CheckBox::new); }
    private <E extends Enum<E> & Named> void populateCheckboxGrid(Class<E> enumType, GridLayout grid) { populateCheckboxGrid(enumType.getEnumConstants(), grid); }
    //private <E extends Enum<E> & QuantityType> void populateRadioGrid(Class<E> enumType, RadioGridGroup radioGrid) { populateGrid(enumType, radioGrid, RadioButton::new); }


    private <E extends Enum<E> & QuantityType, U extends Enum<U> & Unit> void populateRangeSelectionWindow(Class<E> enumType, Class<U> unitType, QuantityTypeCreationBinding qtcBinding) {

        // Set the choices for the first spinner
        final Spinner optionsSpinner = qtcBinding.quantityTypeSpinner;
        final NamedEnumSpinnerAdapter optionsAdapter = new NamedEnumSpinnerAdapter(this, enumType, 12);
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

    private List<Integer> selectedClassIDs() {
        final List<Integer> classIDs = new ArrayList<>();
        final GridLayout grid = binding.classesSelectionGrid;
        for (int i = 0; i < grid.getChildCount(); ++i) {
            final CheckBox cb = (CheckBox) grid.getChildAt(i);
            if (cb.isChecked()) {
                classIDs.add( ((CasterClass) cb.getTag()).getId() );
            }
        }
        return classIDs;
    }

    private void showErrorMessage(String text) {
        binding.errorText.setText(text);
        binding.spellCreationScroll.fullScroll(ScrollView.FOCUS_UP);
    }

    private void setSpellInfo(Spell spell) {

        // Set any text fields
//        binding.nameEntry.setText(spell.getName());
//        binding.levelEntry.setText(String.format(Locale.US, "%d", spell.getLevel()));
//        binding.descriptionEntry.setText(spell.getDescription());
//        binding.higherLevelEntry.setText(spell.getHigherLevel());

        // Set the ritual and concentration switches
        //binding.ritualSelector.setChecked(spell.getRitual());
        //binding.concentrationSelector.setChecked(spell.getConcentration());

        // Set fields from the data binding
        binding.setSpell(spell);
        binding.executePendingBindings();

        // Set the school spinner to the correct position
        AndroidUtils.setSpinnerByItem(binding.schoolSelector, repository.getSchoolByID(spell.getSchoolID()));

        // Set the quantity type UI elements
        final List<Pair<QuantityTypeCreationBinding, Function<Spell,Quantity>>> spinnersAndGetters = Arrays.asList(
            new Pair<>(binding.castingTimeSelection, Spell::getCastingTime),
            new Pair<>(binding.durationSelection, Spell::getDuration),
            new Pair<>(binding.rangeSelection, Spell::getRange)
        );
        for (Pair<QuantityTypeCreationBinding, Function<Spell,Quantity>> pair : spinnersAndGetters) {
            final QuantityTypeCreationBinding qtcBinding = pair.getValue0();
            final Function<Spell, Quantity> quantityGetter = pair.getValue1();
            final Quantity quantity = quantityGetter.apply(spell);
            final QuantityType quantityType = (QuantityType) quantity.type;
            AndroidUtils.setSpinnerByItem(qtcBinding.quantityTypeSpinner, quantity.type);
            if (quantityType.isSpanningType()) {
                qtcBinding.spanningValueEntry.setText(String.format(Locale.US, "%d", quantity.getValue()));
               AndroidUtils.setSpinnerByItem(qtcBinding.spanningUnitSelector, quantity.getUnit());
            }
        }

        // Set the checkboxes in the class selection grid
        final List<Long> spellClassIDs = repository.getClassIDs(spell);
        for (int i = 0; i < binding.classesSelectionGrid.getChildCount(); ++i) {
            final View view = binding.classesSelectionGrid.getChildAt(i);
            if (view instanceof RadioButton) {
                final RadioButton rb = (RadioButton) view;
                final CasterClass cc = (CasterClass) rb.getTag();
                rb.setChecked(spellClassIDs.contains(cc.getId()));
            }
        }
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

        // Check the spell page
        int page;
        try {
            page = Integer.parseInt(binding.pageEntry.getText().toString());
        } catch (NumberFormatException e) {
            page = 0;
        }

        // Check the components
        final boolean verbal = binding.verbalCheckbox.isChecked();
        final boolean somatic = binding.somaticCheckbox.isChecked();
        final boolean material = binding.materialCheckbox.isChecked();
        final boolean oneChecked = verbal || somatic || material;
        if (!oneChecked) {
            showErrorMessage("The spell has no components selected."); return;
        }

        // If material is selected, check that the materials description isn't empty
        final String materialsString = material ? binding.materialsEntry.getText().toString() : "";
        if (material && materialsString.isEmpty()) {
            showErrorMessage("The description of the material components is empty.");
            return;
        }

        // Get the selected classes
        // At least one class must be selected
        final List<Integer> classIDs = selectedClassIDs();
        System.out.println("There are " + classIDs.size() + " classes selected");
        if (classIDs.size() == 0) {
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
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "Couldn't find constructor:\n" + SpellbookUtils.stackTrace(e));
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                Log.e(TAG, "Error creating quantity:\n" + SpellbookUtils.stackTrace(e));
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
                .setLevel(level)
                .setRitual(binding.ritualSelector.isChecked())
                .setConcentration(binding.concentrationSelector.isChecked())
                .setCastingTime((CastingTime) quantityValues.get(CastingTime.CastingTimeType.class))
                .setRange((Range) quantityValues.get(Range.RangeType.class))
                .setVerbalComponent(verbal)
                .setSomaticComponent(somatic)
                .setMaterialComponent(material)
                .setMaterials(materialsString)
                .setDuration((Duration) quantityValues.get(Duration.DurationType.class))
                .setDescription(description)
                .setHigherLevelDesc(binding.higherLevelEntry.getText().toString())
                .setSourceID( ((Source) binding.sourceSelector.getSelectedItem()).getId())
                .setPage(page)
                .build();

        // Add the spell to the return intent and finish the activity
        returnIntent.putExtra(SPELL_KEY, spell);
        returnIntent.putExtra(CLASS_IDS_KEY, classIDs.toArray());
        setResult(Activity.RESULT_OK, returnIntent);


    }




}
