package dnd.jon.spellbook;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;

import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentTransitionImpl;
import androidx.lifecycle.ViewModelProvider;

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

import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import dnd.jon.spellbook.databinding.ActivityMainBinding;
import dnd.jon.spellbook.databinding.FilterGridLayoutBinding;
import dnd.jon.spellbook.databinding.SpellWindowBinding;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    //private NavigationView rightNavView;
    private ExpandableListView rightExpLV;
    private ExpandableListAdapter rightAdapter;
    private SearchView searchView;
    private MenuItem searchViewIcon;
    private MenuItem filterMenuIcon;

    private boolean filterVisible = false;

    private static final String spellBundleKey = "SPELL";
    private static final String spellIndexBundleKey = "SPELL_INDEX";

    private static final String devEmail = "dndspellbookapp@gmail.com";
    private static final String emailMessage = "[Android] Feedback";

    private static final String createCharacterTag = "create_character";
    private static final String feedbackTag = "feedback";

    // The map ID -> StatusFilterField relating left nav bar items to the corresponding spell status filter
    private static final HashMap<Integer,StatusFilterField> statusFilterIDs = new HashMap<Integer,StatusFilterField>() {{
       put(R.id.nav_all, StatusFilterField.ALL);
       put(R.id.nav_favorites, StatusFilterField.FAVORITE);
       put(R.id.nav_prepared, StatusFilterField.PREPARED);
       put(R.id.nav_known, StatusFilterField.KNOWN);
    }};


    // Keys for Bundles
    private static final String FAVORITE_KEY = "FAVORITE";
    private static final String KNOWN_KEY = "KNOWN";
    private static final String PREPARED_KEY = "PREPARED";
    private static final String FILTER_VISIBLE_KEY = "FILTER_VISIBLE";

    // Whether or not this is running on a tablet
    private boolean onTablet;

    // For view and data binding
    private ActivityMainBinding binding = null;
    private SpellWindowBinding spellWindowBinding = null;
    private ConstraintLayout spellWindowCL = null;

    // Fragments
    private SpellTableFragment spellTableFragment = null;
    private SortFilterFragment sortFilterFragment = null;
    private SpellWindowFragment spellWindowFragment = null;

    private SpellbookViewModel spellbookViewModel = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the main activity binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get the view model
        spellbookViewModel = new ViewModelProvider(this, new SpellbookViewModelFactory(this.getApplication())).get(SpellbookViewModel.class);

        // Are we on a tablet or not?
        // If we're on a tablet, do the necessary setup
        onTablet = spellbookViewModel.areOnTablet();

        // Get the fragments
        final FragmentManager fragmentManager = getSupportFragmentManager();
        spellTableFragment = (SpellTableFragment) fragmentManager.findFragmentById(R.id.spell_table_fragment);
        sortFilterFragment = (SortFilterFragment) fragmentManager.findFragmentById(R.id.sort_filter_fragment);
        spellWindowFragment = (SpellWindowFragment) fragmentManager.findFragmentById(R.id.spell_window_fragment);
        if (!onTablet) {
            fragmentManager.beginTransaction().hide(spellWindowFragment).commit();
        }

        // For keyboard visibility listening
        KeyboardVisibilityEvent.setEventListener(this, (isOpen) -> {
            if (!isOpen) {
                clearViewTypeFocus(EditText.class);
            }
        });

