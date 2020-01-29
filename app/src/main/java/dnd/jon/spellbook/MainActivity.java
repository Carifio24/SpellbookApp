package dnd.jon.spellbook;

import android.app.SearchManager;
import android.content.Context;

import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.EditText;
import android.content.Intent;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.javatuples.Quartet;
import org.javatuples.Quintet;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import dnd.jon.spellbook.databinding.ActivityMainBinding;
import dnd.jon.spellbook.databinding.ItemFilterViewBinding;

public class MainActivity extends AppCompatActivity {

    private final String spellsFilename = "Spells.json";
    private static ArrayList<Spell> spells = new ArrayList<>();


    private static final String settingsFile = "Settings.json";
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private NavigationView rightNavView;
    private ExpandableListView rightExpLV;
    private ExpandableListAdapter rightAdapter;
    private SearchView searchView;
    private MenuItem searchViewIcon;
    private MenuItem filterMenuIcon;
    private ConstraintLayout spellsCL;
    private ConstraintLayout filterCL;
    private ScrollView filterSV;

    private static final String profilesDirName = "Characters";
    private CharacterProfile characterProfile;
    private View characterSelect = null;
    private CharacterSelectionDialog selectionDialog = null;
    private File profilesDir;
    private Settings settings;

    // For filtering stuff
    private boolean filterVisible = false;
    private ArrayList<ItemFilterViewBinding> sourcebookFilterViewBindings;
    private ArrayList<ItemFilterViewBinding> casterFilterViewBindings;
    private ArrayList<ItemFilterViewBinding> schoolFilterViewBindings;
    private ArrayList<ItemFilterViewBinding> castingTimeFilterViewBindings;
    private ArrayList<ItemFilterViewBinding> durationTypeFilterViewBindings;
    private ArrayList<ItemFilterViewBinding> rangeTypeFilterViewBindings;
    private HashMap<QuantityType, ItemFilterViewBinding> spanningTypeFilterMap = new HashMap<>();

    private static final String spellBundleKey = "SPELL";
    private static final String spellIndexBundleKey = "SPELL_INDEX";

    private static final String devEmail = "dndspellbookapp@gmail.com";
    private static final String emailMessage = "[Android] Feedback";

    // The map ID -> StatusFilterField relating left nav bar items to the corresponding spell status filter
    private static final HashMap<Integer,StatusFilterField> statusFilterIDs = new HashMap<Integer,StatusFilterField>() {{
       put(R.id.nav_all, StatusFilterField.ALL);
       put(R.id.nav_favorites, StatusFilterField.FAVORITES);
       put(R.id.nav_prepared, StatusFilterField.PREPARED);
       put(R.id.nav_known, StatusFilterField.KNOWN);
    }};

    // Headers and expanding/contracting views for the sort/filter window
    private static final HashMap<Integer,Integer> expandingIDs = new HashMap<Integer,Integer>() {{
        put(R.id.sort_header, R.id.sort_content);
        put(R.id.ranges_filter_header, R.id.ranges_filter_content);
    }};

    private static final HashMap<Class<?>, Quartet<Boolean, Integer, Integer, Integer>> filterBlockInfo = new HashMap<Class<?>, Quartet<Boolean, Integer, Integer, Integer>>() {{
        put(Sourcebook.class, new Quartet<>(false, R.id.sourcebook_filter_block, R.string.sourcebook_filter_title, R.integer.sourcebook_filter_columns));
        put(CasterClass.class, new Quartet<>(false, R.id.caster_filter_block, R.string.caster_filter_title, R.integer.caster_filter_columns));
        put(School.class, new Quartet<>(false, R.id.school_filter_block, R.string.school_filter_title, R.integer.school_filter_columns));
        put(CastingTime.CastingTimeType.class, new Quartet<>(true, R.id.casting_time_filter_range, R.string.casting_time_type_filter_title, R.integer.casting_time_type_filter_columns));
        put(Duration.DurationType.class, new Quartet<>(true, R.id.duration_filter_range, R.string.duration_type_filter_title, R.integer.duration_type_filter_columns));
        put(Range.RangeType.class, new Quartet<>(true, R.id.range_filter_range, R.string.range_type_filter_title, R.integer.range_type_filter_columns));
    }};

    // The Quartets consist of
    // Superclass, Filter/Range view ID, min text, max text, max entry length
    private static final HashMap<Class<? extends QuantityType>, Quintet<Class<? extends Quantity>, Integer,Integer,Integer,Integer>> rangeViewInfo = new HashMap<Class<? extends QuantityType>, Quintet<Class<? extends Quantity>, Integer,Integer,Integer,Integer>>()  {{
        put(CastingTime.CastingTimeType.class, new Quintet<>(CastingTime.class, R.id.casting_time_filter_range, R.string.blank, R.string.blank,R.integer.casting_time_max_length));
        put(Duration.DurationType.class, new Quintet<>(Duration.class, R.id.duration_filter_range, R.string.blank, R.string.blank, R.integer.duration_max_length));
        put(Range.RangeType.class, new Quintet<>(Range.class, R.id.range_filter_range, R.string.blank, R.string.blank, R.integer.range_max_length));
    }};

    // The UI elements for sorting and searching
    private Spinner sort1;
    private Spinner sort2;
    private SortDirectionButton sortArrow1;
    private SortDirectionButton sortArrow2;

    // The RecyclerView and adapter for the table of spells
    private RecyclerView spellRecycler;
    private SpellRowAdapter spellAdapter;

    // The file extension for character files
    private static final String CHARACTER_EXTENSION = ".json";

