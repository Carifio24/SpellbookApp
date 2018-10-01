package dnd.jon.spellbook;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.View;
import android.view.Gravity;
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
import java.util.Arrays;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TableLayout table;
    private TableLayout header;
    private TableLayout sortTable;
    private String filename = "Spells.json";
    private Spellbook spellbook;

    int height;
    int width;
    int nRowsShown = 12;

    int levelWidth;
    int schoolWidth;
    int nameWidth;
    int rowHeight;

    int headerTextSize = 15;
    int textSize = 15;

    LinearLayout ml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The main LinearLayout
        ml = findViewById(R.id.mainLayout);
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mlp.setMargins(0,0,0,0);

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

        // Also set the row height
        rowHeight = Math.round(height/nRowsShown);

        // Create the sort table
        sortTable = findViewById(R.id.sortTable);
        //ConstraintLayout.LayoutParams slp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int sortHeight =  Math.round(height/nRowsShown);
        slp.height = sortHeight;
        slp.width = width;
        slp.setMargins(0,0,0,0);
        sortTable.setLayoutParams(slp);
        populateSortTable();
        sortTable.setBackgroundColor(Color.MAGENTA);

        // Create the header, set its size, and populate it
        header = findViewById(R.id.spellHeader);
        int headerHeight = sortHeight;
        //TableLayout.LayoutParams hlp = new TableLayout.LayoutParams();
        //ConstraintLayout.LayoutParams hlp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hlp.height = headerHeight;
        hlp.width = width;
        hlp.setMargins(0,0,0,0);
        header.setLayoutParams(hlp);
        populateHeader();
        header.setBackgroundColor(Color.YELLOW);

        // Load the spell data
        String jsonStr = loadJSONdata();
        spellbook = new Spellbook(jsonStr);

        // Do the same for the table
        table = findViewById(R.id.spellTable);
        int tableHeight = height - headerHeight - sortHeight;
        //TableLayout.LayoutParams tlp = new TableLayout.LayoutParams();
        ScrollView.LayoutParams tlp = new ScrollView.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.WRAP_CONTENT);
        tlp.height = tableHeight;
        tlp.width = width;
        tlp.setMargins(0,0,0,0);
        table.setLayoutParams(tlp);
        populateTable();
        table.setBackgroundColor(Color.CYAN);

        // For debugging purposes
        System.out.println("Height: " + Integer.toString(height));
        System.out.println("Width: " + Integer.toString(width));
        System.out.println("Header height: " + Integer.toString(headerHeight));
        System.out.println("Table height: " + Integer.toString(tableHeight));
        System.out.println("Sort height: " + Integer.toString(sortHeight));

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

        System.out.println("levelWidth:" + Integer.toString(levelWidth));
        System.out.println("nameWidth:" + Integer.toString(nameWidth));
        System.out.println("schoolWidth:" + Integer.toString(schoolWidth));

        // Also set the row height
        int rowHeight = Math.round(height/nRowsShown);

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
        TableLayout.LayoutParams hrp = new TableLayout.LayoutParams();
        hrp.height = rowHeight;
        hrp.width = width;
        hr.setLayoutParams(hrp);
        hr.setBackgroundColor(Color.YELLOW);
        header.addView(hr);
    }

    void formatTableElement(TextView te, int elWidth) {
        // Does formatting common to each table element
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        lp.height = rowHeight;
        lp.width = elWidth;
        te.setLayoutParams(lp);
        te.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        te.setTextSize(textSize);
    }

    void populateTable() {

        // The onClickListener
        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View view) {
                TableRow tr = (TableRow) view;
                int index = (int) tr.getTag();
                Spell spell = spellbook.spells.get(index);
                Intent intent = new Intent(MainActivity.this, SpellWindow.class);
                intent.putExtra("spell", spell);
                startActivity(intent);
            }
        };

        for (int i = 0; i < spellbook.N_SPELLS; i++) {

            // The first column
            final TextView col1 = new TextView(this);
            col1.setText(spellbook.spells.get(i).getName());
            formatTableElement(col1, nameWidth);
            col1.setBackgroundColor(Color.RED);

            // The second column
            final TextView col2 = new TextView(this);
            col2.setText(spellbook.schoolNames[spellbook.spells.get(i).getSchool().value]);
            formatTableElement(col2, schoolWidth);
            col2.setBackgroundColor(Color.GREEN);

            // The third column
            final TextView col3 = new TextView(this);
            col3.setText(Integer.toString(spellbook.spells.get(i).getLevel()));
            formatTableElement(col3, levelWidth);
            col3.setBackgroundColor(Color.BLUE);

            // Make the TableRow
            TableRow tr = new TableRow(this);
            tr.addView(col1);
            tr.addView(col2);
            tr.addView(col3);
            int index = table.getChildCount();
            tr.setTag(index);
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
        TableRow srow = new TableRow(this);
        int colWidth = Math.round(width/3);
        final Spinner sort1 = new Spinner(this);
        final Spinner sort2 = new Spinner(this);
        final Spinner classChooser = new Spinner(this);
        ArrayList<String> sortFields = new ArrayList<String>();
        sortFields.add("Name");
        sortFields.add("School");
        sortFields.add("Level");

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortFields);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort1.setAdapter(sortAdapter);
        sort2.setAdapter(sortAdapter);

        ArrayList<String> classes = new ArrayList<String>(Arrays.asList(Spellbook.casterNames));
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classes);
        classChooser.setAdapter(classAdapter);

        srow.addView(sort1);
        srow.addView(sort2);
        srow.addView(classChooser);
        sortTable.addView(srow);

    }

    void filterByClass(CasterClass cc) {
        int nchild = table.getChildCount();
        for (int i = 0; i < nchild; i++) {
            View view = table.getChildAt(i);
            if (view instanceof TableRow) {
                if (spellbook.spells.get(i).usableByClass(cc)) {
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.GONE);
                }
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
