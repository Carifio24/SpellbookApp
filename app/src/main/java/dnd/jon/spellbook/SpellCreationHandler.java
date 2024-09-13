package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import org.javatuples.Pair;
import org.javatuples.Quartet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import dnd.jon.spellbook.databinding.QuantityTypeCreationBinding;
import dnd.jon.spellbook.databinding.SpellCreationBinding;

// The purpose of this class is to allow shared functionality between
// the spell creation fragment and dialog
public class SpellCreationHandler {

    private static final Map<Class<? extends QuantityType>, Quartet<Class<? extends Quantity>, Class<? extends Unit>, Function<SpellCreationBinding,QuantityTypeCreationBinding>, Integer>> quantityTypeInfo = new HashMap<Class<? extends QuantityType>, Quartet<Class<? extends Quantity>, Class<? extends Unit>, Function<SpellCreationBinding,QuantityTypeCreationBinding>, Integer>>() {{
        put(CastingTime.CastingTimeType.class, new Quartet<>(CastingTime.class, TimeUnit.class, (b) -> b.castingTimeSelection, R.string.casting_time));
        put(Duration.DurationType.class, new Quartet<>(Duration.class, TimeUnit.class, (b) -> b.durationSelection, R.string.duration));
        put(Range.RangeType.class, new Quartet<>(Range.class, LengthUnit.class, (b) -> b.rangeSelection, R.string.range));
    }};

    private static final String SOURCE_CREATION_TAG = "SOURCE_CREATION";

    final SpellCreationBinding binding;
    private final FragmentActivity activity;
    private final SpellbookViewModel viewModel;
    private Runnable onSpellCreated;
    final Collection<Source> selectedSources = new ArrayList<>();
    private final String tag;
    private Spell spell = null;

    SpellCreationHandler(FragmentActivity activity, SpellCreationBinding binding, String tag, Spell spell) {
        this.activity = activity;
        this.viewModel = new ViewModelProvider(activity).get(SpellbookViewModel.class);
        this.binding = binding;
        this.tag = tag;
        this.spell = spell;
    }

    void setup() {

        setupSchoolSpinner();
        updateSourceSelectionButtonText();

        // Populate the checkbox grid for caster classes
        populateCheckboxGrid(CasterClass.class, binding.classesSelectionGrid);

        // Populate the options for the quantity types
        populateRangeSelectionWindow(CastingTime.CastingTimeType.class, TimeUnit.class, binding.castingTimeSelection);
        populateRangeSelectionWindow(Duration.DurationType.class, TimeUnit.class, binding.durationSelection);
        populateRangeSelectionWindow(Range.RangeType.class, LengthUnit.class, binding.rangeSelection);

        // Set some reasonable defaults for casting time/duration/range
        SpellbookUtils.setNamedSpinnerByItem(binding.castingTimeSelection.quantityTypeSpinner, CastingTime.CastingTimeType.ACTION);
        SpellbookUtils.setNamedSpinnerByItem(binding.durationSelection.quantityTypeSpinner, Duration.DurationType.SPANNING);
        SpellbookUtils.setNamedSpinnerByItem(binding.rangeSelection.quantityTypeSpinner, Range.RangeType.RANGED);

        // Set up the materials entry to show when the material checkbox is selected, and hide when it isn't
        binding.materialCheckbox.setOnCheckedChangeListener((cb, checked) -> {
            final int materialsVisibility = checked ? View.VISIBLE : View.GONE;
            binding.spellCreationMaterialsContent.setVisibility(materialsVisibility);
        });

        // Do the same for the royalty checkbox and entry
        binding.royaltyCheckbox.setOnCheckedChangeListener((cb, checked) -> {
            final int royaltyVisibility = checked ? View.VISIBLE : View.GONE;
            binding.spellCreationRoyaltyContent.setVisibility(royaltyVisibility);
        });

        // Set up button listeners
        binding.createSpellButton.setOnClickListener(view -> createOrUpdateSpell());
        binding.sourceSelectionButton.setOnClickListener(view -> openSourceSelectionDialog());
        binding.sourceCreationButton.setOnClickListener(view -> openSourceCreationDialog());

        // Determine whether we're creating a new spell, or modifying an existing created spell
        if (spell != null) {
            binding.title.setText(R.string.update_spell);
            binding.createSpellButton.setText(R.string.update_spell);
            selectedSources.addAll(spell.getSourcebooks());
            setSpellInfo(spell);
        }

    }

