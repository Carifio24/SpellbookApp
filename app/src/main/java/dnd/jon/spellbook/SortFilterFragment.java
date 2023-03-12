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
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import org.apache.commons.lang3.ArrayUtils;
import org.javatuples.Quintet;
import org.javatuples.Sextet;
import org.javatuples.Triplet;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import dnd.jon.spellbook.databinding.ComponentsFilterLayoutBinding;
import dnd.jon.spellbook.databinding.FilterBlockFeaturedLayoutBinding;
import dnd.jon.spellbook.databinding.FilterBlockLayoutBinding;
import dnd.jon.spellbook.databinding.FilterBlockRangeLayoutBinding;
import dnd.jon.spellbook.databinding.FilterOptionBinding;
import dnd.jon.spellbook.databinding.ItemFilterViewBinding;
import dnd.jon.spellbook.databinding.LevelFilterLayoutBinding;
import dnd.jon.spellbook.databinding.RangeFilterLayoutBinding;
import dnd.jon.spellbook.databinding.RitualConcentrationLayoutBinding;
import dnd.jon.spellbook.databinding.SortFilterLayoutBinding;
import dnd.jon.spellbook.databinding.SortLayoutBinding;
import dnd.jon.spellbook.databinding.YesNoFilterViewBinding;

public class SortFilterFragment extends SpellbookFragment<SortFilterLayoutBinding> {

    private SortFilterStatus sortFilterStatus;

    // Header/expanding views
    private final HashMap<View,View> expandingViews = new HashMap<>();

    // For filtering stuff
    private final HashMap<Class<? extends NameDisplayable>, List<ItemFilterViewBinding>> classToBindingsMap = new HashMap<>();
    private final List<YesNoFilterViewBinding> yesNoBindings = new ArrayList<>();
    private final HashMap<Class<? extends QuantityType>, RangeFilterLayoutBinding> classToRangeMap = new HashMap<>();
    private final Map<Class<? extends NameDisplayable>, Map<NameDisplayable,ToggleButton>> filterButtonMaps = new HashMap<>();

    private static final HashMap<Class<? extends NameDisplayable>, Sextet<Boolean, Function<SortFilterLayoutBinding, ViewBinding>, Integer, Integer, Integer, Integer>> filterBlockInfo = new HashMap<Class<? extends NameDisplayable>, Sextet<Boolean, Function<SortFilterLayoutBinding, ViewBinding>, Integer, Integer, Integer, Integer>>() {{
        put(Source.class, new Sextet<>(false, (b) -> b.sourcebookFilterBlock, R.string.sourcebook_filter_title, R.integer.sourcebook_filter_columns, R.string.sourcebooks_info_title, R.string.sourcebooks_info_description));
        put(CasterClass.class, new Sextet<>(false, (b) -> b.casterFilterBlock, R.string.caster_filter_title, R.integer.caster_filter_columns, R.string.classes_info_title, R.string.classes_info_description));
        put(School.class, new Sextet<>(false, (b) -> b.schoolFilterBlock, R.string.school_filter_title, R.integer.school_filter_columns, R.string.schools_info_title, R.string.schools_info_description));
        put(CastingTime.CastingTimeType.class, new Sextet<>(true, (b) -> b.castingTimeFilterRange, R.string.casting_time_type_filter_title, R.integer.casting_time_type_filter_columns, R.string.casting_time_info_title, R.string.casting_time_info_description));
        put(Duration.DurationType.class, new Sextet<>(true, (b) -> b.durationFilterRange, R.string.duration_type_filter_title, R.integer.duration_type_filter_columns, R.string.duration_info_title, R.string.duration_info_description));
        put(Range.RangeType.class, new Sextet<>(true, (b) -> b.rangeFilterRange, R.string.range_type_filter_title, R.integer.range_type_filter_columns, R.string.range_info_title, R.string.range_info_description));
    }};

    // The Triples consist of
    // Superclass, min text, max text, max entry length
    private static final HashMap<Class<? extends QuantityType>, Triplet<Class<? extends Unit>, Integer, Integer>> rangeViewInfo = new HashMap<Class<? extends QuantityType>, Triplet<Class<? extends Unit>, Integer, Integer>>()  {{
        put(CastingTime.CastingTimeType.class, new Triplet<>(TimeUnit.class, R.string.casting_time_range_text, R.integer.casting_time_max_length));
        put(Duration.DurationType.class, new Triplet<>(TimeUnit.class, R.string.duration_range_text, R.integer.duration_max_length));
        put(Range.RangeType.class, new Triplet<>(LengthUnit.class, R.string.range_range_text, R.integer.range_max_length));
    }};

    public SortFilterFragment() {
        super(R.layout.sort_filter_layout);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        //super.onCreateView(inflater, container, savedInstanceState);
        binding = SortFilterLayoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.currentSortFilterStatus().observe(getViewLifecycleOwner(), this::updateSortFilterStatus);
        setup();
    }

