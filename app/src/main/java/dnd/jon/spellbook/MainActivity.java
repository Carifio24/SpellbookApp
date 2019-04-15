package dnd.jon.spellbook;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.input.InputManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TableRow;
import android.widget.Spinner;
import android.widget.EditText;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.inputmethod.InputMethodManager;
import android.support.design.widget.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
import java.util.function.Function;
import java.util.function.BiConsumer;

public class MainActivity extends AppCompatActivity {

    private TableLayout table;
    private TableLayout header;
    private TableLayout sortTable;
    private String spellsFilename = "Spells.json";
    private Spellbook spellbook;
    private String favFile = "FavoriteSpells.json";
    private String knownFile = "KnownSpells.json";
    private String preparedFile = "PreparedSpells.json";
    private String settingsFile = "Settings.json";
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ImageButton searchButton;
    private Bitmap searchIcon;
    private EditText searchBar;
    private CharacterProfile characterProfile;
    private String profilesDirName = "Characters";
    File profilesDir;
    Settings settings;

    private HashMap<Integer, Sourcebook> subNavIds = new HashMap<Integer, Sourcebook>() {{
        put(R.id.subnav_phb, Sourcebook.PLAYERS_HANDBOOK);
        put(R.id.subnav_xge, Sourcebook.XANATHARS_GTE);
        put(R.id.subnav_scag, Sourcebook.SWORD_COAST_AG);
    }};

    int height;
    int width;
    int dpWidth;
    int dpHeight;

    int topPad;
    int botPad;
    int leftPad;
    int rightPad;

    int levelWidth;
    int schoolWidth;
    int nameWidth;
    int rowHeight;
    int headerHeight;
    int tableHeight;
    int sortHeight;
    int sortRowIndex;
    int firstSpellRowIndex;

    private Spinner sort1;
    private Spinner sort2;
    private Spinner classChooser;

    private static final String CHARACTER_EXTENSION = ".json";

    LinearLayout ml;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The DrawerLayout
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


        // The main LinearLayout
        ml = findViewById(R.id.mainLayout);
        DrawerLayout.LayoutParams mlp = new DrawerLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        mlp.setMargins(0, 0, 0, 0);
        ml.setLayoutParams(mlp);

        //View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        /*int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/

        // Get the height and width of the display
        android.view.Display display = ((android.view.WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int fullHeight = displayMetrics.heightPixels;
        int fullWidth = displayMetrics.widthPixels;

        // Adjust for margins
        int margin_left = 16;
        int margin_right = 16;
        int margin_top = 16;
        int margin_bottom = 0;
        int margin_horizontal = margin_left + margin_right;
        int margin_vertical = margin_top + margin_bottom;

        Configuration config = this.getResources().getConfiguration();
        dpWidth = config.screenWidthDp;
        dpHeight = config.screenHeightDp;
        width = fullWidth - Math.round(fullWidth * margin_horizontal / dpWidth);
        height = fullHeight - Math.round(fullHeight * margin_vertical / dpHeight);

        // Get the padding values
        botPad = ml.getPaddingBottom();
        topPad = ml.getPaddingTop();
        leftPad = ml.getPaddingLeft();
        rightPad = ml.getPaddingRight();

        // Set the column widths7
        levelWidth = (int) Math.round(width * 0.15);
        schoolWidth = (int) Math.round(width * 0.35);
        nameWidth = width - levelWidth - schoolWidth;

        // Create the profiles directory, if necessary
        profilesDir = new File(getApplicationContext().getFilesDir(), profilesDirName);
        if (!(profilesDir.exists() && profilesDir.isDirectory())) {
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
            System.out.println("Settings JSON:");
            System.out.println(json.toString());
            settings = new Settings(json);
            for (Sourcebook sb : Sourcebook.values()) {
                MenuItem m = navView.getMenu().findItem(navIDfromSourcebook(sb));
                m.setIcon(starIcon(settings.getFilter(sb)));
            }
            String charName = json.getString("Character");
            loadCharacterProfile(charName);
            setSideMenuCharacterName();
        } catch (Exception e) {
            settings = new Settings();
            e.printStackTrace();
        }

        // Set up the tables
        initialTablesSetup();

        // If the character profile is null, we create one
        System.out.println("character name is " + settings.characterName());
        if (settings.characterName() == null) {
            openCharacterCreationDialog();
        }

        // For debugging purposes
        //System.out.println("Height: " + Integer.toString(height));
        //System.out.println("Width: " + Integer.toString(width));
        //System.out.println("Header height: " + Integer.toString(headerHeight));
        //System.out.println("Table height: " + Integer.toString(tableHeight));
        //System.out.println("Sort height: " + Integer.toString(sortHeight));

    }

    @Override
    public void onStart() {
        super.onStart();
        filter();
    }

    // Close the drawer with the back button if it's open
    @Override
    public void onBackPressed() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (searchBar.hasFocus()) {
            //System.out.println("hasFocus");
            searchBar.clearFocus();
        } else if (imm.isAcceptingText()) {
            hideSoftKeyboard(searchBar, this);
        } else {
            super.onBackPressed();
        }
    }


