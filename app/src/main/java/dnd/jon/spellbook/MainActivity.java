package dnd.jon.spellbook;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TableRow;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.SimpleExpandableListAdapter;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.inputmethod.InputMethodManager;
import android.support.design.widget.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.BiConsumer;

public class MainActivity extends AppCompatActivity {

    private String spellsFilename = "Spells.json";
    private Spellbook spellbook;
    private String settingsFile = "Settings.json";
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private NavigationView rightNavView;
    private ExpandableListView rightExpLV;
    private ExpandableListAdapter rightAdapter;
    private EditText searchBar;

    private String profilesDirName = "Characters";
    private CharacterProfile characterProfile;
    private View characterSelect = null;
    private CharacterSelectionDialog selectionDialog = null;
    private File profilesDir;
    private Settings settings;

    private HashMap<Integer, Sourcebook> subNavIds = new HashMap<Integer, Sourcebook>() {{
        put(R.id.subnav_phb, Sourcebook.PLAYERS_HANDBOOK);
        put(R.id.subnav_xge, Sourcebook.XANATHARS_GTE);
        put(R.id.subnav_scag, Sourcebook.SWORD_COAST_AG);
    }};

    private Spinner sort1;
    private Spinner sort2;
    private Spinner classChooser;
    private SortDirectionButton sortArrow1;
    private SortDirectionButton sortArrow2;
    private ImageButton clearButton;

    private SpellRowAdapter spellAdapter;

