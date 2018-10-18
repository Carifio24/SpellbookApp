package dnd.jon.spellbook;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.Color;

public final class SpellWindow extends Activity {

    private Spell spell;
    TableLayout swTable;
    Intent returnIntent;
    Button favButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        spell = intent.getParcelableExtra("spell");
        int index = intent.getIntExtra("index",-1);

        returnIntent.putExtra("fav", spell.isFavorite());
        returnIntent.putExtra("index", index);

        setContentView(R.layout.spell_window);
        swTable = this.findViewById(R.id.swTable);

        // Get the window size
        // Get the height and width of the display
        android.view.Display display = ((android.view.WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        // Add the spell text
        // Start with the title
        final TextView title = new TextView(this);
        title.setText(spell.getName());
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(30);

        favButton = new Button(this);
        favButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                spell.setFavorite(!spell.isFavorite());
                returnIntent.putExtra("fav", spell.isFavorite());
                updateButton();
            }
        });
        updateButton();

        TextView schoolTV = makeTextView("School: ", Spellbook.schoolNames[spell.getSchool().value]);
        TextView levelTV = makeTextView("Level: ", Integer.toString(spell.getLevel()));
        TextView castingTimeTV = makeTextView("Casting time: ", spell.getCastingTime());
        TextView durationTV = makeTextView("Duration: ", spell.getDuration());
        TextView pageTV = makeTextView("Location: ", "PHB " + Integer.toString(spell.getPage()));
        TextView materialsTV = makeTextView("Materials: ", spell.getMaterial());
        TextView componentsTV = makeTextView("Components: ", spell.componentsString());
        TextView rangeTV = makeTextView("Range: ", spell.getRange());
        TextView ritualTV = makeTextView("Ritual: ", Util.bool_to_yn(spell.getRitual()));
        TextView concentrationTV = makeTextView("Concentration: ", Util.bool_to_yn(spell.getConcentration()));
        TextView classesTV = makeTextView("Classes: ", spell.classesString());
        TextView descriptionTV = makeTextView("Description:\n", spell.getDescription());
        TextView higherTV = makeTextView("Higher level:\n", spell.getHigherLevelDesc());

        TableRow tr = new TableRow(this);
        tr.addView(title);
        tr.addView(favButton);
        swTable.addView(tr);

        //addRow(title);
        addRow(schoolTV);
        addRow(levelTV);
        addRow(castingTimeTV);
        addRow(durationTV);
        addRow(pageTV);
        addRow(componentsTV);
        if (spell.getComponents()[2]) {
            addRow(materialsTV);
        }
        addRow(rangeTV);
        addRow(ritualTV);
        addRow(concentrationTV);
        addRow(classesTV);
        addRow(descriptionTV);
        if (!spell.getHigherLevelDesc().equals("")) {
            addRow(higherTV);
        }

        ScrollView scroll = this.findViewById(R.id.spellscroll);
        final Activity thisActivity = this;
        scroll.setOnTouchListener(new OnSwipeTouchListener(thisActivity) {

            @Override
            public void onSwipeRight() {
                setResult(Activity.RESULT_OK, returnIntent);
                thisActivity.finish();
            }
        });


    }


    TextView makeTextView(String label, String text) {
        SpannableStringBuilder str = new SpannableStringBuilder(label + text);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, label.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView tv = new TextView(this);
        tv.setText(str);
        return tv;
    }

    void addRow(TextView tv) {
        TableRow tr = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        tr.addView(tv);
        swTable.addView(tr);
    }

    void updateButton() {
        if (spell.isFavorite()) {
            favButton.setBackgroundColor(Color.RED);
            favButton.setText("Remove from favorite spells");
        } else {
            favButton.setBackgroundColor(Color.GREEN);
            favButton.setText("Add to favorite spells");
        }
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK, returnIntent);
        this.finish();
    }


}