    private String stringFromID(int stringID) { return getResources().getString(stringID); }

    private String formattedInteger(int value) {
        return String.format(Locale.getDefault(), "%d", value);
    }

    private void setupSortElements() {

        // Get various UI elements
        final SortLayoutBinding sortBinding = binding.sortBlock;
        final Spinner sortSpinner1 = sortBinding.sortField1Spinner;
        final Spinner sortSpinner2 = sortBinding.sortField2Spinner;
        final SortDirectionButton sortArrow1 = sortBinding.sortField1Arrow;
        final SortDirectionButton sortArrow2 = sortBinding.sortField2Arrow;

        // Set the views to be expanded
        expandingViews.put(sortBinding.sortHeader, sortBinding.sortContent);

        // Set tags for the sorting UI elements
        sortSpinner1.setTag(1);
        sortSpinner2.setTag(2);
        sortArrow1.setTag(1);
        sortArrow2.setTag(2);

        // Populate the dropdown spinners
        final int sortTextSize = 18;
        final NamedSpinnerAdapter<SortField> sortAdapter1 = new NamedSpinnerAdapter<>(context, SortField.class, DisplayUtils::getDisplayName, sortTextSize);
        final NamedSpinnerAdapter<SortField> sortAdapter2 = new NamedSpinnerAdapter<>(context, SortField.class, DisplayUtils::getDisplayName, sortTextSize);
        sortSpinner1.setAdapter(sortAdapter1);
        sortSpinner2.setAdapter(sortAdapter2);

        // Set what happens when the sort spinners are changed
        final AdapterView.OnItemSelectedListener sortListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                final SortFilterStatus sortFilterStatus = viewModel.getSortFilterStatus();
                if (sortFilterStatus == null) { return; }
                final int tag = (int) adapterView.getTag();
                final SortField sf = (SortField) adapterView.getItemAtPosition(position);
                sortFilterStatus.setSortField(tag, sf);
                viewModel.saveSortFilterStatus();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        sortSpinner1.setOnItemSelectedListener(sortListener);
        sortSpinner2.setOnItemSelectedListener(sortListener);

        // Set what happens when the arrow buttons are pressed
        final SortDirectionButton.OnClickListener arrowListener = (View view) -> {
            final SortDirectionButton b = (SortDirectionButton) view;
            final SortFilterStatus sortFilterStatus = viewModel.getSortFilterStatus();
            b.onPress();
            final boolean up = b.pointingUp();
            if (sortFilterStatus == null) { return; }
            final int tag = (int) view.getTag();
            sortFilterStatus.setSortReverse(tag, up);
            viewModel.saveSortFilterStatus();
        };
        sortArrow1.setOnClickListener(arrowListener);
        sortArrow2.setOnClickListener(arrowListener);

    }

//    private void setupSortStatusButtons() {
//        binding.loadStatusButton.setOnClickListener((button) -> {
//            final SortFilterStatusSelectionDialog dialog = new SortFilterStatusSelectionDialog();
//            dialog.show(requireActivity().getSupportFragmentManager(), "selectStatus");
//        });
//
//        binding.saveStatusButton.setOnClickListener((button) -> {
//            final SaveSortFilterStatusDialog dialog = new SaveSortFilterStatusDialog();
//            dialog.show(requireActivity().getSupportFragmentManager(), "saveStatus");
//        });
//    }

    private void setupFilterOptions() {

        // Set up the bindings
        final Map<FilterOptionBinding, BiConsumer<SortFilterStatus,Boolean>> bindingsAndFunctions = new HashMap<FilterOptionBinding, BiConsumer<SortFilterStatus,Boolean>>() {{
            put(binding.filterOptions.filterListsLayout, SortFilterStatus::setApplyFiltersToLists);
            put(binding.filterOptions.filterSearchLayout, SortFilterStatus::setApplyFiltersToSearch);
            put(binding.filterOptions.useExpandedLayout, SortFilterStatus::setUseTashasExpandedLists);
        }};

        for (Map.Entry<FilterOptionBinding, BiConsumer<SortFilterStatus,Boolean>> entry : bindingsAndFunctions.entrySet()) {
            final FilterOptionBinding filterOptionBinding = entry.getKey();
            final BiConsumer<SortFilterStatus, Boolean> function = entry.getValue();
            filterOptionBinding.optionChooser.setOnCheckedChangeListener((chooser, isChecked) -> {
                function.accept(sortFilterStatus, isChecked);
            });
            filterOptionBinding.optionInfoButton.setOnClickListener((v) -> {
                openOptionInfoDialog(filterOptionBinding);
            });
        }

        // Expandable header setup
        expandingViews.put(binding.filterOptions.filterOptionsHeader, binding.filterOptions.filterOptionsContent);

    }

