package dnd.jon.spellbook;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.Color;

public final class SpellWindow extends Activity {

    private Spell spell;
    TableLayout swTable;
    TableLayout swHeader;
    Intent returnIntent;
    ImageButton favButton;
    ImageButton knownButton;
    ImageButton preparedButton;
    Bitmap fav_filled;
    Bitmap fav_empty;
    Bitmap known_filled;
    Bitmap known_empty;
    Bitmap prepared_filled;
    Bitmap prepared_empty;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        spell = intent.getParcelableExtra("spell");
        int index = intent.getIntExtra("index",-1);

        returnIntent = new Intent(SpellWindow.this, MainActivity.class);
        returnIntent.putExtra("fav", spell.isFavorite());
        returnIntent.putExtra("index", index);
        returnIntent.putExtra("known", spell.isKnown());
        returnIntent.putExtra("prepared", spell.isPrepared());

        //System.out.println(spell.getName() + "'s favorite status is: " + spell.isFavorite());

        setContentView(R.layout.spell_window);
        swTable = this.findViewById(R.id.swTable);
        swHeader = this.findViewById(R.id.swHeader);

        // Get the window size
        // Get the height and width of the display
        android.view.Display display = ((android.view.WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        // Adjust for margins
        Configuration config = this.getResources().getConfiguration();
        int dpWidth = config.screenWidthDp;
        int margin_left = 10;
        int margin_right = 10;
        int margin_top = 5;
        int margin_bottom = 20;
        int margin_horizontal = margin_left + margin_right;
        int margin_vertical = margin_top + margin_bottom;
        width = width - Math.round(width*margin_horizontal/dpWidth);
        height = height - Math.round(height*margin_vertical/dpWidth);
        System.out.println("dpWidth: " + dpWidth);

        // Add the spell text
        // Start with the title
        final TextView title = this.findViewById(R.id.spellName);
        title.setText(spell.getName());
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(30);

        // Set the width for the title and the button
        int titleWidth = (int) Math.round(width*0.9);
        int buttonWidth = width - titleWidth;
        //int buttonHeight = 125;

        // Make the bitmaps for the buttons
        int bitmapSize = 100;
        int bitmapDim = (bitmapSize > buttonWidth ? buttonWidth : bitmapSize);
        //int bitmapDim = buttonWidth;
        int bitmapWidth = bitmapDim;
        int bitmapHeight = bitmapDim;
        fav_filled = createBitmap(R.mipmap.star_filled, bitmapWidth, bitmapHeight);
        fav_empty = createBitmap(R.mipmap.star_empty, bitmapWidth, bitmapHeight);
        known_filled = createBitmap(R.mipmap.book_filled, bitmapWidth, bitmapHeight);
        known_empty = createBitmap(R.mipmap.book_empty, bitmapWidth, bitmapHeight);
        prepared_filled = createBitmap(R.mipmap.wand_filled, bitmapWidth, bitmapHeight);
        prepared_empty = createBitmap(R.mipmap.wand_empty, bitmapWidth, bitmapHeight);

        // Set the button actions
        // The favorites button
        favButton = this.findViewById(R.id.favButton);
        favButton.setBackgroundColor(Color.TRANSPARENT);
        //favButton.setImageResource(R.drawable.star_empty);
        favButton.setOnClickListener((v) -> {
            spell.setFavorite(!spell.isFavorite());
            returnIntent.putExtra("fav", spell.isFavorite());
            updateFavButton();
        });
        updateFavButton();

        // The known button
        knownButton = this.findViewById(R.id.knownButton);
        knownButton.setBackgroundColor(Color.TRANSPARENT);
        knownButton.setOnClickListener((v) -> {
            spell.setKnown(!spell.isKnown());
            returnIntent.putExtra("known", spell.isKnown());
            updateKnownButton();
        });
        updateKnownButton();

        // The prepared button
        preparedButton = this.findViewById(R.id.preparedButton);
        preparedButton.setBackgroundColor(Color.TRANSPARENT);
        preparedButton.setOnClickListener((v) -> {
            spell.setPrepared(!spell.isPrepared());
            returnIntent.putExtra("prepared", spell.isPrepared());
            updatePreparedButton();
        });
        updatePreparedButton();

        TextView schoolTV = makeTextView("School: ", Spellbook.schoolNames[spell.getSchool().value]);
        TextView levelTV = makeTextView("Level: ", Integer.toString(spell.getLevel()));
        TextView castingTimeTV = makeTextView("Casting time: ", spell.getCastingTime());
        TextView durationTV = makeTextView("Duration: ", spell.getDuration());
        TextView pageTV = makeTextView("Location: ", Spellbook.sourcebookCodes[spell.getSourcebook().value] + " " + Integer.toString(spell.getPage()));
        TextView materialsTV = makeTextView("Materials: ", spell.getMaterial());
        TextView componentsTV = makeTextView("Components: ", spell.componentsString());
        TextView rangeTV = makeTextView("Range: ", spell.getRange());
        TextView ritualTV = makeTextView("Ritual: ", Util.bool_to_yn(spell.getRitual()));
        TextView concentrationTV = makeTextView("Concentration: ", Util.bool_to_yn(spell.getConcentration()));
        TextView classesTV = makeTextView("Classes: ", spell.classesString());
        TextView descriptionTV = makeTextView("Description:\n", spell.getDescription());
        TextView higherTV = makeTextView("Higher level:\n", spell.getHigherLevelDesc());

        TableRow tr = new TableRow(this);

        // Layout configuration for the title
        TableRow.LayoutParams tlp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        tlp.width = titleWidth;
        title.setLayoutParams(tlp);
        title.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        title.setWidth(titleWidth);

        // Layout configuration for the button
        TableRow.LayoutParams blp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        blp.width = buttonWidth;
        //blp.gravity = Gravity.TOP;
        //blp.height = buttonHeight;
        favButton.setLayoutParams(blp);
        favButton.setVisibility(View.VISIBLE);

        // Layout configuration for the first row
        TableLayout.LayoutParams trlp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        trlp.width = width;
        //trlp.gravity = Gravity.TOP;
        tr.setLayoutParams(trlp);

        // These lines are no longer needed - the views are added in the XML file

        // Add the title and the button to the TableRow
        //tr.addView(title);
        //tr.addView(favButton);

        // Add the first row to the table
        //swHeader.addView(tr);

        //addRow(title);
        addRow(swHeader, schoolTV);
        addRow(swHeader, levelTV);
        addRow(swHeader, durationTV);
        addRow(swHeader, pageTV);
        addRow(swHeader, ritualTV);
        addRow(swHeader, concentrationTV);
        addRow(swTable, castingTimeTV);
        addRow(swTable, componentsTV);
        if (spell.getComponents()[2]) {
            addRow(swTable, materialsTV);
        }
        addRow(swTable, rangeTV);
        addRow(swTable, classesTV);
        addRow(swTable, descriptionTV);
        if (!spell.getHigherLevelDesc().equals("")) {
            addRow(swTable, higherTV);
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

    Bitmap createBitmap(int imageID, int bitmapWidth, int bitmapHeight) {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), imageID);
        bmp = Bitmap.createScaledBitmap(bmp, bitmapWidth, bitmapHeight, true);
        return bmp;
    }


    TextView makeTextView(String label, String text) {
        SpannableStringBuilder str = new SpannableStringBuilder(label + text);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, label.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView tv = new TextView(this);
        tv.setText(str);
        return tv;
    }

    void addRow(TableLayout tbl, TextView tv) {
        TableRow tr = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        tr.addView(tv);
        tbl.addView(tr);
    }

    void updateFavButton() {
        if (spell.isFavorite()) {
            favButton.setImageBitmap(fav_filled);
            //favButton.setImageResource(R.mipmap.star_filled);
            //favButton.setScaleType(ImageView.ScaleType.CENTER);
        } else {
            favButton.setImageBitmap(fav_empty);
            //favButton.setImageResource(R.mipmap.star_empty);
            //favButton.setScaleType(ImageView.ScaleType.CENTER);
        }
    }

    void updateKnownButton() {
        if (spell.isKnown()) {
            knownButton.setImageBitmap(known_filled);
        } else {
            knownButton.setImageBitmap(known_empty);
        }
    }

    void updatePreparedButton() {
        if (spell.isPrepared()) {
            preparedButton.setImageBitmap(prepared_filled);
        } else {
            preparedButton.setImageBitmap(prepared_empty);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK, returnIntent);
        this.finish();
    }


}