//        // Re-set the current spell after a rotation (only needed on tablet)
//        if (onTablet && savedInstanceState != null) {
//            final Spell spell = savedInstanceState.containsKey(spellBundleKey) ? savedInstanceState.getParcelable(spellBundleKey) : null;
//            final int spellIndex = savedInstanceState.containsKey(spellIndexBundleKey) ? savedInstanceState.getInt(spellIndexBundleKey) : -1;
//            if (spell != null) {
//                spellWindowBinding.setSpell(spell);
//                spellWindowBinding.setSpellIndex(spellIndex);
//                spellWindowBinding.executePendingBindings();
//                spellWindowCL.setVisibility(View.VISIBLE);
//                if (savedInstanceState.containsKey(FAVORITE_KEY) && savedInstanceState.containsKey(PREPARED_KEY) && savedInstanceState.containsKey(KNOWN_KEY)) {
//                    updateSpellWindow(spell, savedInstanceState.getBoolean(FAVORITE_KEY), savedInstanceState.getBoolean(PREPARED_KEY), savedInstanceState.getBoolean(KNOWN_KEY));
//                }
//            }
//        }

        // Whether or not we want the filter to be visible
        if (savedInstanceState != null) {
            filterVisible = savedInstanceState.containsKey(FILTER_VISIBLE_KEY) && savedInstanceState.getBoolean(FILTER_VISIBLE_KEY);
        }

        // Set the toolbar as the app bar for the activity
        final Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // The DrawerLayout and the left navigation view
        drawerLayout = binding.drawerLayout;
        //navView = binding.leftNavView.leftNav;
        navView = binding.leftNav;

        // Set the appropriate callbacks for the navigation view items
        final NavigationView.OnNavigationItemSelectedListener navViewListener = menuItem -> {
            final int index = menuItem.getItemId();
            boolean close = false;
            if (index == R.id.subnav_charselect) {
                openCharacterSelection();
            } else if (index == R.id.nav_feedback) {
                sendFeedback();
            } else if (index == R.id.rate_us) {
                openPlayStoreForRating();
            } else if (index == R.id.manage_created_items) {
                openCreationManagementWindow();
                close = true;
            } else if (statusFilterIDs.containsKey(index)) {
                final StatusFilterField sff = statusFilterIDs.get(index);
                spellbookViewModel.setStatusFilter(sff);
                close = true;
            }

            // This piece of code makes the drawer close when an item is selected
            // At the moment, we only want that for when choosing one of favorites, known, prepared
            if (close && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
            }
            return true;
        };
        navView.setNavigationItemSelectedListener(navViewListener);

        // Set the character name label to observe the view model's character name
        spellbookViewModel.getCharacterName().observe(this, (name) -> navView.getMenu().findItem(R.id.nav_character).setTitle("Character: " + name));

        // Set the correct filter item to be checked when the status filter is changed
        for (Map.Entry<Integer, StatusFilterField> entry : statusFilterIDs.entrySet()) {
            final int id = entry.getKey();
            final StatusFilterField sff = entry.getValue();
            spellbookViewModel.getStatusFilter().observe(this, (statusFilter) -> navView.getMenu().findItem(id).setChecked(statusFilter == sff));
        }

        // This listener will stop the spell recycler's scrolling when the navigation drawer is opened
        // This prevents a crash that could occur if a filter button is selected while the spell recycler is still scrolling
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }
            @Override public void onDrawerClosed(@NonNull View drawerView) { }
            @Override public void onDrawerStateChanged(int newState) { }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                final RecyclerView spellRecycler = spellTableFragment.getRecyclerView();
                if (spellRecycler != null) {
                    spellRecycler.stopScroll();
                }
            }

        });

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

        // The right nav drawer often gets in the way of fast scrolling on a phone
        // Since we can open it from the action bar, we'll lock it closed from swiping
        if (!onTablet) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        }

        // Set the correct view visibilities
        updateWindowVisibilities();

        // Add the listener to display the spell window on a phone
        spellbookViewModel.getCurrentSpell().observe(this, this::openSpellWindow);

        // Add a listener to unlock the menu when the spell window is closed, on a phone
        if (!onTablet) {
            System.out.println("Unlocking left menu");
            spellbookViewModel.spellWindowFragmentClose().observe(this, (nothing) -> drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START));
        }

        // If there's no character created yet, prompt the user to make one
        if (!spellbookViewModel.characterLoaded()) {
            openCharacterCreationDialog(true);
        }

    }

    @Override
    public void onDestroy() {
        spellbookViewModel.onShutdown();
        super.onDestroy();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        System.out.println("In onKeyDown");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!searchView.isIconified() && spellWindowFragment.isVisible()) {
                spellWindowFragment.close();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
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
                spellbookViewModel.setFilterText(text);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                if (searchView.isIconified()) {
                    return true;
                }
                spellbookViewModel.setFilterText(text);
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

    // Necessary for handling rotations
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (onTablet && binding != null && spellWindowBinding.getSpell() != null) {
            outState.putParcelable(spellBundleKey, spellWindowBinding.getSpell());
            outState.putInt(spellIndexBundleKey, spellWindowBinding.getSpellIndex());
            outState.putBoolean(FAVORITE_KEY, spellWindowBinding.favoriteButton.isSet());
            outState.putBoolean(PREPARED_KEY, spellWindowBinding.preparedButton.isSet());
            outState.putBoolean(KNOWN_KEY, spellWindowBinding.knownButton.isSet());
        }
        outState.putBoolean(FILTER_VISIBLE_KEY, filterVisible);
    }


    @Override
    public void onBackPressed() {

        // If there's a fragment in the content frame, remove it
//        if (binding.contentFrame.getVisibility() == View.VISIBLE) {
//            final FragmentManager fragmentManager = getSupportFragmentManager();
//            final Fragment fragment = fragmentManager.findFragmentById(R.id.content_frame);
//            if (fragment != null) {
//                final Fragment toShow = filterVisible ? sortFilterFragment : spellTableFragment;
//                fragmentManager.beginTransaction().remove(fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).commit();
//                fragmentManager.beginTransaction().show(toShow).commit();
//            }
//            binding.contentFrame.setVisibility(View.GONE);
//            return;
//        }

        // If the SpellWindowFragment is visible on a phone, close it
        System.out.println("spellWindowFragment is visible: " + spellWindowFragment.isVisible());
        if (!onTablet && spellWindowFragment.isVisible()) {
            System.out.println("Here");
            spellWindowFragment.close();
            return;
        }

        // Close the drawer with the back button if it's open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);

        // If the sort/filter fragment is visible, swap it with the spell table fragment
        } else if (filterVisible) {
            toggleWindowVisibilities();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) { return; }
        switch (requestCode) {
            case RequestCodes.SPELL_WINDOW_REQUEST:
                final Spell s = data.getParcelableExtra(SpellWindow.SPELL_KEY);
                final boolean fav = data.getBooleanExtra(SpellWindow.FAVORITE_KEY, false);
                final boolean known = data.getBooleanExtra(SpellWindow.KNOWN_KEY, false);
                final boolean prepared = data.getBooleanExtra(SpellWindow.PREPARED_KEY, false);
                final int index = data.getIntExtra(SpellWindow.INDEX_KEY, -1);
                final boolean wasFav = spellbookViewModel.isFavorite(s);
                final boolean wasKnown = spellbookViewModel.isKnown(s);
                final boolean wasPrepared = spellbookViewModel.isPrepared(s);
                spellbookViewModel.setFavorite(s, fav);
                spellbookViewModel.setKnown(s, known);
                spellbookViewModel.setPrepared(s, prepared);
                //final boolean changed = (wasFav != fav) || (wasKnown != known) || (wasPrepared != prepared);
                final Menu menu = navView.getMenu();
                final boolean oneChecked = menu.findItem(R.id.nav_favorites).isChecked() || menu.findItem(R.id.nav_known).isChecked() || menu.findItem(R.id.nav_prepared).isChecked();
            case RequestCodes.CREATION_MANAGEMENT_REQUEST:
                final boolean spellsChanged = data.getBooleanExtra(CreationManagementActivity.SPELLS_CHANGED_KEY, false);
                final boolean sourcesChanged = data.getBooleanExtra(CreationManagementActivity.SOURCES_CHANGED_KEY, false);
                if (spellsChanged) { spellbookViewModel.emitFilterSignal(); }
                if (sourcesChanged) { spellbookViewModel.emitSourcesUpdateSignal(); }
        }
    }

    void openSpellWindow(Spell spell) {

        // On a phone, we're going to open the SpellWindowFragment, via a slide in from the right
        // The main views will stay in place
        // On a tablet, the SpellWindow fragment already exists, and we don't need to do anything
        // It will update automatically via LiveData

        if (!onTablet) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().setCustomAnimations(R.anim.right_to_left_enter, R.anim.identity, R.anim.identity, R.anim.left_to_right_exit)
                    .addToBackStack("spell_window").show(spellWindowFragment).commit();

            // Lock the left drawer closed
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
        }
    }

