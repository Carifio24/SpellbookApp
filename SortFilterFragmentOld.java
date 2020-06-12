package dnd.jon.spellbook;

import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import dnd.jon.spellbook.databinding.ComponentsFilterLayoutBinding;
import dnd.jon.spellbook.databinding.FilterBlockLayoutBinding;
import dnd.jon.spellbook.databinding.FilterBlockRangeLayoutBinding;
import dnd.jon.spellbook.databinding.ItemFilterViewBinding;
import dnd.jon.spellbook.databinding.LevelFilterLayoutBinding;
import dnd.jon.spellbook.databinding.RangeFilterLayoutBinding;
import dnd.jon.spellbook.databinding.RitualConcentrationLayoutBinding;
import dnd.jon.spellbook.databinding.SortFilterLayoutBinding;
import dnd.jon.spellbook.databinding.SortLayoutBinding;
import dnd.jon.spellbook.databinding.YesNoFilterViewBinding;

public class SortFilterFragment extends Fragment {

    private static final HashMap<Class<? extends Named>,  Quartet<Boolean, Function<SortFilterLayoutBinding, ViewBinding>, Integer, Integer>> filterBlockInfo = new HashMap<Class<? extends Named>, Quartet<Boolean, Function<SortFilterLayoutBinding, ViewBinding>, Integer, Integer>>() {{
        put(Sourcebook.class, new Quartet<>(false, (b) -> (ViewBinding) b.sourcebookFilterBlock, R.string.sourcebook_filter_title, R.integer.sourcebook_filter_columns));
        put(CasterClass.class, new Quartet<>(false, (b) -> (ViewBinding) b.casterFilterBlock, R.string.caster_filter_title, R.integer.caster_filter_columns));
        put(School.class, new Quartet<>(false, (b) -> (ViewBinding) b.schoolFilterBlock, R.string.school_filter_title, R.integer.school_filter_columns));
        put(CastingTime.CastingTimeType.class, new Quartet<>(true, (b) -> (ViewBinding) b.castingTimeFilterRange, R.string.casting_time_type_filter_title, R.integer.casting_time_type_filter_columns));
        put(Duration.DurationType.class, new Quartet<>(true, (b) -> (ViewBinding) b.durationFilterRange, R.string.duration_type_filter_title, R.integer.duration_type_filter_columns));
        put(Range.RangeType.class, new Quartet<>(true, (b) -> (ViewBinding) b.rangeFilterRange, R.string.range_type_filter_title, R.integer.range_type_filter_columns));
    }};

    // The Triples consist of
    // Superclass, min text, max text, max entry length
    private static final HashMap<Class<? extends QuantityType>, Triplet<Class<? extends Unit>, Integer, Integer>> rangeViewInfo = new HashMap<Class<? extends QuantityType>, Triplet<Class<? extends Unit>, Integer, Integer>>()  {{
        put(CastingTime.CastingTimeType.class, new Triplet<>(TimeUnit.class, R.string.casting_time_range_text, R.integer.casting_time_max_length));
        put(Duration.DurationType.class, new Triplet<>(TimeUnit.class, R.string.duration_range_text, R.integer.duration_max_length));
        put(Range.RangeType.class, new Triplet<>(LengthUnit.class, R.string.range_range_text, R.integer.range_max_length));
    }};

    // Header/expanding views
    private final Map<View,View> expandingViews = new HashMap<>();

    private SortFilterLayoutBinding binding;
    private SpellbookViewModel spellbookViewModel;

    private final Map<Class<? extends Named>, Map<Named,ToggleButton>> filterButtonMaps = new HashMap<>();
    private final Map<Class<? extends QuantityType>, RangeFilterLayoutBinding> classToRangeMap = new HashMap<>();

    private final Map<Class<? extends Named>, List<ItemFilterViewBinding>> classToBindingsMap = new HashMap<>();
    private final List<YesNoFilterViewBinding> yesNoBindings = new ArrayList<>();



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SortFilterLayoutBinding.inflate(inflater);
        spellbookViewModel = new ViewModelProvider(requireActivity()).get(SpellbookViewModel.class);

