package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.javatuples.Pair;
import org.javatuples.Quartet;

import dnd.jon.spellbook.databinding.QuantityTypeCreationBinding;
import dnd.jon.spellbook.databinding.SpellCreationBinding;

public final class SpellCreationFragment extends SpellbookFragment<SpellCreationBinding> {

    private static final String SPELL_KEY = "spell";

    private static final String TAG = "SpellCreationFragment"; // For logging

    private List<String> selectedSourceNames = new ArrayList<>();

    private static final Map<Class<? extends QuantityType>, Quartet<Class<? extends Quantity>, Class<? extends Unit>, Function<SpellCreationBinding,QuantityTypeCreationBinding>, Integer>> quantityTypeInfo = new HashMap<Class<? extends QuantityType>, Quartet<Class<? extends Quantity>, Class<? extends Unit>, Function<SpellCreationBinding,QuantityTypeCreationBinding>, Integer>>() {{
        put(CastingTime.CastingTimeType.class, new Quartet<>(CastingTime.class, TimeUnit.class, (b) -> b.castingTimeSelection, R.string.casting_time));
        put(Duration.DurationType.class, new Quartet<>(Duration.class, TimeUnit.class, (b) -> b.durationSelection, R.string.duration));
        put(Range.RangeType.class, new Quartet<>(Range.class, LengthUnit.class, (b) -> b.rangeSelection, R.string.range));
    }};

    public SpellCreationFragment() { super(R.layout.spell_creation); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = SpellCreationBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setup();
    }

    @Override
    public void onStop() {
        viewModel.setCurrentEditingSpell(null);
        super.onStop();
    }

    private void setup() {

        setupSchoolSpinner();
        setUpSourceSpinner();
        viewModel.currentCreatedSources().observe(getViewLifecycleOwner(), (sources) -> setUpSourceSpinner());

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

        // Determine whether we're creating a new spell, or modifying an existing created spell
        final Spell spell = viewModel.currentEditingSpell().getValue();
        if (spell != null) {
            setSpellInfo(spell);
        }

    }

    private void setupSchoolSpinner() {
        final NameDisplayableEnumSpinnerAdapter<School> schoolAdapter = new NameDisplayableEnumSpinnerAdapter<>(context, School.class);
        binding.schoolSelector.setAdapter(schoolAdapter);
    }

    private void setUpSourceSpinner() {
        final BiFunction<Context,Source,String> nameGetter = (context, source) -> DisplayUtils.getDisplayName(source, context);
        final NamedSpinnerAdapter<Source> sourceAdapter = new NamedSpinnerAdapter<>(context, Source.values(), nameGetter);
        binding.sourceSelector.setAdapter(sourceAdapter);
    }

    private <E extends Enum<E> & NameDisplayable> void populateButtonGrid(Class<E> enumType, GridLayout grid, Function<Context,Button> buttonMaker) {
        // Get the enum constants
        final E[] enums = enumType.getEnumConstants();

        // If E somehow isn't an enum type, we return
        // Note that the generic bounds guarantee that this won't happen
        if (enums == null) { return; }

        // For each enum instance, do the following:
        // Create a checkbox with the enum's name as its text
        // Add it to the grid layout
        final Context context = requireContext();
        for (E e : enums) {
            final Button button = buttonMaker.apply(context);
            button.setText(DisplayUtils.getDisplayName(context, e));
            button.setTag(e);
            grid.addView(button);
        }
    }

    private <E extends Enum<E> & NameDisplayable> void populateCheckboxGrid(Class<E> enumType, GridLayout grid) {
        populateButtonGrid(enumType, grid, CheckBox::new);
    }

    private <E extends Enum<E> & QuantityType> void populateRadioGrid(Class<E> enumType, RadioGridGroup radioGrid) {
        populateButtonGrid(enumType, radioGrid, RadioButton::new);
    }

