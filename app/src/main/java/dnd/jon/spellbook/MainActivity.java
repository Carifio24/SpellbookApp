package dnd.jon.spellbook;

import static dnd.jon.spellbook.R.*;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.EditText;
import android.content.Intent;
import android.view.inputmethod.InputMethodManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import com.getkeepsafe.taptargetview.TapTargetSequence;

import dnd.jon.spellbook.databinding.ActivityMainBinding;
import dnd.jon.spellbook.databinding.SortFilterLayoutBinding;
import dnd.jon.spellbook.databinding.SpellTableBinding;
import dnd.jon.spellbook.databinding.SpellWindowBinding;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;
import uk.co.deanwild.materialshowcaseview.target.Target;
import uk.co.deanwild.materialshowcaseview.target.ViewTarget;

public class MainActivity extends AppCompatActivity
        //implements FragmentManager.OnBackStackChangedListener
    implements SharedPreferences.OnSharedPreferenceChangeListener
{

    private enum WindowStatus {
        TABLE,
        SPELL,
        FILTER,
        SETTINGS,
        SLOTS,
        INFO,
        SPELL_CREATION,
    }

    private WindowStatus windowStatus;
    private WindowStatus prevWindowStatus;

    // Fragment tags
    private static final String SPELL_TABLE_FRAGMENT_TAG = "SpellTableFragment";
    private static final String SORT_FILTER_FRAGMENT_TAG = "SortFilterFragment";
    private static final String SPELL_WINDOW_FRAGMENT_TAG = "SpellWindowFragment";
    private static final String SPELL_SLOTS_FRAGMENT_TAG = "SpellSlotsFragment";
    private static final String SETTINGS_FRAGMENT_TAG = "SettingsFragment";
    private static final String SPELL_SLOTS_DIALOG_TAG = "SpellSlotsDialog";

    // Tags for dialogs
    private static final String CREATE_CHARACTER_TAG = "createCharacter";
    private static final String SELECT_CHARACTER_TAG = "selectCharacter";
    private static final String SPELL_SLOT_ADJUST_TOTALS_TAG = "adjustSpellSlotTotals";

    // Keys for Bundles
    private static final String FILTER_VISIBLE_KEY = "FILTER_VISIBLE";
    private static final String WINDOW_STATUS_KEY = "WINDOW_STATUS";
    private static final String PREV_WINDOW_STATUS_KEY = "PREV_WINDOW_STATUS";

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
    private MenuItem infoMenuIcon;
    private MenuItem editSlotsMenuIcon;
    private MenuItem manageSlotsMenuIcon;
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

    // The center reveal for the FAB
    private CenterReveal fabCenterReveal;

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
       put(id.nav_all, StatusFilterField.ALL);
       put(id.nav_favorites, StatusFilterField.FAVORITES);
       put(id.nav_prepared, StatusFilterField.PREPARED);
       put(id.nav_known, StatusFilterField.KNOWN);
    }};

    // For listening to keyboard visibility events
    //private Unregistrar unregistrar;

    // Whether or not this is running on a tablet
    private boolean onTablet;

    // For view and data binding
    private ActivityMainBinding binding;
    private SpellTableFragment spellTableFragment;
    private SortFilterFragment sortFilterFragment;
    private SpellWindowFragment spellWindowFragment;
    private SpellSlotManagerFragment spellSlotFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the main activity binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        spellTableFragment = (SpellTableFragment) getSupportFragmentManager().findFragmentByTag(SPELL_TABLE_FRAGMENT_TAG);
        sortFilterFragment = (SortFilterFragment) getSupportFragmentManager().findFragmentByTag(SORT_FILTER_FRAGMENT_TAG);
        spellWindowFragment = (SpellWindowFragment) getSupportFragmentManager().findFragmentByTag(SPELL_WINDOW_FRAGMENT_TAG);
        settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(SETTINGS_FRAGMENT_TAG);

        // Should be null unless we're coming off a rotation where it was open
        spellSlotFragment = (SpellSlotManagerFragment) getSupportFragmentManager().findFragmentByTag(SPELL_SLOTS_FRAGMENT_TAG);

        // Are we on a tablet or not?
        // If we're on a tablet, do the necessary setup
        onTablet = getResources().getBoolean(bool.isTablet);
        if (onTablet) { tabletSetup(); }

        // Get the spell view model
        viewModel = new ViewModelProvider(this).get(SpellbookViewModel.class);

        // Any view model observers that we need
        viewModel.currentProfile().observe(this, this::setCharacterProfile);
        viewModel.currentSpell().observe(this, this::handleSpellUpdate);

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

        // Whether or not various views are visible
        if (savedInstanceState != null) {
            savedInstanceState.getBoolean(FILTER_VISIBLE_KEY, false);
            windowStatus = (WindowStatus) savedInstanceState.getSerializable(WINDOW_STATUS_KEY);
            prevWindowStatus = (WindowStatus) savedInstanceState.getSerializable(PREV_WINDOW_STATUS_KEY);
        }

        // Set the toolbar as the app bar for the activity
        setSupportActionBar(binding.toolbar);

        // Listen for preference changes
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        // The DrawerLayout and the left navigation view
        drawerLayout = binding.drawerLayout;
        navView = binding.sideMenu;
        final NavigationView.OnNavigationItemSelectedListener navViewListener = menuItem -> {
            final int index = menuItem.getItemId();
            boolean close = false;
            if (index == id.subnav_charselect) {
                openCharacterSelection();
            } else if (index == id.nav_feedback) {
                sendFeedback();
            } else if (index == id.nav_rate_us) {
                openPlayStoreForRating();
            } else if (index == id.nav_whats_new) {
                showUpdateDialog(false);
            } else if (index == id.nav_settings) {
                updateWindowStatus(WindowStatus.SETTINGS);
                close = true;
            } else if (index == id.nav_tutorial) {
                openTutorial();
                close = true;
            //} else if (index == R.id.create_a_spell) {
            //    openSpellCreationWindow();
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
        leftNavToggle = new ActionBarDrawerToggle(this, drawerLayout, string.left_navigation_drawer_open, string.left_navigation_drawer_closed);
        drawerLayout.addDrawerListener(leftNavToggle);
        setNavigationToHome();

        // Back stack listener
        //getSupportFragmentManager().addOnBackStackChangedListener(this);

        // Set up various views
        setupRightNav();
        setupFAB();
        setupBottomNavBar();
        setupSideMenu();

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

        viewModel.spellTableCurrentlyVisible().observe(this, this::onSpellTableVisibilityChange);

        // The right nav drawer often gets in the way of fast scrolling on a phone
        // Since we can open it from the action bar, we'll lock it closed from swiping
        if (!onTablet) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        }

        // Set the correct window status and view visibilities
        initializeWindow();

        // If we need to, open the update dialog
        showUpdateDialog(true);

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
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                return onTablet || !isSpellWindowOpen();
            }
        });

        filterMenuIcon = menu.findItem(id.action_filter);
        infoMenuIcon = menu.findItem(id.action_info);
        editSlotsMenuIcon = menu.findItem(id.action_edit);
        manageSlotsMenuIcon = menu.findItem(id.action_slots);

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

        updateActionBar();

        return true;
    }

    // To handle actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemID = item.getItemId();
        if (itemID == id.action_filter) {
            final WindowStatus notFilterStatus = onTablet ? WindowStatus.SPELL : WindowStatus.TABLE;
            final WindowStatus newStatus = (windowStatus == WindowStatus.FILTER) ? notFilterStatus : WindowStatus.FILTER;
            updateWindowStatus(newStatus);
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
                updateWindowStatus(WindowStatus.SLOTS);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void initializeWindow() {
        Fragment toHide;
        if (windowStatus == null) {
            final WindowStatus initialWindowStatus = onTablet ? WindowStatus.SPELL : WindowStatus.TABLE;
            toHide = sortFilterFragment;
            windowStatus = initialWindowStatus;
            hideFragment(toHide);
        }
//        else {
//            WindowStatus mainStatus;
//            if (isMainViewStatus(windowStatus)) {
//                mainStatus = windowStatus;
//            } else if (isMainViewStatus(prevWindowStatus)) {
//                mainStatus = prevWindowStatus;
//            } else {
//                mainStatus = onTablet ? WindowStatus.SPELL : WindowStatus.TABLE;
//            }
//            switch (mainStatus) {
//                case FILTER:
//                    toHide = onTablet ? spellWindowFragment : spellTableFragment;
//                    break;
//                case SPELL:
//                    if (onTablet) {
//                        toHide = sortFilterFragment;
//                    } else {
//                        toHide = (prevWindowStatus == WindowStatus.TABLE) ? sortFilterFragment : spellTableFragment;
//                    }
//                    break;
//                case TABLE:
//                default:
//                    toHide = sortFilterFragment;
//            }
//        }
        //hideFragment(toHide);
        updateFabVisibility();
        updateBottomBarVisibility();
        updateSideMenuItemsVisibility();
    }

    private void setLeftDrawerLocked(boolean lock) {
        final int lockSetting = lock ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED;
        drawerLayout.setDrawerLockMode(lockSetting, GravityCompat.START);
    }

    private void setRightDrawerLocked(boolean lock) {
        final int lockSetting = lock ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED;
        drawerLayout.setDrawerLockMode(lockSetting, GravityCompat.END);
    }

    private void openLeftDrawer() { drawerLayout.openDrawer(GravityCompat.START); }
    private void closeLeftDrawer() { drawerLayout.closeDrawer(GravityCompat.START); }
    private void openRightDrawer() { drawerLayout.openDrawer(GravityCompat.END); }
    private void closeRightDrawer() { drawerLayout.closeDrawer(GravityCompat.END); }


    private void showSpellSlotAdjustTotalsDialog() {
        if (spellSlotFragment != null) {
            final SpellSlotStatus spellSlotStatus = viewModel.getSpellSlotStatus();
            final Bundle args = new Bundle();
            args.putParcelable(SpellSlotAdjustTotalsDialog.SPELL_SLOT_STATUS_KEY, spellSlotStatus);
            final SpellSlotAdjustTotalsDialog dialog = new SpellSlotAdjustTotalsDialog();
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), SPELL_SLOT_ADJUST_TOTALS_TAG);
        }
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
        binding.toolbar.setNavigationOnClickListener((v) -> this.onBackPressed());
    }

    private void setNavigationToHome() {
        binding.toolbar.setNavigationIcon(drawable.ic_hamburger);
        binding.toolbar.setNavigationOnClickListener((v) -> this.toggleDrawer());
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

    private void removeFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(fragment)
                //.commitAllowingStateLoss();
                .commit();
    }

    private void hideFragment(Fragment fragment, Runnable onCommit) {
        getSupportFragmentManager()
            .beginTransaction()
            .hide(fragment)
            .runOnCommit(onCommit)
            .commit();
    }

    private void hideFragment(Fragment fragment) { hideFragment(fragment, () -> {});}

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
        outState.putSerializable(WINDOW_STATUS_KEY, windowStatus);
        outState.putSerializable(PREV_WINDOW_STATUS_KEY, prevWindowStatus);
        viewModel.saveCurrentProfile();
        viewModel.saveSettings();
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
            final WindowStatus backStatus = backStatus(windowStatus);
            if (backStatus != null) {
                updateWindowStatus(backStatus);
            } else {
                super.onBackPressed();
            }
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
            //spellWindowFragment.updateUseExpanded(sortFilterStatus.getUseTashasExpandedLists());
            filterVisible = false;
            //updateWindowVisibilities();
        } else {
            final SpellWindowFragment fragment = new SpellWindowFragment();
            replaceFragment(id.phone_fullscreen_fragment_container, fragment, SPELL_WINDOW_FRAGMENT_TAG, true);
        }

    }

    void openSpellPopup(View view, Spell spell) {
        final SpellStatusPopup ssp = new SpellStatusPopup(this, spell);
        ssp.showUnderView(view);
    }

    private void openSpellSlotsFragment() {
        spellSlotFragment = new SpellSlotManagerFragment();
        binding.appBarLayout.setExpanded(true, false);
        editSlotsMenuIcon.setVisible(true);
        final int containerID = onTablet ? id.tablet_detail_container : id.phone_fragment_container;
        getSupportFragmentManager()
            .beginTransaction()
            .add(containerID, spellSlotFragment, SPELL_SLOTS_FRAGMENT_TAG)
            .commit();

        if (!onTablet) {
            hideFragment(spellTableFragment, () -> {
                if (binding.bottomNavBar != null) {
                    binding.bottomNavBar.setVisibility(View.GONE);
                }
            });
        }

        // Adjust icons on the Action Bar
        //binding.toolbar.setNavigationIcon(drawable.ic_action_back);
        infoMenuIcon.setVisible(false);
        filterMenuIcon.setVisible(false);
        searchViewIcon.setVisible(false);
        manageSlotsMenuIcon.setVisible(false);
        editSlotsMenuIcon.setVisible(true);
    }

    private void closeSpellSlotsFragment() {
        if (onTablet) {
            replaceFragment(id.tablet_detail_container, spellTableFragment, SPELL_TABLE_FRAGMENT_TAG, false);
        } else {
            System.out.println("Here");
            removeFragment(spellSlotFragment);
            spellSlotFragment = null;
            showFragment(spellTableFragment);
        }
    }

    private void setupFAB() {
        if (onTablet) { return; }
        binding.fab.setOnClickListener((v) -> {
            fabCenterReveal = new CenterReveal(binding.fab, binding.phoneFragmentContainer);
            fabCenterReveal.start(() -> updateWindowStatus(WindowStatus.SLOTS));
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

    private void setSideMenuTitleText(int itemID, CharSequence text) {
        final Menu menu = binding.sideMenu.getMenu();
        final MenuItem menuItem = menu.findItem(itemID);
        final CharSequence title = text != null ? text : (menuItem.getTitle() != null ? menuItem.getTitle() : "");
        final SpannableString ss = new SpannableString(title);
        ss.setSpan(new ForegroundColorSpan(SpellbookUtils.defaultColor), 0, ss.length(), 0);
        menuItem.setTitle(ss);
    }


    private void setSideMenuCharacterName() {
        final String title = getString(R.string.prompt, getString(R.string.character), characterProfile.getName());
        setSideMenuTitleText(R.id.nav_character, title);
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
        dialog.show(getSupportFragmentManager(), CREATE_CHARACTER_TAG);
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
        spellWindowFragment.updateSpell(null);
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

    private void showSpellTable() {
        if (onTablet) {
            return;
        }

        // Clear the focus from an EditText, if that's where it is
        // since they have an OnFocusChangedListener
        // We want to do this BEFORE we sort/filter so that any changes can be made to the CharacterProfile
        final View view = getCurrentFocus();
        if (view != null) {
            hideSoftKeyboard(view, this);
        }
        showFragment(spellTableFragment);
    }

    private void updateSpellListMenuVisibility() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String bottomNav = getResources().getString(string.bottom_navbar);
        final String locationsKey = getString(string.spell_list_locations);
        final String locationsOption = prefs.getString(locationsKey, bottomNav);
        System.out.println(locationsOption);
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

    private void updateFabVisibility() {
        if (onTablet) { return; }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String fab = getString(string.circular_button);
        final String sideDrawer = getString(string.side_drawer);
        final String locationOption = prefs.getString(getString(string.spell_slot_locations), fab);
        boolean visible = !locationOption.equals(sideDrawer);
        visible = visible && ((windowStatus == WindowStatus.TABLE) || (onTablet && windowStatus == WindowStatus.SPELL));
        final int visibility = visible ? View.VISIBLE : View.GONE;
        binding.fab.setVisibility(visibility);
        if (visible && prevWindowStatus == WindowStatus.SLOTS) {
            if (fabCenterReveal == null) {
                fabCenterReveal = new CenterReveal(binding.fab, binding.phoneFragmentContainer);
            }
            fabCenterReveal.reverse(null);
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
        if (!onTablet && spellWindowFragment != null && ev.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            closeSpellWindow();
            return true;
        } else {
            return super.dispatchKeyEvent(ev);
        }
    }

    private void handleSpellUpdate(Spell spell) {
        if (onTablet) {
            spellWindowFragment.updateSpell(spell);
            updateWindowStatus(WindowStatus.SPELL);
        } else {
            openSpellWindow(spell);
        }
    }

    private void openSpellWindow(Spell spell, boolean commitNow) {
        if (onTablet || spell == null) { return; }
        final Bundle args = new Bundle();
        args.putParcelable(SpellWindowFragment.SPELL_KEY, spell);
        args.putBoolean(SpellWindowFragment.USE_EXPANDED_KEY, viewModel.getUseExpanded());
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
        });
        if (commitNow) {
            transaction.commitNow();
        } else {
            transaction.commit();
        }
    }

    private void openSpellWindow(Spell spell) { openSpellWindow(spell, false); }

    private void closeSpellWindow(boolean commitNow) {
        if (onTablet) { return; }
        final FragmentTransaction transaction = getSupportFragmentManager()
            .beginTransaction()
            .setCustomAnimations(anim.identity, anim.left_to_right_exit)
            .remove(spellWindowFragment)
            .runOnCommit(() -> {
                viewModel.setCurrentSpell(null);
                final Handler handler = new Handler();
                // This delay matches the length of the transition animation
                handler.postDelayed(() -> spellWindowFragment = null, getResources().getInteger(integer.transition_duration));
            });
        if (commitNow) {
            transaction.commitNow();
        } else {
            transaction.commit();
        }
    }

    private void closeSpellWindow() { closeSpellWindow(false); }

    private boolean isSpellWindowOpen() {
        return spellWindowFragment != null;
    }

    private List<FragmentContainerView> visibleMainContainers(WindowStatus status) {
        final List<FragmentContainerView> containers = new ArrayList<>();
        if (onTablet || (status == WindowStatus.TABLE)) {
            containers.add(binding.spellTableContainer);
        }
        if (status == WindowStatus.SPELL) {
            containers.add(binding.spellWindowContainer);
        } else if (status == WindowStatus.FILTER) {
            containers.add(binding.sortFilterContainer);
        }
        return containers;
    }

    private void setAppBarScrollingAllowed(boolean allow) {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) binding.toolbar.getLayoutParams();
        final int flags = allow ? AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                               AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP |
                               AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS : 0;
        params.setScrollFlags(flags);
    }

    private void openSettings() {
        final int containerID = id.settings_container;
        final List<FragmentContainerView> viewsToDisable = visibleMainContainers(windowStatus);
        final FragmentTransaction transaction = getSupportFragmentManager()
            .beginTransaction()
            .setCustomAnimations(anim.left_to_right_enter, anim.identity)
            .add(containerID, SettingsFragment.class, null, SETTINGS_FRAGMENT_TAG)
            .hide(spellTableFragment);
        if (onTablet) {
            final Fragment fragment = (prevWindowStatus == WindowStatus.FILTER) ? sortFilterFragment : spellWindowFragment;
            transaction.hide(fragment);
        }
        transaction.runOnCommit(() -> {
                this.settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(SETTINGS_FRAGMENT_TAG);
                for (FragmentContainerView container : viewsToDisable) {
                    container.setEnabled(false);
                }
            })
            .commit();
        if (!onTablet) {
            setAppBarScrollingAllowed(false);
        }
    }

    private void closeSettings() {
        final List<FragmentContainerView> viewsToEnable = visibleMainContainers(prevWindowStatus);
        final FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(anim.identity, anim.right_to_left_exit)
                .remove(settingsFragment)
                .show(spellTableFragment);
        if (onTablet) {
            final Fragment fragment = (windowStatus == WindowStatus.FILTER) ? sortFilterFragment : spellWindowFragment;
            transaction.show(fragment);
        }
        transaction.runOnCommit(() -> {
                this.settingsFragment = null;
                for (FragmentContainerView container : viewsToEnable) {
                    container.setEnabled(true);
                }
            })
            .commit();
        if (!onTablet) {
            setAppBarScrollingAllowed(true);
        }
    }

    private void showUpdateDialog(boolean checkIfNecessary) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String key = "first_time_" + GlobalInfo.VERSION_CODE;
        final List<String> characterNames = viewModel.currentCharacterNames().getValue();
        final boolean noCharacters = (characterNames == null) || characterNames.size() <= 0;
        final boolean toShow = !checkIfNecessary || !(prefs.contains(key) || noCharacters);
        if (toShow) {
            final int titleID = string.update_02_11_title;
            final int descriptionID = string.update_02_11_description;
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

    private boolean shouldBottomNavBarBeVisible() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String sideDrawer = getResources().getString(string.side_drawer);
        final String bottomNav = getResources().getString(string.bottom_navbar);
        final String locationsKey = getString(string.spell_list_locations);
        final String locationOption = sharedPreferences.getString(locationsKey, bottomNav);
        boolean visible = !locationOption.equals(sideDrawer);
        if (!visible) { return false; }
        if (onTablet) {
            return (windowStatus == WindowStatus.SPELL) || (windowStatus == WindowStatus.FILTER);
        } else {
            return windowStatus == WindowStatus.TABLE;
        }
    }

    void setupBottomNavBar() {
        final BottomNavigationView bottomNavBar = binding.bottomNavBar;
        if (bottomNavBar == null) { return; }
        final boolean bottomNavVisible = shouldBottomNavBarBeVisible();
        final int visibility = bottomNavVisible ? View.VISIBLE : View.GONE;
        bottomNavBar.setVisibility(visibility);
        bottomNavBar.setOnItemSelectedListener(item -> {
            final int id = item.getItemId();
            final SortFilterStatus sortFilterStatus = viewModel.getSortFilterStatus();
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
            updateFabVisibility();
            updateSpellSlotMenuVisibility();
        } else if (key.equals(getString(string.spell_list_locations))) {
            updateBottomBarVisibility();
            updateSpellListMenuVisibility();
        }
    }

    private void updateActionBar() {
        final boolean searchViewVisible = onTablet || (windowStatus == WindowStatus.TABLE);
        final boolean filterIconVisible = (windowStatus == WindowStatus.TABLE) ||
                                          (windowStatus == WindowStatus.FILTER) ||
                                          ((windowStatus == WindowStatus.SPELL) && onTablet);
        final boolean infoIconVisible = filterIconVisible;
        final boolean editIconVisible = (windowStatus == WindowStatus.SLOTS);


        searchViewIcon.setVisible(searchViewVisible);
        filterMenuIcon.setVisible(filterIconVisible);
        editSlotsMenuIcon.setVisible(editIconVisible);
        infoMenuIcon.setVisible(infoIconVisible);
        manageSlotsMenuIcon.setVisible(onTablet);

        if (!onTablet && searchView.isIconified()) {
            searchViewIcon.collapseActionView();
        }

        int title = string.app_name;
        switch (windowStatus) {
            case SLOTS:
                title = string.spell_slots_title;
                setNavigationToBack();
                break;
            case SETTINGS:
                title = string.settings;
                setNavigationToBack();
                break;
            default:
                setNavigationToHome();
        }
        binding.toolbar.setTitle(title);

        // Update the filter icon on the action bar
        // If the filters are open, we show a list or data icon (depending on the platform)
        // instead ("return to the data")
        if (filterMenuIcon != null) {
            final int filterIcon = onTablet ? drawable.ic_data : drawable.ic_list;
            final int icon = (windowStatus == WindowStatus.FILTER) ? filterIcon : drawable.ic_filter;
            filterMenuIcon.setIcon(icon);
        }
    }

    private void updateFragments() {

        // Close any fragments that need to be closed
        boolean filter = windowStatus == WindowStatus.FILTER;
        boolean navVisible = filter;
        //final Runnable onCommit = ()-> binding.bottomNavBar.setVisibility(navVisible ? View.GONE : View.VISIBLE);
        final Runnable onCommit = () -> {};
        switch (prevWindowStatus) {
            case SETTINGS:
                closeSettings();
                break;
            case SLOTS:
                closeSpellSlotsFragment();
                break;
            case SPELL:
                if (onTablet) {
                    hideFragment(spellWindowFragment, onCommit);
                } else {
                    closeSpellWindow();
                }
                break;
            case TABLE:
                //if (!onTablet && windowStatus == WindowStatus.FILTER) {
                if (!onTablet) {
                    hideFragment(spellTableFragment, onCommit);
                }
                break;
            case FILTER:
                hideFragment(sortFilterFragment, onCommit);
                break;
        }

        switch (windowStatus) {
            case SETTINGS:
                openSettings();
                break;
            case SLOTS:
                openSpellSlotsFragment();
                break;
            case FILTER:
                showFragment(sortFilterFragment);
                break;
            case TABLE:
                if (!onTablet) {
                    showFragment(spellTableFragment);
                }
                break;
            case SPELL:
                if (onTablet) {
                    showFragment(spellTableFragment);
                } else {
                    final Spell spell = viewModel.currentSpell().getValue();
                    if (spell != null) {
                        openSpellWindow(spell, true);
                    }
                }
                break;
        }
    }

    private void updateBottomBarVisibility() {
        final boolean visible = shouldBottomNavBarBeVisible();
        final BottomNavigationView bottomBar = binding.bottomNavBar;
        bottomBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void updateDrawerStatus() {
        boolean lock;
        if (onTablet) {
            lock = (windowStatus == WindowStatus.SETTINGS);
        } else {
            lock = Arrays.asList(WindowStatus.SETTINGS, WindowStatus.SLOTS).contains(windowStatus);
        }

        if (lock) {
            closeLeftDrawer();
            closeRightDrawer();
        }
        setLeftDrawerLocked(lock);
        if (!onTablet) {
            setRightDrawerLocked(lock);
        }
    }

    private void updateWindowStatus(WindowStatus newStatus) {
        if (newStatus == windowStatus) {
            return;
        }
        prevWindowStatus = windowStatus;
        windowStatus = newStatus;
        saveCharacterProfile();
        updateActionBar();
        updateBottomBarVisibility();
        updateDrawerStatus();
        updateFabVisibility();
        updateFragments();
    }

    private WindowStatus backStatus(WindowStatus status) {
        switch (status) {
            case TABLE:
                return null;
            case SPELL:
                return onTablet ? null : WindowStatus.TABLE;
            case FILTER:
                return onTablet ? WindowStatus.SPELL : WindowStatus.TABLE;
            default:
                return prevWindowStatus;
        }
    }

    private boolean isMainViewStatus(WindowStatus status) {
        switch (status) {
            case TABLE:
            case FILTER:
                return true;
            case SPELL:
                return onTablet;
            default:
                return false;
        }
    }

    private void openTutorial() {
        final SpellTableBinding spellTableBinding = spellTableFragment.getBinding();
        final SpellAdapter.SpellRowHolder spellRowVH = (SpellAdapter.SpellRowHolder) spellTableBinding.spellRecycler.findViewHolderForAdapterPosition(4);
        final View spellRowView = spellRowVH.itemView;

        // If we don't call this, the showcase sequence will pick up where it left off previously
        // At least for now, we won't deal with that, since our showcase has a lot of visibility/fragment changes
        MaterialShowcaseView.resetAll(this);

        final ShowcaseConfig config = new ShowcaseConfig();
        final String showcaseID = "TUTORIAL";
        config.setDelay(1000);
        config.setMaskColor(color.darkBrown);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, showcaseID);
        sequence.setOnItemShownListener((itemView, position) -> {
            final SortFilterLayoutBinding binding = sortFilterFragment.getBinding();
            final ScrollView filterScroll = binding.getRoot();
            switch (position) {
                case 3:
                    itemView.setTarget(new ViewTarget(spellWindowFragment.getBinding().spellName));
                    break;
                case 4:
                    final SpellWindowBinding spellWindowBinding = spellWindowFragment.getBinding();
                    itemView.setTarget(new Target() {
                        @Override
                        public Point getPoint() {
                            final ToggleButton favButton = spellWindowBinding.favoriteButton;
                            return new Point(favButton.getTop(), favButton.getLeft());
                        }

                        @Override
                        public Rect getBounds() {
                            final Point pt = getPoint();
                            final ToggleButton knownButton = spellWindowBinding.knownButton;
                            return new Rect(pt.x, pt.y, knownButton.getRight(), knownButton.getBottom());
                        }
                    });
                    break;
                case 7:
                    SpellbookUtils.scrollToView(filterScroll, binding.levelFilterRange.getRoot());
                    break;
                case 8:
                    SpellbookUtils.scrollToView(filterScroll, binding.filterOptions.getRoot());
                    break;
                case 9:
                    SpellbookUtils.scrollToView(filterScroll, binding.ritualConcentrationFilterBlock.getRoot());
                    break;
                case 10:
                    SpellbookUtils.scrollToView(filterScroll, binding.sourcebookFilterBlock.getRoot());
                    break;
            }
        });
        sequence.setOnItemDismissedListener((itemView, position) -> {
            switch (position) {
                case 2:
                    openSpellWindow(spellRowVH.getSpell(), true);

                    // Kinda gross, but is there a better way to do this?
                    while (spellWindowFragment == null) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 4:
                    if (!onTablet) {
                        closeSpellWindow(true);
                    }

                    break;
                case 5:
                    updateWindowStatus(WindowStatus.FILTER);
                    break;
                case 10:
                    final WindowStatus status = onTablet ? WindowStatus.SPELL : WindowStatus.TABLE;
                    updateWindowStatus(status);
                    break;
                case 13:
                    openLeftDrawer();
                    break;
            }
        });
        sequence.setConfig(config);

        // 0
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
            .setTarget(spellRowView)
            .withRectangleShape(true)
            .setDismissOnTouch(true)
            .setTitleText("Spell Row")
            .setContentText("A spell's row gives the name, level, school, ritual, and source for each spell")
            .build()
        );
        // 1
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
            .setTarget(spellRowView.findViewById(id.spell_row_buttons_layout))
            .withRectangleShape()
            .setDismissOnTouch(true)
            .setTitleText("Spell list buttons")
            .setContentText("Use the spell list buttons to add/remove spells from a character's spell list. There are three spell lists - favorite (star), prepared (wand), and known (book).")
            .build()
        );
        // 2
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
            .setTarget(spellRowView.findViewById(id.spell_row_text_layout))
            .withRectangleShape()
            .setDismissOnTouch(true)
            .setTitleText("Open spell description")
            .setContentText("Click on a spell to open its full description")
            .build()
        );

        // 3
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
            .setTarget(new View(this))
            .withRectangleShape()
            .setDismissOnTouch(true)
            .setTitleText("Spell window")
            .setContentText("The spell window displays all of the relevant spell information, such as the spell name, level, school, description, etc.")
            .build()
        );
        // 4
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
            .setTarget(new View(this))
            .withRectangleShape()
            .setDismissOnTouch(true)
            .setTitleText("Spell list buttons")
            .setContentText("You can change whether a spell is on a particular spell list from the spell window as well")
            .build()
        );
        // 5
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
            .setTarget(findViewById(id.action_filter))
            .withCircleShape()
            .setDismissOnTouch(true)
            .setTitleText("Sort and filter view")
            .setContentText("Press the filter button to access the sort/filter options")
            .build()
        );
        // 6
        final SortFilterLayoutBinding sortFilterBinding = sortFilterFragment.getBinding();
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
            .setTarget(sortFilterBinding.sortBlock.getRoot())
            .withRectangleShape(true)
            .setDismissOnTouch(true)
            .setTitleText("Sort options")
            .setContentText("Here you can control the sorting options. The spellbook has two-level sorting. For each level you can choose the attribute (click on the name to select) and the sorting direction. If two spells have equal values for both sorting options, they are sorted by name.")
            .build()
        );
        // 7
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
            .setTarget(sortFilterBinding.levelFilterRange.getRoot())
            .withRectangleShape(true)
            .setDismissOnTouch(true)
            .setTitleText("Level range")
            .setContentText("Here you can set the minimum and maximum level for spells that are shown.")
            .build()
        );
        // 8
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
            .setTarget(sortFilterBinding.filterOptions.getRoot())
            .withRectangleShape(true)
            .setDismissOnTouch(true)
            .setTitleText("Filtering options")
            .setContentText("Here you can control options related to spell filtering. Press the info button for each row to get more information on that setting.")
            .build()
        );
        // 9
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
            .setTarget(sortFilterBinding.ritualConcentrationFilterBlock.getRoot())
            .withRectangleShape(true)
            .setDismissOnTouch(true)
            .setTitleText("Ritual, and concentration")
            .setContentText("Here you can control whether spells are shown based on whether they are/aren't rituals, or need concentration or not. This setup allows you to see e.g. only ritual spells, only non-ritual spells, or both. The component filters below use the same logic.")
            .build()
        );
        // 10
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
            .setTarget(sortFilterBinding.ritualConcentrationFilterBlock.getRoot())
            .withRectangleShape(true)
            .setDismissOnTouch(true)
            .setTitleText("Other filters")
            .setContentText("The remaining filters allow you to further customize your visible spells. A spell will be shown if it satisfies at least one condition from each category. For casting time, duration, and range, one can also specify a range of times/distances.")
            .build()
        );
        // 11
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
            .setTarget(searchViewIcon.getActionView())
            .withCircleShape()
            .setDismissOnTouch(true)
            .setTitleText("Search")
            .setContentText("Press the magnifying glass to open the search bar, where you can look for a spell by name.")
            .build()
        );
        // 12
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
            .setTarget(findViewById(id.action_info))
            .withCircleShape()
            .setDismissOnTouch(true)
            .setTitleText("Info")
            .setContentText("Press the info button to open the information menu, which contains information on various aspects of spellcasting, along with information for each caster class.")
            .build()
        );
        // 13
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
            .setTarget(getToolbarNavigationButton())
            .withCircleShape()
            .setDismissOnTouch(true)
            .setTitleText("Main menu")
            .setContentText("Press the hamburger button to open the main menu for the app.")
            .build()
        );

        sequence.start();
    }

