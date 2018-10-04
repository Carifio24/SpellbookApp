package dnd.jon.spellbook;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.View;
import android.view.Gravity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TableRow;
import android.widget.Spinner;
import android.support.constraint.ConstraintLayout;
import android.graphics.Color;
import android.graphics.Typeface;
import android.content.Intent;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private TableLayout table;
    private TableLayout header;
    private TableLayout sortTable;
    private String filename = "Spells.json";
    private Spellbook spellbook;

    int height;
    int width;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The main LinearLayout
        ml = findViewById(R.id.mainLayout);
        FrameLayout.LayoutParams mlp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
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
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

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
        //header.setBackgroundColor(Color.YELLOW);

        // Load the spell data
        String jsonStr = loadJSONdata();
        spellbook = new Spellbook(jsonStr);

        // Set up the table
        table = findViewById(R.id.spellTable);
        tableHeight = height - headerHeight - sortHeight;
        //TableLayout.LayoutParams tlp = new TableLayout.LayoutParams();
        ScrollView.LayoutParams tlp = new ScrollView.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.WRAP_CONTENT);
        tlp.height = tableHeight;
        tlp.width = width;
        tlp.setMargins(0,0,0,0);
        table.setLayoutParams(tlp);
        populateTable(spellbook.spells);
        //table.setBackgroundColor(Color.CYAN);

        // For debugging purposes
        //System.out.println("Height: " + Integer.toString(height));
        //System.out.println("Width: " + Integer.toString(width));
        //System.out.println("Header height: " + Integer.toString(headerHeight));
        //System.out.println("Table height: " + Integer.toString(tableHeight));
        //System.out.println("Sort height: " + Integer.toString(sortHeight));

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
                startActivity(intent);
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
            formatTableElement(col3, levelWidth, Gravity.CENTER_HORIZONTAL);
            //col3.setBackgroundColor(Color.BLUE);

            // Make the TableRow
            TableRow tr = new TableRow(this);
            tr.addView(col1);
            tr.addView(col2);
            tr.addView(col3);
            tr.setTag(i);
            tr.setClickable(true);
            tr.setOnClickListener(listener);
            ScrollView.LayoutParams trp = new ScrollView.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.WRAP_CONTENT);
            trp.height = rowHeight;
            trp.width = width;
            table.setLayoutParams(trp);
            table.addView(tr);

        }

    }

    void populateSortTable() {

        // Create the table row and the spinners
        TableRow srow = new TableRow(this);
        int colWidth = Math.round(width/3);
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

        TableRow.LayoutParams sp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        sp.width = colWidth;
        sp.height = sortHeight;
        sort1.setLayoutParams(sp);
        sort2.setLayoutParams(sp);
        classChooser.setLayoutParams(sp);

        sort1.setGravity(Gravity.CENTER_VERTICAL);
        sort2.setGravity(Gravity.CENTER_VERTICAL);
        classChooser.setGravity(Gravity.CENTER_VERTICAL);
        srow.addView(sort1);
        srow.addView(sort2);
        srow.addView(classChooser);
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
                if (i > 0) {
                    filterByClass(CasterClass.from(i - 1));
                }
                else {
                    unfilter();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };

        classChooser.setOnItemSelectedListener(classListener);

    }

    void unfilter() {
        for (int i = firstSpellRowIndex; i < table.getChildCount(); i++) {
            View view = table.getChildAt(i);
            if (view instanceof TableRow) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    void filterByClass(CasterClass cc) {
        for (int i = firstSpellRowIndex; i < table.getChildCount(); i++) {
            View view = table.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow tr = (TableRow) view;
                if (spellbook.spells.get((int) tr.getTag()).usableByClass(cc)) {
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.GONE);
                }
            }
        }
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


}