    private static final String CHARACTER_EXTENSION = ".json";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The DrawerLayout and the left navigation view
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.side_menu);
        NavigationView.OnNavigationItemSelectedListener navViewListener = new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int index = menuItem.getItemId();
                boolean isBookFilter = false;
                if (subNavIds.containsKey(index)) {
                    Sourcebook source = subNavIds.get(index);
                    boolean tf = changeSourcebookFilter(source);
                    menuItem.setIcon(starIcon(tf));
                    isBookFilter = true;
                } else if (index == R.id.subnav_charselect) {
                    openCharacterSelection();
                } else {
                    settings.setFilterFavorites(index == R.id.nav_favorites);
                    settings.setFilterKnown(index == R.id.nav_known);
                    settings.setFilterPrepared(index == R.id.nav_prepared);
                }
                System.out.println("filter from OnNavigationItemSelectedListener");
                filter();
                saveSettings();

                // This piece of code makes the drawer close when an item is selected
                // At the moment, we only want that for when choosing one of favorites, known, prepared
                if (!isBookFilter) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }

                return true;
            }
        };
        navView.setNavigationItemSelectedListener(navViewListener);

        // Set up the right navigation view
        setupRightNav();

        //View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        /*int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/

        // Set up the sort table
        setupSortTable();

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

        // Load the settings
        try {
            JSONObject json = loadJSONfromData(settingsFile);
            settings = new Settings(json);

            // For now, we're going to ignore the status filters
            settings.setFilterFavorites(false);
            settings.setFilterPrepared(false);
            settings.setFilterKnown(false);

            for (Sourcebook sb : Sourcebook.values()) {
                MenuItem m = navView.getMenu().findItem(navIDfromSourcebook(sb));
                m.setIcon(starIcon(settings.getFilter(sb)));
            }
            String charName = json.getString("Character");
            loadCharacterProfile(charName, true);
            setSideMenuCharacterName();
        } catch (Exception e) {
            System.out.println("Error loading settings");
            settings = new Settings();
            e.printStackTrace();
        }

        // If the character profile is null, we create one
        if (settings.characterName() == null) {
            openCharacterCreationDialog();
        }

        // Set up the RecyclerView that holds the cells
        setupSpellRecycler();

        // Sort and filter
        // sort();

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
        }
    }

    void openSpellWindow(Spell spell) {
        Intent intent = new Intent(MainActivity.this, SpellWindow.class);
        intent.putExtra(SpellWindow.SPELL_KEY, spell);
        intent.putExtra(SpellWindow.TEXT_SIZE_KEY, settings.spellTextSize());
        intent.putExtra(SpellWindow.FAVORITE_KEY, characterProfile.isFavorite(spell));
        intent.putExtra(SpellWindow.PREPARED_KEY, characterProfile.isPrepared(spell));
        intent.putExtra(SpellWindow.KNOWN_KEY, characterProfile.isKnown(spell));
        startActivityForResult(intent, RequestCodes.SPELL_WINDOW_REQUEST);
    }

    void openSpellPopup(View view, Spell spell) {
        SpellStatusPopup ssp = new SpellStatusPopup(this, spell);
        ssp.showUnderView(view);
    }

    void setupSpellRecycler() {
        RecyclerView spellRecycler = findViewById(R.id.spell_recycler);
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
        clearButton = findViewById(R.id.clear_search_button);

        //The list of sort fields
        ArrayList<String> sortFields1 = new ArrayList<String>();
        for (SortField sf : SortField.values()) {
            sortFields1.add(sf.name());
        }
        ArrayList<String> sortFields2 = new ArrayList<String>(sortFields1);

        // Populate the dropdown spinners
        ArrayAdapter<String> sortAdapter1 = new ArrayAdapter<String>(this, R.layout.spinner_item, sortFields1) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                return v;

            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setGravity(Gravity.CENTER);
                return v;

            }
        };
        //sortAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortAdapter1.setDropDownViewResource(R.layout.spinner_item);
        sort1.setAdapter(sortAdapter1);

        ArrayAdapter<String> sortAdapter2 = new ArrayAdapter<String>(this, R.layout.spinner_item, sortFields2) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                return v;

            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setGravity(Gravity.CENTER);
                return v;

            }
        };
        //sortAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortAdapter2.setDropDownViewResource(R.layout.spinner_item);
        sort2.setAdapter(sortAdapter2);

        ArrayList<String> classes = new ArrayList<String>(Arrays.asList(Spellbook.casterNames));
        classes.add(0, "None");
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, classes);
        //classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classAdapter.setDropDownViewResource(R.layout.spinner_item);
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
            if (searchBar.getVisibility() == View.GONE) {
                clearButton.setVisibility(View.VISIBLE);
                searchBar.setVisibility(View.VISIBLE);
                sort1.setVisibility(View.GONE);
                sort2.setVisibility(View.GONE);
                classChooser.setVisibility(View.GONE);
                sortArrow1.setVisibility(View.GONE);
                sortArrow2.setVisibility(View.GONE);
                showKeyboard(searchBar, getApplicationContext());
            } else {
                sort1.setVisibility(View.VISIBLE);
                sort2.setVisibility(View.VISIBLE);
                classChooser.setVisibility(View.VISIBLE);
                sortArrow1.setVisibility(View.VISIBLE);
                sortArrow2.setVisibility(View.VISIBLE);
                searchBar.setVisibility(View.GONE);
                clearButton.setVisibility(View.GONE);
                hideSoftKeyboard(searchBar, getApplicationContext());
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        classChooser.setOnItemSelectedListener(classListener);

        // Set what happens when the arrow buttons are pressed
        SortDirectionButton.OnClickListener arrowListener = (View view) -> {
            SortDirectionButton b = (SortDirectionButton) view;
            System.out.println("SortDirectionButton onClick:");
            System.out.println(view);
            System.out.println(b);
            b.onPress();
            sort();
        };
        System.out.println(sortArrow1);
        System.out.println(sortArrow2);
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

    void setStarIcons() {
        for (Sourcebook sb : Sourcebook.values()) {
            try {
                setStarIcon(sb, settings.getFilter(sb));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    boolean changeSourcebookFilter(Sourcebook book) {
        boolean tf = !settings.getFilter(book);
        settings.setBookFilter(book, tf);
        return tf;
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
        return settings.save(settingsLocation);
    }

    void loadCharacterProfile(String charName, boolean initialLoad) {

        // We don't need to do anything if the given character is already the current one
        boolean skip = (characterProfile != null) && charName.equals(characterProfile.getName());
        if (!skip) {
            String charFile = charName + ".json";
            File profileLocation = new File(profilesDir, charFile);
            try {
                JSONObject charJSON = loadJSONfromData(profileLocation);
                CharacterProfile profile = CharacterProfile.fromJSON(charJSON);
                setCharacterProfile(profile, initialLoad);
                //System.out.println("Loaded character profile for " + profile.getName());
            } catch (JSONException e) {
                System.out.println("Error loading character profile");
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
            JSONObject cpJSON = characterProfile.toJSON();
            saveJSON(cpJSON, profileLocation);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    void setSideMenuCharacterName() {
        MenuItem m = navView.getMenu().findItem(R.id.nav_character);
        m.setTitle("Character: " + characterProfile.getName());
    }

    void setCharacterProfile(CharacterProfile cp, boolean initialLoad) {
        characterProfile = cp;
        settings.setCharacterName(cp.getName());

        setSideMenuCharacterName();
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
        boolean success = profileLocation.delete();

        if (success && name.equals(characterProfile.getName())) {
            ArrayList<String> characters = charactersList();
            if (characters.size() > 0) {
                loadCharacterProfile(characters.get(0));
            } else {
                openCharacterCreationDialog();
            }
        }

        return success;
    }

    ArrayList<String> charactersList() {
        ArrayList<String> charList = new ArrayList<>();
        int toRemove = CHARACTER_EXTENSION.length();
        for (File file : profilesDir.listFiles()) {
            String filename = file.getName();
            if (filename.endsWith(CHARACTER_EXTENSION)) {
                String charName = filename.substring(0, filename.length() - toRemove);
                charList.add(charName);
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
        if (settings.isStatusFilterSet()) {
            System.out.println("filter from filterIfStatusSet");
            filter();
        }
    }

    boolean isClassSelected() {
        int classIndex = classChooser.getSelectedItemPosition();
        return (classIndex != 0);
    }

    Optional<CasterClass> classIfSelected() {
        int classIndex = classChooser.getSelectedItemPosition();
        boolean isClass = (classIndex != 0);
        Optional<CasterClass> ccOpt = Optional.empty();
        if (isClass) {
            ccOpt = Optional.of(CasterClass.from(classIndex - 1));
        }
        return ccOpt;
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
        return SortField.fromIndex(sort1.getSelectedItemPosition());
    }

    SortField sortField2() {
        return SortField.fromIndex(sort2.getSelectedItemPosition());
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
