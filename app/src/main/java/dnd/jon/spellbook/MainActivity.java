package dnd.jon.spellbook;

import android.app.SearchManager;
import android.content.Context;

import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.databinding.ViewDataBinding;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Map;
import java.util.function.BiConsumer;

import dnd.jon.spellbook.databinding.ActivityMainBinding;
import dnd.jon.spellbook.databinding.CasterFilterViewBinding;
import dnd.jon.spellbook.databinding.SchoolFilterViewBinding;
import dnd.jon.spellbook.databinding.SourcebookFilterViewBinding;

public class MainActivity extends AppCompatActivity {

    private final String spellsFilename = "Spells.json";
    private final String settingsFile = "Settings.json";
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private NavigationView rightNavView;
    private ExpandableListView rightExpLV;
    private ExpandableListAdapter rightAdapter;
    private SearchView searchView;
    private MenuItem filterMenuButton;
    private ConstraintLayout spellsCL;
    private ConstraintLayout filterCL;
    private ScrollView filterSV;

    private final String profilesDirName = "Characters";
    private CharacterProfile characterProfile;
    private View characterSelect = null;
    private CharacterSelectionDialog selectionDialog = null;
    private File profilesDir;
    private Settings settings;

    // For filtering stuff
    private boolean filterVisible = false;
    private ArrayList<SourcebookFilterViewBinding> sourcebookFilterViewBindings;
    private ArrayList<CasterFilterViewBinding> casterFilterViewBindings;
    private ArrayList<SchoolFilterViewBinding> schoolFilterViewBindings;


    private static final String spellBundleKey = "SPELL";
    private static final String spellIndexBundleKey = "SPELL_INDEX";

    private static final String devEmail = "dndspellbookapp@gmail.com";
    private static final String emailMessage = "[Android] Feedback";

    // The map ID -> Sourcebook relating left nav bar items to sourcebooks, for sourcebook filtering
    private static HashMap<Integer, Sourcebook> subNavIds = new HashMap<Integer, Sourcebook>() {{
        put(R.id.subnav_phb, Sourcebook.PLAYERS_HANDBOOK);
        put(R.id.subnav_xge, Sourcebook.XANATHARS_GTE);
        put(R.id.subnav_scag, Sourcebook.SWORD_COAST_AG);
    }};

    // The map ID -> StatusFilterField relating left nav bar items to the corresponding spell status filter
    private static HashMap<Integer,StatusFilterField> statusFilterIDs = new HashMap<Integer,StatusFilterField>() {{
       put(R.id.nav_all, StatusFilterField.ALL);
       put(R.id.nav_favorites, StatusFilterField.FAVORITES);
       put(R.id.nav_prepared, StatusFilterField.PREPARED);
       put(R.id.nav_known, StatusFilterField.KNOWN);
    }};

    // The UI elements for sorting and searching
    private Spinner sort1;
    private Spinner sort2;
    private SortDirectionButton sortArrow1;
    private SortDirectionButton sortArrow2;

    // The spinner adapters
    private SortFilterSpinnerAdapter sortAdapter1;
    private SortFilterSpinnerAdapter sortAdapter2;

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

        // Are we on a tablet or not?
        onTablet = getResources().getBoolean(R.bool.isTablet);
        if (onTablet) { tabletSetup(); }

        // Get the main views
        spellsCL = findViewById(R.id.main_constraint_layout);
        filterCL = findViewById(R.id.sort_filter_window);
        filterSV = findViewById(R.id.sort_filter_scroll);