        // Set up the UI elements
        setup();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setUpSortElements() {

        // The context
        final Context context = getContext();

        // Get various UI elements
        final SortLayoutBinding sortBinding = binding.sortingBlock;
        final Spinner sort1 = sortBinding.sortField1Spinner;
        final Spinner sort2 = sortBinding.sortField2Spinner;
        final ToggleButton sortArrow1 = sortBinding.sortField1Arrow;
        final ToggleButton sortArrow2 = sortBinding.sortField2Arrow;

        // Set the views to be expanded
        expandingViews.put(sortBinding.sortHeader, sortBinding.sortContent);

        // Set tags for the sorting UI elements
        sort1.setTag(1);
        sort2.setTag(2);
        sortArrow1.setTag(1);
        sortArrow2.setTag(2);

        // Populate the dropdown spinners
        final int sortTextSize = 18;
        final DisplayNameSpinnerAdapter<SortField> sortAdapter1 = new DisplayNameSpinnerAdapter<>(context, SortField.class, SortField::getDisplayName, sortTextSize);
        final DisplayNameSpinnerAdapter<SortField> sortAdapter2 = new DisplayNameSpinnerAdapter<>(context, SortField.class, SortField::getDisplayName, sortTextSize);
        sort1.setAdapter(sortAdapter1);
        sort2.setAdapter(sortAdapter2);


        // Set what happens when the sort spinners are changed
        final AdapterView.OnItemSelectedListener sortListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                final int level = (int) adapterView.getTag();
                final SortField sf = (SortField) adapterView.getItemAtPosition(position);;
                spellbookViewModel.setSortField(sf, level);
                spellbookViewModel.setFilterNeeded(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        sort1.setOnItemSelectedListener(sortListener);
        sort2.setOnItemSelectedListener(sortListener);

        // Set what happens when the arrow buttons are pressed
        final ToggleButton.OnClickListener arrowListener = (View view) -> {
            final ToggleButton button = (ToggleButton) view;
            final boolean up = button.isSet();
            final int level = (int) view.getTag();
            spellbookViewModel.setSortReverse(up, level);
            spellbookViewModel.setSortNeeded(true);
        };
        sortArrow1.setOnClickListener(arrowListener);
        sortArrow2.setOnClickListener(arrowListener);

        // Set the LiveData observers
        final LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        spellbookViewModel.getFirstSortField().observe(lifecycleOwner, (sf) -> SpellbookUtils.setNamedSpinnerByItem(sort1, sf));
        spellbookViewModel.getSecondSortField().observe(lifecycleOwner, (sf) -> SpellbookUtils.setNamedSpinnerByItem(sort2, sf));
        spellbookViewModel.getFirstSortReverse().observe(lifecycleOwner, sortArrow1::set);
        spellbookViewModel.getSecondSortReverse().observe(lifecycleOwner, sortArrow2::set);

    }


    // The code for populating the filters is all essentially the same
    // So we can just use this generic function to remove redundancy
    private <E extends Enum<E> & Named> List<ItemFilterViewBinding> populateFilters(Class<E> enumType) {

        // Get the GridLayout and the appropriate column weight
        final Quartet<Boolean, Function<SortFilterLayoutBinding, ViewBinding>,Integer,Integer> data = filterBlockInfo.get(enumType);
        final boolean rangeNeeded = data.getValue0();
        final String title = getResources().getString(data.getValue2());
        //final int size = (int) dimensionFromID(R.dimen.sort_filter_titles_text_size);
        final int columns = getResources().getInteger(data.getValue3());
        final ViewBinding filterBinding = data.getValue1().apply(binding);
        final FilterBlockRangeLayoutBinding blockRangeBinding = (filterBinding instanceof FilterBlockRangeLayoutBinding) ? (FilterBlockRangeLayoutBinding) filterBinding : null;
        final FilterBlockLayoutBinding blockBinding = (filterBinding instanceof FilterBlockLayoutBinding) ? (FilterBlockLayoutBinding) filterBinding : null;
        final GridLayout gridLayout = rangeNeeded ? blockRangeBinding.filterGrid.filterGridLayout : blockBinding.filterGrid.filterGridLayout;
        final Button selectAllButton = rangeNeeded ? blockRangeBinding.selectAllButton : blockBinding.selectAllButton;
        final Button unselectAllButton = rangeNeeded ? blockRangeBinding.unselectAllButton : blockBinding.unselectAllButton;
        final SortFilterHeaderView headerView = rangeNeeded ? blockRangeBinding.filterHeader : blockBinding.filterHeader;
        final View contentView = rangeNeeded ? blockRangeBinding.filterRangeBlockContent : blockBinding.filterBlockContent;
        headerView.setTitle(title);
        //headerView.setTitleSize(size);
        gridLayout.setColumnCount(columns);

        // Set up expanding header views
        expandingViews.put(headerView, contentView);

        // An empty list of bindings. We'll populate this and return it
        final ArrayList<ItemFilterViewBinding> bindings = new ArrayList<>();

        // Get an array of instances of the Enum type
        final E[] enums = enumType.getEnumConstants();

        // If this isn't an enum type, return our (currently empty) list
        // This should never happens
        if (enums == null) { return bindings; }

        // The default thing to do for one of the filter buttons
        final Consumer<ToggleButton> defaultConsumer = (v) -> {
            spellbookViewModel.toggleVisibility((E) v.getTag());
            spellbookViewModel.setFilterNeeded(true);
        };

        // Map for the buttons
        final Map<Named,ToggleButton> buttons = new HashMap<>();
        filterButtonMaps.put(enumType, buttons);

        // Populate the list of bindings, one for each instance of the given Enum type
        for (E e : enums) {

            // Create the layout parameters
            //final GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f),  GridLayout.spec(GridLayout.UNDEFINED, 1f));

            // Inflate the binding
            final ItemFilterViewBinding itemBinding = ItemFilterViewBinding.inflate(getLayoutInflater());

            // Get the root view
            final View view = itemBinding.getRoot();

            // Set up the toggle button
            final ToggleButton button = itemBinding.itemFilterButton;
            buttons.put(e, button);
            button.setTag(e);
            final Consumer<ToggleButton> toggleButtonConsumer;
            spellbookViewModel.getVisibility(e).observe(getViewLifecycleOwner(), button::set);

            // On a long press, turn off all other buttons in this grid, and turn this one on
            final Consumer<ToggleButton> longPressConsumer = (v) -> {
                if (!v.isSet()) { v.callOnClick(); }
                final E item = (E) v.getTag();
                final Class<? extends Named> type = (Class<? extends Named>) item.getClass();
                final Map<Named,ToggleButton> gridButtons = filterButtonMaps.get(type);
                if (gridButtons == null) { return; }
                SpellbookUtils.clickButtons(gridButtons.values(), (tb) -> (tb != v && tb.isSet()) );
            };
            button.setOnLongClickListener((v) -> { longPressConsumer.accept((ToggleButton) v); return true; });

            // Set up the select all button
            selectAllButton.setTag(enumType);
            selectAllButton.setOnClickListener((v) -> {
                final Class<? extends Named> type = (Class<? extends Named>) selectAllButton.getTag();
                final Map<Named,ToggleButton> gridButtons = filterButtonMaps.get(type);
                if (gridButtons == null) { return; }
                SpellbookUtils.clickButtons(gridButtons.values(), (tb) -> !tb.isSet());
            });

            // Set up the unselect all button
            unselectAllButton.setTag(enumType);
            unselectAllButton.setOnClickListener((v) -> {
                final Class<? extends Named> type = (Class<? extends Named>) unselectAllButton.getTag();
                final Map<Named,ToggleButton> gridButtons = filterButtonMaps.get(type);
                if (gridButtons == null) { return; }
                SpellbookUtils.clickButtons(gridButtons.values(), ToggleButton::isSet);
            });

            // If this is a spanning type, we want to also set up the range view, set the button to toggle the corresponding range view's visibility,
            // as well as do some other stuff
            final boolean spanning = ( rangeNeeded && (e instanceof QuantityType) && ( ((QuantityType) e).isSpanningType()) );
            if (spanning) {

                // Get the range view
                final RangeFilterLayoutBinding rangeBinding = blockRangeBinding.rangeFilter;

                // Add the range view to map of range views
                classToRangeMap.put( (Class<? extends QuantityType>) enumType, rangeBinding);

                // Set up the range view
                setupRangeView(rangeBinding, (QuantityType) e);

                toggleButtonConsumer = (v) -> {
                    defaultConsumer.accept(v);
                    rangeBinding.getRoot().setVisibility(rangeBinding.getRoot().getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                };
            } else {
                toggleButtonConsumer = defaultConsumer;
            }

            button.setOnClickListener(v -> toggleButtonConsumer.accept((ToggleButton) v));
            gridLayout.addView(view);
            bindings.add(itemBinding);
        }
        return bindings;
    }

    private <E extends QuantityType> void setupRangeView(RangeFilterLayoutBinding rangeBinding, E e) {

        // Get the range filter info
        final Class<? extends QuantityType> quantityType = e.getClass();
        final Triplet<Class<? extends Unit>,Integer,Integer> info = rangeViewInfo.get(quantityType);
        final Class<? extends Unit> unitType = info.getValue0();
        rangeBinding.getRoot().setTag(quantityType);
        final String rangeText = getResources().getString(info.getValue1());
        final int maxLength = getResources().getInteger(info.getValue2());

        // Set the range text
        final TextView rangeTV = rangeBinding.rangeTextView;
        rangeTV.setText(rangeText);

        // Get the unit plural names
        final Unit[] units = unitType.getEnumConstants();
        final String[] unitPluralNames = new String[units.length];
        for (int i = 0; i < units.length; ++i) {
            unitPluralNames[i] = units[i].pluralName();
        }

        // Set up the min spinner
        final int textSize = 14;
        final Spinner minUnitSpinner = rangeBinding.rangeMinSpinner;
        final UnitTypeSpinnerAdapter minUnitAdapter = new UnitTypeSpinnerAdapter(getContext(), unitType, textSize);
        minUnitSpinner.setAdapter(minUnitAdapter);

        // Set up the max spinner
        final Spinner maxUnitSpinner = rangeBinding.rangeMaxSpinner;
        final UnitTypeSpinnerAdapter maxUnitAdapter = new UnitTypeSpinnerAdapter(getContext(), unitType, textSize);
        maxUnitSpinner.setAdapter(maxUnitAdapter);

        // Set what happens when the spinners are changed
        final UnitSpinnerListener minUnitListener = new UnitSpinnerListener(unitType, quantityType, SpellbookViewModel::setMinUnit);
        final UnitSpinnerListener maxUnitListener = new UnitSpinnerListener(unitType, quantityType, this.spellbookViewModel::setMaxUnit);

        minUnitSpinner.setOnItemSelectedListener(minUnitListener);
        maxUnitSpinner.setOnItemSelectedListener(maxUnitListener);

        // Set up the min and max text views
        final EditText minET = rangeBinding.rangeMinEntry;
        minET.setTag(quantityType);
        minET.setFilters( new InputFilter[] { new InputFilter.LengthFilter(maxLength) } );
        minET.setOnFocusChangeListener( (v, hasFocus) -> {
            if (!hasFocus) {
                final Class<? extends QuantityType> type = (Class<? extends QuantityType>) minET.getTag();
                final int min = SpellbookUtils.parseFromString(minET.getText().toString(), SpellbookViewModel.getDefaultMinValue(type));
                spellbookViewModel.setMinValue(quantityType, min);
                spellbookViewModel.setFilterNeeded(true);
            }
        });
        final EditText maxET = rangeBinding.rangeMaxEntry;
        maxET.setTag(quantityType);
        maxET.setFilters( new InputFilter[] { new InputFilter.LengthFilter(maxLength) } );
        maxET.setOnFocusChangeListener( (v, hasFocus) -> {
            if (!hasFocus) {
                final Class<? extends QuantityType> type = (Class<? extends QuantityType>) maxET.getTag();
                final int max = SpellbookUtils.parseFromString(maxET.getText().toString(), SpellbookViewModel.getDefaultMaxValue(type));
                spellbookViewModel.setMaxValue(quantityType, max);
                spellbookViewModel.setFilterNeeded(true);
            }
        });

        // Set up the restore defaults button
        final Button restoreDefaultsButton = rangeBinding.restoreDefaultsButton;
        restoreDefaultsButton.setTag(quantityType);
        restoreDefaultsButton.setOnClickListener((v) -> {
            final Class<? extends QuantityType> type = (Class<? extends QuantityType>) v.getTag();
            spellbookViewModel.setRangeToDefaults(type);
            spellbookViewModel.setFilterNeeded(true);
        });

        // Set the listeners appropriately
        spellbookViewModel.getMinUnit(quantityType).observe(getViewLifecycleOwner(), (newUnit) -> SpellbookUtils.setUnitSpinnerByItem(minUnitSpinner, (Enum) newUnit));
        spellbookViewModel.getMaxUnit(quantityType).observe(getViewLifecycleOwner(), (newUnit) -> SpellbookUtils.setUnitSpinnerByItem(maxUnitSpinner, (Enum) newUnit));
        spellbookViewModel.getMinValue(quantityType).observe(getViewLifecycleOwner(), (newValue) -> AndroidUtils.setNumberText(minET, newValue));
        spellbookViewModel.getMaxValue(quantityType).observe(getViewLifecycleOwner(), (newValue) -> AndroidUtils.setNumberText(maxET, newValue));
        spellbookViewModel.getSpanningTypeVisible(quantityType).observe(getViewLifecycleOwner(), (newVis) -> rangeBinding.getRoot().setVisibility(newVis ? View.VISIBLE : View.GONE));

    }

//    // This function updates the character profile for all of the bindings at once
//    private void updateSortFilterBindings() {
//        for (List<ItemFilterViewBinding> bindings : classToBindingsMap.values()) {
//            for (ItemFilterViewBinding binding : bindings) {
//                binding.setProfile(characterProfile);
//                binding.executePendingBindings();
//            }
//        }
//        for (YesNoFilterViewBinding binding : yesNoBindings) {
//            binding.setProfile(characterProfile);
//            binding.executePendingBindings();
//        }
//        binding.levelFilterRange.setProfile(characterProfile);
//        binding.levelFilterRange.executePendingBindings();
//    }

//    private void updateRangeView(Class<? extends QuantityType> quantityType, RangeFilterLayoutBinding rangeBinding) {
//
//        // Get the appropriate data
//        final Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> data = characterProfile.getQuantityRangeInfo(quantityType);
//
//        // Set the min and max text
//        final EditText minET = rangeBinding.rangeMinEntry;
//        minET.setText(String.format(Locale.US, "%d", data.getValue4()));
//        final EditText maxET = rangeBinding.rangeMaxEntry;
//        maxET.setText(String.format(Locale.US, "%d", data.getValue5()));
//
//        // Set the min and max units
//        final Spinner minUnitSpinner = rangeBinding.rangeMinSpinner;
//        final Spinner maxUnitSpinner = rangeBinding.rangeMaxSpinner;
//        final UnitTypeSpinnerAdapter unitAdapter = (UnitTypeSpinnerAdapter) minUnitSpinner.getAdapter();
//        final List units = Arrays.asList(unitAdapter.getData());
//        final Unit minUnit = data.getValue2();
//        minUnitSpinner.setSelection(units.indexOf(minUnit), false);
//        final Unit maxUnit = data.getValue3();
//        maxUnitSpinner.setSelection(units.indexOf(maxUnit), false);
//
//    }



    private void setupYesNoBinding(YesNoFilterViewBinding binding, int titleResourceID, BiFunction<CharacterProfile,Boolean,Boolean> getter, BiConsumer<CharacterProfile,Boolean> toggler) {
        binding.setProfile(characterProfile);
        binding.setTitle(getResources().getString(titleResourceID));
        binding.setStatusGetter(getter);
        binding.executePendingBindings();
        final ToggleButton yesButton = binding.yesOption.optionFilterButton;
        yesButton.setOnClickListener( (v) -> { toggler.accept(characterProfile, true); saveCharacterProfile(); filterOnTablet.run(); });
        final ToggleButton noButton = binding.noOption.optionFilterButton;
        noButton.setOnClickListener( (v) -> { toggler.accept(characterProfile, false); saveCharacterProfile(); filterOnTablet.run(); });
        yesNoBindings.add(binding);
    }

    private void setupRitualConcentrationFilters() {

        // Get the binding
        final RitualConcentrationLayoutBinding ritualConcentrationBinding = binding.ritualConcentrationFilterBlock;

        // Set the title size
        final SortFilterHeaderView headerView = ritualConcentrationBinding.ritualConcentrationFilterHeader;
        final int textSize = spellbookViewModel.areOnTablet()? 35 : 28;
        headerView.setTitleSize(textSize);

        // Set up the bindings
        setupYesNoBinding(ritualConcentrationBinding.ritualFilter, R.string.ritual_filter_title, CharacterProfile::getRitualFilter, CharacterProfile::toggleRitualFilter);
        setupYesNoBinding(ritualConcentrationBinding.concentrationFilter, R.string.concentration_filter_title, CharacterProfile::getConcentrationFilter, CharacterProfile::toggleConcentrationFilter);

        // Expandability
        expandingViews.put(headerView, ritualConcentrationBinding.ritualConcentrationFlexbox);

    }

    private void setupComponentsFilters() {

        // Get the components view binding
        final ComponentsFilterLayoutBinding componentsBinding = binding.componentsFilterBlock;

        // Set up the bindings
        final List<YesNoFilterViewBinding> bindings = Arrays.asList(componentsBinding.verbalFilter, componentsBinding.somaticFilter, componentsBinding.materialFilter);
        final int[] titleIDs = new int[]{ R.string.verbal_filter_title, R.string.somatic_filter_title, R.string.material_filter_title };
        final List<BiConsumer<CharacterProfile,Boolean>> togglers = Arrays.asList(CharacterProfile::toggleVerbalFilter, CharacterProfile::toggleSomaticFilter, CharacterProfile::toggleMaterialComponentFilter);
        final List<BiFunction<CharacterProfile,Boolean,Boolean>> getters = Arrays.asList(CharacterProfile::getVerbalFilter, CharacterProfile::getSomaticFilter, CharacterProfile::getMaterialComponentFilter);
        for (int i = 0; i < titleIDs.length; ++i) {
            setupYesNoBinding(bindings.get(i), titleIDs[i], getters.get(i), togglers.get(i));
        }

        // Expandability
        expandingViews.put(componentsBinding.componentsFilterHeader, componentsBinding.componentsFlexbox);

    }

    private void setup() {

        // Set up the sorting UI elements
        setUpSortElements();

        // Set up the level filter elements
        setupLevelFilter();

        // Populate the ritual and concentration views
        setupRitualConcentrationFilters();

        // Populate the component filters
        setupComponentsFilters();

        // Populate the filter bindings
        classToBindingsMap.put(Sourcebook.class, populateFilters(Sourcebook.class));
        classToBindingsMap.put(CasterClass.class, populateFilters(CasterClass.class));
        classToBindingsMap.put(School.class, populateFilters(School.class));
        classToBindingsMap.put(CastingTime.CastingTimeType.class, populateFilters(CastingTime.CastingTimeType.class));
        classToBindingsMap.put(Duration.DurationType.class, populateFilters(Duration.DurationType.class));
        classToBindingsMap.put(Range.RangeType.class, populateFilters(Range.RangeType.class));

        // Set up the expanding views
        setupExpandingViews();

    }

    private void setupExpandingViews() {
        for (HashMap.Entry<View,View> entry : expandingViews.entrySet()) {
            ViewAnimations.setExpandableHeader(entry.getKey(), entry.getValue());
        }
    }

    private void setupLevelFilter() {

        final LevelFilterLayoutBinding levelBinding = binding.levelFilterRange;
        expandingViews.put(levelBinding.levelFilterHeader, levelBinding.levelFilterContent);

        // When a number is selected on the min (max) spinner, set the current character profile's min (max) level
        final EditText minLevelET = levelBinding.minLevelEntry;
        minLevelET.setOnFocusChangeListener( (v, hasFocus) -> {
            if (!hasFocus) {
                final TextView tv = (TextView) v;
                final int level = SpellbookUtils.parseFromString(tv.getText().toString(), Spellbook.MIN_SPELL_LEVEL);
                spellbookViewModel.setMinLevel(level);
                spellbookViewModel.setFilterNeeded(true);
            }
        });


        final EditText maxLevelET = levelBinding.maxLevelEntry;
        maxLevelET.setOnFocusChangeListener( (v, hasFocus) -> {
            if (!hasFocus) {
                final TextView tv = (TextView) v;
                final int level = SpellbookUtils.parseFromString(tv.getText().toString(), Spellbook.MAX_SPELL_LEVEL);
                spellbookViewModel.setMaxLevel(level);
                spellbookViewModel.setFilterNeeded(true);
            }
        });

        spellbookViewModel.getMinLevel().observe(getViewLifecycleOwner(), level -> AndroidUtils.setNumberText(minLevelET, level));
        spellbookViewModel.getMaxLevel().observe(getViewLifecycleOwner(), level -> AndroidUtils.setNumberText(maxLevelET, level));

    }


    class UnitSpinnerListener<Q extends QuantityType, U extends Unit> implements AdapterView.OnItemSelectedListener {

        private final Class<U> unitType;
        private final Class<Q> quantityType;
        private TriConsumer<SpellbookViewModel, Class<? extends QuantityType>, Unit> setter;

        UnitSpinnerListener(Class<U> unitType, Class<Q> quantityType, TriConsumer<SpellbookViewModel, Class<? extends QuantityType>, Unit> setter) {
            this.unitType = unitType;
            this.quantityType = quantityType;
            this.setter = setter;
        }

        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            // Null checks
            if (adapterView == null || adapterView.getAdapter() == null) { return; }

            // Set the appropriate unit in the character profile
            final U unit = unitType.cast(adapterView.getItemAtPosition(i));
            setter.accept(spellbookViewModel, quantityType, unit);
            spellbookViewModel.setFilterNeeded(true);

        }

        public void onNothingSelected(AdapterView<?> adapterView) {}
    }

}