    private void setupLevelFilter() {

        final LevelFilterLayoutBinding levelBinding = binding.levelFilterRange;
        expandingViews.put(levelBinding.levelFilterHeader, levelBinding.levelFilterContent);

        // When a number is selected on the min (max) spinner, set the current character profile's min (max) level
        final EditText minLevelET = levelBinding.minLevelEntry;
        minLevelET.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                final TextView tv = (TextView) v;
                int level;
                try {
                    level = Integer.parseInt(tv.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    tv.setText(formattedInteger(Spellbook.MIN_SPELL_LEVEL));
                    return;
                }
                viewModel.getSortFilterStatus().setMinSpellLevel(level);
            }
        });

        final EditText maxLevelET = levelBinding.maxLevelEntry;
        maxLevelET.setOnFocusChangeListener( (v, hasFocus) -> {
            if (!hasFocus) {
                final TextView tv = (TextView) v;
                int level;
                try {
                    level = Integer.parseInt(tv.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    tv.setText(formattedInteger(Spellbook.MAX_SPELL_LEVEL));
                    return;
                }
                viewModel.getSortFilterStatus().setMaxSpellLevel(level);
            }
        });

        // When the restore full range button is pressed, set the min and max levels for the profile to the full min and max
        final Button restoreFullButton = levelBinding.fullRangeButton;
        restoreFullButton.setOnClickListener((v) -> {
            final SortFilterStatus sortFilterStatus = viewModel.getSortFilterStatus();
            binding.levelFilterRange.minLevelEntry.setText(formattedInteger(Spellbook.MIN_SPELL_LEVEL));
            binding.levelFilterRange.maxLevelEntry.setText(formattedInteger(Spellbook.MAX_SPELL_LEVEL));
            sortFilterStatus.setMinSpellLevel(Spellbook.MIN_SPELL_LEVEL);
            sortFilterStatus.setMaxSpellLevel(Spellbook.MAX_SPELL_LEVEL);
        });
    }

    private void setupYesNoBinding(YesNoFilterViewBinding binding, int titleResourceID, BiFunction<SortFilterStatus,Boolean,Boolean> getter, BiConsumer<SortFilterStatus,Boolean> toggler) {
        binding.setStatus(sortFilterStatus);
        binding.setTitle(getResources().getString(titleResourceID));
        binding.setYnGetter(getter);
        binding.executePendingBindings();
        final ToggleButton yesButton = binding.yesOption.optionFilterButton;
        yesButton.setOnClickListener( (v) -> { toggler.accept(sortFilterStatus, true); });
        final ToggleButton noButton = binding.noOption.optionFilterButton;
        noButton.setOnClickListener( (v) -> { toggler.accept(sortFilterStatus, false); });
        yesNoBindings.add(binding);
    }

    private void setupRitualConcentrationFilters() {

        // Get the binding
        final RitualConcentrationLayoutBinding ritualConcentrationBinding = binding.ritualConcentrationFilterBlock;

        // Set the title size
        final SortFilterHeaderView headerView = ritualConcentrationBinding.ritualConcentrationFilterHeader;
        final int textSize = getResources().getDimensionPixelSize(R.dimen.ritual_concentration_title_size);
        headerView.setTitleSize(textSize);

        // Set up the bindings
        setupYesNoBinding(ritualConcentrationBinding.ritualFilter, R.string.ritual_filter_title, SortFilterStatus::getRitualFilter, SortFilterStatus::toggleRitualFilter);
        setupYesNoBinding(ritualConcentrationBinding.concentrationFilter, R.string.concentration_filter_title, SortFilterStatus::getConcentrationFilter, SortFilterStatus::toggleConcentrationFilter);

        // Expandability
        expandingViews.put(headerView, ritualConcentrationBinding.ritualConcentrationFlexbox);

    }

    private void setupComponentsFilters() {

        // Get the components view binding
        final ComponentsFilterLayoutBinding componentsBinding = binding.componentsFilterBlock;

        // Set up the bindings
        final List<YesNoFilterViewBinding> bindings = Arrays.asList(componentsBinding.verbalFilter, componentsBinding.somaticFilter, componentsBinding.materialFilter);
        final int[] titleIDs = new int[]{ R.string.verbal_filter_title, R.string.somatic_filter_title, R.string.material_filter_title };
        final List<BiConsumer<SortFilterStatus,Boolean>> togglers = Arrays.asList(SortFilterStatus::toggleVerbalFilter, SortFilterStatus::toggleSomaticFilter, SortFilterStatus::toggleMaterialFilter);
        final List<BiFunction<SortFilterStatus,Boolean,Boolean>> getters = Arrays.asList(SortFilterStatus::getVerbalFilter, SortFilterStatus::getSomaticFilter, SortFilterStatus::getMaterialFilter);
        for (int i = 0; i < titleIDs.length; ++i) {
            setupYesNoBinding(bindings.get(i), titleIDs[i], getters.get(i), togglers.get(i));
        }

        // Expandability
        expandingViews.put(componentsBinding.componentsFilterHeader, componentsBinding.componentsFlexbox);

    }

    // The code for populating the filters is all essentially the same
    // So we can just use this generic function to remove redundancy
    private <Q extends NameDisplayable> List<ItemFilterViewBinding> populateFilters(Class<Q> type, Q[] items, Q[] featuredItems) {

        // Get the GridLayout and the appropriate column weight
        final Sextet<Boolean, Function<SortFilterLayoutBinding, ViewBinding>,Integer,Integer,Integer,Integer> data = filterBlockInfo.get(type);
        final boolean rangeNeeded = data.getValue0();
        final String title = stringFromID(data.getValue2());
        //final int size = (int) dimensionFromID(R.dimen.sort_filter_titles_text_size);
        final int columns = getResources().getInteger(data.getValue3());
        final ViewBinding filterBinding = data.getValue1().apply(binding);
        final String infoTitle = stringFromID(data.getValue4());
        final String infoDescription = stringFromID(data.getValue5());
//        final FilterBlockRangeLayoutBinding blockRangeBinding = (filterBinding instanceof FilterBlockRangeLayoutBinding) ? (FilterBlockRangeLayoutBinding) filterBinding : null;
//        final FilterBlockLayoutBinding blockBinding = (filterBinding instanceof FilterBlockLayoutBinding) ? (FilterBlockLayoutBinding) filterBinding : null;
//        final GridLayout gridLayout = rangeNeeded ? blockRangeBinding.filterGrid.filterGridLayout : blockBinding.filterGrid.filterGridLayout;
//        final Button selectAllButton = rangeNeeded ? blockRangeBinding.selectAllButton : blockBinding.selectAllButton;
//        final Button unselectAllButton = rangeNeeded ? blockRangeBinding.unselectAllButton : blockBinding.unselectAllButton;
//        final SortFilterHeaderView headerView = rangeNeeded ? blockRangeBinding.filterHeader : blockBinding.filterHeader;
//        final View contentView = rangeNeeded ? blockRangeBinding.filterRangeBlockContent : blockBinding.filterBlockContent;

        final boolean haveFeatured = featuredItems != null;
        final Sextet<GridLayout,SortFilterHeaderView,View,Button,Button,Button> filterViews = getFilterViews(filterBinding);
        final GridLayout gridLayout = filterViews.getValue0();
        final SortFilterHeaderView headerView = filterViews.getValue1();
        final View contentView = filterViews.getValue2();
        final Button selectAllButton = filterViews.getValue3();
        final Button unselectAllButton = filterViews.getValue4();
        final Button showMoreButton = filterViews.getValue5();
        headerView.setTitle(title);
        headerView.setInfoTitle(infoTitle);
        headerView.setInfoDescription(infoDescription);
        //headerView.setTitleSize(size);
        gridLayout.setColumnCount(columns);

        final Collection<View> notFeaturedRows = new ArrayList<>();

        // Set up expanding header views
        expandingViews.put(headerView, contentView);

        // An empty list of bindings. We'll populate this and return it
        final List<ItemFilterViewBinding> bindings = new ArrayList<>();

        // The default thing to do for one of the filter buttons
        final Consumer<ToggleButton> defaultConsumer = (v) -> {
            sortFilterStatus.toggleVisibility((Q) v.getTag());
        };

        // Map for the buttons
        final Map<NameDisplayable,ToggleButton> buttons = new HashMap<>();
        filterButtonMaps.put(type, buttons);

        // Sort the enums by name
        final Locale locale =  getResources().getConfiguration().getLocales().get(0);
        final Collator collator = Collator.getInstance(locale);
        final Comparator<Q> comparator = (e1, e2) -> collator.compare(DisplayUtils.getDisplayName(context, e1), DisplayUtils.getDisplayName(context, e2));
        Arrays.sort(items, comparator);

        // We want to sort the items so that the featured items come before the other items
        final Q[] sortedItems = items.clone();
        if (haveFeatured) {
            final Comparator<Q> featuredPreSorter = (e1, e2) -> {
                final int r1 = ArrayUtils.contains(featuredItems, e1) ? 1 : 0;
                final int r2 = ArrayUtils.contains(featuredItems, e2) ? 1 : 0;
                return r2 - r1;
            };
            Arrays.sort(sortedItems, featuredPreSorter);
        }

        // Populate the list of bindings, one for each instance of the given Enum type
        for (Q q : sortedItems) {

            // Create the layout parameters
            //final GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f),  GridLayout.spec(GridLayout.UNDEFINED, 1f));

            // Inflate the binding
            final ItemFilterViewBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.item_filter_view, null, false);

            // Bind the relevant values
            binding.setStatus(sortFilterStatus);
            binding.setItem(q);
            binding.executePendingBindings();

            // Get the root view
            final View view = binding.getRoot();

            // Set up the toggle button
            final ToggleButton button = binding.itemFilterButton;
            buttons.put(q, button);
            button.setTag(q);
            final Consumer<ToggleButton> toggleButtonConsumer;

            // On a long press, turn off all other buttons in this grid, and turn this one on
            final Consumer<ToggleButton> longPressConsumer = (v) -> {
                if (!v.isSet()) { v.callOnClick(); }
                //final E item = (E) v.getTag();
                final Class<? extends NameDisplayable> t = q.getClass();
                final Map<NameDisplayable,ToggleButton> gridButtons = filterButtonMaps.get(t);
                if (gridButtons == null) { return; }
                SpellbookUtils.clickButtons(gridButtons.values(), (tb) -> (tb != v && tb.isSet()) );
            };
            button.setOnLongClickListener((v) -> { longPressConsumer.accept((ToggleButton) v); return true; });

            // Set up the select all button
            selectAllButton.setTag(type);
            selectAllButton.setOnClickListener((v) -> {
                final Class<? extends NameDisplayable> t = (Class<? extends NameDisplayable>) selectAllButton.getTag();
                final Map<NameDisplayable,ToggleButton> gridButtons = filterButtonMaps.get(t);
                if (gridButtons == null) { return; }
                SpellbookUtils.clickButtons(gridButtons.values(), (tb) -> !tb.isSet());
            });

            // Set up the unselect all button
            unselectAllButton.setTag(type);
            unselectAllButton.setOnClickListener((v) -> {
                final Class<? extends NameDisplayable> t = (Class<? extends NameDisplayable>) unselectAllButton.getTag();
                final Map<NameDisplayable,ToggleButton> gridButtons = filterButtonMaps.get(t);
                if (gridButtons == null) { return; }
                SpellbookUtils.clickButtons(gridButtons.values(), ToggleButton::isSet);
            });

            // If this is a spanning type, we want to also set up the range view, set the button to toggle the corresponding range view's visibility,
            // as well as do some other stuff
            final boolean spanning = ( rangeNeeded && (q instanceof QuantityType) && ( ((QuantityType) q).isSpanningType()) );
            if (spanning) {

                // Get the range view
                final FilterBlockRangeLayoutBinding blockRangeBinding = (FilterBlockRangeLayoutBinding) filterBinding;
                final RangeFilterLayoutBinding rangeBinding = blockRangeBinding.rangeFilter;

                // Add the range view to map of range views
                Class<? extends QuantityType> quantityType = (Class<? extends QuantityType>) type;
                classToRangeMap.put(quantityType, rangeBinding);

                // Set up the range view
                setupRangeView(rangeBinding, quantityType);

                toggleButtonConsumer = (v) -> {
                    defaultConsumer.accept(v);
                    rangeBinding.getRoot().setVisibility(rangeBinding.getRoot().getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                };
            } else {
                toggleButtonConsumer = defaultConsumer;
            }

            button.setOnClickListener(v -> toggleButtonConsumer.accept((ToggleButton) v));
            gridLayout.addView(view);
            bindings.add(binding);

            final boolean isAdditionalItem = haveFeatured && !ArrayUtils.contains(featuredItems, q);
            if (isAdditionalItem) {
                notFeaturedRows.add(view);
                view.setVisibility(View.GONE);
            }

            if (haveFeatured && showMoreButton != null) {
                showMoreButton.setTag(false);
                showMoreButton.setOnClickListener((v) -> {
                    final boolean visible = (boolean) showMoreButton.getTag();
                    for (View nfr : notFeaturedRows) {
                        nfr.setVisibility(visible ? View.GONE : View.VISIBLE);
                    }
                    showMoreButton.setTag(!visible);
                    showMoreButton.setText(visible ? R.string.show_more : R.string.show_less);
                });
            }
        }
        return bindings;
    }

    private <Q extends NameDisplayable> List<ItemFilterViewBinding> populateFilters(Class<Q> enumType, Q[] items) {
        return populateFilters(enumType, items, null);
    }

    private <E extends NameDisplayable> List<ItemFilterViewBinding> populateFilters(Class<E> enumType) {
        // Get an array of instances of the Enum type
        final E[] items = enumType.getEnumConstants();

        // If this isn't an enum type, return our (currently empty) list
        // This should never happens
        if (items == null) { return new ArrayList<>(); }
        return populateFilters(enumType, items, null);
    }

