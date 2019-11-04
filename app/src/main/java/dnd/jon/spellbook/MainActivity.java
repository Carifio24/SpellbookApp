package dnd.jon.spellbook;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.EditText;
import android.content.Intent;
import android.view.inputmethod.InputMethodManager;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.Toolbar;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final String spellsFilename = "Spells.json";
    private Spellbook spellbook;
    private final String settingsFile = "Settings.json";
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private NavigationView rightNavView;
    private ExpandableListView rightExpLV;
    private ExpandableListAdapter rightAdapter;


    private String profilesDirName = "Characters";
    private CharacterProfile characterProfile;
    private View characterSelect = null;
    private CharacterSelectionDialog selectionDialog = null;
    private File profilesDir;
    private Settings settings;

    // The map ID -> Sourcebook relating left nav bar items to sourcebooks, for sourcebook filtering
    private HashMap<Integer, Sourcebook> subNavIds = new HashMap<Integer, Sourcebook>() {{
        put(R.id.subnav_phb, Sourcebook.PLAYERS_HANDBOOK);
        put(R.id.subnav_xge, Sourcebook.XANATHARS_GTE);
        put(R.id.subnav_scag, Sourcebook.SWORD_COAST_AG);
    }};

    // The map ID -> StatusFilterField relating left nav bar items to the corresponding spell status filter
    private HashMap<Integer,StatusFilterField> statusFilterIDs = new HashMap<Integer,StatusFilterField>() {{
       put(R.id.nav_all, StatusFilterField.All);
       put(R.id.nav_favorites, StatusFilterField.Favorites);
       put(R.id.nav_prepared, StatusFilterField.Prepared);
       put(R.id.nav_known, StatusFilterField.Known);
    }};

    // The UI elements for sorting and searching
    private Spinner sort1;
    private Spinner sort2;
    private Spinner classChooser;
    private SortDirectionButton sortArrow1;
    private SortDirectionButton sortArrow2;
    private ImageButton clearButton;
    private EditText searchBar;

    // The spinner adapters
    DefaultTextSpinnerAdapter sortAdapter1;
    DefaultTextSpinnerAdapter sortAdapter2;
    DefaultTextSpinnerAdapter classAdapter;

    // Options for controlling the spinners, with regards to their layout and default text
    private int spinnerItemLayoutID = R.layout.spinner_item;
    private int spinnerItemTextViewID = R.id.spinner_row_text_view;

    // The RecyclerView and adapter for the table of spells
    private RecyclerView spellRecycler;
    private SpellRowAdapter spellAdapter;

    // The file extension for character files
    private static final String CHARACTER_EXTENSION = ".json";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout
        setContentView(R.layout.activity_main);

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
                } else {
                    StatusFilterField sff = statusFilterIDs.get(index);
                    characterProfile.setStatusFilter(sff);
                    saveCharacterProfile();
                    close = true;
                }
                System.out.println("filter from OnNavigationItemSelectedListener");
                filter();
                saveSettings();


                // This piece of code makes the drawer close when an item is selected
                // At the moment, we only want that for when choosing one of favorites, known, prepared
                if (close) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }
                return true;
            }
        };
        navView.setNavigationItemSelectedListener(navViewListener);

        // Set the hamburger button to open the left nav
        ActionBarDrawerToggle leftNavToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.left_navigation_drawer_open, R.string.left_navigation_drawer_closed);
        drawerLayout.addDrawerListener(leftNavToggle);
        leftNavToggle.syncState();
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
        try {
            JSONArray jarr = loadJSONArrayfromAsset(spellsFilename);
            spellbook = new Spellbook(jarr);
        } catch (JSONException e) {
            e.printStackTrace();
            this.finish();
        }

        // Load the settings and the character profile
        try {

            // Load the settings
            JSONObject json = loadJSONfromData(settingsFile);
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
            String s= loadAssetAsString(new File(settingsFile));
            System.out.println("Error loading settings");
            System.out.println("The settings file content is: " + s);
            settings = new Settings();
            if (charactersList().size() > 0) {
                String firstCharacter = charactersList().get(0);
                settings.setCharacterName(firstCharacter);
            }
            e.printStackTrace();
        }

        // If the character profile is null, we create one
        if ( (settings.characterName() == null) || characterProfile == null) {
            openCharacterCreationDialog();
        }

        // Set up the RecyclerView that holds the cells
        setupSpellRecycler();

        // Sort and filter
        sort();
        filter();

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

    // Close the drawer with the back button if it's open
    @Override
    public void onBackPressed() {
        //
        // InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else if (searchBar.hasFocus()) {
            searchBar.clearFocus();
        //} else if (imm.isAcceptingText()) {
        //    hideSoftKeyboard(searchBar, this);
        } else if (searchBar.getVisibility() == View.VISIBLE) {
            sort1.setVisibility(View.VISIBLE);
            sort2.setVisibility(View.VISIBLE);
            classChooser.setVisibility(View.VISIBLE);
            searchBar.setVisibility(View.GONE);
            hideSoftKeyboard(searchBar, getApplicationContext());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.SPELL_WINDOW_REQUEST && resultCode == RESULT_OK) {
            Spell s = data.getParcelableExtra(SpellWindow.SPELL_KEY);
            boolean fav = data.getBooleanExtra(SpellWindow.FAVORITE_KEY, false);
            boolean known = data.getBooleanExtra(SpellWindow.KNOWN_KEY, false);
            boolean prepared = data.getBooleanExtra(SpellWindow.PREPARED_KEY, false);
            int index = data.getIntExtra(SpellWindow.INDEX_KEY, -1);
            boolean wasFav = characterProfile.isFavorite(s);
            boolean wasKnown = characterProfile.isKnown(s);
            boolean wasPrepared = characterProfile.isPrepared(s);
            characterProfile.setFavorite(s, fav);
            characterProfile.setKnown(s, known);
            characterProfile.setPrepared(s, prepared);
            boolean changed = (wasFav != fav) || (wasKnown != known) || (wasPrepared != prepared);
            Menu menu = navView.getMenu();
            boolean oneChecked = menu.findItem(R.id.nav_favorites).isChecked() || menu.findItem(R.id.nav_known).isChecked() || menu.findItem(R.id.nav_prepared).isChecked();

            // Re-display the spells (if this spell's status changed) if we have at least one filter selected
            if (changed && oneChecked) {
                System.out.println("filter from onActivityResult");
                filter();
            }

            // If the spell's status changed, then save
            if (changed) {
                saveCharacterProfile();
                saveSettings();
            }

            // Reload the RecyclerView's data
            System.out.println("NotifyItemChanged at index:\t" + index);
            System.out.println("Prepared:\t" + prepared);
            System.out.println("Favorite:\t" + fav);
            System.out.println("Known:\t" + known);
            spellAdapter.notifyItemChanged(index);

        }
    }

    void openSpellWindow(Spell spell, int pos) {
        Intent intent = new Intent(MainActivity.this, SpellWindow.class);
        intent.putExtra(SpellWindow.SPELL_KEY, spell);
        intent.putExtra(SpellWindow.TEXT_SIZE_KEY, settings.spellTextSize());
        intent.putExtra(SpellWindow.FAVORITE_KEY, characterProfile.isFavorite(spell));
        intent.putExtra(SpellWindow.PREPARED_KEY, characterProfile.isPrepared(spell));
        intent.putExtra(SpellWindow.KNOWN_KEY, characterProfile.isKnown(spell));
        intent.putExtra(SpellWindow.INDEX_KEY, pos);
        startActivityForResult(intent, RequestCodes.SPELL_WINDOW_REQUEST);
    }

    void openSpellPopup(View view, Spell spell) {
        SpellStatusPopup ssp = new SpellStatusPopup(this, spell);
        ssp.showUnderView(view);
    }

    void setupSpellRecycler() {
        spellRecycler = findViewById(R.id.spell_recycler);
        RecyclerView.LayoutManager spellLayoutManager = new LinearLayoutManager(this);
        spellAdapter = new SpellRowAdapter(spellbook.spells);
        spellRecycler.setAdapter(spellAdapter);
        spellRecycler.setLayoutManager(spellLayoutManager);
    }

    void setupSortTable() {

        // Get various UI elements
        sort1 = findViewById(R.id.sort_spinner_1);
        sort2 = findViewById(R.id.sort_spinner_2);
        classChooser = findViewById(R.id.class_spinner);
        sortArrow1 = findViewById(R.id.sort_arrow_1);
        sortArrow2 = findViewById(R.id.sort_arrow_2);
        System.out.println(sortArrow1);
        clearButton = findViewById(R.id.clear_search_button);

        // Set necessary tags
        sort1.setTag(1);
        sort2.setTag(2);
        sortArrow1.setTag(1);
        sortArrow2.setTag(2);

        //The list of sort fields
        ArrayList<String> sortFields1 = new ArrayList<String>();
        for (SortField sf : SortField.values()) {
            sortFields1.add(sf.name());
        }
        String[] sort1Objects = sortFields1.toArray(new String[0]); // Android Studio notes that these days passing a 0-sized array leads to an equal or faster computation, so I won't argue
        ArrayList<String> sortFields2 = new ArrayList<String>(sortFields1);
        String[] sort2Objects = sortFields2.toArray(new String[0]);

        // Populate the dropdown spinners
        sortAdapter1 = new DefaultTextSpinnerAdapter(this, sort1Objects, spinnerItemLayoutID, spinnerItemTextViewID, "Sort 1", true);
        sort1.setAdapter(sortAdapter1);

        sortAdapter2 = new DefaultTextSpinnerAdapter(this, sort2Objects, spinnerItemLayoutID, spinnerItemTextViewID, "Sort 2", true);
        sort2.setAdapter(sortAdapter2);

        String[] casterNames = Spellbook.casterNames;
        String[] none = { "None" };
        String[] classAdapterObjects = ArrayUtils.addAll(none, casterNames);
        classAdapter = new DefaultTextSpinnerAdapter(this, classAdapterObjects, spinnerItemLayoutID, spinnerItemTextViewID, "Class", true);
        classChooser.setAdapter(classAdapter);

        // Create the search button
        ImageButton searchButton = findViewById(R.id.search_button);
        searchButton.setClickable(true);

        // Create the search bar
        searchBar = findViewById(R.id.search_bar);
        searchBar.setHint("Search");
        searchBar.setFocusable(true);
        searchBar.setFocusableInTouchMode(true);

//        // Set a drawable on the right side of the edit text for clearing text
//        Drawable clear = getDrawable(android.R.drawable.ic_notification_clear_all);
//        searchBar.setCompoundDrawables(null, null, clear, null);
//
//        // Set so the keyboard shows when the search bar is selected and doesn't if it isn't
//        searchBar.setOnFocusChangeListener( (View v, boolean hasFocus) -> {
//            if (hasFocus) {
//                showKeyboard(searchBar, this);
//            } else {
//                hideSoftKeyboard(searchBar, this);
//            }
//        });
//        searchBar.setOnClickListener( (View v) -> {
//            showKeyboard(searchBar, this);
//        });

        // Set what happens when the search bar gets focus
        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                //InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (hasFocus) {
                    showKeyboard(searchBar, getApplicationContext());
                } else {
                    hideSoftKeyboard(searchBar, getApplicationContext());
                }
            }
        });

        // Set what happens when the text is changed
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filter();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter();
            }
        });

        // Set the onClickListener for the search button
        searchButton.setOnClickListener((View view) -> {

            // Search bar and clear button
            View[] searchViews = { searchBar, clearButton };

            // Views hidden when the search is visible
            View[] otherViews = { sort1, sortArrow1, sort2, sortArrow2, classChooser };

            // What to set each one to
            boolean searchVisible = searchBar.getVisibility() == View.VISIBLE;
            int forSearch = searchVisible ? View.GONE : View.VISIBLE;
            int forOthers = searchVisible ? View.VISIBLE : View.GONE;

            // Apply the appropriate visibility conditions to the views
            for (View v : searchViews) { v.setVisibility(forSearch); }
            for (View v : otherViews) { v.setVisibility(forOthers); }

            // Adjust the keyboard if necessary
            if (searchVisible) {
                hideSoftKeyboard(searchBar, getApplicationContext());
            } else {
                showKeyboard(searchBar, getApplicationContext());
            }
            boolean gotFocus = searchBar.requestFocus();
        });

        // Set up the clear text button
        clearButton.setOnClickListener( (View view) -> searchBar.getText().clear() );

        // Set what happens when the sort spinners are changed
        AdapterView.OnItemSelectedListener sortListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //System.out.println("Calling sort");
                sort();
                if (characterProfile == null) { return; }
                //try {
                    int tag = (int) adapterView.getTag();
                    System.out.println("Sort spinner " + tag + " selected with position " + i);
                    switch (tag) {
                        case 1:
                            characterProfile.setFirstSortField(SortField.fromIndex(i));
                            break;
                        case 2:
                            characterProfile.setSecondSortField(SortField.fromIndex(i));
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

        // Set what happens when the class chooser is changed
        AdapterView.OnItemSelectedListener classListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                filter();
                if (characterProfile == null) { return; }
                CasterClass cc = (i == 0) ? null : CasterClass.from(i-1);
                characterProfile.setFilterClass(cc);
                saveCharacterProfile();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        classChooser.setOnItemSelectedListener(classListener);

        // We disable the default text for the current profile when the spinner is touched
        sort1.setOnTouchListener( (view, event) -> { characterProfile.setSort1Default(false); sortAdapter1.setDefault(false); saveCharacterProfile(); view.performClick(); return true; });
        sort2.setOnTouchListener( (view, event) -> { characterProfile.setSort2Default(false); sortAdapter2.setDefault(false); saveCharacterProfile(); view.performClick(); return true; });
        classChooser.setOnTouchListener( (view, event) -> { characterProfile.setClassFilterDefault(false); classAdapter.setDefault(false); saveCharacterProfile(); view.performClick(); return true; });

        // Set what happens when the arrow buttons are pressed
        SortDirectionButton.OnClickListener arrowListener = (View view) -> {
            SortDirectionButton b = (SortDirectionButton) view;
            b.onPress();
            sort();
            boolean up = b.pointingUp();
            if (characterProfile == null) { return; }
            //try {
                int tag = (int) view.getTag();
                switch (tag) {
                    case 1:
                        characterProfile.setFirstSortReverse(up);

                        // Remove the default text if we need to
                        if (characterProfile.useSort1Default()) {
                            characterProfile.setSort1Default(false);
                            sortAdapter1.setDefault(false);
                        }
                        break;
                    case 2:
                        characterProfile.setSecondSortReverse(up);
                        if (characterProfile.useSort2Default()) {
                            characterProfile.setSort2Default(false);
                            sortAdapter2.setDefault(false);
                        }
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
        List<String> rightNavGroups = Arrays.asList(getResources().getStringArray(R.array.right_group_names));

        // Get the names of the group elements, as Arrays
        ArrayList<String[]> groups = new ArrayList<>();
        groups.add(getResources().getStringArray(R.array.basics_items));
        groups.add(getResources().getStringArray(R.array.casting_spell_items));
        String[] casterNames = new String[CasterClass.values().length];
        for (CasterClass cc : CasterClass.values()) {
            casterNames[cc.ordinal()] = cc.name();
        }
        groups.add(casterNames);

        // For each group, get the text that corresponds to each child
        // Here, entries with the same index correspond to one another
        ArrayList<Integer> basicsIDs = new ArrayList<>(Arrays.asList(R.string.what_is_a_spell, R.string.spell_level,
                R.string.known_and_prepared_spells, R.string.the_schools_of_magic, R.string.spell_slots, R.string.cantrips,
                R.string.rituals, R.string.the_weave_of_magic));
        ArrayList<Integer> castingSpellIDs = new ArrayList<>(Arrays.asList(R.string.casting_time_info, R.string.range_info, R.string.components_info,
                R.string.duration_info, R.string.targets, R.string.areas_of_effect, R.string.saving_throws,
                R.string.attack_rolls, R.string.combining_magical_effects, R.string.casting_in_armor));
        ArrayList<Integer> classInfoIDs = new ArrayList<>(Arrays.asList(R.string.bard_spellcasting_info, R.string.cleric_spellcasting_info, R.string.druid_spellcasting_info,
                R.string.paladin_spellcasting_info, R.string.ranger_spellcasting_info, R.string.sorcerer_spellcasting_info, R.string.warlock_spellcasting_info, R.string.wizard_spellcasting_info));
        List<List<Integer>> childTextLists = new ArrayList<>(Arrays.asList(basicsIDs, castingSpellIDs, classInfoIDs));

        // Create maps of the form group -> list of children, and group -> list of children's text
        int nGroups = rightNavGroups.size();
        Map<String, List<String>> childData = new HashMap<>();
        Map<String, List<Integer>> childTextIDs = new HashMap<>();
        for (int i = 0; i < nGroups; ++i) {
            childData.put(rightNavGroups.get(i), Arrays.asList(groups.get(i)));
            childTextIDs.put(rightNavGroups.get(i), childTextLists.get(i));
        }

        // Create the adapter
        rightAdapter = new NavExpandableListAdapter(this, rightNavGroups, childData, childTextIDs);
        rightExpLV.setAdapter(rightAdapter);
        View rightHeaderView = getLayoutInflater().inflate(R.layout.right_expander_header, null);
        rightExpLV.addHeaderView(rightHeaderView);

        // Set the callback that displays the appropriate popup when the list item is clicked
        rightExpLV.setOnChildClickListener((ExpandableListView elView, View view, int gp, int cp, long id) -> {
            NavExpandableListAdapter adapter = (NavExpandableListAdapter) elView.getExpandableListAdapter();
            String title = (String) adapter.getChild(gp, cp);
            int textID = adapter.childTextID(gp, cp);
            SpellcastingInfoPopup popup = new SpellcastingInfoPopup(this, title, textID, true);
            popup.show();
            return true;
        });
    }

    public static void showKeyboard(EditText mEtSearch, Context context) {
        mEtSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public static void hideSoftKeyboard(EditText mEtSearch, Context context) {
        mEtSearch.clearFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEtSearch.getWindowToken(), 0);
    }

    private int resIDfromBoolean(boolean TF, int idT, int idF) { return TF ? idT : idF; }
    int starIcon(boolean TF) { return resIDfromBoolean(TF, R.drawable.star_filled, R.drawable.star_empty); }

    void setStarIcon(Sourcebook sb, boolean tf) {
        Iterator<HashMap.Entry<Integer, Sourcebook>> it = subNavIds.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry<Integer, Sourcebook> pair = it.next();
            if (pair.getValue() == sb) {
                MenuItem m = findViewById(pair.getKey());
                m.setIcon(starIcon(tf));
                break;
            }
        }
    }

    void setSourcebookFilter(Sourcebook book, boolean tf) {
        characterProfile.setSourcebookFilter(book, tf);
        MenuItem m = navView.getMenu().findItem(navIDfromSourcebook(book));
        //System.out.println("Sourcebook is " + book.code());
        //System.out.println("tf is " + tf);
        m.setIcon(starIcon(tf));
    }

    void setSourcebookFilters() {
        for (Sourcebook book : Sourcebook.values()) {
            boolean b = characterProfile.getSourcebookFilter(book);
            setSourcebookFilter(book, b);
        }
    }

    void filter() {
        spellAdapter.filter();
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
        Iterator<HashMap.Entry<Integer, Sourcebook>> it = subNavIds.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry<Integer, Sourcebook> pair = it.next();
            if (pair.getValue() == sb) {
                return pair.getKey();
            }
        }
        return -1;
    }


    JSONArray loadJSONArrayfromAsset(String assetFilename) throws JSONException {
        String jsonStr = null;
        try {
            InputStream is = getAssets().open(assetFilename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonStr = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new JSONArray(jsonStr);
    }

    JSONObject loadJSONObjectfromAsset(String assetFilename) throws JSONException {
        String jsonStr = null;
        try {
            InputStream is = getAssets().open(assetFilename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonStr = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new JSONObject(jsonStr);
    }

    JSONObject loadJSONfromData(File file) throws JSONException {
        String jsonStr = null;
        try {
            InputStream is = new FileInputStream(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonStr = new String(buffer, "UTF-8");
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
            String str = new String(buffer, "UTF-8");
            return str;
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


    boolean saveSettings() {
        File settingsLocation = new File(getApplicationContext().getFilesDir(), settingsFile);
        System.out.println("Saving settings");
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
                System.out.println("Loading character profile for " + profile.getName());
                System.out.println("The file location is " + profileLocation);
                System.out.println(charJSON.toString());
                setCharacterProfile(profile, initialLoad);
            } catch (JSONException e) {
                System.out.println("Error loading character profile: " + profileLocation.toString());
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
            System.out.println("Saved character profile for " + characterProfile.getName());
            System.out.println(characterProfile.toJSON().toString());
//            JSONObject cpJSON = characterProfile.toJSON();
//            saveJSON(cpJSON, profileLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void setSideMenuCharacterName() {
        MenuItem m = navView.getMenu().findItem(R.id.nav_character);
        m.setTitle("Character: " + characterProfile.getName());
    }

    void setFilterSettings() {

        // Set the class filter
        boolean classDef = characterProfile.useClassFilterDefault();
        classAdapter.setDefault(classDef);
        //classChooser.setAdapter(classAdapter);
        CasterClass fc = characterProfile.getFilterClass();
        if (fc == null) {
            classChooser.setSelection(0);
        } else {
            classChooser.setSelection(fc.value + 1);
        }
        characterProfile.setClassFilterDefault(classDef);

        // Set the sourcebook filters
        setSourcebookFilters();

        // Set the status filter
        StatusFilterField sff = characterProfile.getStatusFilter();
        navView.getMenu().getItem(sff.index).setChecked(true);
    }

    void setSortSettings() {

        // Set the sort fields
        // The first sort spinner
        // First, whether we want to show the default text
        // We save this first, since setting the spinner in the next block will modify it
        boolean sort1Def = characterProfile.useSort1Default();

        // Set the spinner to the appropriate position
        SortField sf1 = characterProfile.getFirstSortField();
        sort1.setSelection(sf1.index);

        // Adjust the adapter to display the default text, or not, depending on the profile info
        sortAdapter1.setDefault(sort1Def);
        //sort1.setAdapter(sortAdapter1);

        // Finally, restore the character profile's state
        characterProfile.setSort1Default(sort1Def);

        // Repeat the entire process for the second sort spinner
        // First, whether or not we want the default text
        // We save this first, since setting the spinner in the next block will modify it
        boolean sort2Def = characterProfile.useSort2Default();

        // Set the spinner to the appropriate position
        SortField sf2 = characterProfile.getSecondSortField();
        sort2.setSelection(sf2.index);

        // Adjust the adapter to display the default text, or not, depending on the profile info
        sortAdapter2.setDefault(sort2Def);
        //sort2.setAdapter(sortAdapter2);

        // Finally, restore the character profile's state
        characterProfile.setSort2Default(sort2Def);

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
        setFilterSettings();
        setSortSettings();
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
        System.out.println("DONE WITH SETCHARACTERPROFILE");
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

    boolean isClassSelected() {
        int classIndex = classChooser.getSelectedItemPosition();
        return (classIndex != 0);
    }

    boolean searchHasText() {
        String searchText = searchBar.getText().toString();
        return !searchText.isEmpty();
    }

    String searchText() {
        return searchBar.getText().toString();
    }

    Spellbook getSpellbook() { return spellbook; }

    SortField sortField1() {
        SortField sf = SortField.fromName(sort1.getSelectedItem().toString());
        if (sf == null) { sf = SortField.Name; }
        return sf;
    }

    SortField sortField2() {
        SortField sf = SortField.fromName(sort2.getSelectedItem().toString());
        if (sf == null) { sf = SortField.Name; }
        return sf;
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

}
