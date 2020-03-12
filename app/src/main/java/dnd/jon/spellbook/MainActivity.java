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

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
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

import dnd.jon.spellbook.databinding.ActivityMainBinding;
import dnd.jon.spellbook.databinding.SortFilterLayoutBinding;
import dnd.jon.spellbook.databinding.SpellWindowBinding;

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

    private static final String profilesDirName = "Characters";
    private CharacterProfile characterProfile;
    private View characterSelect = null;
    private CharacterSelectionDialog selectionDialog = null;
    private File profilesDir;
    private Settings settings;

    // For the sort/filter window
    private boolean filterVisible = false;
    private SortFilterLayoutBinding sortFilterBinding;
    private SortFilterExpandableAdapter sortFilterAdapter;

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
    private SpellWindowBinding spellWindowBinding = null;
    private ConstraintLayout spellWindowCL = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the main binding and content view
        amBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(amBinding.getRoot());

        // Are we on a tablet or not?
        // If we're on a tablet, do the necessary setup
        onTablet = getResources().getBoolean(R.bool.isTablet);
        if (onTablet) { tabletSetup(); }

        // Get the main views
        spellsCL = amBinding.mainConstraintLayout;
        sortFilterBinding = amBinding.sortFilterView;

        // Re-set the current spell after a rotation (only needed on tablet)
        if (onTablet && savedInstanceState != null) {
            final Spell spell = savedInstanceState.containsKey(spellBundleKey) ? savedInstanceState.getParcelable(spellBundleKey) : null;
            final int spellIndex = savedInstanceState.containsKey(spellIndexBundleKey) ? savedInstanceState.getInt(spellIndexBundleKey) : -1;
            if (spell != null) {
                spellWindowBinding.setSpell(spell);
                spellWindowBinding.setSpellIndex(spellIndex);
                spellWindowBinding.executePendingBindings();
                spellWindowCL.setVisibility(View.VISIBLE);
                if (savedInstanceState.containsKey(FAVORITE_KEY) && savedInstanceState.containsKey(PREPARED_KEY) && savedInstanceState.containsKey(KNOWN_KEY)) {
                    updateSpellWindow(spell, savedInstanceState.getBoolean(FAVORITE_KEY), savedInstanceState.getBoolean(PREPARED_KEY), savedInstanceState.getBoolean(KNOWN_KEY));
                }
            }
        }

        // Any other stuff that needs to be updated after a rotation
        if (savedInstanceState != null) {
            filterVisible = savedInstanceState.containsKey(FILTER_VISIBLE_KEY) && savedInstanceState.getBoolean(FILTER_VISIBLE_KEY);
        }

        // Set the toolbar as the app bar for the activity
        final Toolbar toolbar = amBinding.toolbar;
        setSupportActionBar(toolbar);

        // The DrawerLayout and the left navigation view
        drawerLayout = amBinding.drawerLayout;
        navView = amBinding.sideMenu;
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

        // Set up the sort/filter view
        setupSortFilterView();

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
        // Set up the state, if necessary
        filterMenuIcon = menu.findItem(R.id.action_filter);
        if (filterVisible) {
            final int filterIcon = onTablet ? R.drawable.ic_data : R.drawable.ic_list;
            filterMenuIcon.setIcon(filterIcon);
        }

        // Associate searchable configuration with the SearchView
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchViewIcon = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchViewIcon.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Set up the state, if necessary
        if (filterVisible && !onTablet) {
            searchViewIcon.setVisible(false);
        }


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
        if (onTablet && amBinding != null && spellWindowBinding.getSpell() != null) {
            outState.putParcelable(spellBundleKey, spellWindowBinding.getSpell());
            outState.putInt(spellIndexBundleKey, spellWindowBinding.getSpellIndex());
            final ToggleButton favoriteButton = spellWindowBinding.favoriteButton;
            final ToggleButton preparedButton = spellWindowBinding.preparedButton;
            final ToggleButton knownButton = spellWindowBinding.knownButton;
            outState.putBoolean(FAVORITE_KEY, favoriteButton.isSet());
            outState.putBoolean(PREPARED_KEY, preparedButton.isSet());
            outState.putBoolean(KNOWN_KEY, knownButton.isSet());
        }
        outState.putBoolean(FILTER_VISIBLE_KEY, filterVisible);
    }

    // Close the drawer with the back button if it's open
    @Override
    public void onBackPressed() {
        // InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else if (filterVisible) {
            toggleWindowVisibilities();
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

            // If the spell's status changed, take care of the necessary changes
            if (changed) {

                // Re-display the spells if we have at least one filter selected
                if (oneChecked) {
                    filter();
                } else {
                    spellAdapter.notifyItemChanged(index);
                }

                // Save
                saveCharacterProfile();
                saveSettings();
            }

        }
    }

    void openSpellWindow(Spell spell, int pos) {

        // On a phone, we're going to open a new window by starting a SpellWindow activity
        if (!onTablet) {
            try {
                final Intent intent = new Intent(MainActivity.this, SpellWindow.class);
                intent.putExtra(SpellWindow.SPELL_KEY, spell);
                intent.putExtra(SpellWindow.TEXT_SIZE_KEY, settings.spellTextSize());
                intent.putExtra(SpellWindow.FAVORITE_KEY, characterProfile.isFavorite(spell));
                intent.putExtra(SpellWindow.PREPARED_KEY, characterProfile.isPrepared(spell));
                intent.putExtra(SpellWindow.KNOWN_KEY, characterProfile.isKnown(spell));
                intent.putExtra(SpellWindow.INDEX_KEY, pos);
                startActivityForResult(intent, RequestCodes.SPELL_WINDOW_REQUEST);
                overridePendingTransition(R.anim.right_to_left_enter, R.anim.identity);
            } catch (Exception e) { e.printStackTrace(); }
        }

        // On a tablet, we'll show the spell info on the right-hand side of the screen
        else {
            //spellWindowCL.setVisibility(View.VISIBLE);
            spellWindowBinding.setSpell(spell);
            spellWindowBinding.setSpellIndex(pos);
            spellWindowBinding.executePendingBindings();
            filterVisible = false;
            updateWindowVisibilities();
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

    private void setupSortFilterView() {
        sortFilterAdapter = new SortFilterExpandableAdapter(this);
        sortFilterBinding.sortFilterExpandableList.setAdapter(sortFilterAdapter);
        for (int i = 0; i < sortFilterAdapter.getGroupCount(); ++i) {
            sortFilterBinding.sortFilterExpandableList.expandGroup(i);
        }
    }

    private void setupSpellRecycler(ArrayList<Spell> spells) {
        spellRecycler = amBinding.spellRecycler;
        final RecyclerView.LayoutManager spellLayoutManager = new LinearLayoutManager(this);
        spellAdapter = new SpellRowAdapter(spells);
        spellRecycler.setAdapter(spellAdapter);
        spellRecycler.setLayoutManager(spellLayoutManager);
    }

    private void setupSwipeRefreshLayout() {
        // Set up the 'swipe down to filter' behavior of the RecyclerView
        final SwipeRefreshLayout swipeLayout = amBinding.swipeRefreshLayout;
        swipeLayout.setOnRefreshListener(() -> {
            filter();
            clearCurrentFocus();
            hideSoftKeyboard();
            swipeLayout.setRefreshing(false);
        });

        // Configure the refreshing colors
        swipeLayout.setColorSchemeResources(R.color.darkBrown, R.color.lightBrown, R.color.black);
    }

    private void setupRightNav() {

        // Get the right navigation view and the ExpandableListView
        rightNavView = amBinding.rightMenu;
        rightExpLV = amBinding.navRightExpandable;

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

    public static void hideSoftKeyboard(View view, Context context) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    void filter() {
        if (spellAdapter == null) { return; }
        final CharSequence query = (searchView != null) ? searchView.getQuery() : "";
        spellAdapter.getFilter().filter(query);
    }

    private void singleSort() {
        final SortField sf1 = characterProfile.getFirstSortField();
        final boolean reverse1 = characterProfile.getFirstSortReverse();
        spellAdapter.singleSort(sf1, reverse1);
    }

    private void doubleSort() {
        final SortField sf1 = characterProfile.getFirstSortField();
        final SortField sf2 = characterProfile.getSecondSortField();
        final boolean reverse1 = characterProfile.getFirstSortReverse();
        final boolean reverse2 = characterProfile.getSecondSortReverse();
        spellAdapter.doubleSort(sf1, sf2, reverse1, reverse2);
    }

    void sort() {
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

    private void saveJSON(JSONObject json, File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveJSON(JSONArray json, File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(json.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Saves the current settings to a file, in JSON format
    private boolean saveSettings() {
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
        try {
            final String charFile = characterProfile.getName() + ".json";
            final File profileLocation = new File(profilesDir, charFile);
            characterProfile.save(profileLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setSideMenuCharacterName() {
        final MenuItem m = navView.getMenu().findItem(R.id.nav_character);
        m.setTitle("Character: " + characterProfile.getName());
    }

    private void setFilterSettings() {

        // Set the status filter
        final StatusFilterField sff = characterProfile.getStatusFilter();
        navView.getMenu().getItem(sff.getIndex()).setChecked(true);
    }


    // Sets the given character profile to the active one
    // The boolean parameter should only be true if this is called during initial setup, when all of the UI elements may not be initialized yet
    void setCharacterProfile(CharacterProfile cp, boolean initialLoad) {
        System.out.println("Setting character profile: " + cp.getName());
        characterProfile = cp;
        settings.setCharacterName(cp.getName());

        setSideMenuCharacterName();
        setFilterSettings();
        sortFilterAdapter.updateProfileBindings();
        saveSettings();
        saveCharacterProfile();
        try {
            if (!initialLoad) {
                sort();
                filter();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // Reset the spell view if on the tablet
        if (onTablet && !initialLoad) {
            spellWindowCL.setVisibility(View.INVISIBLE);
            spellWindowBinding.setSpell(null);
            spellWindowBinding.setSpellIndex(-1);
            spellWindowBinding.executePendingBindings();
        }
    }

    // Sets the given character profile to be the active one
    void setCharacterProfile(CharacterProfile cp) {
        setCharacterProfile(cp, false);
    }

    // Opens a character creation dialog
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

    // Opens the email chooser to send feedback
    // In the unlikely event that the user doesn't have an email application, a Toast message displays instead
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

    // Deletes the character profile corresponding to the given name, if one exists
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

    // Returns the current list of characters
    ArrayList<String> charactersList() {
        final ArrayList<String> charList = new ArrayList<>();
        final int toRemove = CHARACTER_EXTENSION.length();
        for (File file : profilesDir.listFiles()) {
            final String filename = file.getName();
            if (filename.endsWith(CHARACTER_EXTENSION)) {
                final String charName = filename.substring(0, filename.length() - toRemove);
                charList.add(charName);
            }
        }
        charList.sort(String::compareToIgnoreCase);
        return charList;
    }

    // Opens a character selection dialog
    void openCharacterSelection() {
        final CharacterSelectionDialog dialog = new CharacterSelectionDialog();
        final Bundle args = new Bundle();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "selectCharacter");
    }

    // This function performs filtering only if one of the spell lists is currently selected
    void filterIfStatusSet() {
        if (characterProfile.isStatusSet()) {
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


    // This function takes care of any setup that's needed only on a tablet layout
    private void tabletSetup() {

        // Spell window background
        spellWindowBinding = amBinding.spellWindow;
        spellWindowCL = amBinding.spellWindow.spellWindowConstraint;
        spellWindowCL.setBackground(null);
        spellWindowCL.setVisibility(View.INVISIBLE);

        // Set button callbacks
        final ToggleButton favoriteButton = spellWindowBinding.favoriteButton;
        favoriteButton.setCallback(() -> {
            characterProfile.toggleFavorite(spellWindowBinding.getSpell());
            spellAdapter.notifyItemChanged(spellWindowBinding.getSpellIndex());
            saveCharacterProfile();
        });
        final ToggleButton knownButton = spellWindowBinding.knownButton;
        knownButton.setCallback(() -> {
            characterProfile.toggleKnown(spellWindowBinding.getSpell());
            spellAdapter.notifyItemChanged(spellWindowBinding.getSpellIndex());
            saveCharacterProfile();
        });
        final ToggleButton preparedButton = spellWindowBinding.preparedButton;
        preparedButton.setCallback(() -> {
            characterProfile.togglePrepared(spellWindowBinding.getSpell());
            spellAdapter.notifyItemChanged(spellWindowBinding.getSpellIndex());
            saveCharacterProfile();
        });
    }

    // If we're on a tablet, this function updates the spell window to match its status in the character profile
    // This is called after one of the spell list buttons is pressed for that spell in the main table
    void updateSpellWindow(Spell s, boolean favorite, boolean prepared, boolean known) {
        if (onTablet && (spellWindowCL.getVisibility() == View.VISIBLE) && (amBinding != null) && (s.equals(spellWindowBinding.getSpell())) ) {
            final ToggleButton favoriteButton = spellWindowBinding.favoriteButton;
            favoriteButton.set(favorite);
            final ToggleButton preparedButton = spellWindowBinding.preparedButton;
            preparedButton.set(prepared);
            final ToggleButton knownButton = spellWindowBinding.knownButton;
            knownButton.set(known);
        }
    }

    // This function clears the current focus
    // It also closes the soft keyboard, if it's open
    private void clearCurrentFocus() {
        final View view = getCurrentFocus();
        if (view != null) {
            view.clearFocus();
        }
    }

    private void hideSoftKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        final View view = getCurrentFocus();
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateWindowVisibilities() {

        // Clear the focus from an EditText, if that's where it is
        // since they have an OnFocusChangedListener
        // We want to do this BEFORE we sort/filter so that any changes can be made to the CharacterProfile
        if (!filterVisible) {
            hideSoftKeyboard();
        }

        // Sort and filter, if necessary
        if (!onTablet && !filterVisible) {
            sort();
            filter();
        }

        // The current window visibilities
        int spellVisibility = filterVisible ? View.GONE : View.VISIBLE;
        final int filterVisibility = filterVisible ? View.VISIBLE : View.GONE;
        if (onTablet && spellWindowBinding.getSpell() == null) {
            spellVisibility = View.GONE;
        }

        // Update window visibilities appropriately
        final View spellView = onTablet ? spellWindowCL : spellsCL;
        spellView.setVisibility(spellVisibility);
        sortFilterBinding.getRoot().setVisibility(filterVisibility);

        // Collapse the SearchView if it's open, and set the search icon visibility appropriately
        if (filterVisible && (searchView != null) && !searchView.isIconified()) {
            searchViewIcon.collapseActionView();
        }
        if (!onTablet && searchViewIcon != null) {
            searchViewIcon.setVisible(spellVisibility == View.VISIBLE);
        }

        // Update the filter icon on the action bar
        // If the filters are open, we show a list or data icon (depending on the platform) instead ("return to the data")
        if (filterMenuIcon != null) {
            final int filterIcon = onTablet ? R.drawable.ic_data : R.drawable.ic_list;
            final int icon = filterVisible ? filterIcon : R.drawable.ic_filter;
            filterMenuIcon.setIcon(icon);
        }

        // Save the character profile
        saveCharacterProfile();
    }

    private void toggleWindowVisibilities() {
        filterVisible = !filterVisible;
        updateWindowVisibilities();
    }

}