//    private <E extends Enum<E> & NameDisplayable> List<ItemFilterViewBinding> populateFeaturedFilters(Class<E> enumType, E[] items) {
//        // Get an array of instances of the Enum type
//        final E[] allItems = enumType.getEnumConstants();
//
//        // If this isn't an enum type, return our (currently empty) list
//        // This should never happens
//        if (items == null) { return new ArrayList<>(); }
//        return populateFilters(enumType, allItems, items);
//    }
    
    private void populateFilterBindings() {
        classToBindingsMap.put(Source.class, populateFilters(Source.class, LocalizationUtils.supportedSources(), LocalizationUtils.supportedCoreSourcebooks()));
        classToBindingsMap.put(CasterClass.class, populateFilters(CasterClass.class, LocalizationUtils.supportedClasses()));
        classToBindingsMap.put(School.class, populateFilters(School.class));
        classToBindingsMap.put(CastingTime.CastingTimeType.class, populateFilters(CastingTime.CastingTimeType.class));
        classToBindingsMap.put(Duration.DurationType.class, populateFilters(Duration.DurationType.class));
        classToBindingsMap.put(Range.RangeType.class, populateFilters(Range.RangeType.class));
    }

    private Sextet<GridLayout,SortFilterHeaderView,View,Button,Button,Button> getFilterViews(ViewBinding binding) {
        if (binding instanceof FilterBlockLayoutBinding) {
            final FilterBlockLayoutBinding filterBinding = (FilterBlockLayoutBinding) binding;
            return new Sextet<>(filterBinding.filterGrid.filterGridLayout, filterBinding.filterHeader, filterBinding.filterBlockContent, filterBinding.selectAllButton, filterBinding.unselectAllButton, null);
        } else if (binding instanceof FilterBlockRangeLayoutBinding) {
            final FilterBlockRangeLayoutBinding filterBinding = (FilterBlockRangeLayoutBinding) binding;
            return new Sextet<>(filterBinding.filterGrid.filterGridLayout, filterBinding.filterHeader, filterBinding.filterRangeBlockContent, filterBinding.selectAllButton, filterBinding.unselectAllButton, null);
        } else if (binding instanceof FilterBlockFeaturedLayoutBinding) {
            final FilterBlockFeaturedLayoutBinding filterBinding = (FilterBlockFeaturedLayoutBinding) binding;
            return new Sextet<>(filterBinding.filterGrid.filterGridLayout, filterBinding.featuredFilterHeader, filterBinding.featuredBlockContent, filterBinding.featuredSelectAllButton, filterBinding.featuredUnselectAllButton, filterBinding.showMoreButton);
        }
        return new Sextet<>(null, null, null, null, null, null); // We shouldn't get here
    }

    private void setupExpandingViews() {
        for (HashMap.Entry<View,View> entry : expandingViews.entrySet()) {
            ViewAnimations.setExpandableHeader(entry.getKey(), entry.getValue());
        }
    }

    // This function updates the sort/filter status for all of the bindings at once
    private void updateSortFilterBindings() {
        for (List<ItemFilterViewBinding> bindings : classToBindingsMap.values()) {
            for (ItemFilterViewBinding binding : bindings) {
                binding.setStatus(sortFilterStatus);
                binding.executePendingBindings();
            }
        }
        for (YesNoFilterViewBinding binding : yesNoBindings) {
            binding.setStatus(sortFilterStatus);
            binding.executePendingBindings();
        }
        binding.levelFilterRange.setStatus(sortFilterStatus);
        binding.levelFilterRange.executePendingBindings();
    }

    private void updateRangeView(Class<? extends QuantityType> quantityType, RangeFilterLayoutBinding rangeBinding) {

        // Get the appropriate data
        final int minValue = sortFilterStatus.getMinValue(quantityType);
        final int maxValue = sortFilterStatus.getMaxValue(quantityType);
        final Unit minUnit = sortFilterStatus.getMinUnit(quantityType);
        final Unit maxUnit = sortFilterStatus.getMaxUnit(quantityType);

        // Set the min and max text
        final EditText minET = rangeBinding.rangeMinEntry;
        minET.setText(formattedInteger(minValue));
        final EditText maxET = rangeBinding.rangeMaxEntry;
        maxET.setText(formattedInteger(maxValue));

        // Set the min and max units
        final Spinner minUnitSpinner = rangeBinding.rangeMinSpinner;
        final Spinner maxUnitSpinner = rangeBinding.rangeMaxSpinner;
        final UnitTypeSpinnerAdapter unitAdapter = (UnitTypeSpinnerAdapter) minUnitSpinner.getAdapter();
        final List units = Arrays.asList(unitAdapter.getData());
        minUnitSpinner.setSelection(units.indexOf(minUnit), false);
        maxUnitSpinner.setSelection(units.indexOf(maxUnit), false);

        // Set the visibility appropriately
        rangeBinding.getRoot().setVisibility(sortFilterStatus.getSpanningTypeVisibility(quantityType));

    }

    private <Q extends QuantityType> void setupRangeView(RangeFilterLayoutBinding rangeBinding, Class<Q> quantityType) {

        // Get the range filter info
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
            unitPluralNames[i] = DisplayUtils.getPluralName(context, units[i]);
        }

        // Set up the min spinner
        final int textSize = 14;
        final Spinner minUnitSpinner = rangeBinding.rangeMinSpinner;
        final UnitTypeSpinnerAdapter minUnitAdapter = new UnitTypeSpinnerAdapter(context, unitType, textSize);
        minUnitSpinner.setAdapter(minUnitAdapter);
        //minUnitSpinner.setTag(R.integer.key_0, 0); // Min or max
        //minUnitSpinner.setTag(R.integer.key_1, unitType); // Unit type
        //minUnitSpinner.setTag(R.integer.key_2, quantityType); // Quantity type

        // Set up the max spinner
        final Spinner maxUnitSpinner = rangeBinding.rangeMaxSpinner;
        final UnitTypeSpinnerAdapter maxUnitAdapter = new UnitTypeSpinnerAdapter(context, unitType, textSize);
        maxUnitSpinner.setAdapter(maxUnitAdapter);
        //maxUnitSpinner.setTag(R.integer.key_0, 1); // Min or max
        //maxUnitSpinner.setTag(R.integer.key_1, unitType); // Unit type
        //maxUnitSpinner.setTag(R.integer.key_2, quantityType); // Quantity type

        // Set what happens when the spinners are changed
        final TriConsumer<SortFilterStatus, Class<? extends QuantityType>, Unit> minSetter = SortFilterStatus::setMinUnit;
        final TriConsumer<SortFilterStatus, Class<? extends QuantityType>, Unit> maxSetter = SortFilterStatus::setMaxUnit;
        final SortFilterFragment.UnitSpinnerListener minUnitListener = new SortFilterFragment.UnitSpinnerListener(unitType, quantityType, minSetter);
        final SortFilterFragment.UnitSpinnerListener maxUnitListener = new SortFilterFragment.UnitSpinnerListener(unitType, quantityType, maxSetter);
        minUnitSpinner.setOnItemSelectedListener(minUnitListener);
        maxUnitSpinner.setOnItemSelectedListener(maxUnitListener);

        // Set up the min and max text views
        final EditText minET = rangeBinding.rangeMinEntry;
        minET.setTag(quantityType);
        minET.setFilters( new InputFilter[] { new InputFilter.LengthFilter(maxLength) } );
        minET.setOnFocusChangeListener( (v, hasFocus) -> {
            if (!hasFocus) {
                if (sortFilterStatus == null) { return; }
                final Class<Q> type = (Class<Q>) minET.getTag();
                int min;
                try {
                    min = Integer.parseInt(minET.getText().toString());
                } catch (NumberFormatException nfe) {
                    min = SortFilterStatus.getDefaultMinValue(type);
                    minET.setText(formattedInteger(min));
                    final Unit unit = SortFilterStatus.getDefaultMinUnit(type);
                    final UnitTypeSpinnerAdapter adapter = (UnitTypeSpinnerAdapter) minUnitSpinner.getAdapter();
                    final List spinnerObjects = Arrays.asList(adapter.getData());
                    minUnitSpinner.setSelection(spinnerObjects.indexOf(unit));
                    sortFilterStatus.setMinUnit(type, unit);
                }
                sortFilterStatus.setMinValue(type, min);
            }
        });
        final EditText maxET = rangeBinding.rangeMaxEntry;
        maxET.setTag(quantityType);
        maxET.setFilters( new InputFilter[] { new InputFilter.LengthFilter(maxLength) } );
        maxET.setOnFocusChangeListener( (v, hasFocus) -> {
            if (!hasFocus) {
                if (sortFilterStatus == null) { return; }
                final Class<Q> type = (Class<Q>) minET.getTag();
                int max;
                try {
                    max = Integer.parseInt(maxET.getText().toString());
                } catch (NumberFormatException nfe) {
                    max = SortFilterStatus.getDefaultMaxValue(type);
                    maxET.setText(formattedInteger(max));
                    final Unit unit = SortFilterStatus.getDefaultMaxUnit(type);
                    final UnitTypeSpinnerAdapter adapter = (UnitTypeSpinnerAdapter) maxUnitSpinner.getAdapter();
                    final List spinnerObjects = Arrays.asList(adapter.getData());
                    maxUnitSpinner.setSelection(spinnerObjects.indexOf(unit));
                    sortFilterStatus.setMaxUnit(type, unit);
                }
                sortFilterStatus.setMaxValue(quantityType, max);
            }
        });

        // Set up the restore defaults button
        final Button restoreDefaultsButton = rangeBinding.restoreDefaultsButton;
        restoreDefaultsButton.setTag(quantityType);
        restoreDefaultsButton.setOnClickListener((v) -> {
            if (sortFilterStatus == null) { return; }
            final Class<Q> type = (Class<Q>) v.getTag();
            final Unit minUnit = SortFilterStatus.getDefaultMinUnit(type);
            final Unit maxUnit = SortFilterStatus.getDefaultMaxUnit(type);
            final int minValue = SortFilterStatus.getDefaultMinValue(type);
            final int maxValue = SortFilterStatus.getDefaultMaxValue(type);
            minET.setText(formattedInteger(minValue));
            maxET.setText(formattedInteger(maxValue));
            final UnitTypeSpinnerAdapter adapter = (UnitTypeSpinnerAdapter) minUnitSpinner.getAdapter();
            final List spinnerObjects = Arrays.asList(adapter.getData());
            minUnitSpinner.setSelection(spinnerObjects.indexOf(minUnit));
            maxUnitSpinner.setSelection(spinnerObjects.indexOf(maxUnit));
            sortFilterStatus.setRangeBoundsToDefault(type);
        });

    }

    private void setup() {
        viewModel.setSuspendSpellListModifications(true);
        setupSortElements();
        setupFilterOptions();
        setupLevelFilter();
        setupRitualConcentrationFilters();
        setupComponentsFilters();
        populateFilterBindings();
        setupExpandingViews();
        //setupSortStatusButtons();
        viewModel.setSuspendSpellListModifications(false);
    }

    void openOptionInfoDialog(FilterOptionBinding binding) {
        final OptionInfoDialog dialog = new OptionInfoDialog();
        final Bundle args = new Bundle();
        args.putString(OptionInfoDialog.TITLE_KEY, binding.getTitle());
        args.putString(OptionInfoDialog.DESCRIPTION_KEY, binding.getDescription());
        dialog.setArguments(args);
        dialog.show(requireActivity().getSupportFragmentManager(), "filter_option_dialog");
    }

    private void setFilterSettings() {

        // Set the min and max level entries
        binding.levelFilterRange.minLevelEntry.setText(String.valueOf(sortFilterStatus.getMinSpellLevel()));
        binding.levelFilterRange.maxLevelEntry.setText(String.valueOf(sortFilterStatus.getMaxSpellLevel()));

        // Set the filter option selectors appropriately
        binding.filterOptions.filterListsLayout.optionChooser.setChecked(sortFilterStatus.getApplyFiltersToLists());
        binding.filterOptions.filterSearchLayout.optionChooser.setChecked(sortFilterStatus.getApplyFiltersToSearch());
        binding.filterOptions.useExpandedLayout.optionChooser.setChecked(sortFilterStatus.getUseTashasExpandedLists());

        // Set the right values for the ranges views
        for (HashMap.Entry<Class<? extends QuantityType>, RangeFilterLayoutBinding> entry : classToRangeMap.entrySet()) {
            updateRangeView(entry.getKey(), entry.getValue());
        }
    }

    // When changing character profiles, this adjusts the sort settings to match the new profile
    private void setSortSettings() {

        final SortLayoutBinding sortBinding = binding.sortBlock;

        // Set the spinners to the appropriate positions
        final Spinner sortSpinner1 = sortBinding.sortField1Spinner;
        final Spinner sortSpinner2 = sortBinding.sortField2Spinner;
        final NamedSpinnerAdapter<SortField> adapter = (NamedSpinnerAdapter<SortField>) sortSpinner1.getAdapter();
        final List<SortField> sortData = Arrays.asList(adapter.getData());
        final SortField sf1 = sortFilterStatus.getFirstSortField();
        sortSpinner1.setSelection(sortData.indexOf(sf1), false);
        final SortField sf2 = sortFilterStatus.getSecondSortField();
        sortSpinner2.setSelection(sortData.indexOf(sf2), false);

        // Set the sort directions
        final boolean reverse1 = sortFilterStatus.getFirstSortReverse();
        final SortDirectionButton sortArrow1 = sortBinding.sortField1Arrow;
        if (reverse1) {
            sortArrow1.setUp();
        } else {
            sortArrow1.setDown();
        }
        final boolean reverse2 = sortFilterStatus.getSecondSortReverse();
        final SortDirectionButton sortArrow2 = sortBinding.sortField2Arrow;
        if (reverse2) {
            sortArrow2.setUp();
        } else {
            sortArrow2.setDown();
        }

    }

    void updateSortFilterStatus(SortFilterStatus sortFilterStatus) {
        viewModel.setSuspendSpellListModifications(true);
        this.sortFilterStatus = sortFilterStatus;
        updateSortFilterBindings();
        setFilterSettings();
        setSortSettings();
        viewModel.setSuspendSpellListModifications(false);
    }

    class UnitSpinnerListener<Q extends QuantityType, U extends Unit> implements AdapterView.OnItemSelectedListener {

        private final Class<U> unitType;
        private final Class<Q> quantityType;
        private final TriConsumer<SortFilterStatus, Class<? extends QuantityType>, Unit> setter;

        UnitSpinnerListener(Class<U> unitType, Class<Q> quantityType, TriConsumer<SortFilterStatus, Class<? extends QuantityType>, Unit> setter) {
            this.unitType = unitType;
            this.quantityType = quantityType;
            this.setter = setter;
        }

        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            // Get the character profile
            final SortFilterStatus sortFilterStatus = SortFilterFragment.this.viewModel.getSortFilterStatus();

            // Null checks
            if (sortFilterStatus == null || adapterView == null || adapterView.getAdapter() == null) { return; }

            // Set the appropriate unit in the character profile
            final U unit = unitType.cast(adapterView.getItemAtPosition(i));
            setter.accept(sortFilterStatus, quantityType, unit);
        }

        public void onNothingSelected(AdapterView<?> adapterView) {}
    }
}