    private <E extends Enum<E> & QuantityType, U extends Enum<U> & Unit> void populateRangeSelectionWindow(Class<E> enumType, Class<U> unitType, QuantityTypeCreationBinding qtcBinding) {

        // Get the context
        final Context context = requireContext();

        // Set the choices for the first spinner
        final Spinner optionsSpinner = qtcBinding.quantityTypeSpinner;
        final NameDisplayableEnumSpinnerAdapter<E> optionsAdapter = new NameDisplayableEnumSpinnerAdapter<>(context, enumType, 12);
        optionsSpinner.setAdapter(optionsAdapter);

        // If the spanning type is selected, we want to display the spanning option choices
        optionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Show or hide the spanning stuff as needed
                final E type = enumType.cast(parent.getItemAtPosition(position));
                if (type == null) { return; }
                final int spanningVisibility = type.isSpanningType() ? View.VISIBLE : View.GONE;
                qtcBinding.spanningTypeContent.setVisibility(spanningVisibility);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                qtcBinding.spanningTypeContent.setVisibility(View.GONE);
            }
        });
        optionsSpinner.setSelection(0);

        // Populate the spanning type elements
        // Note that they're hidden to start
        final Spinner unitSpinner = qtcBinding.spanningUnitSelector;
        final UnitTypeSpinnerAdapter<U> unitAdapter = new UnitTypeSpinnerAdapter<>(context, unitType, 12);
        unitSpinner.setAdapter(unitAdapter);

    }

    private SortedSet<CasterClass> selectedClasses() {
        final TreeSet<CasterClass> classes = new TreeSet<>();
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

    private void showErrorMessage(int textID) {
        showErrorMessage(requireActivity().getString(textID));
    }

    private void setSpellInfo(Spell spell) {

        // Set any text fields
        binding.nameEntry.setText(spell.getName());
        binding.levelEntry.setText(String.format(LocalizationUtils.getLocale(), "%d", spell.getLevel()));
        binding.descriptionEntry.setText(spell.getDescription());
        binding.higherLevelEntry.setText(spell.getHigherLevel());

        // Set the ritual and concentration switches
        binding.ritualSelector.setChecked(spell.getRitual());
        binding.concentrationSelector.setChecked(spell.getConcentration());

        // Set the school spinner to the correct position
        SpellbookUtils.setNamedSpinnerByItem(binding.schoolSelector, spell.getSchool());

        // Set the source spinner to the correct position
        // TODO: What is the best way to deal with this?
        // Currently, created spells only have a single source
        final ArrayAdapter<Source> sourceAdapter = (ArrayAdapter<Source>) binding.sourceSelector.getAdapter();
        final Optional<Source> source = spell.getSourcebooks().stream().findAny();
        if (source.isPresent()) {
            final int position = sourceAdapter.getPosition(source.get());
            binding.sourceSelector.setSelection(position);
        }

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
            SpellbookUtils.setNamedSpinnerByItem(qtcBinding.quantityTypeSpinner, quantity.type);
            final boolean spanningType = quantityType.isSpanningType();
            qtcBinding.spanningTypeContent.setVisibility(spanningType ? View.VISIBLE : View.GONE);
            if (spanningType) {
                qtcBinding.spanningValueEntry.setText(String.format(LocalizationUtils.getLocale(), "%f", quantity.getValue()));
                SpellbookUtils.setNamedSpinnerByItem(qtcBinding.spanningUnitSelector, (Enum) quantity.getUnit());
            }

        }

        // Set the checkboxes in the class selection grid
        final Collection<CasterClass> spellClasses = spell.getClasses();
        for (int i = 0; i < binding.classesSelectionGrid.getChildCount(); ++i) {
            final View view = binding.classesSelectionGrid.getChildAt(i);
            if (view instanceof CheckBox) {
                final CheckBox cb = (CheckBox) view;
                final CasterClass cc = (CasterClass) cb.getTag();
                cb.setChecked(spellClasses.contains(cc));
            }
        }

        final boolean[] components = spell.getComponents();
        binding.verbalCheckbox.setChecked(components[0]);
        binding.somaticCheckbox.setChecked(components[1]);
        binding.materialCheckbox.setChecked(components[2]);

    }

    private void createSpell() {

        // Check the spell name
        final String name = binding.nameEntry.getText().toString();
        if (name.isEmpty()) { showErrorMessage(R.string.spell_name_empty); return; }
        final Character illegalCharacter = SpellbookViewModel.firstIllegalCharacter(name);
        if (illegalCharacter != null) {
            showErrorMessage(getString(R.string.illegal_character, getString(R.string.spell_name_lowercase), illegalCharacter.toString()));
            return;
        }

        // Check the spell level
        int level;
        try {
            level = Integer.parseInt(binding.levelEntry.getText().toString());
        } catch (NumberFormatException e) {
            showErrorMessage(getString(R.string.spell_level_range, Spellbook.MIN_SPELL_LEVEL, Spellbook.MAX_SPELL_LEVEL));
            return;
        }

        // Check the components
        final boolean[] components = new boolean[]{ binding.verbalCheckbox.isChecked(), binding.somaticCheckbox.isChecked(), binding.materialCheckbox.isChecked() };
        final boolean oneChecked = components[0] || components[1] || components[2];
        if (!oneChecked) {
            showErrorMessage(R.string.spell_no_components); return;
        }

        // If material is selected, check that the materials description isn't empty
        final boolean materialChecked = components[2];
        final String materialsString = materialChecked ? binding.materialsEntry.getText().toString() : "";
        if (materialChecked && materialsString.isEmpty()) {
            showErrorMessage(R.string.spell_material_empty);
            return;
        }

        // Get the selected classes
        // At least one class must be selected
        final SortedSet<CasterClass> classes = selectedClasses();
        if (classes.size() == 0) {
            showErrorMessage(R.string.spell_no_caster_classes);
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
                        showErrorMessage(getString(R.string.spell_entry_field_empty, quantityTypeName));
                        return;
                    }
                    final Class<? extends Unit> unitType = data.getValue1();
                    final Unit unit = unitType.cast(qtcBinding.spanningUnitSelector.getSelectedItem());
                    final String valueString = qtcBinding.spanningValueEntry.getText().toString();
                    final int value = Integer.parseInt(valueString);
                    final Constructor<? extends Quantity> constructor = quantityClass.getDeclaredConstructor(quantityType, float.class, unitType, String.class);
                    quantity = quantityClass.cast(constructor.newInstance(type, value, unit, ""));
                } else {
                    final Constructor<? extends Quantity> constructor = quantityClass.getDeclaredConstructor(quantityType);
                    quantity = quantityClass.cast(constructor.newInstance(type));
                }
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "Couldn't find constructor:\n" + SpellbookUtils.stackTrace(e));
            } catch (IllegalAccessException | java.lang.InstantiationException | InvocationTargetException e) {
                Log.e(TAG, "Error creating quantity:\n" + SpellbookUtils.stackTrace(e));
            }
            quantityValues.put(quantityType, quantity);

        }

        // Check if the description is empty
        final String description = binding.descriptionEntry.getText().toString();
        if (description.isEmpty()) {
            showErrorMessage(R.string.spell_description_empty);
            return;
        }

        // Once we've passed all of the checks, create the spell
        final FragmentActivity activity = requireActivity();
        final SpellBuilder spellBuilder = new SpellBuilder(activity);
        final Spell spell = spellBuilder
                .setName(name)
                .setSchool((School) binding.schoolSelector.getSelectedItem())
                .setLevel(level)
                .setRitual(binding.ritualSelector.isChecked())
                .setConcentration(binding.concentrationSelector.isChecked())
                .setCastingTime((CastingTime) quantityValues.get(CastingTime.CastingTimeType.class))
                .setDuration((Duration) quantityValues.get(Duration.DurationType.class))
                .setRange((Range) quantityValues.get(Range.RangeType.class))
                .setComponents(components)
                .setClasses(classes)
                .setDescription(description)
                .setHigherLevelDesc(binding.higherLevelEntry.getText().toString())
                .addLocation((Source) binding.sourceSelector.getSelectedItem(), -1)
                .build();

        // Tell the ViewModel about the new spell
        viewModel.addCreatedSpell(spell);

        final NavHostFragment navHostFragment = (NavHostFragment) activity.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navHostFragment.getNavController().navigateUp();

    }

    private void displaySourceSelectionDialog() {
        final String[] sourceNames = DisplayUtils.getDisplayNames(context, Source.values(), (context, source) -> DisplayUtils.getDisplayName(source, context));
        final Boolean[] selectedIndices = Arrays.stream(Source.values())
                .map(source -> DisplayUtils.getDisplayName(source, context))
                .map(name -> selectedSourceNames.contains(name))
                .collect(Collectors.toList()).toArray(Boolean[]::new);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog dialog = builder.setTitle(0)  // TODO: Create string resource for title and use its ID here
                //.setMultiChoiceItems(sourceNames, selectedIndices)

                .create();

        dialog.show();


    }

}