    void formatHeaderColumn(TextView hc, int colWidth) {
        // Does the formatting common to each header column
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        lp.height = rowHeight;
        lp.width = colWidth;
        hc.setLayoutParams(lp);
        hc.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        hc.setTextSize(settings.headerTextSize());
        hc.setTypeface(null, Typeface.BOLD);
    }


    void initialTablesSetup() {

        // Set up the table
        table = findViewById(R.id.spellTable);
        tableHeight = height - headerHeight - sortHeight;
        //TableLayout.LayoutParams tlp = new TableLayout.LayoutParams();
        ScrollView.LayoutParams tlp = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT);
        tlp.height = tableHeight;
        tlp.setMargins(0, 0, 0, 0);
        table.setLayoutParams(tlp);
        double tableRowFrac = 1.0 / settings.nTableRows();
        rowHeight = fractionBetweenBounds(tableHeight, tableRowFrac, 125, 165); // Min possible is 125, max possible is 165
        System.out.println("tableRowFrac is " + tableRowFrac);
        System.out.println("tableHeight is " + tableHeight);
        System.out.println("rowHeight is " + rowHeight);
        populateTable(spellbook.spells);

        // Create the sort table
        sortTable = findViewById(R.id.sortTable);
        //ConstraintLayout.LayoutParams slp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sortHeight = Math.min(rowHeight, 100);
        slp.height = sortHeight;
        slp.width = width;
        slp.setMargins(0, 0, 0, 0);
        sortTable.setLayoutParams(slp);
        populateSortTable();

