package dnd.jon.spellbook;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MotionEvent;
import android.widget.Toast;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.EditText;
import android.content.Intent;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import dnd.jon.spellbook.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Fragment tags
    private static final String SPELL_TABLE_FRAGMENT_TAG = "SpellTableFragment";
    private static final String SORT_FILTER_FRAGMENT_TAG = "SortFilterFragment";
    private static final String SPELL_WINDOW_FRAGMENT_TAG = "SpellWindowFragment";
    private static final String SPELL_SLOTS_FRAGMENT_TAG = "SpellSlotsFragment";

    // The settings file
    private static final String settingsFile = "Settings.json";

    // ViewModel stuff
    private ViewModelProvider.Factory viewModelFactory;
    private SpellbookViewModel viewModel;

    // UI elements
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    //private NavigationView rightNavView;
    private ExpandableListView rightExpLV;
    private ExpandableListAdapter rightAdapter;
    private SearchView searchView;
    private MenuItem searchViewIcon;
    private MenuItem filterMenuIcon;

    // For close spell windows on a swipe, on a phone
    private OnSwipeTouchListener swipeCloseListener;

    private static final String profilesDirName = "Characters";
    //private static final String createdSpellDirName = "CreatedSpells";
    private File profilesDir;
    //private File createdSpellsDir;
    //private Map<File,String> directories = new HashMap<>();

    // The character profile and settings
    private CharacterProfile characterProfile;
    private SpellFilterStatus spellFilterStatus;
    private SortFilterStatus sortFilterStatus;
    private Settings settings;

    // For filtering stuff
    private boolean filterVisible = false;

    // For passing the spell and its index to the SpellWindow
    private static final String spellBundleKey = "SPELL";
    private static final String spellIndexBundleKey = "SPELL_INDEX";

    // For feedback messages
    private static final String devEmail = "dndspellbookapp@gmail.com";
    private static final String emailMessage = "[Android] Feedback";

    // Logging tag
    private static final String TAG = "MainActivity";

    // The map ID -> StatusFilterField relating left nav bar items to the corresponding spell status filter
    private static final HashMap<Integer,StatusFilterField> statusFilterIDs = new HashMap<Integer,StatusFilterField>() {{
       put(R.id.nav_all, StatusFilterField.ALL);
       put(R.id.nav_favorites, StatusFilterField.FAVORITES);
       put(R.id.nav_prepared, StatusFilterField.PREPARED);
       put(R.id.nav_known, StatusFilterField.KNOWN);
    }};

    // For listening to keyboard visibility events
    //private Unregistrar unregistrar;

    // The file extension for character files
    private static final String CHARACTER_EXTENSION = ".json";

    // Keys for Bundles
    private static final String FAVORITE_KEY = "FAVORITE";
    private static final String KNOWN_KEY = "KNOWN";
    private static final String PREPARED_KEY = "PREPARED";
    private static final String FILTER_VISIBLE_KEY = "FILTER_VISIBLE";

    // Whether or not this is running on a tablet
    private boolean onTablet;

    // For view and data binding
    private ActivityMainBinding binding;
    private SpellTableFragment spellTableFragment;
    private SortFilterFragment sortFilterFragment;
    private SpellWindowFragment spellWindowFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the main activity binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Are we on a tablet or not?
        // If we're on a tablet, do the necessary setup
        onTablet = getResources().getBoolean(R.bool.isTablet);
        if (onTablet) { tabletSetup(); }

        // Get the spell view model
        viewModel = new ViewModelProvider(this).get(SpellbookViewModel.class);

        // Any view model observers that we need
        viewModel.currentProfile().observe(this, this::setCharacterProfile);
        viewModel.currentSpell().observe(this, this::openSpellWindow);

        // For keyboard visibility listening
        KeyboardVisibilityEvent.setEventListener(this, (isOpen) -> {
            if (!isOpen) {
                clearViewTypeFocus(EditText.class);
            }
        });

        // Re-set the current spell after a rotation (only needed on tablet)
        if (onTablet && savedInstanceState != null) {
            final Spell spell = savedInstanceState.containsKey(spellBundleKey) ? savedInstanceState.getParcelable(spellBundleKey) : null;
            if (spell != null) {
                updateSpellWindow(spell);
            }
        }

        // Whether or not we want the filter to be visible
        if (savedInstanceState != null) {
            filterVisible = savedInstanceState.containsKey(FILTER_VISIBLE_KEY) && savedInstanceState.getBoolean(FILTER_VISIBLE_KEY);
        }

        // Set the toolbar as the app bar for the activity
        final Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // The DrawerLayout and the left navigation view
        drawerLayout = binding.drawerLayout;
        navView = binding.sideMenu;
        final NavigationView.OnNavigationItemSelectedListener navViewListener = menuItem -> {
            final int index = menuItem.getItemId();
            boolean close = false;
            if (index == R.id.subnav_charselect) {
                openCharacterSelection();
            } else if (index == R.id.nav_feedback) {
                sendFeedback();
            } else if (index == R.id.nav_rate_us) {
                openPlayStoreForRating();
            } else if (index == R.id.nav_whats_new) {
                showUpdateDialog(false);
            //} else if (index == R.id.create_a_spell) {
            //    openSpellCreationWindow();
            } else if (statusFilterIDs.containsKey(index)) {
                final StatusFilterField sff = statusFilterIDs.get(index);
                sortFilterStatus.setStatusFilterField(sff);
                saveCharacterProfile();
                close = true;
            }
            saveSettings();

            // This piece of code makes the drawer close when an item is selected
            // At the moment, we only want that for when choosing one of favorites, known, prepared
            if (close && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
            }
            return true;
        };
        navView.setNavigationItemSelectedListener(navViewListener);

        // This listener will stop the spell recycler's scrolling when the navigation drawer is opened
        // This prevents a crash that could occur if a filter button is selected while the spell recycler is still scrolling
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }
            @Override public void onDrawerClosed(@NonNull View drawerView) { }
            @Override public void onDrawerStateChanged(int newState) { }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                spellTableFragment.stopScrolling();
            }

        });

        swipeCloseListener = new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                if (!onTablet && !spellTableFragment.isHidden()) {
                    closeSpellWindow();
                }
            }
        };

        // Set the hamburger button to open the left nav
        final ActionBarDrawerToggle leftNavToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.left_navigation_drawer_open, R.string.left_navigation_drawer_closed);
        drawerLayout.addDrawerListener(leftNavToggle);
        leftNavToggle.syncState();
        leftNavToggle.setDrawerSlideAnimationEnabled(true); // Whether or not the hamburger button changes to the arrow when the drawer is open
        leftNavToggle.setDrawerIndicatorEnabled(true);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener((v) -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Set up the right navigation view
        setupRightNav();

        // Set up the FAB
        setupFAB();

        //View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        /*int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/

        // Create any necessary directories
        // If they already exist, this function does nothing
        profilesDir = createFileDirectory(profilesDirName);
        //createdSpellsDir = createFileDirectory(createdSpellDirName);

        // Load the settings and the character profile
        try {

            // Load the settings
            final JSONObject json = loadJSONfromData(settingsFile);
            settings = new Settings(json);

            // Load the character profile
            final String charName = settings.characterName();
            viewModel.setProfileByName(charName);

        } catch (Exception e) {
            String s = loadAssetAsString(new File(settingsFile));
            Log.v(TAG, "Error loading settings");
            Log.v(TAG, "The settings file content is: " + s);
            settings = new Settings();
            final List<String> characterList = viewModel.getCharacterNames().getValue();
            if (characterList != null && characterList.size() > 0) {
                final String firstCharacter = characterList.get(0);
                settings.setCharacterName(firstCharacter);
            }
            e.printStackTrace();
            saveSettings();
        }

        // If the character profile is null, we create one
        if ( (settings.characterName() == null) || viewModel.getProfile() == null ) {
            openCharacterCreationDialog();
        }

        spellTableFragment = (SpellTableFragment) getSupportFragmentManager().findFragmentByTag(SPELL_TABLE_FRAGMENT_TAG);
        sortFilterFragment = (SortFilterFragment) getSupportFragmentManager().findFragmentByTag(SORT_FILTER_FRAGMENT_TAG);
        if (onTablet) {
            spellWindowFragment = (SpellWindowFragment) getSupportFragmentManager().findFragmentByTag(SPELL_WINDOW_FRAGMENT_TAG);
        }

        // The right nav drawer often gets in the way of fast scrolling on a phone
        // Since we can open it from the action bar, we'll lock it closed from swiping
        if (!onTablet) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        }

        // Set the correct view visibilities
        if (filterVisible) {
            updateWindowVisibilities();
        }

        // If we need to, open the update dialog
        showUpdateDialog(true);

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
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                viewModel.setSearchQuery(text);
                spellTableFragment.stopScrolling();
                return true;
            }
        });
        return true;
    }

    // To handle actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemID = item.getItemId();
        if (itemID == R.id.action_filter) {
            toggleWindowVisibilities();
            return true;
        } else if (itemID == R.id.action_info) {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void addFragment(int containerID, Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(containerID, fragment, tag)
                .commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void addFragment(int containerID, Class<? extends Fragment> fragmentClass, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(containerID, fragmentClass, null, tag)
                .commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void replaceFragment(int containerID, Fragment fragment, String tag, boolean addToBackStack) {
        final FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(containerID, fragment, tag);
        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction.commit();
    }

    private void replaceFragment(int containerID, Class<? extends Fragment> fragmentClass, String tag, boolean addToBackStack) {
        final FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(containerID, fragmentClass, null, tag);
        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction.commit();
    }

    private void hideFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .hide(fragment)
                .commit();
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .show(fragment)
                .commit();
    }

    @Override
    public void onStart() {
        //System.out.println("Calling onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        //System.out.println("Calling onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        viewModel.saveCurrentProfile();
        super.onPause();
    }

    @Override
    public void onStop() {
        viewModel.saveCurrentProfile();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        viewModel.saveCurrentProfile();
        super.onDestroy();
    }

    // Necessary for handling rotations
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
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
        } else if (!onTablet && spellWindowFragment != null) {
            closeSpellWindow();
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
            final Spell spell = data.getParcelableExtra(SpellWindow.SPELL_KEY);
            final boolean fav = data.getBooleanExtra(SpellWindow.FAVORITE_KEY, false);
            final boolean known = data.getBooleanExtra(SpellWindow.KNOWN_KEY, false);
            final boolean prepared = data.getBooleanExtra(SpellWindow.PREPARED_KEY, false);
            final boolean wasFav = spellFilterStatus.isFavorite(spell);
            final boolean wasKnown = spellFilterStatus.isKnown(spell);
            final boolean wasPrepared = spellFilterStatus.isPrepared(spell);
            spellFilterStatus.setFavorite(spell, fav);
            spellFilterStatus.setKnown(spell, known);
            spellFilterStatus.setPrepared(spell, prepared);
            final boolean changed = (wasFav != fav) || (wasKnown != known) || (wasPrepared != prepared);
            final Menu menu = navView.getMenu();
            final boolean oneChecked = menu.findItem(R.id.nav_favorites).isChecked() || menu.findItem(R.id.nav_known).isChecked() || menu.findItem(R.id.nav_prepared).isChecked();

            // If the spell's status changed, take care of the necessary changes
            if (changed) {

                // Re-display the spells if we have at least one filter selected
                if (oneChecked) {
                    //filter();
                } else {
                    spellWindowFragment.updateSpell(spell);
                }

                // Save
                saveCharacterProfile();
                saveSettings();
            }

        } else if (requestCode == RequestCodes.SPELL_CREATION_REQUEST && resultCode == RESULT_OK) {

        }
    }

    @NonNull
    @Override
    public ViewModelProvider.Factory getDefaultViewModelProviderFactory() {
        if (viewModelFactory == null) {
            viewModelFactory = new SpellbookViewModelFactory(this.getApplication());
        }
        return viewModelFactory;
    }

    void updateSpellWindow(Spell spell, int pos) {

        if (onTablet) {
            spellWindowFragment.updateSpell(spell);
            //spellWindowFragment.updateUseExpanded(sortFilterStatus.getUseTashasExpandedLists());
            filterVisible = false;
            updateWindowVisibilities();
        } else {
            final SpellWindowFragment fragment = new SpellWindowFragment();
            replaceFragment(R.id.phone_fullscreen_fragment_container, fragment, SPELL_WINDOW_FRAGMENT_TAG, true);
        }

    }

    void openSpellPopup(View view, Spell spell) {
        final SpellStatusPopup ssp = new SpellStatusPopup(this, spell);
        ssp.showUnderView(view);
    }

    private void openSpellSlotsFragment() {

        final SpellSlotManagerFragment fragment = new SpellSlotManagerFragment(characterProfile.getSpellSlotStatus());
        if (onTablet) {
            //replaceFragment(R.id.tablet_detail_fragment_container, fragment, SPELL_SLOTS_FRAGMENT_TAG, false);
        } else {
            addFragment(R.id.phone_fullscreen_fragment_container, fragment, SPELL_SLOTS_FRAGMENT_TAG);
        }
    }

    private void setupFAB() {
        binding.fab.setOnClickListener((v) -> {
            final CenterReveal centerReveal = new CenterReveal(binding.fab);
            centerReveal.start(this::openSpellSlotsFragment);
        });
    }

    private void setupRightNav() {

        // Get the right navigation view and the ExpandableListView
        //rightNavView = amBinding.rightMenu;
        rightExpLV = binding.navRightExpandable;

        // Get the list of group names, as an Array
        // The group names are the headers in the expandable list
        final List<String> rightNavGroups = Arrays.asList(getResources().getStringArray(R.array.right_group_names));

        // Get the names of the group elements, as Arrays
        final List<String[]> groups = new ArrayList<>();
        groups.add(getResources().getStringArray(R.array.basics_items));
        groups.add(getResources().getStringArray(R.array.casting_spell_items));
        final String[] casterNames = DisplayUtils.getDisplayNames(this, CasterClass.class);
        groups.add(casterNames);

        // For each group, get the text that corresponds to each child
        // Here, entries with the same index correspond to one another
        final List<Integer> basicsIDs = new ArrayList<>(Arrays.asList(R.string.what_is_a_spell, R.string.spell_level_info,
                R.string.known_and_prepared_spells, R.string.the_schools_of_magic, R.string.spell_slots_info, R.string.cantrips,
                R.string.rituals, R.string.the_weave_of_magic));
        final List<Integer> castingSpellIDs = new ArrayList<>(Arrays.asList(R.string.casting_time_info, R.string.range_info, R.string.components_info,
                R.string.duration_info, R.string.targets, R.string.areas_of_effect, R.string.saving_throws,
                R.string.attack_rolls, R.string.combining_magical_effects, R.string.casting_in_armor));
        final List<Integer> classInfoIDs = IntStream.of(LocalizationUtils.supportedSpellcastingInfoIDs()).boxed().collect(Collectors.toList());
        final List<List<Integer>> childTextLists = new ArrayList<>(Arrays.asList(basicsIDs, castingSpellIDs, classInfoIDs));

        // Create maps of the form group -> list of children, and group -> list of children's text
        final int nGroups = rightNavGroups.size();
        final Map<String, List<String>> childData = new HashMap<>();
        final Map<String, List<Integer>> childTextIDs = new HashMap<>();
        for (int i = 0; i < nGroups; ++i) {
            childData.put(rightNavGroups.get(i), Arrays.asList(groups.get(i)));
            childTextIDs.put(rightNavGroups.get(i), childTextLists.get(i));
        }

        // Create the tables array
        final int[] tableIDs = LocalizationUtils.supportedTableLayoutIDs();
        //final int[] tableIDs = new int[]{ R.string.bard_table, R.string.cleric_table, R.string.druid_table, R.string.paladin_table, R.string.ranger_table, R.string.sorcerer_table, R.string.warlock_table, R.string.wizard_table };

        // Create the adapter
        rightAdapter = new NavExpandableListAdapter(this, rightNavGroups, childData, childTextIDs, tableIDs);
        rightExpLV.setAdapter(rightAdapter);
        final View rightHeaderView = getLayoutInflater().inflate(R.layout.right_expander_header, null);
        rightExpLV.addHeaderView(rightHeaderView);

        // Set the callback that displays the appropriate popup when the list item is clicked
        rightExpLV.setOnChildClickListener((ExpandableListView elView, View view, int gp, int cp, long id) -> {
            final NavExpandableListAdapter adapter = (NavExpandableListAdapter) elView.getExpandableListAdapter();
            final String title = (String) adapter.getChild(gp, cp);
            final int textID = adapter.childTextID(gp, cp);
            final int tableID = adapter.getTableID(gp, cp);

            // Show a popup
            //SpellcastingInfoPopup popup = new SpellcastingInfoPopup(this, title, textID, true);
            //popup.show();

            // Show a full-screen activity
            final Intent intent = new Intent(MainActivity.this, SpellcastingInfoWindow.class);
            intent.putExtra(SpellcastingInfoWindow.TITLE_KEY, title);
            intent.putExtra(SpellcastingInfoWindow.INFO_KEY, textID);
            intent.putExtra(SpellcastingInfoWindow.TABLE_KEY, tableID);
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

    // Saves the current settings to a file, in JSON format
    private boolean saveSettings() {
        final File settingsLocation = new File(getApplicationContext().getFilesDir(), settingsFile);
        return settings.save(settingsLocation);
    }

    boolean saveCharacterProfile(CharacterProfile profile) {
        try {
            final String charFile = profile.getName() + ".json";
            final File profileLocation = new File(profilesDir, charFile);
            return profile.save(profileLocation);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean saveCharacterProfile() { return saveCharacterProfile(characterProfile); }

    private void setSideMenuCharacterName() {
        final MenuItem m = navView.getMenu().findItem(R.id.nav_character);
        m.setTitle(getString(R.string.prompt, getString(R.string.character), characterProfile.getName()));
    }

    private void setFilterSettings() {

        // Set the status filter
        final StatusFilterField sff = sortFilterStatus.getStatusFilterField();
        navView.getMenu().getItem(sff.getIndex()).setChecked(true);

    }

    // Sets the given character profile to the active one
    // The boolean parameter should only be true if this is called during initial setup, when all of the UI elements may not be initialized yet
    void setCharacterProfile(CharacterProfile cp, boolean initialLoad) {
        //System.out.println("Setting character profile: " + cp.getName());
        characterProfile = cp;
        spellFilterStatus = cp.getSpellFilterStatus();
        sortFilterStatus = cp.getSortFilterStatus();
        settings.setCharacterName(cp.getName());

        setSideMenuCharacterName();
        setFilterSettings();
        saveSettings();
        saveCharacterProfile();

        // Reset the spell view if on the tablet
        if (onTablet && !initialLoad) {
            spellWindowFragment.updateSpell(null);
        }
    }

    // Sets the given character profile to be the active one
    void setCharacterProfile(CharacterProfile cp) {
        setCharacterProfile(cp, false);
    }

    // Opens a character creation dialog
    void openCharacterCreationDialog() {
        final CreateCharacterDialog dialog = new CreateCharacterDialog();
        dialog.show(getSupportFragmentManager(), "createCharacter");
    }

//    void openFeedbackWindow() {
//        final FeedbackDialog dialog = new FeedbackDialog();
//        dialog.show(getSupportFragmentManager(), "feedback");
//    }

    // Opens the email chooser to send feedback
    // In the unlikely event that the user doesn't have an email application, a Toast message displays instead
    void sendFeedback() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{devEmail});
        intent.putExtra(Intent.EXTRA_SUBJECT, emailMessage);
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, getString(R.string.no_email_clients), Toast.LENGTH_SHORT).show();
        }
    }

    // Returns the current list of characters
    List<String> charactersList() {
        final ArrayList<String> charList = new ArrayList<>();
        final int toRemove = CHARACTER_EXTENSION.length();
        if (profilesDir == null) { return charList; }
        final File[] files = profilesDir.listFiles();
        if (files == null) { return charList; }
        for (File file : files) {
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
        dialog.show(getSupportFragmentManager(), "selectCharacter");
    }

    public SpellFilterStatus getSpellFilterStatus() { return spellFilterStatus; }
    public SortFilterStatus getSortFilterStatus() { return sortFilterStatus; }

    // This function takes care of any setup that's needed only on a tablet layout
    private void tabletSetup() {
        spellWindowFragment = new SpellWindowFragment();
        spellWindowFragment.updateSpell(null);
    }

    // If we're on a tablet, this function updates the spell window to match its status in the character profile
    // This is called after one of the spell list buttons is pressed for that spell in the main table
    void updateSpellWindow(Spell spell) {
        if (spellWindowFragment != null) {
            spellWindowFragment.updateSpell(spell);
        }
    }

    public void handleSpellDataUpdate() {
        if (spellTableFragment != null) {
            final Spell spell = getCurrentSpell();
            if (spell != null) {
                spellTableFragment.updateSpell(spell);
            }
        }
    }

    public Spell getCurrentSpell() {
        if (spellWindowFragment != null) {
            return spellWindowFragment.getSpell();
        }
        return null;
    }

    // This function clears the current focus
    // It also closes the soft keyboard, if it's open
    private void clearCurrentFocus() {
        final View view = getCurrentFocus();
        if (view != null) {
            view.clearFocus();
        }
    }

    // This function clears the current focus ONLY IF the focused view is of the given type
    private <T extends View> void clearViewTypeFocus(Class<T> viewType) {
        final View view = getCurrentFocus();
        if (viewType.isInstance(view)) {
            view.clearFocus();
        }
    }

    private void hideSoftKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isAcceptingText()) {
            imm.toggleSoftInput(0, 0);
        }
    }

    private void updateWindowVisibilities() {

        // Clear the focus from an EditText, if that's where it is
        // since they have an OnFocusChangedListener
        // We want to do this BEFORE we sort/filter so that any changes can be made to the CharacterProfile
        if (!filterVisible) {
            final View view = getCurrentFocus();
            if (view != null) {
                hideSoftKeyboard(view, this);
            }
        }

        // The current window visibilities
        boolean spellVisible = !filterVisible;;
        if (onTablet && spellWindowFragment.getSpell() == null) {
            spellVisible = false;
        }

        // Update window visibilities appropriately
        final Fragment notFilterFragment = onTablet ? spellWindowFragment : spellTableFragment;
        final Fragment fragmentToShow = filterVisible ? sortFilterFragment : notFilterFragment;
        final Fragment fragmentToHide = filterVisible ? notFilterFragment : sortFilterFragment;
        hideFragment(fragmentToHide);
        showFragment(fragmentToShow);

        // Show/hide the FAB
        final boolean fabVisibility = onTablet ? !(filterVisible || spellVisible) : !filterVisible;
        binding.fab.setVisibility(fabVisibility ? View.VISIBLE : View.GONE);

        // Collapse the SearchView if it's open, and set the search icon visibility appropriately
        if (filterVisible && (searchView != null) && !searchView.isIconified()) {
            searchViewIcon.collapseActionView();
        }
        if (!onTablet && searchViewIcon != null) {
            searchViewIcon.setVisible(spellVisible);
        }

        // Update the filter icon on the action bar
        // If the filters are open, we show a list or data icon (depending on the platform)
        // instead ("return to the data")
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

    private void openPlayStoreForRating() {
        final Uri uri = Uri.parse("market://details?id=" + getPackageName());
        final Intent goToPlayStore = new Intent(Intent.ACTION_VIEW, uri);
        goToPlayStore.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToPlayStore);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    private void openSpellCreationWindow() {
        final Intent intent = new Intent(MainActivity.this, SpellCreationActivity.class);
        startActivityForResult(intent, RequestCodes.SPELL_CREATION_REQUEST);
        overridePendingTransition(R.anim.identity, android.R.anim.slide_in_left);
    }

    private File createFileDirectory(String directoryName) {
        File directory = new File(getApplicationContext().getFilesDir(), directoryName);
        if ( !(directory.exists() && directory.isDirectory()) ) {
            final boolean success = directory.mkdir();
            if (!success) {
                Log.v(TAG, "Error creating directory: " + directory); // Add something real here eventually
            }
        }
        return directory;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            if (! (view instanceof EditText)) {
                view.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void openSpellWindow(Spell spell) {
        if (!onTablet) {
            final Bundle args = new Bundle();
            args.putParcelable(SpellWindowFragment.SPELL_KEY, spell);
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.right_to_left_enter, R.anim.identity)
                    .add(R.id.phone_fullscreen_fragment_container, SpellWindowFragment.class, args, SPELL_WINDOW_FRAGMENT_TAG)
                    .runOnCommit(() -> {
                        this.spellWindowFragment = (SpellWindowFragment) getSupportFragmentManager().findFragmentByTag(SPELL_WINDOW_FRAGMENT_TAG);
                        setupSpellWindowCloseOnSwipe();
                        //binding.phoneFullscreenFragmentContainer.bringToFront();
                    })
                    .commit();
        }
    }

    private void closeSpellWindow() {
        if (!onTablet) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.identity, R.anim.left_to_right_exit)
                    .remove(spellWindowFragment)
                    .runOnCommit(() -> {
                        this.spellWindowFragment = null;
                        //binding.coordinatorLayout.bringToFront();
                    })
                    .commit();
        }
    }

    private void showUpdateDialog(boolean checkIfNecessary) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String key = "first_time_" + GlobalInfo.VERSION_CODE;
        final boolean toShow = !checkIfNecessary || (!prefs.contains(key) && charactersList().size() > 0);
        if (toShow) {
            final int titleID = R.string.update_02_11_title;
            final int descriptionID = R.string.update_02_11_description;
            final Runnable onDismissAction = () -> {
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(key, true).apply();
            };
            SpellbookUtils.showMessageDialog(this, titleID, descriptionID, false, onDismissAction);
        } else if (checkIfNecessary) {
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(key, true).apply();
        }
    }

    private void saveExternalJSONFile(JSONObject json) {

    }

    private void setupSpellWindowCloseOnSwipe() {
        if (spellWindowFragment == null) { return; }
        final View view = spellWindowFragment.getScrollView();
        if (view == null) { return; }
        view.setOnTouchListener(swipeCloseListener);
    }
}