//    private void openTutorial() {
//        final SpellTableBinding spellTableBinding = spellTableFragment.getBinding();
//        final SpellAdapter.SpellRowHolder spellRowVH = (SpellAdapter.SpellRowHolder) spellTableBinding.spellRecycler.findViewHolderForAdapterPosition(4);
//        final View spellRowView = spellRowVH.itemView;
//        final TapTargetSequence sequence = new TapTargetSequence(this)
//            .targets(
//                    TapTarget.forBounds(new Rect(0, 0, 1000, 1000), "The Spell Table",
//                        "The spell table displays the current list of spells. You can adjust this list using the options we'll see in a minute"),
//                    TapTarget.forView(spellRowView, "Spell Row",
//                        "A spell's row gives the name, level, school, ritual, and source for each spell").tintTarget(false),
//                    TapTarget.forView(spellRowView.findViewById(id.spell_row_buttons_layout), "Spell list buttons",
//                            "Use the star, wand, and book buttons to add or remove spells from a character's favorite, known, and prepared spell lists").tintTarget(false),
//                    TapTarget.forView(spellRowView, "Spell Row",
//                            "Click on a spell's row to open its full description").tintTarget(false)
//            ).listener(new TapTargetSequence.Listener() {
//                    @Override
//                    public void onSequenceFinish() {
//
//                    }
//
//                    @Override
//                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
//
//                    }
//
//                    @Override
//                    public void onSequenceCanceled(TapTarget lastTarget) {
//
//                    }
//                });
//        sequence.start();
//    }


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

    private ImageButton getToolbarNavigationButton() {
        final Toolbar toolbar = binding.toolbar;
        int size = toolbar.getChildCount();
        for (int i = 0; i < size; i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof ImageButton) {
                ImageButton btn = (ImageButton) child;
                if (btn.getDrawable() == toolbar.getNavigationIcon()) {
                    return btn;
                }
            }
        }
        return null;
    }
}