        // Create the header, set its size, and populate it
        header = findViewById(R.id.spellHeader);
        headerHeight = sortHeight;
        //TableLayout.LayoutParams hlp = new TableLayout.LayoutParams();
        //ConstraintLayout.LayoutParams hlp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hlp.height = headerHeight;
        hlp.width = width;
        hlp.setMargins(0, 0, 0, 0);
        header.setLayoutParams(hlp);
        populateHeader(headerHeight, width);

    }


    void populateHeader(int headerHeight, int width) {

        // First let's set how large we want the columns to be

        //System.out.println("levelWidth:" + Integer.toString(levelWidth));
        //System.out.println("nameWidth:" + Integer.toString(nameWidth));
        //System.out.println("schoolWidth:" + Integer.toString(schoolWidth));

        // Add the headers and format them
        final TextView h1 = new TextView(this);
        h1.setText("Spell Name");
        formatHeaderColumn(h1, nameWidth);

        final TextView h2 = new TextView(this);
        h2.setText("School");
        formatHeaderColumn(h2, schoolWidth);

        final TextView h3 = new TextView(this);
        h3.setText("Level");
        formatHeaderColumn(h3, levelWidth);
        h3.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

        TableRow hr = new TableRow(this);
        hr.addView(h1);
        hr.addView(h2);
        hr.addView(h3);
        hr.setGravity(Gravity.CENTER_VERTICAL);
        TableLayout.LayoutParams hrp = new TableLayout.LayoutParams();
        hrp.height = headerHeight;
        hrp.width = width;
        hr.setLayoutParams(hrp);
        //hr.setBackgroundColor(Color.YELLOW);
        header.addView(hr);
    }

    void formatTableElement(TextView te, int elWidth, int hgrav) {
        // Does formatting common to each table element
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        lp.height = rowHeight;
        lp.width = elWidth;
        te.setLayoutParams(lp);
        te.setGravity(Gravity.CENTER_VERTICAL | hgrav);
        te.setTextSize(TypedValue.COMPLEX_UNIT_DIP, settings.tableTextSize());
    }

    void changeTableTextSize(int textSizeDP) {
        for (int i = 0; i < table.getChildCount(); ++i) {
            View child = table.getChildAt(i);
            if (child instanceof TableRow) {
                TableRow tr = (TableRow) child;
                for (int j = 0; j < tr.getChildCount(); ++j) {
                    View tchild = tr.getChildAt(j);
                    if (tchild instanceof TextView) {
                        TextView tv = (TextView) tchild;
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeDP);
                    }
                }
            }
        }
    }

    void changeSpellWindowTextSize(int textSizeDP) {
        settings.setSpellTextSize(textSizeDP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.SPELL_WINDOW_REQUEST && resultCode == RESULT_OK) {
            int index = data.getIntExtra(SpellWindow.INDEX_KEY, -1);
            boolean fav = data.getBooleanExtra(SpellWindow.FAVORITE_KEY, false);
            boolean known = data.getBooleanExtra(SpellWindow.KNOWN_KEY, false);
            boolean prepared = data.getBooleanExtra(SpellWindow.PREPARED_KEY, false);
            Spell s = spellbook.spells.get(index);
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
                filter();
            }

            // If the spell's status changed, then save
            if (changed) {
                System.out.println("Saving character profile");
                saveCharacterProfile();
                saveSettings();
            }
        } else if (requestCode == RequestCodes.DELETE_CHARACTER_REQUEST && resultCode == RESULT_OK) {
            String name = data.getStringExtra(CharacterSelectionWindow.NAME_KEY);
            deleteCharacterProfile(name);
        }
    }

    void populateTable(final ArrayList<Spell> spells) {

        // The onClickListener
        View.OnClickListener listener = (View view) -> {
            TableRow tr = (TableRow) view;
            int index = (int) tr.getTag();
            Spell spell = spells.get(index);
            Intent intent = new Intent(MainActivity.this, SpellWindow.class);
            intent.putExtra(SpellWindow.SPELL_KEY, spell);
            intent.putExtra(SpellWindow.INDEX_KEY, index);
            intent.putExtra(SpellWindow.TEXT_SIZE_KEY, settings.spellTextSize());
            intent.putExtra(SpellWindow.FAVORITE_KEY, characterProfile.isFavorite(spell));
            intent.putExtra(SpellWindow.PREPARED_KEY, characterProfile.isPrepared(spell));
            intent.putExtra(SpellWindow.KNOWN_KEY, characterProfile.isKnown(spell));
            startActivityForResult(intent, RequestCodes.SPELL_WINDOW_REQUEST);
        };

        firstSpellRowIndex = table.getChildCount();
        for (int i = 0; i < spells.size(); i++) {

            Spell spell = spells.get(i);

            // The first column
            final TextView col1 = new TextView(this);
            col1.setText(spell.getName());
            formatTableElement(col1, nameWidth, Gravity.LEFT);

            // The second column
            final TextView col2 = new TextView(this);
            col2.setText(Spellbook.schoolNames[spell.getSchool().value]);
            formatTableElement(col2, schoolWidth, Gravity.LEFT);

            // The third column
            final TextView col3 = new TextView(this);
            col3.setText(Integer.toString(spell.getLevel()));
            formatTableElement(col3, levelWidth, Gravity.RIGHT);

            // Make the TableRow
            TableRow tr = new TableRow(this);
            tr.addView(col1);
            tr.addView(col2);
            tr.addView(col3);
            tr.setTag(i);
            tr.setClickable(true);
            tr.setOnClickListener(listener);
            TableLayout.LayoutParams trp = new TableLayout.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.WRAP_CONTENT);
            trp.height = rowHeight;
            trp.width = width;
            tr.setLayoutParams(trp);
            table.addView(tr);

        }

    }

    void populateSortTable() {

        // Create the table row and the spinners
        TableRow srow = new TableRow(this);
        int searchWidth = Math.min(Math.round(width / 10), Math.round(width * 50 / dpWidth)); // The width is never more than 50 dp
        int colWidth = Math.round((width - searchWidth) / 3);
        int sortWidth = fractionBetweenBounds(width - searchWidth, 0.3, 290, 330);
        //System.out.println("sortWidth: " + sortWidth);
        int classWidth = 3 * colWidth - 2 * sortWidth;
        searchWidth = width - classWidth - 2 * sortWidth;
        sort1 = new Spinner(this);
        sort2 = new Spinner(this);
        classChooser = new Spinner(this);
        sort1.setBackground(null);
        sort2.setBackground(null);
        classChooser.setBackground(null);
        sort1.setBackgroundColor(Color.TRANSPARENT);
        sort2.setBackgroundColor(Color.TRANSPARENT);
        classChooser.setBackgroundColor(Color.TRANSPARENT);

        //The list of sort fields
        ArrayList<String> sortFields1 = new ArrayList<String>();
        sortFields1.add("Name");
        sortFields1.add("School");
        sortFields1.add("Level");
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
        searchButton = new ImageButton(this);
        searchButton.setBackgroundColor(Color.TRANSPARENT);
        searchIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.search_icon);
        int iconDim = Math.min((int) Math.round(searchWidth * 0.7), (int) Math.round(sortHeight * 0.7));
        searchIcon = Bitmap.createScaledBitmap(searchIcon, iconDim, iconDim, true);
        searchButton.setImageBitmap(searchIcon);
        searchButton.setClickable(true);

        // Create the search bar
        searchBar = new EditText(this);
        searchBar.setHint("Search");
        searchBar.setVisibility(View.GONE);

        // Set layout parameters
        TableRow.LayoutParams sp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        sp.width = sortWidth;
        sp.height = sortHeight;
        sp.gravity = Gravity.CENTER;
        System.out.println(sortWidth);
        sort1.setLayoutParams(sp);
        sort2.setLayoutParams(sp);

        TableRow.LayoutParams cp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        cp.width = classWidth;
        cp.height = sortHeight;
        classChooser.setLayoutParams(cp);

        TableRow.LayoutParams sbp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        sbp.gravity = Gravity.RIGHT;
        sbp.width = searchWidth;
        sbp.height = sortHeight;
        searchButton.setLayoutParams(sbp);

        TableRow.LayoutParams searchPar = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        searchPar.gravity = Gravity.START | Gravity.BOTTOM;
        searchPar.width = 3 * colWidth;
        searchPar.height = (int) Math.round(sortHeight * 1.6);
        //searchPar.height = sortHeight;
        searchBar.setLayoutParams(searchPar);
        searchBar.setFocusable(true);
        searchBar.setFocusableInTouchMode(true);
        searchBar.setBackgroundResource(android.R.color.transparent);

        // Set what happens when the search bar gets focus
        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                //InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                LinearLayout mainLayout = findViewById(R.id.mainLayout);
                if (hasFocus) {
                    //mainLayout.setPadding(leftPad, 0, rightPad, botPad);
                    showKeyboard(searchBar, getApplicationContext());
//                    sort1.setVisibility(View.GONE);
//                    sort2.setVisibility(View.GONE);
//                    classChooser.setVisibility(View.GONE);
//                    searchBar.setVisibility(View.VISIBLE);
                } else {
                    //mainLayout.setPadding(leftPad, topPad, rightPad, botPad);
                    hideSoftKeyboard(searchBar, getApplicationContext());
//                    sort1.setVisibility(View.VISIBLE);
//                    sort2.setVisibility(View.VISIBLE);
//                    classChooser.setVisibility(View.VISIBLE);
//                    searchBar.setVisibility(View.GONE);
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
        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (searchBar.getVisibility() == View.GONE) {
                    searchBar.setVisibility(View.VISIBLE);
                    sort1.setVisibility(View.GONE);
                    sort2.setVisibility(View.GONE);
                    classChooser.setVisibility(View.GONE);
                    showKeyboard(searchBar, getApplicationContext());
                } else {
                    sort1.setVisibility(View.VISIBLE);
                    sort2.setVisibility(View.VISIBLE);
                    classChooser.setVisibility(View.VISIBLE);
                    searchBar.setVisibility(View.GONE);
                    hideSoftKeyboard(searchBar, getApplicationContext());
                }
                boolean gotFocus = searchBar.requestFocus();
            }
        });


        sort1.setGravity(Gravity.CENTER_VERTICAL);
        sort2.setGravity(Gravity.CENTER_VERTICAL);
        classChooser.setGravity(Gravity.CENTER_VERTICAL);
        srow.addView(sort1);
        srow.addView(sort2);
        srow.addView(classChooser);
        srow.addView(searchBar);
        srow.addView(searchButton);
        srow.setGravity(Gravity.CENTER_VERTICAL);
        sortTable.addView(srow);
        sortRowIndex = sortTable.getChildCount() - 1;

        // Set what happens when the sort spinners are changed
        AdapterView.OnItemSelectedListener sortListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int index1 = sort1.getSelectedItemPosition();
                int index2;
                if (((index2 = sort2.getSelectedItemPosition()) == 0) || (index1 == 0)) {
                    singleSort(index1);
                } else {
                    doubleSort(index1, index2);
                }
                filter();

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

    int starIcon(boolean TF) {
        return TF ? R.mipmap.star_filled : R.mipmap.star_empty;
    }

    void setStarIcon(Sourcebook sb, boolean tf) {
        int id = 0;
        Iterator<HashMap.Entry<Integer, Sourcebook>> it = subNavIds.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry<Integer, Sourcebook> pair = it.next();
            if (pair.getValue() == sb) {
                id = pair.getKey();
                break;
            }
        }
        MenuItem m = findViewById(id);
        m.setIcon(starIcon(tf));

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

    boolean filterItem(boolean isClass, boolean isText, Spell s, CasterClass cc, String text, boolean knownSelected, boolean preparedSelected, boolean favSelected) {

        // Get the spell name
        String spname = s.getName().toLowerCase();

        // Filter by class usability, favorite, and search text, and finally sourcebook
        boolean toHide = (isClass && !s.usableByClass(cc));
        toHide = toHide || (favSelected && !characterProfile.isFavorite(s));
        toHide = toHide || (knownSelected && !characterProfile.isKnown(s));
        toHide = toHide || (preparedSelected && !characterProfile.isPrepared(s));
        toHide = toHide || (isText && !spname.contains(text));
        toHide = toHide || (!settings.getFilter(s.getSourcebook()));
        return toHide;
    }

    void unfilter() {
        for (int i = firstSpellRowIndex; i < table.getChildCount(); i++) {
            View view = table.getChildAt(i);
            if (view instanceof TableRow) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    void filter() {
        boolean favSelected = settings.filterFavorites();
        boolean knownSelected = settings.filterKnown();
        boolean preparedSelected = settings.filterPrepared();
        int classIndex = classChooser.getSelectedItemPosition();
        boolean isClass = (classIndex != 0);
        String searchText = searchBar.getText().toString();
        boolean isText = (searchText != null && !searchText.isEmpty());
        searchText = searchText.toLowerCase();
        CasterClass cc = (isClass) ? CasterClass.from(classIndex - 1) : CasterClass.from(0);
//        if ( ! (isText || isFav || isClass) ) {
//            unfilter();
//        } else {
        for (int i = firstSpellRowIndex; i < table.getChildCount(); i++) {
            View view = table.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow tr = (TableRow) view;
                Spell s = spellbook.spells.get((int) tr.getTag());
                if (filterItem(isClass, isText, s, cc, searchText, knownSelected, preparedSelected, favSelected)) {
                    view.setVisibility(View.GONE);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
            }
        }
        //}
    }

    void singleSort(int index) {

        // Do the sorting
        //System.out.println("Running singleSort: " + Integer.toString(index));
        ArrayList<Spell> spells = spellbook.spells;
        Collections.sort(spells, new SpellOneFieldComparator(index));
        spellbook.setSpells(spells);

        // Repopulate the table
        //System.out.println("Table child count: " + table.getChildCount());
        //System.out.println("firstSpellRowIndex: " + firstSpellRowIndex);
        for (int i = firstSpellRowIndex; i < table.getChildCount(); i++) {
            View view = table.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow trow = (TableRow) view;
                //System.out.println("trow children: " + trow.getChildCount());
                TextView tv1 = (TextView) trow.getChildAt(0);
                TextView tv2 = (TextView) trow.getChildAt(1);
                TextView tv3 = (TextView) trow.getChildAt(2);
                Spell spell = spells.get((int) trow.getTag());
                tv1.setText(spell.getName());
                tv2.setText(Spellbook.schoolNames[spell.getSchool().value]);
                tv3.setText(Integer.toString(spell.getLevel()));
            }
        }
    }

    void doubleSort(int index1, int index2) {
        // Do the sorting
        //System.out.println("Running doubleSort: " + Integer.toString(index1) + ", " + Integer.toString(index2));
        ArrayList<Spell> spells = spellbook.spells;
        Collections.sort(spells, new SpellTwoFieldComparator(index1, index2));
        spellbook.setSpells(spells);

        // Repopulate the table
        //System.out.println("Table child count: " + table.getChildCount());
        //System.out.println("firstSpellRowIndex: " + firstSpellRowIndex);
        for (int i = firstSpellRowIndex; i < table.getChildCount(); i++) {
            View view = table.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow trow = (TableRow) view;
                //System.out.println("trow children: " + trow.getChildCount());
                TextView tv1 = (TextView) trow.getChildAt(0);
                TextView tv2 = (TextView) trow.getChildAt(1);
                TextView tv3 = (TextView) trow.getChildAt(2);
                Spell spell = spells.get((int) trow.getTag());
                tv1.setText(spell.getName());
                tv2.setText(Spellbook.schoolNames[spell.getSchool().value]);
                tv3.setText(Integer.toString(spell.getLevel()));
            }
        }

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


    int fractionBetweenBounds(int total, double fraction, int min, int max) {
        return Math.max(Math.min(max, (int) Math.round(total * fraction)), min);
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

    void loadFavorites() throws IOException {
        File faveFile = new File(getApplicationContext().getFilesDir(), favFile);
        if (faveFile.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(faveFile));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                Iterator<Spell> it = spellbook.spells.iterator();
                boolean inSpellbook = false;
                while (it.hasNext()) {
                    Spell s = it.next();
                    //System.out.println(s.getName() + "\t" + line);
                    if (s.getName().equals(line)) {
                        inSpellbook = true;
                        characterProfile.setFavorite(s, true);
                        break;
                    }
                }

                if (!inSpellbook) {
                    throw new IOException("Bad spell name!");
                }


            }
            br.close();
        }
    }

    void loadSpellsForProperty(String filename, BiConsumer<Spell, Boolean> propSetter) {
        File fileLocation = new File(getApplicationContext().getFilesDir(), filename);
        if (fileLocation.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileLocation))) {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    Iterator<Spell> it = spellbook.spells.iterator();
                    boolean inSpellbook = false;
                    while (it.hasNext()) {
                        Spell s = it.next();
                        if (s.getName().equals(line)) {
                            inSpellbook = true;
                            propSetter.accept(s, true);
                            break;
                        }
                    }

                    if (!inSpellbook) {
                        throw new IOException("Bad spell name!");
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void saveSpellsWithProperty(Function<Spell, Boolean> property, String filename) throws IOException {
        File fileLocation = new File(getApplicationContext().getFilesDir(), filename);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileLocation))) {
            JSONObject json = new JSONObject();
            Iterator<Spell> it = spellbook.spells.iterator();
            while (it.hasNext()) {
                Spell s = it.next();
                if (property.apply(s)) {
                    bw.write(s.getName() + "\n");
                }
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void saveJSON(JSONObject json, File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void saveFavorites() throws IOException {
        BufferedWriter bw = null;
        try {
            File faveFile = new File(getApplicationContext().getFilesDir(), favFile);
            bw = new BufferedWriter(new FileWriter(faveFile));
            Iterator<Spell> it = spellbook.spells.iterator();
            while (it.hasNext()) {
                Spell s = it.next();
                if (characterProfile.isFavorite(s)) {
                    bw.write(s.getName() + "\n");
                }

            }
            bw.flush();
            bw.close();
        } finally {
            if (bw != null) {
                bw.close();
            }
        }
    }

    void saveSettings() {
        File settingsLocation = new File(getApplicationContext().getFilesDir(), settingsFile);
        //System.out.println("Saving settings to " + settingsLocation);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(settingsLocation))) {
            JSONObject json = settings.toJSON();
            bw.write(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void loadCharacterProfile(String charName) {

        // We don't need to do anything if the given character is already the current one
        boolean skip = (characterProfile != null) && charName.equals(characterProfile.getName());
        if (!skip) {
            String charFile = charName + ".json";
            File profileLocation = new File(profilesDir, charFile);
            try {
                JSONObject charJSON = loadJSONfromData(profileLocation);
                CharacterProfile profile = CharacterProfile.fromJSON(charJSON);
                setCharacterProfile(profile);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
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

    void setCharacterProfile(CharacterProfile cp) {
        characterProfile = cp;
        settings.setCharacterName(cp.getName());

        setSideMenuCharacterName();
        saveSettings();
        saveCharacterProfile();

        try {
            filter();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

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
        return charList;
    }

    void openCharacterSelection() {
        System.out.println("Entering openCharacterSelection");
        CharacterSelectionDialog dialog = new CharacterSelectionDialog();
        Bundle args = new Bundle();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "selectCharacter");
        //CharacterSelectionWindow csw = new CharacterSelectionWindow(this);
        //csw.show();
    }

}
