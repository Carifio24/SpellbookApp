package dnd.jon.spellbook;

import android.text.InputFilter;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.viewbinding.ViewBinding;

import org.javatuples.Pair;
import org.javatuples.Sextet;
import org.javatuples.Triplet;
import org.json.JSONException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import dnd.jon.spellbook.databinding.FilterBlockLayoutBinding;
import dnd.jon.spellbook.databinding.FilterBlockRangeLayoutBinding;
import dnd.jon.spellbook.databinding.ItemFilterViewBinding;
import dnd.jon.spellbook.databinding.LevelFilterLayoutBinding;
import dnd.jon.spellbook.databinding.RangeFilterLayoutBinding;
import dnd.jon.spellbook.databinding.RitualConcentrationLayoutBinding;
import dnd.jon.spellbook.databinding.SortFilterGroupHeaderBinding;
import dnd.jon.spellbook.databinding.SortLayoutBinding;
import dnd.jon.spellbook.databinding.YesNoFilterViewBinding;

class SortFilterExpandableAdapter extends BaseExpandableListAdapter {

    private static final HashMap<Class<? extends NameDisplayable>,  Triplet<Boolean, Integer, Integer>> filterBlockInfo = new HashMap<Class<? extends NameDisplayable>, Triplet<Boolean, Integer, Integer>>() {{
        put(Sourcebook.class, new Triplet<>(false, R.string.sourcebook_filter_title, R.integer.sourcebook_filter_columns));
        put(CasterClass.class, new Triplet<>(false,  R.string.caster_filter_title, R.integer.caster_filter_columns));
        put(School.class, new Triplet<>(false, R.string.school_filter_title, R.integer.school_filter_columns));
        put(CastingTime.CastingTimeType.class, new Triplet<>(true, R.string.casting_time_type_filter_title, R.integer.casting_time_type_filter_columns));
        put(Duration.DurationType.class, new Triplet<>(true, R.string.duration_type_filter_title, R.integer.duration_type_filter_columns));
        put(Range.RangeType.class, new Triplet<>(true, R.string.range_type_filter_title, R.integer.range_type_filter_columns));
    }};

    // The Quartets consist of
    // Superclass, Filter/Range view ID, min text, max text, max entry length
    private static final HashMap<Class<? extends QuantityType>, Triplet<Class<? extends Unit>, Integer, Integer>> rangeViewInfo = new HashMap<Class<? extends QuantityType>, Triplet<Class<? extends Unit>, Integer, Integer>>()  {{
        put(CastingTime.CastingTimeType.class, new Triplet<>(TimeUnit.class, R.string.casting_time_range_text, R.integer.casting_time_max_length));
        put(Duration.DurationType.class, new Triplet<>(TimeUnit.class, R.string.duration_range_text, R.integer.duration_max_length));
        put(Range.RangeType.class, new Triplet<>(LengthUnit.class, R.string.range_range_text, R.integer.range_max_length));
    }};

    private final MainActivity main;
    private final List<Triplet<ViewBinding,String,Integer>> bindingsAndTitles = new ArrayList<>();
    private final HashMap<Class<? extends NameDisplayable>, ArrayList<ItemFilterViewBinding>> classToBindingsMap = new HashMap<>();
    private final List<YesNoFilterViewBinding> yesNoBindings = new ArrayList<>();
    private final HashMap<Class<? extends QuantityType>, RangeFilterLayoutBinding> classToRangeMap = new HashMap<>();

    // Bindings
    private SortLayoutBinding sortBinding;
    private LevelFilterLayoutBinding levelBinding;
    private RitualConcentrationLayoutBinding ritualConcentrationBinding;

    // Perform sorting and filtering only if we're on a tablet layout
    // This is useful for the sort/filter window stuff
    // On a phone layout, we can defer these operations until the sort/filter window is closed, as the spells aren't visible until then
    // But on a tablet layout they're always visible, so we need to account for that
    private final Runnable sortOnTablet;
    private final Runnable filterOnTablet;