    // Keys for Bundles
    private static final String FAVORITE_KEY = "FAVORITE";
    private static final String KNOWN_KEY = "KNOWN";
    private static final String PREPARED_KEY = "PREPARED";
    private static final String FILTER_VISIBLE_KEY = "FILTER_VISIBLE";

    // Whether or not this is running on a tablet
    private boolean onTablet;

    // For use with data binding on a tablet
    private ActivityMainBinding amBinding = null;
    private ConstraintLayout spellWindowCL = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout
        setContentView(R.layout.activity_main);

        // For data binding
        amBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Are we on a tablet or not?
        // If we're on a tablet, do the necessary setup
        onTablet = getResources().getBoolean(R.bool.isTablet);
        if (onTablet) { tabletSetup(); }

        // Get the main views
        spellsCL = findViewById(R.id.main_constraint_layout);
        filterCL = findViewById(R.id.sort_filter_window);
        filterSV = findViewById(R.id.sort_filter_scroll);

        // Re-set the current spell after a rotation (only needed on tablet)
        if (onTablet && savedInstanceState != null) {
            final Spell spell = savedInstanceState.containsKey(spellBundleKey) ? savedInstanceState.getParcelable(spellBundleKey) : null;
            final int spellIndex = savedInstanceState.containsKey(spellIndexBundleKey) ? savedInstanceState.getInt(spellIndexBundleKey) : -1;
            if (spell != null) {
                amBinding.setSpell(spell);
                amBinding.setSpellIndex(spellIndex);
                amBinding.executePendingBindings();
                spellWindowCL.setVisibility(View.VISIBLE);
                if (savedInstanceState.containsKey(FAVORITE_KEY) && savedInstanceState.containsKey(PREPARED_KEY) && savedInstanceState.containsKey(KNOWN_KEY)) {
                    updateSpellWindow(spell, savedInstanceState.getBoolean(FAVORITE_KEY), savedInstanceState.getBoolean(PREPARED_KEY), savedInstanceState.getBoolean(KNOWN_KEY));
                }
            }
        } else if (savedInstanceState != null) {
            filterVisible = savedInstanceState.containsKey(FILTER_VISIBLE_KEY) && savedInstanceState.getBoolean(FILTER_VISIBLE_KEY);
        }