        // Re-set the current spell after a rotation (only needed on tablet
        if (onTablet && savedInstanceState != null) {
            Spell spell = savedInstanceState.containsKey(spellBundleKey) ? savedInstanceState.getParcelable(spellBundleKey) : null;
            int spellIndex = savedInstanceState.containsKey(spellIndexBundleKey) ? savedInstanceState.getInt(spellIndexBundleKey) : -1;
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // The DrawerLayout and the left navigation view
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.side_menu);
        NavigationView.OnNavigationItemSelectedListener navViewListener = new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int index = menuItem.getItemId();
                boolean close = false;
                if (subNavIds.containsKey(index)) {
                    Sourcebook source = subNavIds.get(index);
                    setSourcebookFilter(source, !characterProfile.getSourcebookFilter(source));
                    saveCharacterProfile();
                } else if (index == R.id.subnav_charselect) {
                    openCharacterSelection();
                } else if (index == R.id.nav_feedback) {
                    sendFeedback();
                } else if (statusFilterIDs.containsKey(index)) {
                    StatusFilterField sff = statusFilterIDs.get(index);
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
        ActionBarDrawerToggle leftNavToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.left_navigation_drawer_open, R.string.left_navigation_drawer_closed);
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
            boolean success = profilesDir.mkdir();
            if (!success) {
                System.out.println("Error creating profiles directory"); // Add something real here eventually
            }
        }

        // Load the spell data
        ArrayList<Spell> spells = new ArrayList<>();
        try {
            JSONArray jsonArray = loadJSONArrayfromAsset(spellsFilename);
            spells = SpellCodec.parseSpellList(jsonArray);
        } catch (Exception e) {
            e.printStackTrace();
            this.finish();
        }

        // Load the settings and the character profile
        try {

            // Load the settings
            JSONObject json = loadJSONfromData(settingsFile);
            System.out.println(json.toString());
            settings = new Settings(json);

            // Load the character profile
            String charName = settings.characterName();
            loadCharacterProfile(charName, true);

            // Set the sourcebook filters for this character
            for (Sourcebook sb : Sourcebook.values()) {
                setSourcebookFilter(sb, characterProfile.getSourcebookFilter(sb));
            }
            // Set the character's name in the side menu
            setSideMenuCharacterName();

        } catch (Exception e) {
            String s = loadAssetAsString(new File(settingsFile));
            System.out.println("Error loading settings");
            System.out.println("The settings file content is: " + s);
            settings = new Settings();
            if (charactersList().size() > 0) {
                String firstCharacter = charactersList().get(0);
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

        // Set up the 'swipe down to filter' behavior of the RecyclerView
        SwipeRefreshLayout swipeLayout = findViewById(R.id.swipe_refresh_layout);
        swipeLayout.setOnRefreshListener(() -> {
            filter();
            swipeLayout.setRefreshing(false);
        });

        // Configure the refreshing colors
        swipeLayout.setColorSchemeResources(R.color.darkBrown, R.color.lightBrown, R.color.black);

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
        if (!filterVisible) {
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
        filterMenuButton = menu.findItem(R.id.action_filter);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
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
        sort();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
//        System.out.println("In onDestroy");
//        System.out.println("The current character is " + characterProfile.getName());
//        try {
//            System.out.println("The JSON is:");
//            System.out.println(characterProfile.toJSON().toString());
//        } catch (JSONException e) {
//            System.out.println("Error converting character profile to JSON");
//        }
//        saveCharacterProfile();
//        saveSettings();
        super.onDestroy();
    }

    // Necessary for handling rotations
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (amBinding != null && amBinding.getSpell() != null) {
            outState.putParcelable(spellBundleKey, amBinding.getSpell());
            outState.putInt(spellIndexBundleKey, amBinding.getSpellIndex());
            ToggleButton favoriteButton = spellWindowCL.findViewById(R.id.favorite_button);
            ToggleButton preparedButton = spellWindowCL.findViewById(R.id.prepared_button);
            ToggleButton knownButton = spellWindowCL.findViewById(R.id.known_button);
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

        // Set necessary tags
        sort1.setTag(1);
        sort2.setTag(2);
        sortArrow1.setTag(1);
        sortArrow2.setTag(2);

        //The list of sort fields
        // Since these are going to be affected by the DefaultTextSpinnerAdapter, we want two unique instances
        String[] sort1Objects = Arrays.copyOf(Spellbook.sortFieldNames, Spellbook.sortFieldNames.length);
        String[] sort2Objects = Arrays.copyOf(Spellbook.sortFieldNames, Spellbook.sortFieldNames.length);

        // Populate the dropdown spinners
        sortAdapter1 = new SortFilterSpinnerAdapter(this, sort1Objects);
        sort1.setAdapter(sortAdapter1);

        sortAdapter2 = new SortFilterSpinnerAdapter(this, sort2Objects);
        sort2.setAdapter(sortAdapter2);


        // Set what happens when the sort spinners are changed
        AdapterView.OnItemSelectedListener sortListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //System.out.println("Calling sort");
                sort();
                if (characterProfile == null) { return; }
                String itemName = (String) adapterView.getItemAtPosition(i);
                //try {
                    int tag = (int) adapterView.getTag();
                    SortField sf = Util.coalesce(SortField.fromDisplayName(itemName), SortField.NAME);
                    switch (tag) {
                        case 1:
                            characterProfile.setFirstSortField(sf);
                            break;
                        case 2:
                            characterProfile.setSecondSortField(sf);
                    }
                //} catch (Exception e) {
                //    e.printStackTrace();
                //}
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
            SortDirectionButton b = (SortDirectionButton) view;
            b.onPress();
            sort();
            final boolean up = b.pointingUp();
            if (characterProfile == null) { return; }
            //try {
                final int tag = (int) view.getTag();
                switch (tag) {
                    case 1:
                        characterProfile.setFirstSortReverse(up);
                        break;
                    case 2:
                        characterProfile.setSecondSortReverse(up);
                }

            //} catch (Exception e) {
            //    e.printStackTrace();
            //}
            saveCharacterProfile();
        };
        sortArrow1.setOnClickListener(arrowListener);
        sortArrow2.setOnClickListener(arrowListener);

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
        final String[] casterNames = new String[CasterClass.values().length];
        for (CasterClass cc : CasterClass.values()) {
            casterNames[cc.ordinal()] = cc.getDisplayName();
        }
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
        int nGroups = rightNavGroups.size();
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
            NavExpandableListAdapter adapter = (NavExpandableListAdapter) elView.getExpandableListAdapter();
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

    private int resIDfromBoolean(boolean TF, int idT, int idF) { return TF ? idT : idF; }
    int starIcon(boolean TF) { return resIDfromBoolean(TF, R.drawable.star_filled, R.drawable.star_empty); }

    void setStarIcon(Sourcebook sb, boolean tf) {
        for (HashMap.Entry<Integer, Sourcebook> pair : subNavIds.entrySet()) {
            if (pair.getValue() == sb) {
                MenuItem m = findViewById(pair.getKey());
                m.setIcon(starIcon(tf));
                break;
            }
        }
    }

    void setSourcebookFilter(Sourcebook book, boolean tf) {
        characterProfile.setSourcebookFilter(book, tf);
        final MenuItem m = navView.getMenu().findItem(navIDfromSourcebook(book));
        m.setIcon(starIcon(tf));
    }

    void setSourcebookFilters() {
        for (Sourcebook book : Sourcebook.values()) {
            final boolean b = characterProfile.getSourcebookFilter(book);
            setSourcebookFilter(book, b);
        }
    }

    void filter() {
        if (spellAdapter == null) { return; }
        final CharSequence query = (searchView != null) ? searchView.getQuery() : "";
        spellAdapter.getFilter().filter(query);
    }

    private void singleSort() {
        boolean reverse1 = sortArrow1.pointingUp();
        spellAdapter.singleSort(sortField1(), reverse1);
    }

    private void doubleSort() {
        boolean reverse1 = sortArrow1.pointingUp();
        boolean reverse2 = sortArrow2.pointingUp();
        spellAdapter.doubleSort(sortField1(), sortField2(), reverse1, reverse2);
    }

    private void sort() {
        doubleSort();
    }

    int navIDfromSourcebook(Sourcebook sb) {
        for (HashMap.Entry<Integer, Sourcebook> pair : subNavIds.entrySet()) {
            if (pair.getValue() == sb) {
                return pair.getKey();
            }
        }
        return -1;
    }


    JSONArray loadJSONArrayfromAsset(String assetFilename) throws JSONException {
        String jsonStr;
        try {
            InputStream is = getAssets().open(assetFilename);
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
            InputStream is = getAssets().open(assetFilename);
            int size = is.available();
            byte[] buffer = new byte[size];
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
            InputStream is = new FileInputStream(file);
            int size = is.available();
            byte[] buffer = new byte[size];
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
            int size = is.available();
            byte[] buffer = new byte[size];
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
        File settingsLocation = new File(getApplicationContext().getFilesDir(), settingsFile);
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
            String charFile = charName + ".json";
            File profileLocation = new File(profilesDir, charFile);
            try {
                JSONObject charJSON = loadJSONfromData(profileLocation);
                CharacterProfile profile = CharacterProfile.fromJSON(charJSON);
                setCharacterProfile(profile, initialLoad);
                System.out.println("characterProfile is " + characterProfile.getName());
            } catch (JSONException e) {
                String charStr = loadAssetAsString(profileLocation);
                System.out.println("The offending JSON is: " + charStr);
                e.printStackTrace();
            }

        }
    }
    void loadCharacterProfile(String charName) {
        loadCharacterProfile(charName, false);
    }

    void saveCharacterProfile() {
        String charFile = characterProfile.getName() + ".json";
        File profileLocation = new File(profilesDir, charFile);

        try {
            characterProfile.save(profileLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void setSideMenuCharacterName() {
        MenuItem m = navView.getMenu().findItem(R.id.nav_character);
        m.setTitle("Character: " + characterProfile.getName());
    }

    void setFilterSettings() {

        // Set the sourcebook filters
        setSourcebookFilters();

        // Set the status filter
        StatusFilterField sff = characterProfile.getStatusFilter();
        navView.getMenu().getItem(sff.getIndex()).setChecked(true);
    }

    void setSortSettings() {

        // Set the sort fields
        // The first sort spinner

        // Set the spinner to the appropriate position
        SortField sf1 = characterProfile.getFirstSortField();
        sort1.setSelection(sf1.getIndex());

        // Set the spinner to the appropriate position
        SortField sf2 = characterProfile.getSecondSortField();
        sort2.setSelection(sf2.getIndex());

        // Set the sort directions
        boolean reverse1 = characterProfile.getFirstSortReverse();
        if (reverse1) {
            sortArrow1.setUp();
        } else {
            sortArrow1.setDown();
        }
        boolean reverse2 = characterProfile.getSecondSortReverse();
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
        updateSortFilterBindings();
        saveSettings();
        saveCharacterProfile();
        try {
            if (!initialLoad) {
                //System.out.println("filter from setCharacterProfile");
                filter();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
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
        CreateCharacterDialog dialog = new CreateCharacterDialog();
        Bundle args = new Bundle();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "createCharacter");
    }

    void openFeedbackWindow() {
        FeedbackDialog dialog = new FeedbackDialog();
        Bundle args = new Bundle();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "feedback");
    }

    void sendFeedback() {
        Intent i = new Intent(Intent.ACTION_SEND);
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
        String charFile = name + ".json";
        File profileLocation = new File(profilesDir, charFile);
        String profileLocationStr = profileLocation.toString();
        boolean success = profileLocation.delete();

        if (!success) {
            System.out.println("Error deleting character: " + profileLocation);
        } else {
            System.out.println("Successfully deleted the data file for " + name);
            System.out.println("File location was " + profileLocationStr);
        }

        if (success && name.equals(characterProfile.getName())) {
            ArrayList<String> characters = charactersList();
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
        ArrayList<String> charList = new ArrayList<>();
        int toRemove = CHARACTER_EXTENSION.length();
        System.out.println("The list of characters is:");
        for (File file : profilesDir.listFiles()) {
            String filename = file.getName();
            if (filename.endsWith(CHARACTER_EXTENSION)) {
                String charName = filename.substring(0, filename.length() - toRemove);
                charList.add(charName);
                System.out.println(charName);
            }
        }
        charList.sort(String::compareToIgnoreCase);
        return charList;
    }

    void openCharacterSelection() {
        System.out.println("Opening creation dialog");
        CharacterSelectionDialog dialog = new CharacterSelectionDialog();
        Bundle args = new Bundle();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "selectCharacter");
    }

    void filterIfStatusSet() {
        if (characterProfile.isStatusSet()) {
            System.out.println("filter from filterIfStatusSet");
            filter();
        }
    }


    SortField sortField1() {
        SortField sf = SortField.fromDisplayName(sort1.getSelectedItem().toString());
        return (sf != null) ? sf : SortField.NAME;
    }

    SortField sortField2() {
        SortField sf = SortField.fromDisplayName(sort2.getSelectedItem().toString());
        return (sf != null) ? sf : SortField.NAME;
    }

//    private boolean needDoubleSort() {
//        SortField sf1 = sortField1();
//        SortField sf2 = sortField2();
//        return !( (sf2 == SortField.Name && !reverse2) || (sf1 == sf2) );
//    }

    File getProfilesDir() { return profilesDir; }
    CharacterProfile getCharacterProfile() { return characterProfile; }
    Settings getSettings() { return settings; }
    CharacterSelectionDialog getSelectionDialog() { return selectionDialog; }
    View getCharacterSelect() { return characterSelect; }
    void setCharacterSelect(View v) { characterSelect = v;}
    void setSelectionDialog(CharacterSelectionDialog d) { selectionDialog = d; }
    boolean usingTablet() { return onTablet; }


    private void tabletSetup() {

        System.out.println("Tablet setup");

        // For tablet data binding
        amBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Spell window background
        spellWindowCL = findViewById(R.id.spell_window_constraint);
        spellWindowCL.setBackground(null);
        spellWindowCL.setVisibility(View.INVISIBLE);

        // Set button callbacks
        ToggleButton favoriteButton = spellWindowCL.findViewById(R.id.favorite_button);
        favoriteButton.setCallback(() -> {
            characterProfile.toggleFavorite(amBinding.getSpell());
            spellAdapter.notifyItemChanged(amBinding.getSpellIndex());
            saveCharacterProfile();
        });
        ToggleButton knownButton = spellWindowCL.findViewById(R.id.known_button);
        knownButton.setCallback(() -> {
            characterProfile.toggleKnown(amBinding.getSpell());
            spellAdapter.notifyItemChanged(amBinding.getSpellIndex());
            saveCharacterProfile();
        });
        ToggleButton preparedButton = spellWindowCL.findViewById(R.id.prepared_button);
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
    private <E extends Enum<E>, BindingType extends ViewDataBinding> ArrayList<BindingType> populateFilters(View rootView, int gridLayoutID, int layoutID, int buttonID, Class<E> enumType, Class<BindingType> dataBindingType, BiConsumer<BindingType,CharacterProfile> profileBinder, BiConsumer<BindingType,E> enumBinder, BiConsumer<CharacterProfile, E> toggleMethod) {

        // Get the GridLayout and the appropriate column weight
        final GridLayout gridLayout = rootView.findViewById(gridLayoutID);
        final float columnWeight = 1f / gridLayout.getColumnCount();

        // Get an array of instances of the Enum type
        final E[] enums = enumType.getEnumConstants();

        // If this isn't an enum type, return an empty list
        // This should never happens
        if (enums == null) { return new ArrayList<>(); }

        // Create the list of bindings, one for each instance of the given Enum type
        ArrayList<BindingType> bindings = new ArrayList<>();
        for (E e : enums) {

            // Create the layout parameters
            final GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, columnWeight),  GridLayout.spec(GridLayout.UNDEFINED, 1f));

            // Inflate the binding
            final BindingType binding = DataBindingUtil.inflate(getLayoutInflater(), layoutID, null, false);

            // Bind the relevant values
            profileBinder.accept(binding, characterProfile);
            enumBinder.accept(binding, e);
            binding.executePendingBindings();

            // Get the root view
            final View view = binding.getRoot();
            final ToggleButton button = view.findViewById(buttonID);
            button.setTag(e);
            button.setCallback( (v) -> toggleMethod.accept(characterProfile, (E) v.getTag()) );
            gridLayout.addView(view, params);
            bindings.add(binding);
        }
        return bindings;
    }

    private ArrayList<SourcebookFilterViewBinding> populateSourcebookFilters() { return populateFilters(filterCL, R.id.sourcebook_filter_grid_layout, R.layout.sourcebook_filter_view, R.id.sourcebook_button, Sourcebook.class, SourcebookFilterViewBinding.class, SourcebookFilterViewBinding::setProfile, SourcebookFilterViewBinding::setSourcebook, CharacterProfile::toggleSourcebookVisibility); }
    private ArrayList<CasterFilterViewBinding> populateCasterFilters() { return populateFilters(filterCL, R.id.caster_filter_grid_layout, R.layout.caster_filter_view, R.id.caster_button, CasterClass.class, CasterFilterViewBinding.class, CasterFilterViewBinding::setProfile, CasterFilterViewBinding::setCasterClass, CharacterProfile::toggleCasterVisibility); }
    private ArrayList<SchoolFilterViewBinding> populateSchoolFilters() { return populateFilters(filterCL, R.id.school_filter_grid_layout, R.layout.school_filter_view, R.id.school_button, School.class, SchoolFilterViewBinding.class, SchoolFilterViewBinding::setProfile, SchoolFilterViewBinding::setSchool, CharacterProfile::toggleSchoolVisibility); }


    // Updating the character profile is another operation that is essentially identical for each binding type
    // So we can again use a generic function
    private <BindingType extends ViewDataBinding> void updateBindings(ArrayList<BindingType> bindings, BiConsumer<BindingType,CharacterProfile> characterBinder) {
        for (BindingType binding : bindings) {
            characterBinder.accept(binding, characterProfile);
            binding.executePendingBindings();
        }
    }

    // Call this generic function for each of our ArrayLists of bindings
    private void updateSortFilterBindings() {
        updateBindings(sourcebookFilterViewBindings, SourcebookFilterViewBinding::setProfile);
        updateBindings(casterFilterViewBindings, CasterFilterViewBinding::setProfile);
        updateBindings(schoolFilterViewBindings, SchoolFilterViewBinding::setProfile);
    }

    private void setupSortFilterView() {
        sourcebookFilterViewBindings = populateSourcebookFilters();
        casterFilterViewBindings = populateCasterFilters();
        schoolFilterViewBindings = populateSchoolFilters();
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
        searchView.setVisibility(spellVisibility);

        // Update the action bar icon
        final int filterIcon = onTablet ? R.drawable.ic_data : R.drawable.ic_list;
        final int icon = filterVisible ? filterIcon : R.drawable.ic_filter;
        filterMenuButton.setIcon(icon);
    }

    private void toggleWindowVisibilities() {
        filterVisible = !filterVisible;
        updateWindowVisibilities();
    }

}
