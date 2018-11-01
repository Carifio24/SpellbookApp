package dnd.jon.spellbook;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.input.InputManager;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
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
import android.view.inputmethod.InputMethodManager;
import android.support.design.widget.NavigationView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private TableLayout table;
    private TableLayout header;
    private TableLayout sortTable;
    private String filename = "Spells.json";
    private Spellbook spellbook;
    private String favFile = "FavoriteSpells.json";
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ImageButton searchButton;
    private Bitmap searchIcon;
    private EditText searchBar;

    int height;
    int width;
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

    int headerTextSize = 15;
    int textSize = 15;

    LinearLayout ml;

    static final int SPELL_FAVORITE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.side_menu);
        navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        int index = menuItem.getItemId();
                        int classIndex = classChooser.getSelectedItemPosition();
                        boolean isClass = (classIndex != 0);
                        if (index == R.id.nav_all) {
//                            if (isClass) {
//                                filterByClass(CasterClass.from(classIndex-1));
//                                //filter();
//                            }
//                            else {
//                                unfilter();
//                            }
                            filter(false);
                        }
                        else if (index == R.id.nav_favorites) {
//                            if (isClass) {
//                                filterWithFavorites(CasterClass.from(classIndex-1));
//                            } else {
//                                filterFavorites();
//                            }
                            filter(true);
                        }

                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        }

                        return true;
                    }
                }
        );


        // The main LinearLayout
        ml = findViewById(R.id.mainLayout);
        DrawerLayout.LayoutParams mlp = new DrawerLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        mlp.setMargins(0,0,0,0);
        ml.setLayoutParams(mlp);

        //View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        /*int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/

        // Get the height and width of the display
        android.view.Display display = ((android.view.WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int fullHeight = displayMetrics.heightPixels;
        int fullWidth = displayMetrics.widthPixels;

        // Adjust for margins
        int margin_left = 8;
        int margin_right = 8;
        int margin_top = 8;
        int margin_bottom = 0;
        int margin_horizontal = margin_left + margin_right;
        int margin_vertical = margin_top + margin_bottom;
        width = fullWidth - Math.round(fullWidth*margin_horizontal/160);
        height = fullHeight - Math.round(fullHeight*margin_vertical/160);

        // Get the padding values
        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        botPad = mainLayout.getPaddingBottom();
        topPad = mainLayout.getPaddingTop();
        leftPad = mainLayout.getPaddingLeft();
        rightPad = mainLayout.getPaddingRight();

        // Set the column widths
        levelWidth = (int) Math.round(width*0.15);
        schoolWidth = (int) Math.round(width*0.35);
        nameWidth = width - levelWidth - schoolWidth;

        // Row height
        //rowHeight = Math.round(height/(nRowsShown+2));
        rowHeight = Math.max(Math.min(165, (int)Math.round(height*0.1)), 110); // Min possible is 110, max possible is 165
        //System.out.println("height: " + height);
        //System.out.println("rowHeight: " + rowHeight);

        // Create the sort table
        sortTable = findViewById(R.id.sortTable);
        //ConstraintLayout.LayoutParams slp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sortHeight =  Math.min(rowHeight,100);
        slp.height = sortHeight;
        slp.width = width;
        slp.setMargins(0,0,0,0);
        sortTable.setLayoutParams(slp);
        populateSortTable();
        //sortTable.setBackgroundColor(Color.MAGENTA);

        // Create the header, set its size, and populate it
        header = findViewById(R.id.spellHeader);
        headerHeight = sortHeight;
        //TableLayout.LayoutParams hlp = new TableLayout.LayoutParams();
        //ConstraintLayout.LayoutParams hlp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hlp.height = headerHeight;
        hlp.width = width;
        hlp.setMargins(0,0,0,0);
        header.setLayoutParams(hlp);
        populateHeader();

        // Load the spell data
        String jsonStr = loadJSONdata();
        spellbook = new Spellbook(jsonStr);

        // Set up the table
        table = findViewById(R.id.spellTable);
        tableHeight = height - headerHeight - sortHeight;
        //TableLayout.LayoutParams tlp = new TableLayout.LayoutParams();
        ScrollView.LayoutParams tlp = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT);
        tlp.height = tableHeight;
        tlp.setMargins(0,0,0,0);
        table.setLayoutParams(tlp);
        populateTable(spellbook.spells);

        // Load favorite spells
        try {
            loadFavorites();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // For debugging purposes
        //System.out.println("Height: " + Integer.toString(height));
        //System.out.println("Width: " + Integer.toString(width));
        //System.out.println("Header height: " + Integer.toString(headerHeight));
        //System.out.println("Table height: " + Integer.toString(tableHeight));
        //System.out.println("Sort height: " + Integer.toString(sortHeight));

    }

    // Close the drawer with the back button if it's open
    @Override
    public void onBackPressed() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (searchBar.hasFocus()) {
            System.out.println("hasFocus");
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
        hc.setTextSize(headerTextSize);
        hc.setTypeface(null, Typeface.BOLD);
    }

    void populateHeader() {

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
        te.setTextSize(textSize);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPELL_FAVORITE_REQUEST && resultCode == RESULT_OK) {
            int index = data.getIntExtra("index", -1);
            boolean fav = data.getBooleanExtra("fav", false);
            boolean wasFav = spellbook.spells.get(index).isFavorite();
            spellbook.spells.get(index).setFavorite(fav);
            System.out.println("Setting " + spellbook.spells.get(index).getName() + "'s favorite status to " + fav);

            // Re-display the favorites (if this spell's status changed) if we're on that screen
            if ( (wasFav != fav) && navView.getMenu().findItem(R.id.nav_favorites).isChecked() ) {
                filter();
            }

            try {
                saveFavorites();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void populateTable(final ArrayList<Spell> spells) {

        // The onClickListener
        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View view) {
                TableRow tr = (TableRow) view;
                int index = (int) tr.getTag();
                Spell spell = spells.get(index);
                //System.out.println("Tag: " + index);
                //System.out.println("Spell name: " + spell.getName());
                Intent intent = new Intent(MainActivity.this, SpellWindow.class);
                intent.putExtra("spell", spell);
                intent.putExtra("index", index);
                startActivityForResult(intent, SPELL_FAVORITE_REQUEST);

            }
        };

        firstSpellRowIndex = table.getChildCount();
        for (int i = 0; i < spells.size(); i++) {

            Spell spell = spells.get(i);

            // The first column
            final TextView col1 = new TextView(this);
            col1.setText(spell.getName());
            formatTableElement(col1, nameWidth, Gravity.LEFT);
            //col1.setBackgroundColor(Color.RED);

            // The second column
            final TextView col2 = new TextView(this);
            col2.setText(spellbook.schoolNames[spell.getSchool().value]);
            formatTableElement(col2, schoolWidth, Gravity.LEFT);
            //col2.setBackgroundColor(Color.GREEN);

            // The third column
            final TextView col3 = new TextView(this);
            col3.setText(Integer.toString(spell.getLevel()));
            formatTableElement(col3, levelWidth, Gravity.RIGHT);
            //col3.setBackgroundColor(Color.BLUE);

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
        int searchWidth = Math.min(Math.round(width/10), Math.round(width*25/160)); // The width is never more than 25 dp
        int colWidth = Math.round((width-searchWidth)/3);
        int sortWidth = (int) Math.round((width-searchWidth)*0.34);
        int classWidth = 3*colWidth - 2*sortWidth;
        searchWidth = width - classWidth - 2*sortWidth;
        sort1 = new Spinner(this);
        sort2 = new Spinner(this);
        classChooser = new Spinner(this);

        //The list of sort fields
        ArrayList<String> sortFields1 = new ArrayList<String>();
        sortFields1.add("Name");
        sortFields1.add("School");
        sortFields1.add("Level");
        ArrayList<String> sortFields2 = new ArrayList<String>(sortFields1);

        // Populate the dropdown spinners
        ArrayAdapter<String> sortAdapter1 = new ArrayAdapter<>(this, R.layout.spinner_item, sortFields1);
        sortAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort1.setAdapter(sortAdapter1);

        ArrayAdapter<String> sortAdapter2 = new ArrayAdapter<>(this, R.layout.spinner_item, sortFields2);
        sortAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort2.setAdapter(sortAdapter2);

        ArrayList<String> classes = new ArrayList<String>(Arrays.asList(Spellbook.casterNames));
        classes.add(0, "None");
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, classes);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classChooser.setAdapter(classAdapter);

        // Create the search button
        searchButton = new ImageButton(this);
        searchButton.setBackgroundColor(Color.TRANSPARENT);
        searchIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.search_icon);
        int iconDim = Math.min((int) Math.round(searchWidth*0.7), (int) Math.round(sortHeight*0.7));
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
        searchPar.width = 3*colWidth;
        searchPar.height = (int) Math.round(sortHeight*1.6);
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
                    showKeyboard(searchBar,getApplicationContext());
//                    sort1.setVisibility(View.GONE);
//                    sort2.setVisibility(View.GONE);
//                    classChooser.setVisibility(View.GONE);
//                    searchBar.setVisibility(View.VISIBLE);
                }
                else {
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
                System.out.println("onClick");
                if (searchBar.getVisibility() == View.GONE) {
                    searchBar.setVisibility(View.VISIBLE);
                    sort1.setVisibility(View.GONE);
                    sort2.setVisibility(View.GONE);
                    classChooser.setVisibility(View.GONE);
                } else {
                    sort1.setVisibility(View.VISIBLE);
                    sort2.setVisibility(View.VISIBLE);
                    classChooser.setVisibility(View.VISIBLE);
                    searchBar.setVisibility(View.GONE);
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
                if (((index2 = sort2.getSelectedItemPosition()) == 0) || (index1 == 0) ) {
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

    boolean filterItem(boolean isClass, boolean isFav, boolean isText, Spell s, CasterClass cc, String text) {
        String spname = s.getName().toLowerCase();
        boolean toHide = (isClass && !s.usableByClass(cc));
        toHide = toHide || (isFav && !s.isFavorite());
        toHide = toHide || (isText && !spname.startsWith(text));
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

//    void filterByClass(CasterClass cc) {
//        for (int i = firstSpellRowIndex; i < table.getChildCount(); i++) {
//            View view = table.getChildAt(i);
//            if (view instanceof TableRow) {
//                TableRow tr = (TableRow) view;
//                if (spellbook.spells.get((int) tr.getTag()).usableByClass(cc)) {
//                    view.setVisibility(View.VISIBLE);
//                } else {
//                    view.setVisibility(View.GONE);
//                }
//            }
//        }
//    }
//
//    void filterFavorites() {
//        for (int i = firstSpellRowIndex; i < table.getChildCount(); i++) {
//            View view = table.getChildAt(i);
//            if (view instanceof TableRow) {
//                TableRow tr = (TableRow) view;
//                if (spellbook.spells.get((int) tr.getTag()).isFavorite()) {
//                    view.setVisibility(View.VISIBLE);
//                } else {
//                    view.setVisibility(View.GONE);
//                }
//            }
//        }
//    }
//
//    void filterWithFavorites(CasterClass cc) {
//        for (int i = firstSpellRowIndex; i < table.getChildCount(); i++) {
//            View view = table.getChildAt(i);
//            if (view instanceof TableRow) {
//                TableRow tr = (TableRow) view;
//                Spell s = spellbook.spells.get((int) tr.getTag());
//                if (s.usableByClass(cc) && s.isFavorite()) {
//                    view.setVisibility(View.VISIBLE);
//                } else {
//                    view.setVisibility(View.GONE);
//                }
//            }
//        }
//    }


//    void filter() {
//        int classIndex = classChooser.getSelectedItemPosition();
//        boolean isFav = navView.getMenu().findItem(R.id.nav_favorites).isChecked();
//        boolean isClass = (classIndex != 0);
//        if (isClass && isFav) {
//            filterWithFavorites(CasterClass.from(classIndex - 1));
//        } else if (isClass) {
//            filterByClass(CasterClass.from(classIndex - 1));
//        } else if (isFav) {
//            filterFavorites();
//        }
//    }

    void filter(boolean isFav) {
        int classIndex = classChooser.getSelectedItemPosition();
        boolean isClass = (classIndex != 0);
        String searchText = searchBar.getText().toString();
        boolean isText = (searchText != null && !searchText.isEmpty()); // Just a placeholder for now
        CasterClass cc;
        if (isClass) {
            cc = CasterClass.from(classIndex-1);
        } else {
            cc = CasterClass.from(0);
        }
        if ( ! (isText || isFav || isClass) ) {
            unfilter();
        } else {
            for (int i = firstSpellRowIndex; i < table.getChildCount(); i++) {
                View view = table.getChildAt(i);
                if (view instanceof TableRow) {
                    TableRow tr = (TableRow) view;
                    Spell s = spellbook.spells.get((int) tr.getTag());
                    if (filterItem(isClass, isFav, isText, s, cc, searchText)) {
                        view.setVisibility(View.GONE);
                    } else {
                        view.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    void filter() {
        boolean isFav = navView.getMenu().findItem(R.id.nav_favorites).isChecked();
        filter(isFav);
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
        Collections.sort(spells, new SpellTwoFieldComparator(index1,index2));
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

    String loadJSONdata() {
        String json = null;
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
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
                    System.out.println(s.getName() + "\t" + line);
                    if (s.getName().equals(line)) {
                        inSpellbook = true;
                        s.setFavorite(true);
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

    void saveFavorites() throws IOException {
        BufferedWriter bw = null;
        try {
            File faveFile = new File(getApplicationContext().getFilesDir(), favFile);
            bw = new BufferedWriter(new FileWriter(faveFile));
            Iterator<Spell> it = spellbook.spells.iterator();
            while (it.hasNext()) {
                Spell s = it.next();
                if (s.isFavorite()) {
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


/*    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPELL_FAVORITE_REQUEST) {
            if (resultCode == RESULT_OK) {
                boolean fav = data.getBooleanExtra("fav", false);
                int index = data.getIntExtra("index", -1);
                spellbook.spells.get(index).setFavorite(fav);
            }
        }
    }*/


}
