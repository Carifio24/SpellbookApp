package dnd.jon.spellbook;

import static dnd.jon.spellbook.R.*;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;

import androidx.activity.OnBackPressedCallback;
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

import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
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

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import org.javatuples.Pair;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import dnd.jon.spellbook.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity
        //implements FragmentManager.OnBackStackChangedListener
    implements SharedPreferences.OnSharedPreferenceChangeListener
{

    private boolean openedSpellSlotsFromFAB = false;

    // Fragment tags
    private static final String SPELL_TABLE_FRAGMENT_TAG = "SpellTableFragment";
    private static final String SORT_FILTER_FRAGMENT_TAG = "SortFilterFragment";
    private static final String SPELL_WINDOW_FRAGMENT_TAG = "SpellWindowFragment";
    private static final String SPELL_SLOTS_FRAGMENT_TAG = "SpellSlotsFragment";
    private static final String SETTINGS_FRAGMENT_TAG = "SettingsFragment";
    private static final String HOMEBREW_FRAGMENT_TAG = "HomebrewFragment";
    private static final String SPELL_SLOTS_DIALOG_TAG = "SpellSlotsDialog";
    private static final String DELETE_SPELL_DIALOG_TAG = "DeleteSpellDialog";
    private static final String MANAGE_SOURCES_DIALOG_TAG = "ManageSourcesDialog";

    // Tags for dialogs
    private static final String CREATE_CHARACTER_TAG = "createCharacter";
    private static final String SELECT_CHARACTER_TAG = "selectCharacter";
    private static final String SPELL_SLOT_ADJUST_TOTALS_TAG = "adjustSpellSlotTotals";
    private static final String EXPORT_SPELL_LIST_TAG = "exportSpellList";

    // Keys for Bundles
    private static final String FILTER_VISIBLE_KEY = "FILTER_VISIBLE";
    private static final String WINDOW_STATUS_KEY = "WINDOW_STATUS";
    private static final String PREV_WINDOW_STATUS_KEY = "PREV_WINDOW_STATUS";
    private static final String SLOTS_OPENED_FAB_KEY = "SLOTS_OPENED_FAB";

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
    private MenuItem baseFragment1Icon;
    private MenuItem baseFragment2Icon;
    private MenuItem infoMenuIcon;
    private MenuItem editSlotsMenuIcon;
    private MenuItem manageSlotsMenuIcon;
    private MenuItem regainSlotsMenuIcon;
    private MenuItem deleteIcon;
    private MenuItem homebrewIcon;
    private MenuItem manageSourcesIcon;

    private List<Integer> baseFragments;
    private List<Integer> baseFragmentMenuIDs = Arrays.asList(id.action_base_fragment_1, id.action_base_fragment_2);
    private ActionBarDrawerToggle leftNavToggle;

    // For close spell windows on a swipe, on a phone
    private OnSwipeTouchListener swipeCloseListener;

    private static final String profilesDirName = "Characters";
    //private static final String createdSpellDirName = "CreatedSpells";
    //private File createdSpellsDir;
    //private Map<File,String> directories = new HashMap<>();

    // The character profile and settings
    private CharacterProfile characterProfile;
    private SpellFilterStatus spellFilterStatus;
    private SortFilterStatus sortFilterStatus;

    // For filtering stuff
    private boolean filterVisible = false;

    // Observers for updating the spell list counts
    Observer<Boolean> favoriteListCountObserver;
    Observer<Boolean> preparedListCountObserver;
    Observer<Boolean> knownListCountObserver;
    Observer<SpellFilterStatus> spellFilterStatusObserver;
    Observer<Void> spellFilterEventObserver;

    // The center reveal for the FAB
    private CenterReveal fabCenterReveal;

    // For passing the spell and its index to the SpellWindow
    private static final String spellBundleKey = "SPELL";
    private static final String spellIndexBundleKey = "SPELL_INDEX";

    // Logging tag
    private static final String TAG = "MainActivity";

    // The map ID -> StatusFilterField relating left nav bar items to the corresponding spell status filter
    private static final HashMap<Integer,StatusFilterField> statusFilterIDs = new HashMap<>() {{
       put(id.nav_all, StatusFilterField.ALL);
       put(id.nav_favorites, StatusFilterField.FAVORITES);
       put(id.nav_prepared, StatusFilterField.PREPARED);
       put(id.nav_known, StatusFilterField.KNOWN);
    }};

    private static final Map<Integer, Pair<Integer, Integer>> actionBarData = new HashMap<>() {{
       put(id.spellWindowFragment, new Pair<>(string.action_spell_window, drawable.ic_text_snippet));
       put(id.spellTableFragment, new Pair<>(string.action_table, drawable.ic_list));
       put(id.sortFilterFragment, new Pair<>(string.action_filter, drawable.ic_filter));
       put(id.homebrewManagementFragment, new Pair<>(string.homebrew, drawable.cauldron));
    }};

    // For listening to keyboard visibility events
    //private Unregistrar unregistrar;

    // Whether or not this is running on a tablet
    private boolean onTablet;

    // For view and data binding
    private ActivityMainBinding binding;
    private SpellWindowFragment spellWindowFragment;
    private NavHostFragment navHostFragment;
    private NavController navController;

    // For navigation via the action bar

    private Collection<Integer> currentNavFragmentIDs;

    private boolean ignoreSpellStatusUpdate = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
            .detectLeakedClosableObjects()
            .build());

        // Get the main activity binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        spellWindowFragment = (SpellWindowFragment) getSupportFragmentManager().findFragmentByTag(SPELL_WINDOW_FRAGMENT_TAG);

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Are we on a tablet or not?
        // If we're on a tablet, do the necessary setup
        onTablet = getResources().getBoolean(bool.isTablet);
        if (onTablet) {
            tabletSetup();
        }

        if (onTablet) {
            baseFragments = Arrays.asList(id.spellWindowFragment, id.sortFilterFragment, id.homebrewManagementFragment);
        } else {
            baseFragments = Arrays.asList(id.spellTableFragment, id.sortFilterFragment);
        }

        // Get the spell view model
        viewModel = new ViewModelProvider(this).get(SpellbookViewModel.class);

        // For keyboard visibility listening
        KeyboardVisibilityEvent.setEventListener(this, (isOpen) -> {
            if (!isOpen) {
                clearViewTypeFocus(EditText.class);
            }
        });

        // Re-set the current spell after a rotation (only needed on tablet)
        if (onTablet && savedInstanceState != null) {
            final Spell spell = viewModel.currentSpell().getValue();
            if (spell != null) {
                ignoreSpellStatusUpdate = true;
                updateSpellWindow(spell);
            }
        }

        // Whether or not various views are visible
        if (savedInstanceState != null) {
            filterVisible = savedInstanceState.getBoolean(FILTER_VISIBLE_KEY, false);
            openedSpellSlotsFromFAB = savedInstanceState.getBoolean(SLOTS_OPENED_FAB_KEY, false);
        }

        // Set the toolbar as the app bar for the activity
        setSupportActionBar(binding.toolbar);

        // Listen for preference changes
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        // Set up back press behavior
        // First we make sure to close a drawer if it's open
        AndroidUtils.addOnBackPressedCallback(this, () -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else if (currentDestinationId() == id.homebrewManagementFragment) {
                final HomebrewManagementFragment fragment = (HomebrewManagementFragment) currentNavigationFragment();
                if (fragment != null && fragment.binding.speeddialHomebrewFab.isOpen()) {
                    fragment.binding.speeddialHomebrewFab.close();
                } else {
                    navController.popBackStack();
                }
            } else {
                navController.popBackStack();
            }
        }, 1000);

        // The DrawerLayout and the left navigation view
        drawerLayout = binding.drawerLayout;
        navView = binding.sideMenu;
        final NavigationView.OnNavigationItemSelectedListener navViewListener = menuItem -> {
            final int index = menuItem.getItemId();
            boolean close = false;
            if (index == id.subnav_charselect) {
                openCharacterSelection();
            } else if (index == id.subnav_spell_slots) {
                openedSpellSlotsFromFAB = false;
                globalNavigateTo(id.spellSlotManagerFragment);
                close = true;
            } else if (index == id.nav_feedback) {
                sendFeedback();
            } else if (index == id.nav_rate_us) {
                openPlayStoreForRating();
            } else if (index == id.nav_whats_new) {
                showUpdateDialog(false);
            } else if (index == id.nav_settings) {
                globalNavigateTo(id.settingsFragment);
                close = true;
            } else if (index == id.subnav_manage_homebrew) {
                if (onTablet) {
                    navController.navigate(id.homebrewManagementFragment);
                } else {
                    globalNavigateTo(id.homebrewManagementFragment);
                }
                close = true;
            } else if (index == id.subnav_export_list) {
                openSpellListExportDialog();
            } else if (statusFilterIDs.containsKey(index)) {
                final StatusFilterField sff = statusFilterIDs.get(index);
                sortFilterStatus.setStatusFilterField(sff);
                saveCharacterProfile();
                close = true;
            }
            saveSettings();

            // This piece of code makes the drawer close, if desired
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
                if (currentDestinationId() == id.spellTableFragment) {
                    final SpellTableFragment fragment = (SpellTableFragment) currentNavigationFragment();
                    if (fragment != null) {
                        fragment.stopScrolling();
                    }
                }
            }

        });

        swipeCloseListener = new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                if (!onTablet) {
                    closeSpellWindow();
                }
            }
        };

        // Set the hamburger button to open the left nav
        leftNavToggle = new ActionBarDrawerToggle(this, drawerLayout, string.left_navigation_drawer_open, string.left_navigation_drawer_closed);
        drawerLayout.addDrawerListener(leftNavToggle);
        setNavigationToHome();

        navController.addOnDestinationChangedListener((navController, navDestination, bundle) -> {
            saveCharacterProfile();
            updateFAB(navDestination);
            updateActionBar(navDestination);
            updateBottomBarVisibility(navDestination);

            final int destinationId = navDestination.getId();
            setAppBarScrollingAllowed(destinationId != id.settingsFragment);
        });

        if (!onTablet && binding.fab != null) {
            final String fabMovableKey = getString(string.fab_movable_key);
            binding.fab.setMovable(prefs.getBoolean(fabMovableKey, false));
        }

        //View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        /*int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/

        // If the character profile is null, we create one
        if (viewModel.getProfile() == null) {
            openCharacterCreationDialog();
        }

        setupSpellWindowCloseOnSwipe();

        // The right nav drawer often gets in the way of fast scrolling on a phone
        // Since we can open it from the action bar, we'll lock it closed from swiping
        if (!onTablet) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        }

        // Set the correct window status and view visibilities
        initializeWindow();

        // If we need to, open the update dialog
        showUpdateDialog(true);

        this.updateListCounts();

        // Define our spell list-related observers
        this.spellFilterStatusObserver = (status) -> this.updateListCounts();
        this.spellFilterEventObserver = (nothing) -> this.updateListCounts();
        this.favoriteListCountObserver = (favorite) -> this.updateMenuFavoriteCounts();
        this.preparedListCountObserver = (prepared) -> this.updateMenuPreparedCounts();
        this.knownListCountObserver = (known) -> this.updateMenuKnownCounts();

        // Connect any ViewModel observers
        final boolean showCounts = prefs.getBoolean(getString(string.show_list_counts), true);
        this.onShowListCountsUpdate(showCounts);
        viewModel.currentProfile().observe(this, this::setCharacterProfile);
        viewModel.currentSpell().observe(this, this::handleSpellUpdate);
        viewModel.spellTableCurrentlyVisible().observe(this, this::onSpellTableVisibilityChange);
        viewModel.currentToastEvent().observe(this, this::displayToastMessageFromEvent);
        viewModel.currentEditingSpell().observe(this, this::handleEditingSpellUpdate);

    }


    // Add actions to the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);

        // Associate searchable configuration with the SearchView
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchViewIcon = menu.findItem(id.action_search);
        searchView = (SearchView) searchViewIcon.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchViewIcon.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem menuItem) {
                return onTablet || !isSpellWindowOpen();
            }
        });

        baseFragment1Icon = menu.findItem(id.action_base_fragment_1);
        baseFragment2Icon = menu.findItem(id.action_base_fragment_2);
        infoMenuIcon = menu.findItem(id.action_info);
        editSlotsMenuIcon = menu.findItem(id.action_edit);
        manageSlotsMenuIcon = menu.findItem(id.action_slots);
        regainSlotsMenuIcon = menu.findItem(id.action_regain);
        deleteIcon = menu.findItem(id.action_delete);
        manageSourcesIcon = menu.findItem(id.action_sources);

        // Set up the SearchView functions
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                viewModel.setSearchQuery(text);
                if (currentDestinationId() == id.spellTableFragment) {
                    final SpellTableFragment fragment = (SpellTableFragment) currentNavigationFragment();
                    if (fragment != null) {
                        fragment.stopScrolling();
                    }
                }
                return true;
            }
        });
        updateActionBar(navController.getCurrentDestination());
        return true;
    }

    private void handleBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else if (currentDestinationId() == id.homebrewManagementFragment) {
            final HomebrewManagementFragment fragment = (HomebrewManagementFragment) currentNavigationFragment();
            if (fragment != null && fragment.binding.speeddialHomebrewFab.isOpen()) {
                fragment.binding.speeddialHomebrewFab.close();
            }
        }
    }

    private void updateActionBarBaseFragmentIcon(int index) {
        final int baseFragmentMenuID = baseFragmentMenuIDs.get(index);
        final int fragmentID = baseFragmentID(index);
        final MenuItem item = binding.toolbar.getMenu().findItem(baseFragmentMenuID);
        final Pair<Integer, Integer> data = actionBarData.get(fragmentID);
        if (data != null) {
            item.setTitle(data.getValue0());
            item.setIcon(data.getValue1());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemID = item.getItemId();
        if (itemID == id.action_base_fragment_1) {
            final int fragmentID = baseFragmentID(0);
            navigateToBaseFragment(fragmentID);
            return true;
        } else if (itemID == id.action_base_fragment_2) {
            final int fragmentID = baseFragmentID(1);
            navigateToBaseFragment(fragmentID);
            return true;
        } else if (itemID == id.action_info) {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
            return true;
        } else if (itemID == id.action_edit) {
            showSpellSlotAdjustTotalsDialog();
            return true;
        } else if (itemID == id.action_slots) {
            if (onTablet) {
                showSpellSlotsDialog();
            } else {
                globalNavigateTo(id.spellSlotManagerFragment);
            }
            return true;
        } else if (itemID == id.action_regain) {
            viewModel.getSpellSlotStatus().regainAllSlots();
            return true;
        } else if (itemID == id.action_delete) {
            // TODO: In the future this may need to be navigation-aware
            // as "delete" is in principle a pretty generic action
            final Spell spell = viewModel.currentEditingSpell().getValue();
            if (spell != null) {
                final DeleteSpellDialog dialog = new DeleteSpellDialog();
                dialog.setOnConfirm(navController::popBackStack);
                final Bundle args = new Bundle();
                args.putString(DeleteSpellDialog.NAME_KEY, spell.getName());
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), DELETE_SPELL_DIALOG_TAG);
            }
            return true;
        } else if (itemID == id.action_sources) {
            final SourceManagementDialog dialog = new SourceManagementDialog();
            dialog.show(getSupportFragmentManager(), MANAGE_SOURCES_DIALOG_TAG);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void initializeWindow() {
        updateSideMenuItemsVisibility();
        setupRightNav();
        setupFAB();
        setupBottomNavBar();
        setupSideMenu();
    }

    private void setLeftDrawerLocked(boolean lock) {
        final int lockSetting = lock ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED;
        drawerLayout.setDrawerLockMode(lockSetting, GravityCompat.START);
    }

    private void setRightDrawerLocked(boolean lock) {
        final int lockSetting = lock ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED;
        drawerLayout.setDrawerLockMode(lockSetting, GravityCompat.END);
    }

    private void closeLeftDrawer() { drawerLayout.closeDrawer(GravityCompat.START); }
    private void closeRightDrawer() { drawerLayout.closeDrawer(GravityCompat.END); }


    private void showSpellSlotAdjustTotalsDialog() {
        final SpellSlotStatus spellSlotStatus = viewModel.getSpellSlotStatus();
        final Bundle args = new Bundle();
        args.putParcelable(SpellSlotAdjustTotalsDialog.SPELL_SLOT_STATUS_KEY, spellSlotStatus);
        final SpellSlotAdjustTotalsDialog dialog = new SpellSlotAdjustTotalsDialog();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), SPELL_SLOT_ADJUST_TOTALS_TAG);
    }

    private void showSpellSlotsDialog() {
        final SpellSlotManagerDialog dialog = new SpellSlotManagerDialog();
        dialog.show(getSupportFragmentManager(), SPELL_SLOTS_DIALOG_TAG);
    }

    private void toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void setNavigationToBack() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_action_back);
        binding.toolbar.setNavigationOnClickListener((v) -> navController.popBackStack());
    }

    private Fragment currentNavigationFragment() {
        return navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
    }

    private int currentDestinationId() {
        final NavDestination destination = navController.getCurrentDestination();
        return (destination != null) ? destination.getId() : id.null_id;
    }

    private void globalNavigateTo(int destinationId) {
        navController.navigate(destinationId);
    }

    private void navigateToSpellWindowFragment() {
        globalNavigateTo(id.spellWindowFragment);
    }

    private void navigateToSpellTableFragment() {
        // NB: This should only ever be called when we're in the sort/filter fragment
        if (currentDestinationId() == id.sortFilterFragment) {
            navController.navigate(id.action_sortFilterFragment_to_spellTableFragment);
        }
    }

    private void navigateToSortFilterFragment() {
        if (onTablet) {
            globalNavigateTo(id.sortFilterFragment);
        } else if (currentDestinationId() == id.spellTableFragment) {
            navController.navigate(id.action_spellTableFragment_to_sortFilterFragment);
        }
    }

    private void navigateToHomebrewFragment() {
        if (onTablet) {
            final NavDestination destination = navController.getCurrentDestination();
            final int destinationID = destination.getId();
            if (destinationID == id.spellWindowFragment) {
                navController.navigate(id.action_spellWindowFragment_to_homebrewManagementFragment);
            } else if (destinationID == id.sortFilterFragment) {
                navController.navigate(id.action_sortFilterFragment_to_homebrewManagementFragment);
            }
        } else {
            globalNavigateTo(id.homebrewManagementFragment);
        }
    }

    private void navigateToBaseFragment(int destinationID) {
        if (destinationID == id.spellWindowFragment) {
            navigateToSpellWindowFragment();
        } else if (destinationID == id.spellTableFragment) {
            navigateToSpellTableFragment();
        } else if (destinationID == id.sortFilterFragment) {
            navigateToSortFilterFragment();
        } else if (destinationID == id.homebrewManagementFragment) {
            navigateToHomebrewFragment();
        }
    }

    private int baseFragmentID(int index) {
        final int currentID = currentDestinationId();
        int count = 0;
        for (final int id : baseFragments) {
            if (id == currentID) { continue; }
            if (count == index) { return id; }
            count++;
        }
        return -1;
    }

    private void setNavigationToHome() {
        binding.toolbar.setNavigationIcon(drawable.ic_hamburger);
        binding.toolbar.setNavigationOnClickListener((v) -> this.toggleDrawer());
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

    private void removeFragment(Fragment fragment, boolean commitNow) {
        final FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .remove(fragment);
                //.commitAllowingStateLoss();
        if (commitNow) {
            transaction.commitNow();
        } else {
            transaction.commit();
        }
    }

    private void hideFragment(Fragment fragment, Runnable onCommit) {
        getSupportFragmentManager()
            .beginTransaction()
            .hide(fragment)
            .runOnCommit(onCommit)
            .commit();
    }

    private void showFragment(Fragment fragment, Runnable onCommit) {
        getSupportFragmentManager()
            .beginTransaction()
            .show(fragment)
            .runOnCommit(onCommit)
            .commit();
    }

    private void showFragment(Fragment fragment) { showFragment(fragment, () -> {}); }

    @Override
    public void onPause() {
        //viewModel.saveCurrentProfile();
        //viewModel.saveSettings();
        super.onPause();
    }

    @Override
    public void onStop() {
        //viewModel.saveCurrentProfile();
        //viewModel.saveSettings();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (searchView != null) {
            final CharSequence searchString = searchView.getQuery();
            if (searchString == null || !searchString.equals(viewModel.getSearchQuery())) {
                viewModel.setSearchQuery(searchView.getQuery());
                viewModel.setSortNeeded();
                viewModel.setFilterNeeded();
            }
        }
    }

    @Override
    public void onDestroy() {
        //viewModel.saveCurrentProfile();
        //viewModel.saveSettings();
        super.onDestroy();
    }

    // Necessary for handling rotations
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FILTER_VISIBLE_KEY, filterVisible);
        outState.putBoolean(SLOTS_OPENED_FAB_KEY, openedSpellSlotsFromFAB);
        viewModel.saveCurrentProfile();
        viewModel.saveSettings();
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
            final boolean oneChecked = menu.findItem(id.nav_favorites).isChecked() || menu.findItem(id.nav_known).isChecked() || menu.findItem(id.nav_prepared).isChecked();

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
            filterVisible = false;
        } else {
            final SpellWindowFragment fragment = new SpellWindowFragment();
            // replaceFragment(id.phone_fullscreen_fragment_container, fragment, SPELL_WINDOW_FRAGMENT_TAG, true);
        }

    }

    void openSpellPopup(View view, Spell spell) {
        final SpellStatusPopup ssp = new SpellStatusPopup(this, spell);
        ssp.showUnderView(view);
    }

    private void setupFAB() {
        if (onTablet || binding.fab == null) { return; }
        binding.fab.setOnClickListener((v) -> {
            openedSpellSlotsFromFAB = true;
            try {
                fabCenterReveal = new CenterReveal(binding.fab, null);
                fabCenterReveal.start(() -> globalNavigateTo(id.spellSlotManagerFragment));
            } catch (Exception e) {
                globalNavigateTo(id.spellSlotManagerFragment);
            }
        });
    }

    private void setupRightNav() {

        // Get the right navigation view and the ExpandableListView
        //rightNavView = amBinding.rightMenu;
        rightExpLV = binding.navRightExpandable;

        // Get the list of group names, as an Array
        // The group names are the headers in the expandable list
        final List<String> rightNavGroups = Arrays.asList(getResources().getStringArray(array.right_group_names));

        // Get the names of the group elements, as Arrays
        final List<String[]> groups = new ArrayList<>();
        groups.add(getResources().getStringArray(array.basics_items));
        groups.add(getResources().getStringArray(array.casting_spell_items));
        final String[] casterNames = DisplayUtils.getDisplayNames(this, CasterClass.class);
        groups.add(casterNames);

        // For each group, get the text that corresponds to each child
        // Here, entries with the same index correspond to one another
        final List<Integer> basicsIDs = new ArrayList<>(Arrays.asList(string.what_is_a_spell, string.spell_level_info,
                string.known_and_prepared_spells, string.the_schools_of_magic, string.spell_slots_info, string.cantrips,
                string.rituals, string.the_weave_of_magic));
        final List<Integer> castingSpellIDs = new ArrayList<>(Arrays.asList(string.casting_time_info, string.range_info, string.components_info,
                string.duration_info, string.targets, string.areas_of_effect, string.saving_throws,
                string.attack_rolls, string.combining_magical_effects, string.casting_in_armor));
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
        final View rightHeaderView = getLayoutInflater().inflate(layout.right_expander_header, null);
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
            overridePendingTransition(anim.right_to_left_enter, anim.identity);

            return true;
        });
    }

    private void setActionBarBackButton() {
        leftNavToggle.setDrawerIndicatorEnabled(false);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setActionBarMenuButton() {
        leftNavToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
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

    // Saves the current settings to a file, in JSON format
    private boolean saveSettings() { return viewModel.saveSettings(); }
    boolean saveCharacterProfile() { return viewModel.saveCurrentProfile(); }


    private void setMenuTitleText(Menu menu, int itemID, CharSequence text) {
        final MenuItem menuItem = menu.findItem(itemID);
        final CharSequence title = text != null ? text : (menuItem.getTitle() != null ? menuItem.getTitle() : "");
        final SpannableString ss = new SpannableString(title);
        ss.setSpan(new ForegroundColorSpan(getColor(color.menuHeaderColor)), 0, ss.length(), 0);
        menuItem.setTitle(ss);
    }

    private void setSideMenuTitleText(int itemID, CharSequence text) {
        setMenuTitleText(binding.sideMenu.getMenu(), itemID, text);
    }


    private void setSideMenuCharacterName() {
        final String title = getString(R.string.prompt, getString(R.string.character), characterProfile.getName());
        setSideMenuTitleText(R.id.nav_character, title);
    }

    private void setSideMenuTextWithCount(int menuItemId, int textId, int count) {
        final String text = getString(string.name_with_count, getString(textId), Integer.toString(count));
        setSideMenuTitleText(menuItemId, text);
    }

    private void updateMenuFavoriteCounts() {
        final SpellFilterStatus status = viewModel.getSpellFilterStatus();
        final int count = status != null ? status.favoriteSpellIDs().size() : 0;
        setSideMenuTextWithCount(id.nav_favorites, string.favorites, count);
        setBottomNavTextWithCount(id.action_select_favorites, string.favorites, count);
    }
    
    private void updateMenuPreparedCounts() {
        final SpellFilterStatus status = viewModel.getSpellFilterStatus();
        final int count = status != null ? status.preparedSpellIDs().size() : 0;
        setSideMenuTextWithCount(id.nav_prepared, string.prepared, count);
        setBottomNavTextWithCount(id.action_select_prepared, string.prepared, count);
    }

    private void updateMenuKnownCounts() {
        final SpellFilterStatus status = viewModel.getSpellFilterStatus();
        final int count = status != null ? status.knownSpellIDs().size() : 0;
        setSideMenuTextWithCount(id.nav_known, string.known, count);
        setBottomNavTextWithCount(id.action_select_known, string.known, count);
    }

    private void setBottomNavItemText(int itemId, CharSequence text) {
        setMenuTitleText(binding.bottomNavBar.getMenu(), itemId, text);
    }

    private void setBottomNavTextWithCount(int itemId, int textId, int count) {
        final String text = getString(string.name_with_count, getString(textId), Integer.toString(count));
        setBottomNavItemText(itemId, text);
    }

    private void removeListCounts() {
        final String favorites = getString(string.favorites);
        final String prepared = getString(string.prepared);
        final String known = getString(string.known);
        setSideMenuTitleText(id.nav_favorites, favorites);
        setSideMenuTitleText(id.nav_prepared, prepared);
        setSideMenuTitleText(id.nav_known, known);
        setBottomNavItemText(id.action_select_favorites, favorites);
        setBottomNavItemText(id.action_select_prepared, prepared);
        setBottomNavItemText(id.action_select_known, known);
    }

    private void configureListCountListeners(boolean showCounts) {
        if (showCounts) {
            viewModel.currentSpellFilterStatus().observe(this, spellFilterStatusObserver);
            viewModel.spellFilterEvent().observe(this, spellFilterEventObserver);
            viewModel.currentSpellFavoriteLD().observe(this, favoriteListCountObserver);
            viewModel.currentSpellPreparedLD().observe(this, this.preparedListCountObserver);
            viewModel.currentSpellKnownLD().observe(this, this.knownListCountObserver);
        } else {
            viewModel.currentSpellFilterStatus().removeObserver(this.spellFilterStatusObserver);
            viewModel.spellFilterEvent().removeObserver(this.spellFilterEventObserver);
            viewModel.currentSpellFavoriteLD().removeObserver(this.favoriteListCountObserver);
            viewModel.currentSpellPreparedLD().removeObserver(this.preparedListCountObserver);
            viewModel.currentSpellKnownLD().removeObserver(this.knownListCountObserver);
        }
    }

    private void updateListCounts() {
        updateMenuFavoriteCounts();
        updateMenuPreparedCounts();
        updateMenuKnownCounts();
    }

    private void onShowListCountsUpdate(boolean showCounts) {
        this.configureListCountListeners(showCounts);
        if (showCounts) {
            updateListCounts();
        } else {
            removeListCounts();
        }
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
        if (cp != null) {
            spellFilterStatus = cp.getSpellFilterStatus();
            sortFilterStatus = cp.getSortFilterStatus();
            setSideMenuCharacterName();
            setFilterSettings();
            saveSettings();
            saveCharacterProfile();
        } else {
            spellFilterStatus = null;
            sortFilterStatus = null;
            openCharacterCreationDialog();
        }
    }
    // Sets the given character profile to be the active one
    void setCharacterProfile(CharacterProfile cp) {
        setCharacterProfile(cp, false);
    }

    // Opens a character creation dialog
    void openCharacterCreationDialog() {
        final CreateCharacterDialog dialog = new CreateCharacterDialog();
        dialog.show(getSupportFragmentManager(), CREATE_CHARACTER_TAG);
    }

    void openSpellListExportDialog() {
        final SpellListExportDialog dialog = new SpellListExportDialog();
        dialog.show(getSupportFragmentManager(), EXPORT_SPELL_LIST_TAG);
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
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(string.dev_email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(string.email_subject));
        try {
            startActivity(Intent.createChooser(intent, getString(string.send_email)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, getString(string.no_email_clients), Toast.LENGTH_SHORT).show();
        }
    }

    // Opens a character selection dialog
    void openCharacterSelection() {
        final CharacterSelectionDialog dialog = new CharacterSelectionDialog();
        dialog.show(getSupportFragmentManager(), SELECT_CHARACTER_TAG);
    }

    public SpellFilterStatus getSpellFilterStatus() { return spellFilterStatus; }
    public SortFilterStatus getSortFilterStatus() { return sortFilterStatus; }

    // This function takes care of any setup that's needed only on a tablet layout
    private void tabletSetup() {
        //spellWindowFragment = new SpellWindowFragment();
        //spellWindowFragment.updateSpell(null);
    }

    // If we're on a tablet, this function updates the spell window to match its status in the character profile
    // This is called after one of the spell list buttons is pressed for that spell in the main table
    void updateSpellWindow(Spell spell) {
        if (spellWindowFragment != null) {
            spellWindowFragment.updateSpell(spell);
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

    // This function clears the current focus ONLY IF the focused view is of the given type
    private <V extends View> void clearViewTypeFocus(Class<V> viewType) {
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

    private void updateSpellListMenuVisibility() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String bottomNav = getResources().getString(string.bottom_navbar);
        final String locationsKey = getString(string.spell_list_locations);
        final String locationsOption = prefs.getString(locationsKey, bottomNav);
        final boolean visible = !locationsOption.equals(bottomNav);
        final Menu menu = navView.getMenu();
        final int[] ids = { id.nav_all, id.nav_favorites, id.nav_prepared, id.nav_known };
        for (int id : ids) {
            menu.findItem(id).setVisible(visible);
        }
    }

    private void updateSpellSlotMenuVisibility() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String fab = getString(string.circular_button);
        final String locationsKey = getString(string.spell_slot_locations);
        final String locationOption = prefs.getString(locationsKey, fab);
        final boolean visible = !locationOption.equals(fab);
        final Menu menu = navView.getMenu();
        menu.findItem(id.subnav_spell_slots).setVisible(visible);
    }

    private void updateSideMenuItemsVisibility() {
        updateSpellListMenuVisibility();
        updateSpellSlotMenuVisibility();
    }

    private void updateFABVisibility(NavDestination destination) {
        if (destination == null) { return; }
        final int destinationId = destination.getId();
        if (onTablet || binding.fab == null) { return; }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String fab = getString(string.circular_button);
        final String sideDrawer = getString(string.side_drawer);
        final String locationOption = prefs.getString(getString(string.spell_slot_locations), fab);
        boolean visible = !locationOption.equals(sideDrawer);
        visible = visible && (destinationId == id.spellTableFragment);
        final int visibility = visible ? View.VISIBLE : View.GONE;
        binding.fab.setVisibility(visibility);
        if (visible && openedSpellSlotsFromFAB) {
            if (fabCenterReveal == null) {
                fabCenterReveal = new CenterReveal(binding.fab, null);
            }
            try {
                fabCenterReveal.reverse(null);
            } catch (Exception e) {
                binding.fab.resetPosition();
            }
            openedSpellSlotsFromFAB = false;
        }
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
        final Intent intent = new Intent(MainActivity.this, SpellCreationFragment.class);
        startActivityForResult(intent, RequestCodes.SPELL_CREATION_REQUEST);
        overridePendingTransition(anim.identity, android.R.anim.slide_in_left);
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
            if (!(view instanceof EditText)) {
                view.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent ev) {
        // We only listen for action up events for closing the spell window
        // If we listen to both up and down, we'll get two separate events
        // and the close animation doesn't get to finish[
        if (
                !onTablet &&
                spellWindowFragment != null &&
                ev.getKeyCode() == KeyEvent.KEYCODE_BACK &&
                ev.getAction() == KeyEvent.ACTION_UP
        ) {
            closeSpellWindow();
            return true;
        } else {
            return super.dispatchKeyEvent(ev);
        }
    }

    private void handleSpellUpdate(Spell spell) {

        // We want to do this no matter what
        if (onTablet && currentDestinationId() == id.spellWindowFragment) {
            final SpellWindowFragment fragment = (SpellWindowFragment) currentNavigationFragment();
            fragment.updateSpell(spell);
        }

        if (ignoreSpellStatusUpdate) {
            ignoreSpellStatusUpdate = false;
            return;
        }

        if (onTablet) {
            globalNavigateTo(id.spellWindowFragment);
        } else {
            openSpellWindow(spell);
            final boolean actualSpell = spell != null;
            setLeftDrawerLocked(actualSpell);
            if (actualSpell) {
                closeLeftDrawer();
                closeRightDrawer();
            }
        }
    }

    private void handleEditingSpellUpdate(Spell spell) {
        // TODO: Implement this
    }

    private void openSpellWindow(Spell spell) {
        if (onTablet || spell == null) { return; }
        final Bundle args = new Bundle();
        args.putParcelable(SpellWindowFragment.SPELL_KEY, spell);
        final boolean useExpanded = viewModel != null && viewModel.getUseExpanded();
        args.putBoolean(SpellWindowFragment.USE_EXPANDED_KEY, useExpanded);
        final FragmentTransaction transaction = getSupportFragmentManager()
            .beginTransaction()
            .setCustomAnimations(anim.right_to_left_enter, anim.identity);

        // The SpellWindowFragment won't be null when we're coming off a rotation
        // when we were inside a SpellWindowFragment
        if (spellWindowFragment == null) {
            transaction.add(id.phone_fullscreen_fragment_container, SpellWindowFragment.class, args, SPELL_WINDOW_FRAGMENT_TAG);
        }

        transaction.runOnCommit(() -> {
            this.spellWindowFragment = (SpellWindowFragment) getSupportFragmentManager().findFragmentByTag(SPELL_WINDOW_FRAGMENT_TAG);
            setupSpellWindowCloseOnSwipe();
        }).commit();
    }

    private void closeSpellWindow() {
        if (onTablet) { return; }
        getSupportFragmentManager()
            .beginTransaction()
            .setCustomAnimations(anim.identity, anim.left_to_right_exit)
            .remove(spellWindowFragment)
            .runOnCommit(() -> {
                viewModel.setCurrentSpell(null);
                final Handler handler = new Handler();
                // This delay matches the length of the transition animation
                handler.postDelayed(() -> spellWindowFragment = null, getResources().getInteger(integer.transition_duration));

                // If we're on one of the spell lists,
                // then re-filter to make sure that this spell should still be there
                if (sortFilterStatus.getStatusFilterField() != StatusFilterField.ALL) {
                    viewModel.setFilterNeeded();
                }
            })
            .commit();
    }

    private boolean isSpellWindowOpen() {
        return spellWindowFragment != null;
    }

    private void setAppBarScrollingAllowed(boolean allow) {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) binding.toolbar.getLayoutParams();
        final int flags = allow ? AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                               AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP |
                               AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS : 0;
        params.setScrollFlags(flags);
    }

    private void showUpdateDialog(boolean checkIfNecessary) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String key = "first_time_" + GlobalInfo.UPDATE_LOG_CODE;
        final List<String> characterNames = viewModel.currentCharacterNames().getValue();
        final boolean noCharacters = (characterNames == null) || characterNames.size() <= 0;
        final boolean toShow = !checkIfNecessary || !(prefs.contains(key) || noCharacters);
        if (toShow) {
            final int titleID = GlobalInfo.UPDATE_LOG_TITLE_ID;
            final int descriptionID = GlobalInfo.UPDATE_LOG_DESCRIPTION_ID;
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

    private boolean saveExternalJSONFile(JSONObject json) {
        // TODO: Implement this
        return true;
    }

    private void onSpellTableVisibilityChange(boolean visible) {
        // TODO: Add anything that we want to do here when the spell table visibility changes
    }

    private void setupSpellWindowCloseOnSwipe() {
        if (spellWindowFragment == null) { return; }
        final View view = spellWindowFragment.getScrollView();
        if (view == null) { return; }
        view.setOnTouchListener(swipeCloseListener);
    }

    private boolean shouldBottomNavBarBeVisible(NavDestination destination) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String sideDrawer = getResources().getString(string.side_drawer);
        final String bottomNav = getResources().getString(string.bottom_navbar);
        final String locationsKey = getString(string.spell_list_locations);
        final String locationOption = sharedPreferences.getString(locationsKey, bottomNav);
        boolean visible = !locationOption.equals(sideDrawer);
        if (!visible) { return false; }
        if (onTablet) {
            final int destinationId = destination.getId();
            return baseFragments.contains(destinationId);
        } else {
            return destination.getId() == id.spellTableFragment;
        }
    }

    void setupBottomNavBar() {
        final BottomNavigationView bottomNavBar = binding.bottomNavBar;
        final boolean bottomNavVisible = shouldBottomNavBarBeVisible(navController.getCurrentDestination());
        final int visibility = bottomNavVisible ? View.VISIBLE : View.GONE;
        bottomNavBar.setVisibility(visibility);
        bottomNavBar.setOnItemSelectedListener(item -> {
            final int id = item.getItemId();
            final SortFilterStatus sortFilterStatus = viewModel.getSortFilterStatus();
            if (sortFilterStatus == null) { return true; }
            StatusFilterField statusFilterField;
            if (id == R.id.action_select_favorites) {
                statusFilterField = StatusFilterField.FAVORITES;
            } else if (id == R.id.action_select_prepared) {
                statusFilterField = StatusFilterField.PREPARED;
            } else if (id == R.id.action_select_known) {
                statusFilterField = StatusFilterField.KNOWN;
            } else {
                statusFilterField = StatusFilterField.ALL;
            }
            sortFilterStatus.setStatusFilterField(statusFilterField);
            return true;
        });
    }

    private void setupSideMenu() {
        final int[] ids = new int[]{ R.id.nav_character, R.id.nav_update_title, R.id.nav_feedback_title };
        for (int id : ids) {
            setSideMenuTitleText(id, null);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(string.spell_slot_locations))) {
            updateFABVisibility(navController.getCurrentDestination());
            updateSpellSlotMenuVisibility();
        } else if (key.equals(getString(string.spell_list_locations))) {
            updateBottomBarVisibility(navController.getCurrentDestination());
            updateSpellListMenuVisibility();
        } else if (key.equals(getString(string.spell_language_key))) {
            final Locale locale = new Locale(sharedPreferences.getString(key, getString(string.english_code)));
            viewModel.updateSpellsForLocale(locale);
        } else if (key.equals(getString(string.fab_movable_key))) {
            final boolean movable = sharedPreferences.getBoolean(key, false);
            if (binding.fab != null) {
                binding.fab.setMovable(movable);
            }
        } else if (key.equals(getString(string.show_list_counts))) {
            final boolean showCounts = sharedPreferences.getBoolean(key, true);
            this.onShowListCountsUpdate(showCounts);
        }
    }

    private int actionBarTitleId(NavDestination destination) {
        // IDs are non-final in Gradle 8
        // so Android Studio warns against using a switch
        final int destinationId = destination.getId();
        if (destinationId == id.spellSlotManagerFragment) {
            return string.spell_slots_title;
        } else if (destinationId == id.settingsFragment) {
            return string.settings;
        } else if (destinationId == id.homebrewManagementFragment) {
            return string.homebrew_management_title;
        } else if (destinationId == id.spellCreationFragment) {
            // TODO: Not 100% satisfied with this
            // What would be better?
            return string.spell_creation;
        }
        return string.app_name;
    }

    private void updateActionBar(NavDestination destination) {
        final int destinationId = destination.getId();
        final boolean searchViewVisible = onTablet || destinationId == id.spellTableFragment;
        final boolean atBaseFragment = baseFragments.contains(destinationId);
        final boolean homebrewIconVisible = atBaseFragment && onTablet;
        final boolean fragmentIcon1Visible = atBaseFragment;
        final boolean fragmentIcon2Visible = atBaseFragment;
        final boolean infoIconVisible = atBaseFragment && destinationId != id.homebrewManagementFragment;
        final boolean editIconVisible = destinationId == id.spellSlotManagerFragment;
        final boolean regainIconVisible = destinationId == id.spellSlotManagerFragment;
        final boolean deleteIconVisible = destinationId == id.spellCreationFragment;
        final boolean manageSourcesIconVisible = destinationId == id.homebrewManagementFragment;
        final boolean manageSlotsIconVisible = onTablet && atBaseFragment;

        if (searchViewIcon != null) {
            searchViewIcon.setVisible(searchViewVisible);
        }
        if (baseFragment1Icon != null) {
            baseFragment1Icon.setVisible(fragmentIcon1Visible);
            updateActionBarBaseFragmentIcon(0);
        }
        if (baseFragment2Icon != null) {
            baseFragment2Icon.setVisible(fragmentIcon2Visible);
            updateActionBarBaseFragmentIcon(1);
        }
        if (editSlotsMenuIcon != null) {
            editSlotsMenuIcon.setVisible(editIconVisible);
        }
        if (infoMenuIcon != null) {
            infoMenuIcon.setVisible(infoIconVisible);
        }
        if (manageSlotsMenuIcon != null) {
            manageSlotsMenuIcon.setVisible(manageSlotsIconVisible);
        }
        if (regainSlotsMenuIcon != null) {
            regainSlotsMenuIcon.setVisible(regainIconVisible);
        }
        if (deleteIcon != null) {
            deleteIcon.setVisible(deleteIconVisible);
        }
        if (homebrewIcon != null) {
            homebrewIcon.setVisible(homebrewIconVisible);
        }
        if (manageSourcesIcon != null) {
            manageSourcesIcon.setVisible(manageSourcesIconVisible);
        }

        if (!onTablet && searchView != null && searchView.isIconified()) {
            searchViewIcon.collapseActionView();
        }

        boolean navigationToHome = destinationId == id.sortFilterFragment;
        if (onTablet) {
            navigationToHome |= (destinationId == id.spellWindowFragment) || (destinationId == id.homebrewManagementFragment);
        } else {
            navigationToHome |= (destinationId == id.spellTableFragment);
        }

        if (navigationToHome) {
            setNavigationToHome();
        } else {
            setNavigationToBack();
        }

        final int title = actionBarTitleId(destination);
        binding.toolbar.setTitle(title);
    }

    private void updateBottomBarVisibility(NavDestination destination) {
        final boolean visible = shouldBottomNavBarBeVisible(destination);
        final BottomNavigationView bottomBar = binding.bottomNavBar;
        bottomBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void updateDrawerStatus(NavDestination destination) {
        boolean lock;
        final int destinationId = destination.getId();
        if (onTablet) {
            lock = destinationId == id.settingsFragment;
        } else {
            lock = Arrays.asList(id.settingsFragment, id.spellSlotManagerFragment, id.homebrewManagementFragment, id.spellCreationFragment).contains(destinationId);
        }

        if (lock) {
            closeLeftDrawer();
            closeRightDrawer();
        }
        setLeftDrawerLocked(lock);

        boolean lockRightDrawer = lock || !onTablet;
        setRightDrawerLocked(lockRightDrawer);
    }

    private void updateFAB(NavDestination destination) {
        updateFABVisibility(destination);
    }

    private List<SpellSlotManagerFragment> getSpellSlotFragments() {
        final List<SpellSlotManagerFragment> fragments = new ArrayList<>();
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof SpellSlotManagerFragment) {
                fragments.add((SpellSlotManagerFragment) f);
            }
        }
        return fragments;
    }

    private List<SettingsFragment> getSettingsFragments() {
        final List<SettingsFragment> fragments = new ArrayList<>();
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof SettingsFragment) {
                fragments.add((SettingsFragment) f);
            }
        }
        return fragments;
    }


    private void displayToastMessageFromEvent(Event<String> toastEvent) {
        final String message = toastEvent.getContentIfHandled();
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }


//    @Override
//    public void onBackStackChanged() {
//        shouldDisplayHomeUp();
//    }
//
//    public void shouldDisplayHomeUp(){
//        // Enable Up button only if there are entries in the back stack
//        boolean canGoBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
//        getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        //This method is called when the up button is pressed. Just the pop back stack.
//        getSupportFragmentManager().popBackStack();
//        return true;
//    }
}