        // Set the toolbar as the app bar for the activity
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // The DrawerLayout and the left navigation view
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.side_menu);
        final NavigationView.OnNavigationItemSelectedListener navViewListener = new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                final int index = menuItem.getItemId();
                boolean close = false;
                if (index == R.id.subnav_charselect) {
                    openCharacterSelection();
                } else if (index == R.id.nav_feedback) {
                    sendFeedback();
                } else if (statusFilterIDs.containsKey(index)) {
                    final StatusFilterField sff = statusFilterIDs.get(index);
                    characterProfile.setStatusFilter(sff);
                    saveCharacterProfile();
                    close = true;
                }
                filter();
                saveSettings();

                // This piece of code makes the drawer close when an item is selected
                // At the moment, we only want that for when choosing one of favorites, known, prepared
                if (close && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            }
        };
        navView.setNavigationItemSelectedListener(navViewListener);

        // Set the hamburger button to open the left nav
        final ActionBarDrawerToggle leftNavToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.left_navigation_drawer_open, R.string.left_navigation_drawer_closed);
        drawerLayout.addDrawerListener(leftNavToggle);
        leftNavToggle.syncState();
        leftNavToggle.setDrawerSlideAnimationEnabled(true); // Whether or not the hamburger button changes to the arrow when the drawer is open
        leftNavToggle.setDrawerIndicatorEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener((v) -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Set up the right navigation view
        setupRightNav();

        // Set up the sort table
        setupSortTable();

        //View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        /*int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/

        // Create the profiles directory, if necessary
        profilesDir = new File(getApplicationContext().getFilesDir(), profilesDirName);
        if ( !(profilesDir.exists() && profilesDir.isDirectory()) ) {
            final boolean success = profilesDir.mkdir();
            if (!success) {
                System.out.println("Error creating profiles directory"); // Add something real here eventually
            }
        }

        // Load the spell data
        // Since this is a static variable, we only need to do this once, when the app turns on
        // Doing it this way saves us from repeating this work every time the activity is recreated (such as from a rotation)
        if (spells.isEmpty()) {
            try {
                final JSONArray jsonArray = loadJSONArrayfromAsset(spellsFilename);
                spells = SpellCodec.parseSpellList(jsonArray);
            } catch (Exception e) {
                e.printStackTrace();
                this.finish();
            }
        }

        // Load the settings and the character profile
        try {

            // Load the settings
            final JSONObject json = loadJSONfromData(settingsFile);
            System.out.println(json.toString());
            settings = new Settings(json);

            // Load the character profile
            final String charName = settings.characterName();
            loadCharacterProfile(charName, true);

            // Set the character's name in the side menu
            setSideMenuCharacterName();

        } catch (Exception e) {
            String s = loadAssetAsString(new File(settingsFile));
            System.out.println("Error loading settings");
            System.out.println("The settings file content is: " + s);
            settings = new Settings();
            if (charactersList().size() > 0) {
                final String firstCharacter = charactersList().get(0);
                settings.setCharacterName(firstCharacter);
            }
            e.printStackTrace();
            saveSettings();
        }

        // If the character profile is null, we create one
        //System.out.println("Do we need to open a character creation window?");
        //System.out.println( (settings.characterName() == null) || characterProfile == null );
        if ( (settings.characterName() == null) || characterProfile == null ) {
            openCharacterCreationDialog();
        }

        // Set up the RecyclerView that holds the cells
        setupSpellRecycler(spells);

        // Set up the SwipeRefreshLayout
        setupSwipeRefreshLayout();

        // The right nav drawer often gets in the way of fast scrolling on a phone
        // Since we can open it from the action bar, we'll lock it closed from swiping
        if (!onTablet) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        }

        // Set up the sort/filter view
        setupSortFilterView();

        // Set the correct view visibilities
        if (filterVisible) {
            updateWindowVisibilities();
        }

        // Sort and filter if the filter isn't visible
        if (!filterVisible && characterProfile != null) {
            sort();
            filter();
        }

    }


    // Add actions to the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);

        // Get the filter menu button
        filterMenuIcon = menu.findItem(R.id.action_filter);

        // Associate searchable configuration with the SearchView
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchViewIcon = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchViewIcon.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Set up the SearchView functions
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                spellAdapter.getFilter().filter(text);
                return false;
            }
        });

        return true;
    }

    // To handle actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                toggleWindowVisibilities();
                return true;
            case R.id.action_info:
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    drawerLayout.openDrawer(GravityCompat.END);
                }
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Necessary for handling rotations
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (onTablet && amBinding != null && amBinding.getSpell() != null) {
            outState.putParcelable(spellBundleKey, amBinding.getSpell());
            outState.putInt(spellIndexBundleKey, amBinding.getSpellIndex());
            final ToggleButton favoriteButton = spellWindowCL.findViewById(R.id.favorite_button);
            final ToggleButton preparedButton = spellWindowCL.findViewById(R.id.prepared_button);
            final ToggleButton knownButton = spellWindowCL.findViewById(R.id.known_button);
            outState.putBoolean(FAVORITE_KEY, favoriteButton.isSet());
            outState.putBoolean(PREPARED_KEY, preparedButton.isSet());
            outState.putBoolean(KNOWN_KEY, knownButton.isSet());
        } else {
            outState.putBoolean(FILTER_VISIBLE_KEY, filterVisible);
        }
    }

    // Close the drawer with the back button if it's open
    @Override
    public void onBackPressed() {
        // InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.SPELL_WINDOW_REQUEST && resultCode == RESULT_OK) {
            final Spell s = data.getParcelableExtra(SpellWindow.SPELL_KEY);
            final boolean fav = data.getBooleanExtra(SpellWindow.FAVORITE_KEY, false);
            final boolean known = data.getBooleanExtra(SpellWindow.KNOWN_KEY, false);
            final boolean prepared = data.getBooleanExtra(SpellWindow.PREPARED_KEY, false);
            final int index = data.getIntExtra(SpellWindow.INDEX_KEY, -1);
            final boolean wasFav = characterProfile.isFavorite(s);
            final boolean wasKnown = characterProfile.isKnown(s);
            final boolean wasPrepared = characterProfile.isPrepared(s);
            characterProfile.setFavorite(s, fav);
            characterProfile.setKnown(s, known);
            characterProfile.setPrepared(s, prepared);
            final boolean changed = (wasFav != fav) || (wasKnown != known) || (wasPrepared != prepared);
            final Menu menu = navView.getMenu();
            final boolean oneChecked = menu.findItem(R.id.nav_favorites).isChecked() || menu.findItem(R.id.nav_known).isChecked() || menu.findItem(R.id.nav_prepared).isChecked();

            // Re-display the spells (if this spell's status changed) if we have at least one filter selected
            if (changed && oneChecked) {
                filter();
            }

            // If the spell's status changed, then save
            if (changed) {
                saveCharacterProfile();
                saveSettings();
            }

            // Reload the RecyclerView's data
            spellAdapter.notifyItemChanged(index);

        }
    }

    void openSpellWindow(Spell spell, int pos) {

        // On a phone, we're going to open a new window
        if (!onTablet) {
            final Intent intent = new Intent(MainActivity.this, SpellWindow.class);
            intent.putExtra(SpellWindow.SPELL_KEY, spell);
            intent.putExtra(SpellWindow.TEXT_SIZE_KEY, settings.spellTextSize());
            intent.putExtra(SpellWindow.FAVORITE_KEY, characterProfile.isFavorite(spell));
            intent.putExtra(SpellWindow.PREPARED_KEY, characterProfile.isPrepared(spell));
            intent.putExtra(SpellWindow.KNOWN_KEY, characterProfile.isKnown(spell));
            intent.putExtra(SpellWindow.INDEX_KEY, pos);
            startActivityForResult(intent, RequestCodes.SPELL_WINDOW_REQUEST);
            overridePendingTransition(R.anim.right_to_left_enter, R.anim.identity);
        }

        // On a tablet, we'll show the spell info on the right-hand side of the screen
        else {
            spellWindowCL.setVisibility(View.VISIBLE);
            amBinding.setSpell(spell);
            amBinding.setSpellIndex(pos);
            amBinding.executePendingBindings();
            final ToggleButton favoriteButton = spellWindowCL.findViewById(R.id.favorite_button);
            favoriteButton.set(characterProfile.isFavorite(spell));
            final ToggleButton preparedButton = spellWindowCL.findViewById(R.id.prepared_button);
            preparedButton.set(characterProfile.isPrepared(spell));
            final ToggleButton knownButton = spellWindowCL.findViewById(R.id.known_button);
            knownButton.set(characterProfile.isKnown(spell));
        }
    }

    void openSpellPopup(View view, Spell spell) {
        final SpellStatusPopup ssp = new SpellStatusPopup(this, spell);
        ssp.showUnderView(view);
    }

    void setupSpellRecycler(ArrayList<Spell> spells) {
        spellRecycler = findViewById(R.id.spell_recycler);
        final RecyclerView.LayoutManager spellLayoutManager = new LinearLayoutManager(this);
        spellAdapter = new SpellRowAdapter(spells);
        spellRecycler.setAdapter(spellAdapter);
        spellRecycler.setLayoutManager(spellLayoutManager);
    }

    void setupSortTable() {

        // Get various UI elements
        sort1 = filterCL.findViewById(R.id.sort_field_1_spinner);
        sort2 = filterCL.findViewById(R.id.sort_field_2_spinner);
        sortArrow1 = filterCL.findViewById(R.id.sort_field_1_arrow);
        sortArrow2 = filterCL.findViewById(R.id.sort_field_2_arrow);

        //The list of sort fields
        final String[] sortObjects = Arrays.copyOf(Spellbook.sortFieldNames, Spellbook.sortFieldNames.length);

        // Populate the dropdown spinners
        final SortFilterSpinnerAdapter sortAdapter1 = new SortFilterSpinnerAdapter(this, sortObjects);
        final SortFilterSpinnerAdapter sortAdapter2 = new SortFilterSpinnerAdapter(this, sortObjects);
        sort1.setAdapter(sortAdapter1);
        sort2.setAdapter(sortAdapter2);


        // Set what happens when the sort spinners are changed
        final AdapterView.OnItemSelectedListener sortListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sort();
                if (characterProfile == null) { return; }
                final String itemName = (String) adapterView.getItemAtPosition(i);
                final int tag = Integer.parseInt( (String) adapterView.getTag());
                final SortField sf = SpellbookUtils.coalesce(SortField.fromDisplayName(itemName), SortField.NAME);
                characterProfile.setSortField(sf, tag);
                saveCharacterProfile();
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
            sort();
            final boolean up = b.pointingUp();
            if (characterProfile == null) { return; }
            //try {
                final int tag = (int) view.getTag();
                characterProfile.setSortReverse(up, tag);

            //} catch (Exception e) {
            //    e.printStackTrace();
            //}
            saveCharacterProfile();
        };
        sortArrow1.setOnClickListener(arrowListener);
        sortArrow2.setOnClickListener(arrowListener);

    }

    private void setupSwipeRefreshLayout() {
        // Set up the 'swipe down to filter' behavior of the RecyclerView
        final SwipeRefreshLayout swipeLayout = findViewById(R.id.swipe_refresh_layout);
        swipeLayout.setOnRefreshListener(() -> {
            filter();
            swipeLayout.setRefreshing(false);
        });

        // Configure the refreshing colors
        swipeLayout.setColorSchemeResources(R.color.darkBrown, R.color.lightBrown, R.color.black);
    }

    private void setupRightNav() {

        // Get the right navigation view and the ExpandableListView
        rightNavView = findViewById(R.id.right_menu);
        rightExpLV = findViewById(R.id.nav_right_expandable);

        // Get the list of group names, as an Array
        // The group names are the headers in the expandable list
        final List<String> rightNavGroups = Arrays.asList(getResources().getStringArray(R.array.right_group_names));

        // Get the names of the group elements, as Arrays
        final ArrayList<String[]> groups = new ArrayList<>();
        groups.add(getResources().getStringArray(R.array.basics_items));
        groups.add(getResources().getStringArray(R.array.casting_spell_items));
        final String[] casterNames = Arrays.copyOf(Spellbook.casterNames, Spellbook.casterNames.length);
        groups.add(casterNames);

        // For each group, get the text that corresponds to each child
        // Here, entries with the same index correspond to one another
        final ArrayList<Integer> basicsIDs = new ArrayList<>(Arrays.asList(R.string.what_is_a_spell, R.string.spell_level,
                R.string.known_and_prepared_spells, R.string.the_schools_of_magic, R.string.spell_slots, R.string.cantrips,
                R.string.rituals, R.string.the_weave_of_magic));
        final ArrayList<Integer> castingSpellIDs = new ArrayList<>(Arrays.asList(R.string.casting_time_info, R.string.range_info, R.string.components_info,
                R.string.duration_info, R.string.targets, R.string.areas_of_effect, R.string.saving_throws,
                R.string.attack_rolls, R.string.combining_magical_effects, R.string.casting_in_armor));
        final ArrayList<Integer> classInfoIDs = new ArrayList<>(Arrays.asList(R.string.bard_spellcasting_info, R.string.cleric_spellcasting_info, R.string.druid_spellcasting_info,
                R.string.paladin_spellcasting_info, R.string.ranger_spellcasting_info, R.string.sorcerer_spellcasting_info, R.string.warlock_spellcasting_info, R.string.wizard_spellcasting_info));
        final List<List<Integer>> childTextLists = new ArrayList<>(Arrays.asList(basicsIDs, castingSpellIDs, classInfoIDs));

        // Create maps of the form group -> list of children, and group -> list of children's text
        final int nGroups = rightNavGroups.size();
        final Map<String, List<String>> childData = new HashMap<>();
        final Map<String, List<Integer>> childTextIDs = new HashMap<>();
        for (int i = 0; i < nGroups; ++i) {
            childData.put(rightNavGroups.get(i), Arrays.asList(groups.get(i)));
            childTextIDs.put(rightNavGroups.get(i), childTextLists.get(i));
        }

        // Create the adapter
        rightAdapter = new NavExpandableListAdapter(this, rightNavGroups, childData, childTextIDs);
        rightExpLV.setAdapter(rightAdapter);
        final View rightHeaderView = getLayoutInflater().inflate(R.layout.right_expander_header, null);
        rightExpLV.addHeaderView(rightHeaderView);

        // Set the callback that displays the appropriate popup when the list item is clicked
        rightExpLV.setOnChildClickListener((ExpandableListView elView, View view, int gp, int cp, long id) -> {
            final NavExpandableListAdapter adapter = (NavExpandableListAdapter) elView.getExpandableListAdapter();
            final String title = (String) adapter.getChild(gp, cp);
            final int textID = adapter.childTextID(gp, cp);

            // Show a popup
            //SpellcastingInfoPopup popup = new SpellcastingInfoPopup(this, title, textID, true);
            //popup.show();

            // Show a full-screen activity
            final Intent intent = new Intent(MainActivity.this, SpellcastingInfoWindow.class);
            intent.putExtra(SpellcastingInfoWindow.TITLE_KEY, title);
            intent.putExtra(SpellcastingInfoWindow.INFO_KEY, textID);
            startActivity(intent);
            overridePendingTransition(R.anim.right_to_left_enter, R.anim.identity);

            return true;
        });
    }

    public static void showKeyboard(EditText mEtSearch, Context context) {
        mEtSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEtSearch, 0);
    }

    public static void hideSoftKeyboard(EditText mEtSearch, Context context) {
        mEtSearch.clearFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEtSearch.getWindowToken(), 0);
    }

    void filter() {
        if (spellAdapter == null) { return; }
        final CharSequence query = (searchView != null) ? searchView.getQuery() : "";
        spellAdapter.getFilter().filter(query);
    }

    private void singleSort() {
        final SortField sf1 = characterProfile.getFirstSortField();
        final boolean reverse1 = sortArrow1.pointingUp();
        spellAdapter.singleSort(sf1, reverse1);
    }

    private void doubleSort() {
        final SortField sf1 = characterProfile.getFirstSortField();
        final SortField sf2 = characterProfile.getSecondSortField();
        final boolean reverse1 = sortArrow1.pointingUp();
        final boolean reverse2 = sortArrow2.pointingUp();
        spellAdapter.doubleSort(sf1, sf2, reverse1, reverse2);
    }

    private void sort() {
        doubleSort();
    }


    JSONArray loadJSONArrayfromAsset(String assetFilename) throws JSONException {
        String jsonStr;
        try {
            final InputStream is = getAssets().open(assetFilename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonStr = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new JSONArray(jsonStr);
    }

    JSONObject loadJSONObjectfromAsset(String assetFilename) throws JSONException {
        String jsonStr;
        try {
            final InputStream is = getAssets().open(assetFilename);
            final int size = is.available();
            final byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonStr = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new JSONObject(jsonStr);
    }

    JSONObject loadJSONfromData(File file) throws JSONException {
        String jsonStr;
        try {
            final FileInputStream is = new FileInputStream(file);
            final int size = is.available();
            final byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonStr = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new JSONObject(jsonStr);
    }

    String loadAssetAsString(File file) {
        try {
            InputStream is = new FileInputStream(file);
            final int size = is.available();
            final byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    JSONObject loadJSONfromData(String dataFilename) throws JSONException {
        return loadJSONfromData(new File(getApplicationContext().getFilesDir(), dataFilename));
    }

    void saveJSON(JSONObject json, File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void saveJSON(JSONArray json, File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(json.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    boolean saveSettings() {
        final File settingsLocation = new File(getApplicationContext().getFilesDir(), settingsFile);
        try {
            System.out.println(settings.toJSON().toString());
        } catch (JSONException e) {
            System.out.println("Error creating settings JSON");
        }
        return settings.save(settingsLocation);
    }

    void loadCharacterProfile(String charName, boolean initialLoad) {

        System.out.println("Loading character: " + charName);

        // We don't need to do anything if the given character is already the current one
        boolean skip = (characterProfile != null) && charName.equals(characterProfile.getName());
        if (!skip) {
            final String charFile = charName + ".json";
            final File profileLocation = new File(profilesDir, charFile);
            try {
                final JSONObject charJSON = loadJSONfromData(profileLocation);
                final CharacterProfile profile = CharacterProfile.fromJSON(charJSON);
                setCharacterProfile(profile, initialLoad);
                System.out.println("characterProfile is " + characterProfile.getName());
            } catch (JSONException e) {
                final String charStr = loadAssetAsString(profileLocation);
                System.out.println("The offending JSON is: " + charStr);
                e.printStackTrace();
            }

        }
    }
    void loadCharacterProfile(String charName) {
        loadCharacterProfile(charName, false);
    }

    void saveCharacterProfile() {
        final String charFile = characterProfile.getName() + ".json";
        final File profileLocation = new File(profilesDir, charFile);

        try {
            characterProfile.save(profileLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void setSideMenuCharacterName() {
        final MenuItem m = navView.getMenu().findItem(R.id.nav_character);
        m.setTitle("Character: " + characterProfile.getName());
    }

    void setFilterSettings() {

        // Set the min and max level entries
        final View levelRangeView = filterCL.findViewById(R.id.level_range_view);
        final EditText minLevelET = levelRangeView.findViewById(R.id.min_range_entry);
        minLevelET.setText(String.valueOf(characterProfile.getMinSpellLevel()));
        final EditText maxLevelET = levelRangeView.findViewById(R.id.max_range_entry);
        maxLevelET.setText(String.valueOf(characterProfile.getMaxSpellLevel()));

        // Set the status filter
        final StatusFilterField sff = characterProfile.getStatusFilter();
        navView.getMenu().getItem(sff.getIndex()).setChecked(true);
    }

    void setSortSettings() {

        // Set the spinners to the appropriate positions
        // We use the adapter data so that we aren't relying on any particular order of the enums populating the adapter
        SortFilterSpinnerAdapter adapter = (SortFilterSpinnerAdapter) sort1.getAdapter();
        List<String> sortData = Arrays.asList(adapter.getData());
        final SortField sf1 = characterProfile.getFirstSortField();
        sort1.setSelection(sortData.indexOf(sf1.getDisplayName()));

        // Set the spinner to the appropriate position
        final SortField sf2 = characterProfile.getSecondSortField();
        sort2.setSelection(sortData.indexOf(sf2.getDisplayName()));

        // Set the sort directions
        final boolean reverse1 = characterProfile.getFirstSortReverse();
        if (reverse1) {
            sortArrow1.setUp();
        } else {
            sortArrow1.setDown();
        }
        final boolean reverse2 = characterProfile.getSecondSortReverse();
        if (reverse2) {
            sortArrow2.setUp();
        } else {
            sortArrow2.setDown();
        }

    }

    void setCharacterProfile(CharacterProfile cp, boolean initialLoad) {
        System.out.println("Setting character profile: " + cp.getName());
        characterProfile = cp;
        settings.setCharacterName(cp.getName());

        setSideMenuCharacterName();
        setSortSettings();
        setFilterSettings();
        saveSettings();
        saveCharacterProfile();
        try {
            if (!initialLoad) {
                //System.out.println("filter from setCharacterProfile");
                sort();
                filter();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // Update the sort/filter bindings we're changing characters
        if (!initialLoad) {
            updateSortFilterBindings();
        }

        // Reset the spell view if on the tablet
        if (onTablet && !initialLoad) {
            spellWindowCL.setVisibility(View.INVISIBLE);
            amBinding.setSpell(null);
            amBinding.setSpellIndex(-1);
            amBinding.executePendingBindings();
        }
    }

    void setCharacterProfile(CharacterProfile cp) {
        setCharacterProfile(cp, false);
    }

    void openCharacterCreationDialog() {
        final CreateCharacterDialog dialog = new CreateCharacterDialog();
        final Bundle args = new Bundle();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "createCharacter");
    }

    void openFeedbackWindow() {
        final FeedbackDialog dialog = new FeedbackDialog();
        final Bundle args = new Bundle();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "feedback");
    }

    void sendFeedback() {
        final Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{devEmail});
        i.putExtra(Intent.EXTRA_SUBJECT, emailMessage);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    boolean deleteCharacterProfile(String name) {
        final String charFile = name + ".json";
        final File profileLocation = new File(profilesDir, charFile);
        final String profileLocationStr = profileLocation.toString();
        final boolean success = profileLocation.delete();

        if (!success) {
            System.out.println("Error deleting character: " + profileLocation);
        } else {
            System.out.println("Successfully deleted the data file for " + name);
            System.out.println("File location was " + profileLocationStr);
        }

        if (success && name.equals(characterProfile.getName())) {
            final ArrayList<String> characters = charactersList();
            if (characters.size() > 0) {
                loadCharacterProfile(characters.get(0));
                saveSettings();
            } else {
                openCharacterCreationDialog();
            }
        }

        return success;
    }

    ArrayList<String> charactersList() {
        final ArrayList<String> charList = new ArrayList<>();
        final int toRemove = CHARACTER_EXTENSION.length();
        for (File file : profilesDir.listFiles()) {
            final String filename = file.getName();
            if (filename.endsWith(CHARACTER_EXTENSION)) {
                final String charName = filename.substring(0, filename.length() - toRemove);
                charList.add(charName);
                System.out.println(charName);
            }
        }
        charList.sort(String::compareToIgnoreCase);
        return charList;
    }

    void openCharacterSelection() {
        System.out.println("Opening creation dialog");
        final CharacterSelectionDialog dialog = new CharacterSelectionDialog();
        final Bundle args = new Bundle();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "selectCharacter");
    }

    void filterIfStatusSet() {
        if (characterProfile.isStatusSet()) {
            System.out.println("filter from filterIfStatusSet");
            filter();
        }
    }


    File getProfilesDir() { return profilesDir; }
    CharacterProfile getCharacterProfile() { return characterProfile; }
    Settings getSettings() { return settings; }
    CharacterSelectionDialog getSelectionDialog() { return selectionDialog; }
    View getCharacterSelect() { return characterSelect; }
    void setCharacterSelect(View v) { characterSelect = v;}
    void setSelectionDialog(CharacterSelectionDialog d) { selectionDialog = d; }
    boolean usingTablet() { return onTablet; }


    private void tabletSetup() {

        // Spell window background
        spellWindowCL = findViewById(R.id.spell_window_constraint);
        spellWindowCL.setBackground(null);
        spellWindowCL.setVisibility(View.INVISIBLE);

        // Set button callbacks
        final ToggleButton favoriteButton = spellWindowCL.findViewById(R.id.favorite_button);
        favoriteButton.setCallback(() -> {
            characterProfile.toggleFavorite(amBinding.getSpell());
            spellAdapter.notifyItemChanged(amBinding.getSpellIndex());
            saveCharacterProfile();
        });
        final ToggleButton knownButton = spellWindowCL.findViewById(R.id.known_button);
        knownButton.setCallback(() -> {
            characterProfile.toggleKnown(amBinding.getSpell());
            spellAdapter.notifyItemChanged(amBinding.getSpellIndex());
            saveCharacterProfile();
        });
        final ToggleButton preparedButton = spellWindowCL.findViewById(R.id.prepared_button);
        preparedButton.setCallback(() -> {
            characterProfile.togglePrepared(amBinding.getSpell());
            spellAdapter.notifyItemChanged(amBinding.getSpellIndex());
            saveCharacterProfile();
        });
    }

    void updateSpellWindow(Spell s, boolean favorite, boolean prepared, boolean known) {
        if (onTablet && (spellWindowCL.getVisibility() == View.VISIBLE) && (amBinding != null) && (s.equals(amBinding.getSpell())) ) {
            final ToggleButton favoriteButton = spellWindowCL.findViewById(R.id.favorite_button);
            favoriteButton.set(favorite);
            final ToggleButton preparedButton = spellWindowCL.findViewById(R.id.prepared_button);
            preparedButton.set(prepared);
            final ToggleButton knownButton = spellWindowCL.findViewById(R.id.known_button);
            knownButton.set(known);
        }
    }



    // The code for populating the filters is all essentially the same
    // So we can just use this generic function to remove redundancy
    private <E extends Enum<E> & NameDisplayable> ArrayList<ItemFilterViewBinding> populateFilters(int filterBlockID, Class<E> enumType) {

        // Are we working with an enum type or not
        final boolean isQuantityType = enumType.isAssignableFrom(QuantityType.class);

        // Get the GridLayout and the appropriate column weight
        final View filterBlockRangeView = filterCL.findViewById(filterBlockID);
        final View filterBlockView = isQuantityType ? filterBlockRangeView.findViewById(R.id.filter_block) : filterBlockRangeView;
        final GridLayout gridLayout = filterBlockView.findViewById(R.id.filter_grid_layout);

        // An empty list of bindings. We'll populate this and return it
        final ArrayList<ItemFilterViewBinding> bindings = new ArrayList<>();

        // Get an array of instances of the Enum type
        final E[] enums = enumType.getEnumConstants();

        // If this isn't an enum type, return our (currently empty) list
        // This should never happens
        if (enums == null) { return bindings; }

        // Populate the list of bindings, one for each instance of the given Enum type
        for (E e : enums) {

            // Create the layout parameters
            //final GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f),  GridLayout.spec(GridLayout.UNDEFINED, 1f));

            // Inflate the binding
            final ItemFilterViewBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.item_filter_view, null, false);

            // Bind the relevant values
            binding.setProfile(characterProfile);
            binding.setItem(e);
            binding.executePendingBindings();

            // If the enum is a spanning type, we want to add this to the spanning types binding list
            // We also want to do the appropriate bindings here
            boolean spanning = false;
            if (e instanceof QuantityType) {
                QuantityType qt = (QuantityType) e;
                if (qt.isSpanningType()) {
                    spanningTypeFilterMap.put(qt, binding);
                    spanning = true;
                }
            }

            // Get the root view
            final View view = binding.getRoot();

            // Set up the button

            final ToggleButton button = view.findViewById(R.id.item_filter_button);
            button.setTag(e);
            Consumer<ToggleButton> toggleButtonConsumer;
            Consumer<ToggleButton> defaultConsumer = (v) -> { characterProfile.toggleVisibility((E) v.getTag()); saveCharacterProfile(); };

            // If this is a spanning type, we want to also set the button to toggle the corresponding range view's visibility
            // as well as do some other stuff
            if (spanning) {

                // Get the range view
                final Quintet<Class<? extends Quantity>, Integer,Integer,Integer,Integer> info = rangeViewInfo.get(e);
                final View rangeView = filterBlockRangeView.findViewById(R.id.range_filter);

                // Set up the range view
                setupRangeView(rangeView, info);

                toggleButtonConsumer = (v) -> {
                    defaultConsumer.accept(v);
                    rangeView.setVisibility(rangeView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
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

    // Populating the various types of bindings using our generic function above
    private ArrayList<ItemFilterViewBinding> populateSourcebookFilters() { return populateFilters(R.id.sourcebook_filter_block, Sourcebook.class); }
    private ArrayList<ItemFilterViewBinding> populateCasterFilters() { return populateFilters(R.id.caster_filter_block, CasterClass.class); }
    private ArrayList<ItemFilterViewBinding> populateSchoolFilters() { return populateFilters(R.id.school_filter_block, School.class); }
    private ArrayList<ItemFilterViewBinding> populateCastingTimeFilters() { return populateFilters(R.id.casting_time_filter_range, CastingTime.CastingTimeType.class); }
    private ArrayList<ItemFilterViewBinding> populateDurationFilters() { return populateFilters(R.id.duration_filter_range, Duration.DurationType.class); }
    private ArrayList<ItemFilterViewBinding> populateRangeFilters() { return populateFilters(R.id.range_filter_range, Range.RangeType.class); }

    // Updating the character profile is another operation that is essentially identical for each binding type
    // So we can again use a generic function
    private void updateBindings(ArrayList<ItemFilterViewBinding> bindings) {
        for (ItemFilterViewBinding binding : bindings) {
            binding.setProfile(characterProfile);
            binding.executePendingBindings();
        }
    }

    // Call this function for each of our ArrayLists of bindings
    private void updateSortFilterBindings() {
        final ArrayList<ArrayList<ItemFilterViewBinding>> bindingLists = new ArrayList<>(Arrays.asList(sourcebookFilterViewBindings,
                casterFilterViewBindings,
                schoolFilterViewBindings,
                castingTimeFilterViewBindings,
                durationTypeFilterViewBindings,
                rangeTypeFilterViewBindings,
                new ArrayList<>(spanningTypeFilterMap.values())
        ));
        for (ArrayList<ItemFilterViewBinding> bindings : bindingLists) { updateBindings(bindings); }
    }


    private void setupFilterBlocks() {

        for (HashMap.Entry<Class<?>, Quartet<Boolean,Integer,Integer,Integer>> entry : filterBlockInfo.entrySet()) {
            Quartet<Boolean,Integer,Integer,Integer> data = entry.getValue();
            final boolean isQuantityType = data.getValue0();
            final int blockID = data.getValue1();
            final View blockRangeView = filterCL.findViewById(blockID);
            final View blockView = isQuantityType ? blockRangeView.findViewById(R.id.filter_block) : blockRangeView;
            final SortFilterHeaderView blockTitle = blockView.findViewById(R.id.filter_header);
            final GridLayout blockGrid = blockView.findViewById(R.id.filter_grid_layout);
            final String title = getResources().getString(data.getValue2());
            final int columns = getResources().getInteger(data.getValue3());
            blockTitle.setTitle(title);
            blockGrid.setColumnCount(columns);
        }
    }

    private <E extends NameDisplayable> void setupRangeView(View rangeView, Quintet<Class<? extends Quantity>, Integer,Integer,Integer,Integer> info) {
        // Get the range view and set its info appropriately

        final EditText minET = rangeView.findViewById(R.id.min_range_entry);
        minET.setText(getResources().getString(info.getValue1()));
        final EditText maxET = rangeView.findViewById(R.id.max_range_entry);


        // Set the initial visibility
        rangeView.setVisibility(characterProfile.getVisibility(e) ? View.VISIBLE : View.GONE);

    }

    private void setupExpandingViews() {
        // Expanding views that don't come from an Enum
        for (HashMap.Entry<Integer,Integer> pair : expandingIDs.entrySet()) {
            final SortFilterHeaderView headerView = filterCL.findViewById(pair.getKey());
            final View expandableView = filterCL.findViewById(pair.getValue());
            final Runnable runnable = () -> headerView.getButton().toggle();
            ViewAnimations.setExpandableHeader(this, headerView, expandableView, runnable);
        }

        // Expanding views for the enum filters
        for (Quartet<Boolean,Integer,Integer,Integer> value : filterBlockInfo.values()) {
            final boolean isQuantityType = value.getValue0();
            final View blockRangeView = findViewById(value.getValue1());
            final View blockView = isQuantityType ? blockRangeView.findViewById(R.id.filter_block) : blockRangeView;
            final SortFilterHeaderView blockHeader = blockView.findViewById(R.id.filter_header);
            final View blockScroll = blockView.findViewById(R.id.filter_scroll);
            final Runnable runnable = () -> blockHeader.getButton().toggle();
            ViewAnimations.setExpandableHeader(this, blockHeader, blockScroll, runnable);
        }
    }

    private void setupSortFilterView() {

        // Set up the filter block bindings
        setupFilterBlocks();

        // Populate the filter bindings
        sourcebookFilterViewBindings = populateSourcebookFilters();
        casterFilterViewBindings = populateCasterFilters();
        schoolFilterViewBindings = populateSchoolFilters();
        castingTimeFilterViewBindings = populateCastingTimeFilters();
        durationTypeFilterViewBindings = populateDurationFilters();
        rangeTypeFilterViewBindings = populateRangeFilters();

        // Set headers and expanding views
        setupExpandingViews();

        // Set the range info
        // Spell level range
        final View levelRangeView = filterCL.findViewById(R.id.level_range_view);
        final EditText minLevelET = levelRangeView.findViewById(R.id.min_range_entry);
        final int minLevel = characterProfile!= null ? characterProfile.getMinSpellLevel() : Spellbook.MIN_SPELL_LEVEL;
        minLevelET.setText(String.format(Locale.US, "%d", minLevel));
        minLevelET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                final String text = editable.toString();
                int level = 0;
                try {
                    level = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                characterProfile.setMinSpellLevel(level);
            }
        });

        final EditText maxLevelET = levelRangeView.findViewById(R.id.max_range_entry);
        final int maxLevel = characterProfile!= null ? characterProfile.getMaxSpellLevel() : Spellbook.MAX_SPELL_LEVEL;
        maxLevelET.setText(String.format(Locale.US, "%d", maxLevel));
        maxLevelET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                final String text = editable.toString();
                int level = 9;
                try {
                    level = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                characterProfile.setMaxSpellLevel(level);
            }
        });
    }

    private void updateWindowVisibilities() {

        // Sort and filter, if necessary
        if (!onTablet && !filterVisible) {
            sort();
            filter();
        }

        // The current window visibilities
        final int spellVisibility = filterVisible ? View.GONE : View.VISIBLE;
        final int filterVisibility = filterVisible ? View.VISIBLE : View.GONE;

        // Update window visibilities appropriately
        final View spellView = onTablet ? spellWindowCL : spellsCL;
        spellView.setVisibility(spellVisibility);
        filterSV.setVisibility(filterVisibility);

        // Collapse the SearchView if it's open, and set the search icon visibility appropriately
        if (filterVisible && !searchView.isIconified()) {
            searchViewIcon.collapseActionView();
        }
        searchViewIcon.setVisible(spellVisibility == View.VISIBLE);

        // Update the filter icon on the action bar
        // If the filters are open, we show a list or data icon (depending on the platform) instead ("return to the data")
        final int filterIcon = onTablet ? R.drawable.ic_data : R.drawable.ic_list;
        final int icon = filterVisible ? filterIcon : R.drawable.ic_filter;
        filterMenuIcon.setIcon(icon);

        // Save the character profile
        saveCharacterProfile();
    }

    private void toggleWindowVisibilities() {
        filterVisible = !filterVisible;
        updateWindowVisibilities();
    }

}