    void setOnSpellCreated(Runnable runnable) {
        this.onSpellCreated = runnable;
    }

    private void setupSchoolSpinner() {
        final NameDisplayableEnumSpinnerAdapter<School> schoolAdapter = new NameDisplayableEnumSpinnerAdapter<>(activity, School.class);
        binding.schoolSelector.setAdapter(schoolAdapter);
    }

    private <E extends Enum<E> & NameDisplayable> void populateButtonGrid(Class<E> enumType, GridLayout grid, Function<Context, Button> buttonMaker) {
        // Get the enum constants
        final E[] enums = enumType.getEnumConstants();

        // If E somehow isn't an enum type, we return
        // Note that the generic bounds guarantee that this won't happen
        if (enums == null) { return; }

        // For each enum instance, do the following:
        // Create a checkbox with the enum's name as its text
        // Add it to the grid layout
        for (E e : enums) {
            final Button button = buttonMaker.apply(activity);
            button.setText(DisplayUtils.getDisplayName(activity, e));
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

        // Set the choices for the first spinner
        final Spinner optionsSpinner = qtcBinding.quantityTypeSpinner;
        final NameDisplayableEnumSpinnerAdapter<E> optionsAdapter = new NameDisplayableEnumSpinnerAdapter<>(activity, enumType, 12);
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
        final UnitTypeSpinnerAdapter<U> unitAdapter = new UnitTypeSpinnerAdapter<>(activity, unitType, 12);
        unitSpinner.setAdapter(unitAdapter);

    }


    private String sourceSelectionButtonText() {
        if (selectedSources.size() == 0) {
            return activity.getString(R.string.select_sources);
        } else {
            final List<String> codes = selectedSources
                    .stream()
                    .map(source -> DisplayUtils.getCode(source, activity))
                    .collect(Collectors.toList());
            return String.join(", ", codes);
        }
    }

    void updateSourceSelectionButtonText() {
        binding.sourceSelectionButton.setText(sourceSelectionButtonText());
    }

    void setSpellInfo(@NonNull Spell spell) {

        this.spell = spell;

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

        selectedSources.clear();
        selectedSources.addAll(spell.getSourcebooks());
        updateSourceSelectionButtonText();

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
                qtcBinding.spanningValueEntry.setText(DisplayUtils.DECIMAL_FORMAT.format(quantity.getValue()));
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
        binding.royaltyCheckbox.setChecked(components[3]);
        binding.materialsEntry.setText(spell.getMaterial());
        binding.royaltyEntry.setText(spell.getRoyalty());
    }

    private void showErrorMessage(String text) {
        binding.errorText.setText(text);
        binding.spellCreationScroll.fullScroll(ScrollView.FOCUS_UP);
    }

    private void showErrorMessage(int textID) {
        showErrorMessage(activity.getString(textID));
    }

    SortedSet<CasterClass> selectedClasses() {
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

    void createOrUpdateSpell() {
        // Check the spell name
        final String name = binding.nameEntry.getText().toString();
        if (name.isEmpty()) { showErrorMessage(R.string.spell_name_empty); return; }
        final Character illegalCharacter = SpellbookViewModel.firstIllegalCharacter(name);
        if (illegalCharacter != null) {
            showErrorMessage(activity.getString(R.string.illegal_character, activity.getString(R.string.spell_name_lowercase), illegalCharacter.toString()));
            return;
        }

        // Check the spell level
        int level;
        try {
            level = Integer.parseInt(binding.levelEntry.getText().toString());
        } catch (NumberFormatException e) {
            showErrorMessage(activity.getString(R.string.spell_level_range, Spellbook.MIN_SPELL_LEVEL, Spellbook.MAX_SPELL_LEVEL));
            return;
        }

        // Check the components
        final boolean[] components = new boolean[]{ binding.verbalCheckbox.isChecked(), binding.somaticCheckbox.isChecked(), binding.materialCheckbox.isChecked(), binding.royaltyCheckbox.isChecked() };
        final boolean oneChecked = components[0] || components[1] || components[2] || components[3];
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

        // Same for royalty
        final boolean royaltyChecked = components[3];
        final String royaltyString = royaltyChecked ? binding.royaltyEntry.getText().toString() : "";
        if (royaltyChecked && royaltyString.isEmpty()) {
            showErrorMessage(R.string.spell_royalty_empty);
            return;
        }

        // At least one class must be selected
        final SortedSet<CasterClass> classes = selectedClasses();
        if (classes.size() == 0) {
            showErrorMessage(R.string.spell_no_caster_classes);
            return;
        }

        // At least one source must be selected
        // if (selectedSources.size() == 0) {
        //     showErrorMessage(R.string.spell_no_sources);
        //     return;
        // }

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
                        final String quantityTypeName = activity.getResources().getString(quantityTypeNameID);
                        showErrorMessage(activity.getString(R.string.spell_entry_field_empty, quantityTypeName));
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
                Log.e(tag, "Couldn't find constructor:\n" + SpellbookUtils.stackTrace(e));
            } catch (IllegalAccessException | java.lang.InstantiationException |
                     InvocationTargetException e) {
                Log.e(tag, "Error creating quantity:\n" + SpellbookUtils.stackTrace(e));
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
        final SpellBuilder spellBuilder = new SpellBuilder(activity);
        final int id = spell != null ? spell.getID() : viewModel.newSpellID();
        spellBuilder
                .setID(id)
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
                .setRuleset(Ruleset.RULES_CREATED);
        for (Source source : selectedSources) {
            spellBuilder.addLocation(source, -1);
        }

        if (materialChecked) {
            spellBuilder.setMaterial(materialsString);
        }
        if (royaltyChecked) {
            spellBuilder.setRoyalty(royaltyString);
        }

        final Spell newSpell = spellBuilder.build();
        if (spell == null) {
            // Tell the ViewModel about the new spell
            viewModel.addCreatedSpell(newSpell);
        } else {
            viewModel.updateSpell(spell, newSpell);
        }

        if (onSpellCreated != null) {
            onSpellCreated.run();
        }
    }

    private void openSourceSelectionDialog() {
        final Source[] sources = Source.createdSources();
        final String[] sourceNames = DisplayUtils.getDisplayNames(activity, sources, (context, source) -> DisplayUtils.getDisplayName(source, context));

        // It seems like there should be a way to do this with a stream?
        final boolean[] selectedIndices = new boolean[sources.length];
        if (spell != null) {
            for (int i = 0; i < sources.length; i++) {
                selectedIndices[i] = selectedSources.contains(sources[i]);
            }
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog dialog = builder
                .setTitle(R.string.select_sources)
                .setNegativeButton(R.string.cancel, (dialogInterface, index) -> dialogInterface.dismiss())
                .setPositiveButton(R.string.ok, (dialogInterface, index) -> updateSourceSelectionButtonText())
                .setMultiChoiceItems(sourceNames, selectedIndices, (dialogInterface, index, isChecked) -> {
                    final Source source = DisplayUtils.sourceFromDisplayName(activity, sourceNames[index]);
                    final boolean alreadySelected = selectedSources.contains(source);
                    if (isChecked && !alreadySelected) {
                        selectedSources.add(source);
                    } else if (!isChecked && alreadySelected) {
                        selectedSources.remove(source);
                    }
                })
                .create();

        dialog.show();
    }

    private void openSourceCreationDialog() {
        final DialogFragment sourceCreationDialog = new SourceCreationDialog();
        sourceCreationDialog.show(activity.getSupportFragmentManager(), SOURCE_CREATION_TAG);
    }

    Spell getSpell() { return spell; }
}