//    void openSpellPopup(View view, Spell spell) {
//        final SpellStatusPopup ssp = new SpellStatusPopup(this, spell);
//        ssp.showUnderView(view);
//    }

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
        final String[] casterNames = spellbookViewModel.getAllClassNames().toArray(new String[0]);
        groups.add(casterNames);

        // For each group, get the text that corresponds to each child
        // Here, entries with the same index correspond to one another
        final List<Integer> basicsIDs = new ArrayList<>(Arrays.asList(R.string.what_is_a_spell, R.string.spell_level,
                R.string.known_and_prepared_spells, R.string.the_schools_of_magic, R.string.spell_slots, R.string.cantrips,
                R.string.rituals, R.string.the_weave_of_magic));
        final List<Integer> castingSpellIDs = new ArrayList<>(Arrays.asList(R.string.casting_time_info, R.string.range_info, R.string.components_info,
                R.string.duration_info, R.string.targets, R.string.areas_of_effect, R.string.saving_throws,
                R.string.attack_rolls, R.string.combining_magical_effects, R.string.casting_in_armor));
        final List<Integer> classInfoIDs = new ArrayList<>(Arrays.asList(R.string.bard_spellcasting_info, R.string.cleric_spellcasting_info, R.string.druid_spellcasting_info,
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

        // Create the tables array
        final int[] tableIDs = new int[]{ R.layout.bard_table_layout, R.layout.cleric_table_layout, R.layout.druid_table_layout, R.layout.paladin_table_layout, R.layout.ranger_table_layout, R.layout.sorcerer_table_layout, R.layout.warlock_table_layout, R.layout.wizard_table_layout };

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

    public void showKeyboard(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    public void hideSoftKeyboard(View view) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

//    String loadAssetAsString(File file) {
//        try {
//            InputStream is = new FileInputStream(file);
//            final int size = is.available();
//            final byte[] buffer = new byte[size];
//            is.read(buffer);
//            is.close();
//            return new String(buffer, StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "";
//        }
//    }

    // Opens a character creation dialog
    void openCharacterCreationDialog(boolean mustComplete) {
        final CreateCharacterDialog dialog = new CreateCharacterDialog();
        final Bundle args = new Bundle();
        args.putBoolean(CreateCharacterDialog.MUST_COMPLETE_KEY, mustComplete);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), createCharacterTag);
    }
    void openCharacterCreationDialog() { openCharacterCreationDialog(false); }

    void openFeedbackWindow() {
        final FeedbackDialog dialog = new FeedbackDialog();
        final Bundle args = new Bundle();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), feedbackTag);
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

    // Opens a character selection dialog
    void openCharacterSelection() {
        final CharacterSelectionDialog dialog = new CharacterSelectionDialog();
        final Bundle args = new Bundle();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "selectCharacter");
    }



    // If we're on a tablet, this function updates the spell window to match its status in the character profile
    // This is called after one of the spell list buttons is pressed for that spell in the main table
    void updateSpellWindow(Spell s, boolean favorite, boolean prepared, boolean known) {
        if (onTablet && (spellWindowCL.getVisibility() == View.VISIBLE) && (binding != null) && (s.equals(spellWindowBinding.getSpell())) ) {
            spellWindowBinding.favoriteButton.set(favorite);
            spellWindowBinding.preparedButton.set(prepared);
            spellWindowBinding.knownButton.set(known);
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
    private <T extends View> void clearViewTypeFocus(Class<T> viewType) {
        //System.out.println("Running clearViewTypeFocus with viewType as " + viewType);
        final View view = getCurrentFocus();
        if (viewType.isInstance(view)) {
            view.clearFocus();
            //System.out.println("Cleared focus");
        }
    }

    private void hideSoftKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isAcceptingText()) {
            imm.toggleSoftInput(0, 0);
        }
    }

    private void updateWindowVisibilities() {

        // Let the view model know that the table will become visible
        // So it can begin its sorting right away
        spellbookViewModel.setSpellTableVisible(!filterVisible);

        // Clear the focus from an EditText, if that's where it is
        // since they have an OnFocusChangedListener
        // We want to do this BEFORE we sort/filter so that any changes can be made to the CharacterProfile
        if (!filterVisible) {
            final View view = getCurrentFocus();
            if (view != null) {
                hideSoftKeyboard(view);
            }
        }


        // Update window visibilities appropriately
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment visibleFragment = filterVisible ? sortFilterFragment : spellTableFragment;
        final Fragment hiddenFragment = filterVisible ? spellTableFragment : sortFilterFragment;
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.hide(hiddenFragment);
        ft.show(visibleFragment);
        ft.commit();


        // Collapse the SearchView if it's open, and set the search icon visibility appropriately
        if (filterVisible && (searchView != null) && !searchView.isIconified()) {
            searchViewIcon.collapseActionView();
        }
        if (!onTablet && searchViewIcon != null) {
            searchViewIcon.setVisible(!filterVisible);
        }

        // Update the filter icon on the action bar
        // If the filters are open, we show a list or data icon (depending on the platform) instead ("return to the data")
        if (filterMenuIcon != null) {
            final int filterIcon = onTablet ? R.drawable.ic_data : R.drawable.ic_list;
            final int icon = filterVisible ? filterIcon : R.drawable.ic_filter;
            filterMenuIcon.setIcon(icon);
        }
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


    private void openCreationManagementWindow() {
        final Intent intent = new Intent(MainActivity.this, CreationManagementActivity.class);
        startActivityForResult(intent, RequestCodes.CREATION_MANAGEMENT_REQUEST);
        //overridePendingTransition(R.anim.identity, android.R.anim.slide_in_left);
    }

//    private File createFileDirectory(String directoryName) {
//        File directory = new File(getApplicationContext().getFilesDir(), directoryName);
//        if ( !(directory.exists() && directory.isDirectory()) ) {
//            final boolean success = directory.mkdir();
//            if (!success) {
//                Log.v(TAG, "Error creating directory: " + directory); // Add something real here eventually
//            }
//        }
//        return directory;
//    }

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

}