    SortFilterExpandableAdapter(MainActivity main) {
        this.main = main;
        sortOnTablet = () -> { if (main.usingTablet()) { main.sort(); } };
        filterOnTablet = () -> { if (main.usingTablet()) { main.filter(); } };
        setUpViews();
    }

    private void addBindingTitleSize(ViewBinding binding, String title, int textSize) {
        bindingsAndTitles.add(new Triplet<>(binding, title, textSize));
    }

    @Override
    public int getGroupCount() { return bindingsAndTitles.size(); }

    @Override
    public int getChildrenCount(int groupPosition) { return 1; }

    @Override
    public Object getGroup(int groupPosition) {
        final Triplet<ViewBinding,String,Integer> groupData = bindingsAndTitles.get(groupPosition);
        return new Pair<>(groupData.getValue1(), groupData.getValue2());
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return bindingsAndTitles.get(groupPosition).getValue0();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() { return true; }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final SortFilterGroupHeaderBinding binding = SortFilterGroupHeaderBinding.inflate(main.getLayoutInflater());
        final Pair<String,Integer> groupData = (Pair<String,Integer>) getGroup(groupPosition);
        binding.headerTitle.setText(groupData.getValue0());
        binding.headerTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, groupData.getValue1());
        return binding.getRoot();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ViewBinding binding = (ViewBinding) getChild(groupPosition, childPosition);
        return binding.getRoot();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) { return true; }

    private String stringFromID(int stringID) {
        return main.getResources().getString(stringID);
    }

    private float dimensionFromID(int dimensionID) {
        return main.getResources().getDimension(dimensionID);
    }




    // Do any initial setup required
    private void setUpViews() {

        // Set up the sorting view
        setupSortView();

        // Set up the level range
        setUpLevelRange();

        // Populate the ritual and concentration views
        setupRitualConcentrationFilters();

        // Populate the filter bindings
        classToBindingsMap.put(Sourcebook.class, populateFilters(Sourcebook.class));
        classToBindingsMap.put(CasterClass.class, populateFilters(CasterClass.class));
        classToBindingsMap.put(School.class, populateFilters(School.class));
        classToBindingsMap.put(CastingTime.CastingTimeType.class, populateFilters(CastingTime.CastingTimeType.class));
        classToBindingsMap.put(Duration.DurationType.class, populateFilters(Duration.DurationType.class));
        classToBindingsMap.put(Range.RangeType.class, populateFilters(Range.RangeType.class));

    }


    private void setupSortView() {

        // Inflate the sort layout binding
        sortBinding = SortLayoutBinding.inflate(main.getLayoutInflater());

        // Get various UI elements
        final Spinner sort1 = sortBinding.sortField1Spinner;
        final Spinner sort2 = sortBinding.sortField2Spinner;
        final SortDirectionButton sortArrow1 = sortBinding.sortField1Arrow;
        final SortDirectionButton sortArrow2 = sortBinding.sortField2Arrow;

        // Set tags for the sorting UI elements
        sort1.setTag(1);
        sort2.setTag(2);
        sortArrow1.setTag(1);
        sortArrow2.setTag(2);

        //The list of sort fields
        final String[] sortObjects = Arrays.copyOf(Spellbook.sortFieldNames, Spellbook.sortFieldNames.length);

        // Populate the dropdown spinners
        final SortFilterSpinnerAdapter sortAdapter1 = new SortFilterSpinnerAdapter(main, sortObjects);
        final SortFilterSpinnerAdapter sortAdapter2 = new SortFilterSpinnerAdapter(main, sortObjects);
        sort1.setAdapter(sortAdapter1);
        sort2.setAdapter(sortAdapter2);


        // Set what happens when the sort spinners are changed
        final AdapterView.OnItemSelectedListener sortListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                final CharacterProfile profile = main.getCharacterProfile();
                if (profile == null) { return; }
                final String itemName = (String) adapterView.getItemAtPosition(i);
                final int tag = (int) adapterView.getTag();
                final SortField sf = SpellbookUtils.coalesce(SortField.fromDisplayName(itemName), SortField.NAME);
                profile.setSortField(sf, tag);
                main.saveCharacterProfile();
                sortOnTablet.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        sort1.setOnItemSelectedListener(sortListener);
        sort2.setOnItemSelectedListener(sortListener);

        // Set what happens when the arrow buttons are pressed
        final SortDirectionButton.OnClickListener arrowListener = (View view) -> {
            final SortDirectionButton b = (SortDirectionButton) view;
            b.onPress();
            main.sort();
            final boolean up = b.pointingUp();
            final CharacterProfile profile = main.getCharacterProfile();
            if (profile == null) { return; }
            final int tag = (int) view.getTag();
            profile.setSortReverse(up, tag);
        };
        sortArrow1.setOnClickListener(arrowListener);
        sortArrow2.setOnClickListener(arrowListener);

        // Add the sort block view, along with its title, to our main list
        addBindingTitleSize(sortBinding, stringFromID(R.string.sort_title), (int) dimensionFromID(R.dimen.sort_filter_titles_text_size));
    }


    // The code for populating the filters is all essentially the same
    // So we can just use this generic function to remove redundancy
    private <E extends Enum<E> & NameDisplayable> ArrayList<ItemFilterViewBinding> populateFilters(Class<E> enumType) {

        // Get the GridLayout and the appropriate column weight
        final Triplet<Boolean,Integer,Integer> data = filterBlockInfo.get(enumType);
        final boolean rangeNeeded = data.getValue0();
        final String title = stringFromID(data.getValue1());
        final int size = (int) dimensionFromID(R.dimen.sort_filter_titles_text_size);
        final int columns = main.getResources().getInteger(data.getValue2());
        final FilterBlockRangeLayoutBinding blockRangeBinding = rangeNeeded ? FilterBlockRangeLayoutBinding.inflate(main.getLayoutInflater()) : null;
        final FilterBlockLayoutBinding blockBinding = rangeNeeded ? blockRangeBinding.filterBlock : FilterBlockLayoutBinding.inflate(main.getLayoutInflater());
        final GridLayout gridLayout = blockBinding.filterGrid.filterGridLayout;
        final Button selectAllButton = blockBinding.selectAllButton;
        final ViewBinding bindingToAdd = rangeNeeded ? blockRangeBinding : blockBinding;
        gridLayout.setColumnCount(columns);
        addBindingTitleSize(bindingToAdd, title, size);

        // An empty list of bindings. We'll populate this and return it
        final ArrayList<ItemFilterViewBinding> bindings = new ArrayList<>();

        // Get an array of instances of the Enum type
        final E[] enums = enumType.getEnumConstants();

        // If this isn't an enum type, return our (currently empty) list
        // This should never happens
        if (enums == null) { return bindings; }

        // Get the character profile
        final CharacterProfile profile = main.getCharacterProfile();


        // The default thing to do for one of the filter buttons
        final Consumer<ToggleButton> defaultConsumer = (v) -> {
            main.getCharacterProfile().toggleVisibility((E) v.getTag());
            main.saveCharacterProfile();
            filterOnTablet.run();
        };

        // Populate the list of bindings, one for each instance of the given Enum type
        for (E e : enums) {

            // Inflate the binding
            final ItemFilterViewBinding binding = DataBindingUtil.inflate(main.getLayoutInflater(), R.layout.item_filter_view, null, false);

            // Bind the relevant values
            binding.setProfile(profile);
            binding.setItem(e);
            binding.executePendingBindings();

            // Get the root view
            final View view = binding.getRoot();

            // Set up the toggle button
            final ToggleButton button = binding.itemFilterButton;
            button.setTag(e);
            final Consumer<ToggleButton> toggleButtonConsumer;

            // On a long press, turn off all other buttons in this grid, and turn this one on
            final Consumer<ToggleButton> longPressConsumer = (v) -> {
                if (!v.isSet()) { v.callOnClick(); }
                final GridLayout grid = (GridLayout) v.getParent().getParent();
                for (int i = 0; i < grid.getChildCount(); ++i) {
                    final View x = grid.getChildAt(i);
                    final ToggleButton tb = x.findViewById(R.id.item_filter_button);
                    if (tb != v && tb.isSet()) {
                        tb.callOnClick();
                    }
                }
            };
            button.setLongPressCallback(longPressConsumer);

            // Set up the select all button
            selectAllButton.setTag(gridLayout);
            selectAllButton.setOnClickListener((v) -> {
                final GridLayout grid = (GridLayout) v.getTag();
                for (int i = 0; i < grid.getChildCount(); ++i) {
                    final View x = grid.getChildAt(i);
                    final ToggleButton tb = x.findViewById(R.id.item_filter_button);
                    if (!tb.isSet()) {
                        tb.callOnClick();
                    }
                }
            });

            // If this is a spanning type, we want to also set up the range view, set the button to toggle the corresponding range view's visibility,
            // as well as do some other stuff
            final boolean spanning = ( (e instanceof QuantityType) && ( ((QuantityType) e).isSpanningType()));
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

            button.setCallback(toggleButtonConsumer);
            gridLayout.addView(view);
            bindings.add(binding);
        }
        return bindings;
    }

    private <E extends QuantityType> void setupRangeView(RangeFilterLayoutBinding rangeBinding, E e) {

        // Get the view for the binding
        final View rangeView = rangeBinding.getRoot();

        // Get the range filter info
        final Class<? extends QuantityType> quantityType = e.getClass();
        final Triplet<Class<? extends Unit>,Integer,Integer> info = rangeViewInfo.get(quantityType);
        final Class<? extends Unit> unitType = info.getValue0();
        rangeView.setTag(quantityType);
        final String rangeText = main.getResources().getString(info.getValue1());
        final int maxLength = main.getResources().getInteger(info.getValue2());

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
        final int textSize = 12;
        final Spinner minUnitSpinner = rangeBinding.rangeMinSpinner;
        final SortFilterSpinnerAdapter minUnitAdapter = new SortFilterSpinnerAdapter(main, unitPluralNames, textSize);
        minUnitSpinner.setAdapter(minUnitAdapter);
        minUnitSpinner.setTag(R.integer.key_0, 0); // Min or max
        minUnitSpinner.setTag(R.integer.key_1, unitType); // Unit type
        minUnitSpinner.setTag(R.integer.key_2, quantityType); // Quantity type

        // Set up the max spinner
        final Spinner maxUnitSpinner = rangeBinding.rangeMaxSpinner;
        final SortFilterSpinnerAdapter maxUnitAdapter = new SortFilterSpinnerAdapter(main, Arrays.copyOf(unitPluralNames, unitPluralNames.length), textSize);
        maxUnitSpinner.setAdapter(maxUnitAdapter);
        maxUnitSpinner.setTag(R.integer.key_0, 1); // Min or max
        maxUnitSpinner.setTag(R.integer.key_1, unitType); // Unit type
        maxUnitSpinner.setTag(R.integer.key_2, quantityType); // Quantity type

        // Set what happens when the spinners are changed
        final AdapterView.OnItemSelectedListener unitListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                final String itemName = (String) adapterView.getItemAtPosition(i);
                final int tag = (int) adapterView.getTag(R.integer.key_0);
                final Class<? extends Unit> unitType = (Class<? extends Unit>) adapterView.getTag(R.integer.key_1);
                final Class<? extends QuantityType> quantityType = (Class<? extends QuantityType>) adapterView.getTag(R.integer.key_2);
                try {
                    final Method method = unitType.getDeclaredMethod("fromString", String.class);
                    final Unit unit = (Unit) method.invoke(null, itemName);
                    switch (tag) {
                        case 0:
                            main.getCharacterProfile().setMinUnit(quantityType, unit);
                            break;
                        case 1:
                            main.getCharacterProfile().setMaxUnit(quantityType, unit);
                    }
                    main.saveCharacterProfile();
                    filterOnTablet.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                main.saveCharacterProfile();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        };
        minUnitSpinner.setOnItemSelectedListener(unitListener);
        maxUnitSpinner.setOnItemSelectedListener(unitListener);

        // Set up the min and max text views
        final EditText minET = rangeBinding.rangeMinEntry;
        minET.setTag(quantityType);
        minET.setFilters( new InputFilter[] { new InputFilter.LengthFilter(maxLength) } );
        minET.setOnFocusChangeListener( (v, hasFocus) -> {
            if (!hasFocus) {
                final Class<? extends QuantityType> type = (Class<? extends QuantityType>) minET.getTag();
                int min;
                try {
                    min = Integer.parseInt(minET.getText().toString());
                } catch (NumberFormatException nfe) {
                    min = CharacterProfile.getDefaultMinValue(type);
                    minET.setText(String.format(Locale.US, "%d", min));
                    final Unit unit = CharacterProfile.getDefaultMinUnit(type);
                    final SortFilterSpinnerAdapter adapter = (SortFilterSpinnerAdapter) minUnitSpinner.getAdapter();
                    final List<String> spinnerObjects = Arrays.asList(adapter.getData());
                    minUnitSpinner.setSelection(spinnerObjects.indexOf(unit.pluralName()));
                    main.getCharacterProfile().setMinUnit(quantityType, unit);
                }
                main.getCharacterProfile().setMinValue(quantityType, min);
                main.saveCharacterProfile();
                filterOnTablet.run();
            }
        });
        final EditText maxET = rangeBinding.rangeMaxEntry;
        maxET.setTag(quantityType);
        maxET.setFilters( new InputFilter[] { new InputFilter.LengthFilter(maxLength) } );
        maxET.setOnFocusChangeListener( (v, hasFocus) -> {
            if (!hasFocus) {
                final Class<? extends QuantityType> type = (Class<? extends QuantityType>) maxET.getTag();
                int max;
                try {
                    max = Integer.parseInt(maxET.getText().toString());
                } catch (NumberFormatException nfe) {
                    max = CharacterProfile.getDefaultMaxValue(type);
                    maxET.setText(String.format(Locale.US, "%d", max));
                    final Unit unit = CharacterProfile.getDefaultMaxUnit(type);
                    final SortFilterSpinnerAdapter adapter = (SortFilterSpinnerAdapter) maxUnitSpinner.getAdapter();
                    final List<String> spinnerObjects = Arrays.asList(adapter.getData());
                    maxUnitSpinner.setSelection(spinnerObjects.indexOf(unit.pluralName()));
                    main.getCharacterProfile().setMaxUnit(quantityType, unit);
                }
                main.getCharacterProfile().setMaxValue(quantityType, max);
                main.saveCharacterProfile();
                filterOnTablet.run();
            }
        });

        // Set up the restore defaults button
        final Button restoreDefaultsButton = rangeBinding.restoreDefaultsButton;
        restoreDefaultsButton.setTag(quantityType);
        restoreDefaultsButton.setOnClickListener((v) -> {
            final Class<? extends QuantityType> type = (Class<? extends QuantityType>) v.getTag();
            final Unit minUnit = CharacterProfile.getDefaultMinUnit(type);
            final Unit maxUnit = CharacterProfile.getDefaultMaxUnit(type);
            final int minValue = CharacterProfile.getDefaultMinValue(type);
            final int maxValue = CharacterProfile.getDefaultMaxValue(type);
            minET.setText(String.format(Locale.US, "%d", minValue));
            maxET.setText(String.format(Locale.US, "%d", maxValue));
            final SortFilterSpinnerAdapter adapter = (SortFilterSpinnerAdapter) minUnitSpinner.getAdapter();
            final List<String> spinnerObjects = Arrays.asList(adapter.getData());
            minUnitSpinner.setSelection(spinnerObjects.indexOf(minUnit.pluralName()));
            maxUnitSpinner.setSelection(spinnerObjects.indexOf(maxUnit.pluralName()));
            main.getCharacterProfile().setRangeToDefaults(type);
            main.saveCharacterProfile();
            filterOnTablet.run();
        });

    }

    private void setupRitualConcentrationFilters() {

        // Inflate the binding
        ritualConcentrationBinding = RitualConcentrationLayoutBinding.inflate(main.getLayoutInflater());

        // Get the character profile
        final CharacterProfile profile = main.getCharacterProfile();

        // Set up the ritual binding
        final YesNoFilterViewBinding ritualBinding = DataBindingUtil.inflate(main.getLayoutInflater(), R.layout.yes_no_filter_view, null, false);
        ritualBinding.setProfile(profile);
        ritualBinding.setTitle(main.getResources().getString(R.string.ritual_filter_title));
        ritualBinding.setStatusGetter(CharacterProfile::getRitualFilter);
        ritualBinding.executePendingBindings();
        yesNoBindings.add(ritualBinding);

        // Set up the concentration binding
        final YesNoFilterViewBinding concentrationBinding = DataBindingUtil.inflate(main.getLayoutInflater(), R.layout.yes_no_filter_view, null, false);
        concentrationBinding.setProfile(profile);
        concentrationBinding.setTitle(main.getResources().getString(R.string.concentration_filter_title));
        concentrationBinding.setStatusGetter(CharacterProfile::getConcentrationFilter);
        ritualBinding.executePendingBindings();
        yesNoBindings.add(concentrationBinding);

        // Set up the onClickListeners and add the views to the LinearLayout
        final GridLayout gridLayout = ritualConcentrationBinding.ritualConcentrationContent;
        final int horizontalPadding = 25;
        final int textSizeID = main.usingTablet() ? R.dimen.sort_filter_titles_text_size : R.dimen.sort_filter_titles_smaller_text_size;
        final int textSize = (int) dimensionFromID(textSizeID);
        final List<Pair<YesNoFilterViewBinding, BiConsumer<CharacterProfile,Boolean>>> viewsAndTogglers = Arrays.asList(new Pair<>(ritualBinding, CharacterProfile::toggleRitualFilter), new Pair<>(concentrationBinding, CharacterProfile::toggleConcentrationFilter));
        for (Pair<YesNoFilterViewBinding,BiConsumer<CharacterProfile,Boolean>> pair : viewsAndTogglers) {
            final YesNoFilterViewBinding binding = pair.getValue0();
            final View view = binding.getRoot();
            final BiConsumer<CharacterProfile,Boolean> toggler = pair.getValue1();
            final ToggleButton yButton = binding.yesOption.optionFilterButton;
            yButton.setCallback( () -> { toggler.accept(main.getCharacterProfile(), true); main.saveCharacterProfile(); filterOnTablet.run(); });
            final ToggleButton nButton = binding.noOption.optionFilterButton;
            nButton.setCallback( () -> { toggler.accept(main.getCharacterProfile(), false); main.saveCharacterProfile(); filterOnTablet.run(); });
            view.setPadding(horizontalPadding, 0, horizontalPadding, 0);
            gridLayout.addView(view);
        }

        addBindingTitleSize(ritualConcentrationBinding, stringFromID(R.string.ritual_concentration_filter_title), textSize);

    }


    private void setUpLevelRange() {

        // Inflate the binding
        levelBinding = LevelFilterLayoutBinding.inflate(main.getLayoutInflater());

        final EditText minLevelET = levelBinding.minLevelInput;
        minLevelET.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                final TextView tv = (TextView) v;
                int level;
                try {
                    level = Integer.parseInt(tv.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    tv.setText(String.format(Locale.US, "%d", Spellbook.MIN_SPELL_LEVEL));
                    return;
                }
                main.getCharacterProfile().setMinSpellLevel(level);
                main.saveCharacterProfile();
                filterOnTablet.run();
            }
        });

        final EditText maxLevelET = levelBinding.maxLevelInput;
        maxLevelET.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                final TextView tv = (TextView) v;
                int level;
                try {
                    level = Integer.parseInt(tv.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    tv.setText(String.format(Locale.US, "%d", Spellbook.MAX_SPELL_LEVEL));
                    return;
                }
                main.getCharacterProfile().setMaxSpellLevel(level);
                main.saveCharacterProfile();
                filterOnTablet.run();
            }
        });

        addBindingTitleSize(levelBinding, stringFromID(R.string.level_filter_title), (int) dimensionFromID(R.dimen.sort_filter_titles_text_size));
    }

    void updateProfileBindings() {
        final CharacterProfile profile = main.getCharacterProfile();
        for (ArrayList<ItemFilterViewBinding> bindings : classToBindingsMap.values()) {
            for (ItemFilterViewBinding binding : bindings) {
                binding.setProfile(profile);
                binding.executePendingBindings();
            }
        }
        for (YesNoFilterViewBinding binding : yesNoBindings) {
            binding.setProfile(profile);
            binding.executePendingBindings();
        }
        levelBinding.setProfile(profile);
        levelBinding.executePendingBindings();
        updateSortSettings(profile);
        updateRangeViews(profile);

    }

    private void updateRangeViews(CharacterProfile profile) {

        // Set the right values for the range views
        for (HashMap.Entry<Class<? extends QuantityType>, RangeFilterLayoutBinding> entry : classToRangeMap.entrySet()) {
            updateRangeView(profile, entry.getKey(), entry.getValue());
        }

    }

    private void updateSortSettings(CharacterProfile profile) {

        // Get the UI elements
        final Spinner sort1 = sortBinding.sortField1Spinner;
        final Spinner sort2 = sortBinding.sortField2Spinner;
        final SortDirectionButton sortArrow1 = sortBinding.sortField1Arrow;
        final SortDirectionButton sortArrow2 = sortBinding.sortField2Arrow;

        // Set the first sort spinner to the appropriate position
        final SortFilterSpinnerAdapter adapter = (SortFilterSpinnerAdapter) sort1.getAdapter();
        final List<String> sortData = Arrays.asList(adapter.getData());
        final SortField sf1 = profile.getFirstSortField();
        sort1.setSelection(sortData.indexOf(sf1.getDisplayName()));

        // Set the second sort spinner to the appropriate position
        final SortField sf2 = profile.getSecondSortField();
        sort2.setSelection(sortData.indexOf(sf2.getDisplayName()));

        // Set the sort directions
        final boolean reverse1 = profile.getFirstSortReverse();
        if (reverse1) {
            sortArrow1.setUp();
        } else {
            sortArrow1.setDown();
        }
        final boolean reverse2 = profile.getSecondSortReverse();
        if (reverse2) {
            sortArrow2.setUp();
        } else {
            sortArrow2.setDown();
        }
    }

    private void updateRangeView(CharacterProfile profile, Class<? extends QuantityType> quantityType, RangeFilterLayoutBinding rangeBinding) {

        // Get the appropriate data
        final Sextet<Class<? extends Quantity>, Class<? extends Unit>, Unit, Unit, Integer, Integer> data = profile.getQuantityRangeInfo(quantityType);

        // Set the min and max text
        final EditText minET = rangeBinding.rangeMinEntry;
        minET.setText(String.format(Locale.US, "%d", data.getValue4()));
        final EditText maxET = rangeBinding.rangeMaxEntry;
        maxET.setText(String.format(Locale.US, "%d", data.getValue5()));

        // Set the min and max units
        final Spinner minUnitSpinner = rangeBinding.rangeMinSpinner;
        final Spinner maxUnitSpinner = rangeBinding.rangeMaxSpinner;
        final SortFilterSpinnerAdapter unitAdapter = (SortFilterSpinnerAdapter) minUnitSpinner.getAdapter();
        final List<String> unitPluralNames = Arrays.asList(unitAdapter.getData());
        final Unit minUnit = data.getValue2();
        minUnitSpinner.setSelection(unitPluralNames.indexOf(minUnit.pluralName()));
        final Unit maxUnit = data.getValue3();
        maxUnitSpinner.setSelection(unitPluralNames.indexOf(maxUnit.pluralName()));

        // Set the visibility appropriately
        rangeBinding.getRoot().setVisibility(profile.getSpanningTypeVisible(quantityType));

    }

}
