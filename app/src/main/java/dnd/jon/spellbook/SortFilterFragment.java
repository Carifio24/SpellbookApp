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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import org.javatuples.Triplet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

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

//    private static final HashMap<Class<? extends Named>,  Quartet<Boolean, Function<SortFilterLayoutBinding, ViewBinding>, Integer, Integer>> filterBlockInfo = new HashMap<Class<? extends Named>, Quartet<Boolean, Function<SortFilterLayoutBinding, ViewBinding>, Integer, Integer>>() {{
//        put(Sourcebook.class, new Quartet<>(false, (b) -> (ViewBinding) b.sourcebookFilterBlock, R.string.sourcebook_filter_title, R.integer.sourcebook_filter_columns));
//        put(CasterClass.class, new Quartet<>(false, (b) -> (ViewBinding) b.casterFilterBlock, R.string.caster_filter_title, R.integer.caster_filter_columns));
//        put(School.class, new Quartet<>(false, (b) -> (ViewBinding) b.schoolFilterBlock, R.string.school_filter_title, R.integer.school_filter_columns));
//        put(CastingTime.CastingTimeType.class, new Quartet<>(true, (b) -> (ViewBinding) b.castingTimeFilterRange, R.string.casting_time_type_filter_title, R.integer.casting_time_type_filter_columns));
//        put(Duration.DurationType.class, new Quartet<>(true, (b) -> (ViewBinding) b.durationFilterRange, R.string.duration_type_filter_title, R.integer.duration_type_filter_columns));
//        put(Range.RangeType.class, new Quartet<>(true, (b) -> (ViewBinding) b.rangeFilterRange, R.string.range_type_filter_title, R.integer.range_type_filter_columns));
//    }};

    // The Triples consist of
    // Superclass, min text, max text, max entry length
    private static final HashMap<Class<? extends QuantityType>, Triplet<Class<? extends Unit>, Integer, Integer>> rangeViewInfo = new HashMap<Class<? extends QuantityType>, Triplet<Class<? extends Unit>, Integer, Integer>>()  {{
        put(CastingTime.CastingTimeType.class, new Triplet<>(TimeUnit.class, R.string.casting_time_range_text, R.integer.casting_time_max_length));
        put(Duration.DurationType.class, new Triplet<>(TimeUnit.class, R.string.duration_range_text, R.integer.duration_max_length));
        put(Range.RangeType.class, new Triplet<>(LengthUnit.class, R.string.range_range_text, R.integer.range_max_length));
    }};

    //private static final Collection<Class<? extends Named>> filterTypes = filterBlockInfo.keySet();

    // Header/expanding views
    private final Map<View,View> expandingViews = new HashMap<>();

    private SortFilterLayoutBinding binding;
    private SpellbookViewModel spellbookViewModel;

    private final Map<Class<? extends Named>, Map<Named,ToggleButton>> filterButtonMaps = new HashMap<>();
    //private final Map<Class<? extends QuantityType>, RangeFilterLayoutBinding> classToRangeMap = new HashMap<>();

    //private final Map<Class<? extends Named>, List<ItemFilterViewBinding>> classToBindingsMap = new HashMap<>();
    //private final List<YesNoFilterViewBinding> yesNoBindings = new ArrayList<>();

    private LifecycleOwner lifecycleOwner;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SortFilterLayoutBinding.inflate(inflater);
        spellbookViewModel = new ViewModelProvider(requireActivity(),new SpellbookViewModelFactory(requireActivity().getApplication())).get(SpellbookViewModel.class);

        lifecycleOwner = getViewLifecycleOwner();

        // Set up the UI elements
        setup();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setup() {

        // Set up the sorting UI elements
        setUpSortUI();

        // Set up the level filter
        setUpLevelFilter();

        // Populate the item filters
        final Context context = requireContext();
        populateFilters(Source.class, (Source[]) Source.values().toArray(new Source[]{}), binding.sourcebookFilterBlock, false, AndroidUtils.stringFromID(context, R.string.sourcebook_filter_title), AndroidUtils.integerFromID(context, R.integer.sourcebook_filter_columns));
        populateFilters(School.class, binding.schoolFilterBlock, false, AndroidUtils.stringFromID(context, R.string.school_filter_title), AndroidUtils.integerFromID(context, R.integer.school_filter_columns));
        populateFilters(CasterClass.class, binding.casterFilterBlock, false, AndroidUtils.stringFromID(context, R.string.caster_filter_title), AndroidUtils.integerFromID(context, R.integer.caster_filter_columns));
        populateFilters(CastingTime.CastingTimeType.class, binding.castingTimeFilterRange, true, AndroidUtils.stringFromID(context, R.string.casting_time_type_filter_title), AndroidUtils.integerFromID(context, R.integer.casting_time_type_filter_columns));
        populateFilters(Duration.DurationType.class, binding.durationFilterRange, true, AndroidUtils.stringFromID(context, R.string.duration_type_filter_title), AndroidUtils.integerFromID(context, R.integer.duration_type_filter_columns));
        populateFilters(Range.RangeType.class, binding.rangeFilterRange, true, AndroidUtils.stringFromID(context, R.string.range_type_filter_title), AndroidUtils.integerFromID(context, R.integer.range_type_filter_columns));

        // Set up the ritual and concentration filters
        setUpRitualAndConcentrationFilters();

        // Set up the component filters
        setUpComponentFilters();

        // Set up the expandable views
        setUpExpandingViews();

    }

    private void setUpSortUI() {

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
        final DisplayNameSpinnerAdapter<SortField> sortAdapter1 = new NamedEnumSpinnerAdapter<>(context, SortField.class, sortTextSize);
        final DisplayNameSpinnerAdapter<SortField> sortAdapter2 = new NamedEnumSpinnerAdapter<>(context, SortField.class, sortTextSize);
        sort1.setAdapter(sortAdapter1);
        sort2.setAdapter(sortAdapter2);


        // Set what happens when the sort spinners are changed
        final AdapterView.OnItemSelectedListener sortListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                final int level = (int) adapterView.getTag();
                final SortField sf = (SortField) adapterView.getItemAtPosition(position);;
                spellbookViewModel.setSortField(sf, level);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        sort1.setOnItemSelectedListener(sortListener);
        sort2.setOnItemSelectedListener(sortListener);

        // Set what happens when the arrow buttons are pressed
//        final ToggleButton.OnClickListener arrowListener = (view) -> {
//            final ToggleButton button = (ToggleButton) view;
//            final boolean up = button.isSet();
//            final int level = (int) view.getTag();
//            spellbookViewModel.setSortReverse(up, level);
//            spellbookViewModel.setSortNeeded(true);
//        };
        sortArrow1.setOnClickListener((v) -> spellbookViewModel.setFirstSortReverse( ((ToggleButton)v).isSet() ));
        sortArrow2.setOnClickListener((v) -> spellbookViewModel.setSecondSortReverse( ((ToggleButton)v).isSet() ));

        // Set the LiveData observers
        spellbookViewModel.getFirstSortField().observe(lifecycleOwner, (sf) -> AndroidUtils.setSpinnerByItem(sort1, sf));
        spellbookViewModel.getSecondSortField().observe(lifecycleOwner, (sf) -> AndroidUtils.setSpinnerByItem(sort2, sf));
        spellbookViewModel.getFirstSortReverse().observe(lifecycleOwner, sortArrow1::set);
        spellbookViewModel.getSecondSortReverse().observe(lifecycleOwner, sortArrow2::set);

    }

    // The code for populating the filters is all essentially the same
    // So we can just use this generic function to remove redundancy
    // For the enum classes, we overload this function below with items = Class<T>.getEnumConstants()
    private <T extends Named> void populateFilters(Class<T> type, T[] items, ViewBinding filterBinding, boolean rangeNeeded, String title, int columns) {

        // Get the necessary bindings
        final FilterBlockRangeLayoutBinding blockRangeBinding = (filterBinding instanceof FilterBlockRangeLayoutBinding) ? (FilterBlockRangeLayoutBinding) filterBinding : null;
        final FilterBlockLayoutBinding blockBinding = (filterBinding instanceof FilterBlockLayoutBinding) ? (FilterBlockLayoutBinding) filterBinding : null;
        final GridLayout gridLayout = rangeNeeded ? blockRangeBinding.filterGrid.filterGridLayout : blockBinding.filterGrid.filterGridLayout;
        final Button selectAllButton = rangeNeeded ? blockRangeBinding.selectAllButton : blockBinding.selectAllButton;
        final Button unselectAllButton = rangeNeeded ? blockRangeBinding.unselectAllButton : blockBinding.unselectAllButton;
        final SortFilterHeaderView headerView = rangeNeeded ? blockRangeBinding.filterHeader : blockBinding.filterHeader;
        final View contentView = rangeNeeded ? blockRangeBinding.filterRangeBlockContent : blockBinding.filterBlockContent;

        // Set the title
        headerView.setTitle(title);

        // Set the column count
        gridLayout.setColumnCount(columns);

        // Set up expanding header views
        expandingViews.put(headerView, contentView);

        // An empty list of bindings. We'll populate this and return it

        // The default thing to do for one of the filter buttons
        final Consumer<ToggleButton> toggleConsumer = (v) -> {
            spellbookViewModel.toggleVisibility((T) v.getTag());
        };

        // A map for the buttons
        final Map<Named, ToggleButton> buttons = new HashMap<>();
        filterButtonMaps.put(type, buttons);

        // Populate the list of buttons, one for each item
        final LayoutInflater inflater = getLayoutInflater();
        final LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        for (T t : items) {

            // Inflate the item filter binding
            final ItemFilterViewBinding itemBinding = ItemFilterViewBinding.inflate(inflater);

            // Get the binding's root view
            final View view = itemBinding.getRoot();

            // Set up the toggle button
            final ToggleButton button = itemBinding.itemFilterButton;
            buttons.put(t, button);
            button.setTag(t);
            final Consumer<ToggleButton> toggleButtonConsumer;
            spellbookViewModel.getVisibility(t).observe(lifecycleOwner, button::set);

            // Set the name
            itemBinding.itemFilterLabel.setText(t.getDisplayName());

            // On a long press, turn off all other buttons in this grid, and turn this one on
            final Consumer<ToggleButton> longPressConsumer = (v) -> {
                if (!v.isSet()) {
                    v.callOnClick();
                }
                final T item = (T) v.getTag();
                final Class<? extends Named> itemType = item.getClass();
                final Map<Named, ToggleButton> gridButtons = filterButtonMaps.get(itemType);
                if (gridButtons == null) {
                    return;
                }
                SpellbookUtils.clickButtons(gridButtons.values(), (tb) -> (tb != v && tb.isSet()));
            };
            button.setOnLongClickListener((v) -> {
                longPressConsumer.accept((ToggleButton) v);
                return true;
            });

            // Set up the select all button
            selectAllButton.setTag(type);
            selectAllButton.setOnClickListener((v) -> {
                final Class<? extends Named> itemType = (Class<? extends Named>) selectAllButton.getTag();
                final Map<Named, ToggleButton> gridButtons = filterButtonMaps.get(itemType);
                if (gridButtons == null) {
                    return;
                }
                SpellbookUtils.clickButtons(gridButtons.values(), (tb) -> !tb.isSet());
            });

            // Set up the unselect all button
            unselectAllButton.setTag(type);
            unselectAllButton.setOnClickListener((v) -> {
                final Class<? extends Named> itemType = (Class<? extends Named>) unselectAllButton.getTag();
                final Map<Named, ToggleButton> gridButtons = filterButtonMaps.get(itemType);
                if (gridButtons == null) {
                    return;
                }
                SpellbookUtils.clickButtons(gridButtons.values(), ToggleButton::isSet);
            });

            // If this is a spanning type, we want to also set up the range view, set the button to toggle the corresponding range view's visibility,
            // as well as do some other stuff
            final boolean spanning = (rangeNeeded && (t instanceof QuantityType) && (((QuantityType) t).isSpanningType()));
            if (spanning) {

                final Class<? extends QuantityType> quantityType = (Class<? extends QuantityType>) type;

                // Get the range view
                final RangeFilterLayoutBinding rangeBinding = blockRangeBinding.rangeFilter;

                // Add the range view to map of range views
                //classToRangeMap.put(quantityType, rangeBinding);

                // Set up the range view
                setUpRangeView(rangeBinding, (QuantityType) t);

                // Modify the range view's visibility appropriately
                spellbookViewModel.getSpanningTypeVisible(quantityType).observe(lifecycleOwner, (vis) -> rangeBinding.getRoot().setVisibility(vis ? View.VISIBLE : View.GONE));
            }

            button.setOnClickListener(v -> toggleConsumer.accept((ToggleButton) v));
            gridLayout.addView(view);
        }
    }

    private <E extends Enum<E> & Named> void populateFilters(Class<E> enumType, ViewBinding filterBinding, boolean rangeNeeded, String title, int columns) {
        populateFilters(enumType, enumType.getEnumConstants(), filterBinding, rangeNeeded, title, columns);
    }

    private <E extends QuantityType> void setUpRangeView(RangeFilterLayoutBinding rangeBinding, E e) {

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
        final TriConsumer<SpellbookViewModel, Class<? extends QuantityType>, Unit> minSetter = SpellbookViewModel::setMinUnit;
        final TriConsumer<SpellbookViewModel, Class<? extends QuantityType>, Unit> maxSetter = SpellbookViewModel::setMaxUnit;
        final UnitSpinnerListener minUnitListener = new UnitSpinnerListener(unitType, quantityType, minSetter);
        final UnitSpinnerListener maxUnitListener = new UnitSpinnerListener(unitType, quantityType, maxSetter);

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
            }
        });

        // Set up the restore defaults button
        final Button restoreDefaultsButton = rangeBinding.restoreDefaultsButton;
        restoreDefaultsButton.setTag(quantityType);
        restoreDefaultsButton.setOnClickListener((v) -> {
            final Class<? extends QuantityType> type = (Class<? extends QuantityType>) v.getTag();
            spellbookViewModel.setRangeToDefaults(type);
        });

        // Set the listeners appropriately
        spellbookViewModel.getMinUnit(quantityType).observe(lifecycleOwner, (newUnit) -> AndroidUtils.setSpinnerByItem(minUnitSpinner, newUnit));
        spellbookViewModel.getMaxUnit(quantityType).observe(lifecycleOwner, (newUnit) -> AndroidUtils.setSpinnerByItem(maxUnitSpinner, newUnit));
        spellbookViewModel.getMinValue(quantityType).observe(lifecycleOwner, (newValue) -> AndroidUtils.setNumberText(minET, newValue));
        spellbookViewModel.getMaxValue(quantityType).observe(lifecycleOwner, (newValue) -> AndroidUtils.setNumberText(maxET, newValue));
        spellbookViewModel.getSpanningTypeVisible(quantityType).observe(lifecycleOwner, (newVis) -> rangeBinding.getRoot().setVisibility(newVis ? View.VISIBLE : View.GONE));

    }

    private void setUpYNBinding(YesNoFilterViewBinding ynBinding, int titleResourceID, BiFunction<SpellbookViewModel, Boolean, LiveData<Boolean>> ynGetter, TriConsumer<SpellbookViewModel, Boolean, Boolean> ynSetter) {
        // Set the title
        ynBinding.filterTitle.setText(titleResourceID);
        ynBinding.yesOption.optionFilterLabel.setText(R.string.yes);
        ynBinding.noOption.optionFilterLabel.setText(R.string.no);

        // Get the yes and no buttons
        final ToggleButton yesButton = ynBinding.yesOption.optionFilterButton;
        final ToggleButton noButton = ynBinding.noOption.optionFilterButton;

        // Add the onClick listeners
        yesButton.setOnClickListener((v) -> ynSetter.accept(spellbookViewModel, true, yesButton.isSet()));
        noButton.setOnClickListener((v) -> ynSetter.accept(spellbookViewModel, false, noButton.isSet()));

       // Add the appropriate listeners
        ynGetter.apply(spellbookViewModel, true).observe(lifecycleOwner, yesButton::set);
        ynGetter.apply(spellbookViewModel, false).observe(lifecycleOwner, noButton::set);

    }

    private void setUpRitualAndConcentrationFilters() {

        // Get the binding
        final RitualConcentrationLayoutBinding ritualConcentrationBinding = binding.ritualConcentrationFilterBlock;

        // Set the title size
        final SortFilterHeaderView headerView = ritualConcentrationBinding.ritualConcentrationFilterHeader;
        final int textSize = spellbookViewModel.areOnTablet()? 35 : 28;
        headerView.setTitleSize(textSize);

        // Set up the bindings
        setUpYNBinding(ritualConcentrationBinding.ritualFilter, R.string.ritual_filter_title, SpellbookViewModel::getRitualFilter, SpellbookViewModel::setRitualFilter);
        setUpYNBinding(ritualConcentrationBinding.concentrationFilter, R.string.concentration_filter_title, SpellbookViewModel::getConcentrationFilter, SpellbookViewModel::setConcentrationFilter);
    }

    private void setUpComponentFilters() {

        // Get the binding
        final ComponentsFilterLayoutBinding componentsBinding = binding.componentsFilterBlock;

        // Set up the bindings for the individual component types
        final List<YesNoFilterViewBinding> bindings = Arrays.asList(componentsBinding.verbalFilter, componentsBinding.somaticFilter, componentsBinding.materialFilter);
        final int[] titleIDs = new int[]{ R.string.verbal_filter_title, R.string.somatic_filter_title, R.string.material_filter_title };
        final List<BiFunction<SpellbookViewModel, Boolean, LiveData<Boolean>>> getters = Arrays.asList(SpellbookViewModel::getVerbalFilter, SpellbookViewModel::getSomaticFilter, SpellbookViewModel::getMaterialFilter);
        final List<TriConsumer<SpellbookViewModel, Boolean, Boolean>> setters = Arrays.asList(SpellbookViewModel::setVerbalFilter, SpellbookViewModel::setSomaticFilter, SpellbookViewModel::setMaterialFilter);
        for (int i = 0; i < titleIDs.length; ++i) {
            setUpYNBinding(bindings.get(i), titleIDs[i], getters.get(i), setters.get(i));
        }

        // Set up expanding header view
        expandingViews.put(componentsBinding.componentsFilterHeader, componentsBinding.componentsFlexbox);
    }

    private void setUpLevelFilter() {

        // Get the binding
        final LevelFilterLayoutBinding levelBinding = binding.levelFilterRange;

        // Set up expanding views
        expandingViews.put(levelBinding.levelFilterHeader, levelBinding.levelFilterContent);

        // When a number is selected on the min (max) spinner, set the current character profile's min (max) level
        final EditText minLevelET = levelBinding.minLevelEntry;
        minLevelET.setOnFocusChangeListener( (v, hasFocus) -> {
            if (!hasFocus) {
                final TextView tv = (TextView) v;
                final int level = SpellbookUtils.parseFromString(tv.getText().toString(), Spellbook.MIN_SPELL_LEVEL);
                spellbookViewModel.setMinLevel(level);
            }
        });


        final EditText maxLevelET = levelBinding.maxLevelEntry;
        maxLevelET.setOnFocusChangeListener( (v, hasFocus) -> {
            if (!hasFocus) {
                final TextView tv = (TextView) v;
                final int level = SpellbookUtils.parseFromString(tv.getText().toString(), Spellbook.MAX_SPELL_LEVEL);
                spellbookViewModel.setMaxLevel(level);
            }
        });

        // Set up the necessary observers
        spellbookViewModel.getMinLevel().observe(lifecycleOwner, level -> AndroidUtils.setNumberText(minLevelET, level));
        spellbookViewModel.getMaxLevel().observe(lifecycleOwner, level -> AndroidUtils.setNumberText(maxLevelET, level));

    }

    private void setUpExpandingViews() {
        for (HashMap.Entry<View,View> entry : expandingViews.entrySet()) {
            ViewAnimations.setExpandableHeader(entry.getKey(), entry.getValue());
        }
    }


    class UnitSpinnerListener<Q extends QuantityType, U extends Unit> implements AdapterView.OnItemSelectedListener {

        private final Class<U> unitType;
        private final Class<Q> quantityType;
        private TriConsumer<SpellbookViewModel, Class<Q>, Unit> setter;

        UnitSpinnerListener(Class<U> unitType, Class<Q> quantityType, TriConsumer<SpellbookViewModel, Class<Q>, Unit> setter) {
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

        }

        public void onNothingSelected(AdapterView<?> adapterView) {}
    }



}
